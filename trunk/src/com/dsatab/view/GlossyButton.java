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
import android.widget.Button;

import com.dsatab.R;

/**
 * @author Ganymede
 * 
 */
public class GlossyButton extends Button {

	private Drawable mGlossDrawable;
	private Drawable mMaskDrawable;
	private Rect mBounds;
	private RectF mBoundsF;

	private Paint mMaskedPaint;
	private Paint mCopyPaint;

	public GlossyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GlossyButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.glossyButtonStyle);
	}

	public GlossyButton(Context context) {
		this(context, null);
	}

	/**
	 * 
	 */
	private void init() {
		mGlossDrawable = getResources().getDrawable(R.drawable.btn_glossy_gloss);
		if (!isInEditMode())
			mGlossDrawable.setCallback(this);
		mMaskDrawable = getResources().getDrawable(R.drawable.btn_glossy_mask);
		if (!isInEditMode())
			mMaskDrawable.setCallback(this);

		mMaskedPaint = new Paint();
		mMaskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		mCopyPaint = new Paint();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		int sc = canvas.saveLayer(mBoundsF, mCopyPaint, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
				| Canvas.FULL_COLOR_LAYER_SAVE_FLAG);

		super.onDraw(canvas);
		canvas.saveLayer(mBoundsF, mMaskedPaint, 0);
		if (mMaskDrawable != null)
			mMaskDrawable.draw(canvas);
		canvas.restoreToCount(sc);
		if (mGlossDrawable != null)
			mGlossDrawable.draw(canvas);

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
		if (mGlossDrawable.isStateful()) {
			mGlossDrawable.setState(getDrawableState());
		}
		if (mMaskDrawable.isStateful()) {
			mMaskDrawable.setState(getDrawableState());
		}

		// TODO: is this the right place to invalidate?
		invalidate();
	}

}
