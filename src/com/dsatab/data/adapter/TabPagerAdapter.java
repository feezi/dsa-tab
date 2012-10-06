/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dsatab.data.adapter;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dsatab.HeroConfiguration;
import com.dsatab.TabInfo;
import com.dsatab.util.Debug;
import com.gandulf.guilib.view.adapter.MultiFragmentPagerAdapter;

/**
 * Implementation of {@link android.support.v4.view.PagerAdapter} that uses a
 * {@link Fragment} to manage each page. This class also handles saving and
 * restoring of fragment's state.
 * 
 * <p>
 * This version of the pager is more useful when there are a large number of
 * pages, working more like a list view. When pages are not visible to the user,
 * their entire fragment may be destroyed, only keeping the saved state of that
 * fragment. This allows the pager to hold on to much less memory associated
 * with each visited page as compared to {@link FragmentPagerAdapter} at the
 * cost of potentially more overhead when switching between pages.
 * 
 * <p>
 * When using FragmentPagerAdapter the host ViewPager must have a valid ID set.
 * </p>
 * 
 * <p>
 * Subclasses only need to implement {@link #getItem(int)} and
 * {@link #getCount()} to have a working adapter.
 * 
 * <p>
 * Here is an example implementation of a pager containing fragments of lists:
 * 
 * {@sample
 * development/samples/Support13Demos/src/com/example/android/supportv13/app/
 * FragmentStatePagerSupport.java complete}
 * 
 * <p>
 * The <code>R.layout.fragment_pager</code> resource of the top-level fragment
 * is:
 * 
 * {@sample development/samples/Support13Demos/res/layout/fragment_pager.xml
 * complete}
 * 
 * <p>
 * The <code>R.layout.fragment_pager_list</code> resource containing each
 * individual fragment's layout is:
 * 
 * {@sample
 * development/samples/Support13Demos/res/layout/fragment_pager_list.xml
 * complete}
 */

public class TabPagerAdapter extends MultiFragmentPagerAdapter {

	private Fragment[] mFragments;

	private HeroConfiguration configuration;

	public TabPagerAdapter(Context context, FragmentManager fm, HeroConfiguration configuration) {
		super(context, fm);
		this.configuration = configuration;
		this.mFragments = new Fragment[configuration.getTabs().size() * 2];
	}

	public void setConfiguration(HeroConfiguration configuration) {
		Debug.warning("Resetting configuration of tabs this should only be done on orientation change. /New Hero load");
		this.configuration = configuration;
		this.mFragments = new Fragment[configuration.getTabs().size() * 2];

		notifyDataSetChanged();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.view.adapter.MultiFragmentPagerAdapter#getCount(int)
	 */
	@Override
	public int getCount(int position) {
		TabInfo tabInfo = configuration.getTab(position);
		return tabInfo.getTabCount();
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
	/**
	 * Return the Fragment associated with a specified position.
	 */
	public Fragment getItem(int position, int tab) {

		if (configuration == null)
			return null;

		TabInfo tabInfo = configuration.getTab(position);

		int index = getStartIndex(position) + tab - 1;

		Fragment fragment = mFragments[index];

		if (fragment == null) {
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

				mFragments[index] = fragment;

			} catch (InstantiationException e) {
				Debug.error(e);
			} catch (IllegalAccessException e) {
				Debug.error(e);
			}
		}

		return fragment;
	}

	public DualFragment getCurrentFragments() {
		DualFragment dualFragment = new DualFragment();
		if (mCurrentPrimaryItems != null) {
			if (mCurrentPrimaryItems.size() > 0)
				dualFragment.set(0, mCurrentPrimaryItems.get(0));
			if (mCurrentPrimaryItems.size() > 1)
				dualFragment.set(1, mCurrentPrimaryItems.get(1));
		}
		return dualFragment;
	}

	public List<Fragment> getFragments() {
		return Arrays.asList(mFragments);
	}

	/**
	 * 
	 */
	public void onDestroy() {

	}
}
