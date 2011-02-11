package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Debug;
import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.AbstractModifier;
import com.dsatab.xml.Xml;

public class WoundAttribute extends AbstractModifier {

	private Element element;

	public WoundAttribute(Hero hero, Element element) {
		super(hero);
		this.element = element;
	}

	public String getName() {
		return element.getAttribute(Xml.KEY_NAME);
	}

	@Override
	public String getModifierName() {
		return "Wunde " + getPosition().getName() + " x" + getValue();
	}

	@Override
	public String getModifierInfo() {
		String info = null;

		switch (getPosition()) {
		case Head:
			info = "MU,KL,IN,IB -2; INI-2W6";
			break;
		case Stomach:
			info = "KO,KK,GS,IB,AT,PA -1; +1W6 SP";
			break;
		case Chest:
			info = "KO,KK,AT,PA -1; +1W6 SP";
			break;
		case LeftArm:
		case RightArm:
			info = "KK,FF,AT,PA -2";
			break;
		case UpperLeg:
		case LowerLeg:
			info = "GE,IB,AT,PA -2; GS -1";
			break;
		}

		return info;
	}

	public Position getPosition() {
		return Position.valueOf(getName());
	}

	public Integer getValue() {
		if (element.hasAttribute(Xml.KEY_VALUE)) {
			return Util.parseInt(element.getAttribute(Xml.KEY_VALUE));
		} else {
			return null;
		}

	}

	public void setValue(Integer value) {

		Integer oldValue = getValue();

		if (value != null) {
			element.setAttribute(Xml.KEY_VALUE, Integer.toString(value));
		} else
			element.removeAttribute(Xml.KEY_VALUE);

		Integer newValue = getValue();

		if ((oldValue == null || oldValue == 0) && newValue != null && newValue > 0) {
			hero.fireModifierAddedEvent(this);
		} else if (oldValue != null && oldValue > 0 && (newValue == null || newValue == 0)) {
			hero.fireModifierRemovedEvent(this);
		} else {
			hero.fireModifierChangedEvent(this);
		}
	}

	@Override
	public Modifier getModifier(AttributeType type) {

		int modifier = 0;
		switch (getPosition()) {
		case Head:
			if (type == AttributeType.Mut || type == AttributeType.Klugheit || type == AttributeType.Intuition)
				modifier = -2 * getValue();
			break;
		case Stomach:
		case Chest:
			if (type == AttributeType.Konstitution || type == AttributeType.Körperkraft)
				modifier = -1 * getValue();
			break;
		case LeftArm:
		case RightArm:
			if (type == AttributeType.Fingerfertigkeit || type == AttributeType.Körperkraft)
				modifier = -2 * getValue();
			break;
		case UpperLeg:
		case LowerLeg:
			if (type == AttributeType.Gewandtheit || type == AttributeType.Körperkraft)
				modifier = -2 * getValue();
			break;
		}
		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

	@Override
	public Modifier getModifier(Probe probe) {
		int modifier = 0;
		if (probe instanceof CombatDistanceTalent || probe instanceof CombatShieldTalent || probe instanceof CombatMeleeAttribute
				|| probe instanceof CombatProbe) {
			switch (getPosition()) {
			case Head:
				break;
			case Stomach:
			case Chest:
				modifier = -1 * getValue();
				break;
			case LeftArm:
			case RightArm:

				if (probe instanceof CombatProbe) {
					CombatProbe combatProbe = (CombatProbe) probe;

					EquippedItem equippedItem = combatProbe.getEquippedItem();

					if (equippedItem != null && equippedItem.getItem() instanceof Weapon) {
						Weapon w = (Weapon) equippedItem.getItem();

						if (w.isTwoHanded()) {
							modifier = -1 * getValue();
							Debug.verbose("Zweihandwaffen Handwunde AT/PA-1*" + getValue());
							break;
						} else {
							if (getPosition() == Position.LeftArm) {
								Debug.verbose("Angriff/Parade mit Hauptwaffe und Wunde auf linkem Arm ignoriert");
								break;
							}
						}
					}
					if (equippedItem != null && equippedItem.getItem() instanceof Shield) {
						// Shield w = (Shield)
						// combatProbe.getEquippedItem().getItem();
						if (getPosition() == Position.RightArm) {
							Debug.verbose("Angriff/Parade mit Schildwaffe und Wunde auf rechtem Arm ignoriert");
							break;
						}
					}

				}
				Debug.verbose(" Wunde auf Arm AT/PA -2*" + getValue());
				modifier = -2 * getValue();
				break;
			case UpperLeg:
			case LowerLeg:
				modifier = -2 * getValue();
				break;
			}
		}
		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

}
