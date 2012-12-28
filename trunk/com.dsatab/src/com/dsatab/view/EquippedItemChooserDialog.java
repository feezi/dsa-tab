package com.dsatab.view;

import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dsatab.R;
import com.dsatab.data.adapter.EquippedItemAdapter;
import com.dsatab.data.items.EquippedItem;

public class EquippedItemChooserDialog extends AlertDialog implements AdapterView.OnItemClickListener {

	private EquippedItemAdapter itemAdapter = null;

	private ListView itemList;

	private EquippedItem selectedItem = null;

	private List<EquippedItem> equippedItems = Collections.emptyList();

	public EquippedItemChooserDialog(Context context) {
		super(context);
		init();
	}

	public EquippedItemChooserDialog(Context context, int theme) {
		super(context, theme);
		init();

	}

	@Override
	protected void onStart() {
		super.onStart();

		selectedItem = null;

		itemAdapter = new EquippedItemAdapter(getContext(), equippedItems);
		itemList.setAdapter(itemAdapter);
	}

	public List<EquippedItem> getEquippedItems() {
		return equippedItems;
	}

	public void setEquippedItems(List<EquippedItem> equippedItems) {
		this.equippedItems = equippedItems;
	}

	public EquippedItem getSelectedItem() {
		return selectedItem;
	}

	private void init() {
		setTitle("Wähle einen Gegenstand...");

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_equipped_item_chooser, null, false);
		popupcontent.setLayoutParams(new LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		itemList = (ListView) popupcontent.findViewById(R.id.popup_equipped_item_list);
		itemList.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectedItem = itemAdapter.getItem(position);
		EquippedItemChooserDialog.this.dismiss();
	}

}
