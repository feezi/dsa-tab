package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.dsatab.data.items.Item;
import com.dsatab.view.ItemListItem;
import com.dsatab.R;

public class ItemAdapter extends ArrayAdapter<Item> {

	public ItemAdapter(Context context, int textViewResourceId, Item[] objects) {
		super(context, textViewResourceId, objects);
	}

	public ItemAdapter(Context context, int resource, int textViewResourceId, Item[] objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public ItemAdapter(Context context, int resource, int textViewResourceId, List<Item> objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public ItemAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);

	}

	public ItemAdapter(Context context, int textViewResourceId, List<Item> objects) {
		super(context, textViewResourceId, objects);

	}

	public ItemAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);

	}

	// @Override
	// public View getDropDownView(int position, View convertView, ViewGroup
	// parent) {
	// View view = super.getDropDownView(position, convertView, parent);
	//
	// if (view instanceof TwoLineListItem) {
	// TwoLineListItem view = (TwoLineListItem) view;
	//
	// TextView text1 = view.getText1();
	// TextView text2 = view.getText2();
	//
	// Item e = getItem(position);
	//
	// //
	// textView.setCompoundDrawablePadding(getContext().getResources().getDimensionPixelSize(R.dimen.dices_padding));
	// text1.setText(e.getName());
	// }
	//
	// return view;
	// }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// View view = super.getView(position, convertView, parent);

		ItemListItem view;
		if (!(convertView instanceof ItemListItem)) {
			// We need the layoutinflater to pick up the view from xml
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Pick up the TwoLineListItem defined in the xml file
			view = (ItemListItem) inflater.inflate(R.layout.popup_item_chooser_item, parent, false);
		} else {
			view = (ItemListItem) convertView;
		}

		view.setItem(getItem(position));

		return view;
	}
}
