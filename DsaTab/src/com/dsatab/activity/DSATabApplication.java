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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.view.drag.IconCache;
import com.dsatab.xml.XmlParserNew;
import com.gandulf.guilib.util.Debug;

public class DSATabApplication extends Application {

	public static String DEFAULT_SD_CARD = "/sdcard/dsatab/";

	public static String TAG = "DSATab";

	// instance
	private static DSATabApplication instance = null;

	private Hero hero = null;

	private IconCache mIconCache;

	private DsaTabConfiguration configuration;

	/**
	 * Convenient accessor, saves having to call and cast
	 * getApplicationContext()
	 */
	public static DSATabApplication getInstance() {
		checkInstance();
		return instance;
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

		mIconCache = new IconCache(this);
		configuration = new DsaTabConfiguration(this);

		Debug.setDebugTag(TAG);
	}

	public Hero getHero() {
		if (hero != null)
			return hero;
		else
			return getHero(null);
	}

	public List<String> getHeroes() {
		List<String> heroes = new ArrayList<String>();

		File profilesDir = new File(DSATabApplication.getDsaTabPath());
		if (!profilesDir.exists())
			profilesDir.mkdirs();

		File[] files = profilesDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".xml")) {
					heroes.add(file.getName());
				}
			}
		} else {
			Debug.warning("Unable to read directory " + profilesDir.getAbsolutePath()
					+ ". Make sure the directory exists and contains your heros");
		}

		return heroes;
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
		try {
			File file = new File(path);
			if (!file.exists()) {
				Toast.makeText(this, "Error: Hero file not found at " + file.getAbsolutePath(), Toast.LENGTH_LONG)
						.show();
				Debug.error("Error: Hero file not found at " + file.getAbsolutePath());
				return null;
			}

			XmlParserNew.normalize(file);

			Debug.verbose("Opening inputstream for hero at " + path);
			fis = new FileInputStream(file);

			Debug.verbose("Opened inputstream for hero at " + path);
			hero = XmlParserNew.readHero(path, fis);
			if (hero != null) {
				Debug.verbose("Hero successfully parsed");

				SharedPreferences preferences = getPreferences();
				Editor editor = preferences.edit();
				editor.putString(MainActivity.PREF_LAST_HERO, hero.getPath());
				editor.commit();
				Debug.verbose("Stored path of current hero in prefs:" + hero.getPath());

				Toast.makeText(this, getString(R.string.hero_loaded, hero.getName()), Toast.LENGTH_SHORT).show();

				Debug.verbose("Hero successfully loaded and return hero: " + hero.getName());
			} else {
				Debug.error("Hero could not be parsed, was null after XmlParserNew.readHero.");
			}

			return hero;
		} catch (IOException e) {
			Debug.error(e);
			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			return null;
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
			XmlParserNew.writeHero(hero, out);
			hero.onHeroSaved();
			Toast.makeText(this, getString(R.string.hero_saved, hero.getName()), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Debug.error(e);
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