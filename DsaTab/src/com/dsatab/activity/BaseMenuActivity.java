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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.gandulf.guilib.util.Debug;

public abstract class BaseMenuActivity extends Activity {

	public static final String PREF_LAST_HERO = "LAST_HERO";

	protected static final int ACTION_PREFERENCES = 1;
	protected static final int ACTION_INVENTORY = 3;
	protected static final int ACTION_CHOOSE_HERO = 4;

	protected SharedPreferences preferences;

	public BaseMenuActivity() {

	}

	public Hero getHero() {
		return DSATabApplication.getInstance().getHero();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return (getHero());
	}

	protected final void loadHero(String heroPath) {

		Hero oldHero = DSATabApplication.getInstance().getHero();
		if (oldHero != null)
			onHeroUnloaded(oldHero);

		Hero hero = DSATabApplication.getInstance().getHero(heroPath);
		if (hero != null) {
			onHeroLoaded(hero);
		}
	}

	private void loadHero() {
		if (getLastNonConfigurationInstance() instanceof Hero) {
			onHeroLoaded((Hero) getLastNonConfigurationInstance());
		} else if (DSATabApplication.getInstance().getHero() != null) {
			onHeroLoaded(DSATabApplication.getInstance().getHero());
		} else {
			String heroPath = preferences.getString(PREF_LAST_HERO, null);
			if (heroPath != null && new File(heroPath).exists()) {
				loadHero(heroPath);
			} else {
				showHeroChooser();
			}
		}
	}

	protected abstract void onHeroLoaded(Hero hero);

	protected abstract void onHeroUnloaded(Hero hero);

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ACTION_CHOOSE_HERO && resultCode == RESULT_OK) {
			String heroPath = data.getStringExtra(HeroChooserActivity.INTENT_NAME_HERO_PATH);
			Debug.verbose("HeroChooserActivity returned with path:" + heroPath);
			loadHero(heroPath);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		loadHero();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		Hero hero = DSATabApplication.getInstance().getHero();
		menu.findItem(R.id.option_save_hero).setEnabled(hero != null);
		menu.findItem(R.id.option_items).setEnabled(hero != null);
		menu.findItem(R.id.option_notes).setEnabled(hero != null);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.option_load_hero) {
			showHeroChooser();
			return true;
		} else if (item.getItemId() == R.id.option_save_hero) {
			DSATabApplication.getInstance().saveHero();
			return true;
		} else if (item.getItemId() == R.id.option_settings) {
			startActivityForResult(new Intent(this, DsaPreferenceActivity.class), ACTION_PREFERENCES);
			return true;
		} else if (item.getItemId() == R.id.option_map) {
			startActivity(new Intent(this, MapActivity.class));
			return true;
		} else if (item.getItemId() == R.id.option_notes) {
			if (!getClass().equals(NotesActivity.class)) {
				startActivity(new Intent(this, NotesActivity.class));
			}
			return true;
		} else if (item.getItemId() == R.id.option_items) {
			if (!getClass().equals(ItemsActivity.class)) {
				startActivityForResult(new Intent(this, ItemsActivity.class), ACTION_INVENTORY);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void showHeroChooser() {

		if (DSATabApplication.getInstance().getHeroes().isEmpty()) {
			// --
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Keine Helden gefunden");
			builder.setMessage("Auf der SD-Karte wurden keine Helden-Dateien gefunden. Stell sicher, dass sich unter "
					+ DSATabApplication.getDsaTabPath()
					+ " die als XML Datei exportierten Helden der Helden-Software befinden.");

			builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivityForResult(new Intent(BaseMenuActivity.this, DsaPreferenceActivity.class),
							ACTION_PREFERENCES);
				}
			});
			builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					BaseMenuActivity.this.finish();
				}

			});
			builder.show();
		} else {
			startActivityForResult(new Intent(BaseMenuActivity.this, HeroChooserActivity.class), ACTION_CHOOSE_HERO);
		}
	}
}
