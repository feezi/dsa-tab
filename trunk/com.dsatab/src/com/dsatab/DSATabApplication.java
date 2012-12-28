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
package com.dsatab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.jdom2.Document;
import org.jdom2.Element;
import org.json.JSONException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroFileInfo;
import com.dsatab.db.DatabaseHelper;
import com.dsatab.map.BitmapTileSource;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;
import com.dsatab.xml.DataManager;
import com.dsatab.xml.Xml;
import com.dsatab.xml.XmlParser;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DSATabApplication extends Application implements OnSharedPreferenceChangeListener {

	/**
	 * 
	 */
	public static final String TILESOURCE_AVENTURIEN = "AVENTURIEN";

	public static final String FLURRY_APP_ID = "AK17DSVJZBNH35G554YR";

	public static final String BUGSENSE_API_KEY = "4b4062da";

	public static final String SD_CARD_PATH_PREFIX = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator;

	public static final String DEFAULT_SD_CARD = "dsatab/";

	public static final String DIR_MAPS = "maps";

	public static final String DIR_OSM_MAPS = "osm_map";

	public static final String DIR_PDFS = "pdfs";

	public static final String DIR_PORTRAITS = "portraits";

	public static final String DIR_CARDS = "cards";

	public static final String DIR_BACKGROUNDS = "backgrounds";

	public static final String DIR_RECORDINGS = "recordings";

	public static final String PAYPAL_DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=gandulf%2ek%40gmx%2enet&lc=DE&item_name=Gandulf&item_number=DsaTab&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted";

	public static final String TAG = "DSATab";

	public static final String THEME_LIGHT_PLAIN = "light_plain";
	public static final String THEME_DARK_PLAIN = "dark_plain";
	public static final String THEME_DEFAULT = THEME_LIGHT_PLAIN;

	// instance
	private static DSATabApplication instance = null;

	/**
	 * Cache for corrected path
	 */
	private static String basePath, heroPath;
	private static File baseDir;

	public Hero hero = null;

	private DsaTabConfiguration configuration;

	private DatabaseHelper databaseHelper = null;

	private Typeface poorRichFont;

	/**
	 * Convenient access, saves having to call and cast getApplicationContext()
	 */
	public static DSATabApplication getInstance() {
		checkInstance();
		return instance;
	}

	protected static File getBaseDirectory() {
		if (baseDir == null)
			baseDir = new File(Environment.getExternalStorageDirectory(), getRelativeDsaTabPath());
		return baseDir;
	}

	public static File getDirectory(String name) {
		File dirFile = new File(getBaseDirectory(), name);
		return dirFile;
	}

	private void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
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
		if (key.equals(BasePreferenceActivity.KEY_SETUP_SDCARD_PATH)) {
			basePath = null;
			baseDir = null;
			checkDirectories();
		} else if (key.equals(BasePreferenceActivity.KEY_SETUP_SDCARD_HERO_PATH)) {
			heroPath = null;
			checkDirectories();
		}

	}

	public static String getRelativeDsaTabPath() {
		String path = getDsaTabPath().substring(SD_CARD_PATH_PREFIX.length());
		return path;
	}

	public static String getDsaTabPath() {
		if (basePath == null) {
			basePath = getPreferences().getString(BasePreferenceActivity.KEY_SETUP_SDCARD_PATH, DEFAULT_SD_CARD);

			if (!basePath.endsWith("/"))
				basePath += "/";

			if (!basePath.startsWith(SD_CARD_PATH_PREFIX)) {
				if (basePath.startsWith("/"))
					basePath = SD_CARD_PATH_PREFIX + basePath.substring(1);
				else
					basePath = SD_CARD_PATH_PREFIX + basePath;
			}
		}

		return basePath;
	}

	public static String getRelativeDsaTabHeroPath() {
		String path = getDsaTabHeroPath().substring(SD_CARD_PATH_PREFIX.length());
		return path;
	}

	public static String getDsaTabHeroPath() {
		if (heroPath == null) {
			heroPath = getPreferences().getString(BasePreferenceActivity.KEY_SETUP_SDCARD_HERO_PATH, DEFAULT_SD_CARD);

			if (!heroPath.endsWith("/"))
				heroPath += "/";

			if (!heroPath.startsWith(SD_CARD_PATH_PREFIX)) {
				if (heroPath.startsWith("/"))
					heroPath = SD_CARD_PATH_PREFIX + heroPath.substring(1);
				else
					heroPath = SD_CARD_PATH_PREFIX + heroPath;
			}
		}

		return heroPath;
	}

	private static void checkDirectories() {

		Debug.verbose("Checking dsatab dir " + getDsaTabPath() + " for subdirs");

		File base = getBaseDirectory();
		if (!base.exists())
			base.mkdirs();

		File recordingsDir = getDirectory(DIR_RECORDINGS);
		if (!recordingsDir.exists())
			recordingsDir.mkdirs();

		File mapsDir = getDirectory(DIR_MAPS);
		if (!mapsDir.exists())
			mapsDir.mkdirs();

		File osmmapsDir = getDirectory(DIR_OSM_MAPS);
		if (!osmmapsDir.exists())
			osmmapsDir.mkdirs();

		File cardsDir = getDirectory(DIR_CARDS);
		if (!cardsDir.exists())
			cardsDir.mkdirs();

		File portraitsDir = getDirectory(DIR_PORTRAITS);
		if (!portraitsDir.exists())
			portraitsDir.mkdirs();

		File pdfsDir = getDirectory(DIR_PDFS);
		if (!pdfsDir.exists())
			pdfsDir.mkdirs();

		File bgDir = getDirectory(DIR_BACKGROUNDS);
		if (!bgDir.exists())
			bgDir.mkdirs();

		Debug.verbose("Checking dsatab herodir " + getDsaTabHeroPath());
		File heroes = new File(getDsaTabHeroPath());
		if (!heroes.exists())
			heroes.mkdirs();
	}

	public static SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getInstance());
	}

	public DsaTabConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Accessor for some resource that depends on a context
	 */

	private static void checkInstance() {
		if (instance == null)
			throw new IllegalStateException("Application not created yet!");
	}

	public int getCustomTheme() {
		String theme = getPreferences().getString(BasePreferenceActivity.KEY_THEME, THEME_DEFAULT);

		if (THEME_LIGHT_PLAIN.equals(theme)) {
			return R.style.DsaTabTheme_Light;
		} else if (THEME_DARK_PLAIN.equals(theme)) {
			return R.style.DsaTabTheme_Dark;
		} else {
			return R.style.DsaTabTheme_Light;
		}

	}

	public int getCustomPreferencesTheme() {
		String theme = getPreferences().getString(BasePreferenceActivity.KEY_THEME, THEME_DEFAULT);

		if (THEME_LIGHT_PLAIN.equals(theme)) {
			return R.style.DsaTabTheme_Light;
		} else if (THEME_DARK_PLAIN.equals(theme)) {
			return R.style.DsaTabTheme_Dark;
		} else {
			return R.style.DsaTabTheme_Light;
		}
	}

	public String getCustomThemeValue() {
		String theme = getPreferences().getString(BasePreferenceActivity.KEY_THEME, THEME_DEFAULT);

		List<String> themeValues = Arrays.asList(getResources().getStringArray(R.array.themesValues));
		int index = themeValues.indexOf(theme);
		if (index >= 0 && index < themeValues.size()) {
			return themeValues.get(index);
		} else
			return themeValues.get(0);

	}

	public String getCustomThemeName() {
		String theme = getPreferences().getString(BasePreferenceActivity.KEY_THEME, THEME_DEFAULT);

		List<String> themeValues = Arrays.asList(getResources().getStringArray(R.array.themesValues));
		int index = themeValues.indexOf(theme);

		String[] themes = getResources().getStringArray(R.array.themes);
		if (index >= 0 && index < themes.length)
			return themes[index];
		else
			return themes[0];
	}

	public int getCustomDialogTheme() {
		String theme = getPreferences().getString(BasePreferenceActivity.KEY_THEME, THEME_DEFAULT);

		if (THEME_LIGHT_PLAIN.equals(theme)) {
			return R.style.Theme_Dialog_Light;
		} else if (THEME_DARK_PLAIN.equals(theme)) {
			return R.style.Theme_Dialog_Dark;
		} else {
			return R.style.Theme_Dialog_Light;
		}
	}

	@Override
	public void onCreate() {
		// provide an instance for our static accessors
		instance = this;

		cleanUp();

		setTheme(getCustomTheme());

		configuration = new DsaTabConfiguration(this);

		poorRichFont = Typeface.createFromAsset(this.getAssets(), "fonts/poorich.ttf");
		boolean stats = getPreferences().getBoolean(BasePreferenceActivity.KEY_USAGE_STATS, true);

		AnalyticsManager.setEnabled(stats);
		if (stats) {
			BugSenseHandler.setup(this, BUGSENSE_API_KEY);
		}

		Debug.verbose("AnalytisManager enabled = " + AnalyticsManager.isEnabled());

		checkDirectories();

		getPreferences().registerOnSharedPreferenceChangeListener(this);

		DataManager.init(getApplicationContext());

		TileSourceFactory.getTileSources().clear();
		final ITileSource tileSource = new BitmapTileSource(TILESOURCE_AVENTURIEN, null, 2, 5, 256, ".jpg");
		TileSourceFactory.addTileSource(tileSource);

		disableConnectionReuseIfNecessary();

		File token = new File(getBaseDirectory(), "token.txt");
		if (token.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(token));
				String tokenValue = reader.readLine();
				reader.close();
				if (!TextUtils.isEmpty(tokenValue)) {
					Editor edit = getPreferences().edit();
					edit.putString(BasePreferenceActivity.KEY_EXCHANGE_TOKEN, tokenValue.trim());
					edit.commit();
				}
			} catch (FileNotFoundException e) {
				Debug.error(e);
			} catch (IOException e) {
				Debug.error(e);
			}

		}

	}

	private void cleanUp() {

		// make sure we have a valid theme
		SharedPreferences preferences = getPreferences();
		String theme = preferences.getString(BasePreferenceActivity.KEY_THEME, THEME_DEFAULT);
		List<String> themeValues = Arrays.asList(getResources().getStringArray(R.array.themesValues));
		int index = themeValues.indexOf(theme);
		if (index < 0) {
			Editor edit = preferences.edit();
			edit.putString(BasePreferenceActivity.KEY_THEME, THEME_DEFAULT);
			edit.commit();
		}

	}

	public Typeface getPoorRichardFont() {
		return poorRichFont;
	}

	public Hero getHero() {
		if (hero != null)
			return hero;
		else
			return null;
	}

	public boolean hasHeroes() {
		boolean result = false;

		File profilesDir = new File(DSATabApplication.getDsaTabHeroPath());
		if (!profilesDir.exists())
			profilesDir.mkdirs();

		File[] files = profilesDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase(Locale.GERMAN).endsWith(".xml")) {
					HeroFileInfo info = getHeroInfo(file);
					if (info != null) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 
	 */
	public void release() {
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	public List<HeroFileInfo> getHeroes() {
		List<HeroFileInfo> heroes = new ArrayList<HeroFileInfo>();

		File profilesDir = new File(DSATabApplication.getDsaTabHeroPath());
		if (!profilesDir.exists())
			profilesDir.mkdirs();

		File[] files = profilesDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase(Locale.GERMAN).endsWith(".xml")) {
					HeroFileInfo info = getHeroInfo(file);
					if (info != null) {
						heroes.add(info);
					}
				}
			}
		} else {
			Debug.warning("Unable to read directory " + profilesDir.getAbsolutePath()
					+ ". Make sure the directory exists and contains your heros");
		}

		return heroes;
	}

	private HeroFileInfo getHeroInfo(File file) {
		try {
			return new HeroFileInfo(file);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public void saveHeroConfiguration() throws JSONException, IOException {

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(hero.getPath() + ".dsatab"));
			out.write(hero.getHeroConfiguration().toJSONObject().toString().getBytes());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Debug.error(e);
				}
			}
		}

	}

	public void saveHero() {
		if (hero == null) {
			Toast.makeText(getApplicationContext(),
					"Held kann nicht gespeichert werden, da noch kein Held geladen wurde.", Toast.LENGTH_SHORT).show();
			return;
		}
		FileOutputStream out = null;
		File destFile = new File(hero.getPath());
		try {
			String error = Util.checkFileWriteAccess(destFile);
			if (error != null) {
				Toast.makeText(this, error, Toast.LENGTH_LONG).show();
				return;
			}

			FileInputStream fis = new FileInputStream(destFile);
			Document dom = XmlParser.readDocument(fis);
			fis.close();

			Element heroElement = (Element) dom.getRootElement().getChild(Xml.KEY_HELD);
			XmlParser.onPreHeroSaved(hero, heroElement);

			out = new FileOutputStream(destFile);
			XmlParser.writeHero(hero, dom, out);
			hero.onPostHeroSaved();

			saveHeroConfiguration();
			Toast.makeText(this, getString(R.string.hero_saved, hero.getName()), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, "Held konnte nicht gespeichert werden.", Toast.LENGTH_LONG).show();
			throw new DsaTabRuntimeException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Debug.error(e);
				}
			}
		}
	}

	public DatabaseHelper getDBHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(getApplicationContext(), DatabaseHelper.class);
		}
		return databaseHelper;
	}

}