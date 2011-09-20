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
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;

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

	/**
	 * 
	 */
	public BaseFragmentActivity() {

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
