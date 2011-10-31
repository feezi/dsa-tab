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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.common.HeroExchange;
import com.dsatab.common.HeroExchange.OnHeroExchangeListener;
import com.dsatab.common.Util;
import com.dsatab.data.HeroFileInfo;
import com.gandulf.guilib.util.Debug;

/**
 * 
 * 
 */
public class HeroChooserActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnClickListener {

	public static final String INTENT_NAME_HERO_PATH = "heroPath";

	private static final int CONTEXTMENU_DELETEITEM = 1;

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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_hero_chooser);

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

		findViewById(R.id.popup_hero_import).setOnClickListener(this);

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
			menu.add(0, CONTEXTMENU_DELETEITEM, 0, getString(R.string.menu_delete_item));
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.hero_chooser_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.option_settings:
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				startActivityForResult(new Intent(this, DsaPreferenceActivity.class),
						MainActivity.ACTION_PREFERENCES);
			} else {
				startActivityForResult(new Intent(this, DsaPreferenceActivityHC.class), MainActivity.ACTION_PREFERENCES);
			}
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
		}
		return super.onContextItemSelected(item);
	}

	class HeroAdapter extends ArrayAdapter<HeroFileInfo> {

		public HeroAdapter(Context context, int textViewResourceId, List<HeroFileInfo> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewGroup layout = null;
			if (convertView instanceof ViewGroup) {
				layout = (ViewGroup) convertView;
			} else {
				layout = (ViewGroup) getLayoutInflater().inflate(R.layout.hero_chooser_item, null);
				layout.setFocusable(false);
				layout.setClickable(false);
			}

			TextView tv = (TextView) layout.findViewById(R.id.textView);
			ImageView iv = (ImageView) layout.findViewById(R.id.imageView);

			HeroFileInfo hero = getItem(position);
			tv.setText(hero.getName());

			if (hero.getPortraitUri() != null) {
				iv.setImageURI(Uri.parse(hero.getPortraitUri()));
			} else {
				iv.setImageResource(R.drawable.profile_blank);
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

			updateFullscreenStatus(preferences.getBoolean(DsaPreferenceActivityHC.KEY_FULLSCREEN, true));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.popup_hero_import:
			HeroExchange exchange = new HeroExchange(this);
			exchange.setOnHeroExchangeListener(new OnHeroExchangeListener() {
				@Override
				public void onHeroExported() {
				}

				@Override
				public void onHeroLoaded(String path) {
					Intent intent = new Intent();
					intent.putExtra(INTENT_NAME_HERO_PATH, path);
					setResult(RESULT_OK, intent);
					finish();
				}
			});
			exchange.importHero();
			break;
		}

	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HeroFileInfo hero = (HeroFileInfo) parent.getItemAtPosition(position);
		Intent intent = new Intent();
		intent.putExtra(INTENT_NAME_HERO_PATH, hero.getFile().toString());
		setResult(RESULT_OK, intent);
		finish();
	}

}
