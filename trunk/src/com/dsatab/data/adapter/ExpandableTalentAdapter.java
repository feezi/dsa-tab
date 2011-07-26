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
import com.dsatab.activity.BaseMainActivity.EditListener;
import com.dsatab.activity.BaseMainActivity.ProbeListener;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.common.Util;
import com.dsatab.data.BaseCombatTalent;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.Hero;
import com.dsatab.data.Talent;
import com.dsatab.data.TalentGroup;
import com.dsatab.data.TalentGroup.TalentGroupType;
import com.dsatab.view.FilterSettings;

public class ExpandableTalentAdapter extends BaseExpandableListAdapter {

	private List<TalentGroupType> groups;

	private Hero hero;

	private FilterSettings filterSettings;

	private ProbeListener probeListener;
	private EditListener editListener;

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

		Talent talent = getChild(groupPosition, childPosition);
		BaseCombatTalent combatTalent = hero.getCombatTalent(talent.getName());

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
		// value / at
		TextView text4 = (TextView) listItem.findViewById(R.id.talent_list_item_text4);
		// pa
		TextView text5 = (TextView) listItem.findViewById(R.id.talent_list_item_text5);

		text1.setText(talent.getName());

		String be = talent.getBe();
		if (combatTalent != null) {
			be = combatTalent.getBe();
		}

		if (TextUtils.isEmpty(be)) {
			setVisibility(text2, false, text1);
		} else {
			setVisibility(text2, true, text1);
			text2.setText(be);
		}
		text3.setText(talent.getProbe());

		if (combatTalent != null) {

			// make text5 visible
			setVisibility(text5, true, text1);

			if (combatTalent instanceof CombatMeleeTalent) {
				CombatMeleeTalent meleeTalent = (CombatMeleeTalent) combatTalent;

				if (meleeTalent.getAttack() != null || meleeTalent.getAttack().getValue() != null) {
					text4.setText(Integer.toString(meleeTalent.getAttack().getValue()));
					text4.setOnClickListener(probeListener);
					text4.setTag(R.id.TAG_KEY_VALUE, meleeTalent.getAttack());
					text4.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(hero, meleeTalent, true));
					setVisibility(text4, true, text1);
				} else {
					text4.setText("");
					text4.setTag(R.id.TAG_KEY_VALUE, null);
					text4.setTag(R.id.TAG_KEY_PROBE, null);
					text4.setOnClickListener(null);
					setVisibility(text4, false, text1);
				}

				if (meleeTalent.getDefense() != null && meleeTalent.getDefense().getValue() != null) {
					text5.setText(Integer.toString(meleeTalent.getDefense().getValue()));
					text5.setOnClickListener(probeListener);
					text5.setTag(R.id.TAG_KEY_VALUE, meleeTalent.getDefense());
					text5.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(hero, meleeTalent, false));
					setVisibility(text5, true, text1);
				} else {
					text5.setText("");
					text5.setTag(R.id.TAG_KEY_VALUE, null);
					text5.setTag(R.id.TAG_KEY_PROBE, null);
					text5.setOnClickListener(null);
					setVisibility(text5, false, text1);
				}

			} else if (combatTalent instanceof CombatDistanceTalent) {
				CombatDistanceTalent distanceTalent = (CombatDistanceTalent) combatTalent;

				if (distanceTalent.getAttack() != null || distanceTalent.getAttack().getValue() != null) {
					text4.setText(Integer.toString(distanceTalent.getAttack().getValue()));
					text4.setOnClickListener(probeListener);
					text4.setTag(R.id.TAG_KEY_VALUE, distanceTalent.getAttack());
					text4.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(hero, distanceTalent, true));
					setVisibility(text4, true, text1);
				} else {
					text4.setText("");
					text4.setTag(R.id.TAG_KEY_VALUE, null);
					text4.setTag(R.id.TAG_KEY_PROBE, null);
					text4.setOnClickListener(null);
					setVisibility(text4, false, text1);
				}
				text5.setText("");
				text5.setTag(R.id.TAG_KEY_VALUE, null);
				text5.setTag(R.id.TAG_KEY_PROBE, null);
				text5.setOnClickListener(null);
				setVisibility(text5, false, text1);
			}

		} else {
			text4.setText(Util.toString(talent.getValue()));
			text4.setTag(R.id.TAG_KEY_VALUE, null);
			text4.setTag(R.id.TAG_KEY_PROBE, null);
			text4.setOnClickListener(null);
			text4.setClickable(false);
			// hide text5 and expand text1 with its width
			setVisibility(text5, false, text1);

		}
		Util.applyRowStyle(talent, listItem, childPosition);

		listItem.setTag(talent);
		return listItem;
	}

	private void setVisibility(View view, boolean visible, View expander) {
		if (visible && view.getVisibility() != View.VISIBLE) {

			view.setVisibility(View.VISIBLE);
			// weight of text5 is added to text1 if invisible
			((LinearLayout.LayoutParams) expander.getLayoutParams()).weight -= ((LinearLayout.LayoutParams) view
					.getLayoutParams()).weight;
		}

		if (!visible && view.getVisibility() == View.VISIBLE) {
			view.setVisibility(View.GONE);
			// weight of text5 is added to text1 if invisible
			((LinearLayout.LayoutParams) expander.getLayoutParams()).weight += ((LinearLayout.LayoutParams) view
					.getLayoutParams()).weight;
		}
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

	public ProbeListener getProbeListener() {
		return probeListener;
	}

	public void setProbeListener(ProbeListener probeListener) {
		this.probeListener = probeListener;
	}

	public EditListener getEditListener() {
		return editListener;
	}

	public void setEditListener(EditListener editListener) {
		this.editListener = editListener;
	}

}
