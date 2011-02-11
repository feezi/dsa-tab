package com.dsatab.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.dsatab.common.Debug;

public class ViewFlipperFix extends ViewFlipper {

	public ViewFlipperFix(Context context) {
		super(context);

	}

	public ViewFlipperFix(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	protected void onDetachedFromWindow() {
		int apiLevel = Integer.parseInt(Build.VERSION.SDK);

		if (apiLevel >= 7) {
			try {
				super.onDetachedFromWindow();
			} catch (IllegalArgumentException e) {
				Debug.warning("Android project issue 6191 workaround.");
				/* Quick catch and continue on api level >=7, the Eclair 2.1 */
			} finally {
				super.stopFlipping();
			}
		} else {
			super.onDetachedFromWindow();
		}
	}

}
