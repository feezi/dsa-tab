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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
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
import com.dsatab.common.HeroExchange;
import com.dsatab.common.Util;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroLoader;
import com.dsatab.data.Probe;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.DualFragment;
import com.dsatab.data.adapter.TabPagerMemoryAdapter;
import com.dsatab.fragment.ArtFragment;
import com.dsatab.fragment.AttributeListFragment;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.FightFragment;
import com.dsatab.fragment.SpellFragment;
import com.dsatab.fragment.TalentFragment;
import com.dsatab.util.Debug;
import com.dsatab.view.DiceSlider;
import com.dsatab.view.FightFilterSettings;
import com.dsatab.view.FilterDialog;
import com.dsatab.view.FilterSettings.FilterType;
import com.dsatab.view.InlineEditDialog;
import com.dsatab.view.InlineEditFightDialog;
import com.dsatab.view.ListFilterSettings;
import com.dsatab.view.MyViewPager;
import com.dsatab.view.TipOfTheDayDialog;
import com.dsatab.view.listener.ShakeListener;
import com.gandulf.guilib.view.VersionInfoDialog;

public class MainActivity extends BaseFragmentActivity implements OnClickListener, OnPageChangeListener,
		LoaderManager.LoaderCallbacks<Hero>, OnSharedPreferenceChangeListener {

	protected static final String INTENT_TAB_INFO = "tabInfo";

	public static final String PREF_LAST_HERO = "LAST_HERO";

	private static final String KEY_HERO_PATH = "HERO_PATH";

	public static final int ACTION_PREFERENCES = 1000;
	private static final int ACTION_EDIT_TAB = 1001;
	public static final int ACTION_ADD_MODIFICATOR = 1003;
	protected static final int ACTION_CHOOSE_HERO = 1004;
	public static final int ACTION_EDIT_MODIFICATOR = 1005;

	private static final String KEY_TAB_INFO = "tabInfo";

	protected SharedPreferences preferences;

	// protected List<BaseFragment> fragments;

	private DiceSlider diceSlider;
	private View diceSliderContainer;

	private ShakeListener mShaker;

	private RelativeLayout relMainLayout;

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

	private TabPagerMemoryAdapter viewPagerAdapter;

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
		if (diceSlider.getVisibility() != View.VISIBLE) {
			diceSlider.setVisibility(View.VISIBLE);
			diceSlider.getHandle().startAnimation(AnimationUtils.makeInChildBottomAnimation(this));
		}
	}

	public void hideDiceSlider() {
		if (diceSlider.isOpened())
			diceSlider.close();
		if (diceSlider.getVisibility() != View.GONE) {
			diceSlider.setVisibility(View.GONE);
			diceSlider.getHandle().startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
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
		Debug.verbose("Creating loader for " + args.getString(KEY_HERO_PATH));
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

		if (requestCode == ACTION_EDIT_TAB && resultCode == RESULT_OK) {
			setupTabs();
		}

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

		if (viewPagerAdapter != null && viewPagerAdapter.getCurrentFragments() != null) {
			viewPagerAdapter.getCurrentFragments().onActivityResult(requestCode, resultCode, data);
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

		relMainLayout = (RelativeLayout) findViewById(R.id.gen_main_layout);

		preferences = DSATabApplication.getPreferences();

		String orientation = preferences.getString(BasePreferenceActivity.KEY_SCREEN_ORIENTATION,
				DsaPreferenceActivity.DEFAULT_SCREEN_ORIENTATION);

		Configuration configuration = getResources().getConfiguration();

		Debug.verbose("onCreate Orientation =" + configuration.orientation);
		if (DsaPreferenceActivity.SCREEN_ORIENTATION_LANDSCAPE.equals(orientation)
				&& configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
			Debug.verbose("Setting landscape");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			return;
		} else if (DsaPreferenceActivity.SCREEN_ORIENTATION_PORTRAIT.equals(orientation)
				&& configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Debug.verbose("Setting portrait");
			return;
		} else if (DsaPreferenceActivity.SCREEN_ORIENTATION_AUTO.equals(orientation)
				&& getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			Debug.verbose("Setting sensor");
		}

		probeListener = new ProbeListener(this);
		editListener = new EditListener(this);

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
			registerShakeDice();
		}

		viewPager = (MyViewPager) findViewById(R.id.viewpager);

		if (savedInstanceState != null) {
			tabInfo = savedInstanceState.getParcelable(KEY_TAB_INFO);
		}

		if (tabInfo == null && getIntent() != null) {
			tabInfo = getIntent().getParcelableExtra(INTENT_TAB_INFO);
		}

		setupDiceSilder();

		if (!showNewsInfoPopup()) {
			showTipPopup();
		}
	}

	/**
	 * 
	 */
	private void setupTabs() {

		if (viewPagerAdapter == null) {
			viewPagerAdapter = new TabPagerMemoryAdapter(this, getSupportFragmentManager(), getHeroConfiguration());
			viewPager.setAdapter(viewPagerAdapter);
		} else {
			viewPagerAdapter.setConfiguration(getHeroConfiguration());
		}

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
			if (tabInfo == this.tabInfo)
				bar.getTabAt(i).select();
		}
	}

	private boolean showNewsInfoPopup() {

		if (VersionInfoDialog.newsShown)
			return false;

		VersionInfoDialog newsDialog = new VersionInfoDialog(this);
		newsDialog.setDonateContentId(R.raw.donate);
		newsDialog.setDonateVersion(DSATabApplication.getInstance().isLiteVersion());
		newsDialog.setDonateUrl(DSATabApplication.PAYPAL_DONATION_URL);
		newsDialog.setTitle(R.string.news_title);
		newsDialog.setRawClass(R.raw.class);
		newsDialog.setIcon(R.drawable.icon);

		if (newsDialog.hasContent()) {
			newsDialog.show();
			return true;
		} else {
			return false;
		}

	}

	private boolean showTipPopup() {

		if (TipOfTheDayDialog.tipShown || !preferences.getBoolean(TipOfTheDayDialog.PREF_SHOW_TIPS, true))
			return false;

		TipOfTheDayDialog dialog = new TipOfTheDayDialog(this);
		dialog.show();

		TipOfTheDayDialog.tipShown = true;
		return true;

	}

	private HeroConfiguration getHeroConfiguration() {
		HeroConfiguration tabConfig = null;
		if (getHero() != null) {
			tabConfig = getHero().getHeroConfiguration();
		}
		return tabConfig;
	}

	protected void setupDiceSilder() {
		if (diceSliderContainer != null)
			relMainLayout.removeView(diceSliderContainer);

		diceSliderContainer = LayoutInflater.from(this).inflate(R.layout.dice_slider, relMainLayout, false);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) diceSliderContainer.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		relMainLayout.addView(diceSliderContainer);

		diceSlider = (DiceSlider) relMainLayout.findViewById(R.id.SlidingDrawer);
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

		Debug.verbose("Refreshing tabs, old:" + tabInfo);

		List<TabInfo> tabs = getHeroConfiguration().getTabs();

		// if we have an existing tabinfo check if it's upto date
		if (tabInfo != null) {
			// check wether tabinfo is uptodate (within current tabconfig
			if (tabs.contains(tabInfo)) {
				Debug.verbose("Tab found using old one.");
				return tabInfo;
			}

			// look for tabinfo with same activities
			for (TabInfo tab : tabs) {

				if (Util.equalsOrNull(tab.getPrimaryActivityClazz(), tabInfo.getPrimaryActivityClazz())
						&& Util.equalsOrNull(tab.getSecondaryActivityClazz(), tabInfo.getSecondaryActivityClazz())) {

					Debug.verbose("1 New tab found with same primary and secondary clazz :" + tab);
					return tab;
				}
			}

			// if we have portraitmode and a secondary clazz this means we
			// switched from landscape here, in this case we look for a tabinfo
			// with the primary activityclass and use this one
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
					&& tabInfo.getSecondaryActivityClazz() != null) {

				Class<? extends BaseFragment> activityClazz = tabInfo.getPrimaryActivityClazz();
				if (activityClazz != null) {
					for (TabInfo tab : tabs) {
						if (activityClazz.equals(tab.getPrimaryActivityClazz())) {
							Debug.verbose("2 New tab found with same primary clazz :" + tab);
							return tab;
						}
					}
				}
			}

			// if we have landscape mode and a empty secondary activity look for
			// one with one
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
					&& tabInfo.getSecondaryActivityClazz() == null) {

				Class<? extends BaseFragment> activityClazz = tabInfo.getPrimaryActivityClazz();
				if (activityClazz != null) {
					for (TabInfo tab : tabs) {
						if (activityClazz.equals(tab.getPrimaryActivityClazz())
								|| activityClazz.equals(tab.getSecondaryActivityClazz())) {

							Debug.verbose("3 New tab found with same primary clazz or secondary clazz :" + tab);
							return tab;

						}
					}
				}
			}
		}
		Debug.verbose("No tab found using first first one:" + tabs.get(0));

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
			Toast.makeText(this, "Error: Trying to load empty hero. Please contact developer!", Toast.LENGTH_LONG);
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

		if (viewPagerAdapter.getCurrentFragments() != null) {
			viewPagerAdapter.getCurrentFragments().loadHero(hero);
		}

		BaseFragment f = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AttributeListFragment.TAG);
		if (f != null && f.isAdded())
			f.loadHero(hero);

	}

	protected void onHeroUnloaded(Hero hero) {

		Debug.verbose("Unload hero " + hero.getName());

		if (viewPagerAdapter.getCurrentFragments() != null) {
			viewPagerAdapter.getCurrentFragments().unloadHero(hero);
		}
		BaseFragment f = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AttributeListFragment.TAG);
		if (f != null && f.isAdded())
			f.unloadHero(hero);

		viewPagerAdapter.onDestroy();
		viewPagerAdapter = null;
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

		if (mShaker != null) {
			mShaker.setOnShakeListener(null);
			mShaker = null;
		}
	}

	private void registerShakeDice() {

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

		if (savedInstanceState == null) {
			Debug.verbose("New instance setup tabs");
			showTab(tabInfo);
		} else {
			Debug.verbose("Old instance keep tabs");
			showTab(tabInfo);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_load_hero, Menu.NONE, "Held laden");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_archive);

		item = menu.add(Menu.NONE, R.id.option_save_hero, Menu.NONE, "Held speichern");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_save);

		item = menu.add(Menu.NONE, R.id.option_export_hero, Menu.NONE, "Held exportieren");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		item.setIcon(R.drawable.ic_menu_upload);

		item = menu.add(Menu.NONE, R.id.option_edit_tabs, Menu.NONE, "Tabs anpassen");
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
		com.actionbarsherlock.view.MenuItem item = menu.findItem(R.id.option_fight_set);
		if (item != null) {
			switch (getHero().getActiveSet()) {
			case 0:
				item.setIcon(R.drawable.ic_menu_set_1);
				break;
			case 1:
				item.setIcon(R.drawable.ic_menu_set_2);
				break;
			case 2:
				item.setIcon(R.drawable.ic_menu_set_3);
				break;
			}
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
		case R.id.option_export_hero: {
			HeroExchange exchange = new HeroExchange(this);
			exchange.exportHero(getHero());
			return true;
		}
		case R.id.option_filter:
			FilterDialog dialog = new FilterDialog(this);

			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					SharedPreferences pref = DSATabApplication.getPreferences();

					ListFilterSettings talentSettings = new ListFilterSettings(pref.getBoolean(
							FilterDialog.PREF_KEY_TALENT_FAVORITE, true), pref.getBoolean(
							FilterDialog.PREF_KEY_TALENT_NORMAL, true), pref.getBoolean(
							FilterDialog.PREF_KEY_TALENT_UNUSED, false), pref.getBoolean(
							FilterDialog.PREF_KEY_TALENT_MODIFIERS, true));

					ListFilterSettings spellSettings = new ListFilterSettings(pref.getBoolean(
							FilterDialog.PREF_KEY_SPELL_FAVORITE, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SPELL_NORMAL, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SPELL_UNUSED, false), pref.getBoolean(
							FilterDialog.PREF_KEY_SPELL_MODIFIERS, true));

					ListFilterSettings artSettings = new ListFilterSettings(pref.getBoolean(
							FilterDialog.PREF_KEY_ART_FAVORITE, true), pref.getBoolean(
							FilterDialog.PREF_KEY_ART_NORMAL, true), pref.getBoolean(FilterDialog.PREF_KEY_ART_UNUSED,
							false), pref.getBoolean(FilterDialog.PREF_KEY_ART_MODIFIERS, true));

					FightFilterSettings fightSettings = new FightFilterSettings(pref.getBoolean(
							FilterDialog.PREF_KEY_SHOW_ARMOR, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SHOW_MODIFIER, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SHOW_EVADE, false), pref.getBoolean(
							FilterDialog.PREF_KEY_INCLUDE_MODIFIER, true));

					if (viewPagerAdapter != null && viewPagerAdapter.getCurrentFragments() != null) {
						DualFragment fragment = viewPagerAdapter.getCurrentFragments();
						if (fragment != null) {
							fragment.onFilterChanged(FilterType.Talent, talentSettings);
							fragment.onFilterChanged(FilterType.Spell, spellSettings);
							fragment.onFilterChanged(FilterType.Art, artSettings);
							fragment.onFilterChanged(FilterType.Fight, fightSettings);

						}
					}
				}
			});

			dialog.show();

			dialog.setFilterFightVisibile(viewPagerAdapter.getCurrentFragments().contains(FightFragment.class));
			dialog.setFilterListVisibile(viewPagerAdapter.getCurrentFragments().contains(TalentFragment.class,
					SpellFragment.class, ArtFragment.class));
			return true;

		case R.id.option_edit_tabs:
			startActivityForResult(new Intent(this, TabEditActivity.class), ACTION_EDIT_TAB);
			return true;
		case R.id.option_fight_set:
			if (getHero() != null) {
				getHero().setActiveSet(getHero().getNextActiveSet());
			}
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

		if (viewPagerAdapter != null) {
			for (DualFragment fragment : viewPagerAdapter.getDualFragments()) {
				if (fragment != null) {
					fragment.onSharedPreferenceChanged(sharedPreferences, key);
				}
			}
		}

		BaseFragment f = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AttributeListFragment.TAG);
		if (f != null && f.isAdded())
			f.onSharedPreferenceChanged(sharedPreferences, key);
	}

	protected void showHeroChooser() {
		startActivityForResult(new Intent(MainActivity.this, HeroChooserActivity.class), ACTION_CHOOSE_HERO);
	}
}
