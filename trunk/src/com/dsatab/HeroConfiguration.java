/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import com.dsatab.data.ArmorAttribute;
import com.dsatab.data.CustomAttribute;
import com.dsatab.data.CustomModificator;
import com.dsatab.data.Event;
import com.dsatab.data.Hero;
import com.dsatab.data.Hero.CombatStyle;
import com.dsatab.data.JSONable;
import com.dsatab.data.MetaTalent;
import com.dsatab.data.WoundAttribute;
import com.dsatab.fragment.ArtFragment;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.BodyFragment;
import com.dsatab.fragment.CharacterFragment;
import com.dsatab.fragment.DocumentsFragment;
import com.dsatab.fragment.FightFragment;
import com.dsatab.fragment.ItemsFragment;
import com.dsatab.fragment.MapFragment;
import com.dsatab.fragment.NotesFragment;
import com.dsatab.fragment.PurseFragment;
import com.dsatab.fragment.SpellFragment;
import com.dsatab.fragment.TalentFragment;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class HeroConfiguration {

	private static final String FIELD_TABS_PORTRAIT = "tabsPortrait";
	private static final String FIELD_TABS_LANDSCAPE = "tabsLandscape";
	private static final String FIELD_MODIFICATORS = "modificators";
	private static final String FIELD_WOUNDS = "wounds";
	private static final String FIELD_ARMOR_ATTRIBUTES = "armors";
	private static final String FIELD_ATTRIBUTES = "attributes";
	private static final String FIELD_COMBAT_STYLE = "combatStyle";
	private static final String FIELD_BE_CALCULATION = "beCalculation";
	private static final String FIELD_META_TALENTS = "metaTalents";
	private static final String FIELD_EVENTS = "events";

	private List<TabInfo> tabInfosPortrait;
	private List<TabInfo> tabInfosLandscape;

	private List<CustomModificator> modificators;
	private List<WoundAttribute> wounds;
	private List<ArmorAttribute>[] armorAttributes;
	private List<CustomAttribute> attributes;
	private List<MetaTalent> metaTalents;
	private List<Event> events;

	private CombatStyle combatStyle;
	private boolean beCalculation;

	private Hero hero;

	/**
	 * 
	 */
	public HeroConfiguration(Hero hero) {
		this.hero = hero;
		tabInfosPortrait = new ArrayList<TabInfo>(10);
		tabInfosLandscape = new ArrayList<TabInfo>(10);

		modificators = new ArrayList<CustomModificator>();
		wounds = new ArrayList<WoundAttribute>();
		armorAttributes = new ArrayList[Hero.MAXIMUM_SET_NUMBER];
		attributes = new ArrayList<CustomAttribute>();
		metaTalents = new ArrayList<MetaTalent>();
		events = new ArrayList<Event>();

		combatStyle = CombatStyle.Offensive;
		beCalculation = true;
	}

	public HeroConfiguration(Hero hero, JSONObject in) throws JSONException, ClassNotFoundException {
		this.hero = hero;
		JSONArray array = null;
		if (in.has(FIELD_TABS_PORTRAIT)) {
			array = in.getJSONArray(FIELD_TABS_PORTRAIT);
			tabInfosPortrait = new ArrayList<TabInfo>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject tab = array.getJSONObject(i);
				TabInfo info = new TabInfo(tab);
				tabInfosPortrait.add(info);
			}
		} else {
			tabInfosPortrait = new ArrayList<TabInfo>();
		}

		if (in.has(FIELD_TABS_LANDSCAPE)) {
			array = in.getJSONArray(FIELD_TABS_LANDSCAPE);
			tabInfosLandscape = new ArrayList<TabInfo>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject tab = array.getJSONObject(i);
				TabInfo info = new TabInfo(tab);
				tabInfosLandscape.add(info);
			}
		} else {
			tabInfosLandscape = new ArrayList<TabInfo>();
		}

		if (in.has(FIELD_MODIFICATORS)) {
			array = in.getJSONArray(FIELD_MODIFICATORS);
			modificators = new ArrayList<CustomModificator>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject tab = array.getJSONObject(i);
				CustomModificator info = new CustomModificator(this.hero, tab);
				modificators.add(info);
			}
		} else {
			modificators = new ArrayList<CustomModificator>();
		}
		if (in.has(FIELD_WOUNDS)) {
			array = in.getJSONArray(FIELD_WOUNDS);
			wounds = new ArrayList<WoundAttribute>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject tab = array.getJSONObject(i);
				try {
					WoundAttribute info = new WoundAttribute(this.hero, tab);
					wounds.add(info);
				} catch (Exception e) {
					Debug.warning("Unknown WoundAttribute, skipping it: " + tab);
				}
			}
		} else {
			wounds = new ArrayList<WoundAttribute>();
		}

		if (in.has(FIELD_ARMOR_ATTRIBUTES)) {
			array = in.getJSONArray(FIELD_ARMOR_ATTRIBUTES);
			armorAttributes = new ArrayList[array.length()];
			for (int s = 0; s < array.length(); s++) {
				JSONArray inArray = array.getJSONArray(s);

				armorAttributes[s] = new ArrayList<ArmorAttribute>(inArray.length());

				for (int i = 0; i < inArray.length(); i++) {
					JSONObject tab = inArray.getJSONObject(i);
					try {
						ArmorAttribute info = new ArmorAttribute(this.hero, tab);
						armorAttributes[s].add(info);
					} catch (Exception e) {
						Debug.warning("Unknown ArmorAttribute, skipping it: " + tab);
					}
				}
			}
		} else {
			armorAttributes = new ArrayList[Hero.MAXIMUM_SET_NUMBER];
		}

		if (in.has(FIELD_ATTRIBUTES)) {
			array = in.getJSONArray(FIELD_ATTRIBUTES);
			attributes = new ArrayList<CustomAttribute>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject tab = array.getJSONObject(i);
				try {
					CustomAttribute info = new CustomAttribute(this.hero, tab);
					attributes.add(info);
				} catch (Exception e) {
					Debug.warning("Unknown Attribute, skipping it: " + tab);
				}
			}
		} else {
			attributes = new ArrayList<CustomAttribute>();
		}

		if (in.has(FIELD_META_TALENTS)) {
			array = in.getJSONArray(FIELD_META_TALENTS);
			metaTalents = new ArrayList<MetaTalent>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject tab = array.getJSONObject(i);
				MetaTalent info = new MetaTalent(this.hero, tab);
				metaTalents.add(info);
			}
		} else {
			metaTalents = new ArrayList<MetaTalent>();
		}

		if (in.has(FIELD_EVENTS)) {
			array = in.getJSONArray(FIELD_EVENTS);
			events = new ArrayList<Event>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject tab = array.getJSONObject(i);
				Event info = new Event(tab);
				events.add(info);
			}
		} else {
			events = new ArrayList<Event>();
		}

		if (in.has(FIELD_COMBAT_STYLE)) {
			combatStyle = CombatStyle.valueOf(in.getString(FIELD_COMBAT_STYLE));
		} else
			combatStyle = CombatStyle.Offensive;
		if (in.has(FIELD_BE_CALCULATION)) {
			beCalculation = in.getBoolean(FIELD_BE_CALCULATION);
		}

	}

	/**
	 * 
	 * @param orientation
	 *            one of {@link ActivityInfo} SCREEN_ORIENTATION_
	 * @return
	 */
	public List<TabInfo> getTabs(int orientation) {
		switch (orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			return tabInfosLandscape;
		default:
			return tabInfosPortrait;
		}
	}

	public List<TabInfo> getTabs() {
		Configuration configuration = DSATabApplication.getInstance().getResources().getConfiguration();
		return getTabs(configuration.orientation);
	}

	public TabInfo getTab(int index) {
		return getTabs().get(index);
	}

	private boolean isDualPanel() {
		Configuration configuration = DSATabApplication.getInstance().getResources().getConfiguration();
		int size = Configuration.SCREENLAYOUT_SIZE_MASK & configuration.screenLayout;

		return size == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private int getTabResourceId(Class<? extends BaseFragment> clazz) {
		return DSATabApplication.getInstance().getConfiguration().getTabResourceId(clazz);
	}

	public void addModificator(CustomModificator modificator) {
		modificators.add(modificator);
	}

	public void removeModificator(CustomModificator modificator) {
		modificators.remove(modificator);
	}

	public void addMetaTalent(MetaTalent talent) {
		metaTalents.add(talent);
	}

	public void removeMetaTalent(MetaTalent talent) {
		metaTalents.remove(talent);
	}

	public List<MetaTalent> getMetaTalents() {
		return metaTalents;
	}

	public List<CustomModificator> getModificators() {
		return modificators;
	}

	public List<WoundAttribute> getWounds() {
		return wounds;
	}

	public void addWound(WoundAttribute modificator) {
		wounds.add(modificator);
	}

	public void removeWound(WoundAttribute modificator) {
		wounds.remove(modificator);
	}

	public CombatStyle getCombatStyle() {
		return combatStyle;
	}

	public void setCombatStyle(CombatStyle combatStyle) {
		this.combatStyle = combatStyle;
	}

	public List<ArmorAttribute> getArmorAttributes(int index) {
		return armorAttributes[index];
	}

	public void addArmorAttribute(int index, ArmorAttribute modificator) {
		if (armorAttributes[index] == null)
			armorAttributes[index] = new ArrayList<ArmorAttribute>();

		armorAttributes[index].add(modificator);
	}

	public void removeArmorAttribute(int index, ArmorAttribute modificator) {
		if (armorAttributes[index] != null)
			armorAttributes[index].remove(modificator);
	}

	public List<Event> getEvents() {
		return events;
	}

	public void addEvent(Event event) {
		events.add(event);
	}

	public void removeEvent(Event event) {
		events.remove(event);
	}

	public List<CustomAttribute> getAttributes() {
		return attributes;
	}

	public void addAttribute(CustomAttribute modificator) {
		attributes.add(modificator);
	}

	public void removeAttribute(CustomAttribute modificator) {
		attributes.remove(modificator);
	}

	public boolean isBeCalculation() {
		return beCalculation;
	}

	public void setBeCalculation(boolean beCalculation) {
		this.beCalculation = beCalculation;
	}

	public void reset() {
		Debug.verbose("Restoring tabs");

		List<TabInfo> tabInfos = getTabs(Configuration.ORIENTATION_PORTRAIT);
		tabInfos.clear();
		tabInfos.add(new TabInfo(CharacterFragment.class, getTabResourceId(CharacterFragment.class)));
		tabInfos.add(new TabInfo(TalentFragment.class, getTabResourceId(TalentFragment.class)));
		tabInfos.add(new TabInfo(SpellFragment.class, getTabResourceId(SpellFragment.class)));
		tabInfos.add(new TabInfo(ArtFragment.class, getTabResourceId(ArtFragment.class)));
		tabInfos.add(new TabInfo(BodyFragment.class, getTabResourceId(BodyFragment.class)));
		tabInfos.add(new TabInfo(FightFragment.class, getTabResourceId(FightFragment.class)));
		tabInfos.add(new TabInfo(ItemsFragment.class, getTabResourceId(ItemsFragment.class), false));
		tabInfos.add(new TabInfo(NotesFragment.class, getTabResourceId(NotesFragment.class)));
		tabInfos.add(new TabInfo(PurseFragment.class, getTabResourceId(PurseFragment.class)));
		tabInfos.add(new TabInfo(DocumentsFragment.class, getTabResourceId(DocumentsFragment.class), false));
		tabInfos.add(new TabInfo(MapFragment.class, getTabResourceId(MapFragment.class), false));

		if (isDualPanel()) {
			tabInfos = getTabs(Configuration.ORIENTATION_LANDSCAPE);
			tabInfos.clear();
			tabInfos.add(new TabInfo(CharacterFragment.class, TalentFragment.class,
					getTabResourceId(CharacterFragment.class)));
			tabInfos.add(new TabInfo(SpellFragment.class, ArtFragment.class, getTabResourceId(SpellFragment.class)));
			tabInfos.add(new TabInfo(FightFragment.class, BodyFragment.class, getTabResourceId(FightFragment.class)));
			tabInfos.add(new TabInfo(ItemsFragment.class, getTabResourceId(ItemsFragment.class), false));
			tabInfos.add(new TabInfo(NotesFragment.class, PurseFragment.class, getTabResourceId(NotesFragment.class),
					false));
			tabInfos.add(new TabInfo(DocumentsFragment.class, getTabResourceId(DocumentsFragment.class), false));
			tabInfos.add(new TabInfo(MapFragment.class, getTabResourceId(MapFragment.class), false));
		} else {
			tabInfos = getTabs(Configuration.ORIENTATION_LANDSCAPE);
			tabInfos.clear();
			tabInfos.add(new TabInfo(CharacterFragment.class, getTabResourceId(CharacterFragment.class)));
			tabInfos.add(new TabInfo(TalentFragment.class, getTabResourceId(TalentFragment.class)));
			tabInfos.add(new TabInfo(SpellFragment.class, getTabResourceId(SpellFragment.class)));
			tabInfos.add(new TabInfo(ArtFragment.class, getTabResourceId(ArtFragment.class)));
			tabInfos.add(new TabInfo(BodyFragment.class, getTabResourceId(BodyFragment.class)));
			tabInfos.add(new TabInfo(FightFragment.class, getTabResourceId(FightFragment.class)));
			tabInfos.add(new TabInfo(ItemsFragment.class, getTabResourceId(ItemsFragment.class), false));
			tabInfos.add(new TabInfo(NotesFragment.class, getTabResourceId(NotesFragment.class)));
			tabInfos.add(new TabInfo(PurseFragment.class, getTabResourceId(PurseFragment.class)));
			tabInfos.add(new TabInfo(DocumentsFragment.class, getTabResourceId(DocumentsFragment.class), false));
			tabInfos.add(new TabInfo(MapFragment.class, getTabResourceId(MapFragment.class), false));
		}
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject out = new JSONObject();

		JSONArray array = new JSONArray();
		for (int s = 0; s < armorAttributes.length; s++) {
			JSONArray inArray = new JSONArray();
			if (armorAttributes[s] != null) {
				for (int i = 0; i < armorAttributes[s].size(); i++) {
					inArray.put(i, armorAttributes[s].get(i).toJSONObject());
				}
			}
			array.put(s, inArray);
		}
		out.put(FIELD_ARMOR_ATTRIBUTES, array);

		putArray(out, tabInfosLandscape, FIELD_TABS_LANDSCAPE);
		putArray(out, tabInfosPortrait, FIELD_TABS_PORTRAIT);
		putArray(out, modificators, FIELD_MODIFICATORS);
		putArray(out, wounds, FIELD_WOUNDS);
		putArray(out, attributes, FIELD_ATTRIBUTES);
		putArray(out, metaTalents, FIELD_META_TALENTS);
		putArray(out, events, FIELD_EVENTS);

		out.put(FIELD_COMBAT_STYLE, combatStyle.name());
		out.put(FIELD_BE_CALCULATION, beCalculation);
		return out;
	}

	private void putArray(JSONObject out, List<? extends JSONable> list, String name) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		int index = 0;
		final int count = list.size();
		for (int i = 0; i < count; i++) {
			JSONObject jsonObject = list.get(i).toJSONObject();
			if (jsonObject != null) {
				jsonArray.put(index++, jsonObject);
			}
		}
		out.put(name, jsonArray);

	}
}
