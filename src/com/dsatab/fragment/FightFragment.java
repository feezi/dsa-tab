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
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.commonsware.cwac.merge.MergeAdapter;
import com.commonsware.cwac.sacklist.SackOfViewsAdapter;
import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.activity.MainActivity;
import com.dsatab.activity.ModificatorEditActivity;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.CustomModificator;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.FightEquippedItemAdapter;
import com.dsatab.data.adapter.FightModificatorAdapter;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Hand;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.AbstractModificator;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.util.Debug;
import com.dsatab.view.ArcheryChooserDialog;
import com.dsatab.view.EquippedItemChooserDialog;
import com.dsatab.view.EvadeChooserDialog;
import com.dsatab.view.FightFilterSettings;
import com.dsatab.view.FilterDialog;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;

public class FightFragment extends BaseFragment implements OnLongClickListener, OnClickListener, OnItemClickListener {

	private static final int CONTEXTMENU_SORT_EQUIPPED_ITEM = 5;
	private static final int CONTEXTMENU_ASSIGN_SECONDARY = 6;
	private static final int CONTEXTMENU_ASSIGN_PRIMARY = 7;
	private static final int CONTEXTMENU_UNASSIGN = 8;
	private static final int CONTEXTMENU_ASSIGN_HUNTING = 9;
	private static final int CONTEXTMENU_VIEWEQUIPPEDITEM = 13;
	private static final int CONTEXTMENU_SELECT_VERSION = 14;
	private static final int CONTEXTMENU_SELECT_TALENT = 15;

	private static final String KEY_PICKER_TYPE = "pickerType";

	private WheelView fightNumberPicker;
	private NumericWheelAdapter fightNumberAdapter;
	private LinearLayout fightLpLayout;

	private View fightausweichen;

	private ListView fightList;

	private FightFilterSettings filterSettings;

	private FightEquippedItemAdapter fightItemAdapter;
	private FightModificatorAdapter fightModificatorAdapter;
	private SackOfViewsAdapter evadeAdapter;
	private MergeAdapter fightMergeAdapter;

	public static class TargetListener implements View.OnClickListener {
		private MainActivity activity;

		/**
		 * 
		 */
		public TargetListener(MainActivity activity) {
			this.activity = activity;
		}

		public void onClick(View v) {
			if (v.getTag() instanceof EquippedItem) {

				EquippedItem item = (EquippedItem) v.getTag();
				ArcheryChooserDialog targetChooserDialog = new ArcheryChooserDialog(activity);
				targetChooserDialog.setWeapon(item);
				targetChooserDialog.show();

			}
		}
	}

	private TargetListener targetListener;

	private Button fightPickerButton;
	private AttributeType fightPickerType = AttributeType.Lebensenergie;

	private List<AttributeType> fightPickerTypes;

	private Object menuItem;

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
	 * @see com.dsatab.fragment.BaseFragment#onFilterChanged(com.dsatab.view.
	 * FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {
		if (type == FilterType.Fight && settings instanceof FightFilterSettings) {

			FightFilterSettings newSettings = (FightFilterSettings) settings;
			if (filterSettings.equals(newSettings))
				return;

			if (filterSettings.isShowArmor() != newSettings.isShowArmor()
					|| filterSettings.isIncludeModifiers() != newSettings.isIncludeModifiers()) {
				filterSettings.set(newSettings);
				fightItemAdapter.filter(filterSettings);
				updateAusweichen();
			}

			if (filterSettings.isShowModifier() != newSettings.isShowModifier()) {
				filterSettings.setShowModifier(newSettings.isShowModifier());
				fightModificatorAdapter.filter(filterSettings);
			}

			if (filterSettings.isShowEvade() != newSettings.isShowEvade()) {
				filterSettings.setShowEvade(newSettings.isShowEvade());
				updateAusweichen();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_modifier_add, Menu.NONE,
				"Modifikator hinzufügen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		item.setIcon(R.drawable.ic_menu_add);

		if (menu.findItem(R.id.option_fight_set) == null) {
			item = menu.add(Menu.NONE, R.id.option_fight_set, Menu.NONE, "Set");
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setIcon(R.drawable.ic_menu_set);
			if (item.getIcon() instanceof LevelListDrawable) {
				((LevelListDrawable) item.getIcon()).setLevel(getHero().getActiveSet());
			}
		}

		if (menu.findItem(R.id.option_filter) == null) {
			item = menu.add(Menu.NONE, R.id.option_filter, Menu.NONE, "Filtern");
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setIcon(R.drawable.ic_menu_filter);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.
	 * actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

		if (item.getItemId() == R.id.option_modifier_add) {
			startActivityForResult(new Intent(getActivity(), ModificatorEditActivity.class),
					MainActivity.ACTION_ADD_MODIFICATOR);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
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
	 * android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu
	 * , android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		if (v == fightList) {

			int position = ((AdapterContextMenuInfo) menuInfo).position;

			if (position >= 0) {

				menuItem = fightMergeAdapter.getItem(position);

				if (menuItem instanceof EquippedItem) {

					EquippedItem equippedItem = (EquippedItem) menuItem;

					menu.setHeaderTitle(equippedItem.getItemName());

					if (equippedItem.getItem().hasImage()) {
						menu.add(0, CONTEXTMENU_VIEWEQUIPPEDITEM, 0, getString(R.string.menu_view_item)).setIcon(
								R.drawable.ic_menu_view);
					}

					if (equippedItem.getItem().hasSpecification(Shield.class)) {
						menu.add(0, CONTEXTMENU_ASSIGN_PRIMARY, 0, getString(R.string.menu_assign_main_weapon))
								.setIcon(R.drawable.ic_menu_share);
					}
					if (equippedItem.getItem().hasSpecification(Weapon.class)) {
						Weapon weapon = (Weapon) equippedItem.getItem().getSpecification(Weapon.class);
						if (!weapon.isTwoHanded()) {
							menu.add(0, CONTEXTMENU_ASSIGN_SECONDARY, 0,
									getString(R.string.menu_assign_secondary_weapon)).setIcon(R.drawable.ic_menu_share);
						}
					}

					if (equippedItem.getItem().hasSpecification(DistanceWeapon.class)) {
						menu.add(0, CONTEXTMENU_ASSIGN_HUNTING, 0, getString(R.string.menu_assign_hunting_weapon))
								.setIcon(R.drawable.ic_menu_star);

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
					return;
				} else if (menuItem instanceof CustomModificator) {
					CustomModificator modificator = (CustomModificator) menuItem;

					MenuInflater menuInflater = getActivity().getMenuInflater();
					menuInflater.inflate(R.menu.modifikator_popupmenu, menu);
					return;

				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onContextItemSelected(android.support
	 * .v4.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (menuItem instanceof EquippedItem) {

			final EquippedItem equippedItem = (EquippedItem) menuItem;

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
				fightItemAdapter.notifyDataSetChanged();
			}
		} else if (menuItem instanceof CustomModificator) {

			CustomModificator modificator = (CustomModificator) menuItem;

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
		return super.onContextItemSelected(item);
	}

	private void updateAusweichen() {

		if (filterSettings.isShowEvade()) {

			if (!fightMergeAdapter.containsAdapter(evadeAdapter)) {
				fightMergeAdapter.addAdapter(1, evadeAdapter);
				fightMergeAdapter.notifyDataSetChanged();
			}

			Attribute ausweichen = getHero().getAttribute(AttributeType.Ausweichen);
			TextView text1 = (TextView) fightausweichen.findViewById(android.R.id.text1);

			StyleableSpannableStringBuilder title = new StyleableSpannableStringBuilder();
			title.append(ausweichen.getName());

			Util.appendValue(getHero(), title, ausweichen, null,
					preferences.getBoolean(FilterDialog.PREF_KEY_INCLUDE_MODIFIER, true));

			text1.setText(title);
			final TextView text2 = (TextView) fightausweichen.findViewById(android.R.id.text2);
			text2.setText("Modifikator " + Util.toProbe(ausweichen.getProbeInfo().getErschwernis()));

			// fightausweichen.setBackgroundResource(getFightItemBackgroundResource(fightausweichen));

			ImageButton iconLeft = (ImageButton) fightausweichen.findViewById(android.R.id.icon1);
			iconLeft.setTag(ausweichen);

		} else {
			if (fightMergeAdapter.containsAdapter(evadeAdapter)) {
				fightMergeAdapter.removeAdapter(evadeAdapter);
				fightMergeAdapter.notifyDataSetChanged();
			}
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
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = configureContainerView(inflater.inflate(R.layout.sheet_fight, container, false));

		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		fightList = (ListView) getView().findViewById(R.id.fight_list);

		filterSettings = new FightFilterSettings(preferences.getBoolean(FilterDialog.PREF_KEY_SHOW_ARMOR, true),
				preferences.getBoolean(FilterDialog.PREF_KEY_SHOW_MODIFIER, true), preferences.getBoolean(
						FilterDialog.PREF_KEY_SHOW_EVADE, true), preferences.getBoolean(
						FilterDialog.PREF_KEY_INCLUDE_MODIFIER, true));

		fightItemAdapter = new FightEquippedItemAdapter(getActivity(), getHero(), filterSettings);

		targetListener = new TargetListener((MainActivity) getActivity());

		fightItemAdapter.setProbeListener(getBaseActivity().getProbeListener());
		fightItemAdapter.setTargetListener(targetListener);

		fightMergeAdapter = new MergeAdapter();

		fightMergeAdapter.addAdapter(fightItemAdapter);

		fightausweichen = getLayoutInflater(savedInstanceState).inflate(R.layout.item_listitem, null, false);
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

		List<View> evadeViews = new ArrayList<View>(1);
		evadeViews.add(fightausweichen);
		evadeAdapter = new SackOfViewsAdapter(evadeViews);
		fightMergeAdapter.addAdapter(evadeAdapter);

		fightModificatorAdapter = new FightModificatorAdapter(getActivity());

		fightMergeAdapter.addAdapter(fightModificatorAdapter);

		fightList.setAdapter(fightMergeAdapter);
		fightList.setOnItemClickListener(this);
		registerForContextMenu(fightList);

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

		updateAusweichen();

		fightModificatorAdapter.setNotifyOnChange(false);
		fightModificatorAdapter.clear();
		for (Modificator mod : hero.getModificators()) {
			fightModificatorAdapter.add(mod);
		}
		fightModificatorAdapter.notifyDataSetChanged();

	}

	/**
	 * 
	 */

	@Override
	public void onModifierAdded(Modificator value) {
		fightModificatorAdapter.add(value);
		fightItemAdapter.notifyDataSetChanged();
		updateAusweichen();
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
		fightModificatorAdapter.remove(value);
		fightItemAdapter.notifyDataSetChanged();
		updateAusweichen();
	}

	@Override
	public void onModifierChanged(Modificator value) {
		fightModificatorAdapter.notifyDataSetChanged();
		fightItemAdapter.notifyDataSetChanged();
		updateAusweichen();
	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {
		fightModificatorAdapter.notifyDataSetChanged();
		fightItemAdapter.notifyDataSetChanged();
		updateAusweichen();
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

		float ratio = 1.0f;

		if (value.getType() == AttributeType.Lebensenergie) {
			ratio = getHero().getLeRatio();
		} else if (value.getType() == AttributeType.Ausdauer) {
			ratio = getHero().getAuRatio();
		} else if (value.getType() == AttributeType.Karmaenergie) {
			ratio = getHero().getKeRatio();
		} else if (value.getType() == AttributeType.Astralenergie) {
			ratio = getHero().getAeRatio();
		}

		if (ratio < 0.5f)
			fightNumberAdapter.setTextColor(getResources().getColor(R.color.ValueRed));
		else if (ratio < 1.0f)
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
				updateAusweichen();
				break;
			case Ausweichen: {
				updateAusweichen();
				break;
			}
			case Körperkraft:
				fightItemAdapter.notifyDataSetChanged();
				break;
			}
		}

	}

	private void fillFightItemDescriptions() {
		List<EquippedItem> items = getHero().getEquippedItems();
		Util.sort(items);

		fightItemAdapter.setNotifyOnChange(false);
		fightItemAdapter.clear();
		for (EquippedItem equippedItem : items) {
			fightItemAdapter.add(equippedItem);
		}
		fightItemAdapter.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		if (parent == fightList) {

			Object object = fightMergeAdapter.getItem(position);

			if (object instanceof AbstractModificator) {
				AbstractModificator modificator = (AbstractModificator) object;
				modificator.setActive(!modificator.isActive());
				fightModificatorAdapter.notifyDataSetChanged();
			}
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
		getActivity().supportInvalidateOptionsMenu();
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
		if (item.getSet() == getHero().getActiveSet())
			fightItemAdapter.notifyDataSetChanged();
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
		if (item.getSet() == getHero().getActiveSet()) {
			fightItemAdapter.add(item);
		}
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
		if (item.getSet() == getHero().getActiveSet()) {
			fightItemAdapter.remove(item);
		}

	}

}
