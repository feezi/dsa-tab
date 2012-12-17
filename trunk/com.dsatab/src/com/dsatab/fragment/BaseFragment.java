/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.fragment;

import java.util.List;

import yuku.androidsdk.com.android.internal.view.menu.MenuBuilder;
import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;
import yuku.iconcontextmenu.IconContextMenu.IconContextMenuInfo;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.actionbarsherlock.app.SherlockFragment;
import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.activity.MainActivity;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;
import com.dsatab.view.listener.HeroChangedListener;

/**
 * @author Ganymede
 * 
 */
public abstract class BaseFragment extends SherlockFragment implements HeroChangedListener,
		IconContextItemSelectedListener, FilterChangedListener, OnSharedPreferenceChangeListener {

	private static final String FILTER_SETTINGS = "FILTER_SETTINGS";

	protected SharedPreferences preferences;

	protected FilterSettings filterSettings;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// Debug.verbose(getClass().getName() + " attached");

		// if we reattach a fragment there is already a view present
		if (getView() != null) {
			Hero hero = getHero();
			if (hero != null) {
				loadHero(hero);
			}
		}
		super.onAttach(activity);
	}

	/**
	 * special method that is called when this fragment is actually shown on
	 * screen and not only preloaded in the ViewPager
	 */
	public void onShown() {

	}

	protected void onAttachListener(Hero hero) {
		if (hero != null) {
			hero.addHeroChangedListener(this);
		}
	}

	protected void onDetachListener(Hero hero) {
		if (hero != null) {
			hero.removeHeroChangedListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(FILTER_SETTINGS, filterSettings);
	}

	public void setTabInfo(FilterSettings filterSettings) {
		this.filterSettings = filterSettings;
		if (filterSettings != null) {
			onFilterChanged(null, this.filterSettings);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.FilterChangedListener#onFilterChanged(com.dsatab.
	 * view.FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		// if (getView() != null) {
		// Debug.verbose("Unbinding drawbale to free memory from fragment");
		// Util.unbindDrawables(getView());
		// }

		// Debug.verbose(getClass().getName() + " destroyView");

		Hero hero = getHero();
		if (hero != null) {
			unloadHero(hero);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Debug.verbose(getClass().getName() + " createView");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		return view;
	}

	public static View configureContainerView(View view) {
		LinearLayout.LayoutParams params;

		if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
			params = ((LinearLayout.LayoutParams) view.getLayoutParams());
		} else {
			params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			view.setLayoutParams(params);
		}

		if (params.weight > 0)
			params.width = 0;

		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			filterSettings = savedInstanceState.getParcelable(FILTER_SETTINGS);
		}
		Hero hero = getHero();
		if (hero != null) {
			// Debug.verbose(getClass().getName() +
			// " onActivity created and ONSCREEN CALLING HEROLOADED");
			loadHero(hero);
		}

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#setUserVisibleHint(boolean)
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (getUserVisibleHint() == false && isVisibleToUser == true)
			onShown();

		super.setUserVisibleHint(isVisibleToUser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = DSATabApplication.getPreferences();
	}

	public Hero getHero() {
		return DSATabApplication.getInstance().getHero();
	}

	public final void loadHero(Hero hero) {
		onHeroLoaded(hero);
		onAttachListener(hero);
	}

	public final void unloadHero(Hero hero) {
		onDetachListener(hero);
	}

	public abstract void onHeroLoaded(Hero hero);

	protected MainActivity getBaseActivity() {
		if (getActivity() instanceof MainActivity)
			return (MainActivity) getActivity();
		else
			return null;
	}

	protected View findViewById(int id) {
		return getView().findViewById(id);
	}

	@Override
	public void onValueChanged(Value value) {

	}

	@Override
	public void onModifierAdded(Modificator value) {

	}

	@Override
	public void onModifierRemoved(Modificator value) {

	}

	@Override
	public void onModifierChanged(Modificator value) {

	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {

	}

	@Override
	public void onPortraitChanged() {

	}

	@Override
	public void onItemAdded(Item item) {

	}

	@Override
	public void onItemRemoved(Item item) {

	}

	@Override
	public void onItemChanged(EquippedItem item) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.HeroChangedListener#onItemChanged(com.dsatab
	 * .data.items.Item)
	 */
	@Override
	public void onItemChanged(Item item) {

	}

	@Override
	public void onItemEquipped(EquippedItem item) {

	}

	@Override
	public void onItemUnequipped(EquippedItem item) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.view.listener.HeroChangedListener#onActiveSetChanged(int,
	 * int)
	 */
	@Override
	public void onActiveSetChanged(int newSet, int oldSet) {

	}

	OnLongClickListener contextMenuListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			Menu menu = new MenuBuilder(getActivity());

			IconContextMenuInfo menuInfo = new IconContextMenuInfo();
			Object info = onCreateIconContextMenu(menu, v, menuInfo);

			if (menu != null && menu.hasVisibleItems()) {
				IconContextMenu cm = new IconContextMenu(getActivity(), menu, menuInfo.getTitle());
				cm.setInfo(info);
				cm.setOnIconContextItemSelectedListener(BaseFragment.this);

				onPrepareIconContextMenu(cm, v);
				cm.show();
				return true;
			}

			return false;
		}
	};

	public void registerForIconContextMenu(View v) {
		v.setOnLongClickListener(contextMenuListener);
	}

	public void unregisterForIconContextMenu(View v) {
		v.setOnLongClickListener(null);
	}

	public Object onCreateIconContextMenu(Menu menu, View v, IconContextMenuInfo menuInfo) {
		return null;
	}

	public void onPrepareIconContextMenu(IconContextMenu menu, View v) {
	}

	public void onIconContextItemSelected(MenuItem item, Object info) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#
	 * onSharedPreferenceChanged(android.content.SharedPreferences,
	 * java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (BasePreferenceActivity.KEY_MODIFY_TABS.equals(key)) {
			onFilterChanged(null, getFilterSettings());
		}
	}

	protected FilterSettings getFilterSettings() {
		return filterSettings;
	}
}
