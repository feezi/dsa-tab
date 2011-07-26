package com.dsatab.view;

import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.activity.DsaPreferenceActivity;
import com.dsatab.common.DsaMath;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.Modifier;
import com.dsatab.data.Probe;
import com.dsatab.data.Probe.ProbeType;
import com.dsatab.data.enums.AttributeType;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.NumberPicker;

public class DiceSlider extends SlidingDrawer implements View.OnClickListener {

	private static final int HANDLE_DICE_20 = 1;
	private static final int HANDLE_DICE_6 = 2;
	private static final int HANDLE_MELEE_FAILURE = 3;
	private static final int HANDLE_DISTANCE_FAILURE = 4;

	private TableLayout tblDiceProbe;
	private TextView tfDiceTalent, tfDiceTalentValue, tfDiceProbesAttr, tfDiceProbesAttrValues, tfEffect,
			tfEffectValue;
	private ImageView tfDice20, tfDice6, tfArea;

	private ImageButton info;

	private LinearLayout linDiceResult;

	private int dice20Count, dice6Count;
	private Animation shakeDice20;
	private Animation shakeDice6;

	private DiceHandler mHandler = new DiceHandler();

	private Random rnd = new SecureRandom();

	private NumberFormat effectFormat = NumberFormat.getNumberInstance();

	private NumberFormat probabilityFormat = NumberFormat.getPercentInstance();

	private CombatTalent lastCombatTalent = null;

	private List<Modifier> modifiers;

	private Modifier manualModifer;

	private ProbeInfo probeInfo;

	private SharedPreferences preferences;

	public DiceSlider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		effectFormat.setMaximumFractionDigits(1);
		preferences = DSATabApplication.getPreferences();
	}

	public DiceSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		effectFormat.setMaximumFractionDigits(1);
		preferences = DSATabApplication.getPreferences();
	}

	public void onClick(View v) {
		if (v == tfDice20) {
			rollDice20();
		} else if (v == tfDice6) {
			rollDice6();
		} else if (v == linDiceResult) {
			linDiceResult.removeAllViews();
			linDiceResult.scrollTo(0, 0);
		} else if (v == tfArea && tfArea.getTag() instanceof CombatTalent) {
			CombatTalent talent = (CombatTalent) tfArea.getTag();
			int w20 = rnd.nextInt(20) + 1;
			Toast.makeText(getContext(), "Treffer auf " + talent.getPosition(w20).getName() + " (" + w20 + ")",
					Toast.LENGTH_LONG).show();
		} else if (v == tfDice20) {
			rollDice20();
		}

		if (v.getId() == R.id.dice_probe_table || v.getId() == R.id.dice_info) {

			AlertDialog.Builder builder;
			AlertDialog alertDialog;

			LayoutInflater inflater = LayoutInflater.from(getContext());
			View probeInfoView = inflater.inflate(R.layout.popup_probe_info, null, false);
			LinearLayout linearLayout = (LinearLayout) probeInfoView.findViewById(R.id.popup_probe_layout);

			StyleableSpannableStringBuilder stringBuilder = new StyleableSpannableStringBuilder();

			for (Modifier mod : modifiers) {

				if (mod == manualModifer)
					continue;

				View listItem = inflater.inflate(R.layout.popup_probe_list_item, linearLayout, false);

				TextView text1 = (TextView) listItem.findViewById(R.id.popup_probelist_item_text1);
				text1.setText(mod.getTitle());

				TextView text2 = (TextView) listItem.findViewById(R.id.popup_probelist_item_text2);

				stringBuilder.clear();

				if (mod.getModifier() < 0) {
					stringBuilder.appendColor(Color.RED, Util.toProbe(-mod.getModifier()));
				} else {
					stringBuilder.appendColor(Color.GREEN, Util.toProbe(-mod.getModifier()));
				}
				text2.setText(stringBuilder.toString());

				linearLayout.addView(listItem);
			}

			// manual modifier
			View editListItem = inflater.inflate(R.layout.popup_probe_manual_list_item, linearLayout, false);
			final NumberPicker picker = (NumberPicker) editListItem.findViewById(R.id.popup_probelist_item_text2);
			picker.setRange(-20, 20);
			picker.setNegativeColor(Color.GREEN);
			picker.setPositiveColor(Color.RED);

			if (manualModifer == null) {
				manualModifer = new Modifier(0, "Manuell", "Manuell");
			}
			TextView text1 = (TextView) editListItem.findViewById(R.id.popup_probelist_item_text1);
			text1.setText(manualModifer.getTitle());

			picker.setCurrent(-manualModifer.getModifier());

			linearLayout.addView(editListItem);

			// build
			builder = new AlertDialog.Builder(getContext());
			builder.setView(probeInfoView);
			builder.setTitle("Probenzuschläge");
			builder.setNeutralButton(getContext().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog = builder.create();
			alertDialog.setCanceledOnTouchOutside(true);
			alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					int erschwernis = 0;
					if (picker != null) {
						erschwernis = picker.getCurrent();
					}

					if (manualModifer == null) {
						if (erschwernis != 0) {
							manualModifer = new Modifier(-erschwernis, "Manuell", "Manuell");
						}
					} else {
						manualModifer.setModifier(-erschwernis);
					}

					if (manualModifer != null)
						checkProbe(probeInfo, manualModifer);
					else
						checkProbe(probeInfo);
				}
			});
			alertDialog.show();

		}
	}

	protected void onFinishInflate() {

		View tableView = findViewById(R.id.dice_probe_table);
		tableView.setOnClickListener(this);

		tblDiceProbe = (TableLayout) findViewById(R.id.dice_probe_table);
		tblDiceProbe.setVisibility(View.GONE);

		tfDiceTalent = (TextView) findViewById(R.id.dice_talent);
		tfDiceTalentValue = (TextView) findViewById(R.id.dice_talent_value);
		tfDiceProbesAttr = (TextView) findViewById(R.id.dice_probe);
		tfDiceProbesAttrValues = (TextView) findViewById(R.id.dice_value);

		info = (ImageButton) findViewById(R.id.dice_info);
		info.setOnClickListener(this);
		info.setVisibility(View.INVISIBLE);

		tfEffect = (TextView) findViewById(R.id.dice_effect);
		tfEffectValue = (TextView) findViewById(R.id.dice_effect_value);

		tfDice20 = (ImageView) findViewById(R.id.dice_w20);
		tfDice20.setOnClickListener(this);

		tfDice6 = (ImageView) findViewById(R.id.dice_w6);
		tfDice6.setOnClickListener(this);

		tfArea = (ImageView) findViewById(R.id.dice_area);
		if (tfArea != null) {
			tfArea.setOnClickListener(this);
		}
		linDiceResult = (LinearLayout) findViewById(R.id.dice_dice_result);
		linDiceResult.setOnClickListener(this);

		shakeDice20 = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
		shakeDice6 = AnimationUtils.loadAnimation(getContext(), R.anim.shake);

		setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {

			public void onDrawerClosed() {
				tblDiceProbe.setVisibility(View.GONE);
				info.setVisibility(View.INVISIBLE);
			}
		});

		super.onFinishInflate();
	}

	private void showEffect(boolean successOne, boolean failureTwenty, Double effect, Double probability,
			Integer erschwernis) {

		if (effect != null) {
			tfEffectValue.setVisibility(View.VISIBLE);
			tfEffect.setVisibility(View.VISIBLE);

			tfEffectValue.setText(effectFormat.format(effect));
			if (erschwernis != null)
				tfEffectValue.append(" (" + erschwernis + ")");

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

			if (probability != null && preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_PROBABILITY, false)) {
				tfEffectValue.append(" (" + probabilityFormat.format(probability) + ")");
			}

			if (failureTwenty && effect < 0) {
				if (lastCombatTalent instanceof CombatDistanceTalent)
					mHandler.sendMessageDelayed(Message.obtain(mHandler, HANDLE_DISTANCE_FAILURE), 1000);
				else
					mHandler.sendMessageDelayed(Message.obtain(mHandler, HANDLE_MELEE_FAILURE), 1000);
			}

			if ((effect < 0 && !successOne) || failureTwenty) {
				tfEffectValue.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed));

			} else {
				if (successOne)
					tfEffectValue.setTextColor(DSATabApplication.getInstance().getResources()
							.getColor(R.color.ValueGreen));
				else
					tfEffectValue.setTextColor(Color.WHITE);
			}
		} else {
			tfEffectValue.setVisibility(View.INVISIBLE);
			tfEffect.setVisibility(View.INVISIBLE);
		}
	}

	public void clearDice() {
		// clear any pending w20 still in queue
		mHandler.removeMessages(HANDLE_DICE_20);
		mHandler.removeMessages(HANDLE_DICE_6);
		dice20Count = 0;
		dice6Count = 0;
		lastCombatTalent = null;
		linDiceResult.removeAllViews();
	}

	public Double checkProbe(Hero hero, Probe probe) {
		if (!isOpened())
			animateOpen();

		Debug.verbose("Probe:" + probe);

		clearDice();

		Integer value1 = null;
		Integer value2 = null;
		Integer value3 = null;

		switch (probe.getProbeType()) {
		case ThreeOfThree:
			value1 = probe.getProbeValue(0);
			value2 = probe.getProbeValue(1);
			value3 = probe.getProbeValue(2);
			break;
		case TwoOfThree:
		case One:
			value1 = value2 = value3 = probe.getProbeValue(0);
			break;
		}

		modifiers = hero.getModificator(probe);

		if (probe.getErschwernis() != null) {
			modifiers.add(new Modifier(-1 * probe.getErschwernis(), "Probenerschwernis", null));
		}

		int heroBe = hero.getBe(probe);

		if (heroBe != 0 && !DSATabApplication.getInstance().isLiteVersion()) {
			modifiers.add(new Modifier(-1 * heroBe, "Behinderung " + probe.getBe(), null));
		}

		manualModifer = null;
		probeInfo = new ProbeInfo();
		probeInfo.hero = hero;
		probeInfo.probe = probe;
		probeInfo.value = new Integer[] { value1, value2, value3 };
		probeInfo.be = heroBe;

		return checkProbe(probeInfo);
	}

	private Double checkProbe(ProbeInfo info, Modifier... modificators) {

		probeInfo = info;
		Probe probe = probeInfo.probe;

		for (Modifier mod : modificators) {
			if (!modifiers.contains(mod))
				modifiers.add(mod);
		}
		int modifiersSum = Modifier.sum(modifiers);

		// --
		tblDiceProbe.setVisibility(View.VISIBLE);
		this.info.setVisibility(View.VISIBLE);
		tfDiceTalent.setText(probe.getName());

		if (probe.getProbeBonus() != null) {
			tfDiceTalentValue.setText(Integer.toString(probe.getProbeBonus()));
			if (modifiersSum != 0) {
				tfDiceTalentValue.append(" " + Util.toProbe(modifiersSum));
			}
			tfDiceTalentValue.setVisibility(View.VISIBLE);
		} else {
			tfDiceTalentValue.setVisibility(View.INVISIBLE);
		}

		// if no probe is present and all values are the same display them as
		// bonus
		if (TextUtils.isEmpty(probe.getProbe()) && info.value[0] == info.value[1] && info.value[1] == info.value[2]) {
			tfDiceProbesAttr.setVisibility(View.GONE);
			tfDiceProbesAttrValues.setVisibility(View.GONE);

			tfDiceTalentValue.setText(Util.toString(info.value[0]));

			if (modifiersSum != 0) {
				tfDiceTalentValue.append(" " + Util.toProbe(modifiersSum));
			}

			tfDiceTalentValue.setVisibility(View.VISIBLE);
		} else {
			tfDiceProbesAttr.setText(probe.getProbe());
			tfDiceProbesAttr.setVisibility(View.VISIBLE);

			tfDiceProbesAttrValues.setText(Util.toString(info.value[0]) + "/" + Util.toString(info.value[1]) + "/"
					+ Util.toString(info.value[2]));
			tfDiceProbesAttrValues.setVisibility(View.VISIBLE);
		}

		// special case ini
		if (probe instanceof Attribute) {
			Attribute attribute = (Attribute) probe;
			if (attribute.getType() == AttributeType.ini) {
				if (info.dice[0] == null)
					info.dice[0] = rollDice6();

				double effect = info.hero.getAttributeValue(AttributeType.ini) + info.dice[0] + modifiersSum;
				showEffect(false, false, effect, null, null);

				info.hero.getAttribute(AttributeType.Initiative_Aktuell).setValue((int) effect);
				return effect;
			}
		}

		boolean sucessOne = false, failureTwenty = false;

		Double effect = null;
		Double probability = null;
		Integer erschwernis = null;

		if (info.value[0] != null && info.value[1] != null && info.value[2] != null) {

			effect = 0.0;

			int taw = 0;
			if (probe.getProbeBonus() != null) {
				taw += probe.getProbeBonus();
			}
			taw += modifiersSum;

			int valueModifier = 0;
			if (taw < 0) {
				valueModifier = taw;
			} else {
				effect = new Double(taw);
			}

			// House rule preferences
			ProbeType probeType = probe.getProbeType();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
			if (probeType == ProbeType.TwoOfThree
					&& preferences.getBoolean(DsaPreferenceActivity.KEY_HOUSE_RULES, false) == false) {
				probeType = ProbeType.One;
			}

			switch (probeType) {

			case ThreeOfThree: {

				probability = DsaMath.testTalent(info.value[0], info.value[1], info.value[2], taw);
				Debug.verbose("Change for success is :" + probability);

				if (info.dice[0] == null)
					info.dice[0] = rollDice20(500);

				if (info.dice[1] == null)
					info.dice[1] = rollDice20(1000);

				if (info.dice[2] == null)
					info.dice[2] = rollDice20(1500);

				int effect1 = (info.value[0] + valueModifier) - info.dice[0];
				int effect2 = (info.value[1] + valueModifier) - info.dice[1];
				int effect3 = (info.value[2] + valueModifier) - info.dice[2];

				if (effect1 < 0) {
					Debug.verbose("Dice1 fail result=" + effect1);
					effect += effect1;
				}
				if (effect2 < 0) {
					Debug.verbose("Dice2 fail result=" + effect2);
					effect += effect2;
				}
				if (effect3 < 0) {
					Debug.verbose("Dice3 fail result=" + effect3);
					effect += effect3;
				}
				break;
			}
			case TwoOfThree: {

				probability = DsaMath.testEigen(info.value[0], taw);
				Debug.verbose("Change for success is :" + probability);

				if (info.dice[0] == null) {
					info.dice[0] = rollDice20(500);

					if (info.dice[0] == 1) {
						sucessOne = true;
						info.dice[0] = rollDice20(2000);
					} else if (info.dice[0] == 20) {
						failureTwenty = true;
						info.dice[0] = rollDice20(2000);
					}
				}
				if (info.dice[1] == null)
					info.dice[1] = rollDice20(1000);

				if (info.dice[2] == null)
					info.dice[2] = rollDice20(1500);

				if (info.dice[0] == 1) {
					if (info.successOne == null) {
						info.successOne = true;
						info.dice[0] = rollDice20(2000);
					} else if (info.dice[0] == 20) {
						info.failureTwenty = true;
						info.dice[0] = rollDice20(2000);
					}
				}

				int[] dices = new int[] { info.dice[0], info.dice[1], info.dice[2] };
				Arrays.sort(dices);
				// we only need the two best
				int dice1 = dices[0];
				int dice2 = dices[1];

				if (probe instanceof CombatProbe) {
					lastCombatTalent = ((CombatProbe) probe).getCombatTalent();
				} else if (probe instanceof CombatTalent) {
					lastCombatTalent = (CombatTalent) probe;
				} else if (probe instanceof CombatMeleeAttribute) {
					lastCombatTalent = ((CombatMeleeAttribute) probe).getTalent();
				}

				Debug.verbose("Value Modifier (Be, Wm, Manuell) " + taw);

				// check for success
				int effect1 = info.value[0] - dice1 + taw;
				int effect2 = info.value[1] - dice2 + taw;

				// full success or failure
				if ((effect1 >= 0 && effect2 >= 0) || (effect1 < 0 && effect2 < 0)) {
					effect = (double) effect1 + effect2;
				} else if (effect1 < 0) {
					effect = (double) effect1;
				} else { // effect2 < 0
					effect = (double) effect2;
				}

				effect = (effect / 2.0);

				if (effect >= 0) {
					erschwernis = Math.min(effect1, effect2);
				}
				break;
			}
			case One: {

				if (info.dice[0] == null) {
					info.dice[0] = rollDice20(500);
				}
				if (info.dice[0] == 1) {
					if (info.successOne == null) {
						info.successOne = true;
						info.dice[0] = rollDice20(2000);
					} else if (info.dice[0] == 20) {
						info.failureTwenty = true;
						info.dice[0] = rollDice20(2000);
					}
				}

				if (probe instanceof CombatProbe) {
					lastCombatTalent = ((CombatProbe) probe).getCombatTalent();
				} else if (probe instanceof CombatTalent) {
					lastCombatTalent = (CombatTalent) probe;
				} else if (probe instanceof CombatMeleeAttribute) {
					lastCombatTalent = ((CombatMeleeAttribute) probe).getTalent();
				}

				probability = DsaMath.testEigen(info.value[0], taw);
				Debug.verbose("Change for success is :" + probability);

				Debug.verbose("Value Modifier (Be, Wm) " + taw);

				// check for success
				effect = (double) info.value[0] - info.dice[0] + taw;

				break;
			}
			}
		}

		if (lastCombatTalent != null) {
			tfArea.setVisibility(View.VISIBLE);
			tfArea.setTag(lastCombatTalent);
		} else {
			tfArea.setVisibility(View.GONE);
		}
		showEffect(sucessOne, failureTwenty, effect, probability, erschwernis);

		return effect;
	}

	public int rollDice20(int delay) {
		if (!isOpened())
			animateOpen();

		dice20Count++;

		int dice = rnd.nextInt(20) + 1;

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_ANIM_ROLL_DICE, true)) {

			if (dice20Count == 1) {
				shakeDice20.reset();
				tfDice20.startAnimation(shakeDice20);
			}

			mHandler.sendMessageDelayed(Message.obtain(mHandler, HANDLE_DICE_20, dice), delay);
		} else {
			showDice20(dice);
		}
		return dice;
	}

	public int rollDice20() {
		return rollDice20(1000);
	}

	public int rollDice6(int delay) {
		if (!isOpened())
			animateOpen();

		dice6Count++;

		int dice = rnd.nextInt(6) + 1;

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_ANIM_ROLL_DICE, true)) {
			if (dice6Count == 1) {
				shakeDice6.reset();
				tfDice6.startAnimation(shakeDice6);
			}
			mHandler.sendMessageDelayed(Message.obtain(mHandler, HANDLE_DICE_6, dice), delay);
		} else {
			showDice6(dice);
		}
		return dice;
	}

	public int rollDice6() {
		return rollDice6(1000);
	}

	private void showDice20(int value) {
		TextView res = new TextView(getContext());

		int width = getResources().getDimensionPixelSize(R.dimen.dices_size);
		int padding = getResources().getDimensionPixelSize(R.dimen.dices_padding);

		res.setWidth((int) width);
		res.setHeight((int) width);

		if (rnd.nextBoolean())
			res.setBackgroundResource(R.drawable.w20_empty);
		else
			res.setBackgroundResource(R.drawable.w20_red_empty);

		res.setText(Integer.toString(value));
		res.setTextColor(Color.WHITE);
		res.setTextSize(getResources().getInteger(R.integer.dices_font_size));
		res.setTypeface(Typeface.DEFAULT_BOLD);
		res.setGravity(Gravity.CENTER);
		res.setPadding(padding, 0, padding, 0);
		linDiceResult.addView(res, width, width);
		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_ANIM_ROLL_DICE, true)) {
			res.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.flip_in));
		}

		if (linDiceResult.getWidth() > 0 && linDiceResult.getChildCount() * width > linDiceResult.getWidth()) {
			linDiceResult.removeViewAt(0);
		}
	}

	private void showDice6(int value) {
		ImageView res = new ImageView(getContext());

		int width = getResources().getDimensionPixelSize(R.dimen.dices_size);
		int padding = getResources().getDimensionPixelSize(R.dimen.dices_padding);

		res.setImageResource(Util.getDrawableByName("w6_" + value));
		res.setPadding(padding, 0, padding, 0);
		linDiceResult.addView(res, width, width);
		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_ANIM_ROLL_DICE, true)) {
			res.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.flip_in));
		}

		if (linDiceResult.getWidth() > 0 && linDiceResult.getChildCount() * width > linDiceResult.getWidth()) {
			linDiceResult.removeViewAt(0);
		}
	}

	private String getFailureMelee() {
		int w6 = rnd.nextInt(6) + 1 + rnd.nextInt(6) + 1;

		switch (w6) {
		case 2:
			return "Waffe zerstört";
		case 3:
		case 4:
		case 5:
			return "Sturz";
		case 6:
		case 7:
		case 8:
			return "Stolpern";
		case 9:
		case 10:
			return "Waffe verloren";
		case 11:
			return "Selbst verletzt (TP Waffe)";
		case 12:
			return "Schwerer Eigentreffer (2x TP Waffe)";
		default:
			return "Ungültiger Wert: " + w6;
		}

	}

	private String getFailureDistance() {
		int w6 = rnd.nextInt(6) + 1 + rnd.nextInt(6) + 1;

		switch (w6) {
		case 2:
			return "Waffe zerstört";
		case 3:
			return "Waffe beschädigt";
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return "Fehlschuss";
		case 11:
		case 12:
			return "Kamerad getroffen";
		default:
			return "Ungültiger Wert: " + w6;
		}

	}

	class DiceHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			Integer result = (Integer) msg.obj;

			switch (msg.what) {
			case HANDLE_DICE_6:
				showDice6(result);
				dice6Count--;
				break;
			case HANDLE_DICE_20:
				showDice20(result);
				dice20Count--;
				break;
			case HANDLE_DISTANCE_FAILURE:
				Toast.makeText(getContext(), getFailureDistance(), Toast.LENGTH_LONG).show();
				break;
			case HANDLE_MELEE_FAILURE:
				Toast.makeText(getContext(), getFailureMelee(), Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	class ProbeInfo {
		Integer[] value = new Integer[3];

		Integer[] dice = new Integer[3];

		Hero hero;

		Probe probe;

		Integer be;

		Boolean successOne, failureTwenty;

	}

}
