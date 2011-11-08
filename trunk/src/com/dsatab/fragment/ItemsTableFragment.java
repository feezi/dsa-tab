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

import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.data.Hero;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.view.CardView;
import com.dsatab.view.ItemsTable;
import com.dsatab.view.PageButton;
import com.dsatab.view.drag.DeleteZone;
import com.dsatab.view.drag.ItemLocationInfo;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragLayer;
import com.gandulf.guilib.drag.DragScroller;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class ItemsTableFragment extends BaseFragment implements View.OnLongClickListener, View.OnClickListener,
		DragController.DragListener<ItemCard>, OnItemSelectedListener, DragScroller {

	private static final int ACTION_CHOOSE_CARD = 2;

	private DragController<ItemCard> mDragController;

	private DragLayer mDragLayer;

	private ItemsTable mWorkspace;

	private Spinner mScreenBtn;
	private SpinnerSimpleAdapter<String> mScreenAdapter;

	PageButton mPreviousView, mNextView;
	private int mCurrentScreen = 0;
	private int mMaxScreens;

	private ItemCard selectedItem = null;

	private ImageButton inventoryButton, setButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {

		int itemCount = hero.getItems().size();

		mMaxScreens = (int) (Hero.MAXIMUM_SET_NUMBER + (Math.ceil(itemCount / 16.0f)));

		mScreenBtn.setOnItemSelectedListener(null);
		mScreenAdapter.setNotifyOnChange(false);
		mScreenAdapter.clear();
		for (int i = 1; i <= Hero.MAXIMUM_SET_NUMBER; i++) {
			mScreenAdapter.add("Set " + i);
		}
		for (int i = 1; i <= mMaxScreens - Hero.MAXIMUM_SET_NUMBER; i++) {
			mScreenAdapter.add("Ausrüstung " + i);
		}

		fillBodyItems(hero);

		mScreenAdapter.setNotifyOnChange(true);
		mScreenAdapter.notifyDataSetChanged();

		showScreen(hero.getActiveSet());
		mScreenBtn.setOnItemSelectedListener(this);
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

			final int cellX = data.getIntExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_X,
					ItemLocationInfo.INVALID_POSITION);
			final int cellY = data.getIntExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_Y,
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

				if (cellX != ItemLocationInfo.INVALID_POSITION && cellY != ItemLocationInfo.INVALID_POSITION
						&& item.getItemInfo().getCellX() == cellX && item.getItemInfo().getCellY() == cellY) {
					// the icon is already in the screen no need to add it again
				} else {
					if (selectedItem != null && selectedItem.getItem().equals(item.getName())) {
						// the icon is already in the screen no need to add it
						// again
					} else {

						hero.addItem(getBaseActivity(), item, null, getActiveSet(), new Hero.ItemAddedCallback() {

							@Override
							public void onItemAdded(Item item) {

								if (selectedItem != null) {
									mWorkspace.replaceItem(selectedItem, item);
									if (selectedItem instanceof EquippedItem)
										getHero().removeEquippedItem((EquippedItem) selectedItem);
									else
										getHero().removeItem(selectedItem.getItem());
								} else {
									item.getItemInfo().setCellX(cellX);
									item.getItemInfo().setCellY(cellY);
									item.getItemInfo().setScreen(mCurrentScreen);
									mWorkspace.addItemInScreen(item);
								}

							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.dsatab.data.Hero.ItemAddedCallback#
							 * onEquippedItemAdded
							 * (com.dsatab.data.items.EquippedItem)
							 */
							@Override
							public void onEquippedItemAdded(EquippedItem item) {
								if (selectedItem != null) {
									mWorkspace.replaceItem(selectedItem, item);
									if (selectedItem instanceof EquippedItem)
										getHero().removeEquippedItem((EquippedItem) selectedItem);
									else
										getHero().removeItem(selectedItem.getItem());
								} else {
									item.getItemInfo().setCellX(cellX);
									item.getItemInfo().setCellY(cellY);
									item.getItemInfo().setScreen(mCurrentScreen);
									mWorkspace.addItemInScreen(item);
								}

							}
						});

					}
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean isSetIndex(int index) {
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
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.sheet_items_table, container, false);

		inventoryButton = (ImageButton) root.findViewById(R.id.fight_inventory);
		setButton = (ImageButton) root.findViewById(R.id.fight_set);

		mScreenBtn = (Spinner) root.findViewById(R.id.screen_set_button);
		mWorkspace = (ItemsTable) root.findViewById(R.id.workspace);
		mDragLayer = (DragLayer) root.findViewById(R.id.sheet_items);
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		inventoryButton.setOnClickListener(this);
		setButton.setOnClickListener(this);
		mScreenAdapter = new SpinnerSimpleAdapter<String>(getActivity(), new ArrayList<String>(10));
		mScreenBtn.setAdapter(mScreenAdapter);

		setupViews();

		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * Finds all the views we need and configure them properly.
	 */
	private void setupViews() {

		// 192*288
		// 120*180

		mDragController = new DragController<ItemCard>(getActivity());

		mDragLayer.setDragController(mDragController);
		mWorkspace.setHapticFeedbackEnabled(false);

		DeleteZone mDeleteZone = (DeleteZone) findViewById(R.id.delete_zone);
		mDeleteZone.setOrientation(DeleteZone.ORIENTATION_HORIZONTAL);

		mPreviousView = (PageButton) findViewById(R.id.previous_screen);
		mNextView = (PageButton) findViewById(R.id.next_screen);

		mPreviousView.setHapticFeedbackEnabled(false);
		mPreviousView.setOnClickListener(this);
		mNextView.setHapticFeedbackEnabled(false);
		mNextView.setOnClickListener(this);

		mWorkspace.setOnLongClickListener(this);
		mWorkspace.setOnClickListener(this);
		mWorkspace.setDragController(mDragController);
		// mWorkspace.setOnScreenChangeListener(this);

		mDeleteZone.setDragController(mDragController);

		mDragController.setDragScoller(this);
		mDragController.addDragListener(mDeleteZone);
		mDragController.addDragListener(this);
		mDragController.setMoveTarget(mWorkspace);

		// The order here is bottom to top.
		mDragController.addDropTarget(mDeleteZone);
		mDragController.addDropTarget(mWorkspace);
	}

	private void updateIndicators(int screen) {
		mPreviousView.setLevel(screen);
		mNextView.setLevel(mMaxScreens - 1 - screen);
		mPreviousView.setMaxLevel(mMaxScreens - 1);
		mNextView.setMaxLevel(mMaxScreens - 1);

		if (screen < Hero.MAXIMUM_SET_NUMBER) {
			LevelListDrawable drawable = (LevelListDrawable) setButton.getDrawable();
			drawable.setLevel(screen);

			inventoryButton.setSelected(false);
			setButton.setSelected(true);

		} else {
			LevelListDrawable drawable = (LevelListDrawable) inventoryButton.getDrawable();
			drawable.setLevel(screen - Hero.MAXIMUM_SET_NUMBER);

			inventoryButton.setSelected(true);
			setButton.setSelected(false);
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
		if (isSetIndex(newScreen))
			getHero().setActiveSet(newScreen);
		else
			updateSpinner(newScreen);

		updateIndicators(newScreen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.drag.Workspace.OnScreenChangeListener#onScreenAdded(int)
	 */
	public void onScreenAdded(int newScreen) {
		mScreenAdapter.add("Ausrüstung " + (newScreen + 1 - Hero.MAXIMUM_SET_NUMBER));
		updateIndicators(mCurrentScreen);
	}

	private void updateSpinner(int newScreen) {
		if (mScreenBtn.getSelectedItemPosition() != newScreen)
			mScreenBtn.setSelection(newScreen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.drag.DragController.DragListener#onDragStart(com.dsatab
	 * .view.drag.DragSource, com.dsatab.data.items.Item, int)
	 */
	@Override
	public void onDragStart(DragSource<ItemCard> source, ItemCard info, int dragAction) {
		mDragController.setScrollView(mDragLayer);
		mScreenBtn.setVisibility(View.INVISIBLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DragController.DragListener#onDragDrop(java.lang
	 * .Object, int, int, int)
	 */
	@Override
	public boolean onDragDrop(View cell, ItemCard dragInfo, int x, int y, int newScreen) {
		int oldScreen = dragInfo.getItemInfo().getScreen();

		if (dragInfo instanceof EquippedItem) {
			EquippedItem equippedItem = (EquippedItem) dragInfo;
			if (oldScreen != newScreen) {
				// drag von set to set
				if (isSetIndex(oldScreen) && isSetIndex(newScreen)) {
					getHero().getEquippedItems(oldScreen).remove(equippedItem);
					getHero().getEquippedItems(newScreen).add(equippedItem);
					equippedItem.setSet(newScreen);

				}
				// drag from set to inventory
				else if (isSetIndex(oldScreen) && !isSetIndex(newScreen)) {
					getHero().getEquippedItems(oldScreen).remove(equippedItem);

					Item item = equippedItem.getItem();

					item.getItemInfo().setCellX(x);
					item.getItemInfo().setCellY(y);
					item.getItemInfo().setScreen(newScreen);

					cell.setTag(item);
				} else {
					Debug.error("Should never happen oldscreen=" + oldScreen + " newscreen=" + newScreen);
				}

			}
		} else if (dragInfo instanceof Item) {
			Item item = (Item) dragInfo;
			if (oldScreen != newScreen) {
				// drag from inventory to set
				if (!isSetIndex(oldScreen) && !isSetIndex(newScreen)) {
					// simple drag between two inventory pages, no special
					// handling needed
				}
				// drag a item from inventory to set (equip it)
				else if (!isSetIndex(oldScreen) && isSetIndex(newScreen)) {
					EquippedItem equippedItem = getHero().addEquippedItem(getActivity(), item, null, newScreen);

					equippedItem.getItemInfo().setCellX(x);
					equippedItem.getItemInfo().setCellY(y);
					equippedItem.getItemInfo().setScreen(newScreen);

					cell.setTag(equippedItem);
				}
				// moving a not equippable item from set to inventory
				else if (isSetIndex(oldScreen) && !isSetIndex(newScreen)) {

				}
			}
		}

		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.view.drag.DragController.DragListener#onDragEnd()
	 */
	@Override
	public void onDragEnd() {
		mScreenBtn.setVisibility(View.VISIBLE);
		mDragController.setScrollView(null);
	}

	private void fillBodyItems(Hero hero) {
		boolean success = true;
		mWorkspace.clear();

		if (hero != null) {

			if (isSetIndex(mCurrentScreen)) {
				for (EquippedItem item : hero.getEquippedItems(mCurrentScreen)) {
					if (item.getItem() == null) {
						Debug.verbose("Skipping " + item.getName() + "because equippedItem.getItem was null");
						continue;
					}
					success = mWorkspace.addItemInScreen(item);

					// is unable to add item at specified position try again
					// without position info (will be added in first empty slot)
					if (!success
							&& (item.getItemInfo().getCellX() != ItemLocationInfo.INVALID_POSITION || item
									.getItemInfo().getCellY() != ItemLocationInfo.INVALID_POSITION)) {

						item.getItemInfo().setCellX(ItemLocationInfo.INVALID_POSITION);
						item.getItemInfo().setCellY(ItemLocationInfo.INVALID_POSITION);
						success = mWorkspace.addItemInScreen(item);
					}
					if (!success) {
						Debug.verbose("Skipping " + item.getName() + "because inventory page was FULL");
						break;
					}

				}
			} else {

				for (Item item : hero.getItems()) {

					if (item.getItemInfo().getScreen() != ItemLocationInfo.INVALID_POSITION
							&& item.getItemInfo().getScreen() != mCurrentScreen)
						continue;

					success = mWorkspace.addItemInScreen(item);

					// is unable to add item at specified position try again
					// without position info (will be added in first empty slot)
					if (!success
							&& (item.getItemInfo().getCellX() != ItemLocationInfo.INVALID_POSITION || item
									.getItemInfo().getCellY() != ItemLocationInfo.INVALID_POSITION)) {

						item.getItemInfo().setCellX(ItemLocationInfo.INVALID_POSITION);
						item.getItemInfo().setCellY(ItemLocationInfo.INVALID_POSITION);
						success = mWorkspace.addItemInScreen(item);
					}

					if (!success) {
						Debug.warning("Unable to add item " + item.getTitle() + "screen " + mCurrentScreen + " full");
						break;
					}
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gandulf.guilib.drag.DragScroller#scrollLeft()
	 */
	@Override
	public void scrollLeft() {
		showScreen(mCurrentScreen - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gandulf.guilib.drag.DragScroller#scrollRight()
	 */
	@Override
	public void scrollRight() {
		showScreen(mCurrentScreen + 1);
	}

	private void showScreen(int screen) {

		if (screen >= 0 && screen < mMaxScreens) {
			int oldScreen = mCurrentScreen;

			Debug.verbose("Show screen " + screen);

			mCurrentScreen = screen;
			mWorkspace.setScreen(mCurrentScreen);

			fillBodyItems(getHero());

			onScreenChange(oldScreen, mCurrentScreen);
		}
	}

	private void selectItem(ItemCard itemCard, CardView cardView) {
		if (itemCard != null)
			selectedItem = itemCard;
		else
			selectedItem = null;

		Intent intent = new Intent(getActivity(), ItemChooserActivity.class);

		if (selectedItem != null) {

			Item item = selectedItem.getItem();

			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_ID, item.getId());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_NAME, item.getName());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CATEGORY, item.getCategory());

			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_X, itemCard.getItemInfo().getCellX());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_Y, itemCard.getItemInfo().getCellY());
		}

		if (cardView != null) {
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_X, cardView.getCellX());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_Y, cardView.getCellY());
		}

		startActivityForResult(intent, ACTION_CHOOSE_CARD);
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
			Debug.verbose("Screen OnItem Selected " + position);
			if (isSetIndex(position))
				getHero().setActiveSet(position);
			else {
				showScreen(position);
			}
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
	 * @see com.dsatab.fragment.BaseFragment#onActiveSetChanged(int, int)
	 */
	@Override
	public void onActiveSetChanged(int newSet, int oldSet) {
		super.onActiveSetChanged(newSet, oldSet);
		showScreen(newSet);
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
		case R.id.fight_set:
			if (isSetIndex(mCurrentScreen))
				getHero().setActiveSet(getHero().getNextActiveSet());
			else {
				showScreen(getHero().getActiveSet());
			}
			return;
		case R.id.fight_inventory:
			LevelListDrawable drawable = (LevelListDrawable) inventoryButton.getDrawable();
			int nextInventory = drawable.getLevel();
			if (!isSetIndex(mCurrentScreen))
				nextInventory++;

			if (nextInventory >= mMaxScreens - Hero.MAXIMUM_SET_NUMBER)
				nextInventory = 0;

			showScreen(Hero.MAXIMUM_SET_NUMBER + nextInventory);
			return;
		}

		if (v instanceof CardView) {
			CardView cardView = (CardView) v;

			if (cardView.getItem() != null) {
				ItemCard item = (ItemCard) v.getTag();
				selectItem(item, cardView);
			} else {

				// empty cell clicked add new item
				mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
				selectItem(null, cardView);

			}
		}
	}

	public boolean onLongClick(View v) {

		if (v instanceof CardView) {
			CardView cardView = (CardView) v;

			if (cardView.getItem() == null) {
				mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
				selectItem(null, cardView);
				return true;
			} else {
				// User long pressed on an item
				mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

				mWorkspace.startDrag(cardView);
			}
		}
		return true;
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
	 * com.dsatab.view.listener.InventoryChangedListener#onItemChanged(com.dsatab
	 * .data.items.EquippedItem)
	 */
	@Override
	public void onItemChanged(EquippedItem item) {

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
		mWorkspace.removeItem(item);
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
		mWorkspace.removeItem(item);
	}

}
