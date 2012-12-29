package com.dsatab.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.dsatab.DSATabApplication;
import com.dsatab.HeroConfiguration;
import com.dsatab.R;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.TalentGroup.MetaTalentType;
import com.dsatab.data.TalentGroup.TalentGroupType;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.BeidhaendigerKampf;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Hand;
import com.dsatab.data.items.HuntingWeapon;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.AuModificator;
import com.dsatab.data.modifier.LeModificator;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.exception.InconsistentDataException;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;
import com.dsatab.view.listener.HeroChangedListener;

public class Hero {

	public interface ItemAddedCallback {
		public void onItemAdded(Item item);

		public void onEquippedItemAdded(EquippedItem item);
	}

	public static final String JAGTWAFFE = "jagtwaffe";
	public static final String PREFIX_NKWAFFE = "nkwaffe";
	public static final String PREFIX_FKWAFFE = "fkwaffe";
	public static final String PREFIX_BK = "bk";

	public static final int MAXIMUM_SET_NUMBER = 3;

	public enum CombatStyle {
		Offensive, Defensive
	}

	private String path, key, name;

	private Uri profileUri;

	private EditableValue experience, freeExperience;

	private Map<AttributeType, Attribute> attributes;
	private Map<String, SpecialFeature> specialFeaturesByName;
	private Map<String, Advantage> advantagesByName;
	private Map<String, Advantage> disadvantagesByName;

	private Map<TalentGroupType, TalentGroup> talentGroups;
	private Map<String, Talent> talentByName;
	private Map<String, Spell> spellsByName;
	private Map<String, Art> artsByName;
	private List<Talent> artTalents;
	private List<Item> items;

	private CombatShieldTalent shieldTalent;

	private Map<Position, ArmorAttribute>[] armorAttributes;
	private Map<Position, WoundAttribute> wounds;

	private List<EquippedItem>[] equippedItems = null;
	private List<Connection> connections = null;

	private HuntingWeapon[] huntingWeapons;

	private List<BeidhaendigerKampf> beidhaendigerKampf = new LinkedList<BeidhaendigerKampf>();

	private List<Modificator> modificators = null;

	LeModificator leModificator;
	AuModificator auModificator;

	private Purse purse = null;

	private int activeSet = 0;

	private List<File> deletableAudioFiles = new LinkedList<File>();

	private HeroBaseInfo baseInfo;

	private HeroConfiguration configuration;

	private List<ChangeEvent> changeEvents;
	// transient
	private Map<Probe, ModifierCache> modifiersCache = new HashMap<Probe, ModifierCache>();

	private Integer oldAuRatioLevel, oldLeRatioLevel, beCache;

	// event listener

	private Set<HeroChangedListener> listener = new HashSet<HeroChangedListener>();

	private static class ModifierCache {
		int mod = Integer.MIN_VALUE;
		int modInclBe = Integer.MIN_VALUE;
		int modInclLEAu = Integer.MIN_VALUE;

		void clear() {
			mod = Integer.MIN_VALUE;
			modInclBe = Integer.MIN_VALUE;
			modInclLEAu = Integer.MIN_VALUE;
		}
	}

	@SuppressWarnings("unchecked")
	public Hero(String path) throws IOException, JSONException {
		this.path = path;

		this.equippedItems = new List[MAXIMUM_SET_NUMBER];
		this.huntingWeapons = new HuntingWeapon[MAXIMUM_SET_NUMBER];

		this.changeEvents = new ArrayList<ChangeEvent>();

		// load modifiers
		this.leModificator = new LeModificator(this);
		this.auModificator = new AuModificator(this);

		this.baseInfo = new HeroBaseInfo();

		// preload values

		experience = new Experience(this, "Abenteuerpunkte");
		experience.setMaximum(100000);

		freeExperience = new EditableValue(this, "Freie Abenteuerpunkte");
		freeExperience.setMaximum(100000);

		this.attributes = new HashMap<AttributeType, Attribute>(AttributeType.values().length);

		this.talentGroups = new HashMap<TalentGroupType, TalentGroup>();
		this.talentByName = new HashMap<String, Talent>();
		this.spellsByName = new HashMap<String, Spell>();
		this.specialFeaturesByName = new TreeMap<String, SpecialFeature>();
		this.artsByName = new TreeMap<String, Art>();
		this.disadvantagesByName = new TreeMap<String, Advantage>();
		this.advantagesByName = new TreeMap<String, Advantage>();
		this.items = new ArrayList<Item>();
		for (int i = 0; i < equippedItems.length; i++) {
			this.equippedItems[i] = new LinkedList<EquippedItem>();
		}
		this.purse = new Purse();
		this.connections = new ArrayList<Connection>();
	}

	/**
	 * 
	 */
	private void fillConfiguration() {
		List<MetaTalentType> metaTalentTypes = new ArrayList<MetaTalentType>(Arrays.asList(MetaTalentType.values()));

		for (MetaTalent metaTalent : getHeroConfiguration().getMetaTalents()) {
			metaTalentTypes.remove(metaTalent.getMetaType());
			addTalent(TalentGroupType.Meta, metaTalent);
		}

		for (MetaTalentType metaType : metaTalentTypes) {
			MetaTalent metaTalent = new MetaTalent(this, metaType);
			addTalent(TalentGroupType.Meta, metaTalent);
		}
	}

	public void loadHeroConfiguration(String defaultConfig) throws IOException, JSONException {

		FileInputStream fis = null;
		try {

			File file = new File(getPath() + ".dsatab");

			if (file.exists() && file.length() > 0) {
				fis = new FileInputStream(file);

				byte[] data = new byte[(int) file.length()];
				fis.read(data);

				JSONObject jsonObject = new JSONObject(new String(data));
				configuration = new HeroConfiguration(this, jsonObject);
			} else {
				if (defaultConfig != null) {
					try {
						JSONObject jsonObject = new JSONObject(defaultConfig);
						configuration = new HeroConfiguration(this, jsonObject);
					} catch (JSONException e) {
						Debug.error(e);
						configuration = null;
					}
				}
			}

			if (configuration == null) {
				configuration = new HeroConfiguration(this);
				configuration.reset();
			}
		} finally {
			if (fis != null)
				fis.close();
		}

		fillConfiguration();
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Talent getArtTalent(String name) {
		for (Talent talent : getArtTalents()) {
			if (talent.getName().startsWith(name))
				return talent;
		}
		return null;

	}

	public HeroBaseInfo getBaseInfo() {
		return baseInfo;
	}

	public List<Talent> getArtTalents() {
		if (artTalents == null) {
			TalentGroup gaben = getTalentGroups().get(TalentGroupType.Gaben);

			artTalents = new ArrayList<Talent>();
			if (gaben != null) {
				for (Talent talent : gaben.getTalents()) {
					if (talent.getName().startsWith(Talent.LITURGIE_KENNTNIS_PREFIX)) {
						artTalents.add(talent);
					} else if (talent.getName().startsWith(Talent.RITUAL_KENNTNIS_PREFIX))
						artTalents.add(talent);
				}
			}

			Talent talent = getTalent(Talent.GEISTER_ANRUFEN);
			if (talent != null)
				artTalents.add(talent);
			talent = getTalent(Talent.GEISTER_BANNEN);
			if (talent != null)
				artTalents.add(talent);
			talent = getTalent(Talent.GEISTER_BINDEN);
			if (talent != null)
				artTalents.add(talent);
			talent = getTalent(Talent.GEISTER_RUFEN);
			if (talent != null)
				artTalents.add(talent);

		}

		return artTalents;
	}

	public void setPortraitUri(Uri uri) {
		this.profileUri = uri;

		for (HeroChangedListener l : listener) {
			l.onPortraitChanged();
		}
	}

	public void setPortraitUri(URI uri) {
		this.profileUri = Uri.parse(uri.toString());

		for (HeroChangedListener l : listener) {
			l.onPortraitChanged();
		}
	}

	public Uri getPortraitUri() {
		return profileUri;
	}

	public Bitmap getPortrait() {
		Bitmap portraitBitmap = null;
		if (getPortraitUri() != null) {

			WindowManager wm = (WindowManager) DSATabApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();

			portraitBitmap = Util.decodeBitmap(getPortraitUri(), display.getWidth());
		}
		return portraitBitmap;
	}

	public void addHeroChangedListener(HeroChangedListener v) {
		listener.add(v);
	}

	public void removeHeroChangedListener(HeroChangedListener v) {
		listener.remove(v);
	}

	public EquippedItem getEquippedItem(String name) {
		return getEquippedItem(activeSet, name);
	}

	public EquippedItem getEquippedItem(int set, String name) {
		if (name == null)
			return null;

		for (EquippedItem item : getEquippedItems(set)) {
			if (name.equals(item.getName()))
				return item;
		}
		return null;

	}

	public EquippedItem getEquippedItem(UUID id) {
		for (int i = 0; i < MAXIMUM_SET_NUMBER; i++) {
			for (EquippedItem equippedItem : getEquippedItems(i)) {
				if (equippedItem.getId().equals(id))
					return equippedItem;
			}
		}
		return null;

	}

	public Purse getPurse() {
		return purse;
	}

	public List<EquippedItem> getEquippedItems(Class<? extends ItemSpecification>... itemClass) {

		List<EquippedItem> items = new LinkedList<EquippedItem>();

		for (EquippedItem ei : getEquippedItems()) {
			ItemSpecification item = ei.getItemSpecification();
			for (Class<? extends ItemSpecification> clazz : itemClass) {
				if (clazz.isAssignableFrom(item.getClass())) {
					items.add(ei);
					break;
				}
			}
		}

		return items;

	}

	public List<EquippedItem> getAllEquippedItems() {
		LinkedList<EquippedItem> items = new LinkedList<EquippedItem>();

		for (int i = 0; i < MAXIMUM_SET_NUMBER; i++) {
			items.addAll(getEquippedItems(i));
		}

		return items;
	}

	public List<EquippedItem> getEquippedItems() {
		return getEquippedItems(activeSet);
	}

	public void setHuntingWeapon(EquippedItem item) {
		if (item.isDistanceWeapon() && huntingWeapons[activeSet] != null) {
			huntingWeapons[activeSet].setNumber(item.getNameId());
		}
	}

	public EquippedItem getHuntingWeapon() {
		if (huntingWeapons[activeSet] != null) {
			Integer number = huntingWeapons[activeSet].getNumber();
			if (number != null && number > 0) {
				return getEquippedItem(activeSet, PREFIX_FKWAFFE + number);
			}
		}
		return null;
	}

	public List<EquippedItem> getEquippedItems(int selectedSet) {
		return equippedItems[selectedSet];
	}

	public boolean hasBeidhaendigerKampf(int set, EquippedItem item1, EquippedItem item2) {

		for (BeidhaendigerKampf bhKampf : beidhaendigerKampf) {

			String bk = bhKampf.getName();

			if (bhKampf.getSet() == set && bk.equals(PREFIX_BK + item1.getNameId() + item2.getNameId()))
				return true;

			if (bhKampf.getSet() == set && bk.equals(PREFIX_BK + item2.getNameId() + item1.getNameId()))
				return true;
		}
		return false;
	}

	public void addBeidhaendigerKampf(EquippedItem item1, EquippedItem item2) {

		if (item1.getSet() != item2.getSet()) {
			throw new IllegalArgumentException("BeidhändigerKampf: Sets of item1 and item2 are not the same:"
					+ item1.toString() + item1.getSet() + " " + item2.toString() + item2.getSet());
		}

		if (hasBeidhaendigerKampf(item1.getSet(), item1, item2))
			return;

		BeidhaendigerKampf bhKampf = new BeidhaendigerKampf(item1, item2);
		bhKampf.setSet(item1.getSet());

		if (item1.getNameId() < item2.getNameId())
			bhKampf.setName(PREFIX_BK + item1.getNameId() + item2.getNameId());
		else
			bhKampf.setName(PREFIX_BK + item2.getNameId() + item1.getNameId());

		beidhaendigerKampf.add(bhKampf);
	}

	public void removeBeidhaendigerKampf(int set, EquippedItem item1, EquippedItem item2) {

		for (BeidhaendigerKampf bhKampf : beidhaendigerKampf) {

			String bk = bhKampf.getName();

			if (bhKampf.getSet() == set
					&& (bk.equals(PREFIX_BK + item1.getNameId() + item2.getNameId()) || bk.equals(PREFIX_BK
							+ item2.getNameId() + item1.getNameId()))) {

				beidhaendigerKampf.remove(bhKampf);
			}

		}

	}

	public String getKey() {
		return key;
	}

	public int getActiveSet() {
		return activeSet;
	}

	public int getNextActiveSet() {
		int set = getActiveSet();
		return (set + 1) % MAXIMUM_SET_NUMBER;
	}

	public void setActiveSet(int activeSet) {

		if (activeSet != this.activeSet) {
			int oldSet = activeSet;
			this.activeSet = activeSet;

			resetBe();

			fireActiveSetChangedEvent(activeSet, oldSet);
		}
	}

	public void fireValueChangedEvent(Value value) {
		if (value instanceof Attribute) {

			Attribute attribute = (Attribute) value;
			Attribute attr;
			switch (attribute.getType()) {
			case Behinderung:
				clearModifiersCache();
				attr = getAttribute(AttributeType.Geschwindigkeit);
				if (attr != null)
					attr.checkBaseValue();
				break;
			case Gewandtheit:
				attr = getAttribute(AttributeType.Geschwindigkeit);
				if (attr != null)
					attr.checkBaseValue();
			case Mut:
			case Klugheit:
			case Intuition:
			case Charisma:
			case Fingerfertigkeit:
			case Konstitution:
			case Körperkraft:
				attr = getAttribute(AttributeType.at);
				if (attr != null)
					attr.setValue(attr.getReferenceValue());

				attr = getAttribute(AttributeType.pa);
				if (attr != null)
					attr.setValue(attr.getReferenceValue());

				attr = getAttribute(AttributeType.fk);
				if (attr != null)
					attr.setValue(attr.getReferenceValue());

				attr = getAttribute(AttributeType.ini);
				if (attr != null)
					attr.setValue(attr.getReferenceValue());

				// check for magic resistance changes:
				attr = getAttribute(AttributeType.Magieresistenz);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Astralenergie_Aktuell);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Astralenergie);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Ausdauer_Aktuell);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Ausdauer);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Lebensenergie_Aktuell);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Lebensenergie);
				if (attr != null)
					attr.checkBaseValue();

				break;
			case Ausdauer_Aktuell:
				postAuRatioCheck();
				break;
			case Lebensenergie_Aktuell:
				postLeRatioCheck();
				break;
			case Lebensenergie:
				attr = getAttribute(AttributeType.Lebensenergie_Aktuell);
				if (attr != null) {
					attr.setReferenceValue(value.getValue());
					attr.checkValue();
					postLeRatioCheck();

				}
				break;
			case Ausdauer:
				attr = getAttribute(AttributeType.Ausdauer_Aktuell);
				if (attr != null) {
					attr.setReferenceValue(value.getValue());
					attr.checkValue();
					postAuRatioCheck();
				}

				break;
			case Astralenergie:
				attr = getAttribute(AttributeType.Astralenergie_Aktuell);
				if (attr != null) {
					attr.setReferenceValue(value.getValue());
					attr.checkValue();
					fireValueChangedEvent(attr);
				}
				break;
			case Karmaenergie:
				attr = getAttribute(AttributeType.Karmaenergie_Aktuell);
				if (attr != null) {
					attr.setReferenceValue(value.getValue());
					attr.checkValue();
					fireValueChangedEvent(attr);
				}
				break;
			default:
				// do nothing
				break;
			}
		}

		for (HeroChangedListener l : listener) {
			l.onValueChanged(value);
		}

	}

	public void fireModifierChangedEvent(Modificator modifier) {
		Debug.trace("ON Modifier changed " + modifier);
		clearModifiersCache();
		for (HeroChangedListener l : listener) {
			l.onModifierChanged(modifier);
		}
	}

	public void onPostHeroLoaded() {
		for (int i = 0; i < equippedItems.length; i++) {
			Util.sort(equippedItems[i]);

			if (huntingWeapons[i] == null) {
				HuntingWeapon huntingWeapon = new HuntingWeapon();
				huntingWeapon.setNumber(0);
				huntingWeapon.setSet(i);
				huntingWeapons[i] = huntingWeapon;

			}
		}

		Collections.sort(connections, Connection.NAME_COMPARATOR);
	}

	public void onPostHeroSaved() {
		for (File f : deletableAudioFiles) {
			f.delete();
		}
	}

	void fireItemAddedEvent(Item item) {
		for (HeroChangedListener l : listener) {
			l.onItemAdded(item);
		}
	}

	void fireItemRemovedEvent(Item item) {
		for (HeroChangedListener l : listener) {
			l.onItemRemoved(item);
		}
	}

	void fireItemEquippedEvent(EquippedItem item) {
		for (HeroChangedListener l : listener) {
			l.onItemEquipped(item);
		}
	}

	public void fireItemChangedEvent(EquippedItem item) {
		for (HeroChangedListener l : listener) {
			l.onItemChanged(item);
		}
	}

	public void fireItemChangedEvent(Item item) {
		for (HeroChangedListener l : listener) {
			l.onItemChanged(item);
		}
	}

	public void fireActiveSetChangedEvent(int newSet, int oldSet) {
		Debug.trace("ON set changed from " + oldSet + " to " + newSet);
		for (HeroChangedListener l : listener) {
			l.onActiveSetChanged(newSet, oldSet);
		}
	}

	void fireItemUnequippedEvent(EquippedItem item) {
		for (HeroChangedListener l : listener) {
			l.onItemUnequipped(item);
		}
	}

	void fireModifierAddedEvent(Modificator modifier) {
		Debug.trace("ON modifier added " + modifier);
		clearModifiersCache();
		getModificators().add(modifier);

		for (HeroChangedListener l : listener) {
			l.onModifierAdded(modifier);
		}
	}

	/**
	 * 
	 */
	private void clearModifiersCache() {
		for (ModifierCache cache : modifiersCache.values()) {
			cache.clear();
		}
	}

	void fireModifierRemovedEvent(Modificator modifier) {
		if (getModificators().remove(modifier)) {
			Debug.trace("ON modifier removed " + modifier);
			clearModifiersCache();

			for (HeroChangedListener l : listener) {
				l.onModifierRemoved(modifier);
			}
		}
	}

	/**
	 * @param item
	 */
	public void removeItem(Item item) {
		items.remove(item);

		fireItemRemovedEvent(item);

		List<EquippedItem> toremove = new ArrayList<EquippedItem>();

		for (int i = 0; i < MAXIMUM_SET_NUMBER; i++) {

			for (EquippedItem equippedItem : getEquippedItems(i)) {

				if (equippedItem.getItem() == null) {
					Debug.warning("Empty EquippedItem found during item delete:" + equippedItem.getName() + " - "
							+ equippedItem.getItem().getName());
					continue;
				}

				if (equippedItem.getItem().equals(item)) {
					toremove.add(equippedItem);
				}
			}

		}

		for (EquippedItem equippedItem : toremove) {
			removeEquippedItem(equippedItem);
		}
	}

	public List<CombatTalent> getAvailableCombatTalents(Weapon weapon) {

		List<CombatTalent> combatTalents = new LinkedList<CombatTalent>();

		for (CombatTalentType ctt : weapon.getCombatTalentTypes()) {
			CombatTalent combatTalent = getCombatTalent(ctt.getName());
			if (combatTalent != null) {
				combatTalents.add(combatTalent);
			}
		}

		return combatTalents;

	}

	public void addItem(Context context, Item item, CombatTalent talent) {
		addItem(context, item, null, talent, getActiveSet(), null);
	}

	public void addItem(Context context, Item item, CombatTalent talent, ItemAddedCallback callback) {
		addItem(context, item, null, talent, getActiveSet(), callback);
	}

	public void addItem(Context context, Item item, CombatTalent talent, int set, ItemAddedCallback callback) {
		addItem(context, item, null, talent, set, callback);
	}

	public void addItem(final Context context, final Item item, ItemSpecification itemSpecification,
			final CombatTalent currentTalent, final int set, final ItemAddedCallback callback) {

		if (itemSpecification == null) {
			if (item.getSpecifications().size() > 1) {

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Wähle ein Variante...");
				builder.setItems(item.getSpecificationNames().toArray(new String[0]),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								addItem(context, item, item.getSpecifications().get(which), currentTalent, set,
										callback);
							}
						});

				builder.show().setCanceledOnTouchOutside(false);
				return;
			} else if (item.getSpecifications().size() == 1) {
				itemSpecification = item.getSpecifications().get(0);
			}
		}

		CombatTalent newTalent = currentTalent;
		if (currentTalent == null) {

			if (itemSpecification instanceof Weapon) {
				Weapon weapon = (Weapon) itemSpecification;

				final List<CombatTalent> combatTalents = getAvailableCombatTalents(weapon);

				if (combatTalents.size() == 1) {
					newTalent = combatTalents.get(0);
				} else if (combatTalents.isEmpty()) {
					Toast.makeText(context, "Es wurde kein verwendbares Talent gefunden.", Toast.LENGTH_LONG).show();
					return;
				} else {
					List<String> talentNames = new ArrayList<String>(combatTalents.size());
					for (CombatTalent combatTalent : combatTalents) {
						talentNames.add(combatTalent.getName());
					}

					final ItemSpecification itemSpec = itemSpecification;
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Wähle ein Talent...");
					builder.setItems(talentNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							addItem(context, item, itemSpec, combatTalents.get(which), set, callback);
						}
					});

					builder.show().setCanceledOnTouchOutside(true);
					return;
				}
			} else if (itemSpecification instanceof DistanceWeapon) {
				DistanceWeapon weapon = (DistanceWeapon) itemSpecification;
				if (weapon.getCombatTalentType() != null) {
					newTalent = getCombatTalent(weapon.getCombatTalentType().getName());
					if (newTalent == null) {
						Toast.makeText(context, "Kein verwendbares Talent gefunden", Toast.LENGTH_LONG).show();
						return;
					}
				}
			}
		}

		addItem(item);

		if (item.isEquipable() && set >= 0) {
			addEquippedItem(context, item, itemSpecification, newTalent, set, callback);
		} else {
			if (callback != null) {
				callback.onItemAdded(item);
			}
		}

	}

	/**
	 * @param item
	 * @return <code>true</code> if item has been added successfully, otherwise
	 *         <code>false</code>
	 */
	public boolean addItem(Item item) {

		// item already added, no need to add again
		if (items.contains(item))
			return false;

		items.add(item);
		fireItemAddedEvent(item);

		return true;
	}

	public void addEquippedItem(final Context context, final Item item, ItemSpecification itemSpecification,
			final CombatTalent talent, final int set, final ItemAddedCallback callback) {

		// if hero does not have item yet, add it first.
		Item heroItem = getItem(item.getId());
		if (heroItem == null) {
			addItem(item);
		}

		EquippedItem equippedItem = new EquippedItem(this, talent, item);
		equippedItem.setSet(set);
		if (itemSpecification != null)
			equippedItem.setItemSpecification(context, itemSpecification);
		else {

			if (item.getSpecifications().size() > 1) {

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Wähle ein Variante...");
				builder.setItems(item.getSpecificationNames().toArray(new String[0]),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								addEquippedItem(context, item, item.getSpecifications().get(which), talent, set,
										callback);
							}
						});

				builder.show().setCanceledOnTouchOutside(false);
				return;
			} else if (item.getSpecifications().size() == 1) {
				itemSpecification = item.getSpecifications().get(0);
			}

		}

		equippedItem = addEquippedItem(context, equippedItem, set);

		if (callback != null) {
			callback.onEquippedItemAdded(equippedItem);
		}

	}

	/**
	 * @param context
	 * @param equippedItem
	 */
	public EquippedItem addEquippedItem(Context context, EquippedItem equippedItem, int set) {
		equippedItem.setSet(set);
		equippedItem.getItemInfo().setScreen(set);

		if (equippedItem.getName() == null) {
			Item item = equippedItem.getItem();
			String namePrefix = null;

			if (item.hasSpecification(Weapon.class)) {
				namePrefix = EquippedItem.NAME_PREFIX_NK;
			}
			if (item.hasSpecification(DistanceWeapon.class)) {
				namePrefix = EquippedItem.NAME_PREFIX_FK;
			}
			if (item.hasSpecification(Shield.class)) {
				namePrefix = EquippedItem.NAME_PREFIX_SCHILD;
			}
			if (item.hasSpecification(Armor.class)) {
				namePrefix = EquippedItem.NAME_PREFIX_RUESTUNG;
			}

			// find first free slot
			int i = 1;
			while (getEquippedItem(set, namePrefix + i) != null) {
				i++;
			}
			equippedItem.setName(namePrefix + i);
		}

		getEquippedItems(set).add(equippedItem);

		if (equippedItem.getItem().hasSpecification(Armor.class)) {
			recalcArmorAttributes(set);
			if (set == activeSet) {
				resetBe();
			}
		}
		fireItemEquippedEvent(equippedItem);

		return equippedItem;
	}

	public String getPath() {
		return path;
	}

	public void setBeCalculation(boolean auto) {
		if (auto != isBeCalculation()) {
			getHeroConfiguration().setBeCalculation(auto);
			if (auto) {
				resetBe();
			}
		}
	}

	public boolean isBeCalculation() {
		return getHeroConfiguration().isBeCalculation();
	}

	public Attribute getAttribute(AttributeType type) {
		Attribute attribute = attributes.get(type);
		return attribute;
	}

	public void addAttribute(Attribute attr) {
		this.attributes.put(attr.getType(), attr);
		if (attr instanceof CustomAttribute) {
			getHeroConfiguration().addAttribute((CustomAttribute) attr);
		}
	}

	public boolean hasAttribute(AttributeType type) {
		return attributes.containsKey(type);
	}

	public Map<Position, ArmorAttribute> getArmorAttributes() {
		return getArmorAttributes(activeSet);
	}

	@SuppressWarnings("unchecked")
	public Map<Position, ArmorAttribute> getArmorAttributes(int set) {
		if (armorAttributes == null) {
			armorAttributes = new HashMap[MAXIMUM_SET_NUMBER];
		}

		if (armorAttributes[set] == null) {

			final List<Position> armorPositions = DSATabApplication.getInstance().getConfiguration()
					.getArmorPositions();

			HashMap<Position, ArmorAttribute> map = new HashMap<Position, ArmorAttribute>(armorPositions.size());

			if (getHeroConfiguration().getArmorAttributes(set) != null) {
				for (ArmorAttribute rs : getHeroConfiguration().getArmorAttributes(set)) {
					if (armorPositions.contains(rs.getPosition())) {
						map.put(rs.getPosition(), rs);
					}
				}
			}

			// fill not existing values with 0
			for (Position pos : armorPositions) {
				ArmorAttribute rs = map.get(pos);

				if (rs == null) {
					rs = new ArmorAttribute(this, pos);
					rs.setValue(getArmorRs(pos));

					getHeroConfiguration().addArmorAttribute(set, rs);
					map.put(pos, rs);
				}
			}

			armorAttributes[set] = map;
		}

		return armorAttributes[set];
	}

	public Map<Position, WoundAttribute> getWounds() {
		if (wounds == null) {

			final List<Position> woundPositions = DSATabApplication.getInstance().getConfiguration()
					.getWoundPositions();

			wounds = new HashMap<Position, WoundAttribute>(woundPositions.size());

			for (WoundAttribute rs : getHeroConfiguration().getWounds()) {
				if (woundPositions.contains(rs.getPosition()))
					wounds.put(rs.getPosition(), rs);
			}

			// fill not existing values with 0
			for (Position pos : woundPositions) {

				WoundAttribute wound = wounds.get(pos);

				if (wound == null) {
					wound = new WoundAttribute(this, pos);
					getHeroConfiguration().addWound(wound);
					wounds.put(pos, wound);
				}
			}

		}
		return wounds;
	}

	public String getName() {
		return name;
	}

	public EditableValue getExperience() {
		return experience;
	}

	public int getLevel() {
		int level = getExperience().getValue() - getFreeExperience().getValue();

		level = level / 1000;

		return level;
	}

	public EditableValue getFreeExperience() {
		return freeExperience;
	}

	protected void postLeRatioCheck() {

		if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_LE_MODIFIER, true)) {
			float newLeRatioCheck = getRatio(AttributeType.Lebensenergie_Aktuell);

			int newLeRatioLevel = 0;

			if (newLeRatioCheck < LeModificator.LEVEL_3)
				newLeRatioLevel = 3;
			else if (newLeRatioCheck < LeModificator.LEVEL_2)
				newLeRatioLevel = 2;
			else if (newLeRatioCheck < LeModificator.LEVEL_1)
				newLeRatioLevel = 1;

			if (oldLeRatioLevel == null || oldLeRatioLevel == 0) {
				if (newLeRatioLevel > 0)
					fireModifierAddedEvent(leModificator);
			} else if (oldLeRatioLevel > 0 && newLeRatioLevel == 0)
				fireModifierRemovedEvent(leModificator);
			else if (oldLeRatioLevel != newLeRatioLevel)
				fireModifierChangedEvent(leModificator);

			oldLeRatioLevel = newLeRatioLevel;
		} else {
			oldLeRatioLevel = null;
			if (getModificators().contains(leModificator)) {
				fireModifierRemovedEvent(leModificator);
			}
		}

	}

	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (BasePreferenceActivity.KEY_HOUSE_RULES_AU_MODIFIER.equals(key)) {
			postAuRatioCheck();
		}

		if (BasePreferenceActivity.KEY_HOUSE_RULES_LE_MODIFIER.equals(key)) {
			postLeRatioCheck();
		}
	}

	protected void postAuRatioCheck() {
		if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_AU_MODIFIER, true)) {

			double newAuRatioCheck = getRatio(AttributeType.Ausdauer_Aktuell);

			int newAuRatioLevel = 0;
			if (newAuRatioCheck < AuModificator.LEVEL_2)
				newAuRatioLevel = 2;
			else if (newAuRatioCheck < AuModificator.LEVEL_1)
				newAuRatioLevel = 1;

			if (oldAuRatioLevel == null || oldAuRatioLevel == 0) {
				if (newAuRatioLevel > 0)
					fireModifierAddedEvent(auModificator);
			} else if (oldAuRatioLevel > 0 && newAuRatioLevel == 0)
				fireModifierRemovedEvent(auModificator);
			else if (oldAuRatioLevel != newAuRatioLevel)
				fireModifierChangedEvent(auModificator);

			oldAuRatioLevel = newAuRatioLevel;
		} else {
			oldAuRatioLevel = null;
			if (getModificators().contains(auModificator)) {
				fireModifierRemovedEvent(auModificator);
			}
		}
	}

	/**
	 * @return
	 */
	public float getRatio(AttributeType type) {
		Attribute le = getAttribute(AttributeType.Lebensenergie_Aktuell);
		if (le != null) {
			Integer value = le.getValue();
			Integer ref = le.getReferenceValue();
			if (value != null && ref != null && ref != 0) {
				return ((float) value) / ref;
			} else {
				return 1.0f;
			}
		} else {
			return 1.0f;
		}

	}

	public int getModifier(Probe probe) {
		return getModifier(probe, true, true);
	}

	public int getModifier(Probe probe, boolean includeBe, boolean includeLeAu) {
		ModifierCache cache = null;

		cache = modifiersCache.get(probe);
		if (cache == null) {
			cache = new ModifierCache();
			modifiersCache.put(probe, cache);
		}

		if (includeBe) {
			if (cache.modInclBe != Integer.MIN_VALUE) {
				return cache.modInclBe;
			}
		}
		if (includeLeAu) {
			if (cache.modInclLEAu != Integer.MIN_VALUE) {
				return cache.modInclLEAu;
			}
		} else {
			if (cache.mod != Integer.MIN_VALUE) {
				return cache.mod;
			}
		}

		int modifier = 0;
		Integer[] outMod = new Integer[1];
		populateModifiers(probe, includeBe, includeLeAu, null, outMod);
		modifier = outMod[0];

		if (includeBe)
			cache.modInclBe = modifier;
		else if (includeLeAu)
			cache.modInclLEAu = modifier;
		else
			cache.mod = modifier;

		return modifier;
	}

	private void populateModifiers(Probe probe, boolean includeBe, boolean includeLeAu, List<Modifier> outModifiers,
			Integer[] outValue) {
		if (outValue != null)
			outValue[0] = 0;

		for (Modificator modificator : getModificators()) {
			if (!includeLeAu && (modificator == leModificator || modificator == auModificator))
				continue;

			Modifier mod = modificator.getModifier(probe);
			if (mod != null && mod.getModifier() != 0) {
				if (outValue != null)
					outValue[0] += mod.getModifier();
				if (outModifiers != null)
					outModifiers.add(mod);
			}
		}

		if (probe instanceof Attribute) {
			Attribute attribute = (Attribute) probe;

			if (attribute.getType() == AttributeType.ini) {
				if (attribute.getHero().hasFeature(SpecialFeature.KAMPFGESPUER))
					if (outValue != null)
						outValue[0] += 2;
				if (outModifiers != null)
					outModifiers.add(new Modifier(2, SpecialFeature.KAMPFGESPUER));

				if (attribute.getHero().hasFeature(SpecialFeature.KAMPFREFLEXE))
					if (outValue != null)
						outValue[0] += 4;
				if (outModifiers != null)
					outModifiers.add(new Modifier(4, SpecialFeature.KAMPFREFLEXE));
			}

		} else if (probe instanceof CombatMeleeTalent || probe instanceof CombatMeleeAttribute) {
			CombatMeleeTalent meleeTalent;
			if (probe instanceof CombatMeleeAttribute)
				meleeTalent = ((CombatMeleeAttribute) probe).getTalent();
			else
				meleeTalent = (CombatMeleeTalent) probe;

			if (meleeTalent.getCombatTalentType() == CombatTalentType.Raufen) {
				// waffenlose kampftechniken +1/+1

				if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
				}
				if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST));
				}
				if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO));
				}
				if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT));
				}
			}

			if (meleeTalent.getCombatTalentType() == CombatTalentType.Ringen) {
				if (hasFeature(SpecialFeature.WK_UNAUER_SCHULE)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.WK_UNAUER_SCHULE));
				}
				if (hasFeature(SpecialFeature.WK_BORNLAENDISCH)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.WK_BORNLAENDISCH));
				}
				if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
				}
			}

		} else if (probe instanceof MetaTalent) {
			MetaTalent metaTalent = (MetaTalent) probe;

			switch (metaTalent.getMetaType()) {
			case PirschAnsitzJagd:
				EquippedItem huntingWeapon = getHuntingWeapon();
				if (huntingWeapon != null) {

					if (huntingWeapon.getItemSpecification() instanceof DistanceWeapon) {

						DistanceWeapon distanceWeapon = (DistanceWeapon) huntingWeapon.getItemSpecification();

						int maxDistance = distanceWeapon.getMaxDistance();
						if (maxDistance <= 20) {
							if (outValue != null)
								outValue[0] += -7;
							if (outModifiers != null)
								outModifiers.add(new Modifier(-7, DSATabApplication.getInstance().getString(
										R.string.modifier_reichweite_jagdwaffe_schritt, 20), ""));
						} else if (maxDistance <= 50) {
							if (outValue != null)
								outValue[0] += -3;
							if (outModifiers != null)
								outModifiers.add(new Modifier(-3, DSATabApplication.getInstance().getString(
										R.string.modifier_reichweite_jagdwaffe_schritt, 50), ""));
						}
					}
				}

				if (hasFeature(SpecialFeature.MEISTERSCHUETZE)) {
					if (outValue != null)
						outValue[0] += 7;
					if (outModifiers != null)
						outModifiers.add(new Modifier(7, SpecialFeature.MEISTERSCHUETZE));
				} else if (hasFeature(SpecialFeature.SCHARFSCHUETZE)) {
					if (outValue != null)
						outValue[0] += 3;
					if (outModifiers != null)
						outModifiers.add(new Modifier(3, SpecialFeature.SCHARFSCHUETZE));
				}
				break;
			case Wache:

				// TODO add erschöpfung

				if (hasFeature(SpecialFeature.AUFMERKSAMKEIT)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.AUFMERKSAMKEIT));
				}
				Talent gefahr = getTalent(Talent.GEFAHRENINSTINKT);
				if (gefahr != null) {
					if (outValue != null)
						outValue[0] += gefahr.getProbeBonus() / 2;
					if (outModifiers != null)
						outModifiers.add(new Modifier(gefahr.getProbeBonus() / 2, DSATabApplication.getInstance()
								.getString(R.string.modifier_wache_gefahreninstinkt), ""));
				}

				Advantage ausdauernd = getAdvantage(Advantage.AUSDAUERND);
				if (ausdauernd != null) {
					if (outValue != null)
						outValue[0] += ausdauernd.getValue() / 3;
					if (outModifiers != null)
						outModifiers.add(new Modifier(ausdauernd.getValue() / 3, DSATabApplication.getInstance()
								.getString(R.string.modifier_wache_ausdauernd), ""));
				}

				if (hasFeature(SpecialFeature.DÄMMERNGSSICHT)) {
					if (outValue != null)
						outValue[0] += 1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.DÄMMERNGSSICHT));
				}

				if (hasFeature(SpecialFeature.NACHTSICHT)) {
					if (outValue != null)
						outValue[0] += +3;
					if (outModifiers != null)
						outModifiers.add(new Modifier(3, SpecialFeature.NACHTSICHT));
				}

				if (hasFeature(SpecialFeature.HERRAUSRAGENDER_SINN)) {
					if (outValue != null)
						outValue[0] += +1;
					if (outModifiers != null)
						outModifiers.add(new Modifier(1, SpecialFeature.HERRAUSRAGENDER_SINN));
				}

				if (hasFeature(SpecialFeature.EINÄUGIG)) {
					if (outValue != null)
						outValue[0] += -2;
					if (outModifiers != null)
						outModifiers.add(new Modifier(-2, SpecialFeature.EINÄUGIG));
				}
				if (hasFeature(SpecialFeature.EINBILDUNGEN)) {
					if (outValue != null)
						outValue[0] += -2;
					if (outModifiers != null)
						outModifiers.add(new Modifier(-2, SpecialFeature.EINBILDUNGEN));
				}
				if (hasFeature(SpecialFeature.DUNKELANGST)) {
					if (outValue != null)
						outValue[0] += -3;
					if (outModifiers != null)
						outModifiers.add(new Modifier(-3, SpecialFeature.DUNKELANGST));
				}

				if (hasFeature(SpecialFeature.NACHTBLIND)) {
					if (outValue != null)
						outValue[0] += -3;
					if (outModifiers != null)
						outModifiers.add(new Modifier(-3, SpecialFeature.NACHTBLIND));
				}
				if (hasFeature(SpecialFeature.UNSTET)) {
					if (outValue != null)
						outValue[0] += -2;
					if (outModifiers != null)
						outModifiers.add(new Modifier(-2, SpecialFeature.UNSTET));
				}
				break;
			default:
				// do nothing
				break;
			}

		} else if (probe instanceof CombatProbe) {
			CombatProbe combatProbe = (CombatProbe) probe;
			EquippedItem equippedItem = combatProbe.getEquippedItem();

			if (equippedItem != null) {
				if (equippedItem.getItemSpecification() instanceof Weapon) {
					Item item = equippedItem.getItem();
					Weapon weapon = (Weapon) equippedItem.getItemSpecification();

					int kkModifier = weapon.getKKModifier(getModifiedValue(AttributeType.Körperkraft, true, true));

					if (kkModifier < 0) {
						if (outValue != null)
							outValue[0] += kkModifier;
						if (outModifiers != null)
							outModifiers.add(new Modifier(kkModifier, DSATabApplication.getInstance().getString(
									R.string.modifier_waffe_geringe_kk), ""));
					}

					if (combatProbe.isAttack()) {
						if (weapon.getWmAt() != null && weapon.getWmAt() != 0)
							if (outValue != null)
								outValue[0] += weapon.getWmAt();
						if (outModifiers != null)
							outModifiers.add(new Modifier(weapon.getWmAt(), DSATabApplication.getInstance().getString(
									R.string.modifier_waffe_waffenmodifikator_at)));
					} else {
						if (weapon.getWmPa() != null && weapon.getWmPa() != 0)
							if (outValue != null)
								outValue[0] += weapon.getWmPa();
						if (outModifiers != null)
							outModifiers.add(new Modifier(weapon.getWmPa(), DSATabApplication.getInstance().getString(
									R.string.modifier_waffe_waffenmodifikator_pa)));
					}

					BaseCombatTalent talent = (BaseCombatTalent) equippedItem.getTalent();
					if (talent != null && talent.getTalentSpezialisierung() != null
							&& talent.getTalentSpezialisierung().equalsIgnoreCase(item.getName())) {

						if (outValue != null)
							outValue[0] += 1;
						if (outModifiers != null)
							outModifiers.add(new Modifier(1, SpecialFeature.TALENTSPEZIALISIERUNG_PREFIX + " "
									+ talent.getTalentSpezialisierung()));
					}

					// waffenlose kampftechniken +1/+1
					if (item.getName().startsWith("Raufen")) {
						if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
						}
						if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST));
						}
						if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO));
						}
						if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT));
						}
					}

					if (item.getName().startsWith("Hruruzat")) {
						if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
						}
						if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST));
						}
						if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO));
						}
						if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT));
						}
					}

					if (item.getName().startsWith("Ringen")) {
						if (hasFeature(SpecialFeature.WK_UNAUER_SCHULE)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_UNAUER_SCHULE));
						}
						if (hasFeature(SpecialFeature.WK_BORNLAENDISCH)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_BORNLAENDISCH));
						}
						if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
							if (outValue != null)
								outValue[0] += 1;
							if (outModifiers != null)
								outModifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
						}
					}

					addModForBeidhändigerKampf(outModifiers, outValue, equippedItem);

					// modify weapon attack with shield wmAt modifier if second
					// weapon is shield
					if (combatProbe.isAttack()) {
						EquippedItem equippedShield = equippedItem.getSecondaryItem();
						if (equippedShield != null && equippedShield.getItemSpecification() instanceof Shield) {
							Shield shield = (Shield) equippedShield.getItemSpecification();
							if (outValue != null)
								outValue[0] += shield.getWmAt();
							if (outModifiers != null)
								outModifiers.add(new Modifier(shield.getWmAt(), DSATabApplication.getInstance()
										.getString(R.string.modifier_schildkampf_modifikator_at)));
						}

					}
				} else if (equippedItem.getItemSpecification() instanceof Shield) {
					Shield shield = (Shield) equippedItem.getItemSpecification();

					if (combatProbe.isAttack()) {
						if (outValue != null)
							outValue[0] += shield.getWmAt();
						if (outModifiers != null)
							outModifiers.add(new Modifier(shield.getWmAt(), DSATabApplication.getInstance().getString(
									R.string.modifier_schildmodifikator_at)));
					} else {
						if (outValue != null)
							outValue[0] += shield.getWmPa();
						if (outModifiers != null)
							outModifiers.add(new Modifier(shield.getWmPa(), DSATabApplication.getInstance().getString(
									R.string.modifier_schildmodifikator_pa)));

						// paradevalue is increased by 1 if weaponparade is
						// above
						// 15
						if (shield.isShield()) {
							EquippedItem equippedWeapon = equippedItem.getSecondaryItem();

							if (equippedWeapon != null) {
								int defenseValue = 0;
								if (equippedWeapon.getTalent().getDefense() != null) {
									defenseValue = equippedWeapon.getTalent().getDefense().getProbeValue(0);
								}

								if (defenseValue >= 21) {
									if (outValue != null)
										outValue[0] += 3;
									if (outModifiers != null)
										outModifiers.add(new Modifier(3, DSATabApplication.getInstance().getString(
												R.string.modifier_shield_hauptwaffe_paradewert, defenseValue, "+3")));
								} else if (defenseValue >= 18) {
									if (outValue != null)
										outValue[0] += 2;
									if (outModifiers != null)
										outModifiers.add(new Modifier(2, DSATabApplication.getInstance().getString(
												R.string.modifier_shield_hauptwaffe_paradewert, defenseValue, "+2")));
								} else if (defenseValue >= 15) {
									if (outValue != null)
										outValue[0] += 1;
									if (outModifiers != null)
										outModifiers.add(new Modifier(1, DSATabApplication.getInstance().getString(
												R.string.modifier_shield_hauptwaffe_paradewert, defenseValue, "+1")));
								}
							}
						}
					}
				} else if (equippedItem.getItemSpecification() instanceof DistanceWeapon) {
					Item item = equippedItem.getItem();
					// DistanceWeapon distanceWeapon = (DistanceWeapon)
					// equippedItem.getItemSpecification();

					BaseCombatTalent talent = (BaseCombatTalent) equippedItem.getTalent();
					if (talent != null && talent.getTalentSpezialisierung() != null
							&& talent.getTalentSpezialisierung().equalsIgnoreCase(item.getName())) {
						if (outValue != null)
							outValue[0] += 2;
						if (outModifiers != null)
							outModifiers.add(new Modifier(2, SpecialFeature.TALENTSPEZIALISIERUNG_PREFIX + " "
									+ talent.getTalentSpezialisierung()));

					}

					if (hasFeature(Advantage.ENTFERNUNGSSINN)) {
						if (outValue != null)
							outValue[0] += 2;
						if (outModifiers != null)
							outModifiers.add(new Modifier(2, Advantage.ENTFERNUNGSSINN));
					}
				}
			}

		}

		if (includeBe) {
			int heroBe = getBe(probe);
			if (heroBe != 0) {

				if (outValue != null)
					outValue[0] += -1 * heroBe;
				if (outModifiers != null)
					outModifiers.add(new Modifier(-1 * heroBe, DSATabApplication.getInstance().getString(
							R.string.modifier_behinderung, probe.getProbeInfo().getBe())));
			}
		}

	}

	public List<Modifier> getModifiers(Probe probe, boolean includeBe, boolean includeLeAu) {
		List<Modifier> modifiers = new LinkedList<Modifier>();
		populateModifiers(probe, includeBe, includeLeAu, modifiers, null);
		return modifiers;
	}

	/**
	 * Adds modifiers for lefthanded battle.
	 * 
	 * @param outModifiers
	 * @param equippedItem
	 */
	private void addModForBeidhändigerKampf(List<Modifier> outModifiers, Integer[] outValue, EquippedItem equippedItem) {
		// check for beidhändiger kampf
		if (equippedItem.getHand() == Hand.links) {
			// EquippedItem equippedSecondaryWeapon =
			// equippedItem.getSecondaryItem();
			// if (equippedSecondaryWeapon != null &&
			// equippedSecondaryWeapon.getItem().hasSpecification(Weapon.class))
			// {
			int m = 0;
			if (!hasFeature(Advantage.BEIDHAENDIG)) {
				m = -9;
				if (hasFeature(SpecialFeature.LINKHAND))
					m += 3;
				if (hasFeature(SpecialFeature.BEIDHAENDIGER_KAMPF_1))
					m += 3;
				if (hasFeature(SpecialFeature.BEIDHAENDIGER_KAMPF_2))
					m += 3;
			}

			if (outValue != null)
				outValue[0] += m;
			if (outModifiers != null) {
				outModifiers
						.add(new Modifier(m, "Beidhändigerkampf Links",
								"Beim Beidhändigenkampf bekommt man je nach Sonderfertigkeiten bei Aktionen mit der linken Hand Abzüge."));
			}
			// }
		}
	}

	public Integer getModifiedValue(AttributeType type, boolean includeBe, boolean includeLeAu) {
		Attribute attr = getAttribute(type);
		if (attr == null || attr.getValue() == null)
			return null;

		return attr.getValue() + getModifier(attr, includeBe, includeLeAu);
	}

	public Integer getAttributeValue(AttributeType type) {
		Attribute attribute = getAttribute(type);

		if (attribute != null)
			return attribute.getValue();
		else
			return null;
	}

	public Map<String, SpecialFeature> getSpecialFeatures() {
		return specialFeaturesByName;
	}

	public void addSpecialFeature(SpecialFeature specialFeature) {
		specialFeaturesByName.put(specialFeature.getName(), specialFeature);
	}

	public Map<String, Advantage> getAdvantages() {
		return advantagesByName;
	}

	public Advantage getDisadvantage(String name) {
		return disadvantagesByName.get(name);
	}

	public Map<String, Advantage> getDisadvantages() {
		return disadvantagesByName;
	}

	public Advantage getAdvantage(String name) {
		return advantagesByName.get(name);
	}

	public void addConnection(Connection connection) {
		getConnections().add(connection);
		Collections.sort(getConnections(), Connection.NAME_COMPARATOR);
	}

	public void removeConnection(Connection connection) {
		getConnections().remove(connection);
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public SpecialFeature getSpecialFeature(String name) {
		return specialFeaturesByName.get(name);
	}

	public boolean hasFeature(String name) {
		boolean found = false;

		found = specialFeaturesByName.containsKey(name);

		if (!found) {
			found = advantagesByName.containsKey(name);
		}

		if (!found) {
			found = disadvantagesByName.containsKey(name);
		}

		return found;

	}

	public void addChangeEvent(ChangeEvent event) {
		changeEvents.add(event);
	}

	public void addEvent(Event event) {
		getHeroConfiguration().addEvent(event);
		Collections.sort(getHeroConfiguration().getEvents(), Event.COMPARATOR);
	}

	public List<Event> getEvents() {
		return getHeroConfiguration().getEvents();
	}

	/**
	 * @param probe
	 * @return
	 */
	public int getBe(Probe probe) {
		int heroBe = 0;

		if (probe != null && probe.getProbeInfo().getBe() != null) {

			// base hero be
			heroBe = getAttributeValue(AttributeType.Behinderung);

			heroBe = Math.abs(probe.getProbeInfo().getBe(0, heroBe));

			boolean isAttack = false;
			boolean isDefense = false;
			boolean halfBe = false;
			if (probe instanceof Attribute) {
				Attribute attribute = (Attribute) probe;
				if (attribute.getType() == AttributeType.at) {
					halfBe = true;
					isAttack = true;
				} else if (attribute.getType() == AttributeType.pa) {
					halfBe = true;
					isDefense = true;
				}

				if (attribute.getType() == AttributeType.Geschwindigkeit) {
					// WdH274
					if (hasFeature(SpecialFeature.ZWERGENWUCHS)) {
						halfBe = true;
					}
				}
			}

			if (probe instanceof CombatMeleeAttribute) {
				CombatMeleeAttribute meleeAttr = (CombatMeleeAttribute) probe;
				isAttack = meleeAttr.isAttack();
				isDefense = !meleeAttr.isAttack();
				halfBe = true;
			} else if (probe instanceof CombatProbe) {
				CombatProbe combatProbe = (CombatProbe) probe;
				isAttack = combatProbe.isAttack();
				isDefense = !combatProbe.isAttack();

				if (combatProbe.getCombatTalent() instanceof CombatDistanceTalent)
					halfBe = false;
				else
					halfBe = true;
			}

			if (halfBe) {
				if (getCombatStyle() == CombatStyle.Offensive && isAttack) {
					heroBe = (int) Math.floor(heroBe / 2.0);
				} else if (getCombatStyle() == CombatStyle.Defensive && isDefense) {
					heroBe = (int) Math.floor(heroBe / 2.0);
				} else {
					heroBe = (int) Math.ceil(heroBe / 2.0);
				}
			}

		}
		return heroBe;
	}

	public int[] getWundschwelle() {
		int[] ws = new int[3];

		SharedPreferences preferences = DSATabApplication.getPreferences();
		int wsBase = 0;
		int wsMod = 0;
		if (preferences.getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_EASIER_WOUNDS, false)) {
			wsBase = (int) Math.ceil(getAttributeValue(AttributeType.Konstitution) / 3.0f);
			wsMod = 0;

			if (hasFeature(SpecialFeature.EISERN))
				wsMod += 1;
			if (hasFeature(SpecialFeature.GLASKNOCHEN))
				wsMod -= 1;

			ws[0] = wsBase + wsMod;
			ws[1] = (wsBase * 2) + wsMod;
			ws[2] = getAttributeValue(AttributeType.Konstitution) + wsMod;

		} else {
			wsBase = (int) Math.ceil(getAttributeValue(AttributeType.Konstitution) / 2.0f);
			wsMod = 0;

			if (hasFeature(SpecialFeature.EISERN))
				wsMod += 2;

			if (hasFeature(SpecialFeature.GLASKNOCHEN))
				wsMod -= 2;

			ws[0] = wsBase + wsMod;
			ws[1] = getAttributeValue(AttributeType.Konstitution) + wsMod;
			ws[2] = getAttributeValue(AttributeType.Konstitution) + wsBase + wsMod;
		}

		return ws;

	}

	public int getArmorBe() {

		if (beCache == null) {

			float be = 0.0f;

			String rs1Armor = null;
			if (hasFeature(SpecialFeature.RUESTUNGSGEWOEHNUNG_3)) {
				be -= 2.0;
			} else if (hasFeature(SpecialFeature.RUESTUNGSGEWOEHNUNG_2)) {
				be -= 1.0;
			} else {
				SpecialFeature rs1 = getSpecialFeature(SpecialFeature.RUESTUNGSGEWOEHNUNG_1);
				if (rs1 != null) {
					rs1Armor = rs1.getParameter1();
				}
			}

			switch (DSATabApplication.getInstance().getConfiguration().getArmorType()) {

			case ZonenRuestung: {

				int stars = 0;
				float totalRs = 0;

				for (EquippedItem equippedItem : getEquippedItems()) {

					if (equippedItem.getItemSpecification() instanceof Armor) {
						Armor armor = (Armor) equippedItem.getItemSpecification();
						stars += armor.getStars();

						if (rs1Armor != null && rs1Armor.equals(equippedItem.getItem().getName())) {
							be -= 1.0;
							rs1Armor = null;
						}

						for (int i = 0; i < Position.ARMOR_POSITIONS.size(); i++) {
							float armorRs = armor.getRs(Position.ARMOR_POSITIONS.get(i));

							if (armor.isZonenHalfBe())
								armorRs = armorRs / 2.0f;

							totalRs += (armorRs * Position.ARMOR_POSITIONS_MULTIPLIER[i]);
						}
					}
				}

				totalRs = (float) Math.ceil(totalRs / 20);
				be += (totalRs - stars);
				break;

			}

			case GesamtRuestung: {

				for (EquippedItem equippedItem : getEquippedItems()) {
					ItemSpecification itemSpec = equippedItem.getItemSpecification();
					if (itemSpec instanceof Armor) {
						Armor armor = (Armor) itemSpec;
						if (armor.getTotalPieces() > 1) {
							be += (armor.getTotalBe() / armor.getTotalPieces());
						} else {
							be += armor.getTotalBe();
						}

						if (rs1Armor != null && rs1Armor.equals(equippedItem.getItem().getName())) {
							be -= 1.0;
							rs1Armor = null;
						}
					}
				}
				// be in gesamtrüstung is being rounded
				be = Math.round(be);
				break;

			}
			}
			beCache = Math.max(0, (int) Math.ceil(be));
		}
		return beCache;

	}

	/**
	 * A general overall Rs value calculated using the zone system sum with
	 * multipliers
	 * 
	 * @return
	 */
	public int getArmorRs() {

		float totalRs = 0;

		switch (DSATabApplication.getInstance().getConfiguration().getArmorType()) {

		case ZonenRuestung:
			for (int i = 0; i < Position.ARMOR_POSITIONS.size(); i++) {
				totalRs += (getArmorRs(Position.ARMOR_POSITIONS.get(i)) * Position.ARMOR_POSITIONS_MULTIPLIER[i]);
			}
			totalRs = (int) Math.round(totalRs / 20.0);
			break;
		case GesamtRuestung:
			for (EquippedItem equippedItem : getEquippedItems()) {
				ItemSpecification itemSpec = equippedItem.getItemSpecification();
				if (itemSpec instanceof Armor) {
					Armor armor = (Armor) itemSpec;
					if (armor.getTotalPieces() > 1)
						totalRs += (((float) armor.getTotalRs()) / armor.getTotalPieces());
					else
						totalRs += armor.getTotalRs();
				}
			}

			Advantage natRs = getAdvantage(Advantage.NATUERLICHER_RUESTUNGSSCHUTZ);
			if (natRs != null && natRs.getValue() != null) {
				totalRs += natRs.getValue();
			}

			break;
		}
		return (int) Math.ceil(totalRs);
	}

	public List<EquippedItem> getArmor(Position pos) {
		List<EquippedItem> items = new LinkedList<EquippedItem>();

		for (EquippedItem equippedItem : getEquippedItems()) {
			Item item = equippedItem.getItem();
			Armor armor = (Armor) item.getSpecification(Armor.class);
			if (armor != null && armor.getRs(pos) > 0) {
				items.add(equippedItem);
			}
		}

		return items;
	}

	public int getArmorRs(Position pos) {

		int rs = 0;
		for (EquippedItem equippedItem : getEquippedItems()) {
			Item item = equippedItem.getItem();
			Armor armor = (Armor) item.getSpecification(Armor.class);
			if (armor != null) {
				rs += armor.getRs(pos);
			}
		}

		Advantage natRs = getAdvantage(Advantage.NATUERLICHER_RUESTUNGSSCHUTZ);
		if (natRs != null && natRs.getValue() != null)
			rs += natRs.getValue();

		return rs;
	}

	public List<Item> getItems() {
		return items;
	}

	/**
	 * 
	 */

	public Talent getTalent(String talentName) {
		return talentByName.get(talentName);
	}

	public Spell getSpell(String spellName) {
		return spellsByName.get(spellName);
	}

	public void addSpell(Spell spell) {
		spellsByName.put(spell.getName(), spell);
	}

	public Map<TalentGroupType, TalentGroup> getTalentGroups() {
		return talentGroups;
	}

	public CombatStyle getCombatStyle() {
		return getHeroConfiguration().getCombatStyle();
	}

	public void setCombatStyle(CombatStyle style) {
		getHeroConfiguration().setCombatStyle(style);
	}

	public CombatShieldTalent getCombatShieldTalent() {
		if (shieldTalent == null) {
			shieldTalent = new CombatShieldTalent(this);
		}
		return shieldTalent;
	}

	public CombatShieldTalent getCombatParadeWeaponTalent(EquippedItem paradeItem) {
		return new CombatParadeWeaponTalent(this, paradeItem);
	}

	public Art getArt(String name) {
		return artsByName.get(name);
	}

	public Map<String, Art> getArts() {
		return artsByName;
	}

	public Map<String, Spell> getSpells() {
		return spellsByName;
	}

	public Item getItem(String name, String slot) {
		for (Item item : getItems()) {
			if (item.getName().equals(name)) {
				if (slot != null) {
					if (slot.equals(item.getSlot()))
						return item;
				} else {
					return item;
				}
			}
		}
		return null;
	}

	public Item getItem(String name) {
		return getItem(name, null);
	}

	public Item getItem(UUID id) {
		for (Item item : getItems()) {
			if (item.getId().equals(id)) {
				return item;
			}
		}
		return null;
	}

	public BaseCombatTalent getCombatTalent(String name) {
		Talent talent = talentByName.get(name);

		if (talent == null) {

			Debug.verbose("Couldn't find " + name + " trying combattalents");
			// add missing combat talents with a value of base.
			CombatTalentType talentType = CombatTalentType.byName(name);
			if (talentType != null) {
				if (talentType.isFk()) {
					// TODO what shall be do in such a case???
				} else {

					CombatMeleeAttribute at = new CombatMeleeAttribute(this);
					at.setName(CombatMeleeAttribute.ATTACKE);
					at.setValue(getAttributeValue(AttributeType.at));

					CombatMeleeAttribute pa = new CombatMeleeAttribute(this);
					pa.setName(CombatMeleeAttribute.PARADE);
					pa.setValue(getAttributeValue(AttributeType.pa));

					talent = new CombatMeleeTalent(this, at, pa);
					talent.setValue(0);
					talent.setName(name);

					if (talent.getProbeInfo().getBe() != null) {
						talent.setProbeBe(talent.getProbeInfo().getBe());
					}
				}
			}
		}

		if (talent instanceof BaseCombatTalent)
			return (BaseCombatTalent) talent;
		else
			return null;
	}

	public void removeEvent(Event event) {
		if (event.getAudioPath() != null) {

			File audioFile = new File(event.getAudioPath());
			if (audioFile.exists())
				deletableAudioFiles.add(audioFile);
		}

		getHeroConfiguration().removeEvent(event);
	}

	public void removeEquippedItem(EquippedItem equippedItem) {

		if (equippedItem.getSecondaryItem() != null) {
			equippedItem.getSecondaryItem().setSecondaryItem(null);
		}

		int set = equippedItem.getSet();
		getEquippedItems(set).remove(equippedItem);

		if (equippedItem.getItem().hasSpecification(Armor.class)) {
			recalcArmorAttributes(set);
			if (set == activeSet) {
				resetBe();
			}
		}

		fireItemUnequippedEvent(equippedItem);
	}

	/**
	 * 
	 */
	private void resetBe() {

		if (isBeCalculation()) {
			int oldBe = getAttributeValue(AttributeType.Behinderung);

			beCache = null;
			if (oldBe != getArmorBe()) {
				getAttribute(AttributeType.Behinderung).setValue(getArmorBe());
			}
		} else {
			beCache = null;
		}
	}

	public void addModificator(CustomModificator modificator) {
		getHeroConfiguration().addModificator(modificator);
		fireModifierAddedEvent(modificator);
	}

	public void removeModificator(CustomModificator modificator) {
		getHeroConfiguration().removeModificator(modificator);
		fireModifierRemovedEvent(modificator);
	}

	public List<Modificator> getModificators() {

		// init modifiers
		if (modificators == null) {
			modificators = new LinkedList<Modificator>();

			if (getRatio(AttributeType.Lebensenergie_Aktuell) < LeModificator.LEVEL_1)
				modificators.add(leModificator);

			if (getRatio(AttributeType.Ausdauer_Aktuell) < AuModificator.LEVEL_1)
				modificators.add(auModificator);

			for (WoundAttribute attr : getWounds().values()) {
				if (attr.getValue() > 0)
					modificators.add(attr);
			}

			// add custom modificators
			modificators.addAll(getHeroConfiguration().getModificators());
		}
		return modificators;

	}

	public void reloadArmorAttributes() {
		armorAttributes = null;
	}

	public void recalcArmorAttributes(int set) {
		for (ArmorAttribute a : getArmorAttributes(set).values()) {
			a.recalcValue();
		}
	}

	public HeroConfiguration getHeroConfiguration() {
		return configuration;
	}

	/**
	 * @param weapon
	 * @return
	 */
	public int getModifierTP(EquippedItem weapon) {
		int modifierTP = 0;

		Modifier mod = null;
		for (CustomModificator modificator : getHeroConfiguration().getModificators()) {
			mod = modificator.getModifier(weapon);

			if (mod != null)
				modifierTP += mod.getModifier();
		}
		return modifierTP;
	}

	/**
	 * @param uri
	 */
	public void setProfileUri(Uri uri) {
		this.profileUri = uri;
	}

	/**
	 * @param talent
	 */
	public void addTalent(TalentGroupType type, Talent talent) {
		if (type != null) {
			TalentGroup tg = talentGroups.get(type);
			if (tg != null) {
				tg.getTalents().add(talent);
			} else {
				tg = new TalentGroup(type);
				tg.getTalents().add(talent);
				talentGroups.put(type, tg);
			}
		}
		talentByName.put(talent.getName(), talent);

		if (talent instanceof MetaTalent) {
			getHeroConfiguration().addMetaTalent((MetaTalent) talent);
		}
	}

	/**
	 * @param art
	 */
	public void addArt(Art art) {
		artsByName.put(art.getName(), art);
	}

	/**
	 * @param adv
	 */
	public void addAdvantage(Advantage adv) {
		Map<String, Advantage> targetList;

		if (Advantage.isNachteil(adv.getName())) {
			targetList = disadvantagesByName;
		} else if (Advantage.isVorteil(adv.getName()))
			targetList = advantagesByName;
		else {
			Debug.warning("Not recognised value: " + adv.getName());
			return;
		}
		Advantage existingAdv = targetList.get(adv.getName());
		if (existingAdv == null) {
			targetList.put(adv.getName(), adv);
		} else {
			existingAdv.addValue(adv.getValueAsString());
		}

	}

	/**
	 * @param equippedItem
	 */
	public void addEquippedItem(EquippedItem equippedItem) {
		equippedItems[equippedItem.getSet()].add(equippedItem);
	}

	/**
	 * @param huntingWeapon
	 */
	public void addHuntingWeapon(HuntingWeapon huntingWeapon) {
		if (huntingWeapon != null && huntingWeapon.getSet() != null) {
			huntingWeapons[huntingWeapon.getSet()] = huntingWeapon;
		} else {
			BugSenseHandler.sendException(new InconsistentDataException(
					"Setting hunting weapon with set null not possible: " + huntingWeapon));
		}
	}

	/**
	 * @return
	 */
	public Collection<? extends BeidhaendigerKampf> getBeidhaendigerKampfs() {
		return beidhaendigerKampf;
	}

	/**
	 * @param i
	 * @return
	 */
	public HuntingWeapon getHuntingWeapons(int i) {
		return huntingWeapons[i];
	}

	/**
	 * @return
	 */
	public List<ChangeEvent> getChangeEvents() {
		return changeEvents;
	}

}
