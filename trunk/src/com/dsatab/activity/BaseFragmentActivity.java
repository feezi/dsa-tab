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

import yuku.androidsdk.com.android.internal.view.menu.MenuBuilder;
import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;

import com.dsatab.DSATabApplication;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;

/**
 * @author Ganymede
 * 
 */
public class BaseFragmentActivity extends FragmentActivity implements IconContextItemSelectedListener {

	private OnLongClickListener contextMenuListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			Menu menu = new MenuBuilder(BaseFragmentActivity.this);
			Object info = onCreateIconContextMenu(menu, v, null);

			if (menu != null && menu.hasVisibleItems()) {
				IconContextMenu cm = new IconContextMenu(BaseFragmentActivity.this, menu);
				cm.setInfo(info);
				cm.setOnIconContextItemSelectedListener(BaseFragmentActivity.this);

				onPrepareIconContextMenu(cm, v);
				cm.show();
				return true;
			}

			return false;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.FragmentActivity#onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		SharedPreferences preferences = DSATabApplication.getPreferences();
		updateFullscreenStatus(preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Util.unbindDrawables(getWindow().getDecorView());
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MainActivity.ACTION_PREFERENCES) {

			SharedPreferences preferences = DSATabApplication.getPreferences();

			String orientation = preferences.getString(BasePreferenceActivity.KEY_SCREEN_ORIENTATION,
					BasePreferenceActivity.DEFAULT_SCREEN_ORIENTATION);

			if (BasePreferenceActivity.SCREEN_ORIENTATION_LANDSCAPE.equals(orientation)) {
				if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
					// You need to check if your desired orientation isn't
					// already set because setting orientation restarts your
					// Activity which takes long
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			} else if (BasePreferenceActivity.SCREEN_ORIENTATION_PORTRAIT.equals(orientation)) {
				if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			} else if (BasePreferenceActivity.SCREEN_ORIENTATION_AUTO.equals(orientation)) {
				if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
				}
			}

			updateFullscreenStatus(preferences.getBoolean(BasePreferenceActivity.KEY_FULLSCREEN, true));

			if (DSATabApplication.getInstance().getHero() != null) {
				Hero hero = DSATabApplication.getInstance().getHero();
				hero.firePreferencesChanged();
			}
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

	public void onPrepareIconContextMenu(IconContextMenu cm, View v) {

	}

	public void registerForIconContextMenu(View v) {
		v.setOnLongClickListener(contextMenuListener);
	}

	public Object onCreateIconContextMenu(Menu menu, View v, ContextMenuInfo menuInfo) {
		return null;
	}

	public void onIconContextItemSelected(MenuItem item, Object info) {

	}

}
