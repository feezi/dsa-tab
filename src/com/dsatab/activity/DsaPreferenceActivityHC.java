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

import java.util.List;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.view.MenuItem;
import com.dsatab.DSATabApplication;
import com.dsatab.R;

@TargetApi(11)
public class DsaPreferenceActivityHC extends BasePreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		int screen = getIntent().getIntExtra(INTENT_PREF_SCREEN, SCREEN_HOME);

		switch (screen) {
		case SCREEN_HOME:
			break;
		case SCREEN_EXCHANGE:
			switchToHeader("com.dsatab.activity.DsaPreferenceActivityHC$PrefsSetupFragment", null);
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_OK);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BasePreferenceActivity#onSharedPreferenceChanged(
	 * android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		handlePreferenceChange(findPreference(key), sharedPreferences, key);
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences_headers, target);
	}

	public static class BasePreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onStart()
		 */
		@Override
		public void onStart() {
			super.onStart();

			initPreferences(getPreferenceManager(), getPreferenceScreen());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			SharedPreferences preferences = DSATabApplication.getPreferences();
			preferences.registerOnSharedPreferenceChangeListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onDestroy()
		 */
		@Override
		public void onDestroy() {
			super.onDestroy();
			SharedPreferences preferences = DSATabApplication.getPreferences();
			preferences.unregisterOnSharedPreferenceChangeListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.SharedPreferences.OnSharedPreferenceChangeListener
		 * #onSharedPreferenceChanged(android.content.SharedPreferences,
		 * java.lang.String)
		 */
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			handlePreferenceChange(findPreference(key), sharedPreferences, key);
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
			if (KEY_DOWNLOAD_SCREEN.equals(preference.getKey())) {
				((PreferenceActivity) getActivity()).startPreferenceFragment(new PrefsDownloadFragment(), true);
				return true;
			} else if (KEY_DISPALY_HEADER_SCREEN.equals(preference.getKey())) {
				((PreferenceActivity) getActivity()).startPreferenceFragment(new PrefsDisplayHeaderFragment(), true);
				return true;
			} else if (KEY_DISPALY_DICE_SLIDER_SCREEN.equals(preference.getKey())) {
				((PreferenceActivity) getActivity())
						.startPreferenceFragment(new PrefsDisplayDiceSliderFragment(), true);
				return true;
			} else if (KEY_HOUSE_RULES.equals(preference.getKey())) {
				((PreferenceActivity) getActivity()).startPreferenceFragment(new PrefsHouseRulesFragment(), true);
				return true;
			} else {
				return handlePreferenceTreeClick(getActivity(), preferenceScreen, preference);
			}
		}
	}

	public static class PrefsSetupFragment extends BasePreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_hc_setup);
		}

	}

	public static class PrefsDisplayFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_hc_display);
		}
	}

	public static class PrefsDisplayHeaderFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_hc_display_header);
		}

	}

	public static class PrefsDisplayDiceSliderFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_hc_display_diceslider);
		}
	}

	public static class PrefsDownloadFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_hc_download);
		}

	}

	public static class PrefsHouseRulesFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_hc_houserules);
		}

	}

	public static class PrefsRulesFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_hc_rules);
		}
	}

	public static class PrefsInfoFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences_info);
		}
	}

}