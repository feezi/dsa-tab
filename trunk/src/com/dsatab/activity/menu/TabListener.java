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
package com.dsatab.activity.menu;

import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.dsatab.activity.MainActivity;

/**
 * @author Ganymede
 * 
 */
public class TabListener<T extends Fragment> implements ActionBar.TabListener {

	private final MainActivity mActivity;

	private final int tabIndex;

	/**
	 * Constructor used each time a new tab is created.
	 * 
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param tabIndex
	 *            The index of the tab to show
	 */
	public TabListener(MainActivity activity, int tabIndex) {
		this.mActivity = activity;
		this.tabIndex = tabIndex;
	}

	/* The following are each of the ActionBar.TabListener callbacks */

	public void onTabSelected(Tab tab) {
		mActivity.showTab(tabIndex);
	}

	public void onTabUnselected(Tab tab) {

	}

	public void onTabReselected(Tab tab) {
		// User selected the already selected tab. Usually do nothing.
	}

}