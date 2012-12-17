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
package com.dsatab.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.dsatab.R;
import com.dsatab.common.Util;

/**
 * @author Ganymede
 * 
 */
public abstract class BaseListFragment extends BaseFragment implements OnItemLongClickListener, OnItemClickListener {

	protected ActionMode mMode;

	protected ActionMode.Callback mCallback;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		if (mMode != null) {
			SparseBooleanArray checked = ((ListView) parent).getCheckedItemPositions();
			boolean hasCheckedElement = false;
			for (int i = 0; i < checked.size() && !hasCheckedElement; i++) {
				hasCheckedElement = checked.valueAt(i);
			}

			if (hasCheckedElement) {
				mMode.invalidate();
			} else {
				mMode.finish();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android
	 * .widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mCallback == null) {
			throw new IllegalArgumentException("ListView with Contextual Action Bar needs mCallback to be defined!");
		}
		((ListView) parent).setItemChecked(position, !((ListView) parent).isItemChecked(position));

		List<Object> checkedObjects = new ArrayList<Object>();

		SparseBooleanArray checked = ((ListView) parent).getCheckedItemPositions();
		boolean hasCheckedElement = false;
		if (checked != null) {
			for (int i = 0; i < checked.size() && !hasCheckedElement; i++) {
				hasCheckedElement = checked.valueAt(i);
				checkedObjects.add(parent.getItemAtPosition(checked.keyAt(i)));
			}
		}

		if (hasCheckedElement) {
			if (mMode == null) {
				Callback callback = getActionModeCallback(checkedObjects);
				if (callback != null) {
					mMode = ((SherlockFragmentActivity) getActivity()).startActionMode(callback);
					customizeActionModeCloseButton();
					mMode.invalidate();
				} else {
					return false;
				}
			} else {
				mMode.invalidate();
			}
		} else {
			if (mMode != null) {
				mMode.finish();
			}
		}
		return true;
	}

	protected Callback getActionModeCallback(List<Object> objects) {
		return mCallback;
	}

	// little hack to style actionmode done button
	private void customizeActionModeCloseButton() {
		int buttonId = Resources.getSystem().getIdentifier("action_mode_close_button", "id", "android");
		View v = getActivity().findViewById(buttonId);
		if (v == null) {
			buttonId = R.id.abs__action_mode_close_button;
			v = getActivity().findViewById(buttonId);
		}
		if (v == null)
			return;
		LinearLayout ll = (LinearLayout) v;
		if (ll.getChildCount() > 1 && ll.getChildAt(1) != null) {
			TextView tv = (TextView) ll.getChildAt(1);
			tv.setTextColor(getResources().getColor(android.R.color.white));
			tv.setBackgroundResource(Util.getThemeResourceId(getActivity(), R.attr.actionBarItemBackground));
		}
	}
}
