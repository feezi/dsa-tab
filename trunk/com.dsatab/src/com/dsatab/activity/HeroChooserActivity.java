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

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.cloud.HeroExchange;
import com.dsatab.cloud.HeroExchange.OnHeroExchangeListener;
import com.dsatab.data.HeroFileInfo;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;

/**
 * 
 * 
 */
public class HeroChooserActivity extends BaseActivity implements AdapterView.OnItemClickListener {

	public static final String INTENT_NAME_HERO_PATH = "heroPath";

	private static final int CONTEXTMENU_DELETEITEM = 1;
	private static final int CONTEXTMENU_DOWNLOADITEM = 2;
	private static final int CONTEXTMENU_UPLOADITEM = 3;

	private static final String DUMMY_FILE = "Dummy.xml";
	private static final String DUMMY_NAME = "Dummy";

	private GridView list;
	private HeroAdapter adapter;
	private boolean dummy;

	/**
	 * 
	 */
	public HeroChooserActivity() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(DSATabApplication.getInstance().getCustomTheme());
		applyPreferencesToTheme();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sheet_hero_chooser);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		List<HeroFileInfo> heroes = null;

		// create test hero if no heroes avialable
		if (!DSATabApplication.getInstance().hasHeroes()) {
			dummy = true;

			FileOutputStream fos = null;
			InputStream fis = null;
			try {

				fos = new FileOutputStream(DSATabApplication.getDsaTabPath() + DUMMY_FILE);
				fis = new BufferedInputStream(getAssets().open(DUMMY_FILE));
				byte[] buffer = new byte[8 * 1024];
				int length;

				while ((length = fis.read(buffer)) >= 0) {
					fos.write(buffer, 0, length);
				}

			} catch (FileNotFoundException e) {
				Debug.error(e);
			} catch (IOException e) {
				Debug.error(e);
			} finally {
				try {
					if (fos != null)
						fos.close();

					if (fis != null)
						fis.close();
				} catch (IOException e) {

				}
			}

		} else {

			heroes = DSATabApplication.getInstance().getHeroes();

			if (heroes.size() == 1 && heroes.get(0).getName().equals(DUMMY_NAME))
				dummy = true;
		}

		if (heroes == null)
			heroes = DSATabApplication.getInstance().getHeroes();

		list = (GridView) findViewById(R.id.popup_hero_chooser_list);
		adapter = new HeroAdapter(this, R.layout.hero_chooser_item, heroes);
		list.setAdapter(adapter);
		registerForContextMenu(list);
		list.setOnItemClickListener(this);

		updateViews();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Util.unbindDrawables(findViewById(android.R.id.content));
		super.onDestroy();
	}

	private void updateViews() {
		TextView empty = (TextView) findViewById(R.id.popup_hero_empty);

		if (!DSATabApplication.getInstance().hasHeroes() || dummy) {

			if (dummy)
				list.setVisibility(View.VISIBLE);
			else
				list.setVisibility(View.INVISIBLE);

			empty.setVisibility(View.VISIBLE);
			empty.setText(Util.getText(R.string.message_heroes_empty, DSATabApplication.getRelativeDsaTabPath()));
		} else {

			list.setVisibility(View.VISIBLE);
			empty.setVisibility(View.GONE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.popup_hero_chooser_list) {

			AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
			HeroFileInfo heroFilenfo = adapter.getItem(adapterContextMenuInfo.position);

			if (heroFilenfo.isOnline()) {
				menu.add(0, CONTEXTMENU_DOWNLOADITEM, 0, getString(R.string.menu_download_item));
			}
			// hero export disabled for now!
			menu.add(0, CONTEXTMENU_UPLOADITEM, 0, getString(R.string.menu_upload_item)).setEnabled(false);
			menu.add(0, CONTEXTMENU_DELETEITEM, 0, getString(R.string.menu_delete_item));
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockActivity#onPrepareOptionsMenu(com.
	 * actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		com.actionbarsherlock.view.MenuItem menuItem = menu.findItem(R.id.option_hero_import);
		if (menuItem != null) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				menuItem.setEnabled(true);
			} else {
				menuItem.setEnabled(false);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockActivity#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_hero_import, Menu.NONE,
				"Aus Heldenaustausch importieren");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		item.setIcon(R.drawable.ic_menu_account_list);

		item = menu.add(Menu.NONE, R.id.option_settings, Menu.NONE, "Einstellungen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_preferences);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.option_hero_import:

			HeroExchange exchange = new HeroExchange(this);
			exchange.setOnHeroExchangeListener(new OnHeroExchangeListener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.dsatab.common.HeroExchange.OnHeroExchangeListener#
				 * onHeroLoaded(java.lang.String)
				 */
				@Override
				public void onHeroLoaded(String path) {
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.dsatab.common.HeroExchange.OnHeroExchangeListener#
				 * onHeroInfoLoaded(com.dsatab.data.HeroOnlineInfo)
				 */
				@Override
				public void onHeroInfoLoaded(HeroFileInfo info) {

					for (int i = 0; i < adapter.getCount(); i++) {
						HeroFileInfo heroInfo = adapter.getItem(i);

						if (heroInfo.getKey() != null && heroInfo.getKey().equals(info.getKey())) {
							heroInfo.id = info.id;
							// heroInfo.name = info.name;
							adapter.notifyDataSetChanged();
							// found
							return;
						}
					}
					adapter.add(info);
				}
			});
			exchange.syncHeroes();
			return true;
		case R.id.option_settings:
			BasePreferenceActivity.startPreferenceActivity(this);
			return true;
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CONTEXTMENU_DELETEITEM) {
			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			HeroFileInfo heroInfo = (HeroFileInfo) list.getItemAtPosition(menuInfo.position);

			Debug.verbose("Deleting " + heroInfo.getName());
			heroInfo.getFile().delete();

			adapter.remove(heroInfo);
			adapter.notifyDataSetChanged();

			return true;
		} else if (item.getItemId() == CONTEXTMENU_DOWNLOADITEM) {
			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			final HeroFileInfo heroInfo = (HeroFileInfo) list.getItemAtPosition(menuInfo.position);

			HeroExchange exchange = new HeroExchange(this);
			exchange.setOnHeroExchangeListener(new OnHeroExchangeListener() {
				@Override
				public void onHeroLoaded(String path) {
					Toast.makeText(HeroChooserActivity.this, heroInfo.getName() + " wurde erfolgreich heruntergeladen",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onHeroInfoLoaded(HeroFileInfo info) {
				}
			});

			exchange.importHero(heroInfo);

		} else if (item.getItemId() == CONTEXTMENU_UPLOADITEM) {
			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			final HeroFileInfo heroInfo = (HeroFileInfo) list.getItemAtPosition(menuInfo.position);

			HeroExchange exchange = new HeroExchange(this);
			exchange.exportHero(heroInfo.getFile());
			return true;
		}
		return super.onContextItemSelected(item);
	}

	static class HeroAdapter extends ArrayAdapter<HeroFileInfo> {

		LayoutInflater layoutInflater;

		public HeroAdapter(Context context, int textViewResourceId, List<HeroFileInfo> objects) {
			super(context, textViewResourceId, objects);

			layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewGroup layout = null;
			if (convertView instanceof ViewGroup) {
				layout = (ViewGroup) convertView;
			} else {
				layout = (ViewGroup) layoutInflater.inflate(R.layout.hero_chooser_item, null);
				layout.setFocusable(false);
				layout.setClickable(false);
			}

			TextView tv = (TextView) layout.findViewById(android.R.id.text1);
			ImageView iv = (ImageView) layout.findViewById(android.R.id.icon);
			ImageView cloud = (ImageView) layout.findViewById(R.id.hero_cloud);

			HeroFileInfo hero = getItem(position);
			tv.setText(hero.getName());

			if (hero.getPortraitUri() != null) {
				iv.setImageURI(Uri.parse(hero.getPortraitUri()));
			}

			if (hero.isOnline()) {
				cloud.setVisibility(View.VISIBLE);
			} else {
				cloud.setVisibility(View.GONE);
			}
			return layout;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == MainActivity.ACTION_PREFERENCES) {
			adapter = new HeroAdapter(this, R.layout.hero_chooser_item, DSATabApplication.getInstance().getHeroes());
			list.setAdapter(adapter);

			updateViews();

			updateFullscreenStatus(preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HeroFileInfo hero = (HeroFileInfo) parent.getItemAtPosition(position);

		if (hero.getFile() != null) {
			Intent intent = new Intent();
			intent.putExtra(INTENT_NAME_HERO_PATH, hero.getFile().toString());
			setResult(RESULT_OK, intent);
			finish();
		} else {
			HeroExchange exchange = new HeroExchange(this);
			exchange.setOnHeroExchangeListener(new OnHeroExchangeListener() {
				@Override
				public void onHeroLoaded(String path) {
					Intent intent = new Intent();
					intent.putExtra(INTENT_NAME_HERO_PATH, path);
					setResult(RESULT_OK, intent);
					finish();
				}

				@Override
				public void onHeroInfoLoaded(HeroFileInfo info) {
				}
			});

			exchange.importHero(hero);
		}
	}

}
