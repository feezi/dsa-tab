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

import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.BodyFragment;
import com.dsatab.fragment.CharacterFragment;
import com.dsatab.fragment.FightFragment;
import com.dsatab.fragment.ItemsFragment;
import com.dsatab.fragment.LiturgieFragment;
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
public class TabConfiguration {
	private static final String FIELD_TABS_PORTRAIT = "tabsPortrait";
	private static final String FIELD_TABS_LANDSCAPE = "tabsLandscape";

	private List<TabInfo> tabInfosPortrait;
	private List<TabInfo> tabInfosLandscape;

	/**
	 * 
	 */
	public TabConfiguration() {
		tabInfosPortrait = new ArrayList<TabInfo>(10);
		tabInfosLandscape = new ArrayList<TabInfo>(10);
	}

	public TabConfiguration(JSONObject in) throws JSONException, ClassNotFoundException {
		tabInfosPortrait = new ArrayList<TabInfo>(10);
		tabInfosLandscape = new ArrayList<TabInfo>(10);

		JSONArray array = in.getJSONArray(FIELD_TABS_PORTRAIT);
		for (int i = 0; i < array.length(); i++) {
			JSONObject tab = array.getJSONObject(i);
			TabInfo info = new TabInfo(tab);
			tabInfosPortrait.add(info);
		}

		array = in.getJSONArray(FIELD_TABS_LANDSCAPE);
		for (int i = 0; i < array.length(); i++) {
			JSONObject tab = array.getJSONObject(i);
			TabInfo info = new TabInfo(tab);
			tabInfosLandscape.add(info);
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

	public void reset() {
		Debug.verbose("Restoring tabs");

		List<TabInfo> tabInfos = getTabs(Configuration.ORIENTATION_PORTRAIT);
		tabInfos.clear();
		tabInfos.add(new TabInfo(CharacterFragment.class, getTabResourceId(CharacterFragment.class)));
		tabInfos.add(new TabInfo(TalentFragment.class, getTabResourceId(TalentFragment.class)));
		tabInfos.add(new TabInfo(SpellFragment.class, getTabResourceId(SpellFragment.class)));
		tabInfos.add(new TabInfo(LiturgieFragment.class, getTabResourceId(LiturgieFragment.class)));
		tabInfos.add(new TabInfo(BodyFragment.class, getTabResourceId(BodyFragment.class)));
		tabInfos.add(new TabInfo(FightFragment.class, getTabResourceId(FightFragment.class)));
		tabInfos.add(new TabInfo(ItemsFragment.class, getTabResourceId(ItemsFragment.class), false));
		tabInfos.add(new TabInfo(NotesFragment.class, getTabResourceId(NotesFragment.class)));
		tabInfos.add(new TabInfo(PurseFragment.class, getTabResourceId(PurseFragment.class)));
		tabInfos.add(new TabInfo(MapFragment.class, getTabResourceId(MapFragment.class), false));

		if (isDualPanel()) {
			tabInfos = getTabs(Configuration.ORIENTATION_LANDSCAPE);
			tabInfos.clear();
			tabInfos.add(new TabInfo(CharacterFragment.class, TalentFragment.class,
					getTabResourceId(CharacterFragment.class)));
			tabInfos.add(new TabInfo(SpellFragment.class, LiturgieFragment.class, getTabResourceId(SpellFragment.class)));
			tabInfos.add(new TabInfo(FightFragment.class, BodyFragment.class, getTabResourceId(FightFragment.class)));
			tabInfos.add(new TabInfo(ItemsFragment.class, getTabResourceId(ItemsFragment.class), false));
			tabInfos.add(new TabInfo(NotesFragment.class, PurseFragment.class, getTabResourceId(NotesFragment.class),
					false));
			tabInfos.add(new TabInfo(MapFragment.class, getTabResourceId(MapFragment.class), false));
		} else {
			tabInfos = getTabs(Configuration.ORIENTATION_LANDSCAPE);
			tabInfos.clear();
			tabInfos.add(new TabInfo(CharacterFragment.class, getTabResourceId(CharacterFragment.class)));
			tabInfos.add(new TabInfo(TalentFragment.class, getTabResourceId(TalentFragment.class)));
			tabInfos.add(new TabInfo(SpellFragment.class, getTabResourceId(SpellFragment.class)));
			tabInfos.add(new TabInfo(LiturgieFragment.class, getTabResourceId(LiturgieFragment.class)));
			tabInfos.add(new TabInfo(BodyFragment.class, getTabResourceId(BodyFragment.class)));
			tabInfos.add(new TabInfo(FightFragment.class, getTabResourceId(FightFragment.class)));
			tabInfos.add(new TabInfo(ItemsFragment.class, getTabResourceId(ItemsFragment.class), false));
			tabInfos.add(new TabInfo(NotesFragment.class, getTabResourceId(NotesFragment.class)));
			tabInfos.add(new TabInfo(PurseFragment.class, getTabResourceId(PurseFragment.class)));
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
		for (int i = 0; i < tabInfosLandscape.size(); i++) {
			array.put(i, tabInfosLandscape.get(i).toJSONObject());
		}
		out.put(FIELD_TABS_LANDSCAPE, array);

		array = new JSONArray();
		for (int i = 0; i < tabInfosPortrait.size(); i++) {
			array.put(i, tabInfosPortrait.get(i).toJSONObject());
		}
		out.put(FIELD_TABS_PORTRAIT, array);

		return out;
	}
}
