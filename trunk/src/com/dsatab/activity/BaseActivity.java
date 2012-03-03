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
package com.dsatab.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockActivity;
import com.dsatab.DSATabApplication;
import com.dsatab.common.Util;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class BaseActivity extends SherlockActivity {

	protected SharedPreferences preferences;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		preferences = DSATabApplication.getPreferences();
		super.onCreate(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Debug.verbose("Unbinding drawbale to free memory");
		Util.unbindDrawables(getWindow().getDecorView());

		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		updateFullscreenStatus(preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == MainActivity.ACTION_PREFERENCES) {

			SharedPreferences preferences = DSATabApplication.getPreferences();

			String orientation = preferences.getString(BasePreferenceActivity.KEY_SCREEN_ORIENTATION,
					BasePreferenceActivity.DEFAULT_SCREEN_ORIENTATION);
			if (BasePreferenceActivity.SCREEN_ORIENTATION_LANDSCAPE.equals(orientation)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else if (BasePreferenceActivity.SCREEN_ORIENTATION_PORTRAIT.equals(orientation)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if (BasePreferenceActivity.SCREEN_ORIENTATION_AUTO.equals(orientation)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}

			updateFullscreenStatus(preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void updateFullscreenStatus(boolean bUseFullscreen) {
		if (bUseFullscreen) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		getWindow().getDecorView().requestLayout();
	}
}
