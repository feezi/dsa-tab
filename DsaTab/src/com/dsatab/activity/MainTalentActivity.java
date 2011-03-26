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

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.dsatab.R;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Talent;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.ExpandableTalentAdapter;

/**
 * @author Ganymede
 * 
 */
public class MainTalentActivity extends BaseMainActivity {

	private static final String PREF_KEY_GROUP_EXPANDED = "GROUP_EXPANDED";

	private ExpandableListView talentList = null;

	private ExpandableTalentAdapter talentAdapter = null;

	private View talentAttributeList;

	/**
	 * 
	 */
	public MainTalentActivity() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_talent);
		super.onCreate(savedInstanceState);

		talentList = (ExpandableListView) findViewById(R.id.talent_list);

		talentAttributeList = findViewById(R.id.inc_talent_attributes_list);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		super.onHeroLoaded(hero);

		loadHeroTalents(hero);
		fillAttributesList(talentAttributeList);

		hero.addValueChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroUnloaded(Hero hero) {
		hero.removeValueChangeListener(this);
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Konstitution:
			case Charisma:
				fillAttribute(talentAttributeList, attr);
				break;
			}

		}

		if (value instanceof Talent) {
			talentAdapter.notifyDataSetChanged();
		}
	}

	private void loadHeroTalents(Hero hero2) {

		talentAdapter = new ExpandableTalentAdapter(getHero());

		talentList.setAdapter(talentAdapter);
		talentList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Talent talent = talentAdapter.getChild(groupPosition, childPosition);
				checkProbe(talent);
				return false;
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

		talentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (view.getTag() instanceof Talent) {
					Talent talent = (Talent) view.getTag();
					showEditPopup(talent);
				}
				return false;
			}
		});

		for (int i = 0; i < talentAdapter.getGroupCount(); i++) {
			if (preferences.getBoolean(PREF_KEY_GROUP_EXPANDED + i, true))
				talentList.expandGroup(i);
			else
				talentList.collapseGroup(i);
		}

	}

}
