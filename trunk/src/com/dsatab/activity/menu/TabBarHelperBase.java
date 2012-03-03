/*
 * Copyright 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dsatab.activity.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.dsatab.HeroConfiguration;
import com.dsatab.TabInfo;
import com.dsatab.activity.MainActivity;
import com.dsatab.data.Hero;
import com.gandulf.guilib.R;

/**
 * A class that implements the action bar pattern for pre-Honeycomb devices.
 */
public class TabBarHelperBase {

	protected Set<Integer> mActionItemIds = new HashSet<Integer>();

	private LinearLayout tabLayout;

	private MainActivity mActivity;

	public TabBarHelperBase(MainActivity activity) {

	}

	/** {@inheritDoc} */

	public void onCreate(Bundle savedInstanceState) {

	}

	/** {@inheritDoc} */

	public void onPostCreate(Bundle savedInstanceState) {
		Configuration configuration = mActivity.getResources().getConfiguration();

		if (mActivity.findViewById(R.id.inc_tabs) != null) {
			if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				BitmapDrawable TileMe = new BitmapDrawable(BitmapFactory.decodeResource(mActivity.getResources(),
						R.drawable.bg_tab_land));
				TileMe.setTileModeX(Shader.TileMode.MIRROR);
				TileMe.setTileModeY(Shader.TileMode.MIRROR);
				mActivity.findViewById(R.id.inc_tabs).setBackgroundDrawable(TileMe);
			} else {
				BitmapDrawable TileMe = new BitmapDrawable(BitmapFactory.decodeResource(mActivity.getResources(),
						R.drawable.bg_tab_nonland));
				TileMe.setTileModeX(Shader.TileMode.MIRROR);
				TileMe.setTileModeY(Shader.TileMode.MIRROR);
				mActivity.findViewById(R.id.inc_tabs).setBackgroundDrawable(TileMe);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.menu.TabBarHelper#setNavigationTabSelected(int,
	 * boolean)
	 */

	public void setNavigationTabSelected(int index, boolean selected) {
		tabLayout.getChildAt(index).setSelected(selected);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.menu.TabBarHelper#setNavigationTabsEnabled(boolean)
	 */

	public void setNavigationTabsEnabled(boolean enabled) {
		if (tabLayout != null) {
			int count = tabLayout.getChildCount();
			for (int i = 0; i < count; i++) {
				tabLayout.getChildAt(i).setEnabled(enabled);
			}
		}
	}

	/**
	 * Action bar helper code to be run in
	 * {@link Activity#onCreateOptionsMenu(android.view.Menu)}.
	 * 
	 * NOTE: This code will mark on-screen menu items as invisible.
	 */

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = mActivity.getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		Hero hero = mActivity.getHero();
		menu.findItem(R.id.option_save_hero).setEnabled(hero != null);
		return true;
	}

	private void updateTab(ImageButton tabButton, TabInfo tabInfo) {
		tabButton.setOnClickListener(mActivity);
		tabButton.setTag(tabInfo);
		tabButton.setImageResource(tabInfo.getTabResourceId());
		mActivity.registerForIconContextMenu(tabButton);
	}

	private ImageButton createTab(LayoutInflater inflater, TabInfo tabInfo) {
		ImageButton tabButton = (ImageButton) inflater.inflate(R.layout.hero_tab, tabLayout, false);
		updateTab(tabButton, tabInfo);
		return tabButton;
	}

	/**
	 * 
	 */
	public void setupTabs(HeroConfiguration configuration) {

		if (configuration == null) {
			tabLayout.removeAllViews();
			return;
		}

		LayoutInflater inflater = LayoutInflater.from(mActivity);

		List<TabInfo> tabs = configuration.getTabs();
		int tabCount = tabs.size();

		for (int i = 0; i < tabCount; i++) {
			ImageButton tabButton = (ImageButton) tabLayout.getChildAt(i);
			TabInfo tabInfo = tabs.get(i);

			if (tabButton != null) {
				updateTab(tabButton, tabInfo);
			} else {
				tabButton = createTab(inflater, tabInfo);
				tabLayout.addView(tabButton, i);
			}
		}

		// remove tabs if there are too much
		for (int i = tabLayout.getChildCount() - 1; i >= tabCount; i--) {
			tabLayout.removeViewAt(i);
		}

		return;

	}
}
