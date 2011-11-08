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
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dsatab.AnalyticsManager;
import com.dsatab.DSATabApplication;
import com.dsatab.HeroConfiguration;
import com.dsatab.R;
import com.dsatab.TabInfo;
import com.dsatab.common.HeroExchange;
import com.dsatab.common.Util;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroLoader;
import com.dsatab.data.Probe;
import com.dsatab.data.Value;
import com.dsatab.fragment.AttributeListFragment;
import com.dsatab.fragment.BaseFragment;
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
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.VersionInfoDialog;

public class MainActivity extends BaseFragmentActivity implements OnClickListener, OnPageChangeListener,
		LoaderManager.LoaderCallbacks<Hero> {

	protected static final String INTENT_TAB_INFO = "tabInfo";

	public static final String PREF_LAST_HERO = "LAST_HERO";

	private static final String KEY_HERO_PATH = "HERO_PATH";

	public static final int ACTION_PREFERENCES = 1000;
	private static final int ACTION_EDIT_TAB = 1001;
	private static final int ACTION_ADD_TAB = 1002;
	public static final int ACTION_ADD_MODIFICATOR = 1003;
	protected static final int ACTION_CHOOSE_HERO = 1004;
	public static final int ACTION_EDIT_MODIFICATOR = 1005;

	private static final String KEY_TAB_INFO = "tabInfo";

	protected SharedPreferences preferences;

	// protected List<BaseFragment> fragments;

	private DiceSlider diceSlider;
	private View diceSliderContainer;

	private ShakeListener mShaker;

	private LinearLayout tabLayout;

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

	private TabPagerAdapter viewPagerAdapter;

	/**
	 * 
	 */
	public MainActivity() {
		// fragments = new LinkedList<BaseFragment>();
	}

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
		} else {
			InlineEditDialog inlineEditdialog = new InlineEditDialog(context, value);
			inlineEditdialog.setTitle(value.getName());
			inlineEditdialog.show();
		}

	}

	public boolean isOnScreen(BaseFragment fragment) {
		if (tabInfo != null) {
			for (int i = 0; i < tabInfo.getTabCount(); i++) {
				if (tabInfo.getFragmentTagName(i).equals(fragment.getTag()))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putParcelable(SearchableActivity.INTENT_TAB_INFO, tabInfo);
		startSearch(null, false, appData, false);
		return true;
	}

	public void showDiceSlider() {
		diceSlider.setVisibility(View.VISIBLE);
		// diceSlider.getHandle().startAnimation(AnimationUtils.makeInChildBottomAnimation(this));

	}

	public void hideDiceSlider() {
		if (diceSlider.isOpened())
			diceSlider.close();
		diceSlider.setVisibility(View.GONE);
		// diceSlider.getHandle().startAnimation(AnimationUtils.loadAnimation(this,
		// android.R.anim.fade_out));

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

		// for (BaseFragment fragment : getPageFragments()) {
		// fragment.unloadHero(getHero());
		// }

		int count = tabLayout.getChildCount();

		for (int i = 0; i < count; i++) {
			ImageButton b = (ImageButton) tabLayout.getChildAt(i);
			b.setSelected(i == position);
		}

		tabInfo = getHeroConfiguration().getTab(position);

		viewPager.setDraggable(tabInfo.isTabFlingEnabled());

		if (tabInfo.isDiceSlider())
			showDiceSlider();
		else
			hideDiceSlider();

		// for (BaseFragment fragment : getPageFragments()) {
		// fragment.loadHero(getHero());
		// }
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

		Toast.makeText(this, getString(R.string.hero_loaded, hero.getName()), Toast.LENGTH_SHORT).show();

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
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ACTION_EDIT_TAB && resultCode == RESULT_OK) {
			int icon = data.getIntExtra(TabEditActivity.INTENT_ICON, 0);
			Class<? extends BaseFragment> class1 = (Class<? extends BaseFragment>) data
					.getSerializableExtra(TabEditActivity.INTENT_PRIMARY_CLASS);
			Class<? extends BaseFragment> class2 = (Class<? extends BaseFragment>) data
					.getSerializableExtra(TabEditActivity.INTENT_SECONDARY_CLASS);

			int tabIndex = data.getIntExtra(TabEditActivity.INTENT_TAB_INDEX, -1);

			Debug.verbose("Edit tab with index " + tabIndex);

			if (tabIndex >= 0 && tabIndex < getHeroConfiguration().getTabs().size()) {
				TabInfo info = getHeroConfiguration().getTabs().get(tabIndex);

				ImageButton selectedTab = (ImageButton) tabLayout.findViewWithTag(info);
				info.setTabResourceId(icon);
				info.setPrimaryActivityClazz(class1);
				info.setSecondaryActivityClazz(class2);
				info.setDiceSlider(data.getBooleanExtra(TabEditActivity.INTENT_DICE_SLIDER, true));

				selectedTab.setImageResource(icon);

				// update view if current tab was changed
				if (info == tabInfo) {
					showTab(tabInfo);
				}
				selectedTab = null;

				viewPagerAdapter.notifyDataSetChanged();
			}

		} else if (requestCode == ACTION_ADD_TAB && resultCode == RESULT_OK) {

			int icon = data.getIntExtra(TabEditActivity.INTENT_ICON, 0);
			Class<? extends BaseFragment> class1 = (Class<? extends BaseFragment>) data
					.getSerializableExtra(TabEditActivity.INTENT_PRIMARY_CLASS);
			Class<? extends BaseFragment> class2 = (Class<? extends BaseFragment>) data
					.getSerializableExtra(TabEditActivity.INTENT_SECONDARY_CLASS);
			int index = data.getIntExtra(TabEditActivity.INTENT_TAB_INDEX, -1);

			TabInfo newInfo = new TabInfo(class1, class2, icon);
			newInfo.setDiceSlider(data.getBooleanExtra(TabEditActivity.INTENT_DICE_SLIDER, true));
			LayoutInflater inflater = LayoutInflater.from(this);
			View tab = createTab(inflater, newInfo);
			tab.setEnabled(true);
			if (index >= 0) {
				tabLayout.addView(tab, index);
				getHeroConfiguration().getTabs().add(index, newInfo);
			} else {
				tabLayout.addView(tab);
				getHeroConfiguration().getTabs().add(newInfo);
			}
			viewPagerAdapter.notifyDataSetChanged();

		} else if (requestCode == ACTION_CHOOSE_HERO) {

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

			if (preferences.getBoolean(DsaPreferenceActivityHC.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
				registerShakeDice();
			} else {
				unregisterShakeDice();
			}
		}

		for (BaseFragment fragment : getVisibleFragments()) {
			fragment.onActivityResult(requestCode, resultCode, data);
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
		super.onCreate(savedInstanceState);

		// start tracing to "/sdcard/calc.trace"
		// android.os.Debug.startMethodTracing("dsatab");

		setContentView(R.layout.main_tab_view);

		relMainLayout = (RelativeLayout) findViewById(R.id.gen_main_layout);

		Configuration configuration = getResources().getConfiguration();

		if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			BitmapDrawable TileMe = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
					R.drawable.bg_tab_land));
			TileMe.setTileModeX(Shader.TileMode.MIRROR);
			TileMe.setTileModeY(Shader.TileMode.MIRROR);

			findViewById(R.id.inc_tabs).setBackgroundDrawable(TileMe);
		} else {
			BitmapDrawable TileMe = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
					R.drawable.bg_tab_nonland));
			TileMe.setTileModeX(Shader.TileMode.MIRROR);
			TileMe.setTileModeY(Shader.TileMode.MIRROR);

			findViewById(R.id.inc_tabs).setBackgroundDrawable(TileMe);
		}
		// overridePendingTransition(android.R.anim.fade_in,
		// android.R.anim.fade_out);

		preferences = DSATabApplication.getPreferences();

		String orientation = preferences.getString(DsaPreferenceActivityHC.KEY_SCREEN_ORIENTATION,
				DsaPreferenceActivity.DEFAULT_SCREEN_ORIENTATION);

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
		viewPagerAdapter = new TabPagerAdapter(this, getSupportFragmentManager(), getHeroConfiguration());
		viewPager.setAdapter(viewPagerAdapter);

		tabLayout = (LinearLayout) findViewById(R.id.gen_tab_layout);

		if (savedInstanceState != null) {
			tabInfo = savedInstanceState.getParcelable(KEY_TAB_INFO);
		}

		if (tabInfo == null && getIntent() != null) {
			tabInfo = getIntent().getParcelableExtra(INTENT_TAB_INFO);
		}

		setupDiceSilder();
		tabInfo = refreshTabInfo();
		setupTabs();

		if (savedInstanceState == null) {
			Debug.verbose("New instance setup tabs");
			showTab(tabInfo);
		} else {
			Debug.verbose("Old instance keep tabs");
			showTab(tabInfo);
		}

		if (!showNewsInfoPopup())
			showTipPopup();

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

	private void updateTab(ImageButton tabButton, TabInfo tabInfo) {
		tabButton.setOnClickListener(this);
		tabButton.setTag(tabInfo);
		tabButton.setImageResource(tabInfo.getTabResourceId());
		registerForIconContextMenu(tabButton);
	}

	private ImageButton createTab(LayoutInflater inflater, TabInfo tabInfo) {
		ImageButton tabButton = (ImageButton) inflater.inflate(R.layout.hero_tab, tabLayout, false);
		updateTab(tabButton, tabInfo);
		return tabButton;
	}

	private HeroConfiguration getHeroConfiguration() {
		HeroConfiguration tabConfig = null;
		if (getHero() != null) {
			tabConfig = getHero().getHeroConfiguration();
		}

		return tabConfig;
	}

	/**
	 * 
	 */
	private void setupTabs() {

		if (getHeroConfiguration() == null) {
			tabLayout.removeAllViews();
			return;
		}

		if (viewPagerAdapter == null) {
			viewPagerAdapter = new TabPagerAdapter(this, getSupportFragmentManager(), getHeroConfiguration());
			viewPager.setAdapter(viewPagerAdapter);
		} else {
			viewPagerAdapter.setConfiguration(getHeroConfiguration());
		}
		viewPager.setOnPageChangeListener(this);

		LayoutInflater inflater = LayoutInflater.from(this);

		List<TabInfo> tabs = getHeroConfiguration().getTabs();
		int tabCount = tabs.size();

		for (int i = 0; i < tabCount; i++) {
			ImageButton tabButton = (ImageButton) tabLayout.getChildAt(i);
			TabInfo tabInfo = tabs.get(i);

			if (tabButton != null) {
				updateTab(tabButton, tabInfo);
			} else {
				tabButton = createTab(inflater, tabInfo);
				tabLayout.addView(tabButton, i);
			}
			tabButton.setEnabled(getHero() != null);
			tabButton.setSelected(tabInfo == this.tabInfo);
		}

		// remove tabs if there are too much
		for (int i = tabLayout.getChildCount() - 1; i >= tabCount; i--) {
			tabLayout.removeViewAt(i);
		}

		return;

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

							Debug.verbose("3 New tab found with same primary clazz or secondar clazz :" + tab);
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

	protected boolean showTab(int index) {

		if (index >= 0 && index < getHeroConfiguration().getTabs().size()) {
			viewPager.setCurrentItem(index, false);
			return true;
		} else {
			return false;
		}
	}

	protected boolean showTab(TabInfo newTabInfo) {

		if (newTabInfo != null) {
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
				showTab(tabLayout.indexOfChild(v));
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

	private void setTabsEnabled(boolean enabled) {
		int count = tabLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			tabLayout.getChildAt(i).setEnabled(enabled);
		}
	}

	protected void onHeroLoaded(Hero hero) {

		if (hero == null) {
			Toast.makeText(this, "Error: Trying to load empty hero. Please contact developer!", Toast.LENGTH_LONG);
			setTabsEnabled(false);
			return;
		} else {
			setTabsEnabled(true);
		}

		TabInfo oldInfo = tabInfo;
		tabInfo = refreshTabInfo();
		setupTabs();

		if (tabInfo != oldInfo) {
			showTab(tabInfo);
		}

		for (BaseFragment fragment : getVisibleFragments()) {
			fragment.loadHero(hero);
		}

	}

	protected void onHeroUnloaded(Hero hero) {

		for (BaseFragment fragment : getVisibleFragments()) {
			fragment.unloadHero(hero);
		}

	}

	private List<BaseFragment> getPageFragments() {
		LinkedList<BaseFragment> visibleFragments = new LinkedList<BaseFragment>();

		if (tabInfo != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			for (int i = 0; i < tabInfo.getTabCount(); i++) {
				BaseFragment f = (BaseFragment) fragmentManager.findFragmentByTag(tabInfo.getFragmentTagName(i));
				if (f != null && f.isVisible())
					visibleFragments.add(f);
			}
		}

		return visibleFragments;
	}

	private List<BaseFragment> getVisibleFragments() {
		LinkedList<BaseFragment> visibleFragments = new LinkedList<BaseFragment>();
		FragmentManager fragmentManager = getSupportFragmentManager();
		if (tabInfo != null) {
			for (int i = 0; i < tabInfo.getTabCount(); i++) {
				BaseFragment f = (BaseFragment) fragmentManager.findFragmentByTag(tabInfo.getFragmentTagName(i));
				if (f != null && f.isVisible())
					visibleFragments.add(f);
			}
		}

		BaseFragment f = (BaseFragment) fragmentManager.findFragmentByTag(AttributeListFragment.TAG);
		if (f != null && f.isAdded())
			visibleFragments.add(f);

		return visibleFragments;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseFragmentActivity#onCreateIconContextMenu(android
	 * .view.Menu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public Object onCreateIconContextMenu(Menu menu, View v, ContextMenuInfo menuInfo) {
		Object info = super.onCreateIconContextMenu(menu, v, menuInfo);

		if (v.getTag() instanceof TabInfo) {
			getMenuInflater().inflate(R.menu.tab_menu, menu);
			return v;
		}

		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseFragmentActivity#onIconContextItemSelected(android
	 * .view.MenuItem, java.lang.Object)
	 */
	@Override
	public void onIconContextItemSelected(MenuItem item, Object info) {

		switch (item.getItemId()) {
		case R.id.option_add: {
			ImageButton selectedTab = (ImageButton) info;
			Intent intent = new Intent(this, TabEditActivity.class);
			intent.putExtra(TabEditActivity.INTENT_TAB_INDEX, tabLayout.indexOfChild(selectedTab));
			startActivityForResult(intent, ACTION_ADD_TAB);
			break;
		}
		case R.id.option_delete: {
			ImageButton selectedTab = (ImageButton) info;
			if (selectedTab != null) {
				tabLayout.removeView(selectedTab);
				TabInfo selectedInfo = (TabInfo) selectedTab.getTag();
				getHeroConfiguration().getTabs().remove(selectedInfo);

				viewPagerAdapter.notifyDataSetChanged();
			}
			break;
		}
		case R.id.option_edit: {
			ImageButton selectedTab = (ImageButton) info;
			if (selectedTab != null) {

				TabInfo selectedInfo = (TabInfo) selectedTab.getTag();
				Intent intent = new Intent(this, TabEditActivity.class);
				intent.putExtra(TabEditActivity.INTENT_ICON, selectedInfo.getTabResourceId());
				intent.putExtra(TabEditActivity.INTENT_PRIMARY_CLASS, selectedInfo.getPrimaryActivityClazz());
				intent.putExtra(TabEditActivity.INTENT_SECONDARY_CLASS, selectedInfo.getSecondaryActivityClazz());
				intent.putExtra(TabEditActivity.INTENT_TAB_INDEX, getHeroConfiguration().getTabs()
						.indexOf(selectedInfo));

				startActivityForResult(intent, ACTION_EDIT_TAB);
			}
			break;

		}
		case R.id.option_tab_reset: {
			getHeroConfiguration().reset();
			setupTabs();
			break;
		}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		Hero hero = getHero();
		menu.findItem(R.id.option_save_hero).setEnabled(hero != null);

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
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				startActivityForResult(new Intent(this, DsaPreferenceActivity.class), ACTION_PREFERENCES);
			} else {
				startActivityForResult(new Intent(this, DsaPreferenceActivityHC.class), ACTION_PREFERENCES);
			}
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
							FilterDialog.PREF_KEY_TALENT_UNUSED, false));

					ListFilterSettings spellSettings = new ListFilterSettings(pref.getBoolean(
							FilterDialog.PREF_KEY_SPELL_FAVORITE, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SPELL_NORMAL, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SPELL_UNUSED, false));

					ListFilterSettings artSettings = new ListFilterSettings(pref.getBoolean(
							FilterDialog.PREF_KEY_ART_FAVORITE, true), pref.getBoolean(
							FilterDialog.PREF_KEY_ART_NORMAL, true), pref.getBoolean(FilterDialog.PREF_KEY_ART_UNUSED,
							false));

					FightFilterSettings fightSettings = new FightFilterSettings(pref.getBoolean(
							FilterDialog.PREF_KEY_SHOW_ARMOR, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SHOW_MODIFIER, true), pref.getBoolean(
							FilterDialog.PREF_KEY_SHOW_EVADE, false));

					FragmentManager fragmentManager = getSupportFragmentManager();
					for (int i = 0; i < tabInfo.getTabCount(); i++) {
						BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(tabInfo
								.getFragmentTagName(i));
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
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_TAB_INFO, tabInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey(KEY_TAB_INFO))
			tabInfo = savedInstanceState.getParcelable(KEY_TAB_INFO);

	}

	protected void showHeroChooser() {
		startActivityForResult(new Intent(MainActivity.this, HeroChooserActivity.class), ACTION_CHOOSE_HERO);
	}
}
