/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.AsyncTaskLoader;

import com.dsatab.DSATabApplication;
import com.dsatab.HeroConfiguration;
import com.dsatab.activity.MainActivity;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.util.Debug;
import com.dsatab.xml.Xml;
import com.dsatab.xml.XmlParser;

/**
 * @author Ganymede
 * 
 */
public class HeroLoader extends AsyncTaskLoader<Hero> {

	private Hero hero;

	private String path;

	public HeroLoader(Context context, String heroPath) {
		super(context);
		this.path = heroPath;

	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (hero != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(hero);
		}

		if (takeContentChanged() || hero == null) {
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 */
	@Override
	public Hero loadInBackground() {

		Debug.verbose("Getting hero from " + path);
		if (path == null) {
			Debug.error("Error: Path was null ");
			return null;
		}

		FileInputStream fis = null;
		SharedPreferences preferences = DSATabApplication.getPreferences();

		try {
			File file = new File(path);
			if (!file.exists()) {
				Debug.error("Error: Hero file not found at " + file.getAbsolutePath());
				Editor editor = preferences.edit();
				editor.remove(MainActivity.PREF_LAST_HERO);
				editor.commit();
				return null;
			}

			fis = new FileInputStream(file);
			hero = XmlParser.readHero(path, fis, this);
			hero.setHeroConfiguration(loadHeroConfiguration());
			if (hero != null) {
				Debug.verbose("Hero successfully parsed");

				Editor editor = preferences.edit();
				editor.putString(MainActivity.PREF_LAST_HERO, hero.getPath());
				editor.commit();
				Debug.verbose("Stored path of current hero in prefs:" + hero.getPath());

				Debug.verbose("Hero successfully loaded and return hero: " + hero.getName());
			} else {
				Debug.error("Hero could not be parsed, was null after XmlParserNew.readHero.");
			}

			return hero;
		} catch (Exception e) {
			// clear last hero since loading resulted in an error
			Editor editor = preferences.edit();
			editor.remove(MainActivity.PREF_LAST_HERO);
			editor.commit();
			throw new DsaTabRuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					Debug.error(e);
				}
			}

		}
	}

	public HeroConfiguration loadHeroConfiguration() throws IOException, JSONException, ClassNotFoundException {

		FileInputStream fis = null;
		HeroConfiguration heroConfiguration = null;
		try {

			File file = new File(hero.getPath() + ".dsatab");

			if (file.exists() && file.length() > 0) {
				fis = new FileInputStream(file);

				byte[] data = new byte[(int) file.length()];
				fis.read(data);

				JSONObject jsonObject = new JSONObject(new String(data));
				heroConfiguration = new HeroConfiguration(hero, jsonObject);
			} else {

				String tabConfig = hero.getHeldElement().getAttributeValue(Xml.TAB_CONFIG);
				if (tabConfig != null) {
					try {
						JSONObject jsonObject = new JSONObject(tabConfig);
						heroConfiguration = new HeroConfiguration(hero, jsonObject);
					} catch (JSONException e) {
						Debug.error(e);
						heroConfiguration = null;
					} catch (ClassNotFoundException e) {
						Debug.error(e);
						heroConfiguration = null;
					}
				}
			}

			if (heroConfiguration == null) {
				heroConfiguration = new HeroConfiguration(hero);
				heroConfiguration.reset();
			}
		} finally {
			if (fis != null)
				fis.close();
		}

		return heroConfiguration;

	}

}
