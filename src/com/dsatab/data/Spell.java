package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Spell implements Probe, Value {

	private Element element;

	private AttributeType[] probes;

	private Hero hero;

	public Spell(Hero hero, Element element) {
		this.element = element;
		this.hero = hero;
	}

	public String getName() {
		return element.getAttribute(Xml.KEY_NAME);
	}

	public String getProbe() {
		return element.getAttribute(Xml.KEY_PROBE);
	}

	@Override
	public Integer getErschwernis() {
		return null;
	}

	@Override
	public ProbeType getProbeType() {
		return ProbeType.ThreeOfThree;
	}

	public Integer getProbeValue(int i) {
		if (probes == null) {
			probes = Util.splitProbeString(getProbe());
		}

		if (probes != null && probes.length > i && probes[i] != null) {
			// add leMod again since leModifier values do not count for talent
			// probes.
			int leMod = hero.leModifier.getModifier(probes[i]).getModifier();

			return hero.getModifiedValue(probes[i]) + (-leMod);
		} else {
			return null;
		}
	}

	@Override
	public Integer getProbeBonus() {
		return getValue();
	}

	public Integer getValue() {
		if (element.hasAttribute(Xml.KEY_VALUE))
			return Integer.parseInt(element.getAttribute(Xml.KEY_VALUE));
		else
			return null;
	}

	public void setValue(Integer value) {
		if (value != null)
			element.setAttribute(Xml.KEY_VALUE, Integer.toString(value));
		else
			element.removeAttribute(Xml.KEY_VALUE);

		hero.fireValueChangedEvent(this);
	}

	public Integer getReferenceValue() {
		return getValue();
	}

	public int getMinimum() {
		return 0;
	}

	public int getMaximum() {
		return 25;
	}

	public String getBe() {
		return null;
	}

	public Element getElement() {
		return element;
	}

	public String getNotes() {
		return element.getAttribute(Xml.KEY_ANMERKUNGEN);
	}

	public String getComments() {
		return element.getAttribute(Xml.KEY_ZAUBERKOMMENTAR);
	}

	public boolean isHouseSpell() {
		return Boolean.valueOf(element.getAttribute(Xml.KEY_HAUSZAUBER));
	}

	public String getCosts() {
		return element.getAttribute(Xml.KEY_KOSTEN);
	}

	public String getRange() {
		return element.getAttribute(Xml.KEY_REICHWEITE);
	}

	public String getRepresantation() {
		return element.getAttribute(Xml.KEY_REPRESENTATION);
	}

	public String getVariant() {
		return element.getAttribute(Xml.KEY_VARIANTE);
	}

	public String getSpellDuration() {
		return element.getAttribute(Xml.KEY_WIRKUNGSDAUER);
	}

	public String getCastDuration() {
		return element.getAttribute(Xml.KEY_ZAUBERDAUER);
	}

}
