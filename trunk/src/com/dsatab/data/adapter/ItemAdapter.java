package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dsatab.R;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.ItemListItem;

public class ItemAdapter extends OpenArrayAdapter<Item> {

	private ItemListFilter filter;

	public ItemAdapter(Context context, Item[] objects) {
		super(context, 0, objects);
	}

	public ItemAdapter(Context context, List<Item> objects) {
		super(context, 0, objects);
	}

	public void filter(ItemType type, String category, String constraint) {
		getFilter().setType(type);
		filter.setCategory(category);
		filter.filter(constraint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public ItemListFilter getFilter() {
		if (filter == null)
			filter = new ItemListFilter(this);

		return filter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// View view = super.getView(position, convertView, parent);

		ItemListItem view;
		if (!(convertView instanceof ItemListItem)) {
			// We need the layoutinflater to pick up the view from xml
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Pick up the TwoLineListItem defined in the xml file
			view = (ItemListItem) inflater.inflate(R.layout.item_listitem, parent, false);
		} else {
			view = (ItemListItem) convertView;
		}

		view.setItem(getItem(position));

		return view;
	}

}
