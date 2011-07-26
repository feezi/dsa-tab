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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.gandulf.guilib.util.Debug;

/**
 * 
 * 
 */
public class LinearListLayout extends LinearLayout {

	private List<BaseAdapter> adapters;

	class Observer extends DataSetObserver {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.DataSetObserver#onChanged()
		 */
		@Override
		public void onChanged() {
			super.onChanged();
			refreshRowItems();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.DataSetObserver#onInvalidated()
		 */
		@Override
		public void onInvalidated() {
			super.onInvalidated();
			refreshRowItems();
		}
	}

	Observer observer = new Observer();

	public LinearListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LinearListLayout(Context context) {
		super(context);
		init();
	}

	public void addAdapter(BaseAdapter adapter) {
		adapter.registerDataSetObserver(observer);
		adapters.add(adapter);
		addRowItems(adapter);
	}

	public void removeAllAdapter() {
		for (BaseAdapter adapter : adapters) {
			adapter.unregisterDataSetObserver(observer);
		}
		adapters.clear();
		removeAllViews();
	}

	public void removeAdapter(BaseAdapter adapter) {
		adapter.unregisterDataSetObserver(observer);
		adapters.remove(adapter);
		refreshRowItems();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#showContextMenuForChild(android.view.View)
	 */
	@Override
	public boolean showContextMenuForChild(View originalView) {
		Debug.verbose("cm:" + originalView);
		return super.showContextMenuForChild(originalView);
	}

	private void init() {
		adapters = new ArrayList<BaseAdapter>();
	}

	protected void addRowItems(BaseAdapter adapter) {
		int count = adapter.getCount();

		for (int i = 0; i < count; i++) {
			View view = adapter.getView(i, null, this);
			addView(view);
		}
	}

	protected void refreshRowItems() {

		removeAllViews();

		for (BaseAdapter adapter : adapters) {
			addRowItems(adapter);
		}
	}

}
