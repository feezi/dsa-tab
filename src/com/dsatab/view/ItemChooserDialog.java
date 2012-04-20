package com.dsatab.view;

import java.util.ArrayList;
import java.util.Collection;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
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
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.ItemAdapter;
import com.dsatab.data.adapter.SpinnerSimpleAdapter;
import com.dsatab.data.items.ItemType;
import com.dsatab.xml.DataManager;

public class ItemChooserDialog extends AlertDialog implements android.view.View.OnClickListener, OnItemSelectedListener {

	private ItemAdapter itemAdapter = null;

	private ListView itemList;

	private Button btnOtherItems;

	private Spinner categorySpinner;

	private SpinnerSimpleAdapter<ItemType> categoryAdapter;

	private Collection<ItemType> itemTypes = null;

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
			itemAdapter.filter(itemTypes, null, null);
			btnOtherItems.setVisibility(View.VISIBLE);
			categorySpinner.setVisibility(View.GONE);
		} else {
			itemAdapter = new ItemAdapter(getContext(), DataManager.getItems());
			itemAdapter.filter(itemTypes, null, null);
			btnOtherItems.setVisibility(View.GONE);
			categorySpinner.setVisibility(View.VISIBLE);
		}
		itemList.setAdapter(itemAdapter);

		if (itemTypes != null)
			categorySpinner.setSelection(categoryAdapter.getPosition(itemTypes.iterator().next()));

	}

	public void show(Collection<ItemType> types) {
		setItemTypes(types);
		show();
	}

	public Collection<ItemType> getItemTypes() {
		return itemTypes;
	}

	public void setItemTypes(Collection<ItemType> itemType) {
		this.itemTypes = itemType;

	}

	private void toggleSearch() {
		if (searchText.getVisibility() == View.VISIBLE) {
			closeSearch();
			Util.hideKeyboard(searchText);
		} else
			openSearch();
	}

	private void openSearch() {
		searchText.setText("");
		searchText.setVisibility(View.VISIBLE);
		categorySpinner.setVisibility(View.INVISIBLE);
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
		categorySpinner.setVisibility(View.VISIBLE);
		searchText.setVisibility(View.INVISIBLE);
		searchText.clearFocus();

		searchButton.setSelected(false);

		ItemType type = (ItemType) categorySpinner.getSelectedItem();
		if (type != null)
			itemAdapter.filter(type, null, null);
	}

	private void init() {

		itemTypes = new ArrayList<ItemType>();
		itemTypes.add(ItemType.Waffen);

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
		categoryAdapter = new SpinnerSimpleAdapter<ItemType>(this.getContext(), ItemType.values());
		categorySpinner.setAdapter(categoryAdapter);
		categorySpinner.setSelection(categoryAdapter.getPosition(itemTypes.iterator().next()));
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
				itemAdapter.filter((ItemType) null, null, s.toString());

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
			itemAdapter.filter(itemTypes, null, null);
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