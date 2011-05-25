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
package com.dsatab.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Spell;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.SpellAdapter;
import com.dsatab.view.SpellInfoDialog;

/**
 * @author Ganymede
 * 
 */
public class MainSpellActivity extends BaseMainActivity implements OnItemClickListener {

	private static final String PREF_KEY_SHOW_FAVORITE = "SHOW_FAVORITE";
	private static final String PREF_KEY_SHOW_NORMAL = "SHOW_NORMAL";
	private static final String PREF_KEY_SHOW_UNUSED = "SHOW_UNUSED";

	private ListView spellList;

	private SpellAdapter spellAdapter;

	private View spellAttributeList;

	private Map<Value, TextView[]> tfValues = new HashMap<Value, TextView[]>(50);

	private SpellInfoDialog spellInfo;

	/**
	 * 
	 */
	public MainSpellActivity() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_spell);
		super.onCreate(savedInstanceState);

		spellList = (ListView) findViewById(R.id.spell_list);
		registerForContextMenu(spellList);
		spellList.setOnItemClickListener(this);

		spellAttributeList = findViewById(R.id.inc_spell_attributes_list);

	}

	/**
	 * @param probe
	 */
	private void showInfo(Spell probe) {
		if (spellInfo == null) {
			spellInfo = new SpellInfoDialog(this);
		}
		spellInfo.setSpell(probe);
		spellInfo.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		super.onHeroLoaded(hero);

		loadHeroSpells(hero);
		fillAttributesList(spellAttributeList);

	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Konstitution:
			case Charisma:
				fillAttribute(spellAttributeList, attr);
				break;
			}

		}

		TextView[] tvs = tfValues.get(value);
		if (tvs != null) {
			for (TextView tf : tvs) {
				Util.setText(tf, value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Spell spell = spellAdapter.getItem(position);
		if (spell != null) {
			checkProbe(spell);
		}
	}

	private void loadHeroSpells(Hero hero2) {

		SharedPreferences pref = getPreferences(MODE_PRIVATE);

		spellAdapter = new SpellAdapter(this, getHero(), pref.getBoolean(PREF_KEY_SHOW_FAVORITE, true),
				pref.getBoolean(PREF_KEY_SHOW_NORMAL, true), pref.getBoolean(PREF_KEY_SHOW_UNUSED, false));

		spellList.setAdapter(spellAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		if (v == spellList) {

			int position = ((AdapterContextMenuInfo) menuInfo).position;

			if (position >= 0) {
				getMenuInflater().inflate(R.menu.talent_popupmenu, menu);

				Spell spell = spellAdapter.getItem(position);

				menu.setHeaderTitle(spell.getName());
				menu.findItem(R.id.option_unmark).setVisible(spell.isFavorite() || spell.isUnused());
				menu.findItem(R.id.option_mark_favorite).setVisible(!spell.isFavorite());
				menu.findItem(R.id.option_mark_unused).setVisible(!spell.isUnused());
			}
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

		int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

		Spell spell = null;
		if (position >= 0) {
			spell = spellAdapter.getItem(position);

			switch (item.getItemId()) {
			case R.id.option_edit_value:
				showEditPopup(spell);
				return true;
			case R.id.option_mark_favorite:
				spell.setFavorite(true);
				spellAdapter.updateItem(spell);
				spellAdapter.notifyDataSetChanged();
				return true;
			case R.id.option_mark_unused:
				spell.setUnused(true);
				spellAdapter.updateItem(spell);
				spellAdapter.notifyDataSetChanged();
				return true;
			case R.id.option_unmark:
				spell.setFavorite(false);
				spell.setUnused(false);
				spellAdapter.updateItem(spell);
				spellAdapter.notifyDataSetChanged();
				return true;
			case R.id.option_view_details:
				showInfo(spell);
				break;
			}
		}

		return super.onContextItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onCreateOptionsMenu(android.view
	 * .Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.talent_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onOptionsItemSelected(android.view
	 * .MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.option_filter) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle("Zeuberspürche filtern");
			builder.setIcon(android.R.drawable.ic_menu_view);
			View content = getLayoutInflater().inflate(R.layout.popup_filter, null);

			final CheckBox fav = (CheckBox) content.findViewById(R.id.cb_show_favorites);
			final CheckBox normal = (CheckBox) content.findViewById(R.id.cb_show_normal);
			final CheckBox unused = (CheckBox) content.findViewById(R.id.cb_show_unused);

			SharedPreferences pref = getPreferences(MODE_PRIVATE);

			fav.setChecked(pref.getBoolean(PREF_KEY_SHOW_FAVORITE, true));
			normal.setChecked(pref.getBoolean(PREF_KEY_SHOW_NORMAL, true));
			unused.setChecked(pref.getBoolean(PREF_KEY_SHOW_UNUSED, false));

			builder.setView(content);

			DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {

						SharedPreferences pref = getPreferences(MODE_PRIVATE);
						Editor edit = pref.edit();

						edit.putBoolean(PREF_KEY_SHOW_FAVORITE, fav.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_NORMAL, normal.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_UNUSED, unused.isChecked());

						edit.commit();

						spellAdapter.setFilter(fav.isChecked(), normal.isChecked(), unused.isChecked());
					} else if (which == DialogInterface.BUTTON_NEUTRAL) {
						// do nothing
					}

				}
			};

			builder.setPositiveButton(R.string.label_ok, clickListener);
			builder.setNegativeButton(R.string.label_cancel, clickListener);

			builder.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

}
