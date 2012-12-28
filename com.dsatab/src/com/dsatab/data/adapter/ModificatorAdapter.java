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

	private LayoutInflater inflater;

	public ModificatorAdapter(Context context, int textViewResourceId, List<Modificator> objects) {
		super(context, textViewResourceId, objects);
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public ModificatorAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ItemListItem view;

		if (!(convertView instanceof ItemListItem)) {
			view = (ItemListItem) inflater.inflate(R.layout.item_listitem, parent, false);
		} else {
			view = (ItemListItem) convertView;
		}

		Modificator e = getItem(position);

		// Set value for the first text field
		if (view.text1 != null) {
			view.text1.setText(e.getModificatorName());
		}

		// set value for the second text field
		if (view.text2 != null) {
			view.text2.setText(e.getModificatorInfo());
		}

		return view;
	}
}
