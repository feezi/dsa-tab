package com.dsatab.data.modifier;

import com.dsatab.data.Hero;
import com.dsatab.data.Modifier;
import com.dsatab.data.Probe;
import com.dsatab.data.enums.AttributeType;

public abstract class AbstractModificator implements Modificator {

	protected Hero hero;

	protected boolean active;

	protected Modifier modifier;

	public AbstractModificator(Hero hero) {
		this(hero, true);
	}

	public AbstractModificator(Hero hero, boolean active) {
		this.hero = hero;
		this.active = active;
		this.modifier = new Modifier(0, null);
	}

	@Override
	public abstract Modifier getModifier(Probe type);

	@Override
	public abstract Modifier getModifier(AttributeType type);

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		fireModificatorChanged();
	}

	protected void fireModificatorChanged() {
		if (hero.getModificators().contains(this)) {
			hero.fireModifierChangedEvent(this);
		}
	}

}