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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Art;
import com.dsatab.view.ListFilterSettings;

/**
 *
 */
public class ArtAdapter extends OpenArrayAdapter<Art> {

	private OnClickListener onClickListener;

	private ListFilterSettings filterSettings;

	private FilterableListFilter<Art> filter;

	private LayoutInflater inflater;

	private Bitmap indicatorStar;

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public ArtAdapter(Context context, List<Art> liturgies, boolean showFavorite, boolean showNormal, boolean showUnused) {
		this(context, liturgies, new ListFilterSettings(showFavorite, showNormal, showUnused));
	}

	public ArtAdapter(Context context, List<Art> liturgies, ListFilterSettings filterSettings) {
		super(context, 0, 0, liturgies);

		this.filterSettings = filterSettings;
		if (!filterSettings.isAllVisible())
			filter(filterSettings);

		inflater = LayoutInflater.from(getContext());
		indicatorStar = BitmapFactory.decodeResource(context.getResources(), R.drawable.indicator_star);
	}

	public void filter(ListFilterSettings settings) {

		boolean hasChanged = !filterSettings.equals(settings);

		if (hasChanged) {
			filterSettings.set(settings);
			getFilter().setSettings(filterSettings);
			filter.filter((String) null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public FilterableListFilter<Art> getFilter() {
		if (filter == null)
			filter = new FilterableListFilter<Art>(this);

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

		View listItem;
		ViewHolder holder;
		if (convertView == null) {
			listItem = inflater.inflate(R.layout.talent_list_item, parent, false);

			holder = new ViewHolder();
			holder.text1 = (TextView) listItem.findViewById(R.id.talent_list_item_text1);
			// be
			holder.text2 = (TextView) listItem.findViewById(R.id.talent_list_item_text2);
			// probe
			holder.text3 = (TextView) listItem.findViewById(R.id.talent_list_item_text3);
			// value / at
			holder.text4 = (TextView) listItem.findViewById(R.id.talent_list_item_text4);
			// pa
			holder.text5 = (TextView) listItem.findViewById(R.id.talent_list_item_text5);
			holder.indicator = (ImageView) listItem.findViewById(R.id.talent_list_item_indicator);
			listItem.setTag(holder);
		} else {
			listItem = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		Art art = getItem(position);

		holder.text1.setText(art.getFullName());

		StringBuilder info = new StringBuilder();
		if (!TextUtils.isEmpty(art.getTarget())) {
			info.append(art.getTarget());
		}
		if (!TextUtils.isEmpty(art.getRange())) {
			info.append(",");
			info.append(art.getRange());
		}
		if (!TextUtils.isEmpty(art.getCastDuration())) {
			info.append(",");
			info.append(art.getCastDuration());
		}

		holder.text2.setText(info);

		if (art.hasCustomProbe()) {
			holder.text3.setText(art.getProbeInfo().getAttributesString());
		} else {
			if (!TextUtils.isEmpty(art.getEffectDuration()))
				holder.text3.setText(art.getEffectDuration());
			else
				holder.text3.setText(null);
		}

		holder.text4.setText(Util.toProbe(art.getProbeInfo().getErschwernis()));
		Util.setVisibility(holder.text5, false, holder.text1);

		if (holder.indicator != null && art.isBegabung()) {
			holder.indicator.setVisibility(View.VISIBLE);
			holder.indicator.setImageBitmap(indicatorStar);
		}
		Util.applyRowStyle(art, listItem, position);

		return listItem;
	}

	private static class ViewHolder {
		TextView text1, text2, text3, text4, text5;
		ImageView indicator;
	}

}
