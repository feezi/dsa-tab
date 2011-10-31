package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TwoLineListItem;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Event;
import com.dsatab.data.enums.EventCategory;

public class EventAdapter extends OpenArrayAdapter<Event> {

	private EventListFilter filter;

	public EventAdapter(Context context, Event[] objects) {
		super(context, 0, objects);

	}

	public EventAdapter(Context context, List<Event> objects) {
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
			view = mInflater.inflate(R.layout.event_list_item, parent, false);
		} else {
			view = convertView;
		}

		if (view instanceof TwoLineListItem) {
			TwoLineListItem editView = (TwoLineListItem) view;

			Event e = getItem(position);

			if (e.getCategory() != null) {

				ImageView icon1 = (ImageView) editView.findViewById(android.R.id.icon1);
				if (icon1 != null) {
					icon1.setImageResource(e.getCategory().getDrawableId());
				}

				ImageView icon2 = (ImageView) editView.findViewById(android.R.id.icon2);
				if (icon2 != null) {
					if (e.getAudioPath() != null) {
						icon2.setVisibility(View.VISIBLE);
						icon2.setImageResource(R.drawable.ic_lock_silent_mode_off);
					} else {
						icon2.setVisibility(View.GONE);
					}

				}
			}

			if (e.getCategory().hasName() && !TextUtils.isEmpty(e.getName())) {
				editView.getText1().setText(e.getName());
				editView.getText2().setText(e.getComment());
				editView.getText2().setVisibility(View.VISIBLE);
			} else {
				editView.getText1().setText(e.getComment());
				editView.getText2().setVisibility(View.GONE);
			}

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
	public EventListFilter getFilter() {
		if (filter == null)
			filter = new EventListFilter(this);

		return filter;
	}

}
