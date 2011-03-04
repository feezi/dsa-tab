package com.dsatab.data;

import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;


public interface CombatTalent {

	String getName();

	CombatTalentType getType();

	Probe getAttack();

	Probe getDefense();

	Position getPosition(int w20);
}