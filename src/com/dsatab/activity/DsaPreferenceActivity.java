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

import java.io.File;
import java.io.FileFilter;
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
import com.dsatab.view.VersionInfoDialog;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.Downloader;

public class DsaPreferenceActivity extends PreferenceActivity {

	public static final String INTENT_PREF_SCREEN = "com.dsatab.prefScreen";

	public static final int SCREEN_HOME = 0;
	public static final int SCREEN_EXCHANGE = 1;

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

	public static final String KEY_INFOS = "infos";

	public static final String KEY_FULL_VERSION = "fullVersion";

	public static final String KEY_NEWS_VERSION = "newsversion";

	public static final String KEY_EXCHANGE = "heldenAustauschScreen";

	public static final String KEY_EXCHANGE_PROVIDER = "exchange_provider";

	public static final String KEY_EXCHANGE_USERNAME = "exchange_username";
	public static final String KEY_EXCHANGE_PASSWORD = "exchange_password";

	public static final String DEFAULT_EXCHANGE_PROVIDER = "http://helden.draschenfels.de/";

	public static final String PATH_MAPS = "http://dl.dropbox.com/u/15750588/dsatab-maps.zip";
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

		int screen = getIntent().getIntExtra(INTENT_PREF_SCREEN, SCREEN_HOME);

		switch (screen) {
		case SCREEN_HOME:
			break;
		case SCREEN_EXCHANGE:
			PreferenceScreen preference = (PreferenceScreen) findPreference(KEY_EXCHANGE);
			setPreferenceScreen(preference);
			break;
		}
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
			cleanOldFiles();
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(getString(R.string.path_items));
			// downloader.addPath(Downloader.PATH_MAPS);
			// downloader.addPath(Downloader.PATH_RANG_PORTRAITS);
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_MAPS)) {
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_MAPS);
			downloader.downloadZip();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_ITEMS)) {
			cleanOldFiles();
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(getString(R.string.path_items));
			downloader.downloadZip();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_RANG_PORTRAITS)) {
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_RANG_PORTRAITS);
			downloader.downloadZip();
		} else if (preference.getKey().equals(KEY_DOWNLOAD_WESNOTH_PORTRAITS)) {
			downloader = new Downloader(DSATabApplication.getDsaTabPath(), this);
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
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
		} else if (preference.getKey().equals(KEY_INFOS)) {
			VersionInfoDialog newsDialog = new VersionInfoDialog(this);
			newsDialog.setSeenVersion(-1);
			newsDialog.show();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	private void cleanOldFiles() {
		File cardsDir = new File(DSATabApplication.getDsaTabPath(), "cards");

		File[] dirs = cardsDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		for (File f : dirs) {
			f.delete();
			Debug.verbose("Deleting " + f.getAbsolutePath());
		}
	}
}