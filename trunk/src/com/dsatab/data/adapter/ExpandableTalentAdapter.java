package com.dsatab.data.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.Talent;
import com.dsatab.data.TalentGroup;
import com.dsatab.data.TalentGroup.TalentGroupType;
import com.dsatab.view.FilterSettings;

public class ExpandableTalentAdapter extends BaseExpandableListAdapter {

	private List<TalentGroupType> groups;

	private Hero hero;

	private FilterSettings filterSettings;

	private Map<TalentGroupType, List<Talent>> groupsMap;

	public ExpandableTalentAdapter(Hero hero, boolean showFavorite, boolean showNormal, boolean showUnused) {
		this.hero = hero;
		this.filterSettings = new FilterSettings(showFavorite, showNormal, showUnused);

		groups = new ArrayList<TalentGroupType>(Arrays.asList(TalentGroupType.values()));
		groups.retainAll(hero.getTalentGroups().keySet());

		groupsMap = new HashMap<TalentGroup.TalentGroupType, List<Talent>>();
	}

	public void setFilter(boolean showFavorite, boolean showNormal, boolean showUnused) {

		boolean hasChanged = !filterSettings.equals(showFavorite, showNormal, showUnused);

		filterSettings.set(showFavorite, showNormal, showUnused);

		if (hasChanged) {
			groupsMap.clear();
		}
	}

	public Talent getChild(int groupPosition, int childPosition) {
		TalentGroupType groupType = getGroup(groupPosition);

		List<Talent> talents = getTalents(groupType);

		if (talents != null)
			return talents.get(childPosition);
		else
			return null;
	}

	private List<Talent> getTalents(TalentGroupType groupType) {
		List<Talent> talents = groupsMap.get(groupType);

		if (talents == null) {
			TalentGroup talentGroup = hero.getTalentGroups().get(groupType);

			if (talentGroup != null && talentGroup.getTalents() != null) {
				talents = filter(talentGroup.getTalents());
			}
		}

		return talents;
	}

	private List<Talent> filter(List<Talent> in) {

		if (filterSettings.isAllVisible()) {
			return in;
		} else {
			List<Talent> result = new ArrayList<Talent>();

			for (Talent t : in) {
				if (filterSettings.isVisible(t)) {
					result.add(t);
				}
			}

			return result;
		}
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		TalentGroupType groupType = getGroup(groupPosition);

		List<Talent> talents = getTalents(groupType);

		if (talents != null)
			return talents.size();
		else
			return 0;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {

		View listItem = null;
		if (convertView instanceof LinearLayout) {
			listItem = convertView;
		} else {
			listItem = LayoutInflater.from(DSATabApplication.getInstance()).inflate(R.layout.talent_list_item, null,
					false);
		}

		// name
		TextView text1 = (TextView) listItem.findViewById(R.id.talent_list_item_text1);
		// be
		TextView text2 = (TextView) listItem.findViewById(R.id.talent_list_item_text2);
		// probe
		TextView text3 = (TextView) listItem.findViewById(R.id.talent_list_item_text3);
		// value
		TextView text4 = (TextView) listItem.findViewById(R.id.talent_list_item_text4);

		Talent talent = getChild(groupPosition, childPosition);

		text1.setText(talent.getName());
		if (TextUtils.isEmpty(talent.getBe())) {
			((LinearLayout.LayoutParams) text1.getLayoutParams()).weight = 0.6f;
			text2.setVisibility(View.GONE);
		} else {
			((LinearLayout.LayoutParams) text1.getLayoutParams()).weight = 0.45f;
			text2.setVisibility(View.VISIBLE);
			text2.setText(talent.getBe());
		}
		text3.setText(talent.getProbe());
		text4.setText(Util.toString(talent.getValue()));

		Util.applyRowStyle(talent, listItem, childPosition);

		listItem.setTag(talent);
		return listItem;
	}

	public TalentGroupType getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	public int getGroupCount() {
		return groups.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		TextView listItem = null;
		if (convertView instanceof TextView) {
			listItem = (TextView) convertView;
		} else {
			listItem = (TextView) LayoutInflater.from(DSATabApplication.getInstance()).inflate(
					R.layout.talent_list_headeritem, null, false);
		}

		TalentGroupType groupType = getGroup(groupPosition);
		if (groupType != null)
			listItem.setText(groupType.name());

		return listItem;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return false;
	}

}
