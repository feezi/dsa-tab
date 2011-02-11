package com.dsatab.data.adapter;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DistanceCatgoryAdapter extends ArrayAdapter<String> {

	public DistanceCatgoryAdapter(Context context, int textViewResourceId, String[] items) {
		super(context, textViewResourceId, items);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		TextView textView = null;
		if (convertView instanceof TextView) {
			textView = (TextView) convertView;
		}

		if (convertView == null) {
			textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.simple_spinner_dropdown_item, null, false);
			convertView = textView;
		}

		String e = getItem(position);
		textView.setText(e);

		return textView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = null;
		if (convertView instanceof TextView) {
			textView = (TextView) convertView;
		}

		if (convertView == null) {
			textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.simple_spinner_item, null, false);
			convertView = textView;
		}

		String e = getItem(position);
		textView.setText(e);

		return textView;
	}

}
