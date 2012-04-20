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

import java.util.List;

import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.actionbarsherlock.view.Menu;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.MainActivity;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.MetaTalent;
import com.dsatab.data.Talent;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.ExpandableTalentAdapter;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.FilterDialog;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;
import com.dsatab.view.ListFilterSettings;
import com.dsatab.view.listener.HeroChangedListener;

/**
 * 
 * 
 */
public class TalentFragment extends BaseFragment implements HeroChangedListener {

	private static final String PREF_KEY_GROUP_EXPANDED = "GROUP_EXPANDED";

	private ExpandableListView talentList = null;

	private ExpandableTalentAdapter talentAdapter = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return configureContainerView(inflater.inflate(R.layout.sheet_talent, container, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		talentList = (ExpandableListView) findViewById(R.id.talent_list);
		Drawable drawable = getResources().getDrawable(R.drawable.expandable_group_indicator);
		talentList.setGroupIndicator(drawable);

		talentList
				.setIndicatorBounds(
						0,
						drawable.getIntrinsicWidth()
								+ DSATabApplication.getInstance().getResources()
										.getDimensionPixelSize(R.dimen.expandable_icon_padding));

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {
		loadHeroTalents(hero);
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Attribute) {
			Attribute attribute = (Attribute) value;
			if (attribute.getType() == AttributeType.Behinderung) {
				talentAdapter.notifyDataSetChanged();
			}
		} else if (value instanceof Talent || value instanceof CombatMeleeAttribute || value instanceof CombatTalent) {
			talentAdapter.notifyDataSetChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onModifierAdded(com.dsatab.data.modifier
	 * .Modificator)
	 */
	@Override
	public void onModifierAdded(Modificator value) {
		talentAdapter.notifyDataSetChanged();
		super.onModifierAdded(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onModifierChanged(com.dsatab.data.modifier
	 * .Modificator)
	 */
	@Override
	public void onModifierChanged(Modificator value) {
		talentAdapter.notifyDataSetChanged();
		super.onModifierChanged(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onModifierRemoved(com.dsatab.data.modifier
	 * .Modificator)
	 */
	@Override
	public void onModifierRemoved(Modificator value) {
		talentAdapter.notifyDataSetChanged();
		super.onModifierRemoved(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onModifiersChanged(java.util.List)
	 */
	@Override
	public void onModifiersChanged(List<Modificator> values) {
		talentAdapter.notifyDataSetChanged();
		super.onModifiersChanged(values);
	}

	private void loadHeroTalents(Hero hero2) {

		ListFilterSettings filterSettings = new ListFilterSettings(preferences.getBoolean(
				FilterDialog.PREF_KEY_TALENT_FAVORITE, true), preferences.getBoolean(
				FilterDialog.PREF_KEY_TALENT_NORMAL, true), preferences.getBoolean(FilterDialog.PREF_KEY_TALENT_UNUSED,
				false), preferences.getBoolean(FilterDialog.PREF_KEY_TALENT_MODIFIERS, true));

		talentAdapter = new ExpandableTalentAdapter(getActivity(), getHero(), filterSettings);

		talentAdapter.setProbeListener(getBaseActivity().getProbeListener());
		talentAdapter.setEditListener(getBaseActivity().getEditListener());
		talentList.setAdapter(talentAdapter);
		talentList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Talent talent = talentAdapter.getChild(groupPosition, childPosition);
				getBaseActivity().checkProbe(talent);
				return true;
			}
		});

		talentList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				Editor edit = preferences.edit();
				edit.putBoolean(PREF_KEY_GROUP_EXPANDED + groupPosition, false);
				edit.commit();
			}
		});
		talentList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				Editor edit = preferences.edit();
				edit.putBoolean(PREF_KEY_GROUP_EXPANDED + groupPosition, true);
				edit.commit();
			}
		});

		registerForContextMenu(talentList);

		for (int i = 0; i < talentAdapter.getGroupCount(); i++) {
			if (preferences.getBoolean(PREF_KEY_GROUP_EXPANDED + i, true))
				talentList.expandGroup(i);
			else
				talentList.collapseGroup(i);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (menu.findItem(R.id.option_filter) == null) {
			com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_filter, Menu.NONE, "Filtern");
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setIcon(R.drawable.ic_menu_filter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		if (v == talentList) {

			long packedPosition = ((ExpandableListContextMenuInfo) menuInfo).packedPosition;
			int group = ExpandableListView.getPackedPositionGroup(packedPosition);
			int position = ExpandableListView.getPackedPositionChild(packedPosition);

			if (position >= 0 && group >= 0) {
				MenuInflater inflater = new MenuInflater(getActivity());
				inflater.inflate(R.menu.talent_popupmenu, menu);

				Talent talent = talentAdapter.getChild(group, position);
				if (TextUtils.isEmpty(talent.getTalentSpezialisierung())) {
					menu.setHeaderTitle(talent.getName());
				} else {
					if (talent.getName().equals(talent.getTalentSpezialisierung())) {
						menu.setHeaderTitle(talent.getName() + "*");
					} else {
						menu.setHeaderTitle(talent.getName() + "* (" + talent.getTalentSpezialisierung() + ")");
					}
				}
				menu.findItem(R.id.option_unmark_talent).setVisible(talent.isFavorite() || talent.isUnused());
				menu.findItem(R.id.option_mark_favorite_talent).setVisible(!talent.isFavorite());
				menu.findItem(R.id.option_mark_unused_talent).setVisible(!talent.isUnused());

				if (talent instanceof MetaTalent) {
					menu.findItem(R.id.option_edit_talent).setVisible(false);
				}

				menu.findItem(R.id.option_view_talent).setVisible(false);
			}
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onFilterChanged(com.dsatab.view.
	 * FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {
		if (type == FilterType.Talent && settings instanceof ListFilterSettings) {
			talentAdapter.filter((ListFilterSettings) settings);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getGroupId() == R.id.group_talent && item.getMenuInfo() instanceof ExpandableListContextMenuInfo) {
			ExpandableListContextMenuInfo menuInfo = ((ExpandableListContextMenuInfo) item.getMenuInfo());
			long packedPosition = menuInfo.packedPosition;

			int group = ExpandableListView.getPackedPositionGroup(packedPosition);
			int position = ExpandableListView.getPackedPositionChild(packedPosition);

			View child = menuInfo.targetView;

			switch (item.getItemId()) {

			case R.id.option_edit_talent: {
				Talent talent = getTalent(child, group, position);
				if (talent != null) {
					MainActivity.showEditPopup(getActivity(), talent);
				}
				return true;
			}
			case R.id.option_mark_favorite_talent: {
				Talent talent = getTalent(child, group, position);
				if (talent != null) {
					talent.setFavorite(true);
					talentAdapter.notifyDataSetChanged();
				}
				return true;
			}
			case R.id.option_mark_unused_talent: {
				Talent talent = getTalent(child, group, position);
				if (talent != null) {
					talent.setUnused(true);
					talentAdapter.notifyDataSetChanged();
				}
				return true;
			}
			case R.id.option_unmark_talent: {
				Talent talent = getTalent(child, group, position);
				if (talent != null) {
					talent.setFavorite(false);
					talent.setUnused(false);
					talentAdapter.notifyDataSetChanged();
				}
				return true;
			}
			}

		}

		return super.onContextItemSelected(item);
	}

	private Talent getTalent(View child, int group, int position) {
		Talent talent = null;
		if (child != null && child.getTag() instanceof Talent) {
			talent = (Talent) child.getTag();
		}

		if (talentAdapter != null && talent == null && position >= 0 && group >= 0) {
			talent = talentAdapter.getChild(group, position);
		}

		return talent;
	}

}
