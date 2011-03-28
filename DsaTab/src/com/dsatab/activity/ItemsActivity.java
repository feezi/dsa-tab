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
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.drag.CellLayout;
import com.dsatab.view.drag.DeleteZone;
import com.dsatab.view.drag.ItemInfo;
import com.dsatab.view.drag.Workspace;
import com.dsatab.view.drag.Workspace.OnScreenChangeListener;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragLayer;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.util.Debug;

public class ItemsActivity extends BaseMenuActivity implements View.OnLongClickListener, View.OnClickListener,
		DragController.DragListener<ItemCard>, OnScreenChangeListener {

	private static final int ACTION_CHOOSE_CARD = 2;

	private DragController<ItemCard> mDragController;
	private Workspace mWorkspace;
	private DeleteZone mDeleteZone;

	private ImageView mPreviousView;
	private ImageView mNextView;
	private TextView mScreenTextView;

	private Item selectedItem = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		fillBodyItems(hero);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroUnloaded(Hero hero) {

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

				// add item to hero if he not already has it
				item = hero.getItem(cardName);
				if (item == null) {
					Item card = DataManager.getItemByName(cardName);
					item = (Item) card.duplicate();
					hero.addItem(item);
				}
			}

			if (item != null) {

				if (cellX != ItemInfo.INVALID_POSITION && cellY != ItemInfo.INVALID_POSITION
						&& item.getItemInfo().getCellX() == cellX && item.getItemInfo().getCellY() == cellY) {
					// the icon is already in the screen no need to add it again
				} else {
					if (selectedItem != null && selectedItem.getName().equals(item.getName())) {
						// the icon is already in the screen no need to add it
						// again
					} else {

						int oldActiveSet = hero.getActiveSet();

						if (getActiveSet() >= 0)
							hero.setActiveSet(getActiveSet());

						hero.addItem(this, item, null, new Hero.ItemAddedCallback() {

							@Override
							public void onItemAdded(Item item) {

								if (selectedItem != null) {
									mWorkspace.removeItem(selectedItem);
									getHero().removeItem(selectedItem);
								}

								item.getItemInfo().setCellX(cellX);
								item.getItemInfo().setCellY(cellY);
								item.getItemInfo().setScreen(mWorkspace.getCurrentScreen());
								mWorkspace.addItemInCurrentScreen(item);

							}
						});

						hero.setActiveSet(oldActiveSet);
					}
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private int getActiveSet() {
		if (mWorkspace.getCurrentScreen() > 3)
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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.items_main);

		mDragController = new DragController<ItemCard>(this);
		mScreenTextView = (TextView) findViewById(R.id.screen_set_text);

		setupViews();
	}

	/**
	 * Finds all the views we need and configure them properly.
	 */
	private void setupViews() {

		DragLayer dragLayer = (DragLayer) findViewById(R.id.drag_layer);
		dragLayer.setDragController(mDragController);

		mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
		mWorkspace.setHapticFeedbackEnabled(false);

		mDeleteZone = (DeleteZone) dragLayer.findViewById(R.id.delete_zone);

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
		updateTextView(newScreen);
	}

	private void updateTextView(int newScreen) {
		if (newScreen < 3)
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
	 * @see com.dsatab.view.drag.DragController.DragListener#onDragEnd()
	 */
	@Override
	public void onDragEnd() {
		mScreenTextView.setVisibility(View.VISIBLE);
	}

	private void fillBodyItems(Hero hero) {

		boolean success = true;

		List<Item> skipItems = new LinkedList<Item>();

		int screen = 3;
		if (hero != null) {
			for (int i = 0; i < 3; i++) {
				for (EquippedItem item : hero.getEquippedItems(i)) {
					if (item.getItem() == null) {
						Debug.verbose("Skipping " + item.getName() + "because equippedItem.getItem was null");
						continue;
					}
					success = mWorkspace.addItemInScreen(i, item);
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

			for (List<Item> items : hero.getItems().values()) {
				for (Item item : items) {
					// skip items already added during equipped sets
					if (skipItems.contains(item))
						continue;

					success = mWorkspace.addItemInScreen(screen, item);
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
		}
		showScreen(hero.getActiveSet());

	}

	private void showScreen(int i) {
		mWorkspace.setCurrentScreen(i);
		updateTextView(i);
	}

	private void selectItem(ItemCard item, CellLayout.CellInfo cellInfo) {

		selectedItem = item.getItem();

		Intent intent = new Intent(this, ItemChooserActivity.class);

		if (item != null) {
			ItemType cardType = selectedItem.getType();

			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_ID, selectedItem.getId());
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_NAME, selectedItem.getName());
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_CATEGORY, selectedItem.getCategory());

			if (cardType != null) {
				intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_TYPE, cardType.name());
			}

			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_X, item.getItemInfo().getCellX());
			intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ITEM_Y, item.getItemInfo().getCellY());
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

}
