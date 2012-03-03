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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.data.Hero;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.adapter.GridItemAdapter;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.view.DeleteZone;
import com.dsatab.view.GridCardView;
import com.dsatab.view.ItemGridView;
import com.dsatab.view.PageButton;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragLayer;
import com.gandulf.guilib.drag.DragScroller;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class ItemsFragment extends BaseFragment implements View.OnLongClickListener, View.OnClickListener,
		DragController.DragListener<ItemCard>, OnItemSelectedListener, DragScroller, OnItemClickListener,
		OnItemLongClickListener {

	private static final int ACTION_CHOOSE_CARD = 2;

	private static final String PREF_KEY_LAST_OPEN_SCREEN = "_lastopenscreen";

	private DragController<ItemCard> mDragController;

	private DragLayer mDragLayer;

	private ItemGridView mWorkspace;
	private GridItemAdapter itemAdapter;

	private Spinner mScreenBtn;
	private SpinnerSimpleAdapter<String> mScreenAdapter;

	PageButton mPreviousView, mNextView;
	private int mCurrentScreen = -1;

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

		mScreenBtn.setOnItemSelectedListener(null);
		mScreenAdapter.setNotifyOnChange(false);
		mScreenAdapter.clear();
		for (int i = 1; i <= Hero.MAXIMUM_SET_NUMBER; i++) {
			mScreenAdapter.add("Set " + i);
		}
		mScreenAdapter.add("Rucksack");

		mScreenAdapter.setNotifyOnChange(true);
		mScreenAdapter.notifyDataSetChanged();

		itemAdapter = new GridItemAdapter(getActivity());
		mWorkspace.setAdapter(itemAdapter);

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
		int screen = pref.getInt(PREF_KEY_LAST_OPEN_SCREEN, 0);
		showScreen(screen);
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

						hero.addItem(getBaseActivity(), item, null, getActiveSet(), new Hero.ItemAddedCallback() {

							@Override
							public void onItemAdded(Item item) {
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

								GridCardView cardView = (GridCardView) mWorkspace.getChildAt(cell);
								if (cardView != null) {
									cardView.setItem(item);
								} else {
									itemAdapter.notifyDataSetChanged();
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

								GridCardView cardView = (GridCardView) mWorkspace.getChildAt(cell);
								if (cardView != null) {
									cardView.setItem(item);
								} else {
									itemAdapter.notifyDataSetChanged();
								}

							}
						});

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
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = configureContainerView(inflater.inflate(R.layout.sheet_items_table, container, false));

		inventoryButton = (ImageButton) root.findViewById(R.id.fight_inventory);
		setButton = (ImageButton) root.findViewById(R.id.fight_set);

		mScreenBtn = (Spinner) root.findViewById(R.id.screen_set_button);
		mWorkspace = (ItemGridView) root.findViewById(R.id.workspace);
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

		getView().findViewById(R.id.fight_add_item).setOnClickListener(this);
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

		mPreviousView.setMaxLevel(Hero.MAXIMUM_SET_NUMBER);
		mNextView.setMaxLevel(Hero.MAXIMUM_SET_NUMBER);

		mPreviousView.setHapticFeedbackEnabled(false);
		mPreviousView.setOnClickListener(this);
		mNextView.setHapticFeedbackEnabled(false);
		mNextView.setOnClickListener(this);

		mWorkspace.setOnItemLongClickListener(this);
		mWorkspace.setOnItemClickListener(this);
		// mWorkspace.setDragController(mDragController);
		// mWorkspace.setOnScreenChangeListener(this);

		mDragLayer.setDragController(mDragController);
		mDragLayer.setGridView(mWorkspace);
		mDragLayer.setDeleteZone(mDeleteZone);

		mDeleteZone.setDragController(mDragController);

		mDragController.setDragScoller(this);
		mDragController.addDragListener(mDragLayer);
		mDragController.addDragListener(mDeleteZone);
		mDragController.addDragListener(this);
		mDragController.setMoveTarget(mWorkspace);

	}

	public static boolean handleDrop(Context context, ItemCard dragInfo, GridCardView source, GridCardView target,
			GridView mGrid, int mCellNumber) {

		final Hero hero = DSATabApplication.getInstance().getHero();
		final Integer mCurrentScreen = (Integer) mGrid.getTag();
		final int oldScreen = dragInfo.getItemInfo().getScreen();

		Debug.verbose("onDragDrop " + oldScreen + " to " + mCurrentScreen + " to index " + mCellNumber);

		if (dragInfo instanceof EquippedItem) {
			EquippedItem equippedItem = (EquippedItem) dragInfo;

			Debug.verbose("Moving equippeditem from set " + oldScreen + " to " + mCurrentScreen);

			if (oldScreen == mCurrentScreen) {
				equippedItem.getItemInfo().setCellNumber(mCellNumber);
				hero.fireItemChangedEvent(equippedItem);
			}
			// drag from set to set
			else if (ItemsFragment.isSetIndex(oldScreen) && ItemsFragment.isSetIndex(mCurrentScreen)) {

				hero.removeEquippedItem(equippedItem);

				equippedItem.getItemInfo().setCellNumber(mCellNumber);
				equippedItem.getItemInfo().setScreen(mCurrentScreen);

				hero.addEquippedItem(context, equippedItem, mCurrentScreen);
			}
			// drag from set to inventory
			else if (ItemsFragment.isSetIndex(oldScreen) && !ItemsFragment.isSetIndex(mCurrentScreen)) {
				hero.removeEquippedItem(equippedItem);
			} else {
				Debug.error("Should never happen oldscreen=" + oldScreen + " newscreen=" + mCurrentScreen);
			}

		} else if (dragInfo instanceof Item) {
			Item item = (Item) dragInfo;
			if (oldScreen == mCurrentScreen) {
				item.getItemInfo().setCellNumber(mCellNumber);
				hero.fireItemChangedEvent(item);
			}
			// drag a item from inventory to set (equip it)
			else if (!ItemsFragment.isSetIndex(oldScreen) && ItemsFragment.isSetIndex(mCurrentScreen)) {
				Debug.verbose("Equipping  item on set " + mCurrentScreen);
				EquippedItem equippedItem = hero.addEquippedItem(context, item, null, mCurrentScreen);

				// equippedItem.getItemInfo().setCellX(x);
				equippedItem.getItemInfo().setCellNumber(mCellNumber);

				hero.fireItemChangedEvent(equippedItem);
			}
		}

		return true;
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
		if (view instanceof GridCardView) {
			GridCardView cardView = (GridCardView) view;

			if (cardView.getItem() != null) {
				ItemCard item = (ItemCard) view.getTag();
				selectItem(item, cardView);
			} else {

				// empty cell clicked add new item
				mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
				selectItem(null, cardView);

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android
	 * .widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (view instanceof GridCardView) {
			GridCardView cardView = (GridCardView) view;

			ItemCard card = (ItemCard) cardView.getItem();
			if (card != null) {
				// We are starting a drag. Let the DragController handle it.
				mDragController.startDrag(view, cardView, card, DragController.DRAG_ACTION_MOVE);
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	private void updateIndicators(int screen) {
		mPreviousView.setLevel(screen);
		mNextView.setLevel(Hero.MAXIMUM_SET_NUMBER - screen);

		if (isSetIndex(screen)) {
			LevelListDrawable drawable = (LevelListDrawable) setButton.getDrawable();
			drawable.setLevel(screen);

			inventoryButton.setSelected(false);
			setButton.setSelected(true);

		} else {
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
	 * @see com.dsatab.view.drag.DragController.DragListener#onDragEnd()
	 */
	@Override
	public void onDragEnd() {
		mScreenBtn.setVisibility(View.VISIBLE);
		mDragController.setScrollView(null);
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

		if (screen >= 0 && screen <= Hero.MAXIMUM_SET_NUMBER) {
			int oldScreen = mCurrentScreen;

			Debug.verbose("Show screen " + screen);

			mCurrentScreen = screen;

			fillBodyItems(getHero());

			onScreenChange(oldScreen, mCurrentScreen);
		}
	}

	private void selectItem(ItemCard itemCard, GridCardView cardView) {
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
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CELL, itemCard.getItemInfo().getCellNumber());
		}

		if (cardView != null) {
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CELL, cardView.getCellNumber());
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
		case R.id.fight_set:
			if (isSetIndex(mCurrentScreen))
				showScreen((mCurrentScreen + 1) % Hero.MAXIMUM_SET_NUMBER);
			else {
				LevelListDrawable drawable = (LevelListDrawable) setButton.getDrawable();
				showScreen(drawable.getLevel());
			}
			return;
		case R.id.fight_inventory:
			showScreen(Hero.MAXIMUM_SET_NUMBER);
			return;

		case R.id.fight_add_item:
			selectItem(null, null);
			break;
		}

	}

	public boolean onLongClick(View v) {

		if (v instanceof GridCardView) {
			GridCardView cardView = (GridCardView) v;

			if (cardView.getItem() == null) {
				mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
				selectItem(null, cardView);
				return true;
			} else {
				// User long pressed on an item
				mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

				// mWorkspace.startDrag(cardView);
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
		if (item.getItemInfo().getScreen() == mCurrentScreen) {
			itemAdapter.add(item);
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
