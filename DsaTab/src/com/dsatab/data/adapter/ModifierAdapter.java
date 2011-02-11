package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TwoLineListItem;

import com.dsatab.data.modifier.Modificator;

public class ModifierAdapter extends ArrayAdapter<Modificator> {

	public ModifierAdapter(Context context, int textViewResourceId, Modificator[] objects) {
		super(context, textViewResourceId, objects);
	}

	public ModifierAdapter(Context context, int resource, int textViewResourceId, Modificator[] objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public ModifierAdapter(Context context, int resource, int textViewResourceId, List<Modificator> objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public ModifierAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);

	}

	public ModifierAdapter(Context context, int textViewResourceId, List<Modificator> objects) {
		super(context, textViewResourceId, objects);

	}

	public ModifierAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TwoLineListItem view;

		if (!(convertView instanceof TwoLineListItem)) {
			// We need the layoutinflater to pick up the view from xml
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Pick up the TwoLineListItem defined in the xml file
			view = (TwoLineListItem) inflater.inflate(com.dsatab.R.layout.popup_item_chooser_item, parent, false);
		} else {
			view = (TwoLineListItem) convertView;
		}

		Modificator e = getItem(position);

		// Set value for the first text field
		if (view.getText1() != null) {
			view.getText1().setText(e.getModifierName());
		}

		// set value for the second text field
		if (view.getText2() != null) {
			view.getText2().setText(e.getModifierInfo());
		}

		return view;
	}
}
