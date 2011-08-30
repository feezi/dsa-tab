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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dsatab.activity.DsaPreferenceActivity;
import com.dsatab.data.enums.Position;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.BodyFragment;
import com.dsatab.fragment.CharacterFragment;
import com.dsatab.fragment.FightFragment;
import com.dsatab.fragment.ItemChooserFragment;
import com.dsatab.fragment.ItemsFragment;
import com.dsatab.fragment.ItemsListFragment;
import com.dsatab.fragment.LiturgieFragment;
import com.dsatab.fragment.MapFragment;
import com.dsatab.fragment.NotesEditFragment;
import com.dsatab.fragment.NotesFragment;
import com.dsatab.fragment.PurseFragment;
import com.dsatab.fragment.SpellFragment;
import com.dsatab.fragment.TalentFragment;

/**
 *  
 */
public class DsaTabConfiguration {

	private Map<Class<? extends BaseFragment>, Integer> tabResourceIds = new HashMap<Class<? extends BaseFragment>, Integer>(
			15);

	private Context context;

	private List<Integer> tabIcons;

	public enum ArmorType {
		ZonenRuestung("Zonenrüstung"), GesamtRuestung("Gesamte Rüstung");

		private String title;

		private ArmorType(String t) {
			title = t;
		}

		public String title() {
			return title;
		}
	}

	public DsaTabConfiguration(Context context) {
		this.context = context;

		tabResourceIds.put(CharacterFragment.class, R.drawable.tab_character);
		tabResourceIds.put(TalentFragment.class, R.drawable.tab_talents);
		tabResourceIds.put(SpellFragment.class, R.drawable.tab_magic);
		tabResourceIds.put(LiturgieFragment.class, R.drawable.tab_liturige);
		tabResourceIds.put(BodyFragment.class, R.drawable.tab_wound);
		tabResourceIds.put(FightFragment.class, R.drawable.tab_fight);
		tabResourceIds.put(ItemsFragment.class, R.drawable.tab_items);
		tabResourceIds.put(ItemsListFragment.class, R.drawable.tab_items);
		tabResourceIds.put(ItemChooserFragment.class, R.drawable.tab_items);
		tabResourceIds.put(NotesFragment.class, R.drawable.tab_notes);
		tabResourceIds.put(NotesEditFragment.class, R.drawable.tab_notes);
		tabResourceIds.put(PurseFragment.class, R.drawable.tab_coins);
		tabResourceIds.put(MapFragment.class, R.drawable.tab_map);

		// DO NOT CHANGE ORDER OF ICONS BECAUSE IT IS stored in tabconfiguration
		// as tabResourceIndex, ALWAYS ADD AT THE LASt POSITION!!!
		tabIcons = Arrays.asList(R.drawable.tab_character, R.drawable.tab_coins, R.drawable.tab_fight,
				R.drawable.tab_items, R.drawable.tab_magic, R.drawable.tab_liturige, R.drawable.tab_map,
				R.drawable.tab_notes, R.drawable.tab_talents, R.drawable.tab_wound);
	}

	public List<Integer> getTabIcons() {
		return tabIcons;
	}

	public int getTabResourceId(Class<? extends BaseFragment> clazz) {
		if (tabResourceIds.containsKey(clazz)) {
			return tabResourceIds.get(clazz);
		} else
			return 0;
	}

	/**
	 * 
	 */
	public TabConfiguration getDefaultTabConfiguration() {

		TabConfiguration tabConfiguration = new TabConfiguration();
		tabConfiguration.reset();

		return tabConfiguration;
	}

	public boolean isHouseRules() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(DsaPreferenceActivity.KEY_HOUSE_RULES, false);
	}

	public List<Position> getArmorPositions() {
		if (isHouseRules()) {
			return Position.ARMOR_POSITIONS_HOUSE;
		} else
			return Position.ARMOR_POSITIONS;
	}

	public List<Position> getWoundPositions() {
		if (isHouseRules()) {
			return Position.WOUND_POSITIONS;
		} else
			return Position.WOUND_POSITIONS;
	}

	public ArmorType getArmorType() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return ArmorType.valueOf(preferences.getString(DsaPreferenceActivity.KEY_ARMOR_TYPE,
				ArmorType.ZonenRuestung.name()));
	}

}
