package com.dsatab.view;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.ItemAdapter;
import com.dsatab.data.items.ItemType;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class ItemChooserDialog extends AlertDialog implements android.view.View.OnClickListener, OnItemSelectedListener {

	private ItemAdapter itemAdapter = null;

	private ListView itemList;

	private Button btnOtherItems;

	private Spinner categorySpinner;

	private SpinnerSimpleAdapter<ItemType> categoryAdapter;

	private ItemType itemType = null;

	private boolean showOwnItems = true;

	private ImageButton searchButton;
	private EditText searchText;

	private Hero hero;

	public ItemChooserDialog(Context context, Hero hero) {
		super(context);
		this.hero = hero;
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (showOwnItems) {
			itemAdapter = new ItemAdapter(getContext(), hero.getItems());
			itemAdapter.filter(itemType, null, null);
			btnOtherItems.setVisibility(View.VISIBLE);
			categorySpinner.setVisibility(View.GONE);
		} else {
			itemAdapter = new ItemAdapter(getContext(), DataManager.getItems());
			itemAdapter.filter(itemType, null, null);
			btnOtherItems.setVisibility(View.GONE);
			categorySpinner.setVisibility(View.VISIBLE);
		}
		itemList.setAdapter(itemAdapter);

		if (itemType != null)
			categorySpinner.setSelection(categoryAdapter.getPosition(itemType));

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

	private void toggleSearch() {
		if (searchText.getVisibility() == View.VISIBLE)
			closeSearch();
		else
			openSearch();
	}

	private void openSearch() {
		searchText.setText("");
		searchText.setVisibility(View.VISIBLE);
		searchText.requestFocus();

		searchButton.setSelected(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onSearchRequested()
	 */
	@Override
	public boolean onSearchRequested() {
		toggleSearch();
		return false;
	}

	private void closeSearch() {
		searchText.setVisibility(View.INVISIBLE);
		searchText.clearFocus();

		searchButton.setSelected(false);

		ItemType type = (ItemType) categorySpinner.getSelectedItem();
		if (type != null)
			itemAdapter.filter(type, null, null);
	}

	private void init() {

		itemType = ItemType.Waffen;

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setTitle("Wähle einen Gegenstand...");

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_item_chooser, null, false);

		popupcontent.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		setView(popupcontent);

		itemList = (ListView) popupcontent.findViewById(R.id.popup_item_list);

		btnOtherItems = (Button) popupcontent.findViewById(R.id.popup_item_all);
		btnOtherItems.setOnClickListener(this);

		categorySpinner = (Spinner) popupcontent.findViewById(R.id.popup_item_category);
		categoryAdapter = new SpinnerSimpleAdapter<ItemType>(this.getContext(), 0, ItemType.values());
		categorySpinner.setAdapter(categoryAdapter);
		categorySpinner.setSelection(categoryAdapter.getPosition(itemType));
		categorySpinner.setOnItemSelectedListener(this);

		searchButton = (ImageButton) popupcontent.findViewById(R.id.popup_search_button);
		searchButton.setOnClickListener(this);

		searchText = (EditText) popupcontent.findViewById(R.id.popup_autosearch);

		searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					closeSearch();
				}
			}
		});
		searchText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				itemAdapter.filter(null, null, s.toString());

			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android
	 * .widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.popup_item_category) {
			ItemType type = (ItemType) categorySpinner.getItemAtPosition(position);
			itemAdapter.filter(type, null, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android
	 * .widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.popup_search_button:
			toggleSearch();
			break;
		case R.id.popup_item_all:

			showOwnItems = false;

			itemAdapter = new ItemAdapter(getContext(), DataManager.getItems());
			itemAdapter.filter(itemType, null, null);
			itemList.setAdapter(itemAdapter);

			btnOtherItems.setVisibility(View.GONE);
			categorySpinner.setVisibility(View.VISIBLE);
			break;
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