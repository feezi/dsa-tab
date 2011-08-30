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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.data.HeroInfo;
import com.gandulf.guilib.util.Debug;

/**
 * 
 * 
 */
public class HeroChooserActivity extends Activity implements AdapterView.OnItemClickListener {

	public static final String INTENT_NAME_HERO_PATH = "heroPath";

	private static final int CONTEXTMENU_DELETEITEM = 1;

	private GridView list;
	private HeroAdapter adapter;

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

		list = (GridView) findViewById(R.id.popup_hero_chooser_list);
		adapter = new HeroAdapter(this, R.layout.hero_chooser_item, DSATabApplication.getInstance().getHeroes());
		list.setAdapter(adapter);
		registerForContextMenu(list);
		list.setOnItemClickListener(this);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CONTEXTMENU_DELETEITEM) {
			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			HeroInfo event = (HeroInfo) list.getItemAtPosition(menuInfo.position);

			Debug.verbose("Deleting " + event.getName());
			event.getFile().delete();

			adapter.remove(event);
			adapter.notifyDataSetChanged();

			return true;
		}
		return super.onContextItemSelected(item);
	}

	class HeroAdapter extends ArrayAdapter<HeroInfo> {

		public HeroAdapter(Context context, int textViewResourceId, List<HeroInfo> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewGroup layout = null;
			if (convertView instanceof ViewGroup) {
				layout = (ViewGroup) convertView;
			} else {
				layout = (ViewGroup) getLayoutInflater().inflate(R.layout.hero_chooser_item, null);
			}

			TextView tv = (TextView) layout.findViewById(R.id.textView);
			ImageView iv = (ImageView) layout.findViewById(R.id.imageView);

			HeroInfo hero = getItem(position);
			tv.setText(hero.getName());

			if (hero.getPortraitUri() != null) {
				iv.setImageURI(Uri.parse(hero.getPortraitUri()));
			} else {
				iv.setImageResource(R.drawable.profile_blank);
			}

			return layout;
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HeroInfo hero = (HeroInfo) parent.getItemAtPosition(position);
		Intent intent = new Intent();
		intent.putExtra(INTENT_NAME_HERO_PATH, hero.getFile().toString());
		setResult(RESULT_OK, intent);
		finish();
	}

}
