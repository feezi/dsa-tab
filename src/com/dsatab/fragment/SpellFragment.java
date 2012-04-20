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
package com.dsatab.fragment;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.dsatab.R;
import com.dsatab.activity.MainActivity;
import com.dsatab.data.Hero;
import com.dsatab.data.Spell;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.SpellAdapter;
import com.dsatab.view.FilterDialog;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;
import com.dsatab.view.ListFilterSettings;
import com.dsatab.view.SpellInfoDialog;
import com.dsatab.view.listener.HeroChangedListener;

/**
 * 
 * 
 */
public class SpellFragment extends BaseFragment implements OnItemClickListener, HeroChangedListener {

	private ListView spellList;

	private SpellAdapter spellAdapter;

	private SpellInfoDialog spellInfo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = configureContainerView(inflater.inflate(R.layout.sheet_spell, container, false));

		spellList = (ListView) root.findViewById(R.id.spell_list);

		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		registerForContextMenu(spellList);
		spellList.setOnItemClickListener(this);

		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * @param probe
	 */
	private void showInfo(Spell probe) {
		if (spellInfo == null) {
			spellInfo = new SpellInfoDialog(getBaseActivity());
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
	public void onHeroLoaded(Hero hero) {
		loadHeroSpells(hero);

		if (hero.getSpells().isEmpty()) {
			findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
			spellList.setVisibility(View.GONE);
		} else {
			findViewById(android.R.id.empty).setVisibility(View.GONE);
			spellList.setVisibility(View.VISIBLE);
		}
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Spell) {
			spellAdapter.notifyDataSetChanged();
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
			getBaseActivity().checkProbe(spell);
		}
	}

	private void loadHeroSpells(Hero hero2) {

		ListFilterSettings filterSettings = new ListFilterSettings(preferences.getBoolean(
				FilterDialog.PREF_KEY_SPELL_FAVORITE, true), preferences.getBoolean(FilterDialog.PREF_KEY_SPELL_NORMAL,
				true), preferences.getBoolean(FilterDialog.PREF_KEY_SPELL_UNUSED, false), preferences.getBoolean(
				FilterDialog.PREF_KEY_SPELL_MODIFIERS, true));

		spellAdapter = new SpellAdapter(getBaseActivity(), getHero(), getHero().getSpells(), filterSettings);

		spellList.setAdapter(spellAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (menu.findItem(R.id.option_filter) == null) {
			com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_filter, Menu.NONE, "Filtern");
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setIcon(R.drawable.ic_menu_filter);
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

		if (v == spellList) {

			int position = ((AdapterContextMenuInfo) menuInfo).position;

			if (position >= 0) {
				MenuInflater inflater = new MenuInflater(getActivity());
				inflater.inflate(R.menu.spell_popupmenu, menu);

				Spell spell = spellAdapter.getItem(position);
				menu.setHeaderTitle(spell.getName());
				menu.findItem(R.id.option_unmark_spell).setVisible(spell.isFavorite() || spell.isUnused());
				menu.findItem(R.id.option_mark_favorite_spell).setVisible(!spell.isFavorite());
				menu.findItem(R.id.option_mark_unused_spell).setVisible(!spell.isUnused());
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

		if (item.getGroupId() == R.id.group_spell && item.getMenuInfo() instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo menuInfo = ((AdapterContextMenuInfo) item.getMenuInfo());
			int position = menuInfo.position;

			View child = menuInfo.targetView;

			switch (item.getItemId()) {
			case R.id.option_edit_spell: {
				MainActivity.showEditPopup(getActivity(), getSpell(child, position));
				return true;
			}
			case R.id.option_mark_favorite_spell: {
				Spell spell = getSpell(child, position);
				spell.setFavorite(true);
				spellAdapter.notifyDataSetChanged();

				return true;
			}
			case R.id.option_mark_unused_spell: {
				Spell spell = getSpell(child, position);
				spell.setUnused(true);
				spellAdapter.notifyDataSetChanged();
				return true;
			}
			case R.id.option_unmark_spell: {
				Spell spell = getSpell(child, position);
				spell.setFavorite(false);
				spell.setUnused(false);
				spellAdapter.notifyDataSetChanged();
				return true;
			}
			case R.id.option_view_spell: {
				showInfo(getSpell(child, position));
				return true;
			}
			}

		}

		return super.onContextItemSelected(item);
	}

	private Spell getSpell(View child, int position) {
		Spell spell = null;
		if (child != null && child.getTag() instanceof Spell) {
			spell = (Spell) child.getTag();
		}

		if (spellAdapter != null && spell == null && position >= 0 && position < spellAdapter.getCount()) {
			spell = spellAdapter.getItem(position);
		}
		return spell;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onFilterChanged(com.dsatab.view.
	 * FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {
		if (type == FilterType.Spell && settings instanceof ListFilterSettings) {
			spellAdapter.filter((ListFilterSettings) settings);
		}
	}

}
