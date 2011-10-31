/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.activity;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.dsatab.DsaTabConfiguration;
import com.dsatab.DsaTabConfiguration.ArmorType;
import com.dsatab.R;

public class DsaPreferenceActivity extends BasePreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences_rules);
		addPreferencesFromResource(R.xml.preferences_display);
		addPreferencesFromResource(R.xml.preferences_setup);
		addPreferencesFromResource(R.xml.preferences_info);

		ListPreference listPreference = (ListPreference) findPreference(KEY_ARMOR_TYPE);
		List<String> armorNames = new LinkedList<String>();
		List<String> armorValues = new LinkedList<String>();

		for (ArmorType themeValue : DsaTabConfiguration.ArmorType.values()) {
			armorNames.add(themeValue.title());
			armorValues.add(themeValue.name());
		}

		listPreference.setEntries(armorNames.toArray(new String[0]));
		listPreference.setEntryValues(armorValues.toArray(new String[0]));

		int screen = getIntent().getIntExtra(INTENT_PREF_SCREEN, SCREEN_HOME);

		switch (screen) {
		case SCREEN_HOME:
			break;
		case SCREEN_EXCHANGE:
			PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("setup");
			setPreferenceScreen(preferenceScreen);
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onPreferenceTreeClick(android.
	 * preference.PreferenceScreen, android.preference.Preference)
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		return handlePreferenceTreeClick(this, preferenceScreen, preference);
	}

}