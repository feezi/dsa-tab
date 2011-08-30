/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dsatab.view.drag;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.view.CardView;
import com.dsatab.view.drag.CellLayout.CellInfo.VacantCell;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragScroller;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.drag.DragView;
import com.gandulf.guilib.drag.DropTarget;
import com.gandulf.guilib.util.Debug;

/**
 * The workspace is a wide area with a wallpaper and a finite number of screens.
 * Each screen contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends ViewGroup implements DropTarget<ItemCard>, DragSource<ItemCard>, DragScroller {

	private static final int INVALID_SCREEN = -1;

	private int mDefaultScreen;

	private boolean mFirstLayout = true;

	private int mCurrentScreen;
	private int mNextScreen = INVALID_SCREEN;
	private Scroller mScroller;

	/**
	 * CellInfo for the cell that is currently being dragged
	 */
	private CellLayout.CellInfo mDragInfo;

	/**
	 * Target drop area calculated during last acceptDrop call.
	 */
	private int[] mTargetCell = null;

	private OnLongClickListener mLongClickListener;

	private OnClickListener mClickListener;

	private DragController<ItemCard> mDragController;

	/**
	 * Cache of vacant cells, used during drag events and invalidated as needed.
	 */
	private CellLayout.CellInfo mVacantCache = null;

	private int[] mTempCell = new int[2];
	private int[] mTempEstimate = new int[2];

	private boolean mAllowLongPress = true;

	private Drawable mPreviousIndicator;
	private Drawable mNextIndicator;

	private WorkspaceOvershootInterpolator mScrollInterpolator;

	private static final float BASELINE_FLING_VELOCITY = 2500.f;
	private static final float FLING_VELOCITY_INFLUENCE = 0.4f;

	private OnScreenChangeListener onScreenChangeListener;

	public interface OnScreenChangeListener {
		public void onScreenChange(int oldScreen, int newScreen);
	}

	private static class WorkspaceOvershootInterpolator implements Interpolator {
		private static final float DEFAULT_TENSION = 1.3f;
		private float mTension;

		public WorkspaceOvershootInterpolator() {
			mTension = DEFAULT_TENSION;
		}

		public void setDistance(int distance) {
			mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
		}

		public void disableSettle() {
			mTension = 0.f;
		}

		public float getInterpolation(float t) {
			// _o(t) = t * t * ((tension + 1) * t + tension)
			// o(t) = _o(t - 1) + 1
			t -= 1.0f;
			return t * t * ((mTension + 1) * t + mTension) + 1.0f;
		}
	}

	/**
	 * Used to inflate the Workspace from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attribtues set containing the Workspace's customization
	 *            values.
	 */
	public Workspace(Context context, AttributeSet attrs) {
		super(context, attrs);

		mDefaultScreen = 0;
		setHapticFeedbackEnabled(false);
		initWorkspace();
	}

	/**
	 * Initializes various states for this workspace.
	 */
	private void initWorkspace() {
		Context context = getContext();
		mScrollInterpolator = new WorkspaceOvershootInterpolator();
		mScroller = new Scroller(context, mScrollInterpolator);

		mCurrentScreen = mDefaultScreen;
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		if (!(child instanceof CellLayout)) {
			throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
		}
		super.addView(child, index, params);
	}

	public boolean isCellEmpty(int x, int y) {
		return isCellEmpty(x, y, getCurrentScreen());
	}

	public boolean isCellEmpty(int x, int y, int screen) {
		final CellLayout group = (CellLayout) getChildAt(screen);
		return group.isCellEmpty(x, y);
	}

	@Override
	public void addView(View child) {
		if (!(child instanceof CellLayout)) {
			throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
		}
		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (!(child instanceof CellLayout)) {
			throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
		}
		super.addView(child, index);
	}

	@Override
	public void addView(View child, int width, int height) {
		if (!(child instanceof CellLayout)) {
			throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
		}
		super.addView(child, width, height);
	}

	View generateView(ItemCard item) {
		CardView view = new CardView(getContext(), item);

		if (item.getFile() != null) {
			view.setImageBitmap(DataManager.getBitmap(item.getFile().getAbsolutePath()));
		}
		view.setOnLongClickListener(mLongClickListener);
		view.setOnClickListener(mClickListener);
		view.setTag(item);

		return view;
	}

	public OnScreenChangeListener getOnScreenChangeListener() {
		return onScreenChangeListener;
	}

	public void setOnScreenChangeListener(OnScreenChangeListener onScreenChangeListener) {
		this.onScreenChangeListener = onScreenChangeListener;
	}

	public boolean addItemInCurrentScreen(ItemCard item) {
		return addItemInScreen(mCurrentScreen, item);
	}

	public boolean addItemInScreen(ItemCard item) {
		return addItemInScreen(item.getItemInfo().getScreen(), item);
	}

	public boolean addItemInScreen(int screen, ItemCard item) {

		View view = generateView(item);

		ItemLocationInfo info = item.getItemInfo();

		if (screen == ItemLocationInfo.INVALID_POSITION) {
			if (item instanceof Item) {
				screen = 3;
			} else if (item instanceof EquippedItem) {
				EquippedItem equippedItem = (EquippedItem) item;
				screen = equippedItem.getSet();
			}
		}

		if (info.getScreen() == ItemLocationInfo.INVALID_POSITION
				|| info.getCellX() == ItemLocationInfo.INVALID_POSITION
				|| info.getCellY() == ItemLocationInfo.INVALID_POSITION) {

			CellLayout.CellInfo info2 = findAllVacantCells(screen, null);
			if (!info2.vacantCells.isEmpty()) {
				VacantCell vacant = info2.vacantCells.get(0);
				info.setCellX(vacant.cellX);
				info.setCellY(vacant.cellY);
				info.setScreen(screen);
			} else {
				return false;
			}
		}

		return addInScreen(view, screen, info.getCellX(), info.getCellY(), info.getSpanX(), info.getSpanY());
	}

	@Override
	public void addView(View child, LayoutParams params) {
		if (!(child instanceof CellLayout)) {
			throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
		}
		super.addView(child, params);
	}

	boolean isDefaultScreenShowing() {
		return mCurrentScreen == mDefaultScreen;
	}

	/**
	 * Returns the index of the currently displayed screen.
	 * 
	 * @return The index of the currently displayed screen.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	/**
	 * Sets the current screen.
	 * 
	 * @param currentScreen
	 */
	public void setCurrentScreen(int currentScreen) {
		if (!mScroller.isFinished())
			mScroller.abortAnimation();
		clearVacantCache();
		mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
		mPreviousIndicator.setLevel(mCurrentScreen);
		mNextIndicator.setLevel(mCurrentScreen);
		scrollTo(mCurrentScreen * getWidth(), 0);
		invalidate();
	}

	/**
	 * Adds the specified child in the specified screen. The position and
	 * dimension of the child are defined by x, y, spanX and spanY.
	 * 
	 * @param child
	 *            The child to add in one of the workspace's screens.
	 * @param screen
	 *            The screen in which to add the child.
	 * @param x
	 *            The X position of the child in the screen's grid.
	 * @param y
	 *            The Y position of the child in the screen's grid.
	 * @param spanX
	 *            The number of cells spanned horizontally by the child.
	 * @param spanY
	 *            The number of cells spanned vertically by the child.
	 */
	boolean addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
		return addInScreen(child, screen, x, y, spanX, spanY, false);
	}

	/**
	 * Adds the specified child in the specified screen. The position and
	 * dimension of the child are defined by x, y, spanX and spanY.
	 * 
	 * @param child
	 *            The child to add in one of the workspace's screens.
	 * @param screen
	 *            The screen in which to add the child.
	 * @param x
	 *            The X position of the child in the screen's grid.
	 * @param y
	 *            The Y position of the child in the screen's grid.
	 * @param spanX
	 *            The number of cells spanned horizontally by the child.
	 * @param spanY
	 *            The number of cells spanned vertically by the child.
	 * @param insert
	 *            When true, the child is inserted at the beginning of the
	 *            children list.
	 */
	@SuppressWarnings("unchecked")
	boolean addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
		if (screen < 0 || screen >= getChildCount()) {
			Debug.error("The screen must be >= 0 and < " + getChildCount() + " (was " + screen + "); skipping child");
			return false;
		}

		clearVacantCache();

		final CellLayout group = (CellLayout) getChildAt(screen);

		if (!group.isCellEmpty(x, y)) {
			Debug.error("Cell " + x + "/" + y + " already occupied");
			return false;
		}

		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
		if (lp == null) {
			lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
		} else {
			lp.cellX = x;
			lp.cellY = y;
			lp.cellHSpan = spanX;
			lp.cellVSpan = spanY;
		}
		group.addView(child, insert ? 0 : -1, lp);
		if (child instanceof DropTarget) {
			mDragController.addDropTarget((DropTarget<ItemCard>) child);
		}

		return true;
	}

	CellLayout.CellInfo findAllVacantCells(int screen, boolean[] occupied) {
		CellLayout group = (CellLayout) getChildAt(screen);
		if (group != null) {
			return group.findAllVacantCells(occupied, null);
		}
		return null;
	}

	CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
		return findAllVacantCells(mCurrentScreen, occupied);
	}

	private void clearVacantCache() {
		if (mVacantCache != null) {
			mVacantCache.clearVacantCells();
			mVacantCache = null;
		}
	}

	/**
	 * Registers the specified listener on each screen contained in this
	 * workspace.
	 * 
	 * @param l
	 *            The listener used to respond to long clicks.
	 */
	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mLongClickListener = l;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setOnLongClickListener(l);
		}
	}

	/**
	 * Registers the specified listener on each screen contained in this
	 * workspace.
	 * 
	 * @param l
	 *            The listener used to respond to long clicks.
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		mClickListener = l;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setOnClickListener(l);
		}
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}

	@Override
	public void computeScroll() {

		int mScrollX = getScrollX(), mScrollY = getScrollY();

		if (mScroller.computeScrollOffset()) {
			mScrollY = mScroller.getCurrY();
			postInvalidate();

			if (Math.abs(mScrollX - getScrollX()) > 0 || Math.abs(mScrollY - getScrollY()) > 0)
				super.scrollTo(mScrollX, mScrollY);

		} else if (mNextScreen != INVALID_SCREEN) {
			mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
			mPreviousIndicator.setLevel(mCurrentScreen);
			mNextIndicator.setLevel(mCurrentScreen);
			mNextScreen = INVALID_SCREEN;
			clearChildrenCache();
		}

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		boolean restore = false;
		int restoreCount = 0;

		// ViewGroup.dispatchDraw() supports many features we don't need:
		// clip to padding, layout animation, animation listener, disappearing
		// children, etc. The following implementation attempts to fast-track
		// the drawing dispatch by drawing only what we know needs to be drawn.

		boolean fastDraw = mNextScreen == INVALID_SCREEN;
		// If we are not scrolling or flinging, draw only the current screen
		if (fastDraw) {
			drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
		} else {
			final long drawingTime = getDrawingTime();
			final float scrollPos = (float) getScrollX() / getWidth();
			final int leftScreen = (int) scrollPos;
			final int rightScreen = leftScreen + 1;
			if (leftScreen >= 0) {
				drawChild(canvas, getChildAt(leftScreen), drawingTime);
			}
			if (scrollPos != leftScreen && rightScreen < getChildCount()) {
				drawChild(canvas, getChildAt(rightScreen), drawingTime);
			}
		}

		if (restore) {
			canvas.restoreToCount(restoreCount);
		}
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		computeScroll();
		if (mDragController != null)
			mDragController.setWindowToken(getWindowToken());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		// final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		// if (widthMode != MeasureSpec.EXACTLY) {
		// throw new
		// IllegalStateException("Workspace can only be used in EXACTLY mode.");
		// }

		// final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		// if (heightMode != MeasureSpec.EXACTLY) {
		// throw new
		// IllegalStateException("Workspace can only be used in EXACTLY mode.");
		// }

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			setHorizontalScrollBarEnabled(false);
			scrollTo(mCurrentScreen * (width), 0);
			setHorizontalScrollBarEnabled(true);
			mFirstLayout = false;
		}

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
		int screen = indexOfChild(child);
		if (screen != mCurrentScreen || !mScroller.isFinished()) {
			snapToScreen(screen);
			return true;
		}
		return false;
	}

	@Override
	protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {

		int focusableScreen;
		if (mNextScreen != INVALID_SCREEN) {
			focusableScreen = mNextScreen;
		} else {
			focusableScreen = mCurrentScreen;
		}
		getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);

		return false;
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		if (direction == View.FOCUS_LEFT) {
			if (getCurrentScreen() > 0) {
				snapToScreen(getCurrentScreen() - 1);
				return true;
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (getCurrentScreen() < getChildCount() - 1) {
				snapToScreen(getCurrentScreen() + 1);
				return true;
			}
		}
		return super.dispatchUnhandledMove(focused, direction);
	}

	@Override
	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {

		getChildAt(mCurrentScreen).addFocusables(views, direction);
		if (direction == View.FOCUS_LEFT) {
			if (mCurrentScreen > 0) {
				getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (mCurrentScreen < getChildCount() - 1) {
				getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
			}
		}

	}

	/**
	 * If one of our descendant views decides that it could be focused now, only
	 * pass that along if it's on the current screen.
	 * 
	 * This happens when live folders requery, and if they're off screen, they
	 * end up calling requestFocus, which pulls it on screen.
	 */
	@Override
	public void focusableViewAvailable(View focused) {
		View current = getChildAt(mCurrentScreen);
		View v = focused;
		while (true) {
			if (v == current) {
				super.focusableViewAvailable(focused);
				return;
			}
			if (v == this) {
				return;
			}
			ViewParent parent = v.getParent();
			if (parent instanceof View) {
				v = (View) v.getParent();
			} else {
				return;
			}
		}
	}

	void enableChildrenCache(int fromScreen, int toScreen) {
		if (fromScreen > toScreen) {
			final int temp = fromScreen;
			fromScreen = toScreen;
			toScreen = temp;
		}

		final int count = getChildCount();

		fromScreen = Math.max(fromScreen, 0);
		toScreen = Math.min(toScreen, count - 1);

		for (int i = fromScreen; i <= toScreen; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);
			layout.setChildrenDrawnWithCacheEnabled(true);
			layout.setChildrenDrawingCacheEnabled(true);
		}
	}

	void clearChildrenCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);
			layout.setChildrenDrawnWithCacheEnabled(false);
		}
	}

	public void snapToScreen(int whichScreen) {
		snapToScreen(whichScreen, 0, false);
	}

	private void snapToScreen(int whichScreen, int velocity, boolean settle) {
		if (!mScroller.isFinished())
			return;

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

		clearVacantCache();
		enableChildrenCache(mCurrentScreen, whichScreen);

		mNextScreen = whichScreen;

		mPreviousIndicator.setLevel(mNextScreen);
		mNextIndicator.setLevel(mNextScreen);

		View focusedChild = getFocusedChild();
		if (focusedChild != null && whichScreen != mCurrentScreen && focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}

		final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		int duration = (screenDelta + 1) * 100;

		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		if (settle) {
			mScrollInterpolator.setDistance(screenDelta);
		} else {
			mScrollInterpolator.disableSettle();
		}

		velocity = Math.abs(velocity);
		if (velocity > 0) {
			duration += (duration / (velocity / BASELINE_FLING_VELOCITY)) * FLING_VELOCITY_INFLUENCE;
		} else {
			duration += 100;
		}

		awakenScrollBars(duration);
		mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();

		if (onScreenChangeListener != null) {
			onScreenChangeListener.onScreenChange(mCurrentScreen, mNextScreen);
		}
	}

	public void startDrag(CellLayout.CellInfo cellInfo) {
		View child = cellInfo.cell;

		// Make sure the drag was started by a long press as opposed to a long
		// click.
		if (child == null)
			return;

		if (!child.isInTouchMode()) {
			return;
		}

		mDragInfo = cellInfo;
		mDragInfo.screen = mCurrentScreen;

		CellLayout current = ((CellLayout) getChildAt(mCurrentScreen));

		current.onDragChild(child);
		mDragController.startDrag(child, this, (ItemCard) child.getTag(), DragController.DRAG_ACTION_MOVE);
		invalidate();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState state = new SavedState(super.onSaveInstanceState());
		state.currentScreen = mCurrentScreen;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		if (savedState.currentScreen != -1) {
			mCurrentScreen = savedState.currentScreen;
		}
	}

	public boolean onDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		final CellLayout cellLayout = getCurrentDropLayout();
		if (source != this) {
			return onDropExternal(x - xOffset, y - yOffset, dragInfo, cellLayout);
		} else {
			// Move internally
			if (mDragInfo != null) {
				final View cell = mDragInfo.cell;

				mTargetCell = estimateDropCell(x - xOffset, y - yOffset, mDragInfo.spanX, mDragInfo.spanY, cell,
						cellLayout, mTargetCell);
				final ItemCard info = (ItemCard) cell.getTag();

				int newScreen = mScroller.isFinished() ? mCurrentScreen : mNextScreen;

				boolean drop = mDragController.onDrop(cell, info, mTargetCell[0], mTargetCell[1], newScreen);

				if (drop) {
					if (newScreen != mDragInfo.screen) {
						final CellLayout originalCellLayout = (CellLayout) getChildAt(mDragInfo.screen);
						originalCellLayout.removeView(cell);
						cellLayout.addView(cell);
					}

					cellLayout.onDropChild(cell, mTargetCell);

					Debug.verbose("d&d item from " + mDragInfo.cellX + "/" + mDragInfo.cellY + " to " + mTargetCell[0]
							+ "/" + mTargetCell[1]);

					// handle drag & drop over different screens (equipment
					// etc...)

					info.getItemInfo().setCellX(mTargetCell[0]);
					info.getItemInfo().setCellY(mTargetCell[1]);
					info.getItemInfo().setScreen(newScreen);
				} else {
					Debug.verbose("Cancel drop due to listener result");
				}
			}
			return true;
		}
	}

	public void onDragEnter(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		clearVacantCache();
	}

	public void onDragOver(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
	}

	public void onDragExit(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		clearVacantCache();
	}

	private boolean onDropExternal(int x, int y, ItemCard dragInfo, CellLayout cellLayout) {
		return onDropExternal(x, y, dragInfo, cellLayout, false);
	}

	@SuppressWarnings("unchecked")
	private boolean onDropExternal(int x, int y, ItemCard dragInfo, CellLayout cellLayout, boolean insertAtFirst) {

		// Drag from somewhere else
		View view = generateView(dragInfo);
		final ItemCard item = (ItemCard) view.getTag();

		mTargetCell = estimateDropCell(x, y, 1, 1, view, cellLayout, mTargetCell);

		boolean drop = mDragController.onDrop(view, item, mTargetCell[0], mTargetCell[1], mCurrentScreen);

		if (drop) {
			cellLayout.addView(view, insertAtFirst ? 0 : -1);
			view.setHapticFeedbackEnabled(false);
			if (view instanceof DropTarget) {
				mDragController.addDropTarget((DropTarget<ItemCard>) view);
			}

			cellLayout.onDropChild(view, mTargetCell);
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();

			item.getItemInfo().setCellX(lp.cellX);
			item.getItemInfo().setCellY(lp.cellY);
			item.getItemInfo().setScreen(mCurrentScreen);
			return true;
		} else {
			Debug.verbose("external Drop canceled due to controller result");
			return false;
		}
	}

	/**
	 * Return the current {@link CellLayout}, correctly picking the destination
	 * screen while a scroll is in progress.
	 */
	private CellLayout getCurrentDropLayout() {
		int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
		return (CellLayout) getChildAt(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean acceptDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		final CellLayout layout = getCurrentDropLayout();
		final CellLayout.CellInfo cellInfo = mDragInfo;
		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;

		if (mVacantCache == null) {
			final View ignoreView = cellInfo == null ? null : cellInfo.cell;
			mVacantCache = layout.findAllVacantCells(null, ignoreView);
		}

		return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public Rect estimateDropLocation(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset,
			DragView dragView, ItemCard dragInfo, Rect recycle) {
		final CellLayout layout = getCurrentDropLayout();

		final CellLayout.CellInfo cellInfo = mDragInfo;
		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
		final View ignoreView = cellInfo == null ? null : cellInfo.cell;

		final Rect location = recycle != null ? recycle : new Rect();

		// Find drop cell and convert into rectangle
		int[] dropCell = estimateDropCell(x - xOffset, y - yOffset, spanX, spanY, ignoreView, layout, mTempCell);

		if (dropCell == null) {
			return null;
		}

		layout.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
		location.left = mTempEstimate[0];
		location.top = mTempEstimate[1];

		layout.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY, mTempEstimate);
		location.right = mTempEstimate[0];
		location.bottom = mTempEstimate[1];

		return location;
	}

	/**
	 * Calculate the nearest cell where the given object would be dropped.
	 */
	private int[] estimateDropCell(int pixelX, int pixelY, int spanX, int spanY, View ignoreView, CellLayout layout,
			int[] recycle) {
		// Create vacant cell cache if none exists
		if (mVacantCache == null) {
			mVacantCache = layout.findAllVacantCells(null, ignoreView);
		}

		// Find the best target drop location
		return layout.findNearestVacantArea(pixelX, pixelY, spanX, spanY, mVacantCache, recycle);
	}

	public void setDragController(DragController<ItemCard> dragController) {
		mDragController = dragController;
	}

	@SuppressWarnings("unchecked")
	public void onDropCompleted(DropTarget<ItemCard> target, boolean success) {
		clearVacantCache();

		if (success) {
			if (target != this && mDragInfo != null) {
				final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
				cellLayout.removeView(mDragInfo.cell);
				if (mDragInfo.cell instanceof DropTarget) {
					mDragController.removeDropTarget((DropTarget<ItemCard>) mDragInfo.cell);
				}
				// final Object tag = mDragInfo.cell.getTag();
			}
		} else {
			if (mDragInfo != null) {
				final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
				cellLayout.onDropAborted(mDragInfo.cell);
			}
		}

		mDragInfo = null;
	}

	public void scrollLeft() {
		clearVacantCache();
		if (mScroller.isFinished()) {
			if (mCurrentScreen > 0)
				snapToScreen(mCurrentScreen - 1);
		} else {
			if (mNextScreen > 0)
				snapToScreen(mNextScreen - 1);
		}
	}

	public void scrollRight() {
		clearVacantCache();
		if (mScroller.isFinished()) {
			if (mCurrentScreen < getChildCount() - 1)
				snapToScreen(mCurrentScreen + 1);
		} else {
			if (mNextScreen < getChildCount() - 1)
				snapToScreen(mNextScreen + 1);
		}
	}

	public int getScreenForView(View v) {
		int result = -1;
		if (v != null) {
			ViewParent vp = v.getParent();
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				if (vp == getChildAt(i)) {
					return i;
				}
			}
		}
		return result;
	}

	public View getViewForTag(Object tag) {
		int screenCount = getChildCount();
		for (int screen = 0; screen < screenCount; screen++) {
			CellLayout currentScreen = ((CellLayout) getChildAt(screen));
			int count = currentScreen.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = currentScreen.getChildAt(i);
				if (child.getTag() == tag) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * @return True is long presses are still allowed for the current touch
	 */
	public boolean allowLongPress() {
		return mAllowLongPress;
	}

	/**
	 * Set true to allow long-press events to be triggered, usually checked by
	 * {@link Launcher2} to accept or block dpad-initiated long-presses.
	 */
	public void setAllowLongPress(boolean allowLongPress) {
		mAllowLongPress = allowLongPress;
	}

	public void replaceItem(final ItemCard oldItem, final ItemCard newItem) {

		newItem.getItemInfo().setCellX(oldItem.getItemInfo().getCellX());
		newItem.getItemInfo().setCellY(oldItem.getItemInfo().getCellY());
		newItem.getItemInfo().setScreen(oldItem.getItemInfo().getScreen());
		removeItem(oldItem);

		// post it since it has to run after reomveItem is finished!
		post(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				addItemInScreen(newItem);
			}
		});
	}

	public void removeAllItems() {
		final int count = getChildCount();

		for (int i = 0; i < count; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);
			layout.removeAllViews();

		}
		clearChildrenCache();
	}

	public void removeItem(final ItemCard apps) {
		final int count = getChildCount();

		for (int i = 0; i < count; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);

			// Avoid ANRs by treating each screen separately
			post(new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					int childCount = layout.getChildCount();
					for (int j = 0; j < childCount; j++) {
						final View view = layout.getChildAt(j);
						ItemCard tag = (ItemCard) view.getTag();

						if (apps.equals(tag)) {

							layout.removeViewInLayout(view);
							if (view instanceof DropTarget) {
								mDragController.removeDropTarget((DropTarget<ItemCard>) view);
							}
							layout.requestLayout();
							layout.invalidate();

							clearChildrenCache();
							break;
						}
					}
				}
			});
		}
	}

	public void updateShortcuts(ArrayList<ItemCard> apps) {

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);
			int childCount = layout.getChildCount();
			for (int j = 0; j < childCount; j++) {
				final View view = layout.getChildAt(j);
				Object tag = view.getTag();
				if (tag instanceof ItemCard) {
					// ItemCard info = (ItemCard) tag;

					// TODO
				}
			}
		}
	}

	public void moveToDefaultScreen(boolean animate) {
		if (animate) {
			snapToScreen(mDefaultScreen);
		} else {
			setCurrentScreen(mDefaultScreen);
		}
		getChildAt(mDefaultScreen).requestFocus();
	}

	public void setIndicators(Drawable previous, Drawable next) {
		mPreviousIndicator = previous;
		mNextIndicator = next;
		previous.setLevel(mCurrentScreen);
		next.setLevel(mCurrentScreen);
	}

	public static class SavedState extends BaseSavedState {
		int currentScreen = -1;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentScreen = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(currentScreen);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
