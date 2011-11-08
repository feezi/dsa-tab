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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * @author Ganymede
 * 
 */
public class TileLinearLayout extends LinearLayout {

	private boolean backgroundStretched = false;

	public TileLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TileLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TileLinearLayout(Context context) {
		super(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		backgroundStretched = false;
		init();
	}

	private void init() {
		if (backgroundStretched)
			return;

		if (getBackground() instanceof BitmapDrawable) {
			BitmapDrawable bd = (BitmapDrawable) getBackground();
			int width = getWidth();
			int height = getHeight();
			if (width > 0 && height > 0) {
				int intrinsicHeight = bd.getIntrinsicHeight();
				int intrinsicWidth = bd.getIntrinsicWidth();

				Bitmap bitmap = Bitmap.createBitmap(intrinsicWidth, height, bd.getBitmap().getConfig());

				int clampSize = 0;
				if (height > intrinsicHeight) {
					clampSize = height - intrinsicHeight;
				}

				Canvas canvas = new Canvas(bitmap);

				Rect src = new Rect(0, 0, intrinsicWidth, intrinsicHeight);
				Rect dest = new Rect(0, clampSize, intrinsicWidth, height);

				canvas.drawBitmap(bd.getBitmap(), src, dest, null);

				if (clampSize > 0) {
					// clamp bitmap on top
					src.set(0, 0, intrinsicWidth, 1);
					dest.set(0, 0, width, clampSize);
					canvas.drawBitmap(bd.getBitmap(), src, dest, null);
				}
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
				bitmapDrawable.setTileModeX(TileMode.MIRROR);
				bitmapDrawable.setBounds(0, 0, intrinsicWidth, height);
				setBackgroundDrawable(bitmapDrawable);
				backgroundStretched = true;
			}
		}
	}

	protected void onDraw(android.graphics.Canvas canvas) {
		init();
		super.onDraw(canvas);

	};

}
