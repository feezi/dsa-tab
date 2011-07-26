package com.dsatab.data;

import org.jdom.Element;

import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class CombatMeleeAttribute implements Probe, Value {

	private static final String PARADE = "Parade";

	private static final String ATTACKE = "Attacke";

	private Element element;

	private Integer referenceValue;

	private CombatMeleeTalent talent;

	private String name;

	private Hero hero;

	public CombatMeleeAttribute(Hero hero, CombatMeleeTalent talent, Element element) {
		this.hero = hero;
		this.talent = talent;
		this.element = element;
		if (Xml.KEY_ATTACKE.equals(element.getName()))
			this.name = ATTACKE;
		else if (Xml.KEY_PARADE.equals(element.getName()))
			this.name = PARADE;

		this.referenceValue = getValue();
	}

	public Integer getBaseValue() {
		int base = 0;
		if (isAttack()) {
			base = hero.getAttributeValue(AttributeType.at);
		} else {
			base = hero.getAttributeValue(AttributeType.pa);
		}
		return base;
	}

	public String getBe() {
		return talent.getCombatTalentType().getBe();
	}

	public int getMinimum() {
		return getBaseValue();
	}

	@Override
	public Integer getErschwernis() {
		return null;
	}

	public int getMaximum() {
		return getBaseValue() + talent.getValue();
	}

	public String getName() {
		return talent.getName() + " - " + name;
	}

	@Override
	public ProbeType getProbeType() {
		return ProbeType.TwoOfThree;
	}

	public String getProbe() {
		return null;
	}

	public Integer getProbeValue(int i) {
		return getValue();
	}

	@Override
	public Integer getProbeBonus() {
		return null;
	}

	public Integer getReferenceValue() {
		return referenceValue;
	}

	public Integer getValue() {
		if (element.getAttribute(Xml.KEY_VALUE) != null)
			return Integer.parseInt(element.getAttributeValue(Xml.KEY_VALUE));
		else {
			// TODO implement Verwandte Talente

			// talent not known MbK S.73 Ableiten von Talenten: At Basis -2, Pa
			// Basis -3
			return getBaseValue() - (isAttack() ? 2 : 3);
		}
	}

	public void setValue(Integer value) {
		if (value != null) {
			element.setAttribute(Xml.KEY_VALUE, value.toString());
		} else {
			element.removeAttribute(Xml.KEY_VALUE);
		}

		hero.fireValueChangedEvent(this);
	}

	public boolean isAttack() {
		return name.equals(ATTACKE);
	}

	public CombatMeleeTalent getTalent() {
		return talent;
	}

}
