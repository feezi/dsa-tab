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
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.dsatab.DSATabApplication;
import com.dsatab.DsaTabConfiguration;
import com.dsatab.DsaTabConfiguration.ArmorType;
import com.dsatab.DsaTabConfiguration.WoundType;
import com.dsatab.R;
import com.dsatab.util.Debug;
import com.dsatab.view.PreferenceWithButton;
import com.dsatab.view.TipOfTheDayDialog;
import com.gandulf.guilib.util.AbstractDownloader;
import com.gandulf.guilib.util.DownloaderWrapper;
import com.gandulf.guilib.util.ResUtil;
import com.gandulf.guilib.view.VersionInfoDialog;

public abstract class BasePreferenceActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

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
	public static final String KEY_HOUSE_RULES_MORE_TARGET_ZONES = "houseRules.moreTargetZones";

	public static final String KEY_ARMOR_TYPE = "armorType";

	public static final String KEY_WOUND_TYPE = "woundType";

	public static final String KEY_FULLSCREEN = "fullscreen";

	public static final String KEY_SETUP_SDCARD_PATH = "sdcardPath";
	public static final String KEY_SETUP_SDCARD_HERO_PATH = "sdcardHeroPath";

	public static final String KEY_DOWNLOAD_SCREEN = "downloadMediaScreen";
	public static final String KEY_DOWNLOAD_ALL = "downloadAll";
	public static final String KEY_DOWNLOAD_MAPS = "downloadMaps";
	public static final String KEY_DOWNLOAD_BACKGROUNDS = "downloadBackgrounds";
	public static final String KEY_DOWNLOAD_OSMMAPS = "downloadOSMMaps";

	public static final String KEY_DISPALY_HEADER_SCREEN = "displayHeaderScreen";
	public static final String KEY_DISPALY_DICE_SLIDER_SCREEN = "displayDiceSliderScreen";

	public static final String KEY_DOWNLOAD_WESNOTH_PORTRAITS = "downloadWesnothPortraits";

	public static final String KEY_DOWNLOAD_ITEMS = "downloadItems";

	public static final String KEY_CREDITS = "credits";

	public static final String KEY_INFOS = "infos";
	public static final String KEY_DONATE = "donate";
	public static final String KEY_THEME = "theme";

	public static final String KEY_STYLE_BG_PATH = "theme.bg.path";
	public static final String KEY_STYLE_BG_DELETE = "theme.bg.delete";
	public static final int ACTION_PICK_BG_PATH = 1001;
	public static final int ACTION_PICK_BG_WOUNDS_PATH = 1002;

	public static final String KEY_STYLE_BG_WOUNDS_PATH = "theme.wound.bg.path";
	public static final String KEY_STYLE_BG_WOUNDS_DELETE = "theme.wound.bg.delete";

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

	public static final String PATH_BACKGROUNDS = "http://dsa-tab.googlecode.com/files/dsatab-backgrounds.zip";

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

	public static void initPreferences(PreferenceManager mgr, PreferenceScreen screen) {

		OnClickListener buttonClickListener = new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {

				Preference preference = (Preference) v.getTag();

				if (preference != null) {
					if (preference.getKey().equals(KEY_STYLE_BG_PATH)) {
						handlePreferenceClick(v.getContext(), BasePreferenceActivity.KEY_STYLE_BG_DELETE);
					} else if (preference.getKey().equals(KEY_STYLE_BG_WOUNDS_PATH)) {
						handlePreferenceClick(v.getContext(), BasePreferenceActivity.KEY_STYLE_BG_WOUNDS_DELETE);
					}
				}
			}
		};

		PreferenceWithButton pref = (PreferenceWithButton) mgr.findPreference(KEY_STYLE_BG_PATH);
		if (pref != null) {
			pref.setButtonClickListener(buttonClickListener);
		}

		pref = (PreferenceWithButton) mgr.findPreference(KEY_STYLE_BG_WOUNDS_PATH);
		if (pref != null) {
			pref.setButtonClickListener(buttonClickListener);
		}

		ListPreference listPreference = (ListPreference) mgr.findPreference(KEY_ARMOR_TYPE);
		if (listPreference != null) {
			List<String> themeNames = new LinkedList<String>();
			List<String> themeValues = new LinkedList<String>();

			for (ArmorType themeValue : DsaTabConfiguration.ArmorType.values()) {
				themeNames.add(themeValue.title());
				themeValues.add(themeValue.name());
			}

			listPreference.setEntries(themeNames.toArray(new String[0]));
			listPreference.setEntryValues(themeValues.toArray(new String[0]));
		}

		listPreference = (ListPreference) mgr.findPreference(KEY_WOUND_TYPE);
		if (listPreference != null) {
			List<String> armorNames = new LinkedList<String>();
			List<String> armorValues = new LinkedList<String>();

			for (WoundType themeValue : DsaTabConfiguration.WoundType.values()) {
				armorNames.add(themeValue.title());
				armorValues.add(themeValue.name());
			}

			listPreference.setEntries(armorNames.toArray(new String[0]));
			listPreference.setEntryValues(armorValues.toArray(new String[0]));
		}

		Preference shakeDice = mgr.findPreference(KEY_PROBE_SHAKE_ROLL_DICE);
		if (shakeDice != null) {
			shakeDice.setEnabled(DSATabApplication.getInstance().getPackageManager()
					.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER));
			if (!shakeDice.isEnabled()) {
				shakeDice.setSummary("Dein Smartphone verfügt nicht über den benötigten Sensor.");
			}
		}

		SharedPreferences sharedPreferences = mgr.getSharedPreferences();

		initPreferenceScreen(screen, sharedPreferences);

	}

	private static void initPreferenceScreen(PreferenceGroup screen, SharedPreferences sharedPreferences) {

		final int count = screen.getPreferenceCount();

		for (int i = 0; i < count; i++) {
			Preference preference = screen.getPreference(i);

			if (preference instanceof PreferenceGroup) {
				initPreferenceScreen((PreferenceGroup) preference, sharedPreferences);
			} else {
				handlePreferenceChange(preference, sharedPreferences, preference.getKey());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setTheme(DSATabApplication.getInstance().getCustomPreferencesTheme());
		super.onCreate(savedInstanceState);

		SharedPreferences preferences = DSATabApplication.getPreferences();
		preferences.registerOnSharedPreferenceChangeListener(this);

		updateFullscreenStatus(getWindow(), preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		SharedPreferences preferences = DSATabApplication.getPreferences();
		preferences.unregisterOnSharedPreferenceChangeListener(this);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_PICK_BG_PATH) {
				handleImagePick(KEY_STYLE_BG_PATH, data);
			} else if (requestCode == ACTION_PICK_BG_WOUNDS_PATH) {
				handleImagePick(KEY_STYLE_BG_WOUNDS_PATH, data);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void handleImagePick(String prefKey, Intent data) {

		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_ID };

		Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		int bucketIndex = cursor.getColumnIndex(filePathColumn[1]);
		String filePath = cursor.getString(columnIndex);
		String bucketId = cursor.getString(bucketIndex);

		cursor.close();
		File file = new File(filePath);
		if (file.exists()) {
			SharedPreferences preferences = DSATabApplication.getPreferences();
			Editor edit = preferences.edit();
			edit.putString(prefKey, filePath);
			edit.commit();

			Toast.makeText(this, "Hintergrundbild wurde verändert.", Toast.LENGTH_SHORT);
		}

	}

	protected static void pickImage(Activity activity, int action) {

		Uri targetUri = Media.EXTERNAL_CONTENT_URI;
		String folderPath = DSATabApplication.getDirectory(DSATabApplication.DIR_PORTRAITS).getAbsolutePath();
		String folderBucketId = Integer.toString(folderPath.toLowerCase().hashCode());

		targetUri = targetUri.buildUpon().appendQueryParameter(MediaStore.Images.Media.BUCKET_ID, folderBucketId)
				.build();

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setData(targetUri);

		activity.startActivityForResult(Intent.createChooser(photoPickerIntent, "Bild auswählen"), action);
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

	protected static boolean handlePreferenceClick(Context context, String key) {
		if (KEY_STYLE_BG_WOUNDS_DELETE.equals(key)) {
			SharedPreferences preferences = DSATabApplication.getPreferences();
			Editor edit = preferences.edit();
			edit.remove(KEY_STYLE_BG_WOUNDS_PATH);
			edit.commit();

			Toast.makeText(context, "Wunden-Hintergrundbild wurde zurückgesetzt.", Toast.LENGTH_SHORT).show();
			return true;
		} else if (KEY_STYLE_BG_DELETE.equals(key)) {
			SharedPreferences preferences = DSATabApplication.getPreferences();
			Editor edit = preferences.edit();
			edit.remove(KEY_STYLE_BG_PATH);
			edit.commit();

			Toast.makeText(context, "Hintergrundbild wurde zurückgesetzt.", Toast.LENGTH_SHORT).show();
			return true;
		}

		return false;

	}

	protected static void handlePreferenceChange(Preference preference, SharedPreferences sharedPreferences, String key) {

		if (preference != null) {
			if (KEY_THEME.equals(key)) {
				preference.setSummary("Aktuelles Theme: " + DSATabApplication.getInstance().getCustomThemeName());
			} else if (KEY_STYLE_BG_PATH.equals(key)) {
				((PreferenceWithButton) preference)
						.setWidgetVisibility(sharedPreferences.contains(KEY_STYLE_BG_PATH) ? View.VISIBLE : View.GONE);
			} else if (KEY_STYLE_BG_WOUNDS_PATH.equals(key)) {
				((PreferenceWithButton) preference).setWidgetVisibility(sharedPreferences
						.contains(KEY_STYLE_BG_WOUNDS_PATH) ? View.VISIBLE : View.GONE);
			}
		}
	}

	protected static boolean handlePreferenceTreeClick(Activity context, PreferenceScreen screen, Preference preference) {

		AbstractDownloader downloader;
		if (KEY_DOWNLOAD_ALL.equals(preference.getKey())) {
			cleanOldFiles();
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(context.getString(R.string.path_items));
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_MAPS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath() + DSATabApplication.DIR_MAPS,
					context);
			downloader.addPath(PATH_OFFICIAL_MAP_PACK);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_ITEMS.equals(preference.getKey())) {
			cleanOldFiles();
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(context.getString(R.string.path_items));
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_WESNOTH_PORTRAITS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_BACKGROUNDS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_BACKGROUNDS);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_OSMMAPS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DSATabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_OSM_MAP_PACK);
			downloader.downloadZip();
			return true;
		} else if (KEY_CREDITS.equals(preference.getKey())) {
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
		} else if (KEY_INFOS.equals(preference.getKey())) {
			VersionInfoDialog newsDialog = new VersionInfoDialog(context);
			newsDialog.setDonateContentId(R.raw.donate);
			newsDialog.setDonateVersion(DSATabApplication.getInstance().isLiteVersion());
			newsDialog.setDonateUrl(DSATabApplication.PAYPAL_DONATION_URL);
			newsDialog.setRawClass(R.raw.class);
			newsDialog.setTitle(R.string.news_title);
			newsDialog.setIcon(R.drawable.icon);
			newsDialog.show(true);
			return true;
		} else if (KEY_TIP_TODAY.equals(preference.getKey())) {
			TipOfTheDayDialog newsDialog = new TipOfTheDayDialog(context);
			newsDialog.show();
			return true;
		} else if (KEY_DONATE.equals(preference.getKey())) {
			Uri uriUrl = Uri.parse(DSATabApplication.PAYPAL_DONATION_URL);
			final Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			context.startActivity(launchBrowser);
			return true;
		} else if (KEY_DSA_LICENSE.equals(preference.getKey())) {
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
		} else if (KEY_STYLE_BG_PATH.equals(preference.getKey())) {
			pickImage(context, ACTION_PICK_BG_PATH);
			return true;
		} else if (KEY_STYLE_BG_WOUNDS_PATH.equals(preference.getKey())) {
			pickImage(context, ACTION_PICK_BG_WOUNDS_PATH);
			return true;
		} else if (KEY_STYLE_BG_WOUNDS_DELETE.equals(preference.getKey())) {
			return handlePreferenceClick(context, KEY_STYLE_BG_WOUNDS_DELETE);
		} else if (KEY_STYLE_BG_DELETE.equals(preference.getKey())) {
			return handlePreferenceClick(context, KEY_STYLE_BG_DELETE);
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