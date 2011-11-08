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
package com.dsatab.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.dsatab.data.items.ItemCard;
import com.dsatab.view.drag.ItemLocationInfo;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.drag.DragView;
import com.gandulf.guilib.drag.DropTarget;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class ItemsTable extends TableLayout implements View.OnClickListener, View.OnLongClickListener,
		DragSource<ItemCard>, DropTarget<ItemCard> {

	/**
	 * CellInfo for the cell that is currently being dragged
	 */
	private CardView mDragInfo;
	private int dragStartScreen;

	private OnLongClickListener onLongClickListener;

	private OnClickListener onClickListener;

	private DragController<ItemCard> mDragController;

	private int rows = 4, cols = 4;

	private int screen;

	/**
	 * @param context
	 */
	public ItemsTable(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ItemsTable(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 
	 */
	private void init() {
		populate();
	}

	public OnLongClickListener getOnLongClickListener() {
		return onLongClickListener;
	}

	public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
		this.onLongClickListener = onLongClickListener;
	}

	public OnClickListener getOnClickListener() {
		return onClickListener;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	public DragController<ItemCard> getDragController() {
		return mDragController;
	}

	public void setDragController(DragController<ItemCard> mDragController) {
		this.mDragController = mDragController;
	}

	public int getScreen() {
		return screen;
	}

	public void setScreen(int screen) {
		this.screen = screen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (onClickListener != null)
			onClickListener.onClick(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		if (onLongClickListener != null)
			return onLongClickListener.onLongClick(v);
		else
			return false;
	}

	protected void populate() {
		for (int y = 0; y < rows; y++) {
			TableRow tableRow = (TableRow) getChildAt(y);
			if (tableRow == null) {
				tableRow = new TableRow(getContext());
				tableRow.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT, 1.0f));
				addView(tableRow);
			}

			for (int x = 0; x < cols; x++) {
				CardView child = (CardView) tableRow.getChildAt(x);
				if (child == null) {
					child = generateView(null);
					child.setLocation(x, y);
					child.setItemsTable(this);
					tableRow.addView(child);
				}
			}
		}
	}

	/**
	 * @param oldItem
	 * @param newItem
	 */
	public void replaceItem(ItemCard oldItem, ItemCard newItem) {

		CardView oldView = (CardView) findViewWithTag(oldItem);
		if (oldView != null) {
			oldView.setItem(newItem);

			newItem.getItemInfo().setCellX(oldItem.getItemInfo().getCellX());
			newItem.getItemInfo().setCellY(oldItem.getItemInfo().getCellY());
			newItem.getItemInfo().setScreen(oldItem.getItemInfo().getScreen());
		}
	}

	/**
	 * 
	 */
	public void clear() {
		for (int i = 0; i < getChildCount(); i++) {
			TableRow row = (TableRow) getChildAt(i);
			for (int ii = 0; ii < row.getChildCount(); ii++) {
				((CardView) row.getChildAt(ii)).setItem(null);
			}
		}

	}

	/**
	 * @param item
	 */
	public boolean addItemInScreen(ItemCard item) {

		ItemLocationInfo info = item.getItemInfo();

		Debug.verbose("Adding item to " + info);

		if (info.getCellX() != ItemLocationInfo.INVALID_POSITION
				&& info.getCellY() != ItemLocationInfo.INVALID_POSITION) {
			TableRow row = (TableRow) getChildAt(info.getCellY());

			CardView child = (CardView) row.getChildAt(info.getCellX());
			if (child.getItem() == null) {
				child.setItem(item);
				return true;
			} else {
				return false;
			}
		} else {
			for (int i = 0; i < getChildCount(); i++) {
				TableRow row = (TableRow) getChildAt(i);
				for (int ii = 0; ii < row.getChildCount(); ii++) {
					CardView cardView = (CardView) row.getChildAt(ii);
					if (cardView.getItem() == null) {
						cardView.setItem(item);
						item.getItemInfo().setCellX(cardView.getCellX());
						item.getItemInfo().setCellY(cardView.getCellY());
						item.getItemInfo().setScreen(cardView.getScreen());
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * @param item
	 */
	public void removeItem(ItemCard item) {
		CardView cardView = (CardView) findViewWithTag(item);
		if (cardView != null)
			cardView.setItem(null);
	}

	CardView generateView(ItemCard item) {
		CardView view = new CardView(getContext(), item);
		view.setLayoutParams(new TableRow.LayoutParams(android.widget.TableRow.LayoutParams.MATCH_PARENT,
				android.widget.TableRow.LayoutParams.MATCH_PARENT));
		if (item != null && item.getFile() != null) {
			view.setImageBitmap(DataManager.getBitmap(item.getFile().getAbsolutePath()));
		}
		view.setOnLongClickListener(this);
		view.setOnClickListener(this);
		view.setTag(item);

		return view;
	}

	public void startDrag(CardView cardView) {

		// Make sure the drag was started by a long press as opposed to a long
		// click.
		if (cardView == null)
			return;

		mDragInfo = cardView;
		dragStartScreen = cardView.getScreen();

		mDragController.startDrag(cardView, this, (ItemCard) cardView.getTag(), DragController.DRAG_ACTION_MOVE);
		invalidate();
	}

	public void onDragEnter(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {

	}

	public void onDragExit(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {

	}

	public void onDragOver(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
	}

	public boolean onDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {

		if (mDragInfo != null) {

			CardView targetView = estimateDropCell(x - xOffset, y - yOffset, 1, 1, mDragInfo);

			boolean drop = mDragController.onDrop(mDragInfo, dragInfo, targetView.getCellX(), targetView.getCellY(),
					targetView.getScreen());

			if (drop) {

				Debug.verbose("d&d item from " + mDragInfo.getCellX() + "/" + mDragInfo.getCellY() + " to "
						+ targetView.getCellX() + "/" + targetView.getCellY());

				ItemCard switchItem = null;
				if (targetView.getItem() != null) {
					switchItem = targetView.getItem();
				}

				if (targetView.getScreen() == dragStartScreen) {
					mDragInfo.setItem(switchItem);
				}
				if (switchItem != null) {
					switchItem.getItemInfo().setCellX(mDragInfo.getCellX());
					switchItem.getItemInfo().setCellY(mDragInfo.getCellY());
					switchItem.getItemInfo().setScreen(dragStartScreen);
				}

				dragInfo.getItemInfo().setCellX(targetView.getCellX());
				dragInfo.getItemInfo().setCellY(targetView.getCellY());
				dragInfo.getItemInfo().setScreen(targetView.getScreen());
				targetView.setItem(dragInfo);

			} else {
				Debug.verbose("Cancel drop due to listener result");
			}
		}
		return true;

	}

	public void onDropCompleted(DropTarget<ItemCard> target, boolean success) {

		if (success) {
			if (target != this && mDragInfo != null) {
				// final Object tag = mDragInfo.cell.getTag();
			}
		}
		// } else {
		// if (mDragInfo != null) {
		// final CellLayout cellLayout = (CellLayout)
		// getChildAt(mDragInfo.screen);
		// cellLayout.onDropAborted(mDragInfo.cell);
		// }
		// }

		mDragInfo = null;
	}

	/**
	 * @param x
	 * @param y
	 * @param i
	 * @param j
	 * @param view
	 * @param cellLayout
	 * @param mTargetCell2
	 * @return
	 */
	private CardView estimateDropCell(int x, int y, int spanX, int spanY, CardView view) {

		CardView result = null;

		double bestDistance = Double.MAX_VALUE;

		for (int i = 0; i < rows; i++) {
			TableRow row = (TableRow) getChildAt(i);

			for (int ii = 0; ii < cols; ii++) {
				CardView child = (CardView) row.getChildAt(ii);

				double distance = Math.sqrt(Math.pow(child.getLeft() - x, 2)
						+ Math.pow(child.getTop() + row.getTop() - y, 2));

				if (distance <= bestDistance) {
					bestDistance = distance;
					result = child;
				}

			}

		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DropTarget#acceptDrop(com.gandulf.guilib.drag
	 * .DragSource, int, int, int, int, com.gandulf.guilib.drag.DragView,
	 * java.lang.Object)
	 */
	@Override
	public boolean acceptDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		CardView targetView = estimateDropCell(x - xOffset, y - yOffset, 1, 1, mDragInfo);

		return targetView.getItem() == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DropTarget#estimateDropLocation(com.gandulf.guilib
	 * .drag.DragSource, int, int, int, int, com.gandulf.guilib.drag.DragView,
	 * java.lang.Object, android.graphics.Rect)
	 */
	@Override
	public Rect estimateDropLocation(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset,
			DragView dragView, ItemCard dragInfo, Rect recycle) {
		return null;
	}

}
