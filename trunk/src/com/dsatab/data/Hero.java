package com.dsatab.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jdom.Element;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.HeroConfiguration;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.common.Util;
import com.dsatab.data.MetaTalent.MetaTalentType;
import com.dsatab.data.Talent.Flags;
import com.dsatab.data.TalentGroup.TalentGroupType;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.EventCategory;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Hand;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.ItemType;
import com.dsatab.data.items.MiscSpecification;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.AuModificator;
import com.dsatab.data.modifier.LeModificator;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.util.Debug;
import com.dsatab.view.listener.HeroChangedListener;
import com.dsatab.xml.DataManager;
import com.dsatab.xml.DomUtil;
import com.dsatab.xml.Xml;

public class Hero {

	private static final String JAGTWAFFE = "jagtwaffe";

	private static final String EVENT_NOTE_TEXT = "Sonstiges Ereignis (Hinzugewinn)";

	private static final String PREFIX_NKWAFFE = "nkwaffe";
	private static final String PREFIX_FKWAFFE = "fkwaffe";

	private static final String PREFIX_BK = "bk";

	public static final int MAXIMUM_SET_NUMBER = 3;

	public enum CombatStyle {
		Offensive, Defensive
	}

	private String path;

	private org.jdom.Document dom;

	private Element ereignisse;

	private Element held;

	private EditableValue experience, freeExperience;

	private Map<AttributeType, Attribute> attributes;

	private Map<String, SpecialFeature> specialFeatures;
	private Map<String, Advantage> advantages;
	private Map<String, Advantage> disadvantages;

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

	private Element[] huntingWeapons;

	private List<Element> beidhaendigerKampfElements = new LinkedList<Element>();

	private List<Modificator> modificators = null;

	LeModificator leModificator;
	AuModificator auModificator;

	private Purse purse = null;

	private boolean notizElementFilled = false;

	private List<HeroChangedListener> listener = new LinkedList<HeroChangedListener>();

	private int activeSet = 0;

	private List<File> deletableAudioFiles = new LinkedList<File>();

	private Integer oldAuRatioLevel, oldLeRatioLevel, beCache;

	private Element talentsNode, spellsNode, equippmentNode, itemsNode, basisNode, attributesNode;

	private HeroBaseInfo baseInfo;

	private HeroConfiguration configuration;

	// transient
	private Map<Probe, ModifierCache> modifiersCache = new HashMap<Probe, ModifierCache>();

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
	public Hero(String path, org.jdom.Document dom, HeroLoader heroLoader) {
		this.path = path;
		this.dom = dom;
		this.attributes = new HashMap<AttributeType, Attribute>(AttributeType.values().length);
		this.equippedItems = new List[MAXIMUM_SET_NUMBER];
		this.huntingWeapons = new Element[MAXIMUM_SET_NUMBER];

		// check for valid hero node
		if (getHeldElement() == null) {
			throw new DsaTabRuntimeException("Invalid Hero xml file, could not find <" + Xml.KEY_HELD
					+ "> element with in root node");
		}

		talentsNode = getHeldElement().getChild(Xml.KEY_TALENTLISTE);
		spellsNode = getHeldElement().getChild(Xml.KEY_ZAUBERLISTE);

		equippmentNode = getHeldElement().getChild(Xml.KEY_AUSRUESTUNGEN_UE);
		if (equippmentNode != null) {
			// for newer android versions rename ausrüstung back to ü
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
				equippmentNode.setName(Xml.KEY_AUSRUESTUNGEN);
		} else {
			equippmentNode = getHeldElement().getChild(Xml.KEY_AUSRUESTUNGEN);
		}

		itemsNode = getHeldElement().getChild(Xml.KEY_GEGENSTAENDE_AE);
		if (itemsNode != null) {
			itemsNode.setName(Xml.KEY_GEGENSTAENDE);
		} else {
			itemsNode = getHeldElement().getChild(Xml.KEY_GEGENSTAENDE);
		}

		attributesNode = getHeldElement().getChild(Xml.KEY_EIGENSCHAFTEN);
		basisNode = getHeldElement().getChild(Xml.KEY_BASIS);

		// load modifiers
		this.leModificator = new LeModificator(this);
		this.auModificator = new AuModificator(this);

		if (basisNode != null)
			baseInfo = new HeroBaseInfo(basisNode);
		else
			baseInfo = new HeroBaseInfo(getHeldElement());

		// preload items
		getItems();
	}

	public void setHeroConfiguration(HeroConfiguration configuration) {
		this.configuration = configuration;
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
		getHeldElement().setAttribute(Xml.KEY_PORTRAIT_PATH, uri.toString());

		for (HeroChangedListener l : listener) {
			l.onPortraitChanged();
		}
	}

	public void setPortraitUri(URI uri) {
		getHeldElement().setAttribute(Xml.KEY_PORTRAIT_PATH, uri.toString());

		for (HeroChangedListener l : listener) {
			l.onPortraitChanged();
		}
	}

	public Uri getPortraitUri() {

		Uri uri = null;
		if (getHeldElement().getAttribute(Xml.KEY_PORTRAIT_PATH) != null) {
			uri = Uri.parse(getHeldElement().getAttributeValue(Xml.KEY_PORTRAIT_PATH));
		}

		return uri;

	}

	public Bitmap getPortrait() {

		Bitmap portraitBitmap = null;

		if (getPortraitUri() != null) {
			try {

				WindowManager wm = (WindowManager) DSATabApplication.getInstance().getSystemService(
						Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();

				portraitBitmap = Util.decodeFile(getPortraitUri(), display.getWidth());
			} catch (IOException e) {
				Debug.error("Error getting bitmap", e);
			}
		}
		return portraitBitmap;

	}

	public void addHeroChangedListener(HeroChangedListener v) {
		if (!listener.contains(v))
			listener.add(v);

		Debug.verbose("Add " + v.getClass() + ". Hero currently contains " + listener.size() + " listeners:"
				+ listener.toArray());
	}

	public void removeHeroChangedListener(HeroChangedListener v) {
		listener.remove(v);

		Debug.verbose("Remove " + v.getClass() + ". Hero currently contains " + listener.size() + " listeners:"
				+ listener.toArray());
	}

	public EquippedItem getEquippedItem(String name) {
		return getEquippedItem(activeSet, name);
	}

	public EquippedItem getEquippedItem(int set, String name) {
		for (EquippedItem item : getEquippedItems(set)) {
			if (item.getName().equals(name))
				return item;
		}
		return null;

	}

	public EquippedItem getEquippedItem(UUID id) {
		for (int i = 0; i < MAXIMUM_SET_NUMBER; i++) {
			for (EquippedItem item : getEquippedItems(i)) {
				if (item.getId().equals(id))
					return item;
			}
		}
		return null;

	}

	public Purse getPurse() {
		if (purse == null) {

			Element purseElement = getHeldElement().getChild(Xml.KEY_GELDBOERSE);
			if (purseElement != null) {
				purse = new Purse(purseElement);
			} else {
				purseElement = new Element(Xml.KEY_GELDBOERSE);
				getHeldElement().addContent(purseElement);
				purse = new Purse(purseElement);
			}
		}
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
			huntingWeapons[activeSet].setAttribute(Xml.KEY_NUMMER, Util.toString(item.getNameId()));
		}
	}

	public EquippedItem getHuntingWeapon() {
		if (huntingWeapons[activeSet] != null) {
			Integer number = Util.parseInt(huntingWeapons[activeSet].getAttributeValue(Xml.KEY_NUMMER));

			if (number != null && number > 0) {
				return getEquippedItem(activeSet, PREFIX_FKWAFFE + number);
			}
		}
		return null;
	}

	public List<EquippedItem> getEquippedItems(int selectedSet) {
		if (equippedItems[selectedSet] == null) {
			equippedItems[selectedSet] = new LinkedList<EquippedItem>();

			List<Element> equippedElements = DomUtil.getChildrenByTagName(getHeldElement(), equippmentNode,
					Xml.KEY_HELDENAUSRUESTUNG);

			List<EquippedItem> secondaryItems = new ArrayList<EquippedItem>();

			for (int i = 0; i < equippedElements.size(); i++) {
				Element element = (Element) equippedElements.get(i);

				int set = 0;
				if (element.getAttribute(Xml.KEY_SET) != null) {
					set = Util.parseInt(element.getAttributeValue(Xml.KEY_SET));
					if (set != selectedSet)
						continue;
				}

				if (element.getAttributeValue(Xml.KEY_NAME).equals(JAGTWAFFE)) {
					huntingWeapons[selectedSet] = element;
					continue;
				}

				if (element.getAttributeValue(Xml.KEY_NAME).startsWith(PREFIX_BK)) {
					beidhaendigerKampfElements.add(element);
					continue;
				}

				EquippedItem equippedItem = new EquippedItem(this, element);

				// fix wrong screen iteminfo
				if (equippedItem.getItemInfo().getScreen() == ItemLocationInfo.INVALID_POSITION) {
					equippedItem.getItemInfo().setScreen(equippedItem.getSet());
				}

				if (element.getAttributeValue(Xml.KEY_SCHILD) != null) {
					int schild = Util.parseInt(element.getAttributeValue(Xml.KEY_SCHILD));
					if (schild > 0) {
						secondaryItems.add(equippedItem);
					}
				}

				if (equippedItem.getItem() != null) {
					equippedItems[selectedSet].add(equippedItem);

					equippedItem.getItem().getEquippedItems().add(equippedItem);
				} else {
					Debug.warning("Skipped EquippedItem because Item was not found: " + equippedItem.getItemName());
				}
			}

			// handle bk elements
			for (Iterator<Element> iter = beidhaendigerKampfElements.iterator(); iter.hasNext();) {

				Element element = iter.next();

				int set = 0;
				if (element.getAttribute(Xml.KEY_SET) != null) {
					set = Util.parseInt(element.getAttributeValue(Xml.KEY_SET));
					if (set != selectedSet)
						continue;
				}

				if (element.getAttributeValue(Xml.KEY_NAME).startsWith(PREFIX_BK)) {
					String bk = element.getAttributeValue(Xml.KEY_NAME);
					int bk1 = Util.parseInt(bk.substring(2, 3));
					int bk2 = Util.parseInt(bk.substring(3, 4));

					EquippedItem item1 = getEquippedItem(selectedSet, PREFIX_NKWAFFE + bk1);
					EquippedItem item2 = getEquippedItem(selectedSet, PREFIX_NKWAFFE + bk2);

					if (item2 != null && item1 != null) {
						item1.setSecondaryItem(item2);
						item2.setSecondaryItem(item1);
					} else {
						Debug.warning("Incorrect BeidhaengierKampf setting " + bk);
						getHeldElement().removeContent(element);
						iter.remove();
					}
				}

			}

			for (EquippedItem equippedItem : secondaryItems) {
				Element element = equippedItem.getElement();

				if (element.getAttributeValue(Xml.KEY_SCHILD) != null) {
					int schild = Util.parseInt(element.getAttributeValue(Xml.KEY_SCHILD));
					if (schild > 0) {
						EquippedItem secondaryEquippedItem = getEquippedItem(EquippedItem.NAME_PREFIX_SCHILD + schild);
						if (secondaryEquippedItem != null) {
							equippedItem.setSecondaryItem(secondaryEquippedItem);
							secondaryEquippedItem.setSecondaryItem(equippedItem);
						}
					}
				}
			}

			Util.sort(equippedItems[selectedSet]);

			if (huntingWeapons[selectedSet] == null) {

				Element element = new Element(Xml.KEY_HELDENAUSRUESTUNG);
				element.setAttribute(Xml.KEY_NAME, JAGTWAFFE);
				element.setAttribute(Xml.KEY_NUMMER, "0");
				element.setAttribute(Xml.KEY_SET, Util.toString(selectedSet));

				if (equippmentNode != null)
					equippmentNode.addContent(element);
				else
					getHeldElement().addContent(element);

				huntingWeapons[selectedSet] = element;
			}
		}

		return equippedItems[selectedSet];
	}

	public boolean hasBeidhaendigerKampf(EquippedItem item1, EquippedItem item2) {

		for (Element element : beidhaendigerKampfElements) {

			String bk = element.getAttributeValue(Xml.KEY_NAME);

			if (bk.equals(PREFIX_BK + item1.getNameId() + item2.getNameId()))
				return true;

			if (bk.equals(PREFIX_BK + item2.getNameId() + item1.getNameId()))
				return true;
		}
		return false;
	}

	public void addBeidhaendigerKampf(EquippedItem item1, EquippedItem item2) {

		if (hasBeidhaendigerKampf(item1, item2))
			return;

		Element bk = new Element(Xml.KEY_HELDENAUSRUESTUNG);
		bk.setAttribute(Xml.KEY_SET, Util.toString(activeSet));

		if (item1.getNameId() < item2.getNameId())
			bk.setAttribute(Xml.KEY_NAME, PREFIX_BK + item1.getNameId() + item2.getNameId());
		else
			bk.setAttribute(Xml.KEY_NAME, PREFIX_BK + item2.getNameId() + item1.getNameId());

		beidhaendigerKampfElements.add(bk);

		if (equippmentNode == null)
			getHeldElement().addContent(bk);
		else
			equippmentNode.addContent(bk);
	}

	public void removeBeidhaendigerKampf(EquippedItem item1, EquippedItem item2) {

		for (Element element : beidhaendigerKampfElements) {

			String bk = element.getAttributeValue(Xml.KEY_NAME);

			if (bk.equals(PREFIX_BK + item1.getNameId() + item2.getNameId())
					|| bk.equals(PREFIX_BK + item2.getNameId() + item1.getNameId())) {

				beidhaendigerKampfElements.remove(bk);

				if (equippmentNode == null)
					getHeldElement().removeContent(element);
				else
					equippmentNode.removeContent(element);
			}

		}

	}

	public String getKey() {
		return getHeldElement().getAttributeValue(Xml.KEY_KEY);
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

	void fireModifiersChangedEvent(List<Modificator> modifiers) {
		Debug.trace("ON modifiers changed " + modifiers);
		clearModifiersCache();
		this.modificators = modifiers;

		for (HeroChangedListener l : listener) {
			l.onModifiersChanged(modifiers);
		}
	}

	public void fireValueChangedEvent(Value value) {
		Debug.trace("ON Value changed " + value);

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

				attr = getAttribute(AttributeType.Astralenergie);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Astralenergie_Total);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Ausdauer);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Ausdauer_Total);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Lebensenergie);
				if (attr != null)
					attr.checkBaseValue();

				attr = getAttribute(AttributeType.Lebensenergie_Total);
				if (attr != null)
					attr.checkBaseValue();

				break;
			case Ausdauer:
				postAuRatioCheck();
				break;
			case Lebensenergie:
				postLeRatioCheck();
				break;
			case Lebensenergie_Total:
				attr = getAttribute(AttributeType.Lebensenergie);
				if (attr != null) {
					attr.setReferenceValue(value.getValue());
				}
				postLeRatioCheck();
				break;
			case Ausdauer_Total:
				attr = getAttribute(AttributeType.Ausdauer);
				if (attr != null) {
					attr.setReferenceValue(value.getValue());
				}
				postAuRatioCheck();
				break;
			case Astralenergie_Total:
				attr = getAttribute(AttributeType.Astralenergie);
				if (attr != null)
					attr.setReferenceValue(value.getValue());
				break;
			case Karmaenergie_Total:
				attr = getAttribute(AttributeType.Karmaenergie);
				if (attr != null)
					attr.setReferenceValue(value.getValue());
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

	public void onPostHeroSaved() {
		for (File f : deletableAudioFiles) {
			f.delete();
		}
	}

	void fireItemAddedEvent(Item item) {
		Debug.trace("ON Item added " + item);
		for (HeroChangedListener l : listener) {
			l.onItemAdded(item);
		}
	}

	void fireItemRemovedEvent(Item item) {
		Debug.trace("ON Item removed " + item);
		for (HeroChangedListener l : listener) {
			item.getEquippedItems().clear();
			l.onItemRemoved(item);
		}
	}

	void fireItemEquippedEvent(EquippedItem item) {
		Debug.trace("ON Item equipped " + item);
		for (HeroChangedListener l : listener) {
			item.getItem().getEquippedItems().add(item);
			l.onItemEquipped(item);
		}
	}

	public void fireItemChangedEvent(EquippedItem item) {
		Debug.trace("ON Item changed " + item);
		for (HeroChangedListener l : listener) {
			l.onItemChanged(item);
		}
	}

	public void fireItemChangedEvent(Item item) {
		Debug.trace("ON Item changed " + item);
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
		Debug.trace("ON Item unequipped " + item);
		for (HeroChangedListener l : listener) {

			item.getItem().getEquippedItems().remove(item);

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
		Debug.trace("ON modifier removed " + modifier);
		clearModifiersCache();
		getModificators().remove(modifier);

		for (HeroChangedListener l : listener) {
			l.onModifierRemoved(modifier);
		}
	}

	/**
	 * @param item
	 */
	public void removeItem(Item item) {
		List<Item> items = getItems();
		items.remove(item);
		if (itemsNode != null)
			itemsNode.removeContent(item.getElement());
		else
			getHeldElement().removeContent(item.getElement());

		fireItemRemovedEvent(item);

		List<EquippedItem> toremove = new ArrayList<EquippedItem>();

		for (int i = 0; i < MAXIMUM_SET_NUMBER; i++) {

			for (EquippedItem equippedItem : getEquippedItems(i)) {

				if (equippedItem.getItem() == null) {
					Debug.warning("Empty EquippedItem found during item delete:" + equippedItem.getName() + " - "
							+ equippedItem.getItemName());
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

				builder.show().setCanceledOnTouchOutside(true);
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

		if (callback != null) {
			if (item.isEquipable() && set >= 0) {
				addEquippedItem(context, item, itemSpecification, newTalent, set, callback);
			} else {
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

		List<Item> items = getItems();

		// item already added, no need to add again
		if (items.contains(item))
			return false;

		if (item.getElement() != null) {
			throw new IllegalArgumentException("Item " + item.getName()
					+ " cannot be added since it already has a dom element");
		}

		Element element = new Element(Xml.KEY_GEGENSTAND);
		element.setAttribute(Xml.KEY_NAME, item.getName());
		element.setAttribute(Xml.KEY_ANZAHL, "1");
		element.setAttribute(Xml.KEY_SLOT, "0");
		item.setElement(element);

		items.add(item);
		if (itemsNode == null)
			getHeldElement().addContent(element);
		else
			itemsNode.addContent(element);

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

		Element element = new Element(Xml.KEY_HELDENAUSRUESTUNG);
		element.setAttribute(Xml.KEY_SET, Util.toString(set));

		if (equippmentNode == null)
			getHeldElement().addContent(element);
		else
			equippmentNode.addContent(element);

		EquippedItem equippedItem = new EquippedItem(this, element, item);

		if (talent != null)
			equippedItem.setTalent(talent);
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

				builder.show().setCanceledOnTouchOutside(true);
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

	public org.jdom.Document getDocument() {
		return dom;
	}

	public Element getHeldElement() {
		if (held == null)
			held = (Element) dom.getRootElement().getChild(Xml.KEY_HELD);

		return held;
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

		if (attribute == null) {

			// onload load all attributes if no attributes are found yet, the
			// rest of the attributes will be lazy loaded when needed
			if (attributes.isEmpty()) {
				List<Element> domAttributes = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_EIGENSCHAFTEN,
						Xml.KEY_EIGENSCHAFT);

				for (int i = 0; i < domAttributes.size(); i++) {
					Element attributeElement = (Element) domAttributes.get(i);

					Attribute attr = new Attribute(attributeElement, this);
					this.attributes.put(attr.getType(), attr);

					if (attr.getType() == AttributeType.Lebensenergie) {
						Attribute attr2 = new Attribute(attributeElement, AttributeType.Lebensenergie_Total, this);
						this.attributes.put(attr2.getType(), attr2);
					} else if (attr.getType() == AttributeType.Ausdauer) {
						Attribute attr2 = new Attribute(attributeElement, AttributeType.Ausdauer_Total, this);
						this.attributes.put(attr2.getType(), attr2);
					} else if (attr.getType() == AttributeType.Karmaenergie) {
						Attribute attr2 = new Attribute(attributeElement, AttributeType.Karmaenergie_Total, this);
						this.attributes.put(attr2.getType(), attr2);
					} else if (attr.getType() == AttributeType.Astralenergie) {
						Attribute attr2 = new Attribute(attributeElement, AttributeType.Astralenergie_Total, this);
						this.attributes.put(attr2.getType(), attr2);
					}
				}

				for (CustomAttribute attr : getHeroConfiguration().getAttributes()) {
					this.attributes.put(attr.getType(), attr);
				}
			}

			if (type == AttributeType.Behinderung && !this.attributes.containsKey(AttributeType.Behinderung)) {
				CustomAttribute be = new CustomAttribute(this, AttributeType.Behinderung);
				be.setValue(getArmorBe());
				getHeroConfiguration().addAttribute(be);
				this.attributes.put(be.getType(), be);
			}
			if (type == AttributeType.Ausweichen && !this.attributes.containsKey(AttributeType.Ausweichen)) {
				CustomAttribute aw = new CustomAttribute(this, AttributeType.Ausweichen);
				getHeroConfiguration().addAttribute(aw);
				this.attributes.put(aw.getType(), aw);
			}
			if (type == AttributeType.Geschwindigkeit && !this.attributes.containsKey(AttributeType.Geschwindigkeit)) {
				CustomAttribute aw = new CustomAttribute(this, AttributeType.Geschwindigkeit);
				getHeroConfiguration().addAttribute(aw);
				this.attributes.put(aw.getType(), aw);
			}

			if (type == AttributeType.Initiative_Aktuell
					&& !this.attributes.containsKey(AttributeType.Initiative_Aktuell)) {
				CustomAttribute ini = new CustomAttribute(this, AttributeType.Initiative_Aktuell);
				ini.setValue(0);
				getHeroConfiguration().addAttribute(ini);
				this.attributes.put(ini.getType(), ini);
			}

			if (type == AttributeType.fk && !this.attributes.containsKey(AttributeType.fk)) {
				Element element = new Element(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.fk.name());

				int basefk = getAttributeValue(AttributeType.Intuition)
						+ getAttributeValue(AttributeType.Fingerfertigkeit)
						+ getAttributeValue(AttributeType.Körperkraft);

				basefk = Math.round(basefk / 5.0F);
				element.setAttribute(Xml.KEY_VALUE, Util.toString(basefk));
				element.setAttribute(Xml.KEY_MOD, "0");
				if (attributesNode == null)
					getHeldElement().addContent(element);
				else
					attributesNode.addContent(element);
				this.attributes.put(AttributeType.fk, new Attribute(element, this));
			}

			if (type == AttributeType.pa && !this.attributes.containsKey(type)) {
				Element element = new Element(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.pa.name());

				int basefk = getAttributeValue(AttributeType.Intuition) + getAttributeValue(AttributeType.Gewandtheit)
						+ getAttributeValue(AttributeType.Körperkraft);
				basefk = Math.round(basefk / 5.0F);
				element.setAttribute(Xml.KEY_VALUE, Util.toString(basefk));
				element.setAttribute(Xml.KEY_MOD, "0");
				if (attributesNode == null)
					getHeldElement().addContent(element);
				else
					attributesNode.addContent(element);
				this.attributes.put(AttributeType.pa, new Attribute(element, this));
			}

			if (type == AttributeType.at && !this.attributes.containsKey(type)) {
				Element element = new Element(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.at.name());

				int basefk = getAttributeValue(AttributeType.Mut) + getAttributeValue(AttributeType.Gewandtheit)
						+ getAttributeValue(AttributeType.Körperkraft);
				basefk = Math.round(basefk / 5.0F);
				element.setAttribute(Xml.KEY_VALUE, Util.toString(basefk));
				element.setAttribute(Xml.KEY_MOD, "0");
				if (attributesNode == null)
					getHeldElement().addContent(element);
				else
					attributesNode.addContent(element);
				this.attributes.put(AttributeType.at, new Attribute(element, this));
			}

			if (type == AttributeType.ini && !this.attributes.containsKey(type)) {
				Element element = new Element(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.ini.name());

				int basefk = getAttributeValue(AttributeType.Mut) + getAttributeValue(AttributeType.Mut)
						+ getAttributeValue(AttributeType.Intuition) + getAttributeValue(AttributeType.Gewandtheit);
				basefk = Math.round(basefk / 5.0F);
				element.setAttribute(Xml.KEY_VALUE, Util.toString(basefk));
				element.setAttribute(Xml.KEY_MOD, "0");
				if (attributesNode == null)
					getHeldElement().addContent(element);
				else
					attributesNode.addContent(element);
				this.attributes.put(AttributeType.ini, new Attribute(element, this));
			}

			if (type == AttributeType.Entrueckung && !this.attributes.containsKey(type)) {
				CustomAttribute entr = new CustomAttribute(this, AttributeType.Entrueckung);
				entr.setValue(0);
				getHeroConfiguration().addAttribute(entr);
				this.attributes.put(entr.getType(), entr);
			}

			if (type == AttributeType.Verzueckung && !this.attributes.containsKey(type)) {
				CustomAttribute entr = new CustomAttribute(this, AttributeType.Verzueckung);
				entr.setValue(0);
				getHeroConfiguration().addAttribute(entr);
				this.attributes.put(entr.getType(), entr);
			}

			if (type == AttributeType.Erschoepfung && !this.attributes.containsKey(type)) {
				CustomAttribute entr = new CustomAttribute(this, AttributeType.Erschoepfung);
				entr.setValue(0);
				getHeroConfiguration().addAttribute(entr);
				this.attributes.put(entr.getType(), entr);
			}

			attribute = this.attributes.get(type);
		}

		return attribute;
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
		return getHeldElement().getAttributeValue(Xml.KEY_NAME);
	}

	public EditableValue getExperience() {
		if (experience == null) {
			experience = new Experience(this, "Abenteuerpunkte", DomUtil.getChildByTagName(getHeldElement(),
					Xml.KEY_BASIS, Xml.KEY_ABENTEUERPUNKTE));
			experience.setMaximum(100000);
		}
		return experience;
	}

	public int getLevel() {
		int level = getExperience().getValue() - getFreeExperience().getValue();

		level = level / 1000;

		return level;
	}

	public EditableValue getFreeExperience() {
		if (freeExperience == null) {
			freeExperience = new EditableValue(this, "Freie Abenteuerpunkte", DomUtil.getChildByTagName(
					getHeldElement(), Xml.KEY_BASIS, Xml.KEY_FREIE_ABENTEUERPUNKTE));
			experience.setMaximum(100000);
		}

		return freeExperience;
	}

	protected void postLeRatioCheck() {

		if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_LE_MODIFIER, true)) {
			float newLeRatioCheck = getLeRatio();

			int newLeRatioLevel = 0;

			if (newLeRatioCheck < 0.25)
				newLeRatioLevel = 3;
			else if (newLeRatioCheck < 0.33)
				newLeRatioLevel = 2;
			else if (newLeRatioCheck < 0.5)
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
		Debug.trace("ON Preferences changed");
		if (BasePreferenceActivity.KEY_HOUSE_RULES_AU_MODIFIER.equals(key)) {
			postAuRatioCheck();
		}

		if (BasePreferenceActivity.KEY_HOUSE_RULES_LE_MODIFIER.equals(key)) {
			postLeRatioCheck();
		}
	}

	protected void postAuRatioCheck() {
		if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_AU_MODIFIER, true)) {

			double newAuRatioCheck = getAuRatio();

			int newAuRatioLevel = 0;
			if (newAuRatioCheck < 0.25)
				newAuRatioLevel = 2;
			else if (newAuRatioCheck < 0.33)
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
	public float getLeRatio() {
		Attribute le = getAttribute(AttributeType.Lebensenergie);
		if (le != null) {
			Integer value = le.getValue();
			Integer ref = le.getReferenceValue();
			if (value != null && ref != null) {
				return ((float) value) / ref;
			} else {
				return 1.0f;
			}
		} else {
			return 1.0f;
		}

	}

	/**
	 * @return
	 */
	public float getAuRatio() {
		Attribute au = getAttribute(AttributeType.Ausdauer);
		if (au != null)
			return ((float) au.getValue()) / au.getReferenceValue();
		else
			return 1.0f;
	}

	public float getAeRatio() {
		Attribute ae = getAttribute(AttributeType.Astralenergie);
		if (ae != null)
			return ((float) ae.getValue()) / ae.getReferenceValue();
		else
			return 1.0f;
	}

	public float getKeRatio() {
		Attribute ke = getAttribute(AttributeType.Karmaenergie);
		if (ke != null)
			return ((float) ke.getValue()) / ke.getReferenceValue();
		else
			return 1.0f;
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
		for (Modifier mod : getModifiers(probe, includeBe, includeLeAu)) {
			modifier += mod.getModifier();
		}

		if (includeBe)
			cache.modInclBe = modifier;
		else if (includeLeAu)
			cache.modInclLEAu = modifier;
		else
			cache.mod = modifier;

		return modifier;
	}

	public List<Modifier> getModifiers(Probe probe, boolean includeBe, boolean includeLeAu) {

		List<Modifier> modifiers = new LinkedList<Modifier>();

		for (Modificator modificator : getModificators()) {
			if (!includeLeAu && (modificator == leModificator || modificator == auModificator))
				continue;

			Modifier mod = modificator.getModifier(probe);
			if (mod != null) {
				// Debug.verbose("Modificator of " + mod.getTitle() + " is " +
				// mod.getModifier());
				if (mod.getModifier() != 0) {
					modifiers.add(mod);
				}
			}
		}

		if (probe instanceof Attribute) {
			Attribute attribute = (Attribute) probe;

			if (attribute.getType() == AttributeType.ini) {
				if (attribute.getHero().hasFeature(SpecialFeature.KAMPFGESPUER))
					modifiers.add(new Modifier(2, SpecialFeature.KAMPFGESPUER));

				if (attribute.getHero().hasFeature(SpecialFeature.KAMPFREFLEXE))
					modifiers.add(new Modifier(4, SpecialFeature.KAMPFREFLEXE));
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
					modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
				}
				if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
					modifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST));
				}
				if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
					modifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO));
				}
				if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
					modifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT));
				}
			}

			if (meleeTalent.getCombatTalentType() == CombatTalentType.Ringen) {
				if (hasFeature(SpecialFeature.WK_UNAUER_SCHULE)) {
					modifiers.add(new Modifier(1, SpecialFeature.WK_UNAUER_SCHULE));
				}
				if (hasFeature(SpecialFeature.WK_BORNLAENDISCH)) {
					modifiers.add(new Modifier(1, SpecialFeature.WK_BORNLAENDISCH));
				}
				if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
					modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
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
						if (maxDistance <= 20)
							modifiers.add(new Modifier(-7, "Reichweite der Jagdwaffe unter 20 Schritt", ""));
						else if (maxDistance <= 50)
							modifiers.add(new Modifier(-3, "Reichweite der Jagdwaffe unter 50 Schritt", ""));
					}
				}

				if (hasFeature(SpecialFeature.MEISTERSCHUETZE))
					modifiers.add(new Modifier(7, SpecialFeature.MEISTERSCHUETZE));
				else if (hasFeature(SpecialFeature.SCHARFSCHUETZE))
					modifiers.add(new Modifier(3, SpecialFeature.SCHARFSCHUETZE));

				break;
			case Wache:

				// TODO add erschöpfung

				if (hasFeature(SpecialFeature.AUFMERKSAMKEIT))
					modifiers.add(new Modifier(1, SpecialFeature.AUFMERKSAMKEIT));

				Talent gefahr = getTalent(Talent.GEFAHRENINSTINKT);
				if (gefahr != null) {
					modifiers.add(new Modifier(gefahr.getProbeBonus() / 2, "Gefahreninstinkt TaW/2", ""));
				}

				Advantage ausdauernd = getAdvantage(Advantage.AUSDAUERND);
				if (ausdauernd != null) {
					modifiers.add(new Modifier(ausdauernd.getValue() / 3, "Ausdauernd Pkt/3", ""));
				}

				if (hasFeature(SpecialFeature.DÄMMERNGSSICHT)) {
					modifiers.add(new Modifier(1, SpecialFeature.DÄMMERNGSSICHT));
				}

				if (hasFeature(SpecialFeature.NACHTSICHT)) {
					modifiers.add(new Modifier(3, SpecialFeature.NACHTSICHT));
				}

				if (hasFeature(SpecialFeature.HERRAUSRAGENDER_SINN)) {
					modifiers.add(new Modifier(1, SpecialFeature.HERRAUSRAGENDER_SINN));
				}

				if (hasFeature(SpecialFeature.EINÄUGIG)) {
					modifiers.add(new Modifier(-2, SpecialFeature.EINÄUGIG));
				}
				if (hasFeature(SpecialFeature.EINBILDUNGEN)) {
					modifiers.add(new Modifier(-2, SpecialFeature.EINBILDUNGEN));
				}
				if (hasFeature(SpecialFeature.DUNKELANGST)) {
					modifiers.add(new Modifier(-3, SpecialFeature.DUNKELANGST));
				}

				if (hasFeature(SpecialFeature.NACHTBLIND)) {
					modifiers.add(new Modifier(-3, SpecialFeature.NACHTBLIND));
				}
				if (hasFeature(SpecialFeature.UNSTET)) {
					modifiers.add(new Modifier(-2, SpecialFeature.UNSTET));
				}

			}

		} else if (probe instanceof CombatProbe) {
			CombatProbe combatProbe = (CombatProbe) probe;
			EquippedItem equippedItem = combatProbe.getEquippedItem();

			if (equippedItem != null && equippedItem.getItemSpecification() instanceof Weapon) {
				Item item = equippedItem.getItem();
				Weapon weapon = (Weapon) equippedItem.getItemSpecification();

				int kkModifier = weapon.getKKModifier(getModifiedValue(AttributeType.Körperkraft));

				if (kkModifier < 0) {
					Debug.verbose("Körperkraft liegt unter der Schwelle.");
					modifiers.add(new Modifier(kkModifier, "Zu geringe Körperkraft", ""));
				}

				if (combatProbe.isAttack()) {

					Debug.verbose("Hauptwaffe Wm Attack is " + weapon.getWmAt());
					if (weapon.getWmAt() != null && weapon.getWmAt() != 0)
						modifiers.add(new Modifier(weapon.getWmAt(), "Waffenmodifikator At"));
				} else {
					Debug.verbose("Hauptwaffe Wm Defense is " + weapon.getWmPa());
					if (weapon.getWmPa() != null && weapon.getWmPa() != 0)
						modifiers.add(new Modifier(weapon.getWmPa(), "Waffenmodifikator Pa"));
				}

				BaseCombatTalent talent = (BaseCombatTalent) equippedItem.getTalent();
				if (talent != null && talent.getTalentSpezialisierung() != null
						&& talent.getTalentSpezialisierung().equalsIgnoreCase(item.getName())) {

					Debug.verbose("Talentspezialisierung " + item.getName() + " +1");

					modifiers.add(new Modifier(1, SpecialFeature.TALENTSPEZIALISIERUNG_PREFIX + " "
							+ talent.getTalentSpezialisierung()));

				}

				// waffenlose kampftechniken +1/+1
				if (item.getName().startsWith("Raufen")) {
					if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
					}
					if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST));
					}
					if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO));
					}
					if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT));
					}
				}

				if (item.getName().startsWith("Hruruzat")) {
					if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
					}
					if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST));
					}
					if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO));
					}
					if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT));
					}
				}

				if (item.getName().startsWith("Ringen")) {
					if (hasFeature(SpecialFeature.WK_UNAUER_SCHULE)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_UNAUER_SCHULE));
					}
					if (hasFeature(SpecialFeature.WK_BORNLAENDISCH)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_BORNLAENDISCH));
					}
					if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL));
					}
				}

				// check for beidhändiger kampf
				if (equippedItem.getHand() == Hand.links) {
					EquippedItem equippedSecondaryWeapon = equippedItem.getSecondaryItem();

					if (equippedSecondaryWeapon != null
							&& equippedSecondaryWeapon.getItem().hasSpecification(Weapon.class)) {
						int m = -9;

						if (hasFeature(SpecialFeature.LINKHAND))
							m += 3;

						if (hasFeature(SpecialFeature.BEIDHAENDIGER_KAMPF_1))
							m += 3;

						if (hasFeature(SpecialFeature.BEIDHAENDIGER_KAMPF_2))
							m += 3;

						modifiers
								.add(new Modifier(m, "Beidhändigerkampf Links",
										"Beim Beidhändigenkampf bekommt man je nach Sonderfertigkeiten bei aktionen mit der linken Hand Abzüge."));

						Debug.verbose("Beidhändiger Kampf mit Links " + Util.toProbe(m));

					}

				}

				// modify weapon attack with shield wmAt modifier if second
				// weapon is shield
				if (combatProbe.isAttack()) {
					EquippedItem equippedShield = equippedItem.getSecondaryItem();
					if (equippedShield != null && equippedShield.getItem().hasSpecification(Shield.class)) {

						Shield shield = (Shield) equippedShield.getItem().getSpecification(Shield.class);

						modifiers.add(new Modifier(shield.getWmAt(), "Schildkampf Modifikator At"));

						Debug.verbose("Hauptwaffenattacke is reduziert um Schild WM " + shield.getWmAt());
					}
				}
			}
			if (equippedItem != null && equippedItem.getItemSpecification() instanceof Shield) {
				Shield shield = (Shield) equippedItem.getItemSpecification();

				if (combatProbe.isAttack()) {
					Debug.verbose("Shield Wm Attack is " + shield.getWmAt());

					modifiers.add(new Modifier(shield.getWmAt(), "Schildmodifikator At"));
				} else {
					Debug.verbose("Shield Wm Defense is " + shield.getWmPa());
					modifiers.add(new Modifier(shield.getWmPa(), "Schildmodifikator PA"));

					// paradevalue is increased by 1 if weaponparade is above
					// 15
					if (shield.isShield()) {
						EquippedItem equippedWeapon = equippedItem.getSecondaryItem();

						if (equippedWeapon != null) {
							int defenseValue = 0;
							if (equippedWeapon.getTalent().getDefense() != null) {
								defenseValue = equippedWeapon.getTalent().getDefense().getProbeValue(0);
							}

							if (defenseValue >= 21) {
								Debug.verbose("Shield: Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +3");
								modifiers.add(new Modifier(3, "Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +3"));
							} else if (defenseValue >= 18) {
								Debug.verbose("Shield: Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +2");
								modifiers.add(new Modifier(2, "Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +2"));
							} else if (defenseValue >= 15) {
								Debug.verbose("Shield: Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +1");
								modifiers.add(new Modifier(1, "Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +1"));
							}
						}
					}
				}
			} else if (equippedItem != null && equippedItem.getItemSpecification() instanceof DistanceWeapon) {
				Item item = equippedItem.getItem();
				DistanceWeapon distanceWeapon = (DistanceWeapon) equippedItem.getItemSpecification();

				BaseCombatTalent talent = (BaseCombatTalent) equippedItem.getTalent();
				if (talent != null && talent.getTalentSpezialisierung() != null
						&& talent.getTalentSpezialisierung().equalsIgnoreCase(item.getName())) {

					Debug.verbose("Talentspezialisierung " + item.getName() + " +2");

					modifiers.add(new Modifier(2, SpecialFeature.TALENTSPEZIALISIERUNG_PREFIX + " "
							+ talent.getTalentSpezialisierung()));

				}

				if (hasFeature(Advantage.ENTFERNUNGSSINN)) {
					modifiers.add(new Modifier(2, Advantage.ENTFERNUNGSSINN));
				}
			}

		}

		if (includeBe) {
			int heroBe = getBe(probe);

			if (heroBe != 0) {

				modifiers.add(new Modifier(-1 * heroBe, "Behinderung " + probe.getProbeInfo().getBe()));
			}
		}

		return modifiers;
	}

	public int getModifier(AttributeType type) {
		return getModifier(getAttribute(type), true, true);
	}

	public int getModifier(AttributeType type, boolean includeBe, boolean includeLeAu) {
		return getModifier(getAttribute(type), includeBe, includeLeAu);
	}

	public Integer getModifiedValue(AttributeType type) {
		return getModifiedValue(type, true, true);
	}

	public Integer getModifiedValue(AttributeType type, boolean includeBe, boolean includeLeAu) {
		if (getAttributeValue(type) == null)
			return null;

		return getAttributeValue(type) + getModifier(type, includeBe, includeLeAu);
	}

	public Integer getModifiedValue(Probe probe, boolean includeBe) {

		if (probe == null || probe.getValue() == null)
			return null;

		return probe.getValue() + getModifier(probe, includeBe, true);
	}

	public Integer getAttributeValue(AttributeType type) {
		Attribute attribute = getAttribute(type);

		if (attribute != null)
			return attribute.getValue();
		else
			return null;
	}

	public Map<String, SpecialFeature> getSpecialFeatures() {
		if (specialFeatures == null) {
			fillArtsAndSpecialFeatures();
		}

		return specialFeatures;

	}

	private void fillArtsAndSpecialFeatures() {
		List<Element> sf = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_SONDERFERTIGKEITEN,
				Xml.KEY_SONDERFERTIGKEIT);

		specialFeatures = new TreeMap<String, SpecialFeature>();
		artsByName = new TreeMap<String, Art>();

		for (Element feat : sf) {

			String name = feat.getAttributeValue(Xml.KEY_NAME).trim();
			ArtType type = ArtType.getTypeOfArt(name);
			if (type == null) {
				SpecialFeature specialFeature = new SpecialFeature(feat);
				boolean add = true;

				if (specialFeature.getName().startsWith(SpecialFeature.TALENTSPEZIALISIERUNG_PREFIX)) {
					Talent talent = getTalent(specialFeature.getParameter1());
					if (talent != null) {
						talent.setTalentSpezialisierung(specialFeature.getParameter2());
						add = false;
					}
				} else if (specialFeature.getName().startsWith(SpecialFeature.ZAUBERSPEZIALISIERUNG_PREFIX)) {
					Spell spell = getSpell(specialFeature.getParameter1());
					if (spell != null) {
						spell.setZauberSpezialisierung(specialFeature.getParameter2());
						add = false;
					}
				} else if (specialFeature.getName().startsWith(Talent.RITUAL_KENNTNIS_PREFIX)) {
					// skipp specialfeature ritualkenntnis since it's listed as
					// talent anyway.
					add = false;
				}

				if (add) {
					specialFeatures.put(specialFeature.getName(), specialFeature);
				}
			} else {
				Art art = new Art(this, feat);
				artsByName.put(art.getName(), art);
			}

		}
	}

	public Map<String, Advantage> getAdvantages() {
		if (advantages == null) {
			fillAdvantages();
		}

		return advantages;
	}

	private void fillAdvantages() {
		List<Element> sfs = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_VORTEILE, Xml.KEY_VORTEIL);

		disadvantages = new TreeMap<String, Advantage>();
		advantages = new TreeMap<String, Advantage>();

		for (Element feat : sfs) {
			Advantage adv = new Advantage(feat);
			boolean add = true;
			if (adv.getName().equals(Advantage.BEGABUNG_FUER_TALENT)) {
				Talent talent = getTalent(adv.getValueAsString());
				if (talent != null) {
					talent.addFlag(Flags.Begabung);
					add = false;
				}
			} else if (adv.getName().equals(Advantage.TALENTSCHUB)) {
				Talent talent = getTalent(adv.getValueAsString());
				if (talent != null) {
					talent.addFlag(Flags.Talentschub);
					add = false;
				}
			} else if (adv.getName().equals(Advantage.MEISTERHANDWERK)) {
				Talent talent = getTalent(adv.getValueAsString());
				if (talent != null) {
					talent.addFlag(Flags.Meisterhandwerk);
					add = false;
				}
			} else if (adv.getName().equals(Advantage.BEGABUNG_FUER_TALENTGRUPPE)) {
				try {
					TalentGroupType groupType = TalentGroupType.valueOf(adv.getValueAsString());
					TalentGroup talentGroup = getTalentGroups().get(groupType);
					if (talentGroup != null) {
						talentGroup.addFlag(Flags.Begabung);
						add = false;
					}
				} catch (Exception e) {
					Debug.warning("Begabung für [Talentgruppe], unknown talentgroup:" + adv.getValueAsString());
				}
			} else if (adv.getName().equals(Advantage.BEGABUNG_FUER_ZAUBER)) {
				Spell spell = getSpells().get(adv.getValueAsString());
				if (spell != null) {
					spell.addFlag(Flags.Begabung);
					add = false;
				}

			} else if (adv.getName().equals(Advantage.BEGABUNG_FUER_RITUAL)) {
				Art art = getArts().get(adv.getValueAsString());
				if (art != null) {
					art.addFlag(Flags.Begabung);
					add = false;
				}

			} else if (adv.getName().equals(Advantage.UEBERNATUERLICHE_BEGABUNG)) {
				Spell spell = getSpells().get(adv.getValueAsString());
				if (spell != null) {
					spell.addFlag(Flags.ÜbernatürlicheBegabung);
					add = false;

				}
			}

			if (add) {
				if (Advantage.isNachteil(adv.getName())) {
					disadvantages.put(adv.getName(), adv);
				} else if (Advantage.isVorteil(adv.getName()))
					advantages.put(adv.getName(), adv);
				else {
					Debug.warning("Not recognised value: " + feat.getAttributeValue(Xml.KEY_NAME));
				}
			}
		}
	}

	public Map<String, Advantage> getDisadvantages() {
		if (disadvantages == null) {
			fillAdvantages();
		}

		return disadvantages;
	}

	public Advantage getAdvantage(String name) {
		return getAdvantages().get(name);
	}

	public void addConnection(Connection connection) {
		getConnections().add(connection);
		getConnectionsElement().addContent(connection.getElement());

		Collections.sort(getConnections(), new ConnectionComparator());
	}

	public void removeConnection(Connection connection) {
		getConnections().remove(connection);
		getConnectionsElement().removeContent(connection.getElement());
	}

	private Element getConnectionsElement() {
		Element connectionsElement = getHeldElement().getChild(Xml.KEY_VERBINDUNGEN);
		if (connectionsElement == null) {
			connectionsElement = new Element(Xml.KEY_VERBINDUNGEN);
			getHeldElement().addContent(connectionsElement);
		}
		return connectionsElement;
	}

	public List<Connection> getConnections() {
		if (connections == null) {
			List<Element> connectionElements = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_VERBINDUNGEN,
					Xml.KEY_VERBINDUNG);

			connections = new ArrayList<Connection>(connectionElements.size());
			for (Element connectionElement : connectionElements) {
				connections.add(new Connection(connectionElement));
			}

			Collections.sort(connections, new ConnectionComparator());
		}
		return connections;
	}

	public SpecialFeature getSpecialFeature(String name) {
		return getSpecialFeatures().get(name);
	}

	public boolean hasFeature(String name) {
		boolean found = false;

		found = getSpecialFeatures().containsKey(name);

		if (!found) {
			found = getAdvantages().containsKey(name);
		}

		if (!found) {
			found = getAdvantages().containsKey(name);
		}

		return found;

	}

	public void addEvent(Event event) {
		getHeroConfiguration().addEvent(event);
		Collections.sort(getHeroConfiguration().getEvents(), new NotesComparator());
	}

	private Element getEventsElement() {
		if (ereignisse == null) {
			ereignisse = getHeldElement().getChild(Xml.KEY_EREIGNISSE);
		}
		return ereignisse;
	}

	public List<Event> getEvents() {
		// load old events from xml if events is empty
		if (getHeroConfiguration().getEvents().isEmpty()) {

			if (getEventsElement() != null) {
				@SuppressWarnings("unchecked")
				List<Element> eventElements = getEventsElement().getChildren(Xml.KEY_EREIGNIS);

				for (Element element : eventElements) {

					if (element.getAttribute(Xml.KEY_ABENTEUERPUNKTE_UPPER) != null
							|| element.getAttribute(Xml.KEY_OBJ) != null)
						continue;

					if (EVENT_NOTE_TEXT.equals(element.getAttributeValue(Xml.KEY_TEXT))) {
						getHeroConfiguration().addEvent(new Event(element));
					}
				}

				// UPGRADE: remove element since we will handle it in our
				// json configration from now one
				for (Event event : getHeroConfiguration().getEvents()) {
					if (event.getElement() != null && event.getCategory() == EventCategory.Misc) {
						getEventsElement().removeContent(event.getElement());
						event.setElement(null);
					}
				}
			}

			Collections.sort(getHeroConfiguration().getEvents(), new NotesComparator());

		}

		if (!notizElementFilled) {
			Element notiz = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_NOTIZ);

			if (notiz != null) {
				Event event = new Event(notiz);
				getHeroConfiguration().addEvent(event);
			}
			notizElementFilled = true;

			Collections.sort(getHeroConfiguration().getEvents(), new NotesComparator());
		}

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

			Debug.verbose("Start Be calc");

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

						if (rs1Armor != null && rs1Armor.equals(equippedItem.getItemName())) {
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

						if (rs1Armor != null && rs1Armor.equals(equippedItem.getItemName())) {
							be -= 1.0;
							rs1Armor = null;
						}
					}
				}
				break;

			}
			}

			beCache = Math.max(0, (int) Math.ceil(be));

			Debug.verbose("Finish Be calc " + beCache);
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
			if (item.hasSpecification(Armor.class)) {
				Armor armor = (Armor) item.getSpecification(Armor.class);
				if (armor.getRs(pos) > 0)
					items.add(equippedItem);
			}
		}

		return items;
	}

	public int getArmorRs(Position pos) {

		int rs = 0;
		for (EquippedItem equippedItem : getEquippedItems()) {
			Item item = equippedItem.getItem();
			if (item.hasSpecification(Armor.class)) {
				Armor armor = (Armor) item.getSpecification(Armor.class);
				rs += armor.getRs(pos);
			}
		}

		Advantage natRs = getAdvantage(Advantage.NATUERLICHER_RUESTUNGSSCHUTZ);
		if (natRs != null && natRs.getValue() != null)
			rs += natRs.getValue();

		return rs;
	}

	public List<Item> getItems() {

		if (items == null) {

			items = new LinkedList<Item>();

			List<Element> itemsElements = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_GEGENSTAENDE,
					Xml.KEY_GEGENSTAND);

			for (Element element : itemsElements) {

				if (element.getAttribute(Xml.KEY_NAME) != null) {

					Item item = DataManager.getItemByName(element.getAttributeValue(Xml.KEY_NAME));

					if (item != null) {

						item = (Item) item.duplicate();
						item.setElement(element);
						items.add(item);

					} else {
						Debug.warning("Item not found generating it:" + element.getAttributeValue(Xml.KEY_NAME));

						item = new Item();
						item.setName(element.getAttributeValue(Xml.KEY_NAME));
						item.addSpecification(new MiscSpecification(item, ItemType.Sonstiges));
						item.setElement(element);
						item.setId(UUID.randomUUID());
						item.setCategory("Sonstiges");
						items.add(item);
					}

					// fix invalid screen posisitons (items are always on the
					// last page if nothing else is defined
					if (item.getItemInfo().getScreen() == ItemLocationInfo.INVALID_POSITION) {
						item.getItemInfo().setScreen(Hero.MAXIMUM_SET_NUMBER);
					}
				}
			}

		}

		return items;
	}

	public Talent getTalent(String talentName) {
		if (talentByName == null) {
			getTalentGroups();
		}

		return talentByName.get(talentName);
	}

	public Spell getSpell(String spellName) {
		return getSpells().get(spellName);
	}

	private void replaceCombatTalent(Talent oldTalent, Talent newTalent) {

		TalentGroup tg;

		if (oldTalent != null) {
			talentByName.remove(oldTalent.getName());
			tg = talentGroups.get(TalentGroupType.Nahkampf);
			tg.getTalents().remove(oldTalent);
		}

		if (newTalent != null) {
			talentByName.put(newTalent.getName(), newTalent);
			tg = talentGroups.get(TalentGroupType.Nahkampf);
			tg.getTalents().add(newTalent);
		}

	}

	public Map<TalentGroupType, TalentGroup> getTalentGroups() {
		if (talentGroups == null) {
			talentGroups = new HashMap<TalentGroupType, TalentGroup>();

			talentByName = new HashMap<String, Talent>();

			List<Element> talentList = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_TALENTLISTE,
					Xml.KEY_TALENT);
			Talent talent;
			boolean found = false;
			for (int i = 0; i < talentList.size(); i++) {
				Element element = (Element) talentList.get(i);

				if (element.getAttribute(Xml.KEY_VALUE) == null)
					continue;

				talent = new Talent(this, element);
				found = false;
				for (TalentGroupType type : TalentGroupType.values()) {
					if (type.contains(talent.getName())) {

						CombatTalentType combatType = CombatTalentType.byName(talent.getName());

						if (combatType != null) {
							if (combatType.isFk()) {
								talent = new CombatDistanceTalent(this, talent.getElement());
							}
						}
						found = true;
						TalentGroup tg = talentGroups.get(type);
						if (tg != null) {
							tg.getTalents().add(talent);
						} else {
							tg = new TalentGroup(type);
							tg.getTalents().add(talent);
							talentGroups.put(type, tg);
						}

						break;
					}
				}
				if (!found) {
					Debug.warning("No Talentgroup found for:" + talent.getName());
				}

				talentByName.put(talent.getName(), talent);
			}

			// now replace regular talents with combattalents
			List<Element> combatAttributesList = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_KAMPF,
					Xml.KEY_KAMPFWERTE);

			for (Element element : combatAttributesList) {

				String talentName = element.getAttributeValue(Xml.KEY_NAME);
				talent = talentByName.get(talentName);
				Element talentElement;
				if (talent == null) {
					// create a fake element
					talentElement = new Element(Xml.KEY_TALENT);
					talentElement.setAttribute(Xml.KEY_NAME, talentName);
				} else {
					talentElement = talent.getElement();
				}

				CombatMeleeTalent combatTalent = new CombatMeleeTalent(this, talentElement, element);
				replaceCombatTalent(talent, combatTalent);

			}

			// add Peitsche as CombatTalent although Heldensoftware doesn't
			// treat is as one
			Talent peitsche = talent = talentByName.get(Talent.PEITSCHE);
			if (peitsche != null) {
				Element combatElement = new Element(Xml.KEY_KAMPFWERTE);
				combatElement.setAttribute(Xml.KEY_NAME, Talent.PEITSCHE);
				Element attacke = new Element(Xml.KEY_ATTACKE);
				attacke.setAttribute(Xml.KEY_VALUE,
						Integer.toString(getAttributeValue(AttributeType.at) + peitsche.getValue()));
				combatElement.addContent(attacke);
				CombatMeleeTalent combatTalent = new CombatMeleeTalent(this, peitsche.getElement(), combatElement);
				replaceCombatTalent(talent, combatTalent);
			}

			// add meta talents
			List<Talent> metaTalents = new ArrayList<Talent>(MetaTalentType.values().length);
			TalentGroup metaGroup = new TalentGroup(TalentGroupType.Meta);
			List<MetaTalentType> metaTalentTypes = new ArrayList<MetaTalent.MetaTalentType>(
					Arrays.asList(MetaTalentType.values()));

			for (MetaTalent metaTalent : getHeroConfiguration().getMetaTalents()) {
				metaTalents.add(metaTalent);
				talentByName.put(metaTalent.getName(), metaTalent);
				metaTalentTypes.remove(metaTalent.getMetaType());
			}

			for (MetaTalentType metaType : metaTalentTypes) {
				MetaTalent metaTalent = new MetaTalent(this, metaType);
				metaTalents.add(metaTalent);
				talentByName.put(metaTalent.getName(), metaTalent);
				getHeroConfiguration().addMetaTalent(metaTalent);
			}
			metaGroup.setTalents(metaTalents);
			talentGroups.put(TalentGroupType.Meta, metaGroup);

		}
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

	public Map<String, Art> getArts() {
		if (artsByName == null) {
			fillArtsAndSpecialFeatures();
		}
		return artsByName;
	}

	public Map<String, Spell> getSpells() {
		if (spellsByName == null) {
			List<Element> spellList = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_ZAUBERLISTE,
					Xml.KEY_ZAUBER);

			spellsByName = new TreeMap<String, Spell>();

			for (int i = 0; i < spellList.size(); i++) {
				Element element = (Element) spellList.get(i);
				Spell spell = new Spell(this, element);
				spellsByName.put(spell.getName(), spell);
			}
		}
		return spellsByName;
	}

	public Item getItem(String name) {

		for (Item item : getItems()) {
			if (item.getName().equals(name)) {
				return item;
			}
		}

		return null;
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

		// init talents
		if (talentByName == null) {
			getTalentGroups();
		}

		Talent talent = talentByName.get(name);

		if (talent == null) {

			Debug.verbose("Couldn't find " + name + " trying combattalents");
			// add missing combat talents with a value of base.
			CombatTalentType talentType = CombatTalentType.byName(name);
			if (talentType != null) {
				Element element = new Element(Xml.KEY_KAMPFWERTE);
				element.setAttribute(Xml.KEY_NAME, name);

				if (talentType.isFk()) {
					// TODO what shall be do in such a case???
				} else {
					Element attacke = new Element(Xml.KEY_ATTACKE);
					Element parade = new Element(Xml.KEY_PARADE);

					Element talentElement = new Element(Xml.KEY_TALENT);
					talentElement.setAttribute(Xml.KEY_NAME, name);
					// TODO what value do i have for a talent if i do not know
					// it?
					talentElement.setAttribute(Xml.KEY_VALUE, "0");

					attacke.setAttribute(Xml.KEY_VALUE, Util.toString(getAttributeValue(AttributeType.at)));
					parade.setAttribute(Xml.KEY_VALUE, Util.toString(getAttributeValue(AttributeType.pa)));

					element.addContent(attacke);
					element.addContent(parade);

					talent = new CombatMeleeTalent(this, talentElement, element);
					if (talent.getProbeInfo().getBe() != null)
						talentElement.setAttribute(Xml.KEY_BE, talent.getProbeInfo().getBe());
					else
						talentElement.setAttribute(Xml.KEY_BE, "");
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

		if (equippmentNode != null)
			equippmentNode.removeContent(equippedItem.getElement());
		else
			getHeldElement().removeContent(equippedItem.getElement());

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
			Debug.verbose("Reseting be");
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

			if (getLeRatio() < LeModificator.LEVEL_1)
				modificators.add(leModificator);

			if (getAuRatio() < AuModificator.LEVEL_1)
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

	public interface ItemAddedCallback {
		public void onItemAdded(Item item);

		public void onEquippedItemAdded(EquippedItem item);
	}

	public HeroConfiguration getHeroConfiguration() {
		return configuration;
	}

	/**
	 * 
	 */
	public void onPreHeroSaved() {
		Debug.verbose("Preparing hero to be saved. Populating data to XML.");
		if (attributes != null) {
			for (Attribute attribute : attributes.values()) {
				attribute.populateXml();
			}
		}

		if (talentByName != null) {
			for (Talent talent : talentByName.values()) {
				talent.populateXml();
			}
		}
		if (spellsByName != null) {
			for (Spell spell : spellsByName.values()) {
				spell.populateXml();
			}
		}
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

}
