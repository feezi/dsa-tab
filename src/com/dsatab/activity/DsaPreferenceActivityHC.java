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
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.dsatab.DsaTabConfiguration;
import com.dsatab.DsaTabConfiguration.ArmorType;
import com.dsatab.R;

public class DsaPreferenceActivityHC extends BasePreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int screen = getIntent().getIntExtra(INTENT_PREF_SCREEN, SCREEN_HOME);

		switch (screen) {
		case SCREEN_HOME:
			break;
		case SCREEN_EXCHANGE:
			switchToHeader("com.dsatab.activity.DsaPreferenceActivityHC$PrefsSetupFragment", null);
			break;
		}

	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences_headers, target);
	}

	public static class PrefsSetupFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_setup);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.preference.PreferenceFragment#onPreferenceTreeClick(android
		 * .preference.PreferenceScreen, android.preference.Preference)
		 */
		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			return handlePreferenceTreeClick(getActivity(), preferenceScreen, preference);
		}
	}

	public static class PrefsDisplayFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_display);

		}
	}

	public static class PrefsRulesFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_rules);

			ListPreference listPreference = (ListPreference) findPreference(KEY_ARMOR_TYPE);

			List<String> themeNames = new LinkedList<String>();
			List<String> themeValues = new LinkedList<String>();

			for (ArmorType themeValue : DsaTabConfiguration.ArmorType.values()) {
				themeNames.add(themeValue.title());
				themeValues.add(themeValue.name());
			}

			listPreference.setEntries(themeNames.toArray(new String[0]));
			listPreference.setEntryValues(themeValues.toArray(new String[0]));

		}
	}

	public static class PrefsInfoFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_info);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.preference.PreferenceFragment#onPreferenceTreeClick(android
		 * .preference.PreferenceScreen, android.preference.Preference)
		 */
		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			return handlePreferenceTreeClick(getActivity(), preferenceScreen, preference);
		}
	}

}