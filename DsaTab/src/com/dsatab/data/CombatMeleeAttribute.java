package com.dsatab.data;

import org.w3c.dom.Element;

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
		this.referenceValue = getValue();
		if (Xml.KEY_ATTACKE.equals(element.getNodeName()))
			this.name = ATTACKE;
		else if (Xml.KEY_PARADE.equals(element.getNodeName()))
			this.name = PARADE;
	}

	public String getBe() {
		return talent.getType().getBe();
	}

	public int getMinimum() {
		return 0;
	}

	@Override
	public Integer getErschwernis() {
		return null;
	}

	public int getMaximum() {
		return 25;
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
		if (element.hasAttribute(Xml.KEY_VALUE))
			return Integer.parseInt(element.getAttribute(Xml.KEY_VALUE));
		else
			return null;
	}

	public void setValue(Integer value) {
		if (value != null) {
			element.setAttribute(Xml.KEY_VALUE, value.toString());
		} else {
			element.removeAttribute(Xml.KEY_VALUE);
		}
	}

	public boolean isAttack() {
		return name.equals(ATTACKE);
	}

	public CombatMeleeTalent getTalent() {
		return talent;
	}

}
