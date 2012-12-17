package com.dsatab.data;

import org.jdom2.Element;

import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class CombatMeleeAttribute extends BaseProbe implements Value, XmlWriteable {

	public static final String PARADE = "Parade";

	public static final String ATTACKE = "Attacke";

	private Integer referenceValue, value;

	private CombatMeleeTalent talent;

	private String name;

	private Hero hero;

	public CombatMeleeAttribute(Hero hero) {
		this.hero = hero;
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

	public void setCombatMeleeTalent(CombatMeleeTalent talent) {
		this.talent = talent;
		if (talent.getCombatTalentType() != null) {
			probeInfo.applyBePattern(talent.getCombatTalentType().getBe());
		}
	}

	public int getMinimum() {
		return getBaseValue();
	}

	public int getMaximum() {
		if (talent != null)
			return getBaseValue() + talent.getValue();
		else
			return getBaseValue();
	}

	public String getName() {
		if (talent == null)
			return name;
		else
			return talent.getName() + " - " + name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public ProbeType getProbeType() {
		return ProbeType.TwoOfThree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Value#reset()
	 */
	@Override
	public void reset() {
		setValue(getReferenceValue());
	}

	public Integer getProbeValue(int i) {
		return getValue();
	}

	@Override
	public Integer getProbeBonus() {
		return null;
	}

	public Integer getReferenceValue() {
		if (referenceValue == null)
			this.referenceValue = getValue();
		return referenceValue;
	}

	public Integer getValue() {
		if (value != null)
			return value;
		else {
			// TODO implement Verwandte Talente

			// talent not known MbK S.73 Ableiten von Talenten: At Basis -2, Pa
			// Basis -3
			return getBaseValue() - (isAttack() ? 2 : 3);
		}
	}

	public void setValue(Integer value) {
		if (this.value != value) {
			this.value = value;
			hero.fireValueChangedEvent(this);
		}
	}

	public boolean isAttack() {
		return name.equals(ATTACKE);
	}

	public CombatMeleeTalent getTalent() {
		return talent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml(org.jdom2.Element)
	 */
	@Override
	public void populateXml(Element element) {
		if (value != null) {
			element.setAttribute(Xml.KEY_VALUE, value.toString());
		} else {
			element.removeAttribute(Xml.KEY_VALUE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return getName() + " value=" + getValue() + " range=" + getMinimum() + "-" + getMaximum();
	}

}
