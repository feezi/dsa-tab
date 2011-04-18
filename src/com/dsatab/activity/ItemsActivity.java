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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.drag.CellLayout;
import com.dsatab.view.drag.DeleteZone;
import com.dsatab.view.drag.ItemInfo;
import com.dsatab.view.drag.Workspace;
import com.dsatab.view.drag.Workspace.OnScreenChangeListener;
import com.dsatab.view.listener.InventoryChangedListener;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragLayer;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.util.Debug;

public class ItemsActivity extends BaseMainActivity implements View.OnLongClickListener, View.OnClickListener,
		DragController.DragListener<ItemCard>, OnScreenChangeListener, InventoryChangedListener {

	private static final int ACTION_CHOOSE_CARD = 2;

	private DragController<ItemCard> mDragController;
	private Workspace mWorkspace;
	private DeleteZone mDeleteZone;

	private ImageView mPreviousView;
	private ImageView mNextView;
	private TextView mScreenTextView;

	private ItemCard selectedItem = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		super.onHeroLoaded(hero);
		fillBodyItems(hero);

		hero.addInventoryChangedListener(this);
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
		if (hero != null)
			hero.removeInventoryChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.ValueChangedListener#onValueChanged(com.dsatab
	 * .data.Value)
	 */
	@Override
	public void onValueChanged(Value value) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#setupDiceSilder()
	 */
	@Override
	protected void setupDiceSilder() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ACTION_CHOOSE_CARD && resultCode == RESULT_OK) {

			Item item = null;

			final int cellX = data.getIntExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_X, ItemInfo.INVALID_POSITION);
			final int cellY = data.getIntExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_Y, ItemInfo.INVALID_POSITION);

			UUID id = (UUID) data.getSerializableExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_ID);
			String cardName = data.getStringExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_NAME);

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

				if (cellX != ItemInfo.INVALID_POSITION && cellY != ItemInfo.INVALID_POSITION
						&& item.getItemInfo().getCellX() == cellX && item.getItemInfo().getCellY() == cellY) {
					// the icon is already in the screen no need to add it again
				} else {
					if (selectedItem != null && selectedItem.getItem().equals(item.getName())) {
						// the icon is already in the screen no need to add it
						// again
					} else {

						hero.addItem(this, item, null, getActiveSet(), new Hero.ItemAddedCallback() {

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
									item.getItemInfo().setScreen(mWorkspace.getCurrentScreen());
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
									item.getItemInfo().setScreen(mWorkspace.getCurrentScreen());
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
		return index >= 0 && index < 3;
	}

	private int getActiveSet() {
		if (!isSetIndex(mWorkspace.getCurrentScreen()))
			return -1;
		else
			return mWorkspace.getCurrentScreen();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_items);
		super.onCreate(savedInstanceState);
		tabFlingEnabled = false;
		mDragController = new DragController<ItemCard>(this);
		mScreenTextView = (TextView) findViewById(R.id.screen_set_text);

		setupViews();
	}

	/**
	 * Finds all the views we need and configure them properly.
	 */
	private void setupViews() {

		// 192*288
		// 120*180

		DragLayer dragLayer = (DragLayer) findViewById(R.id.drag_layer);
		dragLayer.setDragController(mDragController);

		mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
		mWorkspace.setHapticFeedbackEnabled(false);

		mDeleteZone = (DeleteZone) dragLayer.findViewById(R.id.delete_zone);
		mDeleteZone.setOrientation(DeleteZone.ORIENTATION_HORIZONTAL);

		mPreviousView = (ImageView) dragLayer.findViewById(R.id.previous_screen);
		mNextView = (ImageView) dragLayer.findViewById(R.id.next_screen);

		Drawable previous = mPreviousView.getDrawable();
		Drawable next = mNextView.getDrawable();
		mWorkspace.setIndicators(previous, next);

		mPreviousView.setHapticFeedbackEnabled(false);
		mPreviousView.setOnClickListener(this);
		mNextView.setHapticFeedbackEnabled(false);
		mNextView.setOnClickListener(this);

		mWorkspace.setOnLongClickListener(this);
		mWorkspace.setOnClickListener(this);
		mWorkspace.setDragController(mDragController);
		mWorkspace.setOnScreenChangeListener(this);

		mDeleteZone.setDragController(mDragController);

		mDragController.setDragScoller(mWorkspace);
		mDragController.addDragListener(mDeleteZone);
		mDragController.addDragListener(this);
		mDragController.setScrollView(dragLayer);
		mDragController.setMoveTarget(mWorkspace);

		// The order here is bottom to top.
		mDragController.addDropTarget(mWorkspace);
		mDragController.addDropTarget(mDeleteZone);

		updateTextView(mWorkspace.getCurrentScreen());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.drag.Workspace.OnScreenChangeListener#onScreenChange(int,
	 * int)
	 */
	@Override
	public void onScreenChange(int oldScreen, int newScreen) {
		if (isSetIndex(newScreen))
			getHero().setActiveSet(newScreen);
		updateTextView(newScreen);
	}

	private void updateTextView(int newScreen) {
		if (isSetIndex(newScreen))
			mScreenTextView.setText("Set " + (newScreen + 1));
		else
			mScreenTextView.setText("Ausrüstung");
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
		mScreenTextView.setVisibility(View.INVISIBLE);
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
					EquippedItem equippedItem = getHero().addEquippedItem(item, null, newScreen);

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
		mScreenTextView.setVisibility(View.VISIBLE);
	}

	private void fillBodyItems(Hero hero) {

		boolean success = true;

		List<Item> skipItems = new LinkedList<Item>();

		int screen = Hero.MAXIMUM_SET_NUMBER;
		if (hero != null) {
			for (int i = 0; i < Hero.MAXIMUM_SET_NUMBER; i++) {
				for (EquippedItem item : hero.getEquippedItems(i)) {
					if (item.getItem() == null) {
						Debug.verbose("Skipping " + item.getName() + "because equippedItem.getItem was null");
						continue;
					}
					success = mWorkspace.addItemInScreen(i, item);

					// is unable to add item at specified position try again
					// without position info (will be added in first empty slot)
					if (!success
							&& (item.getItemInfo().getCellX() != ItemInfo.INVALID_POSITION || item.getItemInfo()
									.getCellY() != ItemInfo.INVALID_POSITION)) {

						item.getItemInfo().setCellX(ItemInfo.INVALID_POSITION);
						item.getItemInfo().setCellY(ItemInfo.INVALID_POSITION);
						success = mWorkspace.addItemInScreen(i, item);
					}
					if (!success) {
						Debug.verbose("Skipping " + item.getName() + "because inventory page was FULL");
					}

					// unable to add item, stop here the inventory is probably
					// full
					if (!success) {
						return;
					} else {
						skipItems.add(item.getItem());
					}

				}
			}

			for (Item item : hero.getItems()) {

				// skip items already added during equipped sets
				if (skipItems.contains(item))
					continue;

				if (item.getItemInfo().getScreen() != ItemInfo.INVALID_POSITION)
					success = mWorkspace.addItemInScreen(item.getItemInfo().getScreen(), item);
				else
					success = false;

				if (!success)
					success = mWorkspace.addItemInScreen(screen, item);

				// is unable to add item at specified position try again
				// without position info (will be added in first empty slot)
				if (!success
						&& (item.getItemInfo().getCellX() != ItemInfo.INVALID_POSITION || item.getItemInfo().getCellY() != ItemInfo.INVALID_POSITION)) {

					item.getItemInfo().setCellX(ItemInfo.INVALID_POSITION);
					item.getItemInfo().setCellY(ItemInfo.INVALID_POSITION);
					success = mWorkspace.addItemInScreen(screen, item);
				}

				if (!success && screen < mWorkspace.getChildCount() - 1) {
					screen++;
					success = mWorkspace.addItemInScreen(screen, item);
				}

				// unable to add item, stop here the inventory is probably
				// full
				if (!success) {
					Debug.warning("Unable to add item " + item.getTitle() + " try on screen " + screen);
					return;
				}
			}
		}

		showScreen(hero.getActiveSet());

	}

	private void showScreen(int i) {
		mWorkspace.setCurrentScreen(i);
		updateTextView(i);
	}

	private void selectItem(ItemCard itemCard, CellLayout.CellInfo cellInfo) {
		if (itemCard != null)
			selectedItem = itemCard;
		else
			selectedItem = null;

		Intent intent = new Intent(this, ItemChooserActivity.class);

		if (selectedItem != null) {

			Item item = selectedItem.getItem();
			ItemType cardType = item.getType();

			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_ID, item.getId());
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_NAME, item.getName());
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_CATEGORY, item.getCategory());

			if (cardType != null) {
				intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_TYPE, cardType.name());
			}

			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_X, itemCard.getItemInfo().getCellX());
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_Y, itemCard.getItemInfo().getCellY());
		}

		if (cellInfo != null) {
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_X, cellInfo.cellX);
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_Y, cellInfo.cellY);
		}

		startActivityForResult(intent, ACTION_CHOOSE_CARD);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v == mNextView) {
			mWorkspace.scrollRight();
		} else if (v == mPreviousView) {
			mWorkspace.scrollLeft();
		} else if (v.getTag() instanceof ItemCard) {
			ItemCard item = (ItemCard) v.getTag();
			selectItem(item, null);
		} else {
			// click on empt cell open browser for items
			if (!(v instanceof CellLayout)) {
				v = (View) v.getParent();
			}

			CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

			// This happens when long clicking an item with the dpad/trackball
			if (cellInfo == null) {
				return;
			}

			// empty cell clicked add new item
			if (cellInfo.cell == null) {
				mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
				selectItem(null, cellInfo);
			}
		}
	}

	public boolean onLongClick(View v) {

		if (!(v instanceof CellLayout)) {
			v = (View) v.getParent();
		}

		CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

		// This happens when long clicking an item with the dpad/trackball
		if (cellInfo == null) {
			return true;
		}

		// empty cell clicked add new item
		if (cellInfo.cell == null) {
			mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
			selectItem(null, cellInfo);
			return true;
		}

		if (mWorkspace.allowLongPress()) {

			// User long pressed on an item
			mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
			mWorkspace.startDrag(cellInfo);

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
