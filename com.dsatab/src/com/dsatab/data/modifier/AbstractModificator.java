package com.dsatab.data.modifier;

import com.dsatab.data.Attribute;
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

	public boolean isActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.Modificator#affects(com.dsatab.data.Probe)
	 */
	@Override
	public final boolean affects(Probe probe) {
		if (probe instanceof Attribute) {
			Attribute attribute = (Attribute) probe;
			return affects(attribute.getType());
		} else {
			return affect(probe);
		}
	}

	protected abstract boolean affect(Probe probe);

	public void setActive(boolean active) {
		this.active = active;
		fireModificatorChanged();
	}

	protected void fireModificatorChanged() {
		if (hero.getModificators().contains(this)) {
			hero.fireModifierChangedEvent(this);
		}
	}

	@Override
	public Modifier getModifier(Probe probe) {
		int modifierValue = getModifierValue(probe);

		if (modifierValue != 0) {
			this.modifier.setModifier(modifierValue);
			this.modifier.setTitle(getModificatorName());
			this.modifier.setDescription(getModificatorInfo());

			return modifier;
		} else {
			return null;
		}
	}

	@Override
	public Modifier getModifier(AttributeType type) {
		int modifierValue = getModifierValue(type);

		if (modifierValue != 0) {
			this.modifier.setModifier(modifierValue);
			this.modifier.setTitle(getModificatorName());
			this.modifier.setDescription(getModificatorInfo());

			return modifier;
		} else {
			return null;
		}
	}

}