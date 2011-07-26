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
package com.dsatab.activity;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.Probe;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.view.DiceSlider;
import com.dsatab.view.InlineEditDialog;
import com.dsatab.view.InlineEditFightDialog;
import com.dsatab.view.PurseDialog;
import com.dsatab.view.listener.ShakeListener;
import com.dsatab.view.listener.ValueChangedListener;
import com.gandulf.guilib.util.Debug;

public abstract class BaseMainActivity extends BaseMenuActivity implements OnClickListener, ValueChangedListener {

	private DiceSlider diceSlider;

	private ShakeListener mShaker;

	private LinearLayout tabLayout;

	private static int tabScrollOffset = 0;

	private List<Class<? extends BaseMainActivity>> tabActivities = Arrays.asList(MainCharacterActivity.class,
			MainTalentActivity.class, MainSpellActivity.class, MainBodyActivity.class, MainFightActivity.class,
			ItemsActivity.class, NotesActivity.class, MapActivity.class);

	protected boolean tabFlingEnabled = true;

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

	protected GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					showNextTab();
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					showPreviousTab();
					return true;
				}
			} catch (Exception e) {
				Debug.error(e);
			}
			return false;

		};

	});

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMenuActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Debug.verbose("onActivityResult request=" + requestCode + " result=" + resultCode);

		if (requestCode == ACTION_PREFERENCES && resultCode == RESULT_OK) {

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

			if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
				registerShakeDice();
			} else {
				unregisterShakeDice();
			}

			if (getHero() == null) {
				showHeroChooser();
			}

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (tabFlingEnabled) {
			super.dispatchTouchEvent(ev);
			return gestureDetector.onTouchEvent(ev);
		} else {
			return super.dispatchTouchEvent(ev);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
			registerShakeDice();
		}

		setupDiceSilder();

		tabLayout = (LinearLayout) findViewById(R.id.gen_tab_layout);

		View tab = findViewById(R.id.gen_tab_char);
		tab.setOnClickListener(this);
		tab.setSelected(getClass().equals(MainCharacterActivity.class));

		tab = findViewById(R.id.gen_tab_talents);
		tab.setOnClickListener(this);
		tab.setSelected(getClass().equals(MainTalentActivity.class));

		tab = findViewById(R.id.gen_tab_magic);
		tab.setOnClickListener(this);
		tab.setSelected(getClass().equals(MainSpellActivity.class));

		tab = findViewById(R.id.gen_tab_body);
		tab.setOnClickListener(this);
		tab.setSelected(getClass().equals(MainBodyActivity.class));

		tab = findViewById(R.id.gen_tab_fight);
		tab.setOnClickListener(this);
		tab.setSelected(getClass().equals(MainFightActivity.class));

		tab = findViewById(R.id.gen_tab_coins);
		tab.setOnClickListener(this);

		tab = findViewById(R.id.gen_tab_items);
		tab.setSelected(getClass().equals(ItemsActivity.class));
		tab.setOnClickListener(this);

		tab = findViewById(R.id.gen_tab_notes);
		tab.setSelected(getClass().equals(NotesActivity.class));
		tab.setOnClickListener(this);

		tab = findViewById(R.id.gen_tab_maps);
		tab.setSelected(getClass().equals(MapActivity.class));
		tab.setOnClickListener(this);

		setTabsEnabled(false);
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
		RelativeLayout relMainLayout = (RelativeLayout) findViewById(R.id.gen_main_layout);
		diceSlider = (DiceSlider) LayoutInflater.from(this).inflate(R.layout.dice_slider, relMainLayout, false);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) diceSlider.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		relMainLayout.addView(diceSlider);
	}

	protected boolean showNextTab() {
		int index = tabActivities.indexOf(getClass());
		int nextIndex = (index + 1) % tabActivities.size();

		Class<?> activity = tabActivities.get(nextIndex);

		if (activity == MainSpellActivity.class && getHero().getSpells().isEmpty()) {
			nextIndex = (nextIndex + 1) % tabActivities.size();
			activity = tabActivities.get(nextIndex);
		}

		startActivity(new Intent(this, activity));
		finish();

		return true;
	}

	protected boolean showPreviousTab() {
		int index = tabActivities.indexOf(getClass());
		int prevIndex = (index - 1) % tabActivities.size();
		if (prevIndex < 0)
			prevIndex = tabActivities.size() - prevIndex;

		Class<?> activity = tabActivities.get(prevIndex);

		if (activity == MainSpellActivity.class && getHero().getSpells().isEmpty()) {
			prevIndex = (prevIndex - 1) % tabActivities.size();
			activity = tabActivities.get(prevIndex);
		}

		startActivity(new Intent(this, activity));
		finish();

		return true;
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.gen_tab_char:
			if (!getClass().equals(MainCharacterActivity.class)) {
				startActivity(new Intent(this, MainCharacterActivity.class));
				AnalyticsManager.onEvent(AnalyticsManager.PAGE_CHARACTER);
				finish();
			}
			break;
		case R.id.gen_tab_talents:
			if (!getClass().equals(MainTalentActivity.class)) {
				startActivity(new Intent(this, MainTalentActivity.class));
				AnalyticsManager.onEvent(AnalyticsManager.PAGE_TALENTS);
				finish();
			}
			break;
		case R.id.gen_tab_magic:
			if (!getClass().equals(MainSpellActivity.class)) {
				startActivity(new Intent(this, MainSpellActivity.class));
				AnalyticsManager.onEvent(AnalyticsManager.PAGE_SPELLS);
				finish();
			}
			break;
		case R.id.gen_tab_fight:
			if (!getClass().equals(MainFightActivity.class)) {
				startActivity(new Intent(this, MainFightActivity.class));
				AnalyticsManager.onEvent(AnalyticsManager.PAGE_FIGHT);
				finish();
			}
			break;
		case R.id.gen_tab_body:
			if (!getClass().equals(MainBodyActivity.class)) {
				startActivity(new Intent(this, MainBodyActivity.class));
				AnalyticsManager.onEvent(AnalyticsManager.PAGE_ARMOR_WOUNDS);
				finish();
			}
			break;
		case R.id.gen_tab_coins:
			PurseDialog dialog = new PurseDialog(this, getHero());
			AnalyticsManager.onEvent(AnalyticsManager.PAGE_PURSE);
			dialog.show();
			break;

		case R.id.gen_tab_items:
			startItems();
			break;
		case R.id.gen_tab_maps:
			startMap();
			break;

		case R.id.gen_tab_notes:
			startNotes();
			break;
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

		if (hero.getSpells().isEmpty()) {
			findViewById(R.id.gen_tab_magic).setVisibility(View.GONE);
		} else {
			findViewById(R.id.gen_tab_magic).setVisibility(View.VISIBLE);
		}

		hero.addValueChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroUnloaded(Hero hero) {
		if (hero != null)
			hero.removeValueChangeListener(this);
	}

	protected void fillAttributesList(View view) {

		fillAttributeValue((TextView) view.findViewById(R.id.talent_mu), AttributeType.Mut);
		fillAttributeValue((TextView) view.findViewById(R.id.talent_kl), AttributeType.Klugheit);
		fillAttributeValue((TextView) view.findViewById(R.id.talent_in), AttributeType.Intuition);
		fillAttributeValue((TextView) view.findViewById(R.id.talent_ch), AttributeType.Charisma);
		fillAttributeValue((TextView) view.findViewById(R.id.talent_ff), AttributeType.Fingerfertigkeit);
		fillAttributeValue((TextView) view.findViewById(R.id.talent_ge), AttributeType.Gewandtheit);
		fillAttributeValue((TextView) view.findViewById(R.id.talent_ko), AttributeType.Konstitution);
		fillAttributeValue((TextView) view.findViewById(R.id.talent_kk), AttributeType.Körperkraft);

		fillAttributeLabel((TextView) view.findViewById(R.id.talent_mu_label), AttributeType.Mut);
		fillAttributeLabel((TextView) view.findViewById(R.id.talent_kl_label), AttributeType.Klugheit);
		fillAttributeLabel((TextView) view.findViewById(R.id.talent_in_label), AttributeType.Intuition);
		fillAttributeLabel((TextView) view.findViewById(R.id.talent_ch_label), AttributeType.Charisma);
		fillAttributeLabel((TextView) view.findViewById(R.id.talent_ff_label), AttributeType.Fingerfertigkeit);
		fillAttributeLabel((TextView) view.findViewById(R.id.talent_ge_label), AttributeType.Gewandtheit);
		fillAttributeLabel((TextView) view.findViewById(R.id.talent_ko_label), AttributeType.Konstitution);
		fillAttributeLabel((TextView) view.findViewById(R.id.talent_kk_label), AttributeType.Körperkraft);

	}

	protected void fillAttribute(View view, Attribute attr) {
		switch (attr.getType()) {
		case Mut:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_mu), AttributeType.Mut);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_mu_label), AttributeType.Mut);
			break;
		case Klugheit:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_kl), AttributeType.Klugheit);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_kl_label), AttributeType.Klugheit);
			break;
		case Intuition:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_in), AttributeType.Intuition);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_in_label), AttributeType.Intuition);
			break;
		case Charisma:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_ch), AttributeType.Charisma);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_ch_label), AttributeType.Charisma);
			break;
		case Fingerfertigkeit:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_ff), AttributeType.Fingerfertigkeit);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_ff_label), AttributeType.Fingerfertigkeit);
			break;
		case Gewandtheit:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_ge), AttributeType.Gewandtheit);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_ge_label), AttributeType.Gewandtheit);
			break;
		case Konstitution:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_ko), AttributeType.Konstitution);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_ko_label), AttributeType.Konstitution);
			break;
		case Körperkraft:
			fillAttributeValue((TextView) view.findViewById(R.id.talent_kk), AttributeType.Körperkraft);
			fillAttributeLabel((TextView) view.findViewById(R.id.talent_kk_label), AttributeType.Körperkraft);
			break;
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

	protected void fillAttributeValue(TextView tv, AttributeType type) {
		fillAttributeValue(tv, type, null);
	}

	protected void fillAttributeValue(TextView tv, AttributeType type, String prefix) {
		if (getHero() == null)
			return;
		Attribute attribute = getHero().getAttribute(type);
		if (attribute != null) {

			Util.setText(tv, attribute, prefix);
			tv.setTag(attribute);

			if (!tv.isLongClickable()) {

				if (type == AttributeType.Lebensenergie || type == AttributeType.Lebensenergie_Total
						|| type == AttributeType.Karmaenergie || type == AttributeType.Karmaenergie_Total
						|| type == AttributeType.Astralenergie || type == AttributeType.Astralenergie_Total
						|| type == AttributeType.Ausdauer || type == AttributeType.Ausdauer_Total
						|| type == AttributeType.Behinderung) {
					tv.setOnClickListener(editListener);
				} else if (type.probable()) {
					tv.setOnClickListener(probeListener);
				}
				tv.setOnLongClickListener(editListener);
			}
		}
	}

	public boolean checkProbe(Probe probe) {
		if (diceSlider != null)
			diceSlider.checkProbe(getHero(), probe);
		return true;
	}

	protected void fillAttributeLabel(TextView tv, AttributeType type) {

		if (!tv.isLongClickable()) {
			if (type == AttributeType.Lebensenergie || type == AttributeType.Karmaenergie
					|| type == AttributeType.Astralenergie || type == AttributeType.Ausdauer
					|| type == AttributeType.Behinderung) {
				tv.setOnClickListener(editListener);
			} else if (type.probable()) {
				tv.setOnClickListener(probeListener);
			}
			tv.setOnClickListener(probeListener);
			tv.setOnLongClickListener(editListener);
		}
		if (getHero() != null) {
			tv.setTag(getHero().getAttribute(type));
		}
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

}
