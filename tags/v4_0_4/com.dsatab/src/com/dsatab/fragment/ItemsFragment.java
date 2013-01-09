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
package com.dsatab.fragment;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.animoto.android.views.DraggableGridView;
import com.animoto.android.views.OnRearrangeListener;
import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.data.Hero;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.adapter.GridItemAdapter;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;
import com.dsatab.view.CardView;
import com.dsatab.view.PageButton;
import com.dsatab.xml.DataManager;

public class ItemsFragment extends BaseFragment implements View.OnClickListener, OnItemSelectedListener,
		OnItemClickListener {

	private static final int ACTION_CHOOSE_CARD = 2;

	private static final String PREF_KEY_LAST_OPEN_SCREEN = "_lastopenscreen";

	private DraggableGridView mWorkspace;

	private GridItemAdapter itemAdapter;

	private PageButton mPreviousView, mNextView;
	private int mCurrentScreen = -1;

	private ItemCard selectedItem = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {

		itemAdapter = new GridItemAdapter(getActivity());

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		int screen = pref.getInt(PREF_KEY_LAST_OPEN_SCREEN, 0);
		showScreen(screen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ACTION_CHOOSE_CARD && resultCode == Activity.RESULT_OK) {

			Item item = null;

			final int cellNumber = data.getIntExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CELL,
					ItemLocationInfo.INVALID_POSITION);

			UUID id = (UUID) data.getSerializableExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_ID);
			String cardName = data.getStringExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_NAME);

			Hero hero = getHero();

			if (id != null) {
				item = hero.getItem(id);
			}
			if (item == null && !TextUtils.isEmpty(cardName)) {

				// on a set page check whether he already has the item and reuse
				// it
				if (getActiveSet() >= 0) {
					item = hero.getItem(cardName);
				}
				if (item == null) {
					Item card = DataManager.getItemByName(cardName);
					item = (Item) card.duplicate();
				}
			}

			if (item != null) {

				if (cellNumber != ItemLocationInfo.INVALID_POSITION && item.getItemInfo().getCellNumber() == cellNumber) {
					// the icon is already in the screen no need to add it again
				} else {
					if (selectedItem != null && selectedItem.getItem().equals(item.getName())) {
						// the icon is already in the screen no need to add it
						// again
					} else {
						int cell = cellNumber;
						if (selectedItem != null) {
							cell = itemAdapter.getPosition(selectedItem);

							if (selectedItem instanceof EquippedItem)
								getHero().removeEquippedItem((EquippedItem) selectedItem);
							else
								getHero().removeItem(selectedItem.getItem());
						}

						item.getItemInfo().setCellNumber(cell);
						item.getItemInfo().setScreen(mCurrentScreen);

						CardView cardView = (CardView) mWorkspace.getChildAt(cell);
						if (cardView != null) {
							cardView.setItem(item);
						}
						if (isSetIndex(mCurrentScreen)) {
							hero.addEquippedItem(getBaseActivity(), item, null, null, getActiveSet());
						} else {
							hero.addItem(item);
						}
					}
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public static boolean isSetIndex(int index) {
		return index >= 0 && index < Hero.MAXIMUM_SET_NUMBER;
	}

	private int getActiveSet() {
		if (!isSetIndex(mCurrentScreen))
			return -1;
		else
			return mCurrentScreen;
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
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.item_grid_menu, menu);
		updateActionBarIcons(menu, mCurrentScreen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onPrepareOptionsMenu(com.
	 * actionbarsherlock.view.Menu)
	 */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		updateActionBarIcons(menu, mCurrentScreen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.
	 * actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.option_item_add_table:
			selectItem(null);
			return true;
		case R.id.option_itemgrid_set1:
			showScreen(0);
			getSherlockActivity().invalidateOptionsMenu();
			return true;
		case R.id.option_itemgrid_set2:
			showScreen(1);
			getSherlockActivity().invalidateOptionsMenu();
			return true;
		case R.id.option_itemgrid_set3:
			showScreen(2);
			getSherlockActivity().invalidateOptionsMenu();
			return true;
		case R.id.option_itemgrid_inventory:
			showScreen(Hero.MAXIMUM_SET_NUMBER);
			getSherlockActivity().invalidateOptionsMenu();
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
		View root = configureContainerView(inflater.inflate(R.layout.sheet_items_table, container, false));

		mWorkspace = (DraggableGridView) root.findViewById(R.id.workspace);

		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// 192*288
		// 120*180

		mPreviousView = (PageButton) findViewById(R.id.previous_screen);
		mNextView = (PageButton) findViewById(R.id.next_screen);

		mPreviousView.setMaxLevel(Hero.MAXIMUM_SET_NUMBER);
		mNextView.setMaxLevel(Hero.MAXIMUM_SET_NUMBER);

		mPreviousView.setHapticFeedbackEnabled(false);
		mPreviousView.setOnClickListener(this);
		mNextView.setHapticFeedbackEnabled(false);
		mNextView.setOnClickListener(this);

		mWorkspace.setOnItemClickListener(this);
		mWorkspace.setOnRearrangeListener(new OnRearrangeListener() {
			public void onRearrange(int oldIndex, int newIndex) {

				Debug.verbose("Moving from " + oldIndex + " to " + newIndex);

				ItemCard oldItemCard = itemAdapter.getItem(oldIndex);
				ItemCard newItemCard = itemAdapter.getItem(newIndex);
				int oldItemIndex = oldItemCard.getItemInfo().getCellNumber();

				oldItemCard.getItemInfo().setCellNumber(newItemCard.getItemInfo().getCellNumber());
				newItemCard.getItemInfo().setCellNumber(oldItemIndex);
			}
		});

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (view instanceof CardView) {
			CardView cardView = (CardView) view;
			if (cardView.getItem() != null) {
				selectItem(cardView.getItem());
			}
		}
	}

	private void updateIndicators(int screen) {
		mPreviousView.setLevel(screen);
		mNextView.setLevel(Hero.MAXIMUM_SET_NUMBER - screen);
	}

	private void updateActionBarIcons(Menu menu, int newScreen) {
		MenuItem item = menu.findItem(R.id.option_itemgrid_set);
		switch (newScreen) {
		case 0:
			item.setIcon(Util.getThemeResourceId(getActivity(), R.attr.imgBarSet1));
			item.setTitle("Set");
			break;
		case 1:
			item.setIcon(Util.getThemeResourceId(getActivity(), R.attr.imgBarSet2));
			item.setTitle("Set");
			break;
		case 2:
			item.setIcon(Util.getThemeResourceId(getActivity(), R.attr.imgBarSet3));
			item.setTitle("Set");
			break;
		case Hero.MAXIMUM_SET_NUMBER:
			item.setIcon(Util.getThemeResourceId(getActivity(), R.attr.imgBarSet3));
			item.setTitle("Rucksack");
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.drag.Workspace.OnScreenChangeListener#onScreenChange(int,
	 * int)
	 */
	public void onScreenChange(int oldScreen, int newScreen) {
		updateIndicators(newScreen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.drag.Workspace.OnScreenChangeListener#onScreenAdded(int)
	 */
	public void onScreenAdded(int newScreen) {
		getSherlockActivity().invalidateOptionsMenu();
		updateIndicators(mCurrentScreen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putInt(PREF_KEY_LAST_OPEN_SCREEN, mCurrentScreen);
		edit.commit();
	}

	private void fillBodyItems(Hero hero) {

		// mWorkspace.clear();

		if (hero != null && mCurrentScreen >= 0) {

			itemAdapter.setNotifyOnChange(false);
			itemAdapter.clear();
			mWorkspace.setTag(mCurrentScreen);

			if (isSetIndex(mCurrentScreen)) {

				for (EquippedItem item : hero.getEquippedItems(mCurrentScreen)) {
					if (item.getItem() == null) {
						Debug.verbose("Skipping " + item.getName() + "because equippedItem.getItem was null");
						continue;
					}

					itemAdapter.add(item);
					// success = mWorkspace.addItemInScreen(item);

					// is unable to add item at specified position try again
					// without position info (will be added in first empty slot)
					// if (!success
					// && (item.getItemInfo().getCellX() !=
					// ItemLocationInfo.INVALID_POSITION || item
					// .getItemInfo().getCellY() !=
					// ItemLocationInfo.INVALID_POSITION)) {
					//
					// item.getItemInfo().setCellX(ItemLocationInfo.INVALID_POSITION);
					// item.getItemInfo().setCellY(ItemLocationInfo.INVALID_POSITION);
					// success = mWorkspace.addItemInScreen(item);
					// }
					// if (!success) {
					// Debug.verbose("Skipping " + item.getName() +
					// "because inventory page was FULL");
					// }

				}
			} else {
				for (Item item : hero.getItems()) {
					itemAdapter.add(item);
					// if (item.getItemInfo().getScreen() !=
					// ItemLocationInfo.INVALID_POSITION
					// && item.getItemInfo().getScreen() != mCurrentScreen)
					// continue;
					//
					// success = mWorkspace.addItemInScreen(item);
					//
					// // is unable to add item at specified position try again
					// // without position info (will be added in first empty
					// slot)
					// if (!success
					// && (item.getItemInfo().getCellX() !=
					// ItemLocationInfo.INVALID_POSITION || item
					// .getItemInfo().getCellY() !=
					// ItemLocationInfo.INVALID_POSITION)) {
					//
					// item.getItemInfo().setCellX(ItemLocationInfo.INVALID_POSITION);
					// item.getItemInfo().setCellY(ItemLocationInfo.INVALID_POSITION);
					// success = mWorkspace.addItemInScreen(item);
					// }
					//
					// if (!success) {
					// Debug.warning("Unable to add item " + item.getTitle() +
					// "screen " + mCurrentScreen + " full");
					// break;
					// }
				}
			}
			itemAdapter.prepare();
			itemAdapter.notifyDataSetChanged();
		}

		mWorkspace.removeAllViews();
		for (int i = 0; i < itemAdapter.getCount(); i++) {
			View view = itemAdapter.getView(i, null, mWorkspace);
			view.setDuplicateParentStateEnabled(false);
			mWorkspace.addView(view);
		}
	}

	private void showScreen(int screen) {

		if (screen >= 0 && screen <= Hero.MAXIMUM_SET_NUMBER) {
			int oldScreen = mCurrentScreen;

			mCurrentScreen = screen;

			fillBodyItems(getHero());

			onScreenChange(oldScreen, mCurrentScreen);
		}
	}

	private void selectItem(ItemCard itemCard) {
		if (itemCard != null)
			selectedItem = itemCard;
		else
			selectedItem = null;

		Intent intent = new Intent(getActivity(), ItemChooserActivity.class);

		if (selectedItem != null) {
			Item item = selectedItem.getItem();
			intent.setAction(Intent.ACTION_PICK);
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_ID, item.getId());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_NAME, item.getName());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CATEGORY, item.getCategory());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CELL, itemCard.getItemInfo().getCellNumber());
		}

		getActivity().startActivityForResult(intent, ACTION_CHOOSE_CARD);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android
	 * .widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (position != AdapterView.INVALID_POSITION) {
			showScreen(position);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android
	 * .widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.next_screen:
			showScreen(mCurrentScreen + 1);
			return;
		case R.id.previous_screen:
			showScreen(mCurrentScreen - 1);
			return;
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
		if (item.getItemInfo().getScreen() == mCurrentScreen) {
			// skip items that are equippable since they will be equipped using
			// a onItemEquipped Event. this would cause duplicates
			if (item.isEquipable() && isSetIndex(mCurrentScreen))
				return;

			itemAdapter.add(item);
			itemAdapter.sort(ItemCard.CELL_NUMBER_COMPARATOR);
		}
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
		if (item.getItemInfo().getScreen() == mCurrentScreen) {
			itemAdapter.sort(ItemCard.CELL_NUMBER_COMPARATOR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.HeroChangedListener#onItemChanged(com.dsatab
	 * .data.items.Item)
	 */
	@Override
	public void onItemChanged(Item item) {
		if (item.getItemInfo().getScreen() == mCurrentScreen) {
			itemAdapter.sort(ItemCard.CELL_NUMBER_COMPARATOR);
		}
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
		if (item.getItemInfo().getScreen() == mCurrentScreen) {
			itemAdapter.remove(item);
		}
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
		if (item.getItemInfo().getScreen() == mCurrentScreen) {
			itemAdapter.add(item);
			itemAdapter.sort(ItemCard.CELL_NUMBER_COMPARATOR);
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
		if (item.getItemInfo().getScreen() == mCurrentScreen) {
			itemAdapter.remove(item);
		}
	}

}
