package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TwoLineListItem;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Connection;
import com.dsatab.data.enums.EventCategory;

public class ConnectionAdapter extends OpenArrayAdapter<Connection> {

	private ConnectionListFilter filter;

	public ConnectionAdapter(Context context, Connection[] objects) {
		super(context, 0, objects);

	}

	public ConnectionAdapter(Context context, List<Connection> objects) {
		super(context, 0, objects);

	}

	public void filter(String constraint, List<EventCategory> types) {
		getFilter().setTypes(types);
		filter.filter(constraint);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.simple_list_item_2_icon, parent, false);
		} else {
			view = convertView;
		}

		if (view instanceof TwoLineListItem) {
			TwoLineListItem editView = (TwoLineListItem) view;
			Connection e = getItem(position);

			ImageView icon = (ImageView) editView.findViewById(android.R.id.icon1);
			if (icon != null) {
				icon.setImageResource(e.getCategory().getDrawableId());
			}
			editView.getText1().setText(e.getName());
			editView.getText2().setText(e.getDescription());

			Util.applyRowStyle(editView, position);
		}

		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public ConnectionListFilter getFilter() {
		if (filter == null)
			filter = new ConnectionListFilter(this);

		return filter;
	}

}
