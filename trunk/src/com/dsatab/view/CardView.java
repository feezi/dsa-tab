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
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.xml.DataManager;

/**
 * 
 */
public class CardView extends ImageView {

	private ItemCard item;

	private boolean hasCardImage;

	private Paint paint;

	private Path textPath;

	private boolean calculated = false;

	private boolean highQuality = false;

	private static int TEXT_PADDING = 20;

	/**
	 * @param context
	 */
	public CardView(Context context, ItemCard item) {
		this(context, null, 0);
		setItem(item);
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

		setDrawingCacheEnabled(false);
		setBackgroundResource(R.drawable.border_patch);
		setScaleType(ScaleType.FIT_XY);

		paint = new Paint();
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(20);
		paint.setAntiAlias(true);
		if (!isInEditMode()) {
			paint.setTypeface(DSATabApplication.getInstance().getPoorRichardFont());
		}
		TEXT_PADDING = getResources().getDimensionPixelOffset(R.dimen.card_text_padding);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	public ItemCard getItem() {
		return item;
	}

	public void setItem(Cursor c) {

	}

	public void setItem(ItemCard item) {
		this.item = item;
		hasCardImage = false;
		calculated = false;
		setTag(item);

		if (highQuality && item != null && item.getHQFile() != null && item.getHQFile().isFile()
				&& !item.getHQFile().getName().equals(Item.BLANK_PATH_HQ)) {
			hasCardImage = true;
			setImageBitmap(Util.decodeFile(item.getHQFile(), 400));
			setScaleType(ScaleType.FIT_CENTER);
		}

		if (!hasCardImage && item != null && item.getFile() != null && item.getFile().isFile()
				&& !item.getFile().getName().equals(Item.BLANK_PATH)) {
			hasCardImage = true;
			setImageBitmap(DataManager.getBitmap(item.getFile().getAbsolutePath()));
			setScaleType(ScaleType.FIT_CENTER);
		}

		if (!hasCardImage) {
			setImageResource(R.drawable.item_card);
			setScaleType(ScaleType.FIT_XY);
		}

		invalidate();
	}

	public boolean isHighQuality() {
		return highQuality;
	}

	public void setHighQuality(boolean highQuality) {
		this.highQuality = highQuality;
	}

	private void calcTextSize(int w, int h) {
		if (calculated || item == null)
			return;
		paint.setTextSize(getWidth() / 7);

		int maxWidth = (int) Math.sqrt((w - TEXT_PADDING * 2) * (w - TEXT_PADDING * 2) + (h - TEXT_PADDING * 2)
				* (h - TEXT_PADDING * 2));

		final String title = item.getTitle();

		float width = paint.measureText(title);

		while (width > maxWidth && paint.getTextSize() > 1.0f) {
			paint.setTextSize(paint.getTextSize() - 2);
			width = paint.measureText(title);
		}

		if (textPath == null)
			textPath = new Path();
		else
			textPath.reset();
		textPath.moveTo(TEXT_PADDING, TEXT_PADDING);
		textPath.lineTo(w - TEXT_PADDING, h - TEXT_PADDING);

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
		if (item != null && !hasCardImage) {
			calcTextSize(getWidth(), getHeight());
			canvas.drawTextOnPath(item.getTitle(), textPath, 0, paint.getTextSize() / 2, paint);
		}

	}
}