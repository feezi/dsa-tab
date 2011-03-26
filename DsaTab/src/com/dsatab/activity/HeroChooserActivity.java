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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;

/**
 * @author Ganymede
 * 
 */
public class HeroChooserActivity extends Activity implements AdapterView.OnItemClickListener {

	public static final String INTENT_NAME_HERO_PATH = "heroPath";

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
		setContentView(R.layout.hero_chooser);

		final GridView list = (GridView) findViewById(R.id.popup_hero_chooser_list);
		HeroAdapter adapter = new HeroAdapter(this, R.layout.hero_chooser_item, DSATabApplication.getInstance()
				.getHeroes());
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	class HeroAdapter extends ArrayAdapter<String> {

		private Map<String, WeakReference<Drawable>> imageCache = new HashMap<String, WeakReference<Drawable>>();

		private SharedPreferences preferences = null;

		public HeroAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);

			preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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

			String name = getItem(position);
			tv.setText(name.substring(0, name.lastIndexOf(".")));
			layout.setTag(name);

			String profileName = preferences.getString(DSATabApplication.getDsaTabPath() + name, null);

			if (profileName != null) {
				Drawable drawable = null;

				if (imageCache.containsKey(profileName))
					drawable = imageCache.get(profileName).get();

				if (drawable == null) {
					drawable = Drawable.createFromPath(DSATabApplication.getDsaTabPath() + "portraits/" + profileName);

					imageCache.put(profileName, new WeakReference<Drawable>(drawable));
				}

				iv.setImageDrawable(drawable);
				iv.invalidate();
			} else {
				iv.setImageResource(R.drawable.profile_blank);
			}

			return layout;
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String heroName = view.getTag().toString();
		Intent intent = new Intent();
		intent.putExtra(INTENT_NAME_HERO_PATH, DSATabApplication.getDsaTabPath() + heroName);
		setResult(RESULT_OK, intent);
		finish();
	}

}
