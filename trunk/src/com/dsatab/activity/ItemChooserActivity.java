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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.data.items.Item;
import com.dsatab.fragment.ItemChooserFragment;
import com.dsatab.fragment.ItemChooserFragment.OnItemChooserListener;
import com.dsatab.util.Debug;

public class ItemChooserActivity extends BaseFragmentActivity implements OnItemChooserListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(DSATabApplication.getInstance().getCustomTheme());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_item_chooser);

		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.ItemChooserFragment.OnItemChooserListener#onDismiss()
	 */
	@Override
	public void onItemCanceled() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected
	 * (com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onItemCanceled();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.ItemChooserFragment.OnItemChooserListener#onItemSelected
	 * (com.dsatab.data.items.Item)
	 */
	@Override
	public void onItemSelected(Item item, int cellNumber) {
		Debug.verbose("Selected " + item.getName());
		Intent intent = new Intent();
		intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_TYPE, item.getSpecifications().get(0).getType().name());
		intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_NAME, item.getName());
		intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_ID, item.getId());
		intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CATEGORY, item.getCategory());

		intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CELL, cellNumber);

		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}
