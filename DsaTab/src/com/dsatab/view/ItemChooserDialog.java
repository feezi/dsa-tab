package com.dsatab.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.data.adapter.ItemAdapter;
import com.dsatab.data.items.ItemType;
import com.dsatab.xml.DataManager;

public class ItemChooserDialog extends Dialog implements android.view.View.OnClickListener {

	private ItemAdapter itemAdapter = null;

	private ListView itemList;

	private Button btnOtherItems;

	private ItemType itemType = null;

	private boolean showOwnItems = true;

	public ItemChooserDialog(Context context) {
		super(context, R.style.EditDialog);
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (showOwnItems) {
			itemAdapter = new ItemAdapter(getContext(), com.dsatab.R.layout.popup_item_chooser_item, DSATabApplication.getInstance().getHero().getItems(
					itemType));
			btnOtherItems.setVisibility(View.VISIBLE);
		} else {
			itemAdapter = new ItemAdapter(getContext(), com.dsatab.R.layout.popup_item_chooser_item, DataManager.getItemsByType(itemType));
			btnOtherItems.setVisibility(View.GONE);
		}
		itemList.setAdapter(itemAdapter);

	}

	public void show(ItemType type) {
		setItemType(type);
		show();
	}

	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;

	}

	private void init() {

		setTitle("Wähle einen Gegenstand...");

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.popup_item_chooser, null, false);
		addContentView(popupcontent, new LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, (int) (getContext().getResources()
				.getDisplayMetrics().widthPixels * 0.80)));

		itemList = (ListView) popupcontent.findViewById(R.id.popup_item_list);

		btnOtherItems = (Button) findViewById(R.id.popup_item_all);
		btnOtherItems.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (v == btnOtherItems) {
			showOwnItems = false;

			itemAdapter = new ItemAdapter(getContext(), com.dsatab.R.layout.popup_item_chooser_item, DataManager.getItemsByType(itemType));
			itemList.setAdapter(itemAdapter);

			btnOtherItems.setVisibility(View.GONE);
		}

	}

	public AdapterView.OnItemClickListener getOnItemClickListener() {
		return itemList.getOnItemClickListener();
	}

	public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
		itemList.setOnItemClickListener(onItemClickListener);
	}

	public boolean isShowOwnItems() {
		return showOwnItems;
	}

	public void setShowOwnItems(boolean showOwnItems) {
		this.showOwnItems = showOwnItems;
	}

}