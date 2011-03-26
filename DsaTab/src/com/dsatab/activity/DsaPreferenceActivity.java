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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.webkit.WebView;

import com.dsatab.R;
import com.dsatab.activity.DsaTabConfiguration.ArmorType;
import com.gandulf.guilib.util.Downloader;

public class DsaPreferenceActivity extends PreferenceActivity {

	public static final String KEY_PROBE_PROBABILITY = "probeProbability";

	public static final String KEY_NOTES_VISIBILITY = "showNotes";

	public static final String KEY_FIGHT_ARMOR_VISIBILITY = "showArmor";

	public static final String KEY_PROBE_SHAKE_ROLL_DICE = "shakeRollDice";

	public static final String KEY_HOUSE_RULES = "houseRules";

	public static final String KEY_ARMOR_TYPE = "armorType";

	public static final String KEY_SETUP_SDCARD_PATH = "sdcardPath";

	public static final String KEY_DOWNLOAD_ALL = "downloadAll";
	public static final String KEY_DOWNLOAD_RANG_PORTRAITS = "downloadRangPortraits";
	public static final String KEY_DOWNLOAD_WESNOTH_PORTRAITS = "downloadWesnothPortraits";
	public static final String KEY_DOWNLOAD_MAPS = "downloadMaps";
	public static final String KEY_DOWNLOAD_ITEMS = "downloadItems";

	public static final String KEY_CREDITS = "credits";

	public static final String PATH_MAPS = "http://dl.dropbox.com/u/15750588/dsatab-maps.zip";
	public static final String PATH_ITEMS = "http://dl.dropbox.com/u/15750588/dsatab-items.zip";
	public static final String PATH_RANG_PORTRAITS = "http://dl.dropbox.com/u/15750588/dsatab-rang-portraits.zip";
	public static final String PATH_WESNOTH_PORTRAITS = "http://dl.dropbox.com/u/15750588/dsatab-wesnoth-portraits.zip";

	private Downloader downloader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @seeandroid.preference.PreferenceActivity#onPreferenceTreeClick(android.
	 * preference.PreferenceScreen, android.preference.Preference)
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference.getKey().equals(KEY_DOWNLOAD_ALL)) {

			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_ITEMS);
			// downloader.addPath(Downloader.PATH_MAPS);
			// downloader.addPath(Downloader.PATH_RANG_PORTRAITS);
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.download();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_MAPS)) {
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_MAPS);
			downloader.download();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_ITEMS)) {
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_ITEMS);
			downloader.download();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_RANG_PORTRAITS)) {
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_RANG_PORTRAITS);
			downloader.download();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_WESNOTH_PORTRAITS)) {
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.download();
		} else if (preference.getKey().equals(KEY_CREDITS)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_credits);
			builder.setCancelable(true);
			WebView webView = new WebView(this);

			String summary = getResources().getString(R.string.credits);
			webView.loadData(summary, "text/html", "utf-8");
			builder.setView(webView);
			builder.setNeutralButton(R.string.label_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}