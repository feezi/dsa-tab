package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.AbstractModifier;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

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
		case Kopf:
			info = "MU,KL,IN,INI -2";
			break;
		case Bauch:
			info = "KO,KK,GS,INI,AT,PA -1; +1W6 SP";
			break;
		case Brust:
			info = "KO,KK,AT,PA -1; +1W6 SP";
			break;
		case LeftLowerArm:
		case RightLowerArm:
			info = "KK,FF,AT,PA -2";
			break;
		case UpperLeg:
		case LowerLeg:
			info = "GE,INI,AT,PA -2; GS -1";
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
		case Kopf:
			if (type == AttributeType.Mut || type == AttributeType.Klugheit || type == AttributeType.Intuition
					|| type == AttributeType.ini || type == AttributeType.Initiative_Aktuell)
				modifier += -2 * getValue();
			break;
		case Bauch:
		case Brust:
			if (type == AttributeType.Konstitution || type == AttributeType.Körperkraft)
				modifier += -1 * getValue();
			break;
		case LeftLowerArm:
		case RightLowerArm:
			if (type == AttributeType.Fingerfertigkeit || type == AttributeType.Körperkraft)
				modifier += -2 * getValue();
			break;
		case UpperLeg:
		case LowerLeg:
			if (type == AttributeType.Gewandtheit || type == AttributeType.ini
					|| type == AttributeType.Initiative_Aktuell)
				modifier += -2 * getValue();
			break;
		}
		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

	@Override
	public Modifier getModifier(Probe probe) {
		int modifier = 0;

		if (probe instanceof Attribute) {
			Attribute attr = (Attribute) probe;
			if (attr.getType() == AttributeType.ini) {
				switch (getPosition()) {
				case Kopf:
					modifier += -2 * getValue();
					break;
				case Bauch:
					modifier += -1 * getValue();
					break;
				case UpperLeg:
				case LowerLeg:
					modifier += -2 * getValue();
					break;
				case Brust:
				case LeftLowerArm:
				case RightLowerArm:
					break;
				}
			}
		}

		if (probe instanceof CombatDistanceTalent || probe instanceof CombatShieldTalent
				|| probe instanceof CombatMeleeAttribute || probe instanceof CombatProbe) {
			switch (getPosition()) {
			case Kopf:
				break;
			case Bauch:
			case Brust:
				modifier += -1 * getValue();
				break;
			case LeftLowerArm:
			case RightLowerArm:

				if (probe instanceof CombatProbe) {
					CombatProbe combatProbe = (CombatProbe) probe;

					EquippedItem equippedItem = combatProbe.getEquippedItem();

					if (equippedItem != null && equippedItem.getItem().hasSpecification(Weapon.class)) {
						Weapon w = (Weapon) equippedItem.getItem().getSpecification(Weapon.class);

						if (w.isTwoHanded()) {
							modifier += -1 * getValue();
							Debug.verbose("Zweihandwaffen Handwunde AT/PA-1*" + getValue());
							break;
						} else {
							if (getPosition() == Position.LeftLowerArm) {
								Debug.verbose("Angriff/Parade mit Hauptwaffe und Wunde auf linkem Arm ignoriert");
								break;
							}
						}
					}
					if (equippedItem != null && equippedItem.getItem().hasSpecification(Shield.class)) {
						// Shield w = (Shield)
						// combatProbe.getEquippedItem().getItem();
						if (getPosition() == Position.RightLowerArm) {
							Debug.verbose("Angriff/Parade mit Schildwaffe und Wunde auf rechtem Arm ignoriert");
							break;
						}
					}

				}
				Debug.verbose(" Wunde auf Arm AT/PA -2*" + getValue());
				modifier += -2 * getValue();
				break;
			case UpperLeg:
			case LowerLeg:
				modifier += -2 * getValue();
				break;
			}
		}
		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

}
