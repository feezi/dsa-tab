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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.dsatab.R;
import com.dsatab.data.BaseCombatTalent;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.Talent;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.ExpandableTalentAdapter;
import com.dsatab.view.listener.HeroChangedListener;

/**
 * 
 * 
 */
public class TalentFragment extends BaseFragment implements HeroChangedListener {

	private static final String PREF_KEY_GROUP_EXPANDED = "GROUP_EXPANDED";

	private static final String PREF_KEY_SHOW_FAVORITE = "SHOW_FAVORITE_TALENT";
	private static final String PREF_KEY_SHOW_NORMAL = "SHOW_NORMAL_TALENT";
	private static final String PREF_KEY_SHOW_UNUSED = "SHOW_UNUSED_TALENT";

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
		return inflater.inflate(R.layout.sheet_talent, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		talentList = (ExpandableListView) findViewById(R.id.talent_list);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroUnloaded(Hero hero) {

	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Talent || value instanceof CombatMeleeAttribute || value instanceof CombatTalent) {
			talentAdapter.notifyDataSetChanged();
		}
	}

	private void loadHeroTalents(Hero hero2) {

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);

		talentAdapter = new ExpandableTalentAdapter(getHero(), pref.getBoolean(PREF_KEY_SHOW_FAVORITE, true),
				pref.getBoolean(PREF_KEY_SHOW_NORMAL, true), pref.getBoolean(PREF_KEY_SHOW_UNUSED, false));

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

				menu.setHeaderTitle(talent.getName());
				menu.findItem(R.id.option_unmark).setVisible(talent.isFavorite() || talent.isUnused());
				menu.findItem(R.id.option_mark_favorite).setVisible(!talent.isFavorite());
				menu.findItem(R.id.option_mark_unused).setVisible(!talent.isUnused());

				menu.findItem(R.id.option_view_details).setVisible(false);
			}
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onCreateOptionsMenu(android.view
	 * .Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.talent_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onOptionsItemSelected(android.view
	 * .MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.option_filter_talent) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setTitle("Talente filtern");
			builder.setIcon(android.R.drawable.ic_menu_view);
			View content = LayoutInflater.from(getActivity()).inflate(R.layout.popup_filter, null);

			final CheckBox fav = (CheckBox) content.findViewById(R.id.cb_show_favorites);
			final CheckBox normal = (CheckBox) content.findViewById(R.id.cb_show_normal);
			final CheckBox unused = (CheckBox) content.findViewById(R.id.cb_show_unused);

			SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);

			fav.setChecked(pref.getBoolean(PREF_KEY_SHOW_FAVORITE, true));
			normal.setChecked(pref.getBoolean(PREF_KEY_SHOW_NORMAL, true));
			unused.setChecked(pref.getBoolean(PREF_KEY_SHOW_UNUSED, false));

			builder.setView(content);

			DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {

						SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
						Editor edit = pref.edit();

						edit.putBoolean(PREF_KEY_SHOW_FAVORITE, fav.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_NORMAL, normal.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_UNUSED, unused.isChecked());

						edit.commit();

						talentAdapter.setFilter(fav.isChecked(), normal.isChecked(), unused.isChecked());
						talentAdapter.notifyDataSetChanged();
					} else if (which == DialogInterface.BUTTON_NEUTRAL) {
						// do nothing
					}

				}
			};

			builder.setPositiveButton(R.string.label_ok, clickListener);
			builder.setNegativeButton(R.string.label_cancel, clickListener);

			builder.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getMenuInfo() instanceof ExpandableListContextMenuInfo) {

			long packedPosition = ((ExpandableListContextMenuInfo) item.getMenuInfo()).packedPosition;
			int group = ExpandableListView.getPackedPositionGroup(packedPosition);
			int position = ExpandableListView.getPackedPositionChild(packedPosition);

			Talent talent = null;
			if (position >= 0 && group >= 0) {
				talent = talentAdapter.getChild(group, position);
				BaseCombatTalent combatTalent = getHero().getCombatTalent(talent.getName());

				switch (item.getItemId()) {

				case R.id.option_edit_value:
					if (combatTalent != null)
						getBaseActivity().showEditPopup(combatTalent);
					else
						getBaseActivity().showEditPopup(talent);
					return true;
				case R.id.option_mark_favorite:
					talent.setFavorite(true);
					talentAdapter.notifyDataSetChanged();
					return true;
				case R.id.option_mark_unused:
					talent.setUnused(true);
					talentAdapter.notifyDataSetChanged();
					return true;
				case R.id.option_unmark:
					talent.setFavorite(false);
					talent.setUnused(false);
					talentAdapter.notifyDataSetChanged();
					return true;
				}
			}
		}

		return super.onContextItemSelected(item);
	}

}
