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
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.view.TipOfTheDayDialog;
import com.gandulf.guilib.util.AbstractDownloader;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.DownloaderWrapper;
import com.gandulf.guilib.util.ResUtil;
import com.gandulf.guilib.view.VersionInfoDialog;

public abstract class BasePreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String INTENT_PREF_SCREEN = "com.dsatab.prefScreen";

	public static final int SCREEN_HOME = 0;
	public static final int SCREEN_EXCHANGE = 1;

	public static final String KEY_PROBE_PROBABILITY = "probeProbability";

	public static final String KEY_NOTES_VISIBILITY = "showNotes";

	public static final String KEY_PROBE_SHAKE_ROLL_DICE = "shakeRollDice";

	public static final String KEY_PROBE_ANIM_ROLL_DICE = "animRollDice";

	public static final String KEY_PROBE_SOUND_ROLL_DICE = "soundRollDice";

	public static final String KEY_PROBE_AUTO_ROLL_DICE = "autoRollDice";

	public static final String KEY_PROBE_SOUND_RESULT_DICE = "soundResultDice";

	public static final String KEY_PROBE_SHOW_MODIFIKATORS = "probeShowModificators";

	public static final String KEY_HOUSE_RULES = "houseRules";
	public static final String KEY_HOUSE_RULES_2_OF_3_DICE = "houseRules.2of3Dice";
	public static final String KEY_HOUSE_RULES_LE_MODIFIER = "houseRules.leModifier";
	public static final String KEY_HOUSE_RULES_AU_MODIFIER = "houseRules.auModifier";
	public static final String KEY_HOUSE_RULES_EASIER_WOUNDS = "houseRules.easierWounds";
	public static final String KEY_HOUSE_RULES_MORE_WOUND_ZONES = "houseRules.moreWoundZones";

	public static final String KEY_ARMOR_TYPE = "armorType";

	public static final String KEY_WOUND_TYPE = "woundType";

	public static final String KEY_FULLSCREEN = "fullscreen";

	public static final String KEY_SETUP_SDCARD_PATH = "sdcardPath";

	public static final String KEY_DOWNLOAD_SCREEN = "downloadMediaScreen";
	public static final String KEY_DOWNLOAD_ALL = "downloadAll";
	public static final String KEY_DOWNLOAD_MAPS = "downloadMaps";
	public static final String KEY_DOWNLOAD_OSMMAPS = "downloadOSMMaps";

	public static final String KEY_DOWNLOAD_WESNOTH_PORTRAITS = "downloadWesnothPortraits";

	public static final String KEY_DOWNLOAD_ITEMS = "downloadItems";

	public static final String KEY_CREDITS = "credits";

	public static final String KEY_INFOS = "infos";
	public static final String KEY_DONATE = "donate";
	public static final String KEY_THEME = "theme";

	public static final String KEY_FULL_VERSION = "fullVersion";

	public static final String KEY_EXCHANGE = "heldenAustauschScreen";

	public static final String KEY_EXCHANGE_PROVIDER = "exchange_provider";

	public static final String KEY_EXCHANGE_USERNAME = "exchange_username";
	public static final String KEY_EXCHANGE_PASSWORD = "exchange_password";

	public static final String KEY_USAGE_STATS = "usage_stats";

	public static final String KEY_SCREEN_ORIENTATION = "screen_orientation";

	public static final String KEY_TIP_TODAY = "tipToday";

	public static final String KEY_DSA_LICENSE = "dsa_license";

	public static final String KEY_HEADER_NAME = "header_name";
	public static final String KEY_HEADER_LE = "header_le";
	public static final String KEY_HEADER_AU = "header_au";
	public static final String KEY_HEADER_KE = "header_ke";
	public static final String KEY_HEADER_AE = "header_ae";
	public static final String KEY_HEADER_BE = "header_be";
	public static final String KEY_HEADER_MR = "header_mr";
	public static final String KEY_HEADER_GS = "header_gs";
	public static final String KEY_HEADER_WS = "header_ws";

	public static final String DEFAULT_EXCHANGE_PROVIDER = "http://helden.draschenfels.de/";

	public static final String SCREEN_ORIENTATION_AUTO = "auto";
	public static final String SCREEN_ORIENTATION_LANDSCAPE = "landscape";
	public static final String SCREEN_ORIENTATION_PORTRAIT = "portrait";

	public static final String DEFAULT_SCREEN_ORIENTATION = SCREEN_ORIENTATION_AUTO;
	// http://dl.dropbox.com/u/15750588/dsatab-wesnoth-portraits.zip
	public static final String PATH_WESNOTH_PORTRAITS = "http://dsa-tab.googlecode.com/files/dsatab-wesnoth-portraits.zip";

	public static final String PATH_OFFICIAL_MAP_PACK = "http://dsa-tab.googlecode.com/files/Das%20Schwarze%20Auge%20Kartenpaketv1.zip";

	public static final String PATH_OSM_MAP_PACK = "http://dsa-tab.googlecode.com/files/dsatab-osmmap-v1.zip";

	private static final String[] RESTART_KEYS = { KEY_THEME };
	static {
		Arrays.sort(RESTART_KEYS);
	}

	private boolean restartRequired = false;

	public static void startPreferenceActivity(Activity context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			context.startActivityForResult(new Intent(context, DsaPreferenceActivity.class),
					MainActivity.ACTION_PREFERENCES);
		} else {
			context.startActivityForResult(new Intent(context, DsaPreferenceActivityHC.class),
					MainActivity.ACTION_PREFERENCES);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences preferences = DSATabApplication.getPreferences();
		preferences.registerOnSharedPreferenceChangeListener(this);

		updateFullscreenStatus(getWindow(), preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onStop() {
		if (restartRequired) {
			Toast.makeText(this, "Veränderte Einstellungen erfordern einen Neustart um übernommen zu werden.",
					Toast.LENGTH_LONG).show();
		}
		super.onStop();
	}

	protected static void cleanOldFiles() {
		File cardsDir = DSATabApplication.getDirectory(DSATabApplication.DIR_CARDS);

		File[] dirs = cardsDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		if (dirs != null) {
			for (File f : dirs) {

				File[] children = f.listFiles();
				if (children != null) {
					for (File child : children) {
						child.delete();
					}
				}

				f.delete();
				Debug.verbose("Deleting " + f.getAbsolutePath());
			}
		}
	}

	protected static void updateFullscreenStatus(Window window, boolean bUseFullscreen) {
		if (bUseFullscreen) {
			window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		window.getDecorView().requestLayout();
	}

	protected static boolean handlePreferenceTreeClick(Context context, PreferenceScreen screen, Preference preference) {

		AbstractDownloader downloader;
		if (preference.getKey().equals(KEY_DOWNLOAD_ALL)) {
			cleanOldFiles();
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(context.getString(R.string.path_items));
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
			return true;
		} else if (preference.getKey().equals(KEY_DOWNLOAD_MAPS)) {
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath() + DSATabApplication.DIR_MAPS,
					context);
			downloader.addPath(PATH_OFFICIAL_MAP_PACK);
			downloader.downloadZip();
			return true;
		} else if (preference.getKey().equals(KEY_DOWNLOAD_ITEMS)) {
			cleanOldFiles();
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(context.getString(R.string.path_items));
			downloader.downloadZip();
			return true;
		} else if (preference.getKey().equals(KEY_DOWNLOAD_WESNOTH_PORTRAITS)) {
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
			return true;
		} else if (preference.getKey().equals(KEY_DOWNLOAD_OSMMAPS)) {
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_OSM_MAP_PACK);
			downloader.downloadZip();
			return true;
		} else if (preference.getKey().equals(KEY_CREDITS)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.title_credits);
			builder.setCancelable(true);
			WebView webView = new WebView(context);

			String summary = ResUtil.loadResToString(R.raw.credits, context);
			webView.loadData(summary, "text/html", "ISO-8859-1");
			builder.setView(webView);
			builder.setNeutralButton(R.string.label_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		} else if (preference.getKey().equals(KEY_INFOS)) {
			VersionInfoDialog newsDialog = new VersionInfoDialog(context);
			newsDialog.setDonateContentId(R.raw.donate);
			newsDialog.setDonateVersion(DSATabApplication.getInstance().isLiteVersion());
			newsDialog.setDonateUrl(DSATabApplication.PAYPAL_DONATION_URL);
			newsDialog.setRawClass(R.raw.class);
			newsDialog.setTitle(R.string.news_title);
			newsDialog.setIcon(R.drawable.icon);
			newsDialog.show(true);
			return true;
		} else if (preference.getKey().equals(KEY_TIP_TODAY)) {
			TipOfTheDayDialog newsDialog = new TipOfTheDayDialog(context);
			newsDialog.show();
			return true;
		} else if (preference.getKey().equals(KEY_DONATE)) {
			Uri uriUrl = Uri.parse(DSATabApplication.PAYPAL_DONATION_URL);
			final Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			context.startActivity(launchBrowser);
			return true;
		} else if (preference.getKey().equals(KEY_DSA_LICENSE)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.title_credits);
			builder.setCancelable(true);
			WebView webView = new WebView(context);

			String summary = ResUtil.loadResToString(R.raw.ulisses_license, context);
			webView.loadData(summary, "text/html", "ISO-8859-1");
			builder.setView(webView);
			builder.setNeutralButton(R.string.label_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#
	 * onSharedPreferenceChanged(android.content.SharedPreferences,
	 * java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_FULLSCREEN)) {
			updateFullscreenStatus(getWindow(), sharedPreferences.getBoolean(KEY_FULLSCREEN, true));
		}

		if (!restartRequired && Arrays.binarySearch(RESTART_KEYS, key) >= 0)
			restartRequired = true;
	}
}