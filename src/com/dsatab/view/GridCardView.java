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
import android.widget.GridView;

import com.dsatab.R;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.fragment.ItemsFragment;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.drag.DragView;
import com.gandulf.guilib.drag.DropTarget;

/**
 * @author Ganymede
 * 
 */
public class GridCardView extends CardView implements DropTarget<ItemCard>, DragSource<ItemCard> {

	private int mCellNumber = -1;

	private GridView mGrid;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DragSource#setDragController(com.gandulf.guilib
	 * .drag.DragController)
	 */

	public GridCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public GridCardView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public GridCardView(Context context, ItemCard item) {
		super(context, item);

	}

	public GridCardView(Context context) {
		super(context);

	}

	public void setDragController(DragController<ItemCard> dragger) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DragSource#onDropCompleted(com.gandulf.guilib
	 * .drag.DropTarget, boolean)
	 */

	public void onDropCompleted(DropTarget<ItemCard> target, boolean success) {

	}

	/**
	 * Handle an object being dropped on the DropTarget. This is the where the
	 * drawable of the dragged view gets copied into the ImageCell.
	 * 
	 * @param source
	 *            DragSource where the drag started
	 * @param x
	 *            X coordinate of the drop location
	 * @param y
	 *            Y coordinate of the drop location
	 * @param xOffset
	 *            Horizontal offset with the object being dragged where the
	 *            original touch happened
	 * @param yOffset
	 *            Vertical offset with the object being dragged where the
	 *            original touch happened
	 * @param dragView
	 *            The DragView that's being dragged around on screen.
	 * @param dragInfo
	 *            Data associated with the object being dragged
	 * 
	 */
	public boolean onDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		// Mark the cell so it is no longer empty.
		final Integer currentScreen = (Integer) mGrid.getTag();

		int destCell = -1;
		if (mCellNumber > dragInfo.getItemInfo().getCellNumber() && dragInfo.getItemInfo().getScreen() == currentScreen) {
			destCell = mCellNumber + 1;
		} else
			destCell = mCellNumber - 1;

		return ItemsFragment.handleDrop(getContext(), dragInfo, (GridCardView) source, this, mGrid, destCell);

		// int bg = mEmpty ? R.color.cell_empty : R.color.cell_filled;
		// setBackgroundResource(bg);

	}

	/**
	 * React to a dragged object entering the area of this DropSpot. Provide the
	 * user with some visual feedback.
	 */
	public void onDragEnter(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		if (acceptDrop(source, x, y, xOffset, yOffset, dragView, dragInfo)) {
			dragView.setOverlayColor(getResources().getColor(R.color.accept_color_filter));
		} else
			dragView.setOverlayColor(getResources().getColor(R.color.reject_color_filter));

	}

	/**
	 * React to something being dragged over the drop target.
	 */
	public void onDragOver(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
	}

	/**
	 * React to a drag
	 */
	public void onDragExit(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		dragView.setOverlayColor(0);
	}

	/**
	 * Check if a drop action can occur at, or near, the requested location.
	 * This may be called repeatedly during a drag, so any calls should return
	 * quickly.
	 * 
	 * @param source
	 *            DragSource where the drag started
	 * @param x
	 *            X coordinate of the drop location
	 * @param y
	 *            Y coordinate of the drop location
	 * @param xOffset
	 *            Horizontal offset with the object being dragged where the
	 *            original touch happened
	 * @param yOffset
	 *            Vertical offset with the object being dragged where the
	 *            original touch happened
	 * @param dragView
	 *            The DragView that's being dragged around on screen.
	 * @param dragInfo
	 *            Data associated with the object being dragged
	 * @return True if the drop will be accepted, false otherwise.
	 */
	public boolean acceptDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		// An ImageCell accepts a drop if it is empty and if it is part of a
		// grid.
		// A free-standing ImageCell does not accept drops.

		if (dragInfo instanceof Item) {
			Item item = (Item) dragInfo;
			if (ItemsFragment.isSetIndex(getScreen())) {
				return item.isEquipable();
			} else {
				return true;
			}
		}
		return true;
	}

	/**
	 * Estimate the surface area where this object would land if dropped at the
	 * given location.
	 * 
	 * @param source
	 *            DragSource where the drag started
	 * @param x
	 *            X coordinate of the drop location
	 * @param y
	 *            Y coordinate of the drop location
	 * @param xOffset
	 *            Horizontal offset with the object being dragged where the
	 *            original touch happened
	 * @param yOffset
	 *            Vertical offset with the object being dragged where the
	 *            original touch happened
	 * @param dragView
	 *            The DragView that's being dragged around on screen.
	 * @param dragInfo
	 *            Data associated with the object being dragged
	 * @param recycle
	 *            {@link Rect} object to be possibly recycled.
	 * @return Estimated area that would be occupied if object was dropped at
	 *         the given location. Should return null if no estimate is found,
	 *         or if this target doesn't provide estimations.
	 */
	public Rect estimateDropLocation(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset,
			DragView dragView, ItemCard dragInfo, Rect recycle) {
		return null;
	}

	/**
	 */
	// Other Methods

	public void setCellNumber(int cellNumber) {
		this.mCellNumber = cellNumber;
	}

	public int getCellNumber() {
		return mCellNumber;
	}

	public int getScreen() {
		return (Integer) mGrid.getTag();
	}

	public GridView getGrid() {
		return mGrid;
	}

	public void setGrid(GridView grid) {
		this.mGrid = grid;
	}

}
