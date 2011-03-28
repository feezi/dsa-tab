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

import java.io.File;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.data.items.ItemCard;

/**
 * 
 */
public class CardView extends ImageView {

	private ItemCard item;

	private Boolean hasCardImage;

	private Paint paint;

	private boolean calculated = false;

	private static int TEXT_PADDING = 20;

	/**
	 * @param context
	 */
	public CardView(Context context, ItemCard item) {
		this(context, null, 0);
		this.item = item;
	}

	public CardView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public CardView(Context context) {
		this(context, null, 0);
	}

	public CardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 
	 */
	private void init() {

		setBackgroundResource(R.drawable.border_patch);
		setScaleType(ScaleType.FIT_XY);

		paint = new Paint();
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(20);
		paint.setAntiAlias(true);
		paint.setTypeface(DSATabApplication.getInstance().getPoorRichardFont());

		TEXT_PADDING = getResources().getDimensionPixelOffset(R.dimen.card_text_padding);
	}

	public ItemCard getItem() {
		return item;
	}

	public void setItem(ItemCard item) {
		this.item = item;
		hasCardImage = null;
		calculated = false;
	}

	private boolean hasCardImage() {

		if (hasCardImage == null) {
			File lqFile = item.getFile();
			if (lqFile == null || !lqFile.isFile())
				hasCardImage = false;
			else
				hasCardImage = (!lqFile.getName().equals("blank_LQ.gif"));
		}
		return hasCardImage;

	}

	private void calcTextSize(int w, int h) {
		if (calculated)
			return;
		paint.setTextSize(getWidth() / 7);

		int maxWidth = (int) Math.sqrt((w - TEXT_PADDING * 2) * (w - TEXT_PADDING * 2) + (h - TEXT_PADDING * 2)
				* (h - TEXT_PADDING * 2));

		float width = paint.measureText(item.getTitle());

		while (width > maxWidth) {
			paint.setTextSize(paint.getTextSize() - 2);
			width = paint.measureText(item.getTitle());
		}

		calculated = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		calculated = false;
		calcTextSize(w, h);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!hasCardImage()) {
			calcTextSize(getWidth(), getHeight());

			Path path = new Path();
			path.moveTo(TEXT_PADDING, TEXT_PADDING);
			path.lineTo(getWidth() - TEXT_PADDING, getHeight() - TEXT_PADDING);
			canvas.drawTextOnPath(item.getTitle(), path, 0, paint.getTextSize() / 2, paint);
		}
	}
}