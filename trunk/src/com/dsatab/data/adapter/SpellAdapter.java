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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
public class SpellAdapter extends OpenArrayAdapter<Spell> {

	private OnClickListener onClickListener;

	private FilterableListFilter<Spell> filter;

	private FilterSettings filterSettings;

	private Hero hero;

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public SpellAdapter(Context context, Hero hero, List<Spell> spells, boolean showFavorite, boolean showNormal,
			boolean showUnused) {
		super(context, 0, 0, spells);

		this.hero = hero;

		this.filterSettings = new FilterSettings(showFavorite, showNormal, showUnused);
		if (!filterSettings.isAllVisible())
			filter(filterSettings, null);
	}

	public void filter(FilterSettings settings, String constraint) {
		getFilter().setSettings(settings);
		filter.filter(constraint);
	}

	public void filter(boolean showFavorite, boolean showNormal, boolean showUnused) {

		boolean hasChanged = !filterSettings.equals(showFavorite, showNormal, showUnused);

		filterSettings.set(showFavorite, showNormal, showUnused);

		if (hasChanged) {
			filter(filterSettings, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public FilterableListFilter<Spell> getFilter() {
		if (filter == null)
			filter = new FilterableListFilter<Spell>(this);

		return filter;
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

		View listItem = null;

		if (convertView == null) {
			listItem = LayoutInflater.from(getContext()).inflate(R.layout.talent_list_item, null, false);
		} else {
			listItem = convertView;
		}

		// name
		TextView text1 = (TextView) listItem.findViewById(R.id.talent_list_item_text1);
		// be
		TextView text2 = (TextView) listItem.findViewById(R.id.talent_list_item_text2);
		// probe
		TextView text3 = (TextView) listItem.findViewById(R.id.talent_list_item_text3);
		// value / at
		TextView text4 = (TextView) listItem.findViewById(R.id.talent_list_item_text4);
		// pa
		TextView text5 = (TextView) listItem.findViewById(R.id.talent_list_item_text5);

		Spell spell = getItem(position);

		text1.setText(spell.getName());
		Util.setVisibility(text2, false, text1);
		text3.setText(spell.getProbe());

		if (spell.getValue() != null) {
			int modifier = hero.getModificator(spell);
			Util.setText(text4, spell.getValue(), modifier, null);
		} else {
			Debug.warning(spell.getName() + " has no value");
		}
		Util.setVisibility(text5, false, text1);

		Util.applyRowStyle(spell, listItem, position);

		return listItem;
	}

}
