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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.app.Fragment;

import com.dsatab.data.Hero;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.FilterChangedListener;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;

/**
 * @author Ganymede
 * 
 */
public class DualFragment implements FilterChangedListener, OnSharedPreferenceChangeListener {
	List<Fragment> fragments;

	public DualFragment() {
		fragments = new ArrayList<Fragment>();
	}

	void setMenuVisibility(boolean value) {
		if (fragments != null) {
			for (Fragment left : fragments) {
				left.setMenuVisibility(value);
			}
		}
	}

	void setUserVisibleHint(boolean value) {
		if (fragments != null) {
			for (Fragment left : fragments) {
				left.setUserVisibleHint(value);
			}
		}
	}

	public Fragment get(int i) {
		return fragments.get(i);
	}

	public void set(int i, Fragment f) {
		while (i >= fragments.size())
			fragments.add(null);
		fragments.set(i, f);
	}

	public boolean contains(Class<? extends BaseFragment>... classes) {
		for (Fragment left : fragments) {
			for (Class<? extends BaseFragment> clazz : classes) {
				if (clazz.isInstance(left))
					return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.FilterChangedListener#onFilterChanged(com.dsatab
	 * .view.FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {
		if (fragments != null) {
			for (Fragment left : fragments) {
				if (left instanceof BaseFragment)
					((BaseFragment) left).onFilterChanged(type, settings);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener
	 * #onSharedPreferenceChanged(android.content.SharedPreferences,
	 * java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (fragments != null) {
			for (Fragment left : fragments) {
				if (left instanceof BaseFragment)
					((BaseFragment) left).onSharedPreferenceChanged(sharedPreferences, key);
			}
		}

	}

	public void loadHero(Hero hero) {
		if (fragments != null) {
			for (Fragment left : fragments) {
				if (left instanceof BaseFragment)
					((BaseFragment) left).loadHero(hero);
			}
		}

	}

	public void unloadHero(Hero hero) {
		if (fragments != null) {
			for (Fragment left : fragments) {
				if (left instanceof BaseFragment)
					((BaseFragment) left).unloadHero(hero);
			}
		}

	}

	/**
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (fragments != null) {
			for (Fragment left : fragments) {
				left.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

}
