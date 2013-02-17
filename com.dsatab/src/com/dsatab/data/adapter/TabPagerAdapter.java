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
package com.dsatab.data.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.dsatab.data.HeroConfiguration;
import com.dsatab.fragment.DualPaneFragment;

/**
 * @author Ganymede
 * 
 */
public class TabPagerAdapter extends FragmentPagerAdapter {

	private HeroConfiguration configuration;

	private Map<Integer, DualPaneFragment> mPageReferenceMap = new HashMap<Integer, DualPaneFragment>();

	/**
	 * 
	 */
	public TabPagerAdapter(FragmentManager fm, HeroConfiguration configuration) {
		super(fm);
		this.configuration = configuration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentStatePagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int pos) {
		DualPaneFragment f = mPageReferenceMap.get(pos);
		if (f == null) {
			f = new DualPaneFragment(configuration.getTab(pos));
			mPageReferenceMap.put(pos, f);
		}
		return f;
	}

	public DualPaneFragment getFragment(int key) {
		return mPageReferenceMap.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.FragmentStatePagerAdapter#destroyItem(android.
	 * view.ViewGroup, int, java.lang.Object)
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return configuration.getTabs().size();
	}

	/**
	 * @return
	 */
	public Collection<DualPaneFragment> getFragments() {
		return mPageReferenceMap.values();
	}

}
