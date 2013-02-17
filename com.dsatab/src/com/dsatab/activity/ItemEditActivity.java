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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.dsatab.DsaTabApplication;
import com.dsatab.R;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemCard;
import com.dsatab.fragment.ItemEditFragment;

public class ItemEditActivity extends BaseFragmentActivity {

	private ItemEditFragment fragment;

	public static void create(Context context) {
		Intent intent = new Intent(context, ItemEditActivity.class);
		intent.setAction(Intent.ACTION_PICK);
		context.startActivity(intent);
	}

	public static void edit(Context context, ItemCard itemCard) {
		if (itemCard != null) {

			Item item = itemCard.getItem();

			Intent intent = new Intent(context, ItemEditActivity.class);
			intent.setAction(Intent.ACTION_PICK);
			intent.putExtra(ItemEditFragment.INTENT_EXTRA_ITEM_ID, item.getId());

			context.startActivity(intent);
		}
	}

	public static void view(Context context, ItemCard itemCard) {
		if (itemCard != null) {

			Item item = itemCard.getItem();

			Intent intent = new Intent(context, ItemEditActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.putExtra(ItemEditFragment.INTENT_EXTRA_ITEM_ID, item.getId());

			context.startActivity(intent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(DsaTabApplication.getInstance().getCustomTheme());
		applyPreferencesToTheme();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_item_edit);

		fragment = (ItemEditFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_item_edit);

		// Inflate a "Done/Discard" custom action bar view.
		LayoutInflater inflater = LayoutInflater.from(this);
		final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_discard, null);
		customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Item item = fragment.accept();
				if (DsaTabApplication.getInstance().getHero().getItem(item.getId()) == null) {
					DsaTabApplication.getInstance().getHero().addItem(item);
				} else {
					DsaTabApplication.getInstance().getHero().fireItemChangedEvent(item);
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// fragment.cancel();
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		// Show the custom action bar view and hide the normal Home icon and
		// title.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
	}

}
