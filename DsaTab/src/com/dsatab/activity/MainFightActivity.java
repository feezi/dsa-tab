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

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Hand;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.ArcheryChooserDialog;
import com.dsatab.view.EquippedItemChooserDialog;
import com.dsatab.view.EvadeChooserDialog;
import com.dsatab.view.listener.ModifierChangedListener;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.NumberPicker;
import com.gandulf.guilib.view.OnViewChangedListener;

/**
 * @author Ganymede
 * 
 */
public class MainFightActivity extends BaseMainActivity implements ModifierChangedListener, OnLongClickListener {

	private static final int CONTEXTMENU_SORT_EQUIPPED_ITEM = 5;
	private static final int CONTEXTMENU_ASSIGN_SECONDARY = 6;
	private static final int CONTEXTMENU_ASSIGN_PRIMARY = 7;
	private static final int CONTEXTMENU_UNASSIGN = 8;
	private static final int CONTEXTMENU_FIGHT_SET_1 = 10;
	private static final int CONTEXTMENU_FIGHT_SET_2 = 11;
	private static final int CONTEXTMENU_FIGHT_SET_3 = 12;
	private static final int CONTEXTMENU_VIEWEQUIPPEDITEM = 13;

	private static final String KEY_PICKER_TYPE = "pickerType";

	private View fightAttributesList;

	private boolean fightItemsOdd = false;

	private View selectedEquippedItemView;

	private NumberPicker fightNumberPicker;
	private LinearLayout fightLpLayout, fightItems, fightModifiers;

	class TargetListener implements View.OnClickListener {

		public void onClick(View v) {
			if (v.getTag() instanceof EquippedItem) {

				if (DSATabApplication.isLiteVersion()) {
					tease("<strong>Wo ist nochmal schnell die Fernkampftabelle?</strong> Hier! Einfach die Größe und Entfernung eingeben und die Vollversion von DsaTab berechnet dir automatisch für jede Waffe die genaue Erschwernis. Auch bewegliche Ziele oder schlechte Sichtverhältnisse können berücksichtigt werden.");
				} else {
					EquippedItem item = (EquippedItem) v.getTag();
					ArcheryChooserDialog targetChooserDialog = new ArcheryChooserDialog(MainFightActivity.this);
					targetChooserDialog.setWeapon(item);
					targetChooserDialog.show();
				}
			}
		}
	}

	private TargetListener targetListener = new TargetListener();
	private Button fightPickerButton;
	private AttributeType fightPickerType = AttributeType.Lebensenergie;

	private List<AttributeType> fightPickerTypes;

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
				fillFightItemDescriptions();
			}

		} else if (requestCode == ACTION_INVENTORY) {
			fillFightItemDescriptions();
			getHero().resetArmorAttributes();
		}

		super.onActivityResult(requestCode, resultCode, data);
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
		} else if (v.getId() == R.id.gen_tab_fight) {
			menu.add(0, CONTEXTMENU_FIGHT_SET_1, 1, getString(R.string.menu_equipment_set, "1"));
			menu.add(0, CONTEXTMENU_FIGHT_SET_2, 2, getString(R.string.menu_equipment_set, "2"));
			menu.add(0, CONTEXTMENU_FIGHT_SET_3, 3, getString(R.string.menu_equipment_set, "3"));
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	/**
	 * @param i
	 */
	private void selectItemSet(int i) {
		getHero().setActiveSet(i);
		fillFightItemDescriptions();
		getHero().resetArmorAttributes();
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
				dialog.setEquippedItems(getHero().getEquippedItems(Weapon.class, Shield.class));

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
			selectItemSet(0);
		} else if (item.getItemId() == CONTEXTMENU_FIGHT_SET_2) {
			selectItemSet(1);
		} else if (item.getItemId() == CONTEXTMENU_FIGHT_SET_3) {
			selectItemSet(2);
		}

		return super.onContextItemSelected(item);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.fight_btn_picker:
			if (fightPickerType == AttributeType.Initiative_Aktuell) {
				checkProbe(getHero().getAttribute(AttributeType.ini));
				return true;
			}
			break;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);

		switch (v.getId()) {
		case R.id.body_set1:
			selectItemSet(0);
			break;
		case R.id.body_set2:
			selectItemSet(1);
			break;
		case R.id.body_set3:
			selectItemSet(2);
			break;
		case R.id.fight_set:
			int set = getHero().getActiveSet();
			selectItemSet((set + 1) % 3);
			break;

		case R.id.fight_btn_picker:

			int index = fightPickerTypes.indexOf(fightPickerType);
			int next = (index + 1) % fightPickerTypes.size();
			fightPickerType = fightPickerTypes.get(next);

			Attribute attr = getHero().getAttribute(fightPickerType);

			updateNumberPicker(attr);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_fight);
		super.onCreate(savedInstanceState);

		fightAttributesList = findViewById(R.id.inc_fight_attributes_list);

		ImageButton tabFight = (ImageButton) findViewById(R.id.gen_tab_fight);
		tabFight.setOnClickListener(this);
		registerForContextMenu(tabFight);

		ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
		fightSet.setOnClickListener(this);

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

				if (DSATabApplication.isLiteVersion()) {
					tease("<strong>Auf welche Seite sind die Ausweichen Modifikatoren?</strong> Egal, die Vollversion von DsaTab berechnet nicht nur deinen Ausweichenwert, sie berücksichtigt auch sämtliche Modifikatoren (Mehrere Gegner, Distanzklasse, Gezieltes Ausweichen).");
				} else {
					EvadeChooserDialog ausweichenModificationDialog = new EvadeChooserDialog(MainFightActivity.this);
					ausweichenModificationDialog.show();
				}
			}
		});

		fightPickerButton = (Button) findViewById(R.id.fight_btn_picker);
		fightPickerButton.setOnClickListener(this);
		fightPickerButton.setOnLongClickListener(this);

		fightNumberPicker = (NumberPicker) findViewById(R.id.fight_picker);
		fightNumberPicker.setOnViewChangedListener(new OnViewChangedListener<NumberPicker>() {

			@Override
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				if (oldVal != newVal) {
					Attribute attr = getHero().getAttribute(fightPickerType);
					attr.setValue(newVal);
					onValueChanged(attr);
				}
			}
		});
		fightLpLayout = (LinearLayout) findViewById(R.id.fight_le_layout);
		fightLpLayout.setOnClickListener(editListener);

		fightItems = (LinearLayout) findViewById(R.id.fight_items);
		fightModifiers = (LinearLayout) findViewById(R.id.fight_modifiers);

		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		try {
			String typeString = pref.getString(KEY_PICKER_TYPE, AttributeType.Lebensenergie.name());
			fightPickerType = AttributeType.valueOf(typeString);
		} catch (Exception e) {
			Debug.error(e);
			fightPickerType = AttributeType.Lebensenergie;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putString(KEY_PICKER_TYPE, fightPickerType.name());
		edit.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		super.onHeroLoaded(hero);

		fillFightItemDescriptions();
		fillAttributesList(fightAttributesList);

		fightPickerTypes = new ArrayList<AttributeType>(4);
		fightPickerTypes.add(AttributeType.Lebensenergie);
		fightPickerTypes.add(AttributeType.Ausdauer);

		if (hero.getAttributeValue(AttributeType.Astralenergie) != null
				&& hero.getAttributeValue(AttributeType.Astralenergie) >= 0) {
			fightPickerTypes.add(AttributeType.Astralenergie);
		}
		if (hero.getAttributeValue(AttributeType.Karmaenergie) != null
				&& hero.getAttributeValue(AttributeType.Karmaenergie) >= 0) {
			fightPickerTypes.add(AttributeType.Karmaenergie);
		}
		fightPickerTypes.add(AttributeType.Initiative_Aktuell);

		if (!fightPickerTypes.contains(fightPickerType)) {
			fightPickerType = fightPickerTypes.get(0);
		}
		// fight
		Attribute attr = hero.getAttribute(fightPickerType);
		updateNumberPicker(attr);

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroUnloaded(Hero hero) {
		super.onHeroUnloaded(hero);
		hero.removeModifierChangedListener(this);
	}

	@Override
	public void onModifierAdded(Modificator value) {
		fillFightModifierDescription(value, null);

	}

	@Override
	public void onModifierRemoved(Modificator value) {
		fightModifiers.removeView(fightModifiers.findViewWithTag(value));

		if (fightModifiers.getChildCount() <= 1)
			fightModifiers.setVisibility(View.GONE);

	}

	@Override
	public void onModifierChanged(Modificator value) {
		View itemLayout = fightModifiers.findViewWithTag(value);
		fillFightModifierDescription(value, itemLayout);
	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {
		fightModifiers.removeViews(1, fightModifiers.getChildCount() - 1);
		for (Modificator item : values) {
			fillFightModifierDescription(item, null);
		}
		if (fightModifiers.getChildCount() <= 1)
			fightModifiers.setVisibility(View.GONE);

	}

	private void updateNumberPicker(Attribute value) {

		fightNumberPicker.setTag(value);
		fightNumberPicker.setRange(value.getMinimum(), value.getMaximum());
		fightNumberPicker.setCurrent(value.getValue());
		fightNumberPicker.setDefault(value.getReferenceValue());

		fightPickerButton.setText(value.getType().code());

		double ratio = 1.0;

		if (value.getType() == AttributeType.Lebensenergie) {
			ratio = getHero().getLeRatio();
		}

		if (ratio < 0.5)
			fightNumberPicker.getEditText().setTextColor(getResources().getColor(R.color.ValueRed));
		else if (ratio < 1.0)
			fightNumberPicker.getEditText().setTextColor(getResources().getColor(R.color.ValueBlack));
		else
			fightNumberPicker.getEditText().setTextColor(getResources().getColor(R.color.ValueGreen));
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Konstitution:
			case Charisma:
				fillAttributesList(fightAttributesList);
				break;
			}

			if (attr.getType() == fightPickerType) {
				updateNumberPicker(attr);
			}

		}

	}

	private void fillFightItemDescription(EquippedItem equippedItem) {

		Item item = equippedItem.getItem();

		if (item == null)
			return;

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
		text1.setText(item.getTitle());
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

	private void fillFightItemDescriptions() {

		ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
		LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
		drawable.setLevel(getHero().getActiveSet());

		fightItems.removeAllViews();
		Util.sort(getHero().getEquippedItems());
		for (EquippedItem eitem : getHero().getEquippedItems()) {
			fillFightItemDescription(eitem);
		}
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

}