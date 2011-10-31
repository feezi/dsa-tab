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
import java.util.UUID;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextMenuInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.activity.MainActivity;
import com.dsatab.activity.ModificatorEditActivity;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.CustomModificator;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
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
import com.dsatab.view.FightFilterSettings;
import com.dsatab.view.FilterDialog;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;
import com.gandulf.guilib.util.Debug;

public class FightFragment extends BaseFragment implements OnLongClickListener, OnClickListener,
		OnCheckedChangeListener {

	private static final int CONTEXTMENU_SORT_EQUIPPED_ITEM = 5;
	private static final int CONTEXTMENU_ASSIGN_SECONDARY = 6;
	private static final int CONTEXTMENU_ASSIGN_PRIMARY = 7;
	private static final int CONTEXTMENU_UNASSIGN = 8;
	private static final int CONTEXTMENU_ASSIGN_HUNTING = 9;
	private static final int CONTEXTMENU_VIEWEQUIPPEDITEM = 13;
	private static final int CONTEXTMENU_SELECT_VERSION = 14;
	private static final int CONTEXTMENU_SELECT_TALENT = 15;

	private static final String KEY_PICKER_TYPE = "pickerType";

	private boolean fightItemsOdd = false;

	private WheelView fightNumberPicker;
	private NumericWheelAdapter fightNumberAdapter;
	private LinearLayout fightLpLayout, fightItems, fightModifiers;

	private FightFilterSettings filterSettings;

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

		if (requestCode == MainActivity.ACTION_PREFERENCES) {
			fillFightItemDescriptions();
		} else if (requestCode == MainActivity.ACTION_ADD_MODIFICATOR) {

			if (resultCode == Activity.RESULT_OK) {

				CustomModificator modificator = new CustomModificator(getHero());
				modificator.setModificatorName(data.getStringExtra(ModificatorEditActivity.INTENT_NAME));
				modificator.setRules(data.getStringExtra(ModificatorEditActivity.INTENT_RULES));
				modificator.setComment(data.getStringExtra(ModificatorEditActivity.INTENT_COMMENT));
				modificator.setActive(data.getBooleanExtra(ModificatorEditActivity.INTENT_ACTIVE, true));

				getHero().addModificator(modificator);

				fillFightModifierDescriptions();
			}
		} else if (requestCode == MainActivity.ACTION_EDIT_MODIFICATOR) {
			if (resultCode == Activity.RESULT_OK) {

				UUID id = (UUID) data.getSerializableExtra(ModificatorEditActivity.INTENT_ID);

				for (Modificator modificator : getHero().getModificators()) {
					if (modificator instanceof CustomModificator) {
						CustomModificator customModificator = (CustomModificator) modificator;
						if (customModificator.getId().equals(id)) {
							customModificator.setModificatorName(data
									.getStringExtra(ModificatorEditActivity.INTENT_NAME));
							customModificator.setRules(data.getStringExtra(ModificatorEditActivity.INTENT_RULES));
							customModificator.setActive(data.getBooleanExtra(ModificatorEditActivity.INTENT_ACTIVE,
									true));
							customModificator.setComment(data.getStringExtra(ModificatorEditActivity.INTENT_COMMENT));

							fillFightModifierDescription(customModificator);
						}
					}
				}

			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onCreateIconContextMenu(android.view
	 * .Menu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public Object onCreateIconContextMenu(Menu menu, View v, IconContextMenuInfo menuInfo) {
		if (v.getTag() instanceof EquippedItem) {

			EquippedItem equippedItem = (EquippedItem) v.getTag();

			menuInfo.setTitle(equippedItem.getItemName());

			if (equippedItem.getItem().hasImage()) {
				menu.add(0, CONTEXTMENU_VIEWEQUIPPEDITEM, 0, getString(R.string.menu_view_item)).setIcon(
						R.drawable.ic_menu_view);
			}

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

			if (equippedItem.getItem().hasSpecification(DistanceWeapon.class)) {
				menu.add(0, CONTEXTMENU_ASSIGN_HUNTING, 0, getString(R.string.menu_assign_hunting_weapon)).setIcon(
						R.drawable.ic_menu_star);

			}

			if (equippedItem.getSecondaryItem() != null) {
				menu.add(0, CONTEXTMENU_UNASSIGN, 1, getString(R.string.menu_unassign_item)).setIcon(
						R.drawable.ic_menu_revert);
			}

			if (equippedItem.getItem().getSpecifications().size() > 1) {
				menu.add(0, CONTEXTMENU_SELECT_VERSION, 2, getString(R.string.menu_select_version)).setIcon(
						R.drawable.ic_menu_more);
			}

			if (equippedItem.getItemSpecification() instanceof Weapon) {
				Weapon weapon = (Weapon) equippedItem.getItemSpecification();
				if (weapon.getCombatTalentTypes().size() > 1) {
					menu.add(0, CONTEXTMENU_SELECT_TALENT, 3, getString(R.string.menu_select_talent)).setIcon(
							R.drawable.ic_menu_more);
				}
			} else if (equippedItem.getItemSpecification() instanceof Shield) {
				Shield shield = (Shield) equippedItem.getItemSpecification();
				if (shield.getCombatTalentTypes().size() > 1) {
					menu.add(0, CONTEXTMENU_SELECT_TALENT, 3, getString(R.string.menu_select_talent)).setIcon(
							R.drawable.ic_menu_more);
				}
			}
			return equippedItem;
		} else if (v.getTag() instanceof CustomModificator) {
			CustomModificator modificator = (CustomModificator) v.getTag();

			MenuInflater menuInflater = getActivity().getMenuInflater();
			menuInflater.inflate(R.menu.modifikator_menu, menu);
			return modificator;

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
	 * @see com.dsatab.fragment.BaseFragment#onFilterChanged(com.dsatab.view.
	 * FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {
		if (type == FilterType.Fight && settings instanceof FightFilterSettings) {

			FightFilterSettings newSettings = (FightFilterSettings) settings;
			if (filterSettings.equals(newSettings))
				return;

			if (filterSettings.isShowArmor() != newSettings.isShowArmor()) {
				filterSettings.setShowArmor(newSettings.isShowArmor());
				fillFightItemDescriptions();
			}

			if (filterSettings.isShowModifier() != newSettings.isShowModifier()) {
				filterSettings.setShowModifier(newSettings.isShowModifier());
				fillFightModifierDescriptions();
			}

			if (filterSettings.isShowEvade() != newSettings.isShowEvade()) {
				filterSettings.setShowEvade(newSettings.isShowEvade());
				fillAusweichen();
			}
		}
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
			} else if (item.getItemId() == CONTEXTMENU_SELECT_TALENT) {

				final List<String> specInfo = new ArrayList<String>();
				if (equippedItem.getItemSpecification() instanceof Weapon) {
					Weapon weapon = (Weapon) equippedItem.getItemSpecification();
					for (CombatTalentType type : weapon.getCombatTalentTypes()) {
						specInfo.add(type.getName());
					}
				} else if (equippedItem.getItemSpecification() instanceof Shield) {
					Shield shield = (Shield) equippedItem.getItemSpecification();
					for (CombatTalentType type : shield.getCombatTalentTypes()) {
						specInfo.add(type.getName());
					}
				}
				if (!specInfo.isEmpty()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setItems(specInfo.toArray(new String[0]), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String talentName = specInfo.get(which);
							CombatTalent talent = getHero().getCombatTalent(talentName);
							if (talent != null) {
								equippedItem.setTalent(talent);
								getHero().fireItemChangedEvent(equippedItem);
							}
							dialog.dismiss();
						}
					});

					builder.setTitle("Wähle ein Talent...");
					builder.show();
				}
			} else if (item.getItemId() == CONTEXTMENU_ASSIGN_HUNTING) {
				getHero().setHuntingWeapon(equippedItem);
				updateFightItemDescriptions();
			}
		} else if (info instanceof CustomModificator) {

			CustomModificator modificator = (CustomModificator) info;

			switch (item.getItemId()) {

			case R.id.option_add:

				startActivityForResult(new Intent(getActivity(), ModificatorEditActivity.class),
						MainActivity.ACTION_ADD_MODIFICATOR);
				break;
			case R.id.option_edit:

				Intent intent = new Intent(getActivity(), ModificatorEditActivity.class);
				intent.putExtra(ModificatorEditActivity.INTENT_ID, modificator.getId());
				intent.putExtra(ModificatorEditActivity.INTENT_NAME, modificator.getModificatorName());
				intent.putExtra(ModificatorEditActivity.INTENT_RULES, modificator.getRules());
				intent.putExtra(ModificatorEditActivity.INTENT_COMMENT, modificator.getComment());
				intent.putExtra(ModificatorEditActivity.INTENT_ACTIVE, modificator.isActive());
				startActivityForResult(intent, MainActivity.ACTION_EDIT_MODIFICATOR);
				break;
			case R.id.option_delete:
				getHero().removeModificator(modificator);
				break;

			}
		}

		super.onIconContextItemSelected(item, info);
	}

	public void fillAusweichen() {

		final Attribute ausweichen = getHero().getAttribute(AttributeType.Ausweichen);
		View fightausweichen = getView().findViewById(R.id.inc_fight_ausweichen);

		if (filterSettings.isShowEvade()) {

			fightausweichen.setVisibility(View.VISIBLE);
			TextView text1 = (TextView) fightausweichen.findViewById(android.R.id.text1);

			StyleableSpannableStringBuilder title = new StyleableSpannableStringBuilder();
			title.append(ausweichen.getName());
			Util.appendValue(getHero(), title, ausweichen, null);
			text1.setText(title);
			final TextView text2 = (TextView) fightausweichen.findViewById(android.R.id.text2);
			text2.setText("Modifikator " + Util.toProbe(ausweichen.getProbeInfo().getErschwernis()));
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
		case R.id.fight_modifiers_add:
			startActivityForResult(new Intent(getActivity(), ModificatorEditActivity.class),
					MainActivity.ACTION_ADD_MODIFICATOR);
			break;
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
		return configureContainerView(inflater.inflate(R.layout.sheet_fight, container, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		filterSettings = new FightFilterSettings(preferences.getBoolean(FilterDialog.PREF_KEY_SHOW_ARMOR, true),
				preferences.getBoolean(FilterDialog.PREF_KEY_SHOW_MODIFIER, true), preferences.getBoolean(
						FilterDialog.PREF_KEY_SHOW_EVADE, true));

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

		fightNumberPicker = (WheelView) findViewById(R.id.fight_picker);

		fightNumberAdapter = new NumericWheelAdapter(getActivity());
		fightNumberPicker.setViewAdapter(fightNumberAdapter);

		fightNumberPicker.setOrientation(WheelView.HORIZONTAL);
		fightNumberPicker.setOnWheelChangedListeners(new OnWheelChangedListener() {

			@Override
			public void onWheelChanged(WheelView wheel, int oldValue, int newValue) {
				Attribute attr = getHero().getAttribute(fightPickerType);
				attr.setValue(fightNumberAdapter.getItem(newValue));
			}
		});

		fightLpLayout = (LinearLayout) findViewById(R.id.fight_le_layout);
		fightLpLayout.setOnClickListener(getBaseActivity().getEditListener());

		fightItems = (LinearLayout) findViewById(R.id.fight_items);
		fightModifiers = (LinearLayout) findViewById(R.id.fight_modifiers);

		findViewById(R.id.fight_modifiers_add).setOnClickListener(this);

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

		if (filterSettings.isShowModifier()) {
			// remove all but first, visibility will be set to visible within
			// fillFightModifierDescription automatically
			fightModifiers.removeViews(1, fightModifiers.getChildCount() - 1);
			for (Modificator item : getHero().getModificators()) {
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
	 * android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged
	 * (android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getTag() instanceof CustomModificator) {
			CustomModificator modificator = (CustomModificator) buttonView.getTag();
			modificator.setActive(isChecked);
		}

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
		updateFightItemDescriptions();
	}

	@Override
	public void onModifierChanged(Modificator value) {
		View itemLayout = fightModifiers.findViewWithTag(value);
		fillFightModifierDescription(value, itemLayout);
		updateFightItemDescriptions();
	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {
		for (Modificator item : values) {
			View itemLayout = fightModifiers.findViewWithTag(item);
			fillFightModifierDescription(item, itemLayout);
		}
		updateFightItemDescriptions();
	}

	private void updateNumberPicker(Attribute value) {

		fightNumberPicker.setTag(value);
		fightNumberAdapter.setRange(value.getMinimum(), value.getMaximum());
		if (value.getValue() != null) {
			fightNumberPicker.setCurrentItem(fightNumberAdapter.getPosition(value.getValue()));
		} else {
			fightNumberPicker.setCurrentItem(0);
		}
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
			fightNumberAdapter.setTextColor(getResources().getColor(R.color.ValueRed));
		else if (ratio < 1.0)
			fightNumberAdapter.setTextColor(getResources().getColor(R.color.ValueBlack));
		else
			fightNumberAdapter.setTextColor(getResources().getColor(R.color.ValueGreen));
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
			case Behinderung:
				fillAusweichen();
				break;
			case Ausweichen: {
				fillAusweichen();
				break;
			}
			case Körperkraft:
				updateFightItemDescriptions();
				break;
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

		if (!filterSettings.isShowArmor() && itemSpecification instanceof Armor) {
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

		if (getHero().getHuntingWeapon() != null && getHero().getHuntingWeapon().equals(equippedItem)) {
			title.append(" (Jagdwaffe)");
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

		int count = fightItems.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = fightItems.getChildAt(i);
			EquippedItem equippedItem = (EquippedItem) v.getTag();

			ItemSpecification itemSpecification = equippedItem.getItemSpecification();
			Item item = equippedItem.getItem();

			if (item == null || itemSpecification == null)
				return;

			if (!filterSettings.isShowArmor() && itemSpecification instanceof Armor) {
				continue;
			}

			fillFightItemDescription(v, equippedItem);
		}

	}

	private void fillFightItemDescriptions() {

		ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
		LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
		drawable.setLevel(getHero().getActiveSet());

		fightItems.removeAllViews();
		fightItemsOdd = false;

		Util.sort(getHero().getEquippedItems());
		for (EquippedItem equippedItem : getHero().getEquippedItems()) {

			ItemSpecification itemSpecification = equippedItem.getItemSpecification();
			Item item = equippedItem.getItem();

			if (item == null || itemSpecification == null)
				return;

			if (!filterSettings.isShowArmor() && itemSpecification instanceof Armor) {
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

	private void fillFightModifierDescription(Modificator item) {
		View itemLayout = fightModifiers.findViewWithTag(item);
		fillFightModifierDescription(item, itemLayout);
	}

	private void fillFightModifierDescription(Modificator item, View itemLayout) {

		if (itemLayout == null) {
			LayoutInflater layoutInflater = getActivity().getLayoutInflater();
			itemLayout = layoutInflater.inflate(R.layout.fight_sheet_modifier, fightModifiers, false);
			fightModifiers.addView(itemLayout);
			Util.applyRowStyle(itemLayout, fightModifiers.getChildCount());
		}
		itemLayout.setTag(item);

		TextView text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
		TextView text2 = (TextView) itemLayout.findViewById(android.R.id.text2);

		CheckBox active = (CheckBox) itemLayout.findViewById(R.id.active);

		if (item instanceof CustomModificator) {
			CustomModificator modificator = (CustomModificator) item;
			active.setVisibility(View.VISIBLE);
			active.setChecked(modificator.isActive());
			active.setOnCheckedChangeListener(this);
			active.setTag(modificator);
			itemLayout.setTag(modificator);
			registerForIconContextMenu(itemLayout);
		} else {
			active.setVisibility(View.GONE);
			unregisterForIconContextMenu(itemLayout);
		}
		if (item != null) {
			text1.setText(item.getModificatorName());
			text2.setText(item.getModificatorInfo());
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
