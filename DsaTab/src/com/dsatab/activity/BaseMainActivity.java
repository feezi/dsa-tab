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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Probe;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.view.DiceSlider;
import com.dsatab.view.InlineEditDialog;
import com.dsatab.view.PurseDialog;
import com.dsatab.view.listener.ShakeListener;
import com.dsatab.view.listener.ValueChangedListener;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public abstract class BaseMainActivity extends BaseMenuActivity implements OnClickListener, ValueChangedListener {

	private DiceSlider diceSlider;

	private ShakeListener mShaker;

	class EditListener implements View.OnClickListener, View.OnLongClickListener {

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

	class ProbeListener implements View.OnClickListener, View.OnLongClickListener {

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

		RelativeLayout relMainLayout = (RelativeLayout) findViewById(R.id.gen_main_layout);
		diceSlider = (DiceSlider) LayoutInflater.from(this).inflate(R.layout.dice_slider, relMainLayout, false);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) diceSlider.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		relMainLayout.addView(diceSlider);

		findViewById(R.id.gen_tab_char).setOnClickListener(this);
		findViewById(R.id.gen_tab_char).setSelected(getClass().equals(MainCharacterActivity.class));
		findViewById(R.id.gen_tab_talents).setOnClickListener(this);
		findViewById(R.id.gen_tab_talents).setSelected(getClass().equals(MainTalentActivity.class));
		findViewById(R.id.gen_tab_magic).setOnClickListener(this);
		findViewById(R.id.gen_tab_magic).setSelected(getClass().equals(MainSpellActivity.class));
		findViewById(R.id.gen_tab_body).setOnClickListener(this);
		findViewById(R.id.gen_tab_body).setSelected(getClass().equals(MainBodyActivity.class));
		findViewById(R.id.gen_tab_fight).setOnClickListener(this);
		findViewById(R.id.gen_tab_fight).setSelected(getClass().equals(MainFightActivity.class));
		findViewById(R.id.gen_tab_coins).setOnClickListener(this);
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.gen_tab_char:
			if (!getClass().equals(MainCharacterActivity.class)) {
				startActivity(new Intent(this, MainCharacterActivity.class));
				finish();
			}
			break;
		case R.id.gen_tab_talents:
			if (!getClass().equals(MainTalentActivity.class)) {
				startActivity(new Intent(this, MainTalentActivity.class));
				finish();
			}
			break;
		case R.id.gen_tab_magic:
			if (!getClass().equals(MainSpellActivity.class)) {
				startActivity(new Intent(this, MainSpellActivity.class));
				finish();
			}
			break;
		case R.id.gen_tab_fight:
			if (!getClass().equals(MainFightActivity.class)) {
				startActivity(new Intent(this, MainFightActivity.class));
				finish();
			}
			break;
		case R.id.gen_tab_body:
			if (!getClass().equals(MainBodyActivity.class)) {
				startActivity(new Intent(this, MainBodyActivity.class));
				finish();
			}
			break;
		case R.id.gen_tab_coins:
			PurseDialog dialog = new PurseDialog(this);
			dialog.show();
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

		if (diceSlider.isOpened()) {
			diceSlider.animateClose();
		} else {
			super.onBackPressed();
		}
	}

	protected void onHeroLoaded(Hero hero) {

		if (hero == null) {
			Toast.makeText(this, "Error: Trying to load empty hero. Please contact developer!", Toast.LENGTH_LONG);
			return;
		}

		if (hero.getSpells().isEmpty()) {
			findViewById(R.id.gen_tab_magic).setVisibility(View.GONE);
		} else {
			findViewById(R.id.gen_tab_magic).setVisibility(View.VISIBLE);
		}

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
		InlineEditDialog inlineEditdialog = new InlineEditDialog(this, value);
		inlineEditdialog.setOnValueChangedListener(this);
		inlineEditdialog.setTitle(value.getName());
		inlineEditdialog.show();
	}

	protected void fillAttributeValue(TextView tv, AttributeType type) {
		if (getHero() == null)
			return;
		Attribute attribute = getHero().getAttribute(type);
		if (attribute != null) {
			Util.setText(tv, attribute);
			tv.setTag(attribute);

			if (!tv.isLongClickable()) {

				if (type == AttributeType.Lebensenergie || type == AttributeType.Karmaenergie
						|| type == AttributeType.Astralenergie || type == AttributeType.Ausdauer
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
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mShaker != null)
			mShaker.resume();
		super.onResume();
	}

}
