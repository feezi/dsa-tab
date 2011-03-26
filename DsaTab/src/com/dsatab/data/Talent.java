package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Talent implements Probe, Value {

	public static final String ATHLETIK = "Athletik";

	private Element element;

	private AttributeType[] probes;

	private Hero hero;

	public Talent(Hero hero, Element element) {
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

		// add leMod again since leModifier values do not count for talent
		// probes.

		if (probes != null && probes[i] != null) {
			int leMod = hero.leModifier.getModifier(probes[i]).getModifier();

			return hero.getModifiedValue(probes[i]) + (-leMod);
		} else
			return null;

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
		if (element.hasAttribute(Xml.KEY_BE) && Util.isNotBlank(element.getAttribute(Xml.KEY_BE)))
			return element.getAttribute(Xml.KEY_BE);
		else
			return null;
	}

	public Element getElement() {
		return element;
	}

	@Override
	public String toString() {
		return getName();
	}

}
