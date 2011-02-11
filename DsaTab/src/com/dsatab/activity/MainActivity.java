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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.dsatab.R;
import com.dsatab.common.Debug;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Advantage;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.Hero;
import com.dsatab.data.Probe;
import com.dsatab.data.Spell;
import com.dsatab.data.Talent;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.ExpandableTalentAdapter;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.EquippedItem.Hand;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.ArcheryChooserDialog;
import com.dsatab.view.BodyLayout;
import com.dsatab.view.DiceSlider;
import com.dsatab.view.EquippedItemChooserDialog;
import com.dsatab.view.EvadeChooserDialog;
import com.dsatab.view.InlineEditDialog;
import com.dsatab.view.NumberPicker;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.PurseDialog;
import com.dsatab.view.listener.ModifierChangedListener;
import com.dsatab.view.listener.ShakeListener;
import com.dsatab.view.listener.ValueChangedListener;

public class MainActivity extends BaseMenuActivity implements OnClickListener, ValueChangedListener,
		ModifierChangedListener, OnGesturePerformedListener {

	private static final String PREF_KEY_GROUP_EXPANDED = "GROUP_EXPANDED";

	private static final String PREF_SHOW_FEATURE_COMMENTS = "SHOW_COMMENTS";

	private static final String STATE_TAB_INDEX = "TAB_INDEX";

	public static final String PREF_LAST_HERO = "LAST_HERO";

	private static final int CONTEXTMENU_SORT_EQUIPPED_ITEM = 5;
	private static final int CONTEXTMENU_ASSIGN_SECONDARY = 6;
	private static final int CONTEXTMENU_ASSIGN_PRIMARY = 7;
	private static final int CONTEXTMENU_UNASSIGN = 8;

	private static final int CONTEXTMENU_FIGHT_SET_1 = 10;
	private static final int CONTEXTMENU_FIGHT_SET_2 = 11;
	private static final int CONTEXTMENU_FIGHT_SET_3 = 12;

	private static final int CONTEXTMENU_VIEWEQUIPPEDITEM = 13;

	private static final int CONTEXTMENU_COMMENTS_TOGGLE = 14;

	private ViewFlipper viewFlipper;

	private TextView tfName, tfSpecialFeatures, tfExperience, tfLabelAe, tfLabelKe, tfTotalLp, tfTotalAu, tfTotalAe,
			tfTotalKe, tfGs, tfWs;

	private ImageButton tabChar, tabtalents, tabMagic, tabBody, tabFight, tabCoins;

	private View fightAttributesList, spellAttributeList, talentAttributeList, charAttributesList;

	private NumberPicker fightLePicker;
	private LinearLayout fightLpLayout, fightItems, fightModifiers;

	private TableLayout tblCombatAttributes, tblSpell1;

	private ExpandableListView talentList = null;
	private ExpandableTalentAdapter talentAdapter = null;

	private DiceSlider diceSlider;

	private BodyLayout bodyLayout;

	private ArcheryChooserDialog targetChooserDialog;

	private boolean fightItemsOdd = false;

	private Map<Value, TextView[]> tfValues = new HashMap<Value, TextView[]>(50);

	private EvadeChooserDialog ausweichenModificationDialog;

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

	class TargetListener implements View.OnClickListener {

		public void onClick(View v) {
			if (v.getTag() instanceof EquippedItem) {
				EquippedItem item = (EquippedItem) v.getTag();
				targetChooserDialog.setWeapon(item);
				targetChooserDialog.show();
			}
		}
	}

	private TargetListener targetListener = new TargetListener();

	private ProbeListener probeListener = new ProbeListener();

	private EditListener editListener = new EditListener();

	private InlineEditDialog inlineEditdialog;

	private View selectedEquippedItemView;

	private ShakeListener mShaker;

	private GestureLibrary mLibrary;

	private SharedPreferences preferences;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Debug.verbose("onActivityResult request=" + requestCode + " result=" + resultCode);

		if (requestCode == ACTION_PREFERENCES) {

			if (resultCode == RESULT_OK) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

				fillFightItemDescriptions();

				if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
					registerShakeDice();
				} else {
					unregisterShakeDice();
				}

			}

			if (getHero() == null) {
				showHeroChooser();
			}

		} else if (requestCode == ACTION_INVENTORY) {
			fillFightItemDescriptions();
			getHero().resetArmorAttributes();
		}

		super.onActivityResult(requestCode, resultCode, data);
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_PROBE_SHAKE_ROLL_DICE, false)) {
			registerShakeDice();
		}

		// GestureOverlayView gestures = (GestureOverlayView)
		// findViewById(R.id.gestures);
		// gestures.addOnGesturePerformedListener(this);
		//
		// mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		// if (!mLibrary.load()) {
		// finish();
		// }

		RelativeLayout relMainLayout = (RelativeLayout) findViewById(R.id.gen_main_layout);
		diceSlider = (DiceSlider) LayoutInflater.from(this).inflate(R.layout.dice_slider, relMainLayout, false);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) diceSlider.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		relMainLayout.addView(diceSlider);

		viewFlipper = (ViewFlipper) findViewById(R.id.gen_viewflipper);

		viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
		viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);

		tabChar = (ImageButton) findViewById(R.id.gen_tab_char);
		tabtalents = (ImageButton) findViewById(R.id.gen_tab_talents);
		tabMagic = (ImageButton) findViewById(R.id.gen_tab_magic);

		tabBody = (ImageButton) findViewById(R.id.gen_tab_body);
		tabFight = (ImageButton) findViewById(R.id.gen_tab_fight);
		registerForContextMenu(tabFight);
		tabChar.setOnClickListener(this);
		tabtalents.setOnClickListener(this);
		tabMagic.setOnClickListener(this);

		tabCoins = (ImageButton) findViewById(R.id.gen_tab_coins);
		tabCoins.setOnClickListener(this);

		tabBody.setOnClickListener(this);
		tabFight.setOnClickListener(this);

		tfName = (TextView) findViewById(R.id.gen_name);

		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/harrington.ttf");
		tfName.setTypeface(tf);

		tfExperience = (TextView) findViewById(R.id.gen_abp);
		((TableRow.LayoutParams) tfExperience.getLayoutParams()).span = 2;

		talentList = (ExpandableListView) findViewById(R.id.talent_list);

		tblSpell1 = (TableLayout) findViewById(R.id.gen_spell_table1);

		tblCombatAttributes = (TableLayout) findViewById(R.id.gen_combat_attributes);

		tfLabelAe = (TextView) findViewById(R.id.gen_label_ae);
		tfLabelKe = (TextView) findViewById(R.id.gen_label_ke);

		tfTotalAe = (TextView) findViewById(R.id.gen_total_ae);
		tfTotalKe = (TextView) findViewById(R.id.gen_total_ke);
		tfTotalLp = (TextView) findViewById(R.id.gen_total_lp);
		tfTotalAu = (TextView) findViewById(R.id.gen_total_au);

		tfGs = (TextView) findViewById(R.id.gen_gs);
		tfWs = (TextView) findViewById(R.id.gen_ws);

		tfSpecialFeatures = (TextView) findViewById(R.id.gen_specialfeatures);

		inlineEditdialog = new InlineEditDialog(this, null);
		inlineEditdialog.setOnValueChangedListener(this);

		charAttributesList = findViewById(R.id.gen_attributes);
		talentAttributeList = findViewById(R.id.inc_talent_attributes_list);
		spellAttributeList = findViewById(R.id.inc_spell_attributes_list);

		//
		// body
		bodyLayout = (BodyLayout) findViewById(R.id.body_layout);

		// Fight
		fightLePicker = (NumberPicker) findViewById(R.id.fight_le_picker);
		fightLePicker.setOnChangeListener(new NumberPicker.OnChangedListener() {

			@Override
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				if (oldVal != newVal) {
					Attribute le = getHero().getAttribute(AttributeType.Lebensenergie);
					le.setValue(newVal);
					onValueChanged(le);
				}
			}
		});
		fightLpLayout = (LinearLayout) findViewById(R.id.fight_le_layout);
		fightLpLayout.setOnClickListener(editListener);

		fightItems = (LinearLayout) findViewById(R.id.fight_items);
		fightModifiers = (LinearLayout) findViewById(R.id.fight_modifiers);

		fightAttributesList = findViewById(R.id.inc_fight_attributes_list);

		View fightausweichen = findViewById(R.id.inc_fight_ausweichen);
		ImageButton iconLeft = (ImageButton) fightausweichen.findViewById(R.id.icon_left);
		iconLeft.setOnClickListener(probeListener);
		iconLeft.setOnLongClickListener(editListener);
		iconLeft.setImageResource(R.drawable.icon_ausweichen);
		ImageButton iconRight = (ImageButton) fightausweichen.findViewById(R.id.icon_right);
		iconRight.setImageResource(R.drawable.icon_target);
		iconRight.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ausweichenModificationDialog == null) {
					ausweichenModificationDialog = new EvadeChooserDialog(MainActivity.this);
				}
				ausweichenModificationDialog.show();
			}
		});
		// listeners

		// init state
		if (savedInstanceState != null) {
			int tabIndex = savedInstanceState.getInt(STATE_TAB_INDEX, 0);
			viewFlipper.setDisplayedChild(tabIndex);
		}

		if (getLastNonConfigurationInstance() instanceof Hero) {
			onHeroLoaded((Hero) getLastNonConfigurationInstance());
		} else {

			String heroPath = preferences.getString(PREF_LAST_HERO, null);

			if (heroPath != null && new File(heroPath).exists()) {
				Hero hero = DSATabApplication.getInstance().getHero(heroPath);
				if (hero != null) {
					onHeroLoaded(hero);
				}
			} else {
				showHeroChooser();
			}
		}
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

		// We want at least one prediction
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			// We want at least some confidence in the result
			if (prediction.score > 1.0) {
				// Show the spell
				Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return (getHero());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getTag() instanceof EquippedItem) {
			selectedEquippedItemView = v;

			EquippedItem equippedItem = (EquippedItem) v.getTag();
			if (equippedItem.getItem() instanceof Shield) {
				menu.add(0, CONTEXTMENU_ASSIGN_PRIMARY, 0, getString(R.string.menu_assign_main_weapon));
			}
			if (equippedItem.getItem() instanceof Weapon) {
				Weapon weapon = (Weapon) equippedItem.getItem();
				if (!weapon.isTwoHanded()) {
					menu.add(0, CONTEXTMENU_ASSIGN_SECONDARY, 0, getString(R.string.menu_assign_secondary_weapon));
				}
			}

			if (equippedItem.getSecondaryItem() != null) {
				menu.add(0, CONTEXTMENU_UNASSIGN, 1, getString(R.string.menu_unassign_item));
			}

			menu.add(1, CONTEXTMENU_VIEWEQUIPPEDITEM, 3, getString(R.string.menu_view_item));
			menu.add(1, CONTEXTMENU_SORT_EQUIPPED_ITEM, 4, getString(R.string.menu_sort_items));
		} else if (v == tabFight) {
			menu.add(0, CONTEXTMENU_FIGHT_SET_1, 1, getString(R.string.menu_equipment_set, "1"));
			menu.add(0, CONTEXTMENU_FIGHT_SET_2, 2, getString(R.string.menu_equipment_set, "2"));
			menu.add(0, CONTEXTMENU_FIGHT_SET_3, 3, getString(R.string.menu_equipment_set, "3"));
		} else if (v == tfSpecialFeatures) {
			menu.add(0, CONTEXTMENU_COMMENTS_TOGGLE, 0, R.string.menu_show_hide_comments);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CONTEXTMENU_VIEWEQUIPPEDITEM) {
			if (selectedEquippedItemView != null) {

				EquippedItem equippedItem = (EquippedItem) selectedEquippedItemView.getTag();
				String name = equippedItem.getItemName();

				Intent intent = new Intent(this, ItemChooserActivity.class);
				intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_NAME, name);
				startActivity(intent);
			}

		} else if (item.getItemId() == CONTEXTMENU_ASSIGN_PRIMARY) {
			if (selectedEquippedItemView != null) {
				final EquippedItem equippedShield = (EquippedItem) selectedEquippedItemView.getTag();

				EquippedItemChooserDialog dialog = new EquippedItemChooserDialog(this);
				dialog.setEquippedItems(getHero().getEquippedItems(Weapon.class));
				dialog.show();
				dialog.setOnDismissListener(new Dialog.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						EquippedItemChooserDialog equippedDialog = (EquippedItemChooserDialog) dialog;

						if (equippedDialog.getSelectedItem() != null) {
							EquippedItem equippedWeapon = equippedDialog.getSelectedItem();

							// remove 2way relation if old secondary item
							// existed
							if (equippedWeapon.getSecondaryItem() != null
									&& equippedWeapon.getSecondaryItem().getSecondaryItem() != null) {
								Debug.verbose("Removing old weapon sec item " + equippedWeapon.getSecondaryItem());
								equippedWeapon.getSecondaryItem().setSecondaryItem(null);
							}
							if (equippedShield.getSecondaryItem() != null
									&& equippedShield.getSecondaryItem().getSecondaryItem() != null) {
								Debug.verbose("Removing old shield sec item " + equippedWeapon.getSecondaryItem());
								equippedShield.getSecondaryItem().setSecondaryItem(null);
							}

							equippedShield.setSecondaryItem(equippedWeapon);
							equippedWeapon.setSecondaryItem(equippedShield);

							fillFightItemDescriptions();
						}
					}
				});

			}
		} else if (item.getItemId() == CONTEXTMENU_ASSIGN_SECONDARY) {
			if (selectedEquippedItemView != null) {
				final EquippedItem equippedPrimaryWeapon = (EquippedItem) selectedEquippedItemView.getTag();

				EquippedItemChooserDialog dialog = new EquippedItemChooserDialog(this);
				dialog.setEquippedItems(getHero().getEquippedItems(Weapon.class));

				// do not select item itself
				dialog.getEquippedItems().remove(equippedPrimaryWeapon);

				dialog.show();
				dialog.setOnDismissListener(new Dialog.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						EquippedItemChooserDialog equippedDialog = (EquippedItemChooserDialog) dialog;

						if (equippedDialog.getSelectedItem() != null) {

							EquippedItem equippedSecondaryWeapon = equippedDialog.getSelectedItem();

							equippedPrimaryWeapon.setHand(Hand.rechts);
							equippedSecondaryWeapon.setHand(Hand.links);

							// remove 2way relation if old secondary item
							// existed
							if (equippedSecondaryWeapon.getSecondaryItem() != null
									&& equippedSecondaryWeapon.getSecondaryItem().getSecondaryItem() != null) {
								Debug.verbose("Removing old weapon sec item "
										+ equippedSecondaryWeapon.getSecondaryItem());
								equippedSecondaryWeapon.getSecondaryItem().setSecondaryItem(null);
							}
							if (equippedPrimaryWeapon.getSecondaryItem() != null
									&& equippedPrimaryWeapon.getSecondaryItem().getSecondaryItem() != null) {
								Debug.verbose("Removing old shield sec item "
										+ equippedSecondaryWeapon.getSecondaryItem());
								equippedPrimaryWeapon.getSecondaryItem().setSecondaryItem(null);
							}

							equippedPrimaryWeapon.setSecondaryItem(equippedSecondaryWeapon);
							equippedSecondaryWeapon.setSecondaryItem(equippedPrimaryWeapon);

							fillFightItemDescriptions();
						}
					}
				});

			}
		} else if (item.getItemId() == CONTEXTMENU_UNASSIGN) {
			if (selectedEquippedItemView != null) {
				final EquippedItem equippedPrimaryWeapon = (EquippedItem) selectedEquippedItemView.getTag();
				EquippedItem equippedSecondaryWeapon = equippedPrimaryWeapon.getSecondaryItem();

				equippedPrimaryWeapon.setSecondaryItem(null);
				equippedSecondaryWeapon.setSecondaryItem(null);

				fillFightItemDescriptions();
			}
		} else if (item.getItemId() == CONTEXTMENU_SORT_EQUIPPED_ITEM) {
			fillFightItemDescriptions();
		} else if (item.getItemId() == CONTEXTMENU_FIGHT_SET_1) {
			getHero().setActiveSet(0);
			fillFightItemDescriptions();
			getHero().resetArmorAttributes();
		} else if (item.getItemId() == CONTEXTMENU_FIGHT_SET_2) {
			getHero().setActiveSet(1);
			fillFightItemDescriptions();
			getHero().resetArmorAttributes();
		} else if (item.getItemId() == CONTEXTMENU_FIGHT_SET_3) {
			getHero().setActiveSet(2);
			fillFightItemDescriptions();
			getHero().resetArmorAttributes();
		} else if (item.getItemId() == CONTEXTMENU_COMMENTS_TOGGLE) {
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

			showComments = !showComments;
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_FEATURE_COMMENTS, showComments);
			edit.commit();

			fillSpecialFeatures(getHero());
		}

		return super.onContextItemSelected(item);
	}

	public void onClick(View v) {

		if (v == tabChar) {
			if (viewFlipper.getDisplayedChild() != 0)
				viewFlipper.setDisplayedChild(0);
		} else if (v == tabtalents) {
			if (viewFlipper.getDisplayedChild() != 1)
				viewFlipper.setDisplayedChild(1);
		} else if (v == tabMagic) {
			if (viewFlipper.getDisplayedChild() != 2)
				viewFlipper.setDisplayedChild(2);
		} else if (v == tabBody) {
			if (viewFlipper.getDisplayedChild() != 3)
				viewFlipper.setDisplayedChild(3);
		} else if (v == tabFight) {
			if (viewFlipper.getDisplayedChild() != 4)
				viewFlipper.setDisplayedChild(4);
		} else if (v == tabCoins) {
			PurseDialog dialog = new PurseDialog(this);
			dialog.show();
		} else if (v == tfName) {
			PortraitChooserDialog dialog = new PortraitChooserDialog(this);
			dialog.show();
		}
	}

	private void fillAttributeLabel(TextView tv, AttributeType type) {

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

	private void fillAttributeValue(TextView tv, AttributeType type) {
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

	private void fillAttribute(View view, Attribute attr) {
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

	private void fillAttributesList(View view) {

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

	private void fillFightModifierDescription(Modificator item, View itemLayout) {

		fightModifiers.setVisibility(View.VISIBLE);
		if (itemLayout == null) {
			LayoutInflater layoutInflater = getLayoutInflater();
			itemLayout = layoutInflater.inflate(R.layout.fight_sheet_modifier, fightModifiers, false);
			fightModifiers.addView(itemLayout);

			if (fightModifiers.getChildCount() % 2 == 0)
				itemLayout.setBackgroundResource(R.color.RowOdd);
		}
		itemLayout.setTag(item);

		TextView text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
		TextView text2 = (TextView) itemLayout.findViewById(android.R.id.text2);

		if (item != null) {
			text1.setText(item.getModifierName());
			text2.setText(item.getModifierInfo());
		} else {
			text1.setText(null);
			text2.setText(null);
		}
	}

	private void fillFightItemDescriptions() {
		fightItems.removeAllViews();
		Util.sort(getHero().getEquippedItems());
		for (EquippedItem eitem : getHero().getEquippedItems()) {
			fillFightItemDescription(eitem);
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

	private void fillFightItemDescription(EquippedItem equippedItem) {

		Item item = equippedItem.getItem();

		if (item == null)
			return;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (!preferences.getBoolean(DsaPreferenceActivity.KEY_FIGHT_ARMOR_VISIBILITY, true)) {
			if (item instanceof Armor) {
				return;
			}
		}

		LayoutInflater layoutInflater = getLayoutInflater();

		View itemLayout = layoutInflater.inflate(R.layout.fight_sheet_item, fightItems, false);
		itemLayout.setTag(equippedItem);
		if (fightItemsOdd) {
			itemLayout.setBackgroundResource(R.color.RowOdd);
		}

		registerForContextMenu(itemLayout);

		fightItems.addView(itemLayout);

		TextView text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
		TextView text2 = (TextView) itemLayout.findViewById(android.R.id.text2);

		ImageButton iconLeft = (ImageButton) itemLayout.findViewById(R.id.icon_left);
		ImageButton iconRight = (ImageButton) itemLayout.findViewById(R.id.icon_right);

		if (equippedItem.getSecondaryItem() != null
				&& (equippedItem.getSecondaryItem().getItem() instanceof Shield || (equippedItem.getSecondaryItem()
						.getItem() instanceof Weapon && equippedItem.getHand() == Hand.rechts))) {
			// keep odd/even
		} else {
			fightItemsOdd = !fightItemsOdd;
		}
		text1.setText(item.getName());
		text2.setText(item.getInfo());

		if (item instanceof DistanceWeapon) {
			DistanceWeapon distanceWeapon = (DistanceWeapon) item;
			iconLeft.setImageResource(distanceWeapon.getResourceId());

			CombatProbe probe = new CombatProbe(getHero(), equippedItem, true);
			iconRight.setImageResource(R.drawable.icon_target);
			iconRight.setTag(equippedItem);
			iconRight.setOnClickListener(targetListener);
			iconLeft.setTag(probe);
			iconLeft.setOnClickListener(probeListener);
		} else if (item instanceof Shield) {
			iconRight.setImageResource(item.getResourceId());
			iconLeft.setVisibility(View.INVISIBLE);
			iconRight.setTag(new CombatProbe(getHero(), equippedItem, false));
			iconRight.setOnClickListener(probeListener);
		} else if (item instanceof Weapon) {
			Weapon weapon = (Weapon) item;
			iconLeft.setImageResource(weapon.getResourceId());
			iconRight.setImageResource(R.drawable.icon_shield);
			iconLeft.setTag(new CombatProbe(getHero(), equippedItem, true));
			iconLeft.setOnClickListener(probeListener);
			iconRight.setTag(new CombatProbe(getHero(), equippedItem, false));
			iconRight.setOnClickListener(probeListener);
			text2.setText(weapon.getInfo(getHero().getModifiedValue(AttributeType.Körperkraft)));
		} else if (item instanceof Armor) {
			Armor armor = (Armor) item;
			iconLeft.setImageResource(armor.getResourceId());
			iconLeft.setFocusable(false);
			iconRight.setVisibility(View.GONE);
		}
	}

	public boolean checkProbe(Probe probe) {
		diceSlider.checkProbe(getHero(), probe);
		return true;
	}

	public void showEditPopup(Value value) {
		inlineEditdialog.setValue(value);
		inlineEditdialog.setTitle(value.getName());
		inlineEditdialog.show();
	}

	@Override
	public void onModifierAdded(Modificator value) {
		fillFightModifierDescription(value, null);

		tfGs.setText(Util.toString(getHero().getGs()));
	}

	@Override
	public void onModifierRemoved(Modificator value) {
		fightModifiers.removeView(fightModifiers.findViewWithTag(value));

		if (fightModifiers.getChildCount() <= 1)
			fightModifiers.setVisibility(View.GONE);

		tfGs.setText(Util.toString(getHero().getGs()));
	}

	@Override
	public void onModifierChanged(Modificator value) {
		View itemLayout = fightModifiers.findViewWithTag(value);
		fillFightModifierDescription(value, itemLayout);

		tfGs.setText(Util.toString(getHero().getGs()));
	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {
		fightModifiers.removeViews(1, fightModifiers.getChildCount() - 1);
		for (Modificator item : values) {
			fillFightModifierDescription(item, null);
		}
		if (fightModifiers.getChildCount() <= 1)
			fightModifiers.setVisibility(View.GONE);

		tfGs.setText(Util.toString(getHero().getGs()));
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		TextView[] tvs = tfValues.get(value);
		if (tvs != null) {
			for (TextView tf : tvs) {
				Util.setText(tf, value);
			}
		}

		if (value instanceof Talent) {
			talentAdapter.notifyDataSetChanged();
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {

			case Lebensenergie:
				fightLePicker.setRange(value.getMinimum(), value.getMaximum());
				fightLePicker.setCurrent(value.getValue());
				fightLePicker.setDefault(value.getReferenceValue());
				double ratio = getHero().getLeRatio();
				if (ratio < 0.5)
					fightLePicker.getEditText().setTextColor(getResources().getColor(R.color.ValueRed));
				else if (ratio < 1.0)
					fightLePicker.getEditText().setTextColor(getResources().getColor(R.color.ValueBlack));
				else
					fightLePicker.getEditText().setTextColor(getResources().getColor(R.color.ValueGreen));

				fillAttributeValue((TextView) findViewById(R.id.gen_lp), AttributeType.Lebensenergie);
				break;
			case Astralenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
				break;
			case Ausdauer:
				fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
				break;
			case Karmaenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
				break;
			case Magieresistenz:
				fillAttributeValue((TextView) findViewById(R.id.gen_mr), AttributeType.Magieresistenz);
				break;
			case Sozialstatus:
				fillAttributeValue((TextView) findViewById(R.id.gen_so), AttributeType.Sozialstatus);
				break;
			case at:
				fillAttributeValue((TextView) findViewById(R.id.gen_at), AttributeType.at);
				break;
			case pa:
				fillAttributeValue((TextView) findViewById(R.id.gen_pa), AttributeType.pa);
				break;
			case fk:
				fillAttributeValue((TextView) findViewById(R.id.gen_fk), AttributeType.fk);
				break;
			case ini:
				fillAttributeValue((TextView) findViewById(R.id.gen_ini), AttributeType.ini);
				break;
			case Behinderung:
				fillAttributeValue((TextView) findViewById(R.id.gen_be), AttributeType.Behinderung);
				tfGs.setText(Util.toString(getHero().getGs()));
				break;
			case Gewandtheit:
				tfGs.setText(Util.toString(getHero().getGs()));
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Konstitution:
			case Charisma:
				fillAttribute(talentAttributeList, attr);
				fillAttribute(spellAttributeList, attr);
				fillAttribute(fightAttributesList, attr);
				fillAttribute(charAttributesList, attr);
				break;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		if (hero == null) {
			Toast.makeText(this, "Error: Trying to load empty hero. Please contact developer!", Toast.LENGTH_LONG);
			return;
		}
		tfName.setText(hero.getName());
		tfName.setOnClickListener(this);
		tfExperience.setText(Integer.toString(hero.getExperience()));

		fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
		fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
		fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
		fillAttributeValue((TextView) findViewById(R.id.gen_lp), AttributeType.Lebensenergie);
		fillAttributeValue((TextView) findViewById(R.id.gen_mr), AttributeType.Magieresistenz);
		fillAttributeValue((TextView) findViewById(R.id.gen_so), AttributeType.Sozialstatus);

		fillAttributeValue((TextView) findViewById(R.id.gen_at), AttributeType.at);
		fillAttributeValue((TextView) findViewById(R.id.gen_pa), AttributeType.pa);
		fillAttributeValue((TextView) findViewById(R.id.gen_fk), AttributeType.fk);
		fillAttributeValue((TextView) findViewById(R.id.gen_ini), AttributeType.ini);
		fillAttributeValue((TextView) findViewById(R.id.gen_be), AttributeType.Behinderung);

		fillAttributeLabel((TextView) findViewById(R.id.gen_at_label), AttributeType.at);
		fillAttributeLabel((TextView) findViewById(R.id.gen_pa_label), AttributeType.pa);
		fillAttributeLabel((TextView) findViewById(R.id.gen_fk_label), AttributeType.fk);
		fillAttributeLabel((TextView) findViewById(R.id.gen_ini_label), AttributeType.ini);
		fillAttributeLabel((TextView) findViewById(R.id.gen_be_label), AttributeType.Behinderung);

		tfTotalLp.setText(" / " + Integer.toString(hero.getAttribute(AttributeType.Lebensenergie).getReferenceValue()));
		tfTotalAu.setText(" / " + Integer.toString(hero.getAttribute(AttributeType.Ausdauer).getReferenceValue()));

		if (hero.getAttributeValue(AttributeType.Karmaenergie) == null) {
			findViewById(R.id.gen_ke).setVisibility(View.GONE);
			tfLabelKe.setVisibility(View.GONE);
			tfTotalKe.setVisibility(View.GONE);
		} else {
			tfTotalKe.setText(" / "
					+ Integer.toString(hero.getAttribute(AttributeType.Karmaenergie).getReferenceValue()));
		}

		if (hero.getAttributeValue(AttributeType.Astralenergie) == null) {
			findViewById(R.id.gen_ae).setVisibility(View.GONE);
			tfLabelAe.setVisibility(View.GONE);
			tfTotalAe.setVisibility(View.GONE);
		} else {
			tfTotalAe.setText(" / "
					+ Integer.toString(hero.getAttribute(AttributeType.Astralenergie).getReferenceValue()));
		}

		if (hero.getSpells().isEmpty()) {
			tabMagic.setVisibility(View.GONE);
		} else {
			tabMagic.setVisibility(View.VISIBLE);
			loadHeroSpells(hero);
		}

		tfGs.setText(Util.toString(hero.getGs()));

		int[] ws = hero.getWundschwelle();
		tfWs.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);

		((TextView) findViewById(R.id.gen_groesse)).setText(hero.getGroesse() + " cm");
		((TextView) findViewById(R.id.gen_gewicht)).setText(hero.getGewicht() + " Stein");
		((TextView) findViewById(R.id.gen_herkunft)).setText(hero.getHerkunft());
		((TextView) findViewById(R.id.gen_ausbildung)).setText(hero.getAusbildung());
		((TextView) findViewById(R.id.gen_alter)).setText(Util.toString(hero.getAlter()));
		((TextView) findViewById(R.id.gen_haar_augen)).setText(hero.getHaarFarbe() + " / " + hero.getAugenFarbe());
		//
		fillAttributesList(charAttributesList);
		fillAttributesList(talentAttributeList);
		fillAttributesList(spellAttributeList);
		fillAttributesList(fightAttributesList);

		fillSpecialFeatures(hero);
		registerForContextMenu(tfSpecialFeatures);

		loadCombatTalents(hero);

		loadHeroTalents(hero);

		// body
		bodyLayout.setWoundAttributes(hero.getWounds());
		bodyLayout.setArmorAttributes(hero.getArmorAttributes());

		// fight
		Attribute leAttr = hero.getAttribute(AttributeType.Lebensenergie);
		fightLePicker.setTag(leAttr);
		onValueChanged(leAttr);

		fightItems.removeAllViews();
		for (EquippedItem item : hero.getEquippedItems()) {
			fillFightItemDescription(item);
		}

		fillAusweichen();

		// remove all but first
		fightModifiers.removeViews(1, fightModifiers.getChildCount() - 1);
		fightModifiers.setVisibility(View.GONE);
		for (Modificator item : hero.getModifiers()) {
			fillFightModifierDescription(item, null);
		}
		hero.addModifierChangedListener(this);
		hero.addValueChangedListener(this);
		hero.addValueChangedListener(bodyLayout);

		targetChooserDialog = new ArcheryChooserDialog(this);
	}

	/**
	 * @param hero
	 */
	private void fillSpecialFeatures(Hero hero) {

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

		StyleableSpannableStringBuilder stringBuilder = new StyleableSpannableStringBuilder();

		stringBuilder.append(TextUtils.join(", ", hero.getSpecialFeatures()));

		if (!hero.getAdvantages().isEmpty()) {
			stringBuilder.append("\n");
			stringBuilder.appendBold(getString(R.string.advantages));
			stringBuilder.appendBold(": ");

			boolean first = true;
			for (Advantage advantage : hero.getAdvantages()) {

				if (!first) {
					stringBuilder.append(", ");
				} else {
					first = false;
				}
				stringBuilder.append(advantage.getName());
				if (showComments && !TextUtils.isEmpty(advantage.getComment())) {
					stringBuilder.appendColor(Color.GRAY, " (");
					stringBuilder.appendColor(Color.GRAY, advantage.getComment());
					stringBuilder.appendColor(Color.GRAY, ")");
				}

			}

		}

		if (!hero.getDisadvantages().isEmpty()) {
			stringBuilder.append("\n");
			stringBuilder.appendBold(getString(R.string.disadvantages));
			stringBuilder.appendBold(": ");

			boolean first = true;
			for (Advantage advantage : hero.getDisadvantages()) {

				if (!first) {
					stringBuilder.append(", ");
				} else {
					first = false;
				}
				stringBuilder.append(advantage.getName());
				if (showComments && !TextUtils.isEmpty(advantage.getComment())) {
					stringBuilder.appendColor(Color.GRAY, " (");
					stringBuilder.appendColor(Color.GRAY, advantage.getComment());
					stringBuilder.appendColor(Color.GRAY, ")");
				}

			}
		}

		tfSpecialFeatures.setText(stringBuilder.toString());
	}

	public void fillAusweichen() {

		final Attribute ausweichen = getHero().getAttribute(AttributeType.Ausweichen);
		View fightausweichen = findViewById(R.id.inc_fight_ausweichen);
		TextView text1 = (TextView) fightausweichen.findViewById(android.R.id.text1);
		text1.setText(ausweichen.getName());
		final TextView text2 = (TextView) fightausweichen.findViewById(android.R.id.text2);
		text2.setText(getString(R.string.ausweichen_info, ausweichen.getValue(), ausweichen.getErschwernis()));

		if (fightItemsOdd) {
			fightausweichen.setBackgroundResource(R.color.RowOdd);
		}

		ImageButton iconLeft = (ImageButton) fightausweichen.findViewById(R.id.icon_left);
		iconLeft.setTag(ausweichen);

	}

	private void loadCombatTalents(Hero hero2) {

		TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		// fill combat attributes
		tblCombatAttributes.removeAllViews();
		int rowCount = 0;
		TableLayout currentTable = tblCombatAttributes;

		for (CombatMeleeTalent element : getHero().getCombatMeleeTalents()) {
			rowCount++;

			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.combat_talent_row, null);

			TextView talentLabel = (TextView) row.findViewById(R.id.combat_talent_name);
			talentLabel.setText(element.getName());

			TextView talentBe = (TextView) row.findViewById(R.id.combat_talent_be);
			talentBe.setText(element.getType().getBe());

			TextView talentValueAt = (TextView) row.findViewById(R.id.combat_talent_at);
			if (element.getAttack() != null || element.getAttack().getValue() != null) {
				talentValueAt.setText(Integer.toString(element.getAttack().getValue()));
				talentValueAt.setOnClickListener(probeListener);
				talentValueAt.setOnLongClickListener(editListener);

				talentValueAt.setTag(R.id.TAG_KEY_VALUE, element.getAttack());
				talentValueAt.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), element, true));
			}

			tfValues.put(element.getAttack(), new TextView[] { talentValueAt });

			TextView talentValuePa = (TextView) row.findViewById(R.id.combat_talent_pa);

			if (element.getDefense() != null && element.getDefense().getValue() != null) {
				talentValuePa.setText(Integer.toString(element.getDefense().getValue()));
				talentValuePa.setOnClickListener(probeListener);
				talentValuePa.setOnLongClickListener(editListener);
				talentValuePa.setTag(R.id.TAG_KEY_VALUE, element.getDefense());
				talentValuePa.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), element, false));
			}
			tfValues.put(element.getDefense(), new TextView[] { talentValuePa });

			if (rowCount % 2 == 1) {
				row.setBackgroundResource(R.color.RowOdd);
			}

			currentTable.addView(row, tableLayout);
		}

		for (CombatDistanceTalent element : getHero().getCombatDistanceTalents()) {
			rowCount++;

			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.combat_talent_row, null);

			row.setOnClickListener(probeListener);
			row.setOnLongClickListener(editListener);
			row.setTag(R.id.TAG_KEY_VALUE, element);
			row.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), element, true));

			TextView talentLabel = (TextView) row.findViewById(R.id.combat_talent_name);
			talentLabel.setText(element.getName());

			TextView talentBe = (TextView) row.findViewById(R.id.combat_talent_be);
			talentBe.setText(element.getBe());

			TextView talentValueAt = (TextView) row.findViewById(R.id.combat_talent_at);
			talentValueAt.setText(Integer.toString(element.getValue()));
			tfValues.put(element, new TextView[] { talentValueAt });

			TextView talentValuePa = (TextView) row.findViewById(R.id.combat_talent_pa);
			talentValuePa.setVisibility(View.INVISIBLE);

			if (rowCount % 2 == 1) {
				row.setBackgroundResource(R.color.RowOdd);
			}

			currentTable.addView(row, tableLayout);
		}

	}

	private void loadHeroSpells(Hero hero2) {

		TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		// fill spells
		int rowCount = 0;
		tblSpell1.removeAllViews();
		for (Spell spell : getHero().getSpells()) {
			rowCount++;
			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.spell_row, null);

			row.setOnClickListener(probeListener);
			row.setOnLongClickListener(editListener);
			row.setTag(spell);

			TextView spellLabel = (TextView) row.findViewById(R.id.spell_row_name);
			spellLabel.setText(spell.getName());

			TextView spellProbe = (TextView) row.findViewById(R.id.spell_row_probe);
			spellProbe.setText(spell.getProbe());

			TextView spellValue = (TextView) row.findViewById(R.id.spell_row_value);
			spellValue.setText(Integer.toString(spell.getValue()));
			tfValues.put(spell, new TextView[] { spellValue });

			if (rowCount % 2 == 1) {
				row.setBackgroundResource(R.color.RowOdd);
			}

			tblSpell1.addView(row, tableLayout);
		}

	}

	private void loadHeroTalents(Hero hero2) {

		talentAdapter = new ExpandableTalentAdapter(getHero());

		final SharedPreferences preference = getPreferences(MODE_PRIVATE);

		talentList.setAdapter(talentAdapter);
		talentList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Talent talent = talentAdapter.getChild(groupPosition, childPosition);
				checkProbe(talent);
				return false;
			}
		});

		talentList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				Editor edit = preference.edit();
				edit.putBoolean(PREF_KEY_GROUP_EXPANDED + groupPosition, false);
				edit.commit();
			}
		});
		talentList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				Editor edit = preference.edit();
				edit.putBoolean(PREF_KEY_GROUP_EXPANDED + groupPosition, true);
				edit.commit();
			}
		});

		talentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (view.getTag() instanceof Talent) {
					Talent talent = (Talent) view.getTag();
					showEditPopup(talent);
				}
				return false;
			}
		});

		for (int i = 0; i < talentAdapter.getGroupCount(); i++) {
			if (preference.getBoolean(PREF_KEY_GROUP_EXPANDED + i, true))
				talentList.expandGroup(i);
			else
				talentList.collapseGroup(i);
		}

	}

	public void setPortraitFile(String drawableId) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = preferences.edit();
		editor.putString(getHero().getPath(), drawableId);
		editor.commit();
	}

	public Hero getHero() {
		return DSATabApplication.getInstance().getHero();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_TAB_INDEX, viewFlipper.getDisplayedChild());
	}

	@Override
	protected void onPause() {
		// DSATabApplication.getInstance().saveHero();
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