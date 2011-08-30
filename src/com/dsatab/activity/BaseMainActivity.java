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
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.dsatab.AnalyticsManager;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.TabConfiguration;
import com.dsatab.TabInfo;
import com.dsatab.common.HeroExchange;
import com.dsatab.common.Util;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.Probe;
import com.dsatab.data.Value;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.view.DiceSlider;
import com.dsatab.view.FlingableLinearLayout;
import com.dsatab.view.FlingableLinearLayout.OnFlingListener;
import com.dsatab.view.InlineEditDialog;
import com.dsatab.view.InlineEditFightDialog;
import com.dsatab.view.LiteInfoDialog;
import com.dsatab.view.TipOfTheDayDialog;
import com.dsatab.view.listener.ShakeListener;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.VersionInfoDialog;

public class BaseMainActivity extends FragmentActivity implements OnClickListener, OnSharedPreferenceChangeListener,
		OnDismissListener, OnFlingListener {

	protected static final String INTENT_TAB_INFO = "tabInfo";

	public static final String PREF_LAST_HERO = "LAST_HERO";

	public static final int ACTION_PREFERENCES = 1000;

	private static final int ACTION_EDIT_TAB = 1001;

	private static final int ACTION_ADD_TAB = 1002;

	protected static final int ACTION_CHOOSE_HERO = 1004;

	private static final String KEY_TAB_INFO = "tabInfo";

	protected SharedPreferences preferences;

	protected LiteInfoDialog liteFeatureTeaser;

	protected List<BaseFragment> fragments;

	private DiceSlider diceSlider;

	private ShakeListener mShaker;

	private LinearLayout tabLayout;

	private RelativeLayout relMainLayout;

	private TabInfo tabInfo;

	private ImageButton selectedTab;

	private FlingableLinearLayout tabStub;

	private static int tabScrollOffset = 0;

	protected boolean tabFlingEnabled = true;

	private VersionInfoDialog newsDialog;

	public class EditListener implements View.OnClickListener, View.OnLongClickListener {

		public void onClick(View v) {

			Value value = null;
			if (v.getTag(R.id.TAG_KEY_VALUE) instanceof Value) {
				value = (Value) v.getTag(R.id.TAG_KEY_VALUE);
			} else if (v.getTag() instanceof Value) {
				value = (Value) v.getTag();
			}

			if (value != null) {
				showEditPopup(value);
			}

		}

		public boolean onLongClick(View v) {
			Value value = null;
			if (v.getTag(R.id.TAG_KEY_VALUE) instanceof Value) {
				value = (Value) v.getTag(R.id.TAG_KEY_VALUE);
			} else if (v.getTag() instanceof Value) {
				value = (Value) v.getTag();
			}

			if (value != null) {
				showEditPopup(value);
				return true;
			}
			return false;

		}

	}

	public class ProbeListener implements View.OnClickListener, View.OnLongClickListener {

		public void onClick(View v) {

			Probe probe = null;

			if (v.getTag(R.id.TAG_KEY_PROBE) instanceof Probe) {
				probe = (Probe) v.getTag(R.id.TAG_KEY_PROBE);
			} else if (v.getTag() instanceof Probe) {
				probe = (Probe) v.getTag();
			}

			if (probe != null) {
				checkProbe(probe);
			}

		}

		public boolean onLongClick(View v) {
			Probe probe = null;

			if (v.getTag(R.id.TAG_KEY_PROBE) instanceof Probe) {
				probe = (Probe) v.getTag(R.id.TAG_KEY_PROBE);
			} else if (v.getTag() instanceof Probe) {
				probe = (Probe) v.getTag();
			}

			if (probe != null) {
				checkProbe(probe);
				return true;
			}
			return false;
		}

	}

	protected ProbeListener probeListener = new ProbeListener();

	protected EditListener editListener = new EditListener();

	// protected GestureDetector gestureDetector = new GestureDetector(new
	// GestureDetector.SimpleOnGestureListener() {

	// private static final int SWIPE_MIN_DISTANCE = 120;
	// private static final int SWIPE_MAX_OFF_PATH = 250;
	// private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	// public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	// float velocityY) {
	// try {
	// if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	// return false;
	// if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE &&
	// Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	// showNextTab();
	// return true;
	// } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE &&
	// Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	// showPreviousTab();
	// return true;
	// }
	// } catch (Exception e) {
	// Debug.error(e);
	// }
	// return false;

	// 10 = fudge by experimentation

	// };
	//
	// });

	/**
	 * 
	 */
	public BaseMainActivity() {
		fragments = new LinkedList<BaseFragment>();
	}

	public Hero getHero() {
		return DSATabApplication.getInstance().getHero();
	}

	public final void loadHero(String heroPath) {

		Hero oldHero = DSATabApplication.getInstance().getHero();
		if (oldHero != null)
			onHeroUnloaded(oldHero);

		Hero hero = DSATabApplication.getInstance().getHero(heroPath);
		if (hero != null) {
			onHeroLoaded(hero);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.FlingableLinearLayout.OnFlingListener#onFling(boolean)
	 */
	@Override
	public void onFling(boolean right) {
		if (right)
			showNextTab();
		else
			showPreviousTab();

	}

	public void showDiceSlider() {
		if (diceSlider.getParent() == null) {
			relMainLayout.addView(diceSlider);
			diceSlider.getHandle().startAnimation(AnimationUtils.makeInChildBottomAnimation(this));

		}
	}

	public void hideDiceSlider() {
		if (diceSlider.getParent() != null) {
			relMainLayout.removeView(diceSlider);
			diceSlider.getHandle().startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

		}
	}

	private void loadHero() {
		if (getLastNonConfigurationInstance() instanceof Hero) {
			onHeroLoaded((Hero) getLastNonConfigurationInstance());
		} else if (DSATabApplication.getInstance().getHero() != null) {
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

	public void addFragment(BaseFragment fragment) {
		fragments.add(fragment);
	}

	public void removeFragment(BaseFragment fragment) {
		fragments.remove(fragment);
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
			TabInfo info = (TabInfo) selectedTab.getTag();
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

		} else if (requestCode == ACTION_ADD_TAB && resultCode == RESULT_OK) {

			int index = -1;
			if (selectedTab != null) {
				TabInfo info = (TabInfo) selectedTab.getTag();
				index = getTabConfiguration().getTabs().indexOf(info);
			}

			int icon = data.getIntExtra(TabEditActivity.INTENT_ICON, 0);
			Class<? extends BaseFragment> class1 = (Class<? extends BaseFragment>) data
					.getSerializableExtra(TabEditActivity.INTENT_PRIMARY_CLASS);
			Class<? extends BaseFragment> class2 = (Class<? extends BaseFragment>) data
					.getSerializableExtra(TabEditActivity.INTENT_SECONDARY_CLASS);

			TabInfo newInfo = new TabInfo(class1, class2, icon);
			newInfo.setDiceSlider(data.getBooleanExtra(TabEditActivity.INTENT_DICE_SLIDER, true));
			LayoutInflater inflater = LayoutInflater.from(this);
			View tab = createTab(inflater, newInfo);
			tab.setEnabled(true);
			if (index >= 0) {
				tabLayout.addView(tab, index);
				getTabConfiguration().getTabs().add(index, newInfo);
			} else {
				tabLayout.addView(tab);
				getTabConfiguration().getTabs().add(newInfo);
			}

			selectedTab = null;
		} else if (requestCode == ACTION_CHOOSE_HERO && resultCode == RESULT_OK) {
			String heroPath = data.getStringExtra(HeroChooserActivity.INTENT_NAME_HERO_PATH);
			Debug.verbose("HeroChooserActivity returned with path:" + heroPath);
			loadHero(heroPath);
		} else if (requestCode == ACTION_PREFERENCES) {

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

			if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
				registerShakeDice();
			} else {
				unregisterShakeDice();
			}

			String orientation = preferences.getString(DsaPreferenceActivity.KEY_SCREEN_ORIENTATION,
					DsaPreferenceActivity.DEFAULT_SCREEN_ORIENTATION);
			if (DsaPreferenceActivity.SCREEN_ORIENTATION_LANDSCAPE.equals(orientation)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else if (DsaPreferenceActivity.SCREEN_ORIENTATION_PORTRAIT.equals(orientation)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if (DsaPreferenceActivity.SCREEN_ORIENTATION_AUTO.equals(orientation)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}

			if (getHero() == null) {
				showHeroChooser();
			}
		}

		for (BaseFragment fragment : fragments) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
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
	}

	public ProbeListener getProbeListener() {
		return probeListener;
	}

	public EditListener getEditListener() {
		return editListener;
	}

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent ev) {
	// if (tabFlingEnabled) {
	// super.dispatchTouchEvent(ev);
	// return gestureDetector.onTouchEvent(ev);
	// } else {
	// return super.dispatchTouchEvent(ev);
	// }
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab_view);

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
			registerShakeDice();
		}

		Configuration configuration = getResources().getConfiguration();

		String orientation = preferences.getString(DsaPreferenceActivity.KEY_SCREEN_ORIENTATION,
				DsaPreferenceActivity.DEFAULT_SCREEN_ORIENTATION);

		if (DsaPreferenceActivity.SCREEN_ORIENTATION_LANDSCAPE.equals(orientation)
				&& configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
			Debug.verbose("Setting landscape");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (DsaPreferenceActivity.SCREEN_ORIENTATION_PORTRAIT.equals(orientation)
				&& configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Debug.verbose("Setting portrait");
		} else if (DsaPreferenceActivity.SCREEN_ORIENTATION_AUTO.equals(orientation)
				&& getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			Debug.verbose("Setting sensor");
		}

		tabLayout = (LinearLayout) findViewById(R.id.gen_tab_layout);

		LinearLayout tabStubs = (LinearLayout) findViewById(R.id.inc_stub);
		if (tabStubs instanceof FlingableLinearLayout) {
			tabStub = (FlingableLinearLayout) tabStubs;
			tabStub.setOnFlingListener(this);
		}

		if (savedInstanceState != null) {
			tabInfo = savedInstanceState.getParcelable(KEY_TAB_INFO);
		}

		if (tabInfo == null && getIntent() != null) {
			tabInfo = getIntent().getParcelableExtra(INTENT_TAB_INFO);
		}

		setupDiceSilder();
		setupTabs();

		if (savedInstanceState == null) {
			Debug.verbose("New instance setup tabs");
			showTab(tabInfo);
		} else {
			Debug.verbose("Old instance keep tabs");
			showTab(tabInfo);
		}

		preferences.registerOnSharedPreferenceChangeListener(this);

		if (!showNewsInfoPopup())
			showTipPopup();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnDismissListener#onDismiss(android.content
	 * .DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {

		if (dialog == newsDialog) {
			newsDialog = null;
			showTipPopup();
		}
	}

	private boolean showNewsInfoPopup() {

		if (VersionInfoDialog.newsShown)
			return false;

		newsDialog = new VersionInfoDialog(this);
		newsDialog.setDonateContentId(R.raw.donate);
		newsDialog.setDonateVersion(DSATabApplication.getInstance().isLiteVersion());
		newsDialog.setTitle(R.string.news_title);
		newsDialog.setRawClass(R.raw.class);
		newsDialog.setIcon(R.drawable.icon);
		newsDialog.setOnDismissListener(this);
		if (newsDialog.hasContent()) {
			newsDialog.show();
			return true;
		} else {
			return false;
		}

	}

	private boolean showTipPopup() {

		if (TipOfTheDayDialog.tipShown)
			return false;

		TipOfTheDayDialog dialog = new TipOfTheDayDialog(this);
		dialog.setOnDismissListener(this);
		dialog.show();

		TipOfTheDayDialog.tipShown = true;
		return true;

	}

	private void updateTab(ImageButton tabButton, TabInfo tabInfo) {
		tabButton.setOnClickListener(this);
		tabButton.setTag(tabInfo);
		tabButton.setImageResource(tabInfo.getTabResourceId());
		registerForContextMenu(tabButton);
	}

	private ImageButton createTab(LayoutInflater inflater, TabInfo tabInfo) {
		ImageButton tabButton = (ImageButton) inflater.inflate(R.layout.hero_tab, tabLayout, false);
		updateTab(tabButton, tabInfo);
		return tabButton;
	}

	private TabConfiguration getTabConfiguration() {
		TabConfiguration tabConfig = null;
		if (getHero() != null) {
			tabConfig = getHero().getTabConfiguration();
		}

		return tabConfig;
	}

	/**
	 * 
	 */
	private void setupTabs() {

		if (getTabConfiguration() == null) {
			tabLayout.removeAllViews();
			return;
		}

		LayoutInflater inflater = LayoutInflater.from(this);

		List<TabInfo> tabs = getTabConfiguration().getTabs();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (hasFocus) {
			View scroller = findViewById(R.id.inc_tabs);
			ScrollView scrollView = null;
			HorizontalScrollView hScrollView = null;
			if (scroller instanceof ScrollView)
				scrollView = (ScrollView) scroller;
			if (scroller instanceof HorizontalScrollView)
				hScrollView = (HorizontalScrollView) scroller;

			if (scrollView != null)
				scrollView.scrollTo(0, tabScrollOffset);

			if (hScrollView != null)
				hScrollView.scrollTo(tabScrollOffset, 0);

		}

		super.onWindowFocusChanged(hasFocus);
	}

	protected void setupDiceSilder() {
		relMainLayout = (RelativeLayout) findViewById(R.id.gen_main_layout);
		diceSlider = (DiceSlider) LayoutInflater.from(this).inflate(R.layout.dice_slider, relMainLayout, false);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) diceSlider.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		relMainLayout.addView(diceSlider);
	}

	protected boolean showNextTab() {
		if (tabInfo != null) {
			int index = getTabConfiguration().getTabs().indexOf(tabInfo);
			return showTab(index + 1);
		} else {
			return false;
		}
	}

	protected boolean showPreviousTab() {
		if (tabInfo != null) {
			int index = getTabConfiguration().getTabs().indexOf(tabInfo);
			return showTab(index - 1);
		} else {
			return false;
		}
	}

	/**
	 * 
	 */
	private TabInfo refreshTabInfo() {

		if (getTabConfiguration() == null || getTabConfiguration().getTabs().isEmpty()) {
			tabInfo = null;
			return null;
		}

		// if we have an existing tabinfo check if it's upto date
		if (tabInfo != null) {
			// check wether tabinfo is uptodate (within current tabconfig
			if (getTabConfiguration().getTabs().contains(tabInfo))
				return tabInfo;

			// look for tabinfo with same activities
			for (TabInfo tab : getTabConfiguration().getTabs()) {

				if (Util.equalsOrNull(tab.getPrimaryActivityClazz(), tabInfo.getPrimaryActivityClazz())
						&& Util.equalsOrNull(tab.getSecondaryActivityClazz(), tabInfo.getSecondaryActivityClazz())) {
					return tab;
				}
			}

			// if we have portraitmode and a secondary clazz this means we
			// switched from landscape here, in this case we look for a tabinfo
			// with the primary ctivityclass and use this one
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
					&& tabInfo.getSecondaryActivityClazz() != null) {

				Class<? extends BaseFragment> activityClazz = tabInfo.getPrimaryActivityClazz();
				if (activityClazz != null) {
					for (TabInfo tab : getTabConfiguration().getTabs()) {
						if (activityClazz.equals(tab.getPrimaryActivityClazz())) {
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
					for (TabInfo tab : getTabConfiguration().getTabs()) {
						if (activityClazz.equals(tab.getPrimaryActivityClazz())
								|| activityClazz.equals(tab.getSecondaryActivityClazz())) {
							return tab;

						}
					}
				}
			}
		}

		// last resort set tabinfo to first one if no matching one is found
		return getTabConfiguration().getTab(0);
	}

	protected boolean showTab(int index) {

		if (index >= 0 && index < getTabConfiguration().getTabs().size()) {
			TabInfo tabInfo = getTabConfiguration().getTab(index);
			return showTab(tabInfo);
		} else {
			return false;
		}
	}

	protected boolean showTab(TabInfo newTabInfo) {

		if (newTabInfo != null) {
			// unselect old tab
			if (tabInfo != null) {
				View oldTab = tabLayout.findViewWithTag(tabInfo);
				if (oldTab != null) {
					oldTab.setSelected(false);
				}
			}
			setFragments(newTabInfo.getPrimaryActivityClazz(), newTabInfo.getSecondaryActivityClazz());
			tabInfo = newTabInfo;

			// select new one
			View newTab = tabLayout.findViewWithTag(newTabInfo);
			if (newTab != null) {
				newTab.setSelected(true);
			}

			if (tabInfo.isDiceSlider())
				showDiceSlider();
			else
				hideDiceSlider();
			return true;
		} else {
			return false;
		}
	}

	private void setFragments(Class<? extends BaseFragment> primaryFragmentClazz,
			Class<? extends BaseFragment> secondaryFragmentClazz) {

		BaseFragment primary = null;
		BaseFragment secondary = null;
		try {
			if (primaryFragmentClazz != null) {
				primary = primaryFragmentClazz.newInstance();
			}
			if (secondaryFragmentClazz != null) {
				secondary = secondaryFragmentClazz.newInstance();
			}
			setFragments(primary, secondary);
		} catch (InstantiationException e) {
			Debug.error(e);
		} catch (IllegalAccessException e) {
			Debug.error(e);
		}

	}

	protected void setFragments(BaseFragment primaryFragment, BaseFragment secondaryFragment) {

		FragmentManager fragmentManager = getSupportFragmentManager();

		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		Fragment oldPrimary = fragmentManager.findFragmentByTag("primary");
		if (oldPrimary != null) {
			transaction.remove(oldPrimary);
		}
		if (primaryFragment != null) {
			transaction.add(R.id.inc_stub, primaryFragment, "primary");
			AnalyticsManager.onEvent(primaryFragment.getClass().getName());
		}

		Fragment oldSecondary = fragmentManager.findFragmentByTag("secondary");
		if (oldSecondary != null) {
			transaction.remove(oldSecondary);
		}
		if (secondaryFragment != null) {
			transaction.add(R.id.inc_stub, secondaryFragment, "secondary");
			AnalyticsManager.onEvent(secondaryFragment.getClass().getName());
		}

		transaction.commitAllowingStateLoss();
	}

	public void onClick(View v) {

		if (v.getTag() instanceof TabInfo) {
			TabInfo tabInfo = (TabInfo) v.getTag();
			showTab(tabInfo);
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

		for (BaseFragment fragment : fragments) {
			fragment.loadHero(hero);
		}

	}

	protected void onHeroUnloaded(Hero hero) {
		for (BaseFragment fragment : fragments) {
			fragment.unloadHero(hero);
		}
	}

	public void showEditPopup(Value value) {

		if (DSATabApplication.getInstance().isLiteVersion()) {
			tease("<strong>Mal eben schnell einen Wert steigern?</strong> Mit der Vollversion von DsaTab können Eigenschaften, Talente, Zauber, Rüstungsschutz und noch vieles mehr einfach und bequem editiert werden. Getätigte Änderungen werden in der XML Datei nachgezogen und können somit auch wieder in die Helden-Software importiert werden, falls notwendig. ");
		} else {

			if (value instanceof CombatMeleeTalent) {
				InlineEditFightDialog inlineEditFightdialog = new InlineEditFightDialog(this, (CombatMeleeTalent) value);
				inlineEditFightdialog.setTitle(value.getName());
				inlineEditFightdialog.show();
			} else {
				InlineEditDialog inlineEditdialog = new InlineEditDialog(this, value);
				inlineEditdialog.setTitle(value.getName());
				inlineEditdialog.show();
			}
		}
	}

	public void showEditPopup(CombatMeleeTalent value) {

		if (DSATabApplication.getInstance().isLiteVersion()) {
			tease("<strong>Mal eben schnell einen Wert steigern?</strong> Mit der Vollversion von DsaTab können Eigenschaften, Talente, Zauber, Rüstungsschutz und noch vieles mehr einfach und bequem editiert werden. Getätigte Änderungen werden in der XML Datei nachgezogen und können somit auch wieder in die Helden-Software importiert werden, falls notwendig. ");
		} else {

			InlineEditFightDialog inlineEditFightdialog = new InlineEditFightDialog(this, value);
			inlineEditFightdialog.setTitle(value.getName());
			inlineEditFightdialog.show();

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

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if (mShaker != null)
			mShaker.pause();

		View scroller = findViewById(R.id.inc_tabs);
		ScrollView scrollView = null;
		HorizontalScrollView hScrollView = null;
		if (scroller instanceof ScrollView)
			scrollView = (ScrollView) scroller;
		if (scroller instanceof HorizontalScrollView)
			hScrollView = (HorizontalScrollView) scroller;

		if (scrollView != null)
			tabScrollOffset = scrollView.getScrollY();

		if (hScrollView != null)
			tabScrollOffset = hScrollView.getScrollX();

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
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getTag() instanceof TabInfo) {
			// TabInfo info = (TabInfo) v.getTag();
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.tab_menu, menu);

			selectedTab = (ImageButton) v;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.option_add:
			startActivityForResult(new Intent(this, TabEditActivity.class), ACTION_ADD_TAB);
			return true;
		case R.id.option_delete:
			if (selectedTab != null) {
				tabLayout.removeView(selectedTab);
				TabInfo selectedInfo = (TabInfo) selectedTab.getTag();
				getTabConfiguration().getTabs().remove(selectedInfo);
			}
			return true;
		case R.id.option_icon:
			if (selectedTab != null) {

				TabInfo selectedInfo = (TabInfo) selectedTab.getTag();
				Intent intent = new Intent(this, TabEditActivity.class);
				intent.putExtra(TabEditActivity.INTENT_ICON, selectedInfo.getTabResourceId());
				intent.putExtra(TabEditActivity.INTENT_PRIMARY_CLASS, selectedInfo.getPrimaryActivityClazz());
				intent.putExtra(TabEditActivity.INTENT_SECONDARY_CLASS, selectedInfo.getSecondaryActivityClazz());

				startActivityForResult(intent, ACTION_EDIT_TAB);
			}
			return true;

		case R.id.option_tab_reset:
			getTabConfiguration().reset();
			setupTabs();
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public void tease(String feature) {
		if (liteFeatureTeaser == null) {
			liteFeatureTeaser = new LiteInfoDialog(this);
			liteFeatureTeaser.setOwnerActivity(this);
		}
		liteFeatureTeaser.setFeature(feature);
		liteFeatureTeaser.show();
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
			startActivityForResult(new Intent(this, DsaPreferenceActivity.class), ACTION_PREFERENCES);
			return true;
		case R.id.option_import_hero: {
			HeroExchange exchange = new HeroExchange(this);

			if (!exchange.isConfigured()) {

				Toast.makeText(this, "Bitte zuerst die Logindaten bei den Heldenaustausch Einstellungen angeben.",
						Toast.LENGTH_LONG).show();

				Intent intent = new Intent(this, DsaPreferenceActivity.class);
				intent.putExtra(DsaPreferenceActivity.INTENT_PREF_SCREEN, DsaPreferenceActivity.SCREEN_EXCHANGE);
				startActivityForResult(intent, BaseMainActivity.ACTION_PREFERENCES);
			}
			exchange.importHero();
			return true;
		}
		case R.id.option_export_hero: {
			HeroExchange exchange = new HeroExchange(this);

			if (!exchange.isConfigured()) {

				Toast.makeText(this, "Bitte zuerst die Logindaten bei den Heldenaustausch Einstellungen angeben.",
						Toast.LENGTH_LONG).show();

				Intent intent = new Intent(this, DsaPreferenceActivity.class);
				intent.putExtra(DsaPreferenceActivity.INTENT_PREF_SCREEN, DsaPreferenceActivity.SCREEN_EXCHANGE);
				startActivityForResult(intent, BaseMainActivity.ACTION_PREFERENCES);
			}
			exchange.exportHero(getHero());
			return true;
		}
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

	protected void showHeroChooser() {

		if (!DSATabApplication.getInstance().hasHeroes()) {
			// --
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Keine Helden gefunden");
			builder.setMessage("Auf der SD-Karte wurden keine Helden-Dateien gefunden. Stell sicher, dass sich unter "
					+ DSATabApplication.getDsaTabPath()
					+ " die als XML Datei exportierten Helden der Helden-Software befinden.");

			builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivityForResult(new Intent(BaseMainActivity.this, DsaPreferenceActivity.class),
							ACTION_PREFERENCES);
				}
			});
			builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					BaseMainActivity.this.finish();
				}

			});
			builder.show();
		} else {
			startActivityForResult(new Intent(BaseMainActivity.this, HeroChooserActivity.class), ACTION_CHOOSE_HERO);
		}
	}
}
