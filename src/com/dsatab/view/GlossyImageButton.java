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
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;
import android.widget.ImageButton;

import com.dsatab.R;
import com.dsatab.common.Util;

/**
 * @author Ganymede
 * 
 */
public class GlossyImageButton extends ImageButton implements Checkable {

	private boolean mChecked;

	private Drawable mGlossDrawable;
	private Drawable mMaskDrawable;
	private Rect mBounds;
	private RectF mBoundsF;

	private Paint mMaskedPaint;
	private Paint mCopyPaint;

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	public GlossyImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GlossyImageButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.glossyImageButtonStyle);
	}

	public GlossyImageButton(Context context) {
		this(context, null, R.attr.glossyImageButtonStyle);
	}

	public void toggle() {
		setChecked(!mChecked);
	}

	@ViewDebug.ExportedProperty
	public boolean isChecked() {
		return mChecked;
	}

	/**
	 * <p>
	 * Changes the checked state of this text view.
	 * </p>
	 * 
	 * @param checked
	 *            true to check the text, false to uncheck it
	 */
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			refreshDrawableState();
		}
	}

	/**
	 * 
	 */
	private void init() {

		int overlayId = Util.getThemeResourceId(getContext(), R.attr.glossyButtonOverlay);
		if (overlayId > 0) {
			mGlossDrawable = getResources().getDrawable(R.drawable.btn_glossy_gloss);
			if (!isInEditMode())
				mGlossDrawable.setCallback(this);
		}

		int maskId = Util.getThemeResourceId(getContext(), R.attr.glossyButtonMask);
		if (maskId > 0) {
			mMaskDrawable = getResources().getDrawable(maskId);
			if (!isInEditMode())
				mMaskDrawable.setCallback(this);

			mMaskedPaint = new Paint();
			mMaskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

			mCopyPaint = new Paint();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		if (!isInEditMode() && (mMaskDrawable != null || mGlossDrawable != null)) {
			int sc = canvas.saveLayer(mBoundsF, mCopyPaint, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
					| Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
			super.onDraw(canvas);
			if (mMaskDrawable != null) {
				canvas.saveLayer(mBoundsF, mMaskedPaint, 0);
				mMaskDrawable.draw(canvas);
				canvas.restoreToCount(sc);
			}
			if (mGlossDrawable != null)
				mGlossDrawable.draw(canvas);

		} else {
			super.onDraw(canvas);
		}

	}

	@Override
	protected boolean setFrame(int l, int t, int r, int b) {
		final boolean changed = super.setFrame(l, t, r, b);
		mBounds = new Rect(0, 0, r - l, b - t);
		mBoundsF = new RectF(mBounds);
		if (mGlossDrawable != null)
			mGlossDrawable.setBounds(mBounds);
		if (mMaskDrawable != null)
			mMaskDrawable.setBounds(mBounds);

		return changed;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mGlossDrawable != null && mGlossDrawable.isStateful()) {
			mGlossDrawable.setState(getDrawableState());
		}
		if (mMaskDrawable != null && mMaskDrawable.isStateful()) {
			mMaskDrawable.setState(getDrawableState());
		}

		// TODO: is this the right place to invalidate?
		if (mGlossDrawable != null || mMaskDrawable != null)
			invalidate();
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		boolean populated = super.dispatchPopulateAccessibilityEvent(event);
		if (!populated) {
			event.setChecked(mChecked);
		}
		return populated;
	}
}
