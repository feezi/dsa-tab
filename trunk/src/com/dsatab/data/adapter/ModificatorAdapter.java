package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.dsatab.R;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.ItemListItem;

public class ModificatorAdapter extends ArrayAdapter<Modificator> {

	public ModificatorAdapter(Context context, int textViewResourceId, Modificator[] objects) {
		super(context, textViewResourceId, objects);
	}

	public ModificatorAdapter(Context context, int resource, int textViewResourceId, Modificator[] objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public ModificatorAdapter(Context context, int resource, int textViewResourceId, List<Modificator> objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public ModificatorAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);

	}

	public ModificatorAdapter(Context context, int textViewResourceId, List<Modificator> objects) {
		super(context, textViewResourceId, objects);

	}

	public ModificatorAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ItemListItem view;

		if (!(convertView instanceof ItemListItem)) {
			// We need the layoutinflater to pick up the view from xml
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Pick up the TwoLineListItem defined in the xml file
			view = (ItemListItem) inflater.inflate(R.layout.item_listitem, parent, false);
		} else {
			view = (ItemListItem) convertView;
		}

		Modificator e = getItem(position);

		// Set value for the first text field
		if (view.getText1() != null) {
			view.getText1().setText(e.getModificatorName());
		}

		// set value for the second text field
		if (view.getText2() != null) {
			view.getText2().setText(e.getModificatorInfo());
		}

		return view;
	}
}
