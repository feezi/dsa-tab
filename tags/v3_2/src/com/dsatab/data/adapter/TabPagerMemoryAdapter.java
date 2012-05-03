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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dsatab.HeroConfiguration;
import com.dsatab.R;
import com.dsatab.TabInfo;
import com.dsatab.util.Debug;

/**
 * Implementation of {@link android.support.v4.view.PagerAdapter} that
 * represents each page as a {@link Fragment} that is persistently kept in the
 * fragment manager as long as the user can return to the page.
 * 
 * <p>
 * This version of the pager is best for use when there are a handful of
 * typically more static fragments to be paged through, such as a set of tabs.
 * The fragment of each page the user visits will be kept in memory, though its
 * view hierarchy may be destroyed when not visible. This can result in using a
 * significant amount of memory since fragment instances can hold on to an
 * arbitrary amount of state. For larger sets of pages, consider
 * {@link FragmentStatePagerAdapter}.
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
 * development/samples/Support4Demos/src/com/example/android/supportv4/app/
 * FragmentPagerSupport.java complete}
 * 
 * <p>
 * The <code>R.layout.fragment_pager</code> resource of the top-level fragment
 * is:
 * 
 * {@sample development/samples/Support4Demos/res/layout/fragment_pager.xml
 * complete}
 * 
 * <p>
 * The <code>R.layout.fragment_pager_list</code> resource containing each
 * individual fragment's layout is:
 * 
 * {@sample development/samples/Support4Demos/res/layout/fragment_pager_list.xml
 * complete}
 */

public class TabPagerMemoryAdapter extends PagerAdapter {

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;

	private ArrayList<DualFragment> mFragments = new ArrayList<DualFragment>();
	private ArrayList<String> mFragmentTags = new ArrayList<String>();

	private DualFragment mCurrentPrimaryItem = null;

	private HeroConfiguration configuration;
	private Context context;

	private Stack<Integer> freeContainerIds;

	public TabPagerMemoryAdapter(Context context, FragmentManager fm, HeroConfiguration configuration) {
		this.context = context;
		this.mFragmentManager = fm;
		this.configuration = configuration;

		freeContainerIds = new Stack<Integer>();
		resetContainerIds();
	}

	private void resetContainerIds() {
		freeContainerIds.clear();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.PagerAdapter#getItemPosition(java.lang.Object)
	 */
	@Override
	public int getItemPosition(Object object) {
		TabInfo tabInfo = null;
		if (object instanceof View) {
			tabInfo = (TabInfo) ((View) object).getTag();
		}

		if (configuration != null) {
			final int count = configuration.getTabs().size();

			for (int i = 0; i < count; i++) {
				TabInfo tb = configuration.getTabs().get(i);

				if (tb.equals(tabInfo))
					return i;
			}
		}

		return POSITION_NONE;
	}

	public void onDestroy() {
		mCurrentPrimaryItem = null;
		resetContainerIds();

		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		for (String tag : mFragmentTags) {
			Debug.verbose("CHECKING " + tag + " due to Configuration change");
			Fragment f = mFragmentManager.findFragmentByTag(tag);
			if (f != null) {
				f.setUserVisibleHint(false);
				f.setMenuVisibility(false);
				transaction.remove(f);
				Debug.verbose("DESTROYING " + f + " due to Configuration change");
			}
		}
		transaction.commitAllowingStateLoss();
		mFragmentManager.executePendingTransactions();

		mFragmentTags.clear();
		mFragments.clear();
		configuration = null;

		notifyDataSetChanged();
	}

	public void setConfiguration(HeroConfiguration configuration) {
		Debug.warning("Resetting configuration of tabs this should only be done on orientation change. /New Hero load");
		this.configuration = configuration;
		notifyDataSetChanged();
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
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
	public void startUpdate(ViewGroup container) {

	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// If we already have this item instantiated, there is nothing
		// to do. This can happen when we are restoring the entire pager
		// from its saved state, where the fragment manager has already
		// taken care of restoring the fragments we previously had instantiated.
		// if (mFragments.size() > position) {
		// Fragment f = mFragments.get(position);
		// if (f != null) {
		// return f;
		// }
		// }

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		TabInfo tabInfo = configuration.getTab(position);

		Debug.verbose("Init page " + position + " " + tabInfo);

		while (position >= mFragments.size())
			mFragments.add(null);

		DualFragment dualFragment = mFragments.get(position);
		if (dualFragment == null) {
			dualFragment = new DualFragment();
			mFragments.set(position, dualFragment);
		}

		LinearLayout tabContainer = null;
		for (int i = 0; i < tabInfo.getTabCount(); i++) {
			String fragmentName = tabInfo.getFragmentTagName(i);
			if (!TextUtils.isEmpty(fragmentName)) {
				dualFragment.set(i, mFragmentManager.findFragmentByTag(fragmentName));

				if (dualFragment.get(i) != null) {
					Debug.verbose("Attaching item #" + position + ": f=" + dualFragment.get(i));
					mCurTransaction.attach(dualFragment.get(i));
					if (tabContainer == null)
						tabContainer = (LinearLayout) ((ViewPager) container).findViewById(dualFragment.get(i).getId());
				} else {
					if (tabContainer == null) {
						tabContainer = new LinearLayout(context);
						int containerId = freeContainerIds.pop();
						tabContainer.setId(containerId);
						tabInfo.setContainerId(containerId);
						// Debug.verbose("Adding to new container " +
						// tabInfo.getContainerId());

						tabContainer.setTag(tabInfo);
						((ViewPager) container).addView(tabContainer);
					}
					Fragment f = getItem(position, i);
					if (f != null) {
						f.setMenuVisibility(false);
						f.setUserVisibleHint(false);
					}
					dualFragment.set(i, f);
					Debug.warning("Adding item #" + position + ": f=" + dualFragment.get(i));
					mFragmentTags.add(fragmentName);
					mCurTransaction.add(tabContainer.getId(), dualFragment.get(i), fragmentName);
				}

				mCurTransaction.commitAllowingStateLoss();
				mCurTransaction = mFragmentManager.beginTransaction();
			}

		}

		if (mCurrentPrimaryItem != null) {

			for (Fragment f : dualFragment.fragments) {
				if (!mCurrentPrimaryItem.fragments.contains(f)) {
					f.setMenuVisibility(false);
					f.setUserVisibleHint(false);
				}
			}
		}

		return tabContainer;
	}

	public DualFragment getCurrentFragments() {
		return mCurrentPrimaryItem;
	}

	public List<DualFragment> getDualFragments() {
		return mFragments;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		TabInfo tabInfo = null;
		if (object instanceof View) {
			tabInfo = (TabInfo) ((View) object).getTag();
		}

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		DualFragment fragment = null;

		if (mFragments.size() > position) {
			fragment = mFragments.get(position);
			mFragments.set(position, null);
		}

		if (fragment != null) {
			for (Fragment f : fragment.fragments) {
				if (f != null) {
					Debug.verbose("Detaching item #" + position + ": f=" + f);
					mCurTransaction.detach(f);
				}
			}
		}

		if (tabInfo != null) {
			for (int i = 0; i < tabInfo.getTabCount(); i++) {
				String fragmentName = tabInfo.getFragmentTagName(i);
				Fragment f = mFragmentManager.findFragmentByTag(fragmentName);
				if (f != null) {
					Debug.verbose("Detaching item #" + position + ": f=" + f);
					mCurTransaction.detach(f);
				}
			}
		}

	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (position >= mFragments.size()) {
			Debug.warning("setPrimaryItem with invalid index " + position + ", mFragments is " + mFragments);
		} else {
			DualFragment dualFragment = mFragments.get(position);
			if (dualFragment != mCurrentPrimaryItem) {

				Debug.verbose("setPrimaryItem " + position + ", fragments are " + dualFragment);

				if (mCurrentPrimaryItem != null) {
					mCurrentPrimaryItem.setMenuVisibility(false);
					mCurrentPrimaryItem.setUserVisibleHint(false);
				}
				if (dualFragment != null) {
					dualFragment.setMenuVisibility(true);
					dualFragment.setUserVisibleHint(true);
				}
				mCurrentPrimaryItem = dualFragment;
			}
		}
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
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
