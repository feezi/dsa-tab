package com.dsatab.data.modifier;

import com.dsatab.data.Hero;
import com.dsatab.data.Modifier;
import com.dsatab.data.Probe;
import com.dsatab.data.enums.AttributeType;

public abstract class AbstractModifier implements Modificator {

	protected Hero hero;

	public AbstractModifier(Hero hero) {
		this.hero = hero;
	}

	@Override
	public abstract Modifier getModifier(Probe type);

	@Override
	public abstract Modifier getModifier(AttributeType type);

}
