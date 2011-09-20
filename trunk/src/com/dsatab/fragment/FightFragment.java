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

import java.util.ArrayList;
import java.util.List;

import yuku.iconcontextmenu.IconContextMenu;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.BaseMainActivity;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.common.StyleableSpannableStringBuilder;
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
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.ArcheryChooserDialog;
import com.dsatab.view.EquippedItemChooserDialog;
import com.dsatab.view.EvadeChooserDialog;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.NumberPicker;
import com.gandulf.guilib.view.OnViewChangedListener;

public class FightFragment extends BaseFragment implements OnLongClickListener, OnClickListener {

	private static final int CONTEXTMENU_SORT_EQUIPPED_ITEM = 5;
	private static final int CONTEXTMENU_ASSIGN_SECONDARY = 6;
	private static final int CONTEXTMENU_ASSIGN_PRIMARY = 7;
	private static final int CONTEXTMENU_UNASSIGN = 8;
	private static final int CONTEXTMENU_VIEWEQUIPPEDITEM = 13;
	private static final int CONTEXTMENU_SELECT_VERSION = 14;

	private static final String KEY_PICKER_TYPE = "pickerType";
	private static final String PREF_KEY_SHOW_ARMOR = "show_armor";
	private static final String PREF_KEY_SHOW_MODIFIER = "show_modifier";
	private static final String PREF_KEY_SHOW_EVADE = "show_evade";

	private boolean fightItemsOdd = false;

	private NumberPicker fightNumberPicker;
	private LinearLayout fightLpLayout, fightItems, fightModifiers;

	class TargetListener implements View.OnClickListener {

		public void onClick(View v) {
			if (v.getTag() instanceof EquippedItem) {

				EquippedItem item = (EquippedItem) v.getTag();
				ArcheryChooserDialog targetChooserDialog = new ArcheryChooserDialog(getBaseActivity());
				targetChooserDialog.setWeapon(item);
				targetChooserDialog.show();

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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == BaseMainActivity.ACTION_PREFERENCES) {
			fillFightItemDescriptions();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onCreateIconContextMenu(android.view
	 * .Menu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public Object onCreateIconContextMenu(Menu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getTag() instanceof EquippedItem) {
			EquippedItem equippedItem = (EquippedItem) v.getTag();
			if (equippedItem.getItem().hasSpecification(Shield.class)) {
				menu.add(0, CONTEXTMENU_ASSIGN_PRIMARY, 0, getString(R.string.menu_assign_main_weapon)).setIcon(
						R.drawable.ic_menu_share);
			}
			if (equippedItem.getItem().hasSpecification(Weapon.class)) {
				Weapon weapon = (Weapon) equippedItem.getItem().getSpecification(Weapon.class);
				if (!weapon.isTwoHanded()) {
					menu.add(0, CONTEXTMENU_ASSIGN_SECONDARY, 0, getString(R.string.menu_assign_secondary_weapon))
							.setIcon(R.drawable.ic_menu_share);
				}
			}

			if (equippedItem.getSecondaryItem() != null) {
				menu.add(0, CONTEXTMENU_UNASSIGN, 1, getString(R.string.menu_unassign_item)).setIcon(
						R.drawable.ic_menu_revert);
			}

			if (equippedItem.getItem().getSpecifications().size() > 1) {
				menu.add(0, CONTEXTMENU_SELECT_VERSION, 2, getString(R.string.menu_select_version)).setIcon(
						R.drawable.ic_menu_more);
			}

			return equippedItem;
		} else {
			return super.onCreateIconContextMenu(menu, v, menuInfo);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onPrepareIconContextMenu(yuku.
	 * iconcontextmenu.IconContextMenu, android.view.View)
	 */
	@Override
	public void onPrepareIconContextMenu(IconContextMenu menu, View v) {
		super.onPrepareIconContextMenu(menu, v);
		if (menu.getInfo() instanceof EquippedItem) {
			EquippedItem equippedItem = (EquippedItem) menu.getInfo();
			menu.setTitle(equippedItem.getItemName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.option_filter_fight) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setTitle("Kampfübersicht");
			builder.setIcon(android.R.drawable.ic_menu_view);
			View content = LayoutInflater.from(getActivity()).inflate(R.layout.popup_filter_fight, null);

			final CheckBox armor = (CheckBox) content.findViewById(R.id.cb_show_armor);
			final CheckBox modifier = (CheckBox) content.findViewById(R.id.cb_show_modifier);
			final CheckBox evade = (CheckBox) content.findViewById(R.id.cb_show_evade);

			SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);

			armor.setChecked(pref.getBoolean(PREF_KEY_SHOW_ARMOR, true));
			modifier.setChecked(pref.getBoolean(PREF_KEY_SHOW_MODIFIER, true));
			evade.setChecked(pref.getBoolean(PREF_KEY_SHOW_EVADE, false));

			builder.setView(content);

			DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {

						SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
						Editor edit = pref.edit();

						edit.putBoolean(PREF_KEY_SHOW_ARMOR, armor.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_MODIFIER, modifier.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_EVADE, evade.isChecked());

						edit.commit();

						fillFightItemDescriptions();
						fillFightModifierDescriptions();
						fillAusweichen();
					} else if (which == DialogInterface.BUTTON_NEUTRAL) {
						// do nothing
					}

				}
			};

			builder.setPositiveButton(R.string.label_ok, clickListener);
			builder.setNegativeButton(R.string.label_cancel, clickListener);

			builder.show();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onCreateOptionsMenu(android.view
	 * .Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fight_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onIconContextItemSelected(android.view
	 * .MenuItem, java.lang.Object)
	 */
	@Override
	public void onIconContextItemSelected(MenuItem item, Object info) {

		if (info instanceof EquippedItem) {

			final EquippedItem equippedItem = (EquippedItem) info;

			if (item.getItemId() == CONTEXTMENU_VIEWEQUIPPEDITEM) {

				Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_EQUIPPED_ITEM_ID, equippedItem.getId());
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_SEARCHABLE, false);
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_CATEGORY_SELECTABLE, false);
				startActivity(intent);

			} else if (item.getItemId() == CONTEXTMENU_ASSIGN_PRIMARY) {

				final EquippedItem equippedShield = equippedItem;

				EquippedItemChooserDialog dialog = new EquippedItemChooserDialog(getActivity());
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

			} else if (item.getItemId() == CONTEXTMENU_ASSIGN_SECONDARY) {

				final EquippedItem equippedPrimaryWeapon = equippedItem;

				EquippedItemChooserDialog dialog = new EquippedItemChooserDialog(getActivity());
				dialog.setEquippedItems(getHero().getEquippedItems(Weapon.class, Shield.class));

				// do not select item itself
				dialog.getEquippedItems().remove(equippedPrimaryWeapon);
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

				dialog.show();

			} else if (item.getItemId() == CONTEXTMENU_UNASSIGN) {

				final EquippedItem equippedPrimaryWeapon = equippedItem;
				EquippedItem equippedSecondaryWeapon = equippedPrimaryWeapon.getSecondaryItem();

				equippedPrimaryWeapon.setSecondaryItem(null);
				equippedSecondaryWeapon.setSecondaryItem(null);

				fillFightItemDescriptions();

			} else if (item.getItemId() == CONTEXTMENU_SORT_EQUIPPED_ITEM) {
				fillFightItemDescriptions();
			} else if (item.getItemId() == CONTEXTMENU_SELECT_VERSION) {

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

				List<String> specInfo = equippedItem.getItem().getSpecificationNames();

				builder.setItems(specInfo.toArray(new String[0]), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						equippedItem.setItemSpecification(getActivity(), equippedItem.getItem().getSpecifications()
								.get(which));
						dialog.dismiss();
					}
				});

				builder.setTitle("Wähle eine Variante...");
				builder.show();

			}
		}

		super.onIconContextItemSelected(item, info);
	}

	public void fillAusweichen() {

		final Attribute ausweichen = getHero().getAttribute(AttributeType.Ausweichen);
		View fightausweichen = getView().findViewById(R.id.inc_fight_ausweichen);

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);

		final boolean showEvade = pref.getBoolean(PREF_KEY_SHOW_EVADE, true);

		if (showEvade) {

			fightausweichen.setVisibility(View.VISIBLE);
			TextView text1 = (TextView) fightausweichen.findViewById(android.R.id.text1);

			StyleableSpannableStringBuilder title = new StyleableSpannableStringBuilder();
			title.append(ausweichen.getName());
			Util.appendValue(getHero(), title, ausweichen, null);
			text1.setText(title);
			final TextView text2 = (TextView) fightausweichen.findViewById(android.R.id.text2);
			text2.setText("Modifikator " + Util.toProbe(ausweichen.getErschwernis()));
			if (fightItemsOdd) {
				fightausweichen.setBackgroundResource(R.color.RowOdd);
			}

			ImageButton iconLeft = (ImageButton) fightausweichen.findViewById(android.R.id.icon1);
			iconLeft.setTag(ausweichen);

		} else {
			fightausweichen.setVisibility(View.GONE);
		}

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
				getBaseActivity().checkProbe(getHero().getAttribute(AttributeType.ini));
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

		switch (v.getId()) {
		case R.id.fight_set:
			getHero().setActiveSet(getHero().getNextActiveSet());
			break;

		case R.id.fight_btn_picker:

			int index = fightPickerTypes.indexOf(fightPickerType);
			int next = (index + 1) % fightPickerTypes.size();
			fightPickerType = fightPickerTypes.get(next);

			Attribute attr = getHero().getAttribute(fightPickerType);

			updateNumberPicker(attr);
			break;
		}

		if (v.getTag() instanceof EquippedItem) {
			EquippedItem equippedItem = (EquippedItem) v.getTag();
			Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_EQUIPPED_ITEM_ID, equippedItem.getId());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_SEARCHABLE, false);
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_CATEGORY_SELECTABLE, false);
			startActivity(intent);
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

		return inflater.inflate(R.layout.sheet_fight, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
		fightSet.setOnClickListener(this);

		View fightausweichen = findViewById(R.id.inc_fight_ausweichen);
		ImageButton iconLeft = (ImageButton) fightausweichen.findViewById(android.R.id.icon1);
		iconLeft.setOnClickListener(getBaseActivity().getProbeListener());
		iconLeft.setOnLongClickListener(getBaseActivity().getEditListener());
		iconLeft.setImageResource(R.drawable.icon_ausweichen);
		iconLeft.setVisibility(View.VISIBLE);
		iconLeft.setFocusable(true);
		ImageButton iconRight = (ImageButton) fightausweichen.findViewById(android.R.id.icon2);
		iconRight.setImageResource(R.drawable.icon_target);
		iconRight.setVisibility(View.VISIBLE);
		iconRight.setFocusable(true);
		iconRight.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EvadeChooserDialog ausweichenModificationDialog = new EvadeChooserDialog(getBaseActivity());
				ausweichenModificationDialog.show();
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
				}
			}
		});
		fightLpLayout = (LinearLayout) findViewById(R.id.fight_le_layout);
		fightLpLayout.setOnClickListener(getBaseActivity().getEditListener());

		fightItems = (LinearLayout) findViewById(R.id.fight_items);
		fightModifiers = (LinearLayout) findViewById(R.id.fight_modifiers);

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		try {
			String typeString = pref.getString(KEY_PICKER_TYPE, AttributeType.Lebensenergie.name());
			fightPickerType = AttributeType.valueOf(typeString);
		} catch (Exception e) {
			Debug.error(e);
			fightPickerType = AttributeType.Lebensenergie;
		}

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		;
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
	public void onHeroLoaded(Hero hero) {

		fillFightItemDescriptions();

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

		fillAusweichen();

		fillFightModifierDescriptions();

	}

	/**
	 * 
	 */
	private void fillFightModifierDescriptions() {

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);

		final boolean showModifier = pref.getBoolean(PREF_KEY_SHOW_MODIFIER, true);

		if (showModifier) {
			// remove all but first, visibility will be set to visible within
			// fillFightModifierDescription automatically
			fightModifiers.removeViews(1, fightModifiers.getChildCount() - 1);
			fightModifiers.setVisibility(View.GONE);
			for (Modificator item : getHero().getModifiers()) {
				fillFightModifierDescription(item, null);
			}
		} else {
			fightModifiers.setVisibility(View.GONE);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroUnloaded(Hero hero) {
	}

	@Override
	public void onModifierAdded(Modificator value) {
		updateFightItemDescriptions();
		fillFightModifierDescription(value, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.view.listener.ModifierChangedListener#onPortraitChanged()
	 */
	@Override
	public void onPortraitChanged() {

	}

	@Override
	public void onModifierRemoved(Modificator value) {
		fightModifiers.removeView(fightModifiers.findViewWithTag(value));

		if (fightModifiers.getChildCount() <= 1) {
			fightModifiers.setVisibility(View.GONE);
		}

		updateFightItemDescriptions();
		;
	}

	@Override
	public void onModifierChanged(Modificator value) {
		View itemLayout = fightModifiers.findViewWithTag(value);
		fillFightModifierDescription(value, itemLayout);
		updateFightItemDescriptions();
	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {
		fightModifiers.removeViews(1, fightModifiers.getChildCount() - 1);
		for (Modificator item : values) {
			fillFightModifierDescription(item, null);
		}
		if (fightModifiers.getChildCount() <= 1) {
			fightModifiers.setVisibility(View.GONE);
		}
		updateFightItemDescriptions();
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
		} else if (value.getType() == AttributeType.Ausdauer) {
			ratio = getHero().getAuRatio();
		} else if (value.getType() == AttributeType.Karmaenergie) {
			ratio = getHero().getKeRatio();
		} else if (value.getType() == AttributeType.Astralenergie) {
			ratio = getHero().getAeRatio();
		}

		if (ratio < 0.5)
			fightNumberPicker.setTextColor(getResources().getColor(R.color.ValueRed));
		else if (ratio < 1.0)
			fightNumberPicker.setTextColor(getResources().getColor(R.color.ValueBlack));
		else
			fightNumberPicker.setTextColor(getResources().getColor(R.color.ValueGreen));
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			if (attr.getType() == fightPickerType) {
				updateNumberPicker(attr);
			}

			switch (attr.getType()) {

			case Ausweichen: {

				fillAusweichen();
			}

			case Körperkraft:
				updateFightItemDescriptions();
			}

		}

	}

	private void fillFightItemDescription(EquippedItem equippedItem) {
		fillFightItemDescription(null, equippedItem);
	}

	private void fillFightItemDescription(View itemLayout, EquippedItem equippedItem) {

		Item item = equippedItem.getItem();
		ItemSpecification itemSpecification = equippedItem.getItemSpecification();

		if (item == null || itemSpecification == null)
			return;

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		final boolean showArmor = pref.getBoolean(PREF_KEY_SHOW_ARMOR, true);
		if (!showArmor && itemSpecification instanceof Armor) {
			return;
		}

		if (itemLayout == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

			itemLayout = layoutInflater.inflate(R.layout.item_listitem, fightItems, false);
			itemLayout.setOnClickListener(this);

			registerForIconContextMenu(itemLayout);

			fightItems.addView(itemLayout);
		}

		itemLayout.setTag(equippedItem);

		if (fightItemsOdd) {
			itemLayout.setBackgroundResource(R.color.RowOdd);
		}

		TextView text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
		TextView text2 = (TextView) itemLayout.findViewById(android.R.id.text2);

		ImageButton iconLeft = (ImageButton) itemLayout.findViewById(android.R.id.icon1);
		ImageButton iconRight = (ImageButton) itemLayout.findViewById(android.R.id.icon2);

		if (equippedItem.getSecondaryItem() != null
				&& (equippedItem.getSecondaryItem().getItem().hasSpecification(Shield.class) || (equippedItem
						.getSecondaryItem().getItem().hasSpecification(Weapon.class) && equippedItem.getHand() == Hand.rechts))) {
			// keep odd/even
		} else {
			fightItemsOdd = !fightItemsOdd;
		}

		StyleableSpannableStringBuilder title = new StyleableSpannableStringBuilder();
		title.append(item.getTitle());

		text2.setText(itemSpecification.getInfo());

		if (itemSpecification instanceof DistanceWeapon) {
			DistanceWeapon distanceWeapon = (DistanceWeapon) itemSpecification;
			iconLeft.setImageResource(distanceWeapon.getResourceId());
			iconLeft.setVisibility(View.VISIBLE);
			iconLeft.setFocusable(true);
			iconRight.setImageResource(R.drawable.icon_target);
			iconRight.setVisibility(View.VISIBLE);
			iconRight.setFocusable(true);

			if (equippedItem.getTalent() != null) {
				CombatProbe probe = new CombatProbe(getHero(), equippedItem, true);
				Util.appendValue(getHero(), title, probe, null);
				iconRight.setEnabled(true);
				iconLeft.setEnabled(true);
				iconRight.setTag(equippedItem);
				iconRight.setOnClickListener(targetListener);
				iconLeft.setTag(probe);
				iconLeft.setOnClickListener(getBaseActivity().getProbeListener());
			} else {
				iconRight.setEnabled(false);
				iconLeft.setEnabled(false);
			}
		} else if (itemSpecification instanceof Shield) {
			iconRight.setImageResource(item.getResourceId());
			iconRight.setVisibility(View.VISIBLE);
			iconRight.setFocusable(true);
			iconLeft.setVisibility(View.INVISIBLE);
			if (equippedItem.getTalent() != null) {
				iconRight.setEnabled(true);
				iconLeft.setEnabled(true);
				CombatProbe probe = new CombatProbe(getHero(), equippedItem, false);
				Util.appendValue(getHero(), title, probe, null);
				iconRight.setTag(probe);
				iconRight.setOnClickListener(getBaseActivity().getProbeListener());
			} else {
				iconRight.setEnabled(false);
				iconLeft.setEnabled(false);
			}
		} else if (itemSpecification instanceof Weapon) {
			Weapon weapon = (Weapon) itemSpecification;
			iconLeft.setImageResource(weapon.getResourceId());
			iconLeft.setVisibility(View.VISIBLE);
			iconLeft.setFocusable(true);
			iconRight.setImageResource(R.drawable.icon_shield);
			iconRight.setVisibility(View.VISIBLE);
			iconRight.setFocusable(true);
			if (equippedItem.getTalent() != null) {
				iconRight.setEnabled(true);
				iconLeft.setEnabled(true);

				CombatProbe at = new CombatProbe(getHero(), equippedItem, true);
				iconLeft.setTag(at);
				iconLeft.setOnClickListener(getBaseActivity().getProbeListener());
				CombatProbe pa = new CombatProbe(getHero(), equippedItem, false);
				iconRight.setTag(pa);
				iconRight.setOnClickListener(getBaseActivity().getProbeListener());

				Util.appendValue(getHero(), title, at, pa);
			} else {
				iconRight.setEnabled(false);
				iconLeft.setEnabled(false);
			}
			text2.setText(weapon.getInfo(getHero().getModifiedValue(AttributeType.Körperkraft)));
		} else if (itemSpecification instanceof Armor) {
			Armor armor = (Armor) itemSpecification;
			iconLeft.setImageResource(armor.getResourceId());
			iconLeft.setVisibility(View.VISIBLE);
			iconLeft.setFocusable(true);
			iconLeft.setFocusable(false);
			iconRight.setVisibility(View.GONE);
		}

		text1.setSelected(true);
		text1.setText(title);
	}

	private View findFightItemDescription(EquippedItem equippedItem) {
		int count = fightItems.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = fightItems.getChildAt(i);
			if (equippedItem.equals(child.getTag()))
				return child;
		}
		return null;
	}

	private void updateFightItemDescriptions() {

		ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
		LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
		drawable.setLevel(getHero().getActiveSet());

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		final boolean showArmor = pref.getBoolean(PREF_KEY_SHOW_ARMOR, true);

		int count = fightItems.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = fightItems.getChildAt(i);
			EquippedItem equippedItem = (EquippedItem) v.getTag();

			ItemSpecification itemSpecification = equippedItem.getItemSpecification();
			Item item = equippedItem.getItem();

			if (item == null || itemSpecification == null)
				return;

			if (!showArmor && itemSpecification instanceof Armor) {
				continue;
			}

			fillFightItemDescription(v, equippedItem);
		}

	}

	private void fillFightItemDescriptions() {

		ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
		LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
		drawable.setLevel(getHero().getActiveSet());

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		final boolean showArmor = pref.getBoolean(PREF_KEY_SHOW_ARMOR, true);

		fightItems.removeAllViews();
		fightItemsOdd = false;

		Util.sort(getHero().getEquippedItems());
		for (EquippedItem equippedItem : getHero().getEquippedItems()) {

			ItemSpecification itemSpecification = equippedItem.getItemSpecification();
			Item item = equippedItem.getItem();

			if (item == null || itemSpecification == null)
				return;

			if (!showArmor && itemSpecification instanceof Armor) {
				continue;
			}
			fillFightItemDescription(equippedItem);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onActiveSetChanged(int, int)
	 */
	@Override
	public void onActiveSetChanged(int newSet, int oldSet) {
		super.onActiveSetChanged(newSet, oldSet);
		fillFightItemDescriptions();
	}

	private void fillFightModifierDescription(Modificator item, View itemLayout) {

		fightModifiers.setVisibility(View.VISIBLE);
		if (itemLayout == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemAdded(com.dsatab
	 * .data.items.Item)
	 */
	@Override
	public void onItemAdded(Item item) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemRemoved(com.dsatab
	 * .data.items.Item)
	 */
	@Override
	public void onItemRemoved(Item item) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemChanged(com.dsatab
	 * .data.items.EquippedItem)
	 */
	@Override
	public void onItemChanged(EquippedItem item) {
		fillFightItemDescription(findFightItemDescription(item), item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemEquipped(com.
	 * dsatab.data.items.EquippedItem)
	 */
	@Override
	public void onItemEquipped(EquippedItem item) {
		fillFightItemDescription(findFightItemDescription(item), item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemUnequipped(com
	 * .dsatab.data.items.EquippedItem)
	 */
	@Override
	public void onItemUnequipped(EquippedItem item) {
		fightItems.removeView(findFightItemDescription(item));
	}

}
