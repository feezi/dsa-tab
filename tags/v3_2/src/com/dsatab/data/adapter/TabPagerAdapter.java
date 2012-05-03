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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dsatab.HeroConfiguration;
import com.dsatab.R;
import com.dsatab.TabInfo;
import com.dsatab.data.Hero;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.FilterChangedListener;
import com.dsatab.util.Debug;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;

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

public class TabPagerAdapter extends PagerAdapter {

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;

	private ArrayList<DualPaneSavedFragmentState> mSavedState = new ArrayList<DualPaneSavedFragmentState>();
	private ArrayList<DualFragment> mFragments = new ArrayList<DualFragment>();

	private DualFragment mCurrentPrimaryItem = null;

	private HeroConfiguration configuration;
	private Context context;

	private Stack<Integer> freeContainerIds;

	public TabPagerAdapter(Context context, FragmentManager fm, HeroConfiguration configuration) {
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

	public void setConfiguration(HeroConfiguration configuration) {
		Debug.warning("Resetting configuration of tabs this should only be done on orientation change. /New Hero load");
		this.configuration = configuration;
		this.mFragments.clear();
		this.mSavedState.clear();
		mCurrentPrimaryItem = null;
		resetContainerIds();
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

		Debug.warning("Init page " + position + " " + tabInfo);

		LinearLayout tabContainer = new LinearLayout(context);
		LinearLayout leftContainer = new LinearLayout(context);
		LinearLayout rightContainer = new LinearLayout(context);

		int containerId = freeContainerIds.pop();
		tabContainer.setId(containerId);
		tabInfo.setContainerId(containerId);
		// Debug.verbose("Adding to new container " + tabInfo.getContainerId());

		tabContainer.setTag(tabInfo);

		((ViewPager) container).addView(tabContainer);

		DualFragment dualFragment = null;
		if (mFragments.size() > position) {
			dualFragment = mFragments.get(position);

		}

		if (dualFragment == null) {
			dualFragment = new DualFragment();

			int tabs = getTabCount(tabInfo, position);

			for (int i = 0; i < tabs; i++) {

				Fragment fragment = getItem(position, i);

				if (i == 0)
					dualFragment.left = fragment;
				else
					dualFragment.right = fragment;

				// Debug.verbose("Adding item #" + position + ": f=" + fragment
				// + ": tab=" + i);
				mCurTransaction.add(tabInfo.getContainerId(), fragment);
			}

			if (mSavedState.size() > position) {
				DualPaneSavedFragmentState fss = mSavedState.get(position);
				if (fss != null) {
					dualFragment.setInitialSavedState(fss);
				}
			}

			while (mFragments.size() <= position) {
				mFragments.add(null);
			}
			mFragments.set(position, dualFragment);
		} else {
			boolean commit = false;
			if (dualFragment.left != null && dualFragment.left.isAdded()) {
				if (dualFragment.left.getView() != null && dualFragment.left.getView().getParent() != null)
					((ViewGroup) dualFragment.left.getView().getParent()).removeAllViews();
				mCurTransaction.remove(dualFragment.left);
				commit = true;
			}
			if (dualFragment.right != null && dualFragment.right.isAdded()) {
				if (dualFragment.right.getView() != null && dualFragment.right.getView().getParent() != null)
					((ViewGroup) dualFragment.right.getView().getParent()).removeAllViews();
				mCurTransaction.remove(dualFragment.right);
				commit = true;
			}

			if (commit) {
				mCurTransaction.commitAllowingStateLoss();
				mFragmentManager.executePendingTransactions();
				mCurTransaction = mFragmentManager.beginTransaction();
			}

			if (dualFragment.left != null) {
				mCurTransaction.add(tabInfo.getContainerId(), dualFragment.left);
				mCurTransaction.commitAllowingStateLoss();
				mFragmentManager.executePendingTransactions();
				mCurTransaction = mFragmentManager.beginTransaction();
			}
			if (dualFragment.right != null) {
				mCurTransaction.add(tabInfo.getContainerId(), dualFragment.right);
			}
		}
		dualFragment.setMenuVisibility(false);

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

		while (mSavedState.size() <= position) {
			mSavedState.add(null);
		}

		DualPaneSavedFragmentState fragmentState = new DualPaneSavedFragmentState();
		DualFragment dualFragment = mFragments.get(position);

		if (dualFragment.left != null) {
			fragmentState.left = mFragmentManager.saveFragmentInstanceState(dualFragment.left);
		}
		if (dualFragment.right != null) {
			fragmentState.right = mFragmentManager.saveFragmentInstanceState(dualFragment.right);
		}

		mSavedState.set(position, fragmentState);
		mFragments.set(position, null);

		if (dualFragment.left != null) {
			mCurTransaction.remove(dualFragment.left);
		}
		if (dualFragment.right != null)
			mCurTransaction.remove(dualFragment.right);

		freeContainerIds.push(tabInfo.getContainerId());
		tabInfo.setContainerId(-1);
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (position >= mFragments.size()) {
			Debug.warning("setPrimaryItem with invalid index " + position + ", mFragments is " + mFragments);
		} else {
			DualFragment dualFragment = mFragments.get(position);
			if (dualFragment != mCurrentPrimaryItem) {

				Debug.warning("setPrimaryItem " + position + ", fragment is " + dualFragment.left + ","
						+ dualFragment.right);

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
		Bundle state = null;
		if (mSavedState.size() > 0) {
			state = new Bundle();

			Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size() * 2];
			for (int i = 0; i < mSavedState.size(); i++) {
				if (mSavedState.get(i) != null) {
					fss[(i * 2)] = mSavedState.get(i).left;
					fss[(i * 2) + 1] = mSavedState.get(i).right;
				}
			}
			state.putParcelableArray("states", fss);
		}

		for (int i = 0; i < mFragments.size(); i++) {
			DualFragment f = mFragments.get(i);

			if (f != null) {
				if (state == null) {
					state = new Bundle();
				}
				if (f.left != null) {
					String key = "fl" + i;
					mFragmentManager.putFragment(state, key, f.left);
				}

				if (f.right != null) {
					String key = "fr" + i;
					mFragmentManager.putFragment(state, key, f.right);
				}
			}
		}
		return state;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		if (state != null) {
			Bundle bundle = (Bundle) state;
			bundle.setClassLoader(loader);
			Parcelable[] fss = bundle.getParcelableArray("states");
			mSavedState.clear();
			mFragments.clear();
			if (fss != null) {
				for (int i = 0; i < fss.length; i = i + 2) {
					if (fss[i] != null || fss[i + 1] != null) {
						DualPaneSavedFragmentState fragmentState = new DualPaneSavedFragmentState();
						fragmentState.left = (Fragment.SavedState) fss[i];
						fragmentState.right = (Fragment.SavedState) fss[i + 1];

						mSavedState.add(fragmentState);
					} else {
						mSavedState.add(null);
					}
				}
			}

			Iterable<String> keys = bundle.keySet();
			for (String key : keys) {
				if (key.startsWith("f")) {
					int index = Integer.parseInt(key.substring(2));
					Fragment f = mFragmentManager.getFragment(bundle, key);
					if (f != null) {
						while (mFragments.size() <= index) {
							mFragments.add(null);
						}
						f.setMenuVisibility(false);

						DualFragment dualFragment = mFragments.get(index);

						if (dualFragment == null) {
							dualFragment = new DualFragment();
							mFragments.set(index, dualFragment);
						}

						if (key.startsWith("fl"))
							dualFragment.left = f;
						else if (key.startsWith("fr"))
							dualFragment.right = f;

					} else {
						Debug.warning("Bad fragment at key " + key);
					}
				}
			}
		}
	}

	public static class DualFragment implements FilterChangedListener, OnSharedPreferenceChangeListener {
		Fragment left;
		Fragment right;

		void setMenuVisibility(boolean value) {
			if (left != null)
				left.setMenuVisibility(value);

			if (right != null)
				right.setMenuVisibility(value);
		}

		void setUserVisibleHint(boolean value) {
			if (left != null)
				left.setUserVisibleHint(value);
			if (right != null)
				right.setUserVisibleHint(value);
		}

		void setInitialSavedState(DualPaneSavedFragmentState fss) {
			if (left != null)
				left.setInitialSavedState(fss.left);

			if (right != null)
				right.setInitialSavedState(fss.right);
		}

		public Fragment getLeft() {
			return left;
		}

		public Fragment getRight() {
			return right;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.dsatab.fragment.FilterChangedListener#onFilterChanged(com.dsatab
		 * .view.FilterSettings.FilterType, com.dsatab.view.FilterSettings)
		 */
		@Override
		public void onFilterChanged(FilterType type, FilterSettings settings) {
			if (left instanceof BaseFragment)
				((BaseFragment) left).onFilterChanged(type, settings);

			if (right instanceof BaseFragment)
				((BaseFragment) right).onFilterChanged(type, settings);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.SharedPreferences.OnSharedPreferenceChangeListener
		 * #onSharedPreferenceChanged(android.content.SharedPreferences,
		 * java.lang.String)
		 */
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (left instanceof BaseFragment)
				((BaseFragment) left).onSharedPreferenceChanged(sharedPreferences, key);

			if (right instanceof BaseFragment)
				((BaseFragment) right).onSharedPreferenceChanged(sharedPreferences, key);

		}

		public void loadHero(Hero hero) {
			if (left instanceof BaseFragment)
				((BaseFragment) left).loadHero(hero);

			if (right instanceof BaseFragment)
				((BaseFragment) right).loadHero(hero);
		}

		public void unloadHero(Hero hero) {
			if (left instanceof BaseFragment)
				((BaseFragment) left).unloadHero(hero);

			if (right instanceof BaseFragment)
				((BaseFragment) right).unloadHero(hero);
		}

		/**
		 * @param requestCode
		 * @param resultCode
		 * @param data
		 */
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (left != null)
				left.onActivityResult(requestCode, resultCode, data);

			if (right != null)
				right.onActivityResult(requestCode, resultCode, data);

		}

	}

	public static class DualPaneSavedFragmentState {
		Fragment.SavedState left;
		Fragment.SavedState right;
	}
}
