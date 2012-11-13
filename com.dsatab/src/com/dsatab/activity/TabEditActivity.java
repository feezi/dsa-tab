/**
 *  This file is part of Risk.
 *
 *  Risk is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Risk is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Risk.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.TabInfo;
import com.dsatab.common.Util;
import com.dsatab.data.adapter.SpinnerSimpleAdapter;
import com.dsatab.fragment.ArtFragment;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.BodyFragment;
import com.dsatab.fragment.CharacterFragment;
import com.dsatab.fragment.DocumentsFragment;
import com.dsatab.fragment.FightFragment;
import com.dsatab.fragment.ItemsFragment;
import com.dsatab.fragment.ItemsListFragment;
import com.dsatab.fragment.MapFragment;
import com.dsatab.fragment.NotesFragment;
import com.dsatab.fragment.PurseFragment;
import com.dsatab.fragment.SpellFragment;
import com.dsatab.fragment.TalentFragment;
import com.dsatab.view.GlossyImageButton;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class TabEditActivity extends BaseFragmentActivity implements OnItemClickListener, OnClickListener,
		OnItemSelectedListener, DropListener, RemoveListener, OnCheckedChangeListener {

	private Spinner spinner1, spinner2, iconSpinner;
	private CheckBox diceslider;

	private List<String> activities;
	private List<Class<? extends BaseFragment>> activityValues;
	private int selectedPosition = 0;

	private TabInfo currentInfo = null;

	private DragSortListView tabsList;
	private TabsAdapter tabsAdapter;
	private TabIconAdapter iconAdapter;

	private List<TabInfo> tabs;

	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(DSATabApplication.getInstance().getCustomTheme());
		applyPreferencesToTheme();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_edit_tab);

		if (DSATabApplication.getInstance().getHero() == null) {
			Toast.makeText(this, "Tabs können erst editiert werden, wenn ein Held geladen wurde.", Toast.LENGTH_SHORT)
					.show();
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		List<Integer> avatars = DSATabApplication.getInstance().getConfiguration().getTabIcons();

		diceslider = (CheckBox) findViewById(R.id.popup_edit_diceslider);
		diceslider.setOnCheckedChangeListener(this);

		spinner1 = (Spinner) findViewById(R.id.popup_edit_primary);

		tabsList = (DragSortListView) findViewById(R.id.popup_tab_list);
		tabsList.setDropListener(this);
		tabsList.setRemoveListener(this);
		tabsList.setOnItemClickListener(this);
		tabsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// tabsList.setItemHeightExpanded(tabsList.getItemHeightNormal() * 2);

		tabs = new ArrayList<TabInfo>(DSATabApplication.getInstance().getHero().getHeroConfiguration().getTabs());

		tabsAdapter = new TabsAdapter(this, tabs);
		tabsList.setAdapter(tabsAdapter);

		activities = Arrays.asList("Keine", "Charakterbogen", "Talente", "Zaubersprüche", "Künste", "Wunden",
				"Kampfschirm", "Ausrüstung (Bilder)", "Ausrüstung (Liste)", "Notizen", "Geldbörse", "Karte",
				"Dokumente");

		activityValues = Arrays.asList(null, CharacterFragment.class, TalentFragment.class, SpellFragment.class,
				ArtFragment.class, BodyFragment.class, FightFragment.class, ItemsFragment.class,
				ItemsListFragment.class, NotesFragment.class, PurseFragment.class, MapFragment.class,
				DocumentsFragment.class);

		SpinnerSimpleAdapter<String> adapter = new SpinnerSimpleAdapter<String>(this, activities);
		spinner1.setAdapter(adapter);
		spinner1.setOnItemSelectedListener(this);

		spinner2 = (Spinner) findViewById(R.id.popup_edit_secondary);
		spinner2.setAdapter(adapter);
		spinner2.setOnItemSelectedListener(this);

		iconSpinner = (Spinner) findViewById(R.id.popup_edit_icon);
		iconAdapter = new TabIconAdapter(this, avatars);
		iconSpinner.setAdapter(iconAdapter);
		iconSpinner.setOnItemSelectedListener(this);

		Button ok = (Button) findViewById(R.id.popup_edit_ok);
		ok.setOnClickListener(this);
		Button cancel = (Button) findViewById(R.id.popup_edit_cancel);
		cancel.setOnClickListener(this);

		updateView(null);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		com.actionbarsherlock.view.MenuItem item = menu
				.add(Menu.NONE, R.id.option_tab_add, Menu.NONE, "Tab hinzufügen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		item.setIcon(R.drawable.ic_menu_add);

		item = menu.add(Menu.NONE, R.id.option_tab_delete, Menu.NONE, "Tab entfernen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		item.setIcon(R.drawable.ic_menu_delete);

		item = menu.add(Menu.NONE, R.id.option_tab_reset, Menu.NONE, "Tabs zurücksetzen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		item.setIcon(R.drawable.ic_menu_revert);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragmentActivity#onPrepareOptionsMenu
	 * (com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.option_tab_delete);
		if (item != null) {
			item.setEnabled(currentInfo != null);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.option_tab_add:
			TabInfo info = new TabInfo();
			getTabs().add(info);
			tabsAdapter.notifyDataSetChanged();
			updateView(info);
			break;
		case R.id.option_tab_delete:
			getTabs().remove(currentInfo);
			updateView(null);
			tabsAdapter.notifyDataSetChanged();
			break;
		case R.id.option_tab_reset:
			tabs = DSATabApplication.getInstance().getHero().getHeroConfiguration().getDefaultTabs();
			tabsAdapter = new TabsAdapter(this, tabs);
			tabsList.setAdapter(tabsAdapter);
			updateView(null);
			break;
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
		}

		return false;
	}

	protected void updateView(TabInfo info) {

		currentInfo = info;
		tabsAdapter.setSelectedTab(info);

		if (info != null) {
			Class<? extends BaseFragment> clazz1 = info.getPrimaryActivityClazz();

			spinner1.setSelection(activityValues.indexOf(clazz1));

			Class<? extends BaseFragment> clazz2 = info.getSecondaryActivityClazz();
			spinner2.setSelection(activityValues.indexOf(clazz2));

			diceslider.setChecked(info.isDiceSlider());

			setSelectedTabIcon(info.getTabResourceIndex());
		}

		spinner1.setEnabled(info != null);
		spinner2.setEnabled(info != null);
		diceslider.setEnabled(info != null);
		iconSpinner.setEnabled(info != null);

		invalidateOptionsMenu();
	}

	protected List<TabInfo> getTabs() {
		return tabs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.commonsware.cwac.tlv.TouchListView.DropListener#drop(int, int)
	 */
	@Override
	public void drop(int from, int to) {

		TabInfo tab = getTabs().remove(from);

		getTabs().add(to, tab);

		tabsAdapter.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.commonsware.cwac.tlv.TouchListView.RemoveListener#remove(int)
	 */
	@Override
	public void remove(int which) {
		getTabs().remove(which);
		tabsAdapter.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.popup_edit_ok:
			setResult(RESULT_OK);
			Util.hideKeyboard(v);
			DSATabApplication.getInstance().getHero().getHeroConfiguration().setTabs(tabs);
			finish();

			Editor edit = DSATabApplication.getPreferences().edit();
			edit.putString(BasePreferenceActivity.KEY_MODIFY_TABS, "" + System.currentTimeMillis());
			edit.commit();
			break;
		case R.id.popup_edit_cancel:
			setResult(RESULT_CANCELED);
			Util.hideKeyboard(v);
			finish();
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged
	 * (android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (currentInfo != null) {
			currentInfo.setDiceSlider(isChecked);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android
	 * .widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
		if (currentInfo != null) {
			if (adapter == spinner1) {
				Class<? extends BaseFragment> clazz1 = activityValues.get(spinner1.getSelectedItemPosition());
				currentInfo.setPrimaryActivityClazz(clazz1);
			} else if (adapter == spinner2) {
				Class<? extends BaseFragment> clazz2 = activityValues.get(spinner2.getSelectedItemPosition());
				currentInfo.setSecondaryActivityClazz(clazz2);
			} else if (adapter == iconSpinner) {
				currentInfo.setTabResourceIndex(position);
			}
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
	public void onNothingSelected(AdapterView<?> adapter) {
		if (currentInfo != null) {
			if (adapter == spinner1) {
				currentInfo.setPrimaryActivityClazz(null);
			} else if (adapter == spinner2) {
				currentInfo.setSecondaryActivityClazz(null);
			} else if (adapter == iconSpinner) {
				// currentInfo.setTabResourceIndex(0);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (parent == tabsList) {
			TabInfo info = tabsAdapter.getItem(position);

			tabsAdapter.notifyDataSetChanged();
			updateView(info);
			tabsList.setItemChecked(position, true);
		}

	}

	protected void setSelectedTabIcon(int position) {
		selectedPosition = position;
		iconAdapter.setSelectedIndex(position);
		// check new position
		iconSpinner.setSelection(position);

		if (currentInfo != null) {
			currentInfo.setTabResourceIndex(selectedPosition);
			tabsAdapter.notifyDataSetChanged();
		}
	}

	static class TabIconAdapter extends ArrayAdapter<Integer> {

		LayoutInflater inflater;

		private int selectedIndex;

		public TabIconAdapter(Context context, List<Integer> objects) {
			super(context, 0, objects);

			inflater = LayoutInflater.from(getContext());
		}

		public int getSelectedIndex() {
			return selectedIndex;
		}

		public void setSelectedIndex(int selectedIndex) {
			this.selectedIndex = selectedIndex;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getDropDownView(int,
		 * android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LinearLayout view;
			if (convertView instanceof LinearLayout) {
				view = (LinearLayout) convertView;
			} else {
				view = (LinearLayout) inflater.inflate(R.layout.item_tab, parent, false);
			}

			GlossyImageButton imageButton = (GlossyImageButton) view.findViewById(R.id.gen_tab);

			imageButton.setFocusable(false);
			imageButton.setClickable(false);
			imageButton.setImageResource(getItem(position));
			imageButton.setChecked(selectedIndex >= 0 && selectedIndex == position);

			return view;
		}

	}

	static class TabsAdapter extends ArrayAdapter<TabInfo> {

		LayoutInflater inflater;

		TabInfo selectedTab;

		/**
		 * 
		 */
		public TabsAdapter(Context context, List<TabInfo> objects) {
			super(context, 0, objects);

			inflater = LayoutInflater.from(getContext());
		}

		public TabInfo getSelectedTab() {
			return selectedTab;
		}

		public void setSelectedTab(TabInfo selectedTab) {
			this.selectedTab = selectedTab;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout view;

			if (convertView instanceof LinearLayout) {
				view = (LinearLayout) convertView;
			} else {
				view = (LinearLayout) inflater.inflate(R.layout.item_drag_tab, parent, false);
			}

			GlossyImageButton imageButton = (GlossyImageButton) view.findViewById(R.id.gen_tab);
			TabInfo info = getItem(position);

			imageButton.setFocusable(false);
			imageButton.setClickable(false);
			imageButton.setImageResource(info.getTabResourceId());
			imageButton.setChecked(info == selectedTab);

			return view;
		}
	}

}