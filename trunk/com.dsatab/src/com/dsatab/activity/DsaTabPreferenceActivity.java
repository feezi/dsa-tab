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
import java.util.Locale;

import net.saik0.android.unifiedpreference.UnifiedPreferenceFragment;
import net.saik0.android.unifiedpreference.UnifiedSherlockPreferenceActivity;
import android.annotation.TargetApi;
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
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.dsatab.DsaTabApplication;
import com.dsatab.DsaTabConfiguration;
import com.dsatab.DsaTabConfiguration.ArmorType;
import com.dsatab.DsaTabConfiguration.WoundType;
import com.dsatab.R;
import com.dsatab.util.Debug;
import com.dsatab.util.Hint;
import com.dsatab.view.ChangeLogDialog;
import com.dsatab.view.DirectoryChooserDialogHelper;
import com.dsatab.view.DirectoryChooserDialogHelper.Result;
import com.dsatab.view.PreferenceWithButton;
import com.gandulf.guilib.download.AbstractDownloader;
import com.gandulf.guilib.download.DownloaderWrapper;
import com.gandulf.guilib.util.ResUtil;

public class DsaTabPreferenceActivity extends UnifiedSherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	/**
	 * 
	 */
	public static final String INTENT_DATA_LAYOUT = "layout";
	public static final String INTENT_DATA_FRAGMENT = "fragment";

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

	public static final String KEY_EXCHANGE = "heldenAustauschScreen";

	public static final String KEY_EXCHANGE_PROVIDER = "exchange_provider";

	public static final String KEY_EXCHANGE_USERNAME = "exchange_username";
	public static final String KEY_EXCHANGE_PASSWORD = "exchange_password";
	public static final String KEY_EXCHANGE_TOKEN = "exchange_token";

	public static final String KEY_USAGE_STATS = "usage_stats";

	public static final String KEY_SCREEN_ORIENTATION = "screen_orientation";

	public static final String KEY_TIP_TODAY = "tipToday";
	public static final String KEY_TIP_TODAY_RESET = "tipTodayReset";

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

	public static final String KEY_MODIFY_TABS = "modifyTabs";

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
		context.startActivityForResult(new Intent(context, DsaTabPreferenceActivity.class),
				DsaTabActivity.ACTION_PREFERENCES);

	}

	public static void initPreferences(final PreferenceManager mgr, final PreferenceScreen screen) {

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
						handlePreferenceClick(v.getContext(), DsaTabPreferenceActivity.KEY_STYLE_BG_DELETE,
								mgr.getSharedPreferences());
					} else if (preference.getKey().equals(KEY_STYLE_BG_WOUNDS_PATH)) {
						handlePreferenceClick(v.getContext(), DsaTabPreferenceActivity.KEY_STYLE_BG_WOUNDS_DELETE,
								mgr.getSharedPreferences());
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
			shakeDice.setEnabled(DsaTabApplication.getInstance().getPackageManager()
					.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER));
			if (!shakeDice.isEnabled()) {
				shakeDice.setSummary("Dein Smartphone verfügt nicht über den benötigten Sensor.");
			}
		}

		SharedPreferences sharedPreferences = mgr.getSharedPreferences();
		if (screen != null) {
			initPreferenceScreen(screen, sharedPreferences);
		}

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

	protected void legacyPreOnCreate(Bundle savedInstanceState) {
		if (!hasIntentLegacyLayoutData()) {
			setHeaderRes(com.dsatab.R.xml.preferences_headers);
		}
	}

	protected void preOnCreate(Bundle savedInstanceState) {
		setHeaderRes(com.dsatab.R.xml.preferences_headers);
	}

	protected boolean hasIntentLegacyLayoutData() {
		return getIntent() != null && getIntent().getAction() != null && getIntent().hasExtra(INTENT_DATA_LAYOUT);
	}

	protected boolean hasIntentLayoutData() {
		return getIntent() != null && getIntent().getAction() != null && getIntent().hasExtra(INTENT_DATA_FRAGMENT);
	}

	protected void legacyPostOnCreate(Bundle savedInstanceState) {
		if (hasIntentLegacyLayoutData()) {
			int layoutId = getIntent().getIntExtra(INTENT_DATA_LAYOUT, 0);
			if (layoutId == 0) {
				String layout = getIntent().getStringExtra(INTENT_DATA_LAYOUT);
				if (layout.startsWith("@xml/"))
					layout = layout.substring(5);
				if (layout.startsWith("res/xml/"))
					layout = layout.substring(8, layout.length() - 4);

				layoutId = getResources().getIdentifier(layout, "xml", getPackageName());
			}

			if (layoutId != 0) {
				addPreferencesFromResource(layoutId);
			} else {
				throw new IllegalArgumentException("Unable to find layout with name/id:"
						+ getIntent().getStringExtra(INTENT_DATA_LAYOUT));
			}
		}
	}

	protected void postOnCreate(Bundle savedInstanceState) {
		if (hasIntentLayoutData()) {
			String fragment = getIntent().getStringExtra(INTENT_DATA_FRAGMENT);
			if (!TextUtils.isEmpty(fragment)) {
				switchToHeader(fragment, null);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(DsaTabApplication.getInstance().getCustomPreferencesTheme());

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			legacyPreOnCreate(savedInstanceState);
		else
			preOnCreate(savedInstanceState);

		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		SharedPreferences preferences = DsaTabApplication.getPreferences();
		preferences.registerOnSharedPreferenceChangeListener(this);

		updateFullscreenStatus(getWindow(), preferences.getBoolean(DsaTabPreferenceActivity.KEY_FULLSCREEN, true));

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			legacyPostOnCreate(savedInstanceState);
		else
			postOnCreate(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.saik0.android.unifiedpreference.UnifiedSherlockPreferenceActivity
	 * #onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (isSinglePane()) {
			initPreferences(getPreferenceManager(), getPreferenceScreen());
			onBindPreferenceSummariesToValues();
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

		if (KEY_FULLSCREEN.equals(key)) {
			updateFullscreenStatus(getWindow(), sharedPreferences.getBoolean(KEY_FULLSCREEN, true));
		}

		if (!restartRequired && Arrays.binarySearch(RESTART_KEYS, key) >= 0)
			restartRequired = true;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		SharedPreferences preferences = DsaTabApplication.getPreferences();
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
		// int bucketIndex = cursor.getColumnIndex(filePathColumn[1]);
		String filePath = cursor.getString(columnIndex);
		// String bucketId = cursor.getString(bucketIndex);

		cursor.close();
		File file = new File(filePath);
		if (file.exists()) {
			SharedPreferences preferences = DsaTabApplication.getPreferences();
			Editor edit = preferences.edit();
			edit.putString(prefKey, filePath);
			edit.commit();

			Toast.makeText(this, "Hintergrundbild wurde verändert.", Toast.LENGTH_SHORT).show();
		}

	}

	protected static void pickImage(Activity activity, int action) {

		Uri targetUri = Media.EXTERNAL_CONTENT_URI;
		String folderPath = DsaTabApplication.getDirectory(DsaTabApplication.DIR_PORTRAITS).getAbsolutePath();
		String folderBucketId = Integer.toString(folderPath.toLowerCase(Locale.GERMAN).hashCode());

		targetUri = targetUri.buildUpon().appendQueryParameter(MediaStore.Images.Media.BUCKET_ID, folderBucketId)
				.build();

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setData(targetUri);

		activity.startActivityForResult(Intent.createChooser(photoPickerIntent, "Bild auswählen"), action);
	}

	protected static void cleanOldFiles() {
		File cardsDir = DsaTabApplication.getDirectory(DsaTabApplication.DIR_CARDS);

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

	protected static boolean handlePreferenceClick(final Context context, final String key,
			final SharedPreferences preferences) {
		if (KEY_STYLE_BG_WOUNDS_DELETE.equals(key)) {
			Editor edit = preferences.edit();
			edit.remove(KEY_STYLE_BG_WOUNDS_PATH);
			edit.commit();

			Toast.makeText(context, "Wunden-Hintergrundbild wurde zurückgesetzt.", Toast.LENGTH_SHORT).show();
			return true;
		} else if (KEY_STYLE_BG_DELETE.equals(key)) {
			Editor edit = preferences.edit();
			edit.remove(KEY_STYLE_BG_PATH);
			edit.commit();

			Toast.makeText(context, "Hintergrundbild wurde zurückgesetzt.", Toast.LENGTH_SHORT).show();
			return true;
		} else if (KEY_SETUP_SDCARD_PATH.equals(key)) {
			Result resultListener = new Result() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.dsatab.view.DirectoryChooserDialogHelper.Result#
				 * onChooseDirectory(java.lang.String)
				 */
				@Override
				public void onChooseDirectory(String dir) {

					File directory = new File(dir);
					if (directory.exists() && directory.canWrite()) {
						Editor edit = preferences.edit();
						edit.putString(KEY_SETUP_SDCARD_PATH, dir);
						edit.commit();
					} else {
						Toast.makeText(context,
								"DsaTab hat in diesem Verzeichnis keine Schreibrechte. Wähle bitte ein anderes aus.",
								Toast.LENGTH_LONG).show();
					}
				}
			};
			new DirectoryChooserDialogHelper(context, resultListener, DsaTabApplication.getDsaTabHeroPath());
			return true;
		} else if (KEY_SETUP_SDCARD_HERO_PATH.equals(key)) {
			Result resultListener = new Result() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.dsatab.view.DirectoryChooserDialogHelper.Result#
				 * onChooseDirectory(java.lang.String)
				 */
				@Override
				public void onChooseDirectory(String dir) {
					File directory = new File(dir);
					if (directory.exists() && directory.canWrite()) {
						Editor edit = preferences.edit();
						edit.putString(KEY_SETUP_SDCARD_HERO_PATH, dir);
						edit.commit();
					} else {
						Toast.makeText(context,
								"DsaTab hat in diesem Verzeichnis keine Schreibrechte. Wähle bitte ein anderes aus.",
								Toast.LENGTH_LONG).show();
					}
				}
			};
			new DirectoryChooserDialogHelper(context, resultListener, DsaTabApplication.getDsaTabHeroPath());
			return true;
		} else if (KEY_TIP_TODAY_RESET.equals(key)) {
			Editor edit = preferences.edit();
			for (String prefKey : preferences.getAll().keySet()) {
				if (prefKey.startsWith(Hint.PREF_PREFIX_HINT_STORAGE)) {
					edit.remove(prefKey);
				}
			}
			edit.commit();
			Toast.makeText(context, "Tips zurückgesetzt.", Toast.LENGTH_SHORT).show();
			return true;
		}

		return false;

	}

	protected static void handlePreferenceChange(Preference preference, SharedPreferences sharedPreferences, String key) {

		if (preference != null) {
			if (KEY_THEME.equals(key)) {
				preference.setSummary("Aktuelles Theme: " + DsaTabApplication.getInstance().getCustomThemeName());
			} else if (KEY_STYLE_BG_PATH.equals(key)) {
				((PreferenceWithButton) preference)
						.setWidgetVisibility(sharedPreferences.contains(KEY_STYLE_BG_PATH) ? View.VISIBLE : View.GONE);
			} else if (KEY_STYLE_BG_WOUNDS_PATH.equals(key)) {
				((PreferenceWithButton) preference).setWidgetVisibility(sharedPreferences
						.contains(KEY_STYLE_BG_WOUNDS_PATH) ? View.VISIBLE : View.GONE);
			} else if (KEY_SETUP_SDCARD_PATH.equals(key)) {
				preference.setSummary(DsaTabApplication.getInstance().getString(R.string.pref_sdcardPath_description)
						+ ": " + sharedPreferences.getString(KEY_SETUP_SDCARD_PATH, DsaTabApplication.getDsaTabPath()));
			} else if (KEY_SETUP_SDCARD_HERO_PATH.equals(key)) {
				preference
						.setSummary(DsaTabApplication.getInstance().getString(R.string.pref_sdcardHeroPath_description)
								+ ": "
								+ sharedPreferences.getString(KEY_SETUP_SDCARD_HERO_PATH,
										DsaTabApplication.getDsaTabHeroPath()));
			}
		}
	}

	protected static boolean handlePreferenceTreeClick(Activity context, PreferenceScreen screen, Preference preference) {

		AbstractDownloader downloader;
		if (KEY_DOWNLOAD_ALL.equals(preference.getKey())) {
			cleanOldFiles();
			downloader = DownloaderWrapper.getInstance(DsaTabApplication.getDsaTabPath(), context);
			downloader.addPath(context.getString(R.string.path_items));
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_MAPS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DsaTabApplication.getDsaTabPath() + DsaTabApplication.DIR_MAPS,
					context);
			downloader.addPath(PATH_OFFICIAL_MAP_PACK);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_ITEMS.equals(preference.getKey())) {
			cleanOldFiles();
			downloader = DownloaderWrapper.getInstance(DsaTabApplication.getDsaTabPath(), context);
			downloader.addPath(context.getString(R.string.path_items));
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_WESNOTH_PORTRAITS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DsaTabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_WESNOTH_PORTRAITS);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_BACKGROUNDS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DsaTabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_BACKGROUNDS);
			downloader.downloadZip();
			return true;
		} else if (KEY_DOWNLOAD_OSMMAPS.equals(preference.getKey())) {
			downloader = DownloaderWrapper.getInstance(DsaTabApplication.getDsaTabPath(), context);
			downloader.addPath(PATH_OSM_MAP_PACK);
			downloader.downloadZip();
			return true;
		} else if (KEY_CREDITS.equals(preference.getKey())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.title_credits);
			builder.setCancelable(true);
			WebView webView = new WebView(context);
			webView.getSettings().setDefaultTextEncodingName("utf-8");

			String summary = ResUtil.loadResToString(R.raw.credits, context);
			webView.loadDataWithBaseURL(null, summary, "text/html", "utf-8", null);
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
			ChangeLogDialog logDialog = new ChangeLogDialog(context);
			logDialog.show(true);
			return true;
		} else if (KEY_DONATE.equals(preference.getKey())) {
			Uri uriUrl = Uri.parse(DsaTabApplication.PAYPAL_DONATION_URL);
			final Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			context.startActivity(launchBrowser);
			return true;
		} else if (KEY_DSA_LICENSE.equals(preference.getKey())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.title_credits);
			builder.setCancelable(true);
			WebView webView = new WebView(context);
			webView.getSettings().setDefaultTextEncodingName("utf-8");
			String summary = ResUtil.loadResToString(R.raw.ulisses_license, context);
			webView.loadDataWithBaseURL(null, summary, "text/html", "utf-8", null);
			builder.setView(webView);
			builder.setNeutralButton(R.string.label_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		} else if (KEY_STYLE_BG_PATH.equals(preference.getKey())) {
			pickImage(context, ACTION_PICK_BG_PATH);
			return true;
		} else if (KEY_STYLE_BG_WOUNDS_PATH.equals(preference.getKey())) {
			pickImage(context, ACTION_PICK_BG_WOUNDS_PATH);
			return true;
		} else {
			return handlePreferenceClick(context, preference.getKey(),
					PreferenceManager.getDefaultSharedPreferences(context));
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static abstract class BasePreferenceFragment extends UnifiedPreferenceFragment implements
			OnSharedPreferenceChangeListener {

		/**
		 * 
		 */
		public BasePreferenceFragment() {
			// always make sure the res arg is set
			if (getArguments() == null) {
				Bundle bundle = new Bundle();
				bundle.putInt(ARG_PREFERENCE_RES, getPreferenceResourceId());
				setArguments(bundle);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onStart()
		 */
		@Override
		public void onStart() {
			super.onStart();

			// initPreferences(getPreferenceManager(), getPreferenceScreen());
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

			initPreferences(getPreferenceManager(), getPreferenceScreen());
			onBindPreferenceSummariesToValues();

			SharedPreferences preferences = DsaTabApplication.getPreferences();
			preferences.registerOnSharedPreferenceChangeListener(this);

		}

		public abstract int getPreferenceResourceId();

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onDestroy()
		 */
		@Override
		public void onDestroy() {
			super.onDestroy();
			SharedPreferences preferences = DsaTabApplication.getPreferences();
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
			if (!TextUtils.isEmpty(preference.getFragment())) {
				try {
					BasePreferenceFragment fragment = (BasePreferenceFragment) Class.forName(preference.getFragment())
							.newInstance();
					Bundle args = new Bundle();
					args.putInt(UnifiedPreferenceFragment.ARG_PREFERENCE_RES, fragment.getPreferenceResourceId());
					fragment.setArguments(args);
					((DsaTabPreferenceActivity) getActivity()).startPreferenceFragment(fragment, true);
					return true;
				} catch (Exception e) {
					Debug.error(e);
				}
				return false;
			} else {
				return handlePreferenceTreeClick(getActivity(), preferenceScreen, preference);
			}
		}
	}

	public static class PrefsSetupFragment extends BasePreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_setup;
		}
	}

	public static class PrefsDisplayFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_display;
		}
	}

	public static class PrefsDisplayHeaderFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_display_header;
		}
	}

	public static class PrefsDisplayDiceSliderFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_display_diceslider;
		}
	}

	public static class PrefsDownloadFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_setup_download;
		}

	}

	public static class PrefsHouseRulesFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_rules_houserules;
		}
	}

	public static class PrefsRulesFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_rules;
		}
	}

	public static class PrefsInfoFragment extends BasePreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.activity.BasePreferenceActivity.BasePreferenceFragment
		 * #getPreferenceResourceId()
		 */
		@Override
		public int getPreferenceResourceId() {
			return R.xml.preferences_hc_info;
		}
	}
}