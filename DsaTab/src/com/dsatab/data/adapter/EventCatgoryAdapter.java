package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.data.enums.EventCategory;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class EventCatgoryAdapter extends SpinnerSimpleAdapter<EventCategory> {

	public EventCatgoryAdapter(Context context, int textViewResourceId, EventCategory[] objects) {
		super(context, textViewResourceId, objects);

	}

	public EventCatgoryAdapter(Context context, int resource, int textViewResourceId, EventCategory[] objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public EventCatgoryAdapter(Context context, int resource, int textViewResourceId, List<EventCategory> objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public EventCatgoryAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);

	}

	public EventCatgoryAdapter(Context context, int textViewResourceId, List<EventCategory> objects) {
		super(context, textViewResourceId, objects);

	}

	public EventCatgoryAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);

	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = super.getDropDownView(position, convertView, parent);

		if (view instanceof TextView) {
			TextView textView = (TextView) view;
			EventCategory e = getItem(position);

			textView.setCompoundDrawablesWithIntrinsicBounds(e.getDrawableId(), 0, 0, 0);
			textView.setCompoundDrawablePadding(getContext().getResources()
					.getDimensionPixelSize(R.dimen.dices_padding));
			textView.setText(e.name());
		}

		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		if (view instanceof TextView) {
			TextView textView = (TextView) view;
			EventCategory e = getItem(position);

			textView.setCompoundDrawablesWithIntrinsicBounds(e.getDrawableId(), 0, 0, 0);
			textView.setCompoundDrawablePadding(getContext().getResources()
					.getDimensionPixelSize(R.dimen.dices_padding));
			textView.setText(e.name());
		}

		return view;
	}

}
