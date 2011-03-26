package com.dsatab.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.dsatab.data.items.ItemType;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.LeModifier;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.listener.ModifierChangedListener;
import com.dsatab.view.listener.ValueChangedListener;
import com.dsatab.xml.DataManager;
import com.dsatab.xml.DomUtil;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

public class Hero {

	private static final String PREFIX_NKWAFFE = "nkwaffe";

	private static final String PREFIX_BK = "bk";

	public enum CombatStyle {
		Offensive, Defensive
	}

	private String path;

	private Document dom;

	private Element ereignisse;

	private Element held;

	private Element experience;

	private Map<AttributeType, Attribute> attributes;

	private List<SpecialFeature> specialFeatures;
	private List<Advantage> advantages;
	private List<Advantage> disadvantages;

	private Map<TalentGroupType, TalentGroup> talentGroups;
	private Map<String, Talent> talentByName;

	private List<CombatMeleeTalent> combatTalents;
	private CombatShieldTalent shieldTalent;
	private List<Spell> spells;
	private List<CombatDistanceTalent> combatDistanceTalents;
	private List<Event> events;
	private Map<ItemType, List<Item>> items;

	private Map<Position, ArmorAttribute> armorAttributes;
	private Map<Position, WoundAttribute> wounds;

	private List<EquippedItem>[] equippedItems = null;

	private List<Element> beidhaendigerKampfElements = new LinkedList<Element>();

	private List<ModifierChangedListener> modifierChangedListeners = new LinkedList<ModifierChangedListener>();
	private List<Modificator> modifiers = new LinkedList<Modificator>();

	LeModifier leModifier;

	private Purse purse = null;

	private List<ValueChangedListener> listener = new LinkedList<ValueChangedListener>();

	private int activeSet = 0;

	private List<File> deletableAudioFiles = new LinkedList<File>();

	public Hero(String path, Document dom) {
		this.path = path;
		this.dom = dom;
		this.attributes = new HashMap<AttributeType, Attribute>(AttributeType.values().length);
		this.equippedItems = (LinkedList<EquippedItem>[]) new LinkedList[3];

		this.leModifier = new LeModifier(this);
		if (getLeRatio() < 0.5)
			modifiers.add(leModifier);

		for (WoundAttribute attr : getWounds().values()) {
			if (attr.getValue() > 0)
				modifiers.add(attr);
		}

	}

	public void addValueChangedListener(ValueChangedListener v) {
		listener.add(v);
	}

	public void removeValueChangeListener(ValueChangedListener v) {
		listener.remove(v);
	}

	public EquippedItem getEquippedItem(String name) {

		for (EquippedItem item : getEquippedItems()) {
			if (item.getName().equals(name))
				return item;
		}
		return null;

	}

	public Purse getPurse() {
		if (purse == null) {

			NodeList purseElements = getHeldElement().getElementsByTagName(Xml.KEY_GELDBOERSE);

			if (purseElements.getLength() > 0) {
				Element purseElement = (Element) purseElements.item(0);
				purse = new Purse(purseElement);
			}
		}
		return purse;
	}

	public List<EquippedItem> getEquippedItems(Class<? extends Item> itemClass) {

		List<EquippedItem> items = new LinkedList<EquippedItem>();
		for (EquippedItem ei : getEquippedItems()) {

			Item item = ei.getItem();

			if (itemClass.isAssignableFrom(item.getClass())) {
				items.add(ei);
			}

		}

		return items;

	}

	public List<EquippedItem> getEquippedItems() {
		return getEquippedItems(activeSet);
	}

	public List<EquippedItem> getEquippedItems(int selectedSet) {
		if (equippedItems[selectedSet] == null) {
			equippedItems[selectedSet] = new LinkedList<EquippedItem>();

			List<Node> equippedElements = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_HELDENAUSRUESTUNG);

			for (int i = 0; i < equippedElements.size(); i++) {
				Element element = (Element) equippedElements.get(i);

				if (element.getAttribute(Xml.KEY_NAME).equals("jagtwaffe"))
					continue;

				int set = 0;
				if (element.hasAttribute(Xml.KEY_SET)) {
					set = Util.parseInt(element.getAttribute(Xml.KEY_SET));
					if (set != selectedSet)
						continue;
				}

				if (element.getAttribute(Xml.KEY_NAME).startsWith(PREFIX_BK)) {
					beidhaendigerKampfElements.add(element);
					continue;
				}

				EquippedItem item = new EquippedItem(this, element);
				equippedItems[selectedSet].add(item);
			}

			// handle bk elements
			for (Element element : beidhaendigerKampfElements) {

				int set = 0;
				if (element.hasAttribute(Xml.KEY_SET)) {
					set = Util.parseInt(element.getAttribute(Xml.KEY_SET));
					if (set != selectedSet)
						continue;
				}

				if (element.getAttribute(Xml.KEY_NAME).startsWith(PREFIX_BK)) {
					String bk = element.getAttribute(Xml.KEY_NAME);
					int bk1 = Util.parseInt(bk.substring(2, 3));
					int bk2 = Util.parseInt(bk.substring(3, 4));

					EquippedItem item1 = getEquippedItem(PREFIX_NKWAFFE + bk1);
					EquippedItem item2 = getEquippedItem(PREFIX_NKWAFFE + bk2);

					if (item2 != null && item1 != null) {
						item1.setSecondaryItem(item2);
						item2.setSecondaryItem(item1);
					} else {
						Debug.warning("Incorrect BeidhaengierKampf setting " + bk);
						getHeldElement().removeChild(element);
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

			String bk = element.getAttribute(Xml.KEY_NAME);

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

		Element bk = dom.createElement(Xml.KEY_HELDENAUSRUESTUNG);
		bk.setAttribute(Xml.KEY_SET, Util.toString(activeSet));

		if (item1.getNameId() < item2.getNameId())
			bk.setAttribute(Xml.KEY_NAME, PREFIX_BK + item1.getNameId() + item2.getNameId());
		else
			bk.setAttribute(Xml.KEY_NAME, PREFIX_BK + item2.getNameId() + item1.getNameId());

		beidhaendigerKampfElements.add(bk);
		getHeldElement().appendChild(bk);

	}

	public void removeBeidhaendigerKampf(EquippedItem item1, EquippedItem item2) {

		for (Element element : beidhaendigerKampfElements) {

			String bk = element.getAttribute(Xml.KEY_NAME);

			if (bk.equals(PREFIX_BK + item1.getNameId() + item2.getNameId())
					|| bk.equals(PREFIX_BK + item2.getNameId() + item1.getNameId())) {

				beidhaendigerKampfElements.remove(bk);
				getHeldElement().removeChild(element);
			}

		}

	}

	public int getActiveSet() {
		return activeSet;
	}

	public void setActiveSet(int activeSet) {
		// if (activeSet != this.activeSet)
		// equippedItems = null;
		this.activeSet = activeSet;
	}

	public void addModifierChangedListener(ModifierChangedListener listener) {
		modifierChangedListeners.add(listener);
	}

	public void removeModifierChangedListener(ModifierChangedListener listener) {
		modifierChangedListeners.remove(listener);
	}

	void fireModifiersChangedEvent(List<Modificator> modifiers) {

		this.modifiers = modifiers;

		for (ModifierChangedListener listener : modifierChangedListeners) {
			listener.onModifiersChanged(modifiers);
		}
	}

	public void fireValueChangedEvent(Value value) {
		for (ValueChangedListener l : listener)
			l.onValueChanged(value);
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
		List<Item> items = getItems().get(item.getType());
		items.remove(item);
		getHeldElement().removeChild(item.getElement());

		List<EquippedItem> toremove = new ArrayList<EquippedItem>();
		for (EquippedItem equippedItem : getEquippedItems()) {

			if (equippedItem.getItem() == null) {
				Debug.warning("Empty EquippedItem found during item delete:" + equippedItem.getName() + " - "
						+ equippedItem.getItemName());
				continue;
			}

			if (equippedItem.getItem().equals(item)) {
				toremove.add(equippedItem);
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

	public void addItem(final Context context, final Item item, CombatTalent talent) {
		addItem(context, item, talent, null);
	}

	public void addItem(final Context context, final Item item, CombatTalent talent, final ItemAddedCallback callback) {

		if (talent == null) {
			if (item instanceof Weapon) {
				Weapon weapon = (Weapon) item;

				final List<CombatTalent> combatTalents = getAvailableCombatTalents(weapon);

				if (combatTalents.size() == 1) {
					talent = combatTalents.get(0);
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
							addItem(context, item, combatTalents.get(which), callback);
						}
					});

					builder.show().setCanceledOnTouchOutside(true);
					return;
				}

			} else if (item instanceof DistanceWeapon) {
				DistanceWeapon weapon = (DistanceWeapon) item;
				if (weapon.getCombatTalentType() != null) {
					talent = getCombatTalent(weapon.getCombatTalentType().getName());
					if (talent == null) {
						Toast.makeText(context, "Kein verwendbares Talent gefunden", Toast.LENGTH_LONG).show();
						return;
					}
				}
			}
		}

		addItem(item);

		if (callback != null)
			callback.onItemAdded(item);

		if (item.getType().isEquipable()) {
			addEquippedItem(item, talent);
		}
	}

	public void addItem(Item item) {

		Element element = dom.createElement(Xml.KEY_GEGENSTAND);
		element.setAttribute(Xml.KEY_NAME, item.getName());
		element.setAttribute(Xml.KEY_ANZAHL, "1");
		element.setAttribute(Xml.KEY_SLOT, "0");

		item.setElement(element);
		List<Item> items = getItems().get(item.getType());
		if (items == null) {
			items = new LinkedList<Item>();
			getItems().put(item.getType(), items);
		}
		items.add(item);

		getHeldElement().appendChild(element);

	}

	public EquippedItem addEquippedItem(Item item, CombatTalent talent) {

		// if hero does not have item yet, add it first.
		Item heroItem = getItem(item.getName());
		if (heroItem == null) {
			addItem(item);
		}

		Element element = dom.createElement(Xml.KEY_HELDENAUSRUESTUNG);
		element.setAttribute(Xml.KEY_SET, Util.toString(activeSet));
		getHeldElement().appendChild(element);
		EquippedItem equippedItem = new EquippedItem(this, element, item);
		if (talent != null)
			equippedItem.setTalent(talent);
		getEquippedItems().add(equippedItem);

		return equippedItem;
	}

	public String getPath() {
		return path;
	}

	public Document getDocument() {
		return dom;
	}

	private Element getHeldElement() {
		if (held == null)
			held = (Element) dom.getElementsByTagName(Xml.KEY_HELD).item(0);

		return held;
	}

	public Attribute getAttribute(AttributeType type) {
		Attribute attribute = attributes.get(type);

		if (attribute == null) {

			List<Node> attributes = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_EIGENSCHAFT);
			for (int i = 0; i < attributes.size(); i++) {
				Element attributeElement = (Element) attributes.get(i);

				Attribute attr = new Attribute(attributeElement, this);
				this.attributes.put(attr.getType(), attr);
			}

			if (!this.attributes.containsKey(AttributeType.Behinderung)) {

				Element element = dom.createElement(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.Behinderung.name());
				element.setAttribute(Xml.KEY_VALUE, Integer.toString(getArmorBe()));
				getHeldElement().appendChild(element);
				this.attributes.put(AttributeType.Behinderung, new Attribute(element, this));
			}
			if (!this.attributes.containsKey(AttributeType.Ausweichen)) {

				Element element = dom.createElement(Xml.KEY_EIGENSCHAFT);
				element.setAttribute(Xml.KEY_NAME, AttributeType.Ausweichen.name());
				getHeldElement().appendChild(element);
				this.attributes.put(AttributeType.Ausweichen, new Attribute(element, this));
			}

			attribute = this.attributes.get(type);
		}

		return attribute;
	}

	public Map<Position, ArmorAttribute> getArmorAttributes() {
		if (armorAttributes == null) {
			armorAttributes = new HashMap<Position, ArmorAttribute>(Position.values().length);

			NodeList rsNodes = getHeldElement().getElementsByTagName(Xml.KEY_RUESTUNGSSCHUTZ);

			final List<Position> armorPositions = DSATabApplication.getInstance().getConfiguration()
					.getArmorPositions();
			for (int i = 0; i < rsNodes.getLength(); i++) {
				Element rsNode = null;
				try {
					rsNode = (Element) rsNodes.item(i);

					ArmorAttribute rs = new ArmorAttribute(rsNode, this);

					if (armorPositions.contains(rs.getPosition())) {
						armorAttributes.put(rs.getPosition(), rs);
					}
				} catch (Exception e) {
					getHeldElement().removeChild(rsNode);
					Debug.warn(e);
				}
			}

			// fill not existing values with 0
			for (Position pos : armorPositions) {

				ArmorAttribute rs = armorAttributes.get(pos);

				if (rs == null) {
					Element rsNode = dom.createElement(Xml.KEY_RUESTUNGSSCHUTZ);
					rsNode.setAttribute(Xml.KEY_NAME, pos.name());
					rsNode.setAttribute(Xml.KEY_VALUE, Integer.toString(getArmorRs(pos)));
					getHeldElement().appendChild(rsNode);
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

			NodeList rsNodes = getHeldElement().getElementsByTagName(Xml.KEY_WUNDE);

			for (int i = 0; i < rsNodes.getLength(); i++) {
				Element rsNode = null;
				try {
					rsNode = (Element) rsNodes.item(i);
					WoundAttribute rs = new WoundAttribute(this, rsNode);
					wounds.put(rs.getPosition(), rs);
				} catch (Exception e) {
					Debug.warn(e);
					getHeldElement().removeChild(rsNode);
				}
			}

			// fill not existing values with 0
			for (Position pos : DSATabApplication.getInstance().getConfiguration().getWoundPositions()) {

				WoundAttribute rs = wounds.get(pos);

				if (rs == null) {

					Element rsNode = dom.createElement(Xml.KEY_WUNDE);
					rsNode.setAttribute(Xml.KEY_NAME, pos.name());
					rsNode.setAttribute(Xml.KEY_VALUE, "0");
					getHeldElement().appendChild(rsNode);
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

		NodeList gewicht = getHeldElement().getElementsByTagName(Xml.KEY_GROESSE);

		if (gewicht.getLength() > 0) {
			return Util.parseInt(((Element) gewicht.item(0)).getAttribute(Xml.KEY_GEWICHT));
		} else
			return null;
	}

	public Integer getGroesse() {

		NodeList rasse = getHeldElement().getElementsByTagName(Xml.KEY_GROESSE);

		if (rasse.getLength() > 0) {
			return Util.parseInt(((Element) rasse.item(0)).getAttribute(Xml.KEY_VALUE));
		} else
			return null;
	}

	public Integer getAlter() {
		NodeList rasse = getHeldElement().getElementsByTagName(Xml.KEY_AUSSEHEN);

		if (rasse.getLength() > 0) {
			return Util.parseInt(((Element) rasse.item(0)).getAttribute(Xml.KEY_ALTER));
		} else
			return null;
	}

	public String getAugenFarbe() {
		NodeList rasse = getHeldElement().getElementsByTagName(Xml.KEY_AUSSEHEN);

		if (rasse.getLength() > 0) {
			return ((Element) rasse.item(0)).getAttribute(Xml.KEY_EYECOLOR);
		} else
			return null;
	}

	public String getHaarFarbe() {
		NodeList rasse = getHeldElement().getElementsByTagName(Xml.KEY_AUSSEHEN);

		if (rasse.getLength() > 0) {
			return ((Element) rasse.item(0)).getAttribute(Xml.KEY_HAIRCOLOR);
		} else
			return null;
	}

	public String getAusbildung() {

		NodeList ausbildung = getHeldElement().getElementsByTagName(Xml.KEY_AUSBILDUNG);
		if (ausbildung.getLength() > 0) {
			String value = ((Element) ausbildung.item(0)).getAttribute(Xml.KEY_STRING);
			if (!TextUtils.isEmpty(value))
				return value;
		}

		NodeList profession = getHeldElement().getElementsByTagName(Xml.KEY_PROFESSION);
		if (profession.getLength() > 0) {
			String value = ((Element) profession.item(0)).getAttribute(Xml.KEY_STRING);
			if (!TextUtils.isEmpty(value))
				return value;
		}

		return null;

	}

	public String getHerkunft() {
		NodeList rasse = getHeldElement().getElementsByTagName(Xml.KEY_RASSE);

		if (rasse.getLength() > 0) {
			return ((Element) rasse.item(0)).getAttribute(Xml.KEY_STRING);
		} else
			return null;
	}

	public String getName() {
		return getHeldElement().getAttribute(Xml.KEY_NAME);
	}

	public int getExperience() {
		if (experience == null)
			experience = (Element) getHeldElement().getElementsByTagName(Xml.KEY_ABENTEUERPUNKTE).item(0);

		return Integer.parseInt(experience.getAttribute(Xml.KEY_VALUE));
	}

	/**
	 * @return
	 */
	public double getLeRatio() {
		Attribute le = getAttribute(AttributeType.Lebensenergie);
		return ((double) le.getValue()) / le.getReferenceValue();
	}

	public List<Modifier> getModificator(Probe probe) {

		List<Modifier> modifiers = new LinkedList<Modifier>();
		for (Modificator modificator : getModifiers()) {
			Modifier mod = modificator.getModifier(probe);

			Debug.verbose("Modificator of " + mod.getTitle() + " is " + mod.getModifier());
			if (mod.getModifier() != 0) {
				modifiers.add(mod);
			}
		}

		if (probe instanceof CombatProbe) {
			CombatProbe combatProbe = (CombatProbe) probe;
			EquippedItem equippedItem = combatProbe.getEquippedItem();

			if (equippedItem != null && equippedItem.getItem() instanceof Weapon) {
				Weapon weapon = (Weapon) combatProbe.getEquippedItem().getItem();

				if (combatProbe.isAttack()) {
					Debug.verbose("Hauptwaffe Wm Attack is " + weapon.getWmAt());
					modifiers.add(new Modifier(weapon.getWmAt(), "Waffenmodifikator At", ""));
				} else {
					Debug.verbose("Hauptwaffe  Wm Defense is " + weapon.getWmPa());
					modifiers.add(new Modifier(weapon.getWmPa(), "Waffenmodifikator Pa", ""));
				}

				for (SpecialFeature special : getSpecialFeatures()) {
					if (special.getName().startsWith("Talentspezialisierung")
							&& special.getName().endsWith(weapon.getName() + ")")) {
						Debug.verbose("Talentspezialisierung " + weapon.getName());

						modifiers.add(new Modifier(1, special.getName(), ""));

						Debug.verbose(special.getName() + " AT/PA+1");
					}
				}

				// waffenlose kampftechniken +1/+1
				if (weapon.getName().startsWith("Raufen")) {
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

				if (weapon.getName().startsWith("Hruruzat")) {
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

				if (weapon.getName().startsWith("Ringen")) {
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

					if (equippedSecondaryWeapon != null && equippedSecondaryWeapon.getItem() instanceof Weapon) {
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
					if (equippedShield != null && equippedShield.getItem() instanceof Shield) {

						Shield shield = (Shield) equippedShield.getItem();

						modifiers.add(new Modifier(shield.getWmAt(), "Schildkampf Modifikator At", ""));

						Debug.verbose("Hauptwaffenattacke is reduziert um Schild WM " + shield.getWmAt());
					}
				}
			}
			if (equippedItem != null && equippedItem.getItem() instanceof Shield) {
				Shield shield = (Shield) combatProbe.getEquippedItem().getItem();

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
		for (Modificator m : getModifiers()) {
			modifier += m.getModifier(type).getModifier();
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
			NodeList specialFeaturesNodes = getHeldElement().getElementsByTagName(Xml.KEY_SONDERFERTIGKEIT);

			specialFeatures = new LinkedList<SpecialFeature>();

			for (int i = 0; i < specialFeaturesNodes.getLength(); i++) {
				Element feat = (Element) specialFeaturesNodes.item(i);

				specialFeatures.add(new SpecialFeature(feat));

			}
		}

		return specialFeatures;

	}

	public List<Advantage> getAdvantages() {
		if (advantages == null) {
			NodeList specialFeaturesNodes = getHeldElement().getElementsByTagName(Xml.KEY_VORTEIL);

			advantages = new LinkedList<Advantage>();

			for (int i = 0; i < specialFeaturesNodes.getLength(); i++) {
				Element feat = (Element) specialFeaturesNodes.item(i);

				advantages.add(new Advantage(feat));

			}
		}

		return advantages;
	}

	public List<Advantage> getDisadvantages() {
		if (disadvantages == null) {
			NodeList specialFeaturesNodes = getHeldElement().getElementsByTagName(Xml.KEY_NACHTEIL);

			disadvantages = new LinkedList<Advantage>();

			for (int i = 0; i < specialFeaturesNodes.getLength(); i++) {
				Element feat = (Element) specialFeaturesNodes.item(i);

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

		Element element = dom.createElement(Xml.KEY_EREIGNIS);
		getEventsElement().appendChild(element);
		Event event = new Event(element);
		event.setCategory(category);
		event.setComment(message);
		event.setAudioPath(audioPath);
		getEvents().add(event);
		return event;
	}

	public Element getEventsElement() {
		if (ereignisse == null) {

			NodeList eventsList = getHeldElement().getElementsByTagName(Xml.KEY_EREIGNISSE);

			if (eventsList.getLength() == 0) {
				ereignisse = dom.createElement(Xml.KEY_EREIGNISSE);
				getHeldElement().appendChild(ereignisse);
			} else {
				ereignisse = (Element) eventsList.item(0);
			}
		}
		return ereignisse;
	}

	public List<Event> getEvents() {
		if (events == null) {
			events = new LinkedList<Event>();

			NodeList eventElements = getEventsElement().getElementsByTagName(Xml.KEY_EREIGNIS);

			for (int i = 0; i < eventElements.getLength(); i++) {
				Element element = (Element) eventElements.item(i);

				if (element.hasAttribute(Xml.KEY_ABENTEUERPUNKTE_UPPER) || element.hasAttribute(Xml.KEY_OBJ))
					continue;

				if ("Sonstiges Ereignis (Hinzugewinn)".equals(element.getAttribute(Xml.KEY_TEXT))) {
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

		List<Item> result = null;
		if (types.length == 1) {
			result = getItems().get(types[0]);
		} else if (types.length > 1) {
			result = new LinkedList<Item>();
			for (ItemType t : types) {
				result.addAll(getItems(t));
			}
		}
		if (result == null)
			result = Collections.emptyList();

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
				Item item = equippedItem.getItem();
				if (item instanceof Armor) {
					Armor armor = (Armor) item;
					stars += armor.getStars();

					if (rs1Armor != null && rs1Armor.equals(armor.getName())) {
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
				Item item = equippedItem.getItem();
				if (item instanceof Armor) {
					Armor armor = (Armor) item;
					be += armor.getBe();

					if (rs1Armor != null && rs1Armor.equals(armor.getName())) {
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

		for (int i = 0; i < Position.ARMOR_POSITIONS.size(); i++) {
			totalRs += (getArmorRs(Position.ARMOR_POSITIONS.get(i)) * Position.ARMOR_POSITIONS_MULTIPLIER[i]);
		}

		totalRs = (int) Math.round(totalRs / 20.0);

		return totalRs;

	}

	public int getArmorRs(Position pos) {

		int rs = 0;
		for (EquippedItem equippedItem : getEquippedItems()) {
			Item item = equippedItem.getItem();
			if (item instanceof Armor) {
				Armor armor = (Armor) item;
				rs += armor.getRs(pos);
			}
		}
		return rs;
	}

	public Map<ItemType, List<Item>> getItems() {

		if (items == null) {

			items = new HashMap<ItemType, List<Item>>();

			List<Node> itemsElements = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_GEGENSTAND);

			for (int i = 0; i < itemsElements.size(); i++) {
				Element element = (Element) itemsElements.get(i);

				if (element.hasAttribute(Xml.KEY_NAME)) {

					Item item = DataManager.getItemByName(element.getAttribute(Xml.KEY_NAME));

					if (item != null) {

						item = (Item) item.duplicate();
						item.setElement(element);

						List<Item> its = items.get(item.getType());

						if (its == null) {
							its = new LinkedList<Item>();
							items.put(item.getType(), its);
						}
						its.add(item);

					} else {
						Debug.warning("Item not found skipping it:" + element.getAttribute(Xml.KEY_NAME));
					}
				}
			}

		}

		return items;
	}

	public static String getChildValue(Element node, String childTagName, String childParamName) {
		NodeList childList = node.getElementsByTagName(childTagName);

		if (childList.getLength() > 0) {
			Element child = (Element) childList.item(0);

			if (child.hasAttribute(childParamName)) {
				return child.getAttribute(childParamName);
			}
		}
		return null;
	}

	public Talent getTalent(String talentName) {
		if (talentByName == null) {
			getTalentGroups();
		}

		return talentByName.get(talentName);
	}

	public Map<TalentGroupType, TalentGroup> getTalentGroups() {
		if (talentGroups == null) {
			talentGroups = new HashMap<TalentGroupType, TalentGroup>();

			talentByName = new HashMap<String, Talent>();

			List<Node> talentList = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_TALENT);
			Talent talent;
			boolean found = false;
			for (int i = 0; i < talentList.size(); i++) {
				Element element = (Element) talentList.get(i);

				if (!element.hasAttribute(Xml.KEY_VALUE))
					continue;

				talent = new Talent(this, element);
				found = false;
				for (TalentGroupType type : TalentGroupType.values()) {
					if (type.contains(talent.getName())) {

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

		}
		return talentGroups;
	}

	public CombatStyle getCombatStyle() {
		if (getHeldElement().hasAttribute(Xml.KEY_COMBATSTYLE))
			return CombatStyle.valueOf(getHeldElement().getAttribute(Xml.KEY_COMBATSTYLE));
		else
			return CombatStyle.Offensive;
	}

	public void setCombatStyle(CombatStyle style) {
		if (style != null)
			getHeldElement().setAttribute(Xml.KEY_COMBATSTYLE, style.name());
		else
			getHeldElement().removeAttribute(Xml.KEY_COMBATSTYLE);
	}

	public List<CombatMeleeTalent> getCombatMeleeTalents() {
		if (combatTalents == null) {
			NodeList combatAttributesList = getHeldElement().getElementsByTagName(Xml.KEY_KAMPFWERTE);

			combatTalents = new ArrayList<CombatMeleeTalent>(combatAttributesList.getLength());

			List<CombatTalentType> missingTypes = new ArrayList<CombatTalentType>(Arrays.asList(CombatTalentType
					.values()));

			for (int i = 0; i < combatAttributesList.getLength(); i++) {
				Element element = (Element) combatAttributesList.item(i);
				CombatMeleeTalent talent = new CombatMeleeTalent(this, element);
				combatTalents.add(talent);

				missingTypes.remove(talent.getType());
			}

			// add missing combat talents with a value of 0.
			for (CombatTalentType talentType : missingTypes) {

				// skip fernkampf talent
				if (talentType.isFk())
					continue;

				String talentName = talentType.getName();

				Element element = dom.createElement(Xml.KEY_KAMPFWERTE);
				element.setAttribute(Xml.KEY_NAME, talentName);

				Element attacke = dom.createElement(Xml.KEY_ATTACKE);
				attacke.setAttribute(Xml.KEY_VALUE, "0");
				Element parade = dom.createElement(Xml.KEY_PARADE);
				parade.setAttribute(Xml.KEY_VALUE, "0");

				element.appendChild(attacke);
				element.appendChild(parade);
				getHeldElement().appendChild(element);

				CombatMeleeTalent talent = new CombatMeleeTalent(this, element);
				combatTalents.add(talent);
			}

		}

		return combatTalents;

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
			List<Node> spellList = DomUtil.getChildrenByTagName(getHeldElement(), Xml.KEY_ZAUBER);

			spells = new ArrayList<Spell>(spellList.size());

			for (int i = 0; i < spellList.size(); i++) {
				Element element = (Element) spellList.get(i);
				spells.add(new Spell(this, element));
			}
		}
		return spells;
	}

	public Item getItem(String name) {
		for (ItemType type : getItems().keySet()) {
			for (Item item : getItems(type)) {
				if (item.getName().equals(name)) {
					return item;
				}
			}
		}
		return null;
	}

	public Item getItem(UUID id) {
		for (ItemType type : getItems().keySet()) {
			for (Item item : getItems(type)) {
				if (item.getId().equals(id)) {
					return item;
				}
			}
		}
		return null;
	}

	public CombatTalent getCombatTalent(String name) {
		for (CombatTalent talent : getCombatMeleeTalents()) {
			if (talent.getName().equals(name)) {
				return talent;
			}
		}
		for (CombatTalent talent : getCombatDistanceTalents()) {
			if (talent.getName().equals(name)) {
				return talent;
			}
		}
		return null;
	}

	public List<CombatDistanceTalent> getCombatDistanceTalents() {

		if (combatDistanceTalents == null) {

			combatDistanceTalents = new ArrayList<CombatDistanceTalent>(10);
			for (Talent talent : getTalentGroups().get(TalentGroupType.Kampf).getTalents()) {

				CombatTalentType type = CombatTalentType.byName(talent.getName());
				if (!type.isFk())
					continue;

				combatDistanceTalents.add(new CombatDistanceTalent(this, talent.getElement()));
			}

		}

		return combatDistanceTalents;

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
		getEventsElement().removeChild(event.getElement());
	}

	public void removeEquippedItem(EquippedItem equippedItem) {

		if (equippedItem.getSecondaryItem() != null) {
			equippedItem.getSecondaryItem().setSecondaryItem(null);
		}

		getHeldElement().removeChild(equippedItem.getElement());
		getEquippedItems().remove(equippedItem);
	}

	public List<Modificator> getModifiers() {
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
	}
}
