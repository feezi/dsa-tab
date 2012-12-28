/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dsatab.AnalyticsManager;
import com.dsatab.DSATabApplication;
import com.dsatab.HeroConfiguration;
import com.dsatab.R;
import com.dsatab.TabInfo;
import com.dsatab.activity.menu.TabListener;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroLoader;
import com.dsatab.data.Probe;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.TabPagerAdapter;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.fragment.AttributeListFragment;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.DualPaneFragment;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;
import com.dsatab.view.ChangeLogDialog;
import com.dsatab.view.DiceSlider;
import com.dsatab.view.InlineEditDialog;
import com.dsatab.view.InlineEditFightDialog;
import com.dsatab.view.MyViewPager;
import com.dsatab.view.listener.ShakeListener;

public class MainActivity extends BaseFragmentActivity implements OnClickListener, OnPageChangeListener,
		LoaderManager.LoaderCallbacks<Hero>, OnSharedPreferenceChangeListener {

	public static boolean newsShown = false;

	protected static final String INTENT_TAB_INFO = "tabInfo";

	public static final String PREF_LAST_HERO = "LAST_HERO";

	private static final String KEY_HERO_PATH = "HERO_PATH";

	public static final int ACTION_PREFERENCES = 1000;
	public static final int ACTION_ADD_MODIFICATOR = 1003;
	protected static final int ACTION_CHOOSE_HERO = 1004;
	public static final int ACTION_EDIT_MODIFICATOR = 1005;

	private static final String KEY_TAB_INFO = "tabInfo";

	protected SharedPreferences preferences;

	// protected List<BaseFragment> fragments;

	private DiceSlider diceSlider;
	private View attributeList;

	private ShakeListener mShaker;

	private MyViewPager viewPager;

	private TabInfo tabInfo;

	public static class EditListener implements View.OnClickListener, View.OnLongClickListener {

		private WeakReference<MainActivity> mActivity;

		/**
		 * 
		 */
		public EditListener(MainActivity context) {
			this.mActivity = new WeakReference<MainActivity>(context);
		}

		public void onClick(View v) {

			Value value = null;
			if (v.getTag(R.id.TAG_KEY_VALUE) instanceof Value) {
				value = (Value) v.getTag(R.id.TAG_KEY_VALUE);
			} else if (v.getTag() instanceof Value) {
				value = (Value) v.getTag();
			} else if (v.getTag(R.id.TAG_KEY_VALUE) instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag(R.id.TAG_KEY_VALUE);
				if (DSATabApplication.getInstance().getHero() != null)
					value = DSATabApplication.getInstance().getHero().getAttribute(type);
			} else if (v.getTag() instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag();
				if (DSATabApplication.getInstance().getHero() != null)
					value = DSATabApplication.getInstance().getHero().getAttribute(type);
			}

			if (value != null) {
				showEditPopup(v.getContext(), value);
			}

		}

		public boolean onLongClick(View v) {
			Value value = null;
			if (v.getTag(R.id.TAG_KEY_VALUE) instanceof Value) {
				value = (Value) v.getTag(R.id.TAG_KEY_VALUE);
			} else if (v.getTag() instanceof Value) {
				value = (Value) v.getTag();
			} else if (v.getTag(R.id.TAG_KEY_VALUE) instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag(R.id.TAG_KEY_VALUE);
				if (DSATabApplication.getInstance().getHero() != null)
					value = DSATabApplication.getInstance().getHero().getAttribute(type);
			} else if (v.getTag() instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag();
				if (DSATabApplication.getInstance().getHero() != null)
					value = DSATabApplication.getInstance().getHero().getAttribute(type);
			}

			if (value != null && mActivity.get() != null) {
				showEditPopup(mActivity.get(), value);
				return true;
			}
			return false;

		}

	}

	public static class ProbeListener implements View.OnClickListener, View.OnLongClickListener {

		private WeakReference<MainActivity> mActivity;

		/**
		 * 
		 */
		public ProbeListener(MainActivity context) {
			this.mActivity = new WeakReference<MainActivity>(context);
		}

		public void onClick(View v) {

			Probe probe = null;

			if (v.getTag(R.id.TAG_KEY_PROBE) instanceof Probe) {
				probe = (Probe) v.getTag(R.id.TAG_KEY_PROBE);
			} else if (v.getTag() instanceof Probe) {
				probe = (Probe) v.getTag();
			} else if (v.getTag(R.id.TAG_KEY_PROBE) instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag(R.id.TAG_KEY_PROBE);
				if (DSATabApplication.getInstance().getHero() != null)
					probe = DSATabApplication.getInstance().getHero().getAttribute(type);
			} else if (v.getTag() instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag();
				if (DSATabApplication.getInstance().getHero() != null)
					probe = DSATabApplication.getInstance().getHero().getAttribute(type);
			}

			if (probe != null && mActivity.get() != null) {
				mActivity.get().checkProbe(probe);
			}

		}

		public boolean onLongClick(View v) {
			Probe probe = null;

			if (v.getTag(R.id.TAG_KEY_PROBE) instanceof Probe) {
				probe = (Probe) v.getTag(R.id.TAG_KEY_PROBE);
			} else if (v.getTag() instanceof Probe) {
				probe = (Probe) v.getTag();
			} else if (v.getTag(R.id.TAG_KEY_PROBE) instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag(R.id.TAG_KEY_PROBE);
				if (DSATabApplication.getInstance().getHero() != null)
					probe = DSATabApplication.getInstance().getHero().getAttribute(type);
			} else if (v.getTag() instanceof AttributeType) {
				AttributeType type = (AttributeType) v.getTag();
				if (DSATabApplication.getInstance().getHero() != null)
					probe = DSATabApplication.getInstance().getHero().getAttribute(type);
			}

			if (probe != null && mActivity.get() != null) {
				mActivity.get().checkProbe(probe);
				return true;
			}
			return false;
		}

	}

	protected ProbeListener probeListener;

	protected EditListener editListener;

	private TabPagerAdapter viewPagerAdapter;

	public Hero getHero() {
		return DSATabApplication.getInstance().getHero();
	}

	public final void loadHero(String heroPath) {

		Hero oldHero = DSATabApplication.getInstance().getHero();
		if (oldHero != null)
			onHeroUnloaded(oldHero);

		Bundle args = new Bundle();
		args.putString(KEY_HERO_PATH, heroPath);
		getSupportLoaderManager().restartLoader(0, args, this);
	}

	public static void showEditPopup(Context context, Value value) {

		if (value instanceof CombatMeleeTalent) {
			InlineEditFightDialog inlineEditFightdialog = new InlineEditFightDialog(context, (CombatMeleeTalent) value);
			inlineEditFightdialog.setTitle(value.getName());
			inlineEditFightdialog.show();
		} else if (value != null) {
			InlineEditDialog inlineEditdialog = new InlineEditDialog(context, value);
			inlineEditdialog.setTitle(value.getName());
			inlineEditdialog.show();
		}

	}

	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putParcelable(SearchableActivity.INTENT_TAB_INFO, tabInfo);
		startSearch(null, false, appData, false);
		return true;
	}

	public void showDiceSlider() {
		if (diceSlider != null) {
			diceSlider.setSliderVisible(true);
		}
	}

	public void hideDiceSlider() {
		if (diceSlider != null) {
			if (diceSlider.isOpened()) {
				diceSlider.close();
			}
			diceSlider.setSliderVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled
	 * (int, float, int)
	 */
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#
	 * onPageScrollStateChanged(int)
	 */
	@Override
	public void onPageScrollStateChanged(int state) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected
	 * (int)
	 */
	@Override
	public void onPageSelected(int position) {

		if (getSupportActionBar().getSelectedNavigationIndex() != position)
			getSupportActionBar().getTabAt(position).select();

		tabInfo = getHeroConfiguration().getTab(position);

		viewPager.setDraggable(tabInfo.isTabFlingEnabled());

		if (tabInfo.isDiceSlider())
			showDiceSlider();
		else
			hideDiceSlider();

		if (tabInfo.isAttributeList()) {
			attributeList.setVisibility(View.VISIBLE);
		} else {
			attributeList.setVisibility(View.GONE);
		}
		// invalidateOptionsMenu();
	}

	private void loadHero() {
		if (DSATabApplication.getInstance().getHero() != null) {
			onHeroLoaded(DSATabApplication.getInstance().getHero());
		} else {
			String heroPath = preferences.getString(PREF_LAST_HERO, null);
			if (heroPath != null && new File(heroPath).exists()) {
				loadHero(heroPath);
			} else {
				showHeroChooser();
			}
		}
	}

	public Loader<Hero> onCreateLoader(int id, Bundle args) {
		// Debug.verbose("Creating loader for " +
		// args.getString(KEY_HERO_PATH));
		return new HeroLoader(this, args.getString(KEY_HERO_PATH));
	}

	public void onLoadFinished(Loader<Hero> loader, Hero hero) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)

		DSATabApplication.getInstance().hero = hero;

		if (hero != null) {
			Toast.makeText(this, getString(R.string.hero_loaded, hero.getName()), Toast.LENGTH_SHORT).show();
		}
		onHeroLoaded(hero);

	}

	public void onLoaderReset(Loader<Hero> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		// mAdapter.swapCursor(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// int icon = data.getIntExtra(TabEditActivity.INTENT_ICON, 0);
		// Class<? extends BaseFragment> class1 = (Class<? extends
		// BaseFragment>) data
		// .getSerializableExtra(TabEditActivity.INTENT_PRIMARY_CLASS);
		// Class<? extends BaseFragment> class2 = (Class<? extends
		// BaseFragment>) data
		// .getSerializableExtra(TabEditActivity.INTENT_SECONDARY_CLASS);
		//
		// int tabIndex = data.getIntExtra(TabEditActivity.INTENT_TAB_INDEX,
		// -1);
		//
		// Debug.verbose("Edit tab with index " + tabIndex);
		//
		// if (tabIndex >= 0 && tabIndex <
		// getHeroConfiguration().getTabs().size()) {
		// TabInfo info = getHeroConfiguration().getTabs().get(tabIndex);
		//
		// ImageButton selectedTab = (ImageButton)
		// tabLayout.findViewWithTag(info);
		// info.setTabResourceId(icon);
		// info.setPrimaryActivityClazz(class1);
		// info.setSecondaryActivityClazz(class2);
		// info.setDiceSlider(data.getBooleanExtra(TabEditActivity.INTENT_DICE_SLIDER,
		// true));
		//
		// selectedTab.setImageResource(icon);
		//
		// // update view if current tab was changed
		// if (info == tabInfo) {
		// showTab(tabInfo);
		// }
		// selectedTab = null;
		//
		// viewPagerAdapter.notifyDataSetChanged();
		// }
		//
		// } else if (requestCode == ACTION_ADD_TAB && resultCode == RESULT_OK)
		// {
		//
		// int icon = data.getIntExtra(TabEditActivity.INTENT_ICON, 0);
		// Class<? extends BaseFragment> class1 = (Class<? extends
		// BaseFragment>) data
		// .getSerializableExtra(TabEditActivity.INTENT_PRIMARY_CLASS);
		// Class<? extends BaseFragment> class2 = (Class<? extends
		// BaseFragment>) data
		// .getSerializableExtra(TabEditActivity.INTENT_SECONDARY_CLASS);
		// int index = data.getIntExtra(TabEditActivity.INTENT_TAB_INDEX, -1);
		//
		// TabInfo newInfo = new TabInfo(class1, class2, icon);
		// newInfo.setDiceSlider(data.getBooleanExtra(TabEditActivity.INTENT_DICE_SLIDER,
		// true));
		// LayoutInflater inflater = LayoutInflater.from(this);
		// View tab = createTab(inflater, newInfo);
		// tab.setEnabled(true);
		// if (index >= 0) {
		// tabLayout.addView(tab, index);
		// getHeroConfiguration().getTabs().add(index, newInfo);
		// } else {
		// tabLayout.addView(tab);
		// getHeroConfiguration().getTabs().add(newInfo);
		// }
		// viewPagerAdapter.notifyDataSetChanged();

		// } else
		if (requestCode == ACTION_CHOOSE_HERO) {

			if (resultCode == RESULT_OK) {
				String heroPath = data.getStringExtra(HeroChooserActivity.INTENT_NAME_HERO_PATH);
				Debug.verbose("HeroChooserActivity returned with path:" + heroPath);
				loadHero(heroPath);
			} else if (resultCode == RESULT_CANCELED) {
				if (getHero() == null) {
					finish();
				}
			}
		} else if (requestCode == ACTION_PREFERENCES) {

			SharedPreferences preferences = DSATabApplication.getPreferences();

			if (preferences.getBoolean(BasePreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
				registerShakeDice();
			} else {
				unregisterShakeDice();
			}
		}

		// notify other listeners (fragments, heroes)
		if (viewPagerAdapter != null) {
			DualPaneFragment fragment = viewPagerAdapter.getFragment(viewPager.getCurrentItem());
			if (fragment != null) {
				((DualPaneFragment) fragment).onActivityResult(requestCode, resultCode, data);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public ProbeListener getProbeListener() {
		return probeListener;
	}

	public EditListener getEditListener() {
		return editListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(DSATabApplication.getInstance().getCustomTheme());
		applyPreferencesToTheme();
		super.onCreate(savedInstanceState);

		DSATabApplication.getPreferences().registerOnSharedPreferenceChangeListener(this);

		// start tracing to "/sdcard/calc.trace"
		// android.os.Debug.startMethodTracing("dsatab");

		setContentView(R.layout.main_tab_view);

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		viewPager = (MyViewPager) findViewById(R.id.viewpager);

		preferences = DSATabApplication.getPreferences();

		String orientation = preferences.getString(BasePreferenceActivity.KEY_SCREEN_ORIENTATION,
				BasePreferenceActivity.DEFAULT_SCREEN_ORIENTATION);

		Configuration configuration = getResources().getConfiguration();

		// Debug.verbose("onCreate Orientation =" + configuration.orientation);
		if (BasePreferenceActivity.SCREEN_ORIENTATION_LANDSCAPE.equals(orientation)
				&& configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
			// Debug.verbose("Setting landscape");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			return;
		} else if (BasePreferenceActivity.SCREEN_ORIENTATION_PORTRAIT.equals(orientation)
				&& configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			// Debug.verbose("Setting portrait");
			return;
		} else if (BasePreferenceActivity.SCREEN_ORIENTATION_AUTO.equals(orientation)
				&& getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR
				&& getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			// Debug.verbose("Setting sensor");
			return;
		}

		probeListener = new ProbeListener(this);
		editListener = new EditListener(this);

		if (preferences.getBoolean(BasePreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
			registerShakeDice();
		}

		if (savedInstanceState != null) {
			tabInfo = savedInstanceState.getParcelable(KEY_TAB_INFO);
		}

		if (tabInfo == null && getIntent() != null) {
			tabInfo = getIntent().getParcelableExtra(INTENT_TAB_INFO);
		}

		diceSlider = (DiceSlider) findViewById(R.id.SlidingDrawer);
		diceSlider.setSlideHandleButton(findViewById(R.id.slideHandleButton));

		attributeList = findViewById(R.id.inc_attributes);

		showNewsInfoPopup();
	}

	/**
	 * 
	 */
	private void setupTabs() {
		viewPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), getHeroConfiguration());
		viewPager.setAdapter(viewPagerAdapter);

		viewPager.setOnPageChangeListener(null);
		ActionBar bar = getSupportActionBar();

		List<TabInfo> tabs = getHeroConfiguration().getTabs();
		int tabCount = tabs.size();

		bar.removeAllTabs();
		for (int i = 0; i < tabCount; i++) {
			TabInfo tabInfo = tabs.get(i);
			bar.addTab(bar.newTab().setIcon(tabInfo.getTabResourceId()).setTabListener(new TabListener(this, i)));
		}

		viewPager.setOnPageChangeListener(this);
		for (int i = 0; i < tabCount; i++) {
			TabInfo tabInfo = tabs.get(i);
			if (tabInfo == this.tabInfo) {
				bar.getTabAt(i).select();
				break;
			}
		}

	}

	private void showNewsInfoPopup() {

		if (newsShown)
			return;

		ChangeLogDialog logDialog = new ChangeLogDialog(this);
		logDialog.show();
		newsShown = true;
	}

	private HeroConfiguration getHeroConfiguration() {
		HeroConfiguration tabConfig = null;
		if (getHero() != null) {
			tabConfig = getHero().getHeroConfiguration();
		}
		return tabConfig;
	}

	protected boolean showNextTab() {
		if (tabInfo != null) {
			int index = getHeroConfiguration().getTabs().indexOf(tabInfo);
			return showTab(index + 1);
		} else {
			return false;
		}
	}

	protected boolean showPreviousTab() {
		if (tabInfo != null) {
			int index = getHeroConfiguration().getTabs().indexOf(tabInfo);
			return showTab(index - 1);
		} else {
			return false;
		}
	}

	/**
	 * 
	 */
	private TabInfo refreshTabInfo() {

		if (getHeroConfiguration() == null || getHeroConfiguration().getTabs().isEmpty()) {
			tabInfo = null;
			return null;
		}

		List<TabInfo> tabs = getHeroConfiguration().getTabs();

		// if we have an existing tabinfo check if it's upto date
		if (tabInfo != null) {
			// check wether tabinfo is uptodate (within current tabconfig
			if (tabs.contains(tabInfo)) {
				return tabInfo;
			}

			// look for tabinfo with same activities
			for (TabInfo tab : tabs) {

				if (Util.equalsOrNull(tab.getActivityClazz(0), tabInfo.getActivityClazz(0))
						&& Util.equalsOrNull(tab.getActivityClazz(1), tabInfo.getActivityClazz(1))) {
					return tab;
				}
			}

			// if we have portraitmode and a secondary clazz this means we
			// switched from landscape here, in this case we look for a tabinfo
			// with the primary activityclass and use this one
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
					&& tabInfo.getActivityClazz(1) != null) {

				Class<? extends BaseFragment> activityClazz = tabInfo.getActivityClazz(0);
				if (activityClazz != null) {
					for (TabInfo tab : tabs) {
						if (activityClazz.equals(tab.getActivityClazz(0))) {
							return tab;
						}
					}
				}
			}

			// if we have landscape mode and a empty secondary activity look for
			// one with one
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
					&& tabInfo.getActivityClazz(1) == null) {

				Class<? extends BaseFragment> activityClazz = tabInfo.getActivityClazz(0);
				if (activityClazz != null) {
					for (TabInfo tab : tabs) {
						if (activityClazz.equals(tab.getActivityClazz(0))
								|| activityClazz.equals(tab.getActivityClazz(1))) {
							return tab;

						}
					}
				}
			}
		}

		// last resort set tabinfo to first one if no matching one is found
		return tabs.get(0);
	}

	public boolean showTab(int index) {

		if (index >= 0 && index < getHeroConfiguration().getTabs().size()) {
			if (viewPager.getCurrentItem() != index)
				viewPager.setCurrentItem(index, false);
			return true;
		} else {
			return false;
		}
	}

	protected boolean showTab(TabInfo newTabInfo) {

		if (newTabInfo != null && getHeroConfiguration() != null) {
			viewPager.setCurrentItem(getHeroConfiguration().getTabs().indexOf(newTabInfo));
			return true;
		} else {
			return false;
		}
	}

	public void onClick(View v) {

		if (v.getTag() instanceof TabInfo) {
			TabInfo tabInfo = (TabInfo) v.getTag();

			// no need to reselect the current tab
			if (this.tabInfo != tabInfo) {
				showTab(tabInfo);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {

		if (diceSlider != null && diceSlider.isOpened()) {
			diceSlider.animateClose();
		} else {
			super.onBackPressed();
		}
	}

	protected void onHeroLoaded(Hero hero) {

		if (hero == null) {
			Toast.makeText(this, "Error: Trying to load empty hero. Please contact developer!", Toast.LENGTH_LONG)
					.show();
			// tabBarHelper.setNavigationTabsEnabled(false);
			return;
		} else {
			// tabBarHelper.setNavigationTabsEnabled(true);
		}

		TabInfo oldInfo = tabInfo;
		tabInfo = refreshTabInfo();
		setupTabs();

		if (tabInfo != oldInfo) {
			showTab(tabInfo);
		}

		if (viewPagerAdapter != null && viewPagerAdapter.getFragment(viewPager.getCurrentItem()) != null) {
			viewPagerAdapter.getFragment(viewPager.getCurrentItem()).loadHero(hero);
		}

		BaseFragment f = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AttributeListFragment.TAG);
		if (f != null && f.isAdded())
			f.loadHero(hero);

	}

	protected void onHeroUnloaded(Hero hero) {
		if (viewPagerAdapter != null && viewPagerAdapter.getFragment(viewPager.getCurrentItem()) != null) {
			viewPagerAdapter.getFragment(viewPager.getCurrentItem()).unloadHero(hero);
		}
		BaseFragment f = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AttributeListFragment.TAG);
		if (f != null && f.isAdded()) {
			f.unloadHero(hero);
		}

	}

	public boolean checkProbe(Probe probe) {
		if (diceSlider != null) {
			if (probe != null) {
				diceSlider.checkProbe(getHero(), probe);
				return true;
			}
		}
		return false;

	}

	private void unregisterShakeDice() {
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
			if (mShaker != null) {
				mShaker.setOnShakeListener(null);
				mShaker = null;
			}
		}
	}

	private void registerShakeDice() {
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
			if (mShaker == null) {

				final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				mShaker = new ShakeListener(this);
				mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
					public void onShake() {
						vibe.vibrate(100);
						if (diceSlider != null)
							diceSlider.rollDice20();
					}
				});

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		AnalyticsManager.startSession(this);
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		onHeroUnloaded(getHero());
		unregisterShakeDice();
		DSATabApplication.getPreferences().unregisterOnSharedPreferenceChangeListener(this);

		// android.os.Debug.stopMethodTracing();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if (mShaker != null)
			mShaker.pause();

		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mShaker != null)
			mShaker.resume();

		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		AnalyticsManager.endSession(this);
		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		loadHero();

		// setupTabs();
		showTab(tabInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_load_hero, Menu.NONE, "Helden");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_archive);

		item = menu.add(Menu.NONE, R.id.option_save_hero, Menu.NONE, "Held speichern");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_save);

		item = menu.add(Menu.NONE, R.id.option_tabs, Menu.NONE, "Tabs anpassen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_account_list);

		item = menu.add(Menu.NONE, R.id.option_settings, 99, "Einstellungen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_preferences);

		return super.onCreateOptionsMenu(menu);
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
		com.actionbarsherlock.view.MenuItem item = menu.findItem(R.id.option_set);
		if (item != null) {
			switch (getHero().getActiveSet()) {
			case 0:
				item.setIcon(Util.getThemeResourceId(this, R.attr.imgBarSet1));
				break;
			case 1:
				item.setIcon(Util.getThemeResourceId(this, R.attr.imgBarSet2));
				break;
			case 2:
				item.setIcon(Util.getThemeResourceId(this, R.attr.imgBarSet3));
				break;
			}
		}

		item = menu.findItem(R.id.option_save_hero);
		if (item != null) {
			item.setEnabled(getHero() != null);
		}
		item = menu.findItem(R.id.option_export_hero);
		if (item != null) {
			item.setEnabled(getHero() != null);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.option_load_hero:
			showHeroChooser();
			return true;
		case R.id.option_save_hero:
			DSATabApplication.getInstance().saveHero();
			return true;
		case R.id.option_settings:
			BasePreferenceActivity.startPreferenceActivity(this);
			return true;
		case R.id.option_set:
			if (getHero() != null) {
				getHero().setActiveSet(getHero().getNextActiveSet());
			}
			return true;
		case R.id.option_tabs:
			startActivity(new Intent(this, TabEditActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os
	 * .Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_TAB_INFO, tabInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey(KEY_TAB_INFO))
			tabInfo = savedInstanceState.getParcelable(KEY_TAB_INFO);

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
		// Debug.verbose(key + " changed");

		if (BasePreferenceActivity.KEY_STYLE_BG_PATH.equals(key)) {
			applyPreferencesToTheme();
		}

		if (BasePreferenceActivity.KEY_SCREEN_ORIENTATION.equals(key)) {
			String orientation = sharedPreferences.getString(BasePreferenceActivity.KEY_SCREEN_ORIENTATION,
					BasePreferenceActivity.DEFAULT_SCREEN_ORIENTATION);

			if (BasePreferenceActivity.SCREEN_ORIENTATION_LANDSCAPE.equals(orientation)) {
				if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
					// You need to check if your desired orientation isn't
					// already set because setting orientation restarts your
					// Activity which takes long
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			} else if (BasePreferenceActivity.SCREEN_ORIENTATION_PORTRAIT.equals(orientation)) {
				if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			} else if (BasePreferenceActivity.SCREEN_ORIENTATION_AUTO.equals(orientation)) {
				if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
				}
			}
		}

		if (BasePreferenceActivity.KEY_FULLSCREEN.equals(key)) {
			updateFullscreenStatus(preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));
		}

		if (BasePreferenceActivity.KEY_MODIFY_TABS.equals(key)) {
			setupTabs();
		}

		// notify other listeners (fragments, heroes)
		if (viewPagerAdapter != null) {
			for (Fragment fragment : viewPagerAdapter.getFragments()) {
				if (fragment != null) {
					((DualPaneFragment) fragment).onSharedPreferenceChanged(sharedPreferences, key);
				}
			}
		}

		BaseFragment f = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AttributeListFragment.TAG);
		if (f != null && f.isAdded())
			f.onSharedPreferenceChanged(sharedPreferences, key);

		Hero hero = DSATabApplication.getInstance().getHero();
		if (hero != null) {
			hero.onSharedPreferenceChanged(sharedPreferences, key);
		}
	}

	protected void showHeroChooser() {
		startActivityForResult(new Intent(MainActivity.this, HeroChooserActivity.class), ACTION_CHOOSE_HERO);
	}
}
