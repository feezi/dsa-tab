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

import java.util.Stack;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;

import com.dsatab.HeroConfiguration;
import com.dsatab.R;
import com.dsatab.TabInfo;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class TabPagerAdapter extends PagerAdapter {

	private HeroConfiguration configuration;

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	private Context context;

	private Stack<Integer> freeContainerIds;

	public TabPagerAdapter(Context context, FragmentManager fm, HeroConfiguration configuration) {
		this.context = context;
		mFragmentManager = fm;
		this.configuration = configuration;

		freeContainerIds = new Stack<Integer>();
		freeContainerIds.add(R.id.fragmentContainer0);
		freeContainerIds.add(R.id.fragmentContainer1);
		freeContainerIds.add(R.id.fragmentContainer2);
		freeContainerIds.add(R.id.fragmentContainer3);
		freeContainerIds.add(R.id.fragmentContainer4);
		freeContainerIds.add(R.id.fragmentContainer5);
		freeContainerIds.add(R.id.fragmentContainer6);
		freeContainerIds.add(R.id.fragmentContainer7);
		freeContainerIds.add(R.id.fragmentContainer8);
		freeContainerIds.add(R.id.fragmentContainer9);
		freeContainerIds.add(R.id.fragmentContainer10);
		freeContainerIds.add(R.id.fragmentContainer11);
		freeContainerIds.add(R.id.fragmentContainer12);
		freeContainerIds.add(R.id.fragmentContainer13);
		freeContainerIds.add(R.id.fragmentContainer14);
		freeContainerIds.add(R.id.fragmentContainer15);
	}

	public void setConfiguration(HeroConfiguration configuration) {
		this.configuration = configuration;
		notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.PagerAdapter#getItemPosition(java.lang.Object)
	 */
	@Override
	public int getItemPosition(Object object) {
		LinearLayout layout = (LinearLayout) object;
		TabInfo oldInfo = (TabInfo) layout.getTag();
		if (configuration != null) {
			final int count = configuration.getTabs().size();
			for (int i = 0; i < count; i++) {
				TabInfo info = configuration.getTab(i);

				if (info.equals(oldInfo))
					return i;
			}
		}

		return POSITION_NONE;
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
	private Fragment getItem(int position, int tab) {

		if (configuration == null)
			return null;

		TabInfo tabInfo = configuration.getTab(position);

		Fragment fragment = null;

		try {
			if (tab == 0) {
				if (tabInfo.getPrimaryActivityClazz() != null) {
					fragment = tabInfo.getPrimaryActivityClazz().newInstance();
				} else if (tabInfo.getSecondaryActivityClazz() != null) {
					fragment = tabInfo.getSecondaryActivityClazz().newInstance();
				}
			} else {
				if (tabInfo.getSecondaryActivityClazz() != null) {
					fragment = tabInfo.getSecondaryActivityClazz().newInstance();
				}
			}

		} catch (InstantiationException e) {
			Debug.error(e);
		} catch (IllegalAccessException e) {
			Debug.error(e);
		}
		return fragment;
	}

	private int getTabCount(TabInfo tabInfo, int position) {
		int count = 0;
		if (tabInfo == null && configuration != null)
			tabInfo = configuration.getTab(position);

		if (tabInfo != null) {
			if (tabInfo.getPrimaryActivityClazz() != null)
				count++;

			if (tabInfo.getSecondaryActivityClazz() != null)
				count++;
		}

		return count;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		if (configuration != null && configuration.getTabs() != null)
			return configuration.getTabs().size();
		else
			return 0;
	}

	@Override
	public void startUpdate(View container) {
	}

	@Override
	public Object instantiateItem(View container, int position) {

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		TabInfo tabInfo = configuration.getTab(position);

		Debug.warning("Init page " + position + " " + tabInfo);
		LinearLayout tabContainer = null;
		tabContainer = new LinearLayout(context);
		int containerId = freeContainerIds.pop();
		tabContainer.setId(containerId);
		tabInfo.setContainerId(containerId);
		// Debug.verbose("Adding to new container " + tabInfo.getContainerId());

		tabContainer.setTag(tabInfo);

		((ViewPager) container).addView(tabContainer);

		int tabs = getTabCount(tabInfo, position);
		Fragment fragment = null;

		for (int i = 0; i < tabs; i++) {
			// Do we already have this fragment?
			String name = tabInfo.getFragmentTagName(i);
			fragment = mFragmentManager.findFragmentByTag(name);
			if (fragment != null) {
				// Debug.verbose("Readding item #" + position + ": f=" +
				// fragment + ": tab=" + i);
				mCurTransaction.remove(fragment);
				mCurTransaction.commitAllowingStateLoss();
				mFragmentManager.executePendingTransactions();
				mCurTransaction = mFragmentManager.beginTransaction();
				mCurTransaction.add(tabInfo.getContainerId(), fragment, name);
			} else {
				fragment = getItem(position, i);
				// Debug.verbose("Adding item #" + position + ": f=" + fragment
				// + ": tab=" + i);
				mCurTransaction.add(tabInfo.getContainerId(), fragment, name);
			}

			mCurTransaction.commitAllowingStateLoss();
			mFragmentManager.executePendingTransactions();
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		return tabContainer;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		TabInfo tabInfo = null;
		if (object instanceof View) {
			tabInfo = (TabInfo) ((View) object).getTag();
		}

		// Debug.warning("Destroy page " + position + " " + tabInfo);

		int tabs = getTabCount(tabInfo, position);
		for (int i = 0; i < tabs; i++) {
			String name = tabInfo.getFragmentTagName(i);
			Fragment fragment = mFragmentManager.findFragmentByTag(name);
			if (fragment != null) {
				// Debug.verbose("Removing item #" + position + ": f=" + object
				// + ":tab=" + i + " v=" + object);
				mCurTransaction.remove(fragment);
			}
		}
		freeContainerIds.push(tabInfo.getContainerId());
		tabInfo.setContainerId(-1);
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		// return ((Fragment) object).getView() == view;
		return view.equals(object);
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

}
