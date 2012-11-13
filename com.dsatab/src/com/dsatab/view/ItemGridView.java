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
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.drag.DragView;
import com.gandulf.guilib.drag.DropTarget;

/**
 * @author Ganymede
 * 
 */
public class ItemGridView extends GridView implements DropTarget<ItemCard> {

	public ItemGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public ItemGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public ItemGridView(Context context) {
		super(context);

	}

	public int getScreen() {
		return (Integer) getTag();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DropTarget#onDragEnter(com.gandulf.guilib.drag
	 * .DragSource, int, int, int, int, com.gandulf.guilib.drag.DragView,
	 * java.lang.Object)
	 */
	@Override
	public void onDragEnter(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		if (acceptDrop(source, x, y, xOffset, yOffset, dragView, dragInfo)) {
			dragView.setOverlayColor(getResources().getColor(R.color.accept_color_filter));
		} else
			dragView.setOverlayColor(getResources().getColor(R.color.reject_color_filter));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DropTarget#onDragExit(com.gandulf.guilib.drag
	 * .DragSource, int, int, int, int, com.gandulf.guilib.drag.DragView,
	 * java.lang.Object)
	 */
	@Override
	public void onDragExit(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		dragView.setOverlayColor(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DropTarget#onDragOver(com.gandulf.guilib.drag
	 * .DragSource, int, int, int, int, com.gandulf.guilib.drag.DragView,
	 * java.lang.Object)
	 */
	@Override
	public void onDragOver(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DropTarget#onDrop(com.gandulf.guilib.drag.DragSource
	 * , int, int, int, int, com.gandulf.guilib.drag.DragView, java.lang.Object)
	 */
	@Override
	public boolean onDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		return ItemsFragment.handleDrop(getContext(), dragInfo, (GridCardView) source, null, this, -1);
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
