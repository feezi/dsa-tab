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
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * @author Ganymede
 * 
 */
public class FlingableLinearLayout extends LinearLayout {

	private boolean tabFlingEnabled = true;

	private OnFlingListener onFlingListener;

	public interface OnFlingListener {
		void onFling(boolean right);
	}

	public FlingableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private final GestureDetector gdt = new GestureDetector(new SimpleOnGestureListener() {

		private static final int SWIPE_MIN_DISTANCE = 60;
		private static final int SWIPE_THRESHOLD_VELOCITY = 100;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

			if (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
					if (tabFlingEnabled && onFlingListener != null)
						onFlingListener.onFling(true);
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
					if (tabFlingEnabled && onFlingListener != null)
						onFlingListener.onFling(false);
					return true;
				}
			}
			return false;
		}

	});

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (tabFlingEnabled)
			return gdt.onTouchEvent(ev);
		else
			return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (tabFlingEnabled) {
			gdt.onTouchEvent(event);
			return true;
		} else
			return false;
	}

	public boolean isTabFlingEnabled() {
		return tabFlingEnabled;
	}

	public void setTabFlingEnabled(boolean tabFlingEnabled) {
		this.tabFlingEnabled = tabFlingEnabled;
	}

	public OnFlingListener getOnFlingListener() {
		return onFlingListener;
	}

	public void setOnFlingListener(OnFlingListener onFlingListener) {
		this.onFlingListener = onFlingListener;
	}

}
