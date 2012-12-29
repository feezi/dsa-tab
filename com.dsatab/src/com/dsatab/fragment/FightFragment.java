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
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.commonsware.cwac.merge.MergeAdapter;
import com.commonsware.cwac.sacklist.SackOfViewsAdapter;
import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.activity.MainActivity;
import com.dsatab.activity.ModificatorEditActivity;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.data.Advantage;
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
import com.dsatab.util.Util;
import com.dsatab.view.ArcheryChooserDialog;
import com.dsatab.view.EquippedItemChooserDialog;
import com.dsatab.view.EvadeChooserDialog;
import com.dsatab.view.FightFilterSettings;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;

public class FightFragment extends BaseListFragment implements OnLongClickListener, OnClickListener,
		OnItemClickListener {

	private static final String KEY_PICKER_TYPE = "pickerType";

	private WheelView fightNumberPicker;
	private NumericWheelAdapter fightNumberAdapter;
	private LinearLayout fightLpLayout;

	private View fightausweichen;

	private ListView fightList;

	private FightEquippedItemAdapter fightItemAdapter;
	private FightModificatorAdapter fightModificatorAdapter;
	private SackOfViewsAdapter evadeAdapter;
	private MergeAdapter fightMergeAdapter;

	private final class ModifierActionMode implements ActionMode.Callback {
		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			boolean notifyChanged = false;

			SparseBooleanArray checkedPositions = fightList.getCheckedItemPositions();
			if (checkedPositions != null) {
				for (int i = checkedPositions.size() - 1; i >= 0; i--) {
					if (checkedPositions.valueAt(i)) {
						Object obj = fightList.getItemAtPosition(checkedPositions.keyAt(i));

						if (obj instanceof CustomModificator) {
							CustomModificator modificator = (CustomModificator) obj;

							switch (item.getItemId()) {
							case R.id.option_edit:
								Intent intent = new Intent(getActivity(), ModificatorEditActivity.class);
								intent.putExtra(ModificatorEditActivity.INTENT_ID, modificator.getId());
								intent.putExtra(ModificatorEditActivity.INTENT_NAME, modificator.getModificatorName());
								intent.putExtra(ModificatorEditActivity.INTENT_RULES, modificator.getRules());
								intent.putExtra(ModificatorEditActivity.INTENT_COMMENT, modificator.getComment());
								intent.putExtra(ModificatorEditActivity.INTENT_ACTIVE, modificator.isActive());
								getActivity().startActivityForResult(intent, MainActivity.ACTION_EDIT_MODIFICATOR);
								mode.finish();
								return true;
							case R.id.option_delete:
								getHero().removeModificator(modificator);
								break;
							}
						}
					}

				}
				if (notifyChanged) {
					fightModificatorAdapter.notifyDataSetChanged();
				}
			}
			mode.finish();
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			boolean hasItems = false;
			boolean hasModifiers = false;

			SparseBooleanArray checkedPositions = fightList.getCheckedItemPositions();
			if (checkedPositions != null) {
				for (int i = checkedPositions.size() - 1; i >= 0; i--) {
					if (checkedPositions.valueAt(i)) {

						Object obj = fightList.getItemAtPosition(checkedPositions.keyAt(i));

						if (obj instanceof CustomModificator) {
							hasModifiers = true;
						}
						if (obj instanceof EquippedItem) {
							hasItems = true;
						}

					}
				}
			}

			if (hasModifiers && !hasItems) {
				mode.getMenuInflater().inflate(R.menu.modifikator_popupmenu, menu);
			} else if (hasItems && !hasModifiers) {
				mode.getMenuInflater().inflate(R.menu.equipped_item_popupmenu, menu);
			}

			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
			fightList.clearChoices();
			fightModificatorAdapter.notifyDataSetChanged();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			SparseBooleanArray checkedPositions = fightList.getCheckedItemPositions();
			if (checkedPositions != null) {
				for (int i = checkedPositions.size() - 1; i >= 0; i--) {
					if (checkedPositions.valueAt(i)) {
						Object obj = fightList.getItemAtPosition(checkedPositions.keyAt(i));

						if (obj instanceof CustomModificator) {
							if (!menu.findItem(R.id.option_edit).isEnabled()) {
								menu.findItem(R.id.option_edit).setEnabled(true);
								return true;
							} else {
								return false;
							}
						} else {
							if (menu.findItem(R.id.option_edit).isEnabled()) {
								menu.findItem(R.id.option_edit).setEnabled(false);
								return true;
							} else {
								return false;
							}
						}

					}
				}
			}

			return false;
		}
	}

	private final class EquippedItemActionMode implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			boolean notifyChanged = false;

			SparseBooleanArray checkedPositions = fightList.getCheckedItemPositions();
			if (checkedPositions != null) {
				for (int i = checkedPositions.size() - 1; i >= 0; i--) {
					if (checkedPositions.valueAt(i)) {
						Object obj = fightList.getItemAtPosition(checkedPositions.keyAt(i));

						if (obj instanceof EquippedItem) {

							final EquippedItem equippedItem = (EquippedItem) obj;

							if (item.getItemId() == R.id.option_view) {

								Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
								intent.setAction(Intent.ACTION_VIEW);
								intent.putExtra(ItemChooserFragment.INTENT_EXTRA_EQUIPPED_ITEM_ID, equippedItem.getId());
								intent.putExtra(ItemChooserFragment.INTENT_EXTRA_SEARCHABLE, false);
								intent.putExtra(ItemChooserFragment.INTENT_EXTRA_CATEGORY_SELECTABLE, false);
								startActivity(intent);

							} else if (item.getItemId() == R.id.option_assign_primary) {

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

											// remove 2way relation if old
											// secondary item existed
											if (equippedWeapon.getSecondaryItem() != null
													&& equippedWeapon.getSecondaryItem().getSecondaryItem() != null) {
												Debug.verbose("Removing old weapon sec item "
														+ equippedWeapon.getSecondaryItem());
												equippedWeapon.getSecondaryItem().setSecondaryItem(null);
											}
											if (equippedShield.getSecondaryItem() != null
													&& equippedShield.getSecondaryItem().getSecondaryItem() != null) {
												Debug.verbose("Removing old shield sec item "
														+ equippedWeapon.getSecondaryItem());
												equippedShield.getSecondaryItem().setSecondaryItem(null);
											}

											equippedShield.setSecondaryItem(equippedWeapon);
											equippedWeapon.setSecondaryItem(equippedShield);

											fillFightItemDescriptions();
										}
									}
								});

							} else if (item.getItemId() == R.id.option_assign_secondary) {

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

											// remove 2way relation if old
											// secondary item existed
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

							} else if (item.getItemId() == R.id.option_unassign) {

								final EquippedItem equippedPrimaryWeapon = equippedItem;
								EquippedItem equippedSecondaryWeapon = equippedPrimaryWeapon.getSecondaryItem();

								equippedPrimaryWeapon.setSecondaryItem(null);
								equippedSecondaryWeapon.setSecondaryItem(null);

								fillFightItemDescriptions();

							} else if (item.getItemId() == R.id.option_select_version) {

								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

								List<String> specInfo = equippedItem.getItem().getSpecificationNames();

								builder.setItems(specInfo.toArray(new String[0]),
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												equippedItem.setItemSpecification(getActivity(), equippedItem.getItem()
														.getSpecifications().get(which));
												dialog.dismiss();
											}
										});

								builder.setTitle("Wähle eine Variante...");
								builder.show();
							} else if (item.getItemId() == R.id.option_select_talent) {

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
									builder.setItems(specInfo.toArray(new String[0]),
											new DialogInterface.OnClickListener() {
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
							} else if (item.getItemId() == R.id.option_assign_hunting) {
								getHero().setHuntingWeapon(equippedItem);
								notifyChanged = true;
							} else if (item.getItemId() == R.id.option_delete) {
								getHero().removeEquippedItem(equippedItem);
							}
						}
					}

				}
				if (notifyChanged) {
					fightItemAdapter.notifyDataSetChanged();
				}
			}
			mode.finish();
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.equipped_item_popupmenu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
			fightList.clearChoices();
			fightItemAdapter.notifyDataSetChanged();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			SparseBooleanArray checkedPositions = fightList.getCheckedItemPositions();
			if (checkedPositions != null) {
				for (int i = checkedPositions.size() - 1; i >= 0; i--) {
					if (checkedPositions.valueAt(i)) {
						Object obj = fightList.getItemAtPosition(checkedPositions.keyAt(i));

						if (obj instanceof EquippedItem) {
							EquippedItem equippedItem = (EquippedItem) obj;

							menu.findItem(R.id.option_view).setVisible(equippedItem.getItem().hasImage());

							menu.findItem(R.id.option_assign_primary).setVisible(
									equippedItem.getItem().hasSpecification(Shield.class));

							menu.findItem(R.id.option_assign_secondary).setVisible(false);
							if (equippedItem.getItem().hasSpecification(Weapon.class)) {
								Weapon weapon = (Weapon) equippedItem.getItem().getSpecification(Weapon.class);
								if (!weapon.isTwoHanded()) {
									menu.findItem(R.id.option_assign_secondary).setVisible(true);
								}
							}

							menu.findItem(R.id.option_assign_hunting).setVisible(
									equippedItem.getItem().hasSpecification(DistanceWeapon.class));

							menu.findItem(R.id.option_unassign).setVisible(equippedItem.getSecondaryItem() != null);
							menu.findItem(R.id.option_select_version).setVisible(
									equippedItem.getItem().getSpecifications().size() > 1);

							boolean hasMultipleTalentTypes = false;
							if (equippedItem.getItemSpecification() instanceof Weapon) {
								Weapon weapon = (Weapon) equippedItem.getItemSpecification();
								if (weapon.getCombatTalentTypes().size() > 1) {
									hasMultipleTalentTypes = true;
								}
							} else if (equippedItem.getItemSpecification() instanceof Shield) {
								Shield shield = (Shield) equippedItem.getItemSpecification();
								if (shield.getCombatTalentTypes().size() > 1) {
									hasMultipleTalentTypes = true;
								}
							}
							menu.findItem(R.id.option_select_talent).setVisible(hasMultipleTalentTypes);

						}
					}
				}
			}

			return true;
		}
	}

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
	private AttributeType fightPickerType = AttributeType.Lebensenergie_Aktuell;

	private List<AttributeType> fightPickerTypes;

	private Callback mItemsCallback = new EquippedItemActionMode();

	private Callback mModifiersCallback = new ModifierActionMode();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == MainActivity.ACTION_ADD_MODIFICATOR) {

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

	protected Callback getActionModeCallback(List<Object> objects) {
		boolean hasItems = false;
		boolean hasModifiers = false;
		for (Object o : objects) {
			if (o instanceof EquippedItem)
				hasItems = true;
			else if (o instanceof CustomModificator)
				hasModifiers = true;
		}

		if (hasItems && !hasModifiers) {
			return mItemsCallback;
		} else if (hasModifiers && !hasItems)
			return mModifiersCallback;
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onFilterChanged(com.dsatab.view.
	 * FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {
		if (fightMergeAdapter != null && (type == FilterType.Fight || type == null)
				&& settings instanceof FightFilterSettings) {

			Debug.verbose("fight filter " + settings);

			FightFilterSettings newSettings = (FightFilterSettings) settings;

			fightItemAdapter.filter(newSettings);
			fightMergeAdapter.setActive(fightModificatorAdapter, newSettings.isShowModifier());
			updateAusweichen();

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
		super.onCreateOptionsMenu(menu, inflater);
		menu.removeItem(R.id.option_set);
		inflater.inflate(R.menu.fight_menu, menu);
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
			getActivity().startActivityForResult(new Intent(getActivity(), ModificatorEditActivity.class),
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

		mCallback = new ModifierActionMode();
	}

	private void updateAusweichen() {
		if (evadeAdapter == null || fightMergeAdapter == null)
			return;

		fightMergeAdapter.setActive(evadeAdapter, getFilterSettings().isShowEvade());

		if (getFilterSettings().isShowEvade()) {

			Attribute ausweichen = getHero().getAttribute(AttributeType.Ausweichen);
			TextView text1 = (TextView) fightausweichen.findViewById(android.R.id.text1);

			StyleableSpannableStringBuilder title = new StyleableSpannableStringBuilder();
			title.append(ausweichen.getName());

			Util.appendValue(getHero(), title, ausweichen, null, getFilterSettings() != null ? getFilterSettings()
					.isIncludeModifiers() : true);

			text1.setText(title);
			final TextView text2 = (TextView) fightausweichen.findViewById(android.R.id.text2);
			text2.setText("Modifikator " + Util.toProbe(ausweichen.getProbeInfo().getErschwernis()));

			// fightausweichen.setBackgroundResource(getFightItemBackgroundResource(fightausweichen));

			ImageButton iconLeft = (ImageButton) fightausweichen.findViewById(android.R.id.icon1);
			iconLeft.setTag(ausweichen);

			Util.applyRowStyle(fightausweichen, fightItemAdapter.getCount());
		}

	}

	protected FightFilterSettings getFilterSettings() {
		return (FightFilterSettings) super.getFilterSettings();
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

		fightList = (ListView) root.findViewById(R.id.fight_list);
		fightPickerButton = (Button) root.findViewById(R.id.fight_btn_picker);
		fightNumberPicker = (WheelView) root.findViewById(R.id.fight_picker);
		fightLpLayout = (LinearLayout) root.findViewById(R.id.fight_le_layout);
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		fightList.setOnItemLongClickListener(this);
		fightList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		fightList.setOnItemClickListener(this);

		targetListener = new TargetListener((MainActivity) getActivity());

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

		fightPickerButton.setOnClickListener(this);
		fightPickerButton.setOnLongClickListener(this);

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

		fightLpLayout.setOnClickListener(getBaseActivity().getEditListener());

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		try {
			String typeString = pref.getString(KEY_PICKER_TYPE, AttributeType.Lebensenergie_Aktuell.name());
			fightPickerType = AttributeType.valueOf(typeString);
		} catch (Exception e) {
			Debug.error(e);
			fightPickerType = AttributeType.Lebensenergie_Aktuell;
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

		fightItemAdapter = new FightEquippedItemAdapter(getActivity(), getHero(), getHero().getEquippedItems(),
				getFilterSettings());
		fightItemAdapter.setProbeListener(getBaseActivity().getProbeListener());
		fightItemAdapter.setTargetListener(targetListener);

		fightMergeAdapter = new MergeAdapter();
		fightMergeAdapter.addAdapter(fightItemAdapter);

		List<View> evadeViews = new ArrayList<View>(1);
		evadeViews.add(fightausweichen);
		evadeAdapter = new SackOfViewsAdapter(evadeViews);

		fightMergeAdapter.addAdapter(evadeAdapter);
		fightMergeAdapter.setActive(evadeAdapter, getFilterSettings().isShowEvade());

		fightModificatorAdapter = new FightModificatorAdapter(getActivity(), hero.getModificators());
		fightMergeAdapter.addAdapter(fightModificatorAdapter);
		fightMergeAdapter.setActive(fightModificatorAdapter, getFilterSettings().isShowModifier());

		fightPickerTypes = new ArrayList<AttributeType>(6);
		fightPickerTypes.add(AttributeType.Lebensenergie_Aktuell);
		fightPickerTypes.add(AttributeType.Ausdauer_Aktuell);

		if (hero.getAttributeValue(AttributeType.Astralenergie_Aktuell) != null
				&& hero.getAttributeValue(AttributeType.Astralenergie_Aktuell) >= 0) {
			fightPickerTypes.add(AttributeType.Astralenergie_Aktuell);
		}
		if (hero.getAttributeValue(AttributeType.Karmaenergie_Aktuell) != null
				&& hero.getAttributeValue(AttributeType.Karmaenergie_Aktuell) >= 0) {
			fightPickerTypes.add(AttributeType.Karmaenergie_Aktuell);
			fightPickerTypes.add(AttributeType.Entrueckung);
		}
		if (getHero().hasFeature(Advantage.MONDSUECHTIG)) {
			fightPickerTypes.add(AttributeType.Verzueckung);
		}

		fightPickerTypes.add(AttributeType.Initiative_Aktuell);
		fightPickerTypes.add(AttributeType.Erschoepfung);

		if (!fightPickerTypes.contains(fightPickerType)) {
			fightPickerType = fightPickerTypes.get(0);
		}
		// fight
		Attribute attr = hero.getAttribute(fightPickerType);
		updateNumberPicker(attr);

		updateAusweichen();

		fightList.setAdapter(fightMergeAdapter);
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

		float ratio;

		switch (value.getType()) {
		case Lebensenergie_Aktuell:
		case Ausdauer_Aktuell:
		case Karmaenergie_Aktuell:
		case Astralenergie_Aktuell:
			ratio = getHero().getRatio(value.getType());
			break;
		default:
			ratio = 1.0f;
			break;
		}

		if (ratio < 0.5f)
			fightNumberAdapter.setTextColor(getResources().getColor(R.color.ValueRed));
		else if (ratio < 1.0f)
			fightNumberAdapter.setTextColor(getResources().getColor(android.R.color.black));
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
			default:
				// do nothing
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
			if (mMode == null) {
				Object object = fightMergeAdapter.getItem(position);

				if (object instanceof AbstractModificator) {
					AbstractModificator modificator = (AbstractModificator) object;
					modificator.setActive(!modificator.isActive());
					fightModificatorAdapter.notifyDataSetChanged();
				}
				fightList.setItemChecked(position, false);
			} else {
				super.onItemClick(parent, v, position, id);
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
