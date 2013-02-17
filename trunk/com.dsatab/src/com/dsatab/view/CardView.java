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

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;

import com.dsatab.DsaTabApplication;
import com.dsatab.R;
import com.dsatab.data.items.ItemCard;
import com.dsatab.xml.DataManager;

/**
 * 
 */
public class CardView extends ImageView implements Checkable {

	private static final int HQ_IMAGE_SIZE = 300;
	private static final int LQ_IMAGE_SIZE = 120;

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	private ItemCard item;

	private String itemText;

	private boolean hasCardImage;

	private Paint paint;

	private Path textPath;

	private boolean calculated = false;

	private boolean highQuality = false;

	private static int TEXT_PADDING = 20;

	boolean mChecked = false;

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
	@TargetApi(11)
	private void init() {

		setDrawingCacheEnabled(false);
		setBackgroundResource(R.drawable.border_patch);
		setScaleType(ScaleType.FIT_XY);

		paint = new Paint();
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(20);
		paint.setAntiAlias(true);
		if (!isInEditMode()) {
			paint.setTypeface(DsaTabApplication.getInstance().getPoorRichardFont());
			TEXT_PADDING = getResources().getDimensionPixelOffset(R.dimen.card_text_padding);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			refreshDrawableState();
		}
	}

	@Override
	public void toggle() {
		mChecked = !mChecked;
		refreshDrawableState();
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	public ItemCard getItem() {
		return item;
	}

	public void setItem(Cursor c) {

	}

	public void setItem(ItemCard item) {
		this.item = item;

		calculated = false;
		setTag(item);

		if (item != null && item.hasImage()) {
			if (highQuality) {
				setImageBitmap(DataManager.getBitmap(item.getImageUri(), HQ_IMAGE_SIZE));
				setScaleType(ScaleType.FIT_CENTER);
			} else {
				setImageBitmap(DataManager.getBitmap(item.getImageUri(), LQ_IMAGE_SIZE));
				setScaleType(ScaleType.FIT_CENTER);
			}
			hasCardImage = true;
			itemText = null;
		} else {
			hasCardImage = false;
			setImageResource(R.drawable.item_card);
			setScaleType(ScaleType.FIT_XY);
			itemText = item.getTitle();
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
		if (calculated || TextUtils.isEmpty(itemText))
			return;
		paint.setTextSize(getWidth() / 7);

		int maxWidth;
		if (hasCardImage) {
			maxWidth = (w - TEXT_PADDING * 2);
		} else {
			maxWidth = (int) Math.sqrt((w - TEXT_PADDING * 2) * (w - TEXT_PADDING * 2) + (h - TEXT_PADDING * 2)
					* (h - TEXT_PADDING * 2));
		}

		float width = paint.measureText(itemText);

		while (width > maxWidth && paint.getTextSize() > 1.0f) {
			paint.setTextSize(paint.getTextSize() - 2);
			width = paint.measureText(itemText);
		}

		if (textPath == null)
			textPath = new Path();
		else
			textPath.reset();

		if (hasCardImage) {
			textPath.moveTo(TEXT_PADDING, TEXT_PADDING);
			textPath.lineTo(w - TEXT_PADDING, TEXT_PADDING);
		} else {
			textPath.moveTo(TEXT_PADDING, TEXT_PADDING);
			textPath.lineTo(w - TEXT_PADDING, h - TEXT_PADDING);
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
		if (!TextUtils.isEmpty(itemText)) {
			calcTextSize(getWidth(), getHeight());
			canvas.drawTextOnPath(itemText, textPath, 0, paint.getTextSize() / 2, paint);
		}
	}
}