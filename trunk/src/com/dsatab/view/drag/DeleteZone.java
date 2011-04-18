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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.data.Hero;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.view.FastAnimationSet;
import com.dsatab.view.FastTranslateAnimation;
import com.gandulf.guilib.drag.DragController;
import com.gandulf.guilib.drag.DragSource;
import com.gandulf.guilib.drag.DragView;
import com.gandulf.guilib.drag.DropTarget;

public class DeleteZone extends ImageView implements DropTarget<ItemCard>, DragController.DragListener<ItemCard> {

	public static final int ORIENTATION_HORIZONTAL = 1;
	private static final int TRANSITION_DURATION = 250;
	private static final int ANIMATION_DURATION = 200;

	private final int[] mLocation = new int[2];

	private boolean mTrashMode;

	private AnimationSet mInAnimation;
	private AnimationSet mOutAnimation;
	private Animation mHandleInAnimation;
	private Animation mHandleOutAnimation;

	private int mOrientation;
	private DragController<ItemCard> mDragController;

	private final RectF mRegion = new RectF();
	private TransitionDrawable mTransition;

	private final Paint mTrashPaint = new Paint();

	public DeleteZone(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		final int srcColor = context.getResources().getColor(R.color.delete_color_filter);
		mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));

		// TypedArray a = context.obtainStyledAttributes(attrs,
		// R.styleable.DeleteZone, defStyle, 0);
		// mOrientation = a.getInt(R.styleable.DeleteZone_direction,
		// ORIENTATION_HORIZONTAL);
		mOrientation = ORIENTATION_HORIZONTAL;
		// a.recycle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTransition = (TransitionDrawable) getDrawable();
	}

	public boolean acceptDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		return true;
	}

	public Rect estimateDropLocation(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset,
			DragView dragView, ItemCard dragInfo, Rect recycle) {
		return null;
	}

	public boolean onDrop(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {

		Hero hero = DSATabApplication.getInstance().getHero();

		if (dragInfo instanceof Item) {
			final Item item = (Item) dragInfo;

			if (item.getType().isEquipable()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							Hero hero = DSATabApplication.getInstance().getHero();
							hero.removeItem(item);
						}
					}
				};
				builder.setTitle("Wirklich löschen?");
				builder.setMessage("Falls dieser Gegenstand gelöscht wird, wird er auch aus allen Ausrüstungssets entfernt.");
				builder.setPositiveButton("Löschen", listener);
				builder.setNegativeButton("Abbrechen", listener);
				builder.show();

				return false;
			}

		} else if (dragInfo instanceof EquippedItem)
			hero.removeEquippedItem((EquippedItem) dragInfo);
		else
			hero.removeItem(dragInfo.getItem());

		return true;
	}

	public void onDragEnter(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		mTransition.reverseTransition(TRANSITION_DURATION);
		dragView.setPaint(mTrashPaint);
	}

	public void onDragOver(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
	}

	public void onDragExit(DragSource<ItemCard> source, int x, int y, int xOffset, int yOffset, DragView dragView,
			ItemCard dragInfo) {
		mTransition.reverseTransition(TRANSITION_DURATION);
		dragView.setPaint(null);
	}

	public void onDragStart(DragSource<ItemCard> source, ItemCard item, int dragAction) {

		if (item != null) {
			mTrashMode = true;
			createAnimations();
			final int[] location = mLocation;
			getLocationOnScreen(location);
			mRegion.set(location[0], location[1], location[0] + getRight() - getLeft(), location[1] + getBottom()
					- getTop());
			mDragController.setDeleteRegion(mRegion);
			mTransition.resetTransition();
			startAnimation(mInAnimation);

			setVisibility(VISIBLE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.drag.DragController.DragListener#onDragDrop(java.lang
	 * .Object, int, int, int)
	 */
	@Override
	public boolean onDragDrop(View cell, ItemCard dragInfo, int x, int y, int screen) {
		return true;
	}

	public void onDragEnd() {
		if (mTrashMode) {
			mTrashMode = false;
			mDragController.setDeleteRegion(null);
			startAnimation(mOutAnimation);
			setVisibility(GONE);
		}
	}

	private void createAnimations() {
		if (mInAnimation == null) {
			mInAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mInAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
			if (mOrientation == ORIENTATION_HORIZONTAL) {
				animationSet.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
						Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f));
			} else {
				animationSet.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
						Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f));
			}
			animationSet.setDuration(ANIMATION_DURATION);
		}
		if (mHandleInAnimation == null) {
			mHandleInAnimation = new AlphaAnimation(0.0f, 1.0f);
			mHandleInAnimation.setDuration(ANIMATION_DURATION);
		}
		if (mOutAnimation == null) {
			mOutAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mOutAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
			if (mOrientation == ORIENTATION_HORIZONTAL) {
				animationSet.addAnimation(new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE,
						0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f));
			} else {
				animationSet.addAnimation(new FastTranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f));
			}
			animationSet.setDuration(ANIMATION_DURATION);
		}
		if (mHandleOutAnimation == null) {
			mHandleOutAnimation = new AlphaAnimation(1.0f, 0.0f);
			mHandleOutAnimation.setFillAfter(true);
			mHandleOutAnimation.setDuration(ANIMATION_DURATION);
		}
	}

	public void setDragController(DragController<ItemCard> dragController) {
		mDragController = dragController;
	}

	public int getOrientation() {
		return mOrientation;
	}

	public void setOrientation(int orientation) {
		this.mOrientation = orientation;
	}

}
