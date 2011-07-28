package com.dsatab.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jdom.Element;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.dsatab.activity.DSATabApplication;
import com.dsatab.activity.DsaPreferenceActivity;
import com.dsatab.common.Util;
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
import com.dsatab.data.modifier.AuModifier;
import com.dsatab.data.modifier.LeModifier;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.drag.ItemLocationInfo;
import com.dsatab.view.listener.InventoryChangedListener;
import com.dsatab.view.listener.ModifierChangedListener;
import com.dsatab.view.listener.ValueChangedListener;
import com.dsatab.xml.DataManager;
import com.dsatab.xml.DomUtil;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

public class Hero {

	private static final String PREFIX_NKWAFFE = "nkwaffe";

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

	private List<SpecialFeature> specialFeatures;
	private List<Advantage> advantages;
	private List<Advantage> disadvantages;

	private Map<TalentGroupType, TalentGroup> talentGroups;
	private Map<String, Talent> talentByName;

	private CombatShieldTalent shieldTalent;
	private List<Spell> spells;
	private List<Event> events;
	private List<Item> items;

	private Map<Position, ArmorAttribute> armorAttributes;
	private Map<Position, WoundAttribute> wounds;

	private List<EquippedItem>[] equippedItems = null;

	private List<Element> beidhaendigerKampfElements = new LinkedList<Element>();

	private List<ModifierChangedListener> modifierChangedListeners = new LinkedList<ModifierChangedListener>();
	private List<InventoryChangedListener> inventoryChangedListeners = new LinkedList<InventoryChangedListener>();
	private List<Modificator> modifiers = new LinkedList<Modificator>();

	LeModifier leModifier;
	AuModifier auModifier;

	private Purse purse = null;

	private List<ValueChangedListener> listener = new LinkedList<ValueChangedListener>();

	private int activeSet = 0;

	private List<File> deletableAudioFiles = new LinkedList<File>();

	private Integer oldAuRatioLevel, oldLeRatioLevel;

	private Element talentsNode, spellsNode, equippmentNode, itemsNode, basisNode, attributesNode;

	@SuppressWarnings("unchecked")
	public Hero(String path, org.jdom.Document dom) {
		this.path = path;
		this.dom = dom;
		this.attributes = new HashMap<AttributeType, Attribute>(AttributeType.values().length);
		this.equippedItems = new List[MAXIMUM_SET_NUMBER];

		this.leModifier = new LeModifier(this);
		if (getLeRatio() < LeModifier.LEVEL_1)
			modifiers.add(leModifier);

		this.auModifier = new AuModifier(this);
		if (getAuRatio() < AuModifier.LEVEL_1)
			modifiers.add(auModifier);

		for (WoundAttribute attr : getWounds().values()) {
			if (attr.getValue() > 0)
				modifiers.add(attr);
		}

		talentsNode = getHeldElement().getChild(Xml.KEY_TALENTLISTE);
		spellsNode = getHeldElement().getChild(Xml.KEY_ZAUBERLISTE);

		equippmentNode = getHeldElement().getChild(Xml.KEY_AUSRUESTUNGEN_UE);
		if (equippmentNode != null) {
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
	}

	public void setPortraitUri(Uri uri) {
		getHeldElement().setAttribute(Xml.KEY_PORTRAIT_PATH, uri.toString());
	}

	public void setPortraitUri(URI uri) {
		getHeldElement().setAttribute(Xml.KEY_PORTRAIT_PATH, uri.toString());
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

				InputStream is = DSATabApplication.getInstance().getBaseContext().getContentResolver()
						.openInputStream(getPortraitUri());
				BufferedInputStream bis = new BufferedInputStream(is);
				portraitBitmap = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
			} catch (IOException e) {
				Debug.error("Error getting bitmap", e);
			}
		}
		return portraitBitmap;

	}

	public void addValueChangedListener(ValueChangedListener v) {
		listener.add(v);
	}

	public void removeValueChangeListener(ValueChangedListener v) {
		listener.remove(v);
	}

	public EquippedItem getEquippedItem(String name) {
		for (int i = 0; i < MAXIMUM_SET_NUMBER; i++) {
			for (EquippedItem item : getEquippedItems(i)) {
				if (item.getName().equals(name))
					return item;
			}
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

	public List<EquippedItem> getEquippedItems(int selectedSet) {
		if (equippedItems[selectedSet] == null) {
			equippedItems[selectedSet] = new LinkedList<EquippedItem>();

			List<Element> equippedElements = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_AUSRUESTUNGEN,
					Xml.KEY_HELDENAUSRUESTUNG);

			for (int i = 0; i < equippedElements.size(); i++) {
				Element element = (Element) equippedElements.get(i);

				if (element.getAttributeValue(Xml.KEY_NAME).equals("jagtwaffe"))
					continue;

				int set = 0;
				if (element.getAttribute(Xml.KEY_SET) != null) {
					set = Util.parseInt(element.getAttributeValue(Xml.KEY_SET));
					if (set != selectedSet)
						continue;
				}

				if (element.getAttributeValue(Xml.KEY_NAME).startsWith(PREFIX_BK)) {
					beidhaendigerKampfElements.add(element);
					continue;
				}

				EquippedItem item = new EquippedItem(this, element);

				// fix wrong screen iteminfo
				if (item.getItemInfo().getScreen() == ItemLocationInfo.INVALID_POSITION) {
					item.getItemInfo().setScreen(item.getSet());
				}

				if (item.getItem() != null) {
					equippedItems[selectedSet].add(item);
				} else {
					Debug.warning("Skipped EquippedItem because Item was not found: " + item.getItemName());
				}
			}

			// handle bk elements
			for (Element element : beidhaendigerKampfElements) {

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

					EquippedItem item1 = getEquippedItem(PREFIX_NKWAFFE + bk1);
					EquippedItem item2 = getEquippedItem(PREFIX_NKWAFFE + bk2);

					if (item2 != null && item1 != null) {
						item1.setSecondaryItem(item2);
						item2.setSecondaryItem(item1);
					} else {
						Debug.warning("Incorrect BeidhaengierKampf setting " + bk);
						getHeldElement().removeContent(element);
						beidhaendigerKampfElements.remove(element);
					}
				}

			}
			Util.sort(equippedItems[selectedSet]);

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
		// if (activeSet != this.activeSet)
		// equippedItems = null;

		int heroBe = getAttributeValue(AttributeType.Behinderung);
		int armorBe = getArmorBe();

		boolean resetBe = heroBe == armorBe;

		this.activeSet = activeSet;

		if (resetBe) {
			getAttribute(AttributeType.Behinderung).setValue(getArmorBe());

			fireValueChangedEvent(getAttribute(AttributeType.Behinderung));
		}
	}

	public void addModifierChangedListener(ModifierChangedListener listener) {
		modifierChangedListeners.add(listener);
	}

	public void removeModifierChangedListener(ModifierChangedListener listener) {
		modifierChangedListeners.remove(listener);
	}

	public void addInventoryChangedListener(InventoryChangedListener listener) {
		inventoryChangedListeners.add(listener);
	}

	public void removeInventoryChangedListener(InventoryChangedListener listener) {
		inventoryChangedListeners.remove(listener);
	}

	void fireModifiersChangedEvent(List<Modificator> modifiers) {

		this.modifiers = modifiers;

		for (ModifierChangedListener listener : modifierChangedListeners) {
			listener.onModifiersChanged(modifiers);
		}
	}

	public void fireValueChangedEvent(Value value) {
		if (value instanceof Attribute) {

			Attribute attribute = (Attribute) value;

			switch (attribute.getType()) {
			case Mut:
			case Klugheit:
			case Intuition:
			case Charisma:
			case Fingerfertigkeit:
			case Gewandtheit:
			case Konstitution:
			case Körperkraft:
				Attribute attr = getAttribute(AttributeType.at);
				attr.setValue(attr.getReferenceValue());

				attr = getAttribute(AttributeType.pa);
				attr.setValue(attr.getReferenceValue());

				attr = getAttribute(AttributeType.fk);
				attr.setValue(attr.getReferenceValue());

				attr = getAttribute(AttributeType.ini);
				attr.setValue(attr.getReferenceValue());
				break;
			case Ausdauer:
				postAuRatioCheck();
				break;
			case Lebensenergie:
				postLeRatioCheck();
				break;
			case Lebensenergie_Total:
				getAttribute(AttributeType.Lebensenergie).setReferenceValue(value.getValue());
				postLeRatioCheck();
				break;
			case Ausdauer_Total:
				getAttribute(AttributeType.Ausdauer).setReferenceValue(value.getValue());
				postAuRatioCheck();
				break;
			case Astralenergie_Total:
				getAttribute(AttributeType.Astralenergie).setReferenceValue(value.getValue());
				break;
			case Karmaenergie_Total:
				getAttribute(AttributeType.Karmaenergie).setReferenceValue(value.getValue());
				break;
			}
		}

		for (ValueChangedListener l : listener) {
			l.onValueChanged(value);
		}

	}

	void fireModifierChangedEvent(Modificator modifier) {
		for (ModifierChangedListener listener : modifierChangedListeners) {
			listener.onModifierChanged(modifier);
		}
	}

	public void onHeroSaved() {
		for (File f : deletableAudioFiles) {
			f.delete();
		}
	}

	void fireItemAddedEvent(Item item) {
		for (InventoryChangedListener listener : inventoryChangedListeners) {
			listener.onItemAdded(item);
		}
	}

	void fireItemRemovedEvent(Item item) {
		for (InventoryChangedListener listener : inventoryChangedListeners) {
			listener.onItemRemoved(item);
		}
	}

	void fireItemEquippedEvent(EquippedItem item) {
		for (InventoryChangedListener listener : inventoryChangedListeners) {
			listener.onItemEquipped(item);
		}
	}

	public void fireItemChangedEvent(EquippedItem item) {
		for (InventoryChangedListener listener : inventoryChangedListeners) {
			listener.onItemChanged(item);
		}
	}

	void fireItemUnequippedEvent(EquippedItem item) {
		for (InventoryChangedListener listener : inventoryChangedListeners) {
			listener.onItemUnequipped(item);
		}
	}

	void fireModifierAddedEvent(Modificator modifier) {

		modifiers.add(modifier);

		for (ModifierChangedListener listener : modifierChangedListeners) {
			listener.onModifierAdded(modifier);
		}
	}

	void fireModifierRemovedEvent(Modificator modifier) {

		modifiers.remove(modifier);

		for (ModifierChangedListener listener : modifierChangedListeners) {
			listener.onModifierRemoved(modifier);
		}
	}

	/**
	 * @param item
	 */
	public void removeItem(Item item) {
		List<Item> items = getItems();
		items.remove(item);
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
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Wähle ein Talent...");
					builder.setItems(talentNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							addItem(context, item, combatTalents.get(which), set, callback);
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

		fireItemAddedEvent(item);

		if (callback != null)

			if (item.isEquipable() && set >= 0) {
				EquippedItem equippedItem = addEquippedItem(context, item, itemSpecification, newTalent, set);
				callback.onEquippedItemAdded(equippedItem);
			} else {
				callback.onItemAdded(item);
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

		return true;
	}

	public EquippedItem addEquippedItem(Context context, Item item, CombatTalent talent) {
		return addEquippedItem(context, item, null, talent, getActiveSet());
	}

	public EquippedItem addEquippedItem(Context context, Item item, CombatTalent talent, final int set) {
		return addEquippedItem(context, item, null, talent, set);
	}

	public EquippedItem addEquippedItem(Context context, Item item, ItemSpecification itemSpecification,
			CombatTalent talent, final int set) {

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

		getEquippedItems(set).add(equippedItem);

		if (item.hasSpecification(Armor.class)) {
			resetArmorAttributes();
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

	private Element getHeldElement() {
		if (held == null)
			held = (Element) dom.getRootElement().getChild(Xml.KEY_HELD);

		return held;
	}

	public Attribute getAttribute(AttributeType type) {
		Attribute attribute = attributes.get(type);

		if (attribute == null) {

			List<Element> attributes = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_EIGENSCHAFTEN,
					Xml.KEY_EIGENSCHAFT);
			for (int i = 0; i < attributes.size(); i++) {
				Element attributeElement = (Element) attributes.get(i);

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

			if (type == AttributeType.Behinderung && !this.attributes.containsKey(AttributeType.Behinderung)) {
				Element element = new Element(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.Behinderung.name());
				element.setAttribute(Xml.KEY_VALUE, "0");

				if (attributesNode == null)
					getHeldElement().addContent(element);
				else
					attributesNode.addContent(element);

				Attribute be = new Attribute(element, this);
				be.setValue(getArmorBe());
				this.attributes.put(AttributeType.Behinderung, be);
			}
			if (type == AttributeType.Ausweichen && !this.attributes.containsKey(AttributeType.Ausweichen)) {
				Element element = new Element(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.Ausweichen.name());
				if (attributesNode == null)
					getHeldElement().addContent(element);
				else
					attributesNode.addContent(element);

				this.attributes.put(AttributeType.Ausweichen, new Attribute(element, this));
			}

			if (type == AttributeType.Initiative_Aktuell
					&& !this.attributes.containsKey(AttributeType.Initiative_Aktuell)) {
				Element element = new Element(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.Initiative_Aktuell.name());
				element.setAttribute(Xml.KEY_VALUE, "0");
				if (attributesNode == null)
					getHeldElement().addContent(element);
				else
					attributesNode.addContent(element);
				this.attributes.put(AttributeType.Initiative_Aktuell, new Attribute(element, this));
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

			if (type == AttributeType.pa && !this.attributes.containsKey(AttributeType.pa)) {
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

			if (type == AttributeType.at && !this.attributes.containsKey(AttributeType.at)) {
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

			if (type == AttributeType.ini && !this.attributes.containsKey(AttributeType.ini)) {
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

			attribute = this.attributes.get(type);
		}

		return attribute;
	}

	public Map<Position, ArmorAttribute> getArmorAttributes() {
		if (armorAttributes == null) {
			armorAttributes = new HashMap<Position, ArmorAttribute>(Position.values().length);

			@SuppressWarnings("unchecked")
			List<Element> rsNodes = getHeldElement().getChildren(Xml.KEY_RUESTUNGSSCHUTZ);
			List<Element> remove = new LinkedList<Element>();

			final List<Position> armorPositions = DSATabApplication.getInstance().getConfiguration()
					.getArmorPositions();
			for (Element rsNode : rsNodes) {
				try {
					ArmorAttribute rs = new ArmorAttribute(rsNode, this);

					if (armorPositions.contains(rs.getPosition())) {
						armorAttributes.put(rs.getPosition(), rs);
					}
				} catch (Exception e) {
					remove.add(rsNode);
					Debug.warn(e);
				}
			}

			for (Element node : remove)
				getHeldElement().removeContent(node);

			// fill not existing values with 0
			for (Position pos : armorPositions) {

				ArmorAttribute rs = armorAttributes.get(pos);

				if (rs == null) {
					Element rsNode = new Element(Xml.KEY_RUESTUNGSSCHUTZ);
					rsNode.setAttribute(Xml.KEY_NAME, pos.name());
					rsNode.setAttribute(Xml.KEY_VALUE, Integer.toString(getArmorRs(pos)));
					getHeldElement().addContent(rsNode);
					rs = new ArmorAttribute(rsNode, this);
					armorAttributes.put(pos, rs);
				}
			}

		}
		return armorAttributes;
	}

	public Map<Position, WoundAttribute> getWounds() {
		if (wounds == null) {
			wounds = new HashMap<Position, WoundAttribute>(5);

			@SuppressWarnings("unchecked")
			List<Element> rsNodes = getHeldElement().getChildren(Xml.KEY_WUNDE);

			List<Element> remove = new LinkedList<Element>();
			for (Element rsNode : rsNodes) {
				try {
					WoundAttribute rs = new WoundAttribute(this, rsNode);
					wounds.put(rs.getPosition(), rs);
				} catch (Exception e) {
					remove.add(rsNode);
					Debug.warn(e);
				}
			}

			for (Element node : remove) {
				getHeldElement().removeContent(node);
			}

			// fill not existing values with 0
			for (Position pos : DSATabApplication.getInstance().getConfiguration().getWoundPositions()) {

				WoundAttribute rs = wounds.get(pos);

				if (rs == null) {

					Element rsNode = new Element(Xml.KEY_WUNDE);
					rsNode.setAttribute(Xml.KEY_NAME, pos.name());
					rsNode.setAttribute(Xml.KEY_VALUE, "0");
					getHeldElement().addContent(rsNode);
					rs = new WoundAttribute(this, rsNode);
					wounds.put(pos, rs);
				}
			}

		}
		return wounds;
	}

	public void setWounds(Map<Position, WoundAttribute> wounds) {

		if (this.wounds != null) {
			for (WoundAttribute w : this.wounds.values()) {
				if (w.getValue() > 0)
					fireModifierRemovedEvent(w);
			}
		}

		this.wounds = wounds;

		for (WoundAttribute w : this.wounds.values()) {
			if (w.getValue() > 0)
				fireModifierAddedEvent(w);
		}
	}

	public void setArmorAttributes(Map<Position, ArmorAttribute> armorAttributes) {
		this.armorAttributes = armorAttributes;
	}

	public Integer getGewicht() {

		Element rasse = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_RASSE);
		if (rasse != null) {
			Element groesse = rasse.getChild(Xml.KEY_GROESSE);
			if (groesse != null) {
				return Util.parseInt(groesse.getAttributeValue(Xml.KEY_GEWICHT));
			}
		}

		return null;
	}

	public Integer getGroesse() {

		Element rasse = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_RASSE);
		if (rasse != null) {
			Element groesse = rasse.getChild(Xml.KEY_GROESSE);
			if (groesse != null) {
				return Util.parseInt(groesse.getAttributeValue(Xml.KEY_VALUE));
			}
		}
		return null;
	}

	public Integer getAlter() {
		Element rasse = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_RASSE);
		if (rasse != null) {
			Element aussehen = rasse.getChild(Xml.KEY_AUSSEHEN);
			if (aussehen != null) {
				return Util.parseInt(aussehen.getAttributeValue(Xml.KEY_ALTER));
			}
		}
		return null;

	}

	public String getAugenFarbe() {
		Element rasse = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_RASSE);
		if (rasse != null) {
			Element aussehen = rasse.getChild(Xml.KEY_AUSSEHEN);
			if (aussehen != null) {
				return aussehen.getAttributeValue(Xml.KEY_EYECOLOR);
			}
		}
		return null;
	}

	public String getHaarFarbe() {
		Element rasse = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_RASSE);
		if (rasse != null) {
			Element aussehen = rasse.getChild(Xml.KEY_AUSSEHEN);
			if (aussehen != null) {
				return aussehen.getAttributeValue(Xml.KEY_HAIRCOLOR);
			}
		}
		return null;
	}

	public String getAusbildung() {

		Element ausbildungen = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_AUSBILDUNGEN);

		if (ausbildungen != null) {
			@SuppressWarnings("unchecked")
			List<Element> ausbildungElements = ausbildungen.getChildren();

			for (Element ausbildung : ausbildungElements) {
				String value = ausbildung.getAttributeValue(Xml.KEY_STRING);
				if (!TextUtils.isEmpty(value))
					return value;
			}
		}

		Element ausbildung = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_AUSBILDUNG);
		if (ausbildung != null) {
			String value = ausbildung.getAttributeValue(Xml.KEY_STRING);
			if (!TextUtils.isEmpty(value))
				return value;
		}

		Element profession = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_PROFESSION);
		if (profession != null) {
			String value = profession.getAttributeValue(Xml.KEY_STRING);
			if (!TextUtils.isEmpty(value))
				return value;
		}

		return null;

	}

	public String getHerkunft() {

		Element rasse = DomUtil.getChildByTagName(getHeldElement(), Xml.KEY_BASIS, Xml.KEY_RASSE);

		if (rasse != null) {
			return rasse.getAttributeValue(Xml.KEY_STRING);
		} else
			return null;
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

		double newLeRatioCheck = getLeRatio();

		int newLeRatioLevel = 0;

		if (newLeRatioCheck < 0.25)
			newLeRatioLevel = 3;
		else if (newLeRatioCheck < 0.33)
			newLeRatioLevel = 2;
		else if (newLeRatioCheck < 0.5)
			newLeRatioLevel = 1;

		if (oldLeRatioLevel == null || oldLeRatioLevel == 0) {
			if (newLeRatioLevel > 0)
				fireModifierAddedEvent(leModifier);
		} else if (oldLeRatioLevel > 0 && newLeRatioLevel == 0)
			fireModifierRemovedEvent(leModifier);
		else if (oldLeRatioLevel != newLeRatioLevel)
			fireModifierChangedEvent(leModifier);

		oldLeRatioLevel = newLeRatioLevel;

	}

	protected void postAuRatioCheck() {

		double newAuRatioCheck = getAuRatio();

		int newAuRatioLevel = 0;
		if (newAuRatioCheck < 0.25)
			newAuRatioLevel = 2;
		else if (newAuRatioCheck < 0.33)
			newAuRatioLevel = 1;

		if (oldAuRatioLevel == null || oldAuRatioLevel == 0) {
			if (newAuRatioLevel > 0)
				fireModifierAddedEvent(auModifier);
		} else if (oldAuRatioLevel > 0 && newAuRatioLevel == 0)
			fireModifierRemovedEvent(auModifier);
		else if (oldAuRatioLevel != newAuRatioLevel)
			fireModifierChangedEvent(auModifier);

		oldAuRatioLevel = newAuRatioLevel;

	}

	/**
	 * @return
	 */
	public double getLeRatio() {
		Attribute le = getAttribute(AttributeType.Lebensenergie);
		return ((double) le.getValue()) / le.getReferenceValue();
	}

	/**
	 * @return
	 */
	public double getAuRatio() {
		Attribute au = getAttribute(AttributeType.Ausdauer);
		return ((double) au.getValue()) / au.getReferenceValue();
	}

	public List<Modifier> getModificator(Probe probe) {

		List<Modifier> modifiers = new LinkedList<Modifier>();

		if (DSATabApplication.getInstance().isLiteVersion())
			return modifiers;

		for (Modificator modificator : getModifiers()) {
			Modifier mod = modificator.getModifier(probe);

			Debug.verbose("Modificator of " + mod.getTitle() + " is " + mod.getModifier());
			if (mod.getModifier() != 0) {
				modifiers.add(mod);
			}
		}

		if (probe instanceof Attribute) {
			Attribute attribute = (Attribute) probe;

			if (attribute.getType() == AttributeType.ini) {
				if (attribute.getHero().hasFeature(SpecialFeature.KAMPFGESPUER))
					modifiers.add(new Modifier(2, "Kampfgespür", ""));

				if (attribute.getHero().hasFeature(SpecialFeature.KAMPFREFLEXE))
					modifiers.add(new Modifier(4, "Kampfreflexe", ""));
			}
		}

		if (probe instanceof CombatProbe) {
			CombatProbe combatProbe = (CombatProbe) probe;
			EquippedItem equippedItem = combatProbe.getEquippedItem();

			if (equippedItem != null && equippedItem.getItemSpecification() instanceof Weapon) {
				Item item = equippedItem.getItem();
				Weapon weapon = (Weapon) equippedItem.getItemSpecification();

				if (combatProbe.isAttack()) {
					Debug.verbose("Hauptwaffe Wm Attack is " + weapon.getWmAt());
					modifiers.add(new Modifier(weapon.getWmAt(), "Waffenmodifikator At", ""));
				} else {
					Debug.verbose("Hauptwaffe Wm Defense is " + weapon.getWmPa());
					modifiers.add(new Modifier(weapon.getWmPa(), "Waffenmodifikator Pa", ""));
				}

				for (SpecialFeature special : getSpecialFeatures()) {
					if (special.getName().startsWith("Talentspezialisierung")
							&& special.getName().endsWith(item.getName() + ")")) {
						Debug.verbose("Talentspezialisierung " + item.getName());

						modifiers.add(new Modifier(1, special.getName(), ""));

						Debug.verbose(special.getName() + " AT/PA+1");
					}
				}

				// waffenlose kampftechniken +1/+1
				if (item.getName().startsWith("Raufen")) {
					if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL, ""));
					}
					if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST, ""));
					}
					if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO, ""));
					}
					if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT, ""));
					}
				}

				if (item.getName().startsWith("Hruruzat")) {
					if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL, ""));
					}
					if (hasFeature(SpecialFeature.WK_HAMMERFAUST)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HAMMERFAUST, ""));
					}
					if (hasFeature(SpecialFeature.WK_MERCENARIO)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_MERCENARIO, ""));
					}
					if (hasFeature(SpecialFeature.WK_HRURUZAT)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_HRURUZAT, ""));
					}
				}

				if (item.getName().startsWith("Ringen")) {
					if (hasFeature(SpecialFeature.WK_UNAUER_SCHULE)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_UNAUER_SCHULE, ""));
					}
					if (hasFeature(SpecialFeature.WK_BORNLAENDISCH)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_BORNLAENDISCH, ""));
					}
					if (hasFeature(SpecialFeature.WK_GLADIATORENSTIL)) {
						modifiers.add(new Modifier(1, SpecialFeature.WK_GLADIATORENSTIL, ""));
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

						modifiers.add(new Modifier(shield.getWmAt(), "Schildkampf Modifikator At", ""));

						Debug.verbose("Hauptwaffenattacke is reduziert um Schild WM " + shield.getWmAt());
					}
				}
			}
			if (equippedItem != null && equippedItem.getItemSpecification() instanceof Shield) {
				Shield shield = (Shield) equippedItem.getItemSpecification();

				if (combatProbe.isAttack()) {
					Debug.verbose("Shield Wm Attack is " + shield.getWmAt());

					modifiers.add(new Modifier(shield.getWmAt(), "Schildmodifikator At", ""));
				} else {
					Debug.verbose("Shield Wm Defense is " + shield.getWmPa());
					modifiers.add(new Modifier(shield.getWmPa(), "Schildmodifikator PA", ""));

					// paradevalue is increased by 1 if weaponparade is above
					// 15
					if (shield.isShield()) {
						EquippedItem equippedWeapon = equippedItem.getSecondaryItem();

						if (equippedWeapon != null) {
							int defenseValue = equippedWeapon.getTalent().getDefense().getProbeValue(0);

							if (defenseValue >= 21) {
								Debug.verbose("Shield: Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +3");

								modifiers.add(new Modifier(3, "Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +3", ""));
							} else if (defenseValue >= 18) {
								Debug.verbose("Shield: Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +2");
								modifiers.add(new Modifier(2, "Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +2", ""));
							} else if (defenseValue >= 15) {
								Debug.verbose("Shield: Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +1");
								modifiers.add(new Modifier(1, "Hauptwaffe hat Paradewert von " + defenseValue
										+ ". Schildparade +1", ""));
							}
						}
					}
				}
			}

		}

		return modifiers;
	}

	public Integer getModifiedValue(AttributeType type) {
		int modifier = 0;

		if (!DSATabApplication.getInstance().isLiteVersion()) {
			for (Modificator m : getModifiers()) {
				modifier += m.getModifier(type).getModifier();
			}
		}

		if (getAttributeValue(type) != null)
			return getAttributeValue(type) + modifier;
		else
			return null;
	}

	public Integer getAttributeValue(AttributeType type) {
		Attribute attribute = getAttribute(type);

		if (attribute != null)
			return attribute.getValue();
		else
			return null;
	}

	public List<SpecialFeature> getSpecialFeatures() {
		if (specialFeatures == null) {

			List<Element> sfs = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_SONDERFERTIGKEITEN,
					Xml.KEY_SONDERFERTIGKEIT);

			specialFeatures = new LinkedList<SpecialFeature>();

			for (Element feat : sfs) {
				specialFeatures.add(new SpecialFeature(feat));
			}
		}

		return specialFeatures;

	}

	public List<Advantage> getAdvantages() {
		if (advantages == null) {

			List<Element> sfs = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_VORTEILE, Xml.KEY_VORTEIL);

			advantages = new LinkedList<Advantage>();

			for (Element feat : sfs) {
				advantages.add(new Advantage(feat));
			}
		}

		return advantages;
	}

	public List<Advantage> getDisadvantages() {
		if (disadvantages == null) {
			List<Element> sfs = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_VORTEILE, Xml.KEY_NACHTEIL);

			disadvantages = new LinkedList<Advantage>();

			for (Element feat : sfs) {
				disadvantages.add(new Advantage(feat));
			}
		}

		return disadvantages;
	}

	public SpecialFeature getSpecialFeature(String name) {
		for (SpecialFeature sf : getSpecialFeatures()) {
			if (sf.getName().equals(name)) {
				return sf;
			}
		}
		return null;
	}

	public boolean hasFeature(String name) {
		boolean found = false;

		found = getSpecialFeature(name) != null;

		if (!found) {
			for (Advantage adv : getAdvantages()) {
				if (adv.getName().equals(name)) {
					found = true;
					break;
				}
			}
		}

		if (!found) {
			for (Advantage adv : getDisadvantages()) {
				if (adv.getName().equals(name)) {
					found = true;
					break;
				}
			}
		}

		return found;

	}

	public Event addEvent(EventCategory category, String message, String audioPath) {

		Element element = new Element(Xml.KEY_EREIGNIS);
		getEventsElement().addContent(element);
		Event event = new Event(element);
		event.setCategory(category);
		event.setComment(message);
		event.setAudioPath(audioPath);
		getEvents().add(event);
		return event;
	}

	public Element getEventsElement() {
		if (ereignisse == null) {

			ereignisse = getHeldElement().getChild(Xml.KEY_EREIGNISSE);

			if (events == null) {
				ereignisse = new Element(Xml.KEY_EREIGNISSE);
				getHeldElement().addContent(ereignisse);
			}
		}
		return ereignisse;
	}

	public List<Event> getEvents() {
		if (events == null) {
			events = new LinkedList<Event>();

			@SuppressWarnings("unchecked")
			List<Element> eventElements = getEventsElement().getChildren(Xml.KEY_EREIGNIS);

			for (Element element : eventElements) {

				if (element.getAttribute(Xml.KEY_ABENTEUERPUNKTE_UPPER) != null
						|| element.getAttribute(Xml.KEY_OBJ) != null)
					continue;

				if ("Sonstiges Ereignis (Hinzugewinn)".equals(element.getAttributeValue(Xml.KEY_TEXT))) {
					events.add(new Event(element));
				}
			}

		}

		return events;
	}

	public int getBe(Probe probe) {
		int heroBe = 0;

		if (probe.getBe() != null) {

			// base hero be
			heroBe = getAttributeValue(AttributeType.Behinderung);

			Debug.verbose("Proben BE is " + probe.getBe() + ", Hero has a base BE of " + heroBe);

			heroBe = Math.abs(Math.min(0, Util.modifyBe(0, probe.getBe(), heroBe)));

			Debug.verbose("Full BE after modifications is " + heroBe);

			boolean isAttack = false;
			boolean isDefense = false;
			boolean halfBe = false;

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
					Debug.verbose("Offensive Attack Half BE is " + heroBe);
				} else if (getCombatStyle() == CombatStyle.Defensive && isDefense) {
					heroBe = (int) Math.floor(heroBe / 2.0);
					Debug.verbose("Defensive Parade Half BE is " + heroBe);
				} else {
					heroBe = (int) Math.ceil(heroBe / 2.0);
					Debug.verbose("Half BE is " + heroBe);
				}
			}

		}
		return heroBe;
	}

	public List<Item> getItems(ItemType... types) {
		List<Item> result = new LinkedList<Item>();
		List<ItemType> typeList = Arrays.asList(types);

		for (Item item : getItems()) {
			for (ItemSpecification spec : item.getSpecifications()) {
				if (typeList.contains(spec.getType())) {
					result.add(item);
					break;
				}
			}
		}

		return result;
	}

	public int getGs() {
		int base = 7;

		int ge = getAttributeValue(AttributeType.Gewandtheit);
		if (ge >= 16)
			base += 2;
		else if (ge >= 11)
			base += 1;

		if (hasFeature(SpecialFeature.FLINK))
			base++;
		if (hasFeature(SpecialFeature.BEHAEBIG))
			base--;
		if (hasFeature(SpecialFeature.EINBEINIG))
			base -= 3;
		if (hasFeature(SpecialFeature.KLEINWUECHSIG))
			base -= 1;
		if (hasFeature(SpecialFeature.LAHM))
			base -= 1;
		if (hasFeature(SpecialFeature.ZWERGENWUCHS))
			base -= 2;

		base -= getAttributeValue(AttributeType.Behinderung);

		base -= getWounds().get(Position.Bauch).getValue();
		base -= getWounds().get(Position.LowerLeg).getValue();

		return base;
	}

	public int[] getWundschwelle() {
		int[] ws = new int[3];

		SharedPreferences preferences = DSATabApplication.getPreferences();
		int wsBase = 0;
		int wsMod = 0;
		if (preferences.getBoolean(DsaPreferenceActivity.KEY_HOUSE_RULES, false)) {
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
				rs1Armor = rs1.getGegenstand();
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
					be += armor.getBe();

					if (rs1Armor != null && rs1Armor.equals(equippedItem.getItemName())) {
						be -= 1.0;
						rs1Armor = null;
					}
				}
			}
			break;

		}
		}

		Debug.verbose("Finish Be calc");

		return Math.max(0, (int) Math.ceil(be));
	}

	/**
	 * A general overall Rs value calculated using the zone system sum with
	 * multipliers
	 * 
	 * @return
	 */
	public int getArmorRs() {

		int totalRs = 0;

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
					totalRs += armor.getTotalRs();
				}
			}
			break;
		}
		return totalRs;

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
				}
			}

		}

		return items;
	}

	public static String getChildValue(Element node, String childTagName, String childParamName) {
		Element child = node.getChild(childTagName);

		if (child != null) {
			return child.getAttributeValue(childParamName);
		} else {
			return null;
		}
	}

	public Talent getTalent(String talentName) {
		if (talentByName == null) {
			getTalentGroups();
		}

		return talentByName.get(talentName);
	}

	private void replaceTalent(Talent oldTalent, Talent newTalent) {

		TalentGroup tg;

		if (oldTalent != null) {
			talentByName.remove(oldTalent.getName());
			tg = talentGroups.get(oldTalent.getType());
			tg.getTalents().remove(oldTalent);
		}

		if (newTalent != null) {
			talentByName.put(newTalent.getName(), newTalent);
			tg = talentGroups.get(newTalent.getType());
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
						talent.setType(type);
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

			List<CombatTalentType> missingTypes = new ArrayList<CombatTalentType>(Arrays.asList(CombatTalentType
					.values()));

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
				combatTalent.setType(TalentGroupType.Kampf);
				replaceTalent(talent, combatTalent);

				missingTypes.remove(combatTalent.getType());

			}

		}
		return talentGroups;
	}

	public CombatStyle getCombatStyle() {
		if (getHeldElement().getAttribute(Xml.KEY_COMBATSTYLE) != null)
			return CombatStyle.valueOf(getHeldElement().getAttributeValue(Xml.KEY_COMBATSTYLE));
		else
			return CombatStyle.Offensive;
	}

	public void setCombatStyle(CombatStyle style) {
		if (style != null)
			getHeldElement().setAttribute(Xml.KEY_COMBATSTYLE, style.name());
		else
			getHeldElement().removeAttribute(Xml.KEY_COMBATSTYLE);
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

	public List<Spell> getSpells() {
		if (spells == null) {
			List<Element> spellList = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_ZAUBERLISTE,
					Xml.KEY_ZAUBER);

			spells = new ArrayList<Spell>(spellList.size());

			for (int i = 0; i < spellList.size(); i++) {
				Element element = (Element) spellList.get(i);
				spells.add(new Spell(this, element));
			}
		}

		Collections.sort(spells, Spell.NAME_COMPARATOR);
		return spells;
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

			// add missing combat talents with a value of base.
			CombatTalentType talentType = CombatTalentType.byName(name);

			Element element = new Element(Xml.KEY_KAMPFWERTE);
			element.setAttribute(Xml.KEY_NAME, name);

			if (talentType.isFk()) {
				// TODO what shall be do in such a case???
			} else {
				Element attacke = new Element(Xml.KEY_ATTACKE);
				Element parade = new Element(Xml.KEY_PARADE);

				Element talentElement = new Element(Xml.KEY_TALENT);
				talentElement.setAttribute(Xml.KEY_NAME, name);
				talentElement.setAttribute(Xml.KEY_BE, talentType.getBe());
				// TODO what value do i have for a talent if i do not know it?
				talentElement.setAttribute(Xml.KEY_VALUE, "0");

				attacke.setAttribute(Xml.KEY_VALUE, Util.toString(getAttributeValue(AttributeType.at)));
				parade.setAttribute(Xml.KEY_VALUE, Util.toString(getAttributeValue(AttributeType.pa)));

				element.addContent(attacke);
				element.addContent(parade);

				talent = new CombatMeleeTalent(this, talentElement, element);
				talent.setType(TalentGroupType.Kampf);
			}

		}

		if (talent instanceof BaseCombatTalent)
			return (BaseCombatTalent) talent;
		else
			return null;
	}

	public void setName(String name) {
		getHeldElement().setAttribute(Xml.KEY_NAME, name);
	}

	public void removeEvent(Event event) {
		if (event.getAudioPath() != null) {

			File audioFile = new File(event.getAudioPath());
			if (audioFile.exists())
				deletableAudioFiles.add(audioFile);
		}

		getEvents().remove(event);
		getEventsElement().removeContent(event.getElement());
	}

	public void removeEquippedItem(EquippedItem equippedItem) {

		if (equippedItem.getSecondaryItem() != null) {
			equippedItem.getSecondaryItem().setSecondaryItem(null);
		}

		getHeldElement().removeContent(equippedItem.getElement());

		for (int i = 0; i < MAXIMUM_SET_NUMBER; i++) {
			getEquippedItems(i).remove(equippedItem);
		}

		if (equippedItem.getItem().hasSpecification(Armor.class)) {
			resetArmorAttributes();
		}

		fireItemUnequippedEvent(equippedItem);
	}

	public List<Modificator> getModifiers() {
		if (DSATabApplication.getInstance().isLiteVersion())
			return Collections.emptyList();
		else
			return modifiers;
	}

	public void reloadArmorAttributes() {
		armorAttributes = null;
	}

	public void resetArmorAttributes() {
		for (ArmorAttribute a : getArmorAttributes().values()) {
			int newArmor = getArmorRs(a.getPosition());
			if (newArmor != a.getValue()) {
				a.setValue(newArmor);
				fireValueChangedEvent(a);
			}
		}
	}

	public interface ItemAddedCallback {
		public void onItemAdded(Item item);

		public void onEquippedItemAdded(EquippedItem item);
	}
}
