package com.dsatab.data;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import com.dsatab.data.Talent.Flags;
import com.dsatab.data.enums.TalentGroupType;

public class TalentGroup {

	private List<Talent> talents = new LinkedList<Talent>();

	private TalentGroupType type;

	private EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

	public TalentGroup(TalentGroupType name) {
		this.type = name;
	}

	public void setTalents(List<Talent> talents) {
		this.talents = talents;
	}

	public boolean hasFlag(Flags flag) {
		return flags.contains(flag);
	}

	public void addFlag(Flags flag) {
		flags.add(flag);
	}

	public List<Talent> getTalents() {
		return talents;
	}

	public TalentGroupType getType() {
		return type;
	}

}
