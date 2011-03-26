package com.dsatab.view;

import java.util.Collections;
import java.util.List;

import android.app.Dialog;
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

public class EquippedItemChooserDialog extends Dialog implements android.view.View.OnClickListener {

	private EquippedItemAdapter itemAdapter = null;

	private ListView itemList;

	private EquippedItem selectedItem = null;

	private List<EquippedItem> equippedItems = Collections.emptyList();

	public EquippedItemChooserDialog(Context context) {
		super(context, R.style.EditDialog);
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

		itemAdapter = new EquippedItemAdapter(getContext(), com.dsatab.R.layout.popup_item_chooser_item, equippedItems);
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
		addContentView(popupcontent, new LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT,
				(int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.80)));

		itemList = (ListView) popupcontent.findViewById(R.id.popup_equipped_item_list);
		itemList.setOnItemClickListener(new ItemChooserListener());

	}

	@Override
	public void onClick(View v) {

	}

	class ItemChooserListener implements AdapterView.OnItemClickListener {

		public ItemChooserListener() {

		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectedItem = itemAdapter.getItem(position);
			EquippedItemChooserDialog.this.dismiss();
		}

	}

}
