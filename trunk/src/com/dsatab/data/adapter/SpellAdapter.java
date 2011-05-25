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
package com.dsatab.data.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.Spell;
import com.dsatab.view.FilterSettings;
import com.gandulf.guilib.util.Debug;

/**
 * 
 *
 */
public class SpellAdapter extends BaseAdapter {

	private OnClickListener onClickListener;

	private Hero hero;

	private List<Spell> spells;

	private Context context;

	private FilterSettings filterSettings;

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public SpellAdapter(Context context, Hero hero, boolean showFavorite, boolean showNormal, boolean showUnused) {
		this.context = context;
		this.filterSettings = new FilterSettings(showFavorite, showNormal, showUnused);
		this.hero = hero;

		this.spells = filter(hero.getSpells());

	}

	public void setFilter(boolean showFavorite, boolean showNormal, boolean showUnused) {

		boolean hasChanged = !filterSettings.equals(showFavorite, showNormal, showUnused);

		filterSettings.set(showFavorite, showNormal, showUnused);

		if (hasChanged) {
			spells = filter(hero.getSpells());
			notifyDataSetChanged();
		}
	}

	public void updateItem(Spell spell) {

		if (spells.contains(spell)) {
			if (spell.isFavorite() && !filterSettings.isShowFavorite())
				spells.remove(spell);

			if (spell.isUnused() && !filterSettings.isShowUnused())
				spells.remove(spell);

			if (!spell.isFavorite() && !spell.isUnused() && !filterSettings.isShowNormal())
				spells.remove(spell);

		} else {

			if (spell.isFavorite() && filterSettings.isShowFavorite())
				spells.add(spell);

			if (spell.isUnused() && filterSettings.isShowUnused())
				spells.add(spell);

			if (!spell.isUnused() && !spell.isFavorite() && filterSettings.isShowNormal())
				spells.add(spell);

			Collections.sort(spells, Spell.NAME_COMPARATOR);
		}
	}

	private List<Spell> filter(List<Spell> in) {

		if (filterSettings.isAllVisible()) {
			return in;
		} else {
			List<Spell> result = new ArrayList<Spell>();

			for (Spell t : in) {
				if (filterSettings.isVisible(t)) {
					result.add(t);
				}
			}

			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return spells.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Spell getItem(int position) {
		return spells.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	public OnClickListener getOnClickListener() {
		return onClickListener;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = (View) LayoutInflater.from(context).inflate(R.layout.spell_row, null);
		}

		TextView name = (TextView) convertView.findViewById(R.id.spell_row_name);
		TextView probe = (TextView) convertView.findViewById(R.id.spell_row_probe);
		TextView value = (TextView) convertView.findViewById(R.id.spell_row_value);

		Spell spell = getItem(position);

		name.setText(spell.getName());
		probe.setText(spell.getProbe());

		if (spell.getValue() != null) {
			value.setText(Integer.toString(spell.getValue()));
		} else {
			Debug.warning(spell.getName() + " has no value");
		}

		if (position % 2 == 1) {
			convertView.setBackgroundResource(R.color.RowOdd);
		} else {
			convertView.setBackgroundResource(R.color.RowEven);
		}

		Util.applyRowStyle(spell, convertView, position);

		return convertView;
	}

}
