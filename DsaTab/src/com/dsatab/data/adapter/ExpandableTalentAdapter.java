package com.dsatab.data.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class ExpandableTalentAdapter extends BaseExpandableListAdapter {

	private List<TalentGroupType> groups;

	private Hero hero;

	public ExpandableTalentAdapter(Hero hero) {
		this.hero = hero;
		groups = new ArrayList<TalentGroupType>(Arrays.asList(TalentGroupType.values()));
		groups.retainAll(hero.getTalentGroups().keySet());
	}

	public Talent getChild(int groupPosition, int childPosition) {
		TalentGroupType groupType = getGroup(groupPosition);

		TalentGroup talentGroup = hero.getTalentGroups().get(groupType);

		if (talentGroup != null && talentGroup.getTalents() != null)
			return talentGroup.getTalents().get(childPosition);
		else
			return null;

	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		TalentGroupType talentType = getGroup(groupPosition);

		TalentGroup talentGroup = hero.getTalentGroups().get(talentType);

		if (talentGroup != null && talentGroup.getTalents() != null)
			return talentGroup.getTalents().size();
		else
			return 0;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		View listItem = null;
		if (convertView instanceof LinearLayout) {
			listItem = convertView;
		} else {
			listItem = LayoutInflater.from(DSATabApplication.getInstance()).inflate(R.layout.talent_list_item, null, false);
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

		if (childPosition % 2 == 1) {
			listItem.setBackgroundResource(R.color.RowOdd);
		} else
			listItem.setBackgroundResource(0);

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
			listItem = (TextView) LayoutInflater.from(DSATabApplication.getInstance()).inflate(R.layout.talent_list_headeritem, null, false);
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
		return true;
	}

}
