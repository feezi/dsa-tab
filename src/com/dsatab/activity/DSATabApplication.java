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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AndroidRuntimeException;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroInfo;
import com.dsatab.view.drag.IconCache;
import com.dsatab.xml.XmlParser;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.ErrorHandler;

public class DSATabApplication extends Application {

	public static final String FLURRY_APP_ID = "AK17DSVJZBNH35G554YR";

	public static final String FULL_PACKAGE_NAME = "com.dsatab";

	public static final String DEFAULT_SD_CARD = "/sdcard/dsatab/";

	public static final String PAYPAL_DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=gandulf%2ek%40gmx%2enet&lc=DE&item_name=Gandulf&item_number=DsaTab&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted";

	public static final String TAG = "DSATab";

	// instance
	private static DSATabApplication instance = null;

	private Hero hero = null;

	private IconCache mIconCache;

	private DsaTabConfiguration configuration;

	private Typeface poorRichFont;

	public boolean liteShown = false;

	/**
	 * Convenient accessor, saves having to call and cast
	 * getApplicationContext()
	 */
	public static DSATabApplication getInstance() {
		checkInstance();
		return instance;
	}

	public boolean isLiteVersion() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getInstance().getBaseContext());
		return !preferences.getBoolean(DsaPreferenceActivity.KEY_FULL_VERSION, false);
	}

	public static String getDsaTabPath() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getInstance().getBaseContext());
		return preferences.getString(DsaPreferenceActivity.KEY_SETUP_SDCARD_PATH, DEFAULT_SD_CARD);
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

		mIconCache = new IconCache(this);
		configuration = new DsaTabConfiguration(this);

		poorRichFont = Typeface.createFromAsset(this.getAssets(), "fonts/poorich.ttf");
		Debug.setDebugTag(TAG);

		AnalyticsManager.setEnabled(getPreferences().getBoolean(DsaPreferenceActivity.KEY_USAGE_STATS, true));

		Debug.verbose("AnalytisManager enabled = " + AnalyticsManager.isEnabled());

		liteShown = false;
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
				if (file.isFile() && file.getName().endsWith(".xml")) {
					String heroName = getHeroName(file);
					if (heroName != null) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	public List<HeroInfo> getHeroes() {
		List<HeroInfo> heroes = new ArrayList<HeroInfo>();

		File profilesDir = new File(DSATabApplication.getDsaTabPath());
		if (!profilesDir.exists())
			profilesDir.mkdirs();

		File[] files = profilesDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".xml")) {
					String heroName = getHeroName(file);
					if (heroName != null) {
						HeroInfo info = new HeroInfo(heroName, file);
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

	private String getHeroName(File file) {
		String heroName = null;
		try {
			FileReader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader, 8 * 1024);
			String line = null;
			int lines = 0;
			while ((line = br.readLine()) != null) {
				int index = line.indexOf("name=\"");
				if (index >= 0) {
					heroName = line.substring(index + 6, line.indexOf("\"", index + 6));
					break;
				}
				if (lines++ > 10)
					break;
			}
			br.close();
		} catch (IOException e) {
			Debug.error(e);
		}
		return heroName;
	}

	public IconCache getIconCache() {
		return mIconCache;
	}

	public Hero getHero(String path) {
		Debug.verbose("Getting hero from " + path);
		if (path == null) {
			Debug.error("Error: Path was null ");
			return null;
		}

		FileInputStream fis = null;
		SharedPreferences preferences = getPreferences();

		try {
			File file = new File(path);
			if (!file.exists()) {
				Toast.makeText(this, "Error: Hero file not found at " + file.getAbsolutePath(), Toast.LENGTH_LONG)
						.show();
				Debug.error("Error: Hero file not found at " + file.getAbsolutePath());
				return null;
			}

			Debug.verbose("Opening inputstream for hero at " + path);
			fis = new FileInputStream(file);

			Debug.verbose("Opened inputstream for hero at " + path);

			hero = XmlParser.readHero(path, fis);
			if (hero != null) {
				Debug.verbose("Hero successfully parsed");

				Editor editor = preferences.edit();
				editor.putString(MainCharacterActivity.PREF_LAST_HERO, hero.getPath());
				editor.commit();
				Debug.verbose("Stored path of current hero in prefs:" + hero.getPath());

				Toast.makeText(this, getString(R.string.hero_loaded, hero.getName()), Toast.LENGTH_SHORT).show();

				Debug.verbose("Hero successfully loaded and return hero: " + hero.getName());
			} else {
				Debug.error("Hero could not be parsed, was null after XmlParserNew.readHero.");
			}

			return hero;
		} catch (Exception e) {

			Toast.makeText(this, "Held konnte nicht geladen werden.", Toast.LENGTH_LONG);
			ErrorHandler.handleError(e, this);

			// clear last hero since loading resulted in an error
			Editor editor = preferences.edit();
			editor.remove(MainCharacterActivity.PREF_LAST_HERO);
			editor.commit();
			throw new AndroidRuntimeException(e);
		} finally {
			if (fis != null) {
				Debug.verbose("Closing inputstream");
				try {
					fis.close();
				} catch (IOException e) {
					Debug.error(e);
				}
			}
		}
	}

	public void saveHero() {
		OutputStream out = null;
		try {
			out = new FileOutputStream(new File(hero.getPath()));
			XmlParser.writeHero(hero, out);
			hero.onHeroSaved();
			Toast.makeText(this, getString(R.string.hero_saved, hero.getName()), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, "Held konnte nicht gespeichert werden.", Toast.LENGTH_LONG);

			ErrorHandler.handleError(e, this);
			throw new AndroidRuntimeException(e);
		} finally {
			if (out != null) {
				Debug.verbose("Closing outputstream");
				try {
					out.close();
				} catch (IOException e) {
					Debug.error(e);
				}
			}
		}
	}

}