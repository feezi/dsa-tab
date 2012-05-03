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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

import com.dsatab.R;

/**
 * @author Ganymede
 * 
 */
public class PageButton extends Button {
	private int level;
	private int maxLevel;

	private Rect container;

	private Drawable drawable;

	private Rect bounds = new Rect();

	private static final float SHRINK = 0.5f;

	public PageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PageButton(Context context) {
		super(context);
		init();
	}

	/**
	 * 
	 */
	private void init() {
		drawable = getResources().getDrawable(R.drawable.page_button);
		drawable.setCallback(this);

		container = new Rect();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
		invalidate();
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		if (maxLevel != this.maxLevel) {
			this.maxLevel = maxLevel;
			requestLayout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#invalidate()
	 */
	@Override
	public void invalidate() {
		super.invalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TextView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int prefHeight = resolveSize(drawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom(),
				heightMeasureSpec);

		int prefWidth = resolveSize(getButtonsWidth(maxLevel, prefHeight - getPaddingTop() - getPaddingBottom())
				+ getPaddingLeft() + getPaddingRight(), widthMeasureSpec);

		setMeasuredDimension(prefWidth, prefHeight);
	}

	private int getButtonsWidth(int items, int height) {
		if (items > 0) {
			return (int) (height + (height * SHRINK * (items - 1)));
		} else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TextView#onDraw(android.graphics.Canvas)
	 */
	@Override
	public void onDraw(Canvas canvas) {
		int buttonSize = getHeight() - getPaddingTop() - getPaddingBottom();

		bounds.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());

		Gravity.apply(getGravity(), getButtonsWidth(level, buttonSize), buttonSize, bounds, container);

		int left = 0;
		for (int i = 0; i < level; i++) {
			left = (int) (container.left + (buttonSize * SHRINK * i));
			drawable.setBounds(left, container.top, left + buttonSize, container.top + buttonSize);
			drawable.draw(canvas);
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (drawable != null && drawable.isStateful()) {
			drawable.setState(getDrawableState());
			invalidate();
		}
	}
}
