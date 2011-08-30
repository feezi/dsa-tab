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
import com.dsatab.data.Liturgie;
import com.dsatab.view.FilterSettings;

/**
 *
 */
public class LiturigeAdapter extends OpenArrayAdapter<Liturgie> {

	private OnClickListener onClickListener;

	private FilterSettings filterSettings;

	private FilterableListFilter<Liturgie> filter;

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public LiturigeAdapter(Context context, List<Liturgie> liturgies, boolean showFavorite, boolean showNormal,
			boolean showUnused) {
		super(context, 0, 0, liturgies);

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
	public FilterableListFilter<Liturgie> getFilter() {
		if (filter == null)
			filter = new FilterableListFilter<Liturgie>(this);

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

		Liturgie liturige = getItem(position);

		text1.setText(liturige.getFullName());
		text2.setText(liturige.getTarget() + "," + liturige.getRange() + "," + liturige.getCastDuration());
		text3.setText(liturige.getEffectDuration());
		text4.setText(Util.toProbe(liturige.getErschwernis()));
		Util.setVisibility(text5, false, text1);

		Util.applyRowStyle(liturige, listItem, position);

		return listItem;
	}

}
