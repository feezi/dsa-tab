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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.AndroidRuntimeException;
import android.widget.Toast;

import com.dsatab.activity.DsaPreferenceActivityHC;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroFileInfo;
import com.dsatab.map.BitmapTileSource;
import com.dsatab.xml.Xml;
import com.dsatab.xml.XmlParser;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.ErrorHandler;

public class DSATabApplication extends Application implements OnSharedPreferenceChangeListener {

	/**
	 * 
	 */
	public static final String TILESOURCE_AVENTURIEN = "AVENTURIEN";

	public static final String FLURRY_APP_ID = "AK17DSVJZBNH35G554YR";

	public static final String SD_CARD_PATH_PREFIX = "/sdcard/";

	public static final String DEFAULT_SD_CARD = "dsatab/";

	public static final String DIR_MAPS = "maps";

	public static final String DIR_OSM_MAPS = "osm_map";

	public static final String DIR_PDFS = "pdfs";

	public static final String DIR_PORTRAITS = "portraits";

	public static final String DIR_CARDS = "cards";

	public static final String DIR_RECORDINGS = "recordings";

	public static final String PAYPAL_DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=gandulf%2ek%40gmx%2enet&lc=DE&item_name=Gandulf&item_number=DsaTab&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted";

	public static final String TAG = "DSATab";

	// instance
	private static DSATabApplication instance = null;

	/**
	 * Cache for corrected path
	 */
	private static String path;

	public Hero hero = null;

	private DsaTabConfiguration configuration;

	private Typeface poorRichFont;

	/**
	 * Convenient access, saves having to call and cast getApplicationContext()
	 */
	public static DSATabApplication getInstance() {
		checkInstance();
		return instance;
	}

	public boolean isLiteVersion() {
		return !getPreferences().getBoolean(DsaPreferenceActivityHC.KEY_FULL_VERSION, false);
	}

	public static File getDirectory(String name) {
		File mapfile = new File(Environment.getExternalStoragePublicDirectory(getRelativeDsaTabPath()), name);
		return mapfile;
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
		if (key.equals(DsaPreferenceActivityHC.KEY_SETUP_SDCARD_PATH)) {
			path = null;
			checkDirectories(getDsaTabPath());
		}

	}

	public static String getRelativeDsaTabPath() {
		String path = getDsaTabPath().replace(SD_CARD_PATH_PREFIX, "");
		return path;
	}

	public static String getDsaTabPath() {
		if (path == null) {
			path = getPreferences().getString(DsaPreferenceActivityHC.KEY_SETUP_SDCARD_PATH, DEFAULT_SD_CARD);

			if (!path.endsWith("/"))
				path += "/";

			if (!path.startsWith(SD_CARD_PATH_PREFIX)) {
				if (path.startsWith("/"))
					path = SD_CARD_PATH_PREFIX + path.substring(1);
				else
					path = SD_CARD_PATH_PREFIX + path;
			}
		}

		return path;
	}

	private static void checkDirectories(String path) {

		Debug.verbose("Checking path " + path + " for subdirs");
		File base = new File(path);
		if (!base.exists())
			base.mkdirs();

		File recordingsDir = new File(base, DIR_RECORDINGS);
		if (!recordingsDir.exists())
			recordingsDir.mkdirs();

		File mapsDir = new File(base, DIR_MAPS);
		if (!mapsDir.exists())
			mapsDir.mkdirs();

		File cardsDir = new File(base, DIR_CARDS);
		if (!cardsDir.exists())
			cardsDir.mkdirs();

		File portraitsDir = new File(base, DIR_PORTRAITS);
		if (!portraitsDir.exists())
			portraitsDir.mkdirs();

		File pdfsDir = new File(base, DIR_PDFS);
		if (!pdfsDir.exists())
			pdfsDir.mkdirs();

	}

	public static SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getInstance().getBaseContext());
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

	@Override
	public void onCreate() {
		// provide an instance for our static accessors
		instance = this;

		ErrorHandler.setup("gandulf.k@gmail.com", "DsaTab Fehlerbericht");

		configuration = new DsaTabConfiguration(this);

		poorRichFont = Typeface.createFromAsset(this.getAssets(), "fonts/poorich.ttf");
		Debug.setDebugTag(TAG);

		AnalyticsManager.setEnabled(getPreferences().getBoolean(DsaPreferenceActivityHC.KEY_USAGE_STATS, true));

		Debug.verbose("AnalytisManager enabled = " + AnalyticsManager.isEnabled());

		checkDirectories(getDsaTabPath());

		getPreferences().registerOnSharedPreferenceChangeListener(this);

		TileSourceFactory.getTileSources().clear();
		final ITileSource tileSource = new BitmapTileSource(TILESOURCE_AVENTURIEN, null, 2, 5, 256, ".jpg");
		TileSourceFactory.addTileSource(tileSource);

		disableConnectionReuseIfNecessary();

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

		File profilesDir = new File(DSATabApplication.getDsaTabPath());
		if (!profilesDir.exists())
			profilesDir.mkdirs();

		File[] files = profilesDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
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

	public List<HeroFileInfo> getHeroes() {
		List<HeroFileInfo> heroes = new ArrayList<HeroFileInfo>();

		File profilesDir = new File(DSATabApplication.getDsaTabPath());
		if (!profilesDir.exists())
			profilesDir.mkdirs();

		File[] files = profilesDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
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
		String path = null, name = null;

		FileInputStream fis = null;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			fis = new FileInputStream(file);
			xpp.setInput(fis, XmlParser.ENCODING);

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (xpp.getName().equals(Xml.KEY_HELD)) {
						path = xpp.getAttributeValue(null, Xml.KEY_PORTRAIT_PATH);
						name = xpp.getAttributeValue(null, Xml.KEY_NAME);
						break;
					}
				}
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			Debug.error(e);
		} catch (XmlPullParserException e) {
			Debug.error(e);
		} catch (IOException e) {
			Debug.error(e);
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
			}
		}
		if (name != null)
			return new HeroFileInfo(name, file, path);
		else
			return null;

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
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(hero.getPath()));
			hero.onPreHeroSaved();
			XmlParser.writeHero(hero, out);
			hero.onPostHeroSaved();

			saveHeroConfiguration();
			Toast.makeText(this, getString(R.string.hero_saved, hero.getName()), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, "Held konnte nicht gespeichert werden.", Toast.LENGTH_LONG);
			ErrorHandler.handleError(e, this);
			throw new AndroidRuntimeException(e);
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

}