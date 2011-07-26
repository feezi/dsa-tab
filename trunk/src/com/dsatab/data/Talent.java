package com.dsatab.data;

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.TalentGroup.TalentGroupType;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Talent implements Probe, Value, Markable {

	public static final String ATHLETIK = "Athletik";

	private Element element;

	private AttributeType[] probes;

	private Hero hero;

	private TalentGroupType type;

	public Talent(Hero hero, Element element) {
		this.element = element;
		this.hero = hero;
	}

	public String getName() {
		return element.getAttributeValue(Xml.KEY_NAME);
	}

	public String getProbe() {
		return element.getAttributeValue(Xml.KEY_PROBE);
	}

	public boolean isFavorite() {
		if (element != null && element.getAttribute(Xml.KEY_FAVORITE) !=null) {
			return Boolean.valueOf(element.getAttributeValue(Xml.KEY_FAVORITE));
		} else {
			return false;
		}
	}

	public TalentGroupType getType() {
		return type;
	}

	public void setType(TalentGroupType type) {
		this.type = type;
	}

	public boolean isUnused() {
		if (element != null && element.getAttribute(Xml.KEY_UNUSED) !=null) {
			return Boolean.valueOf(element.getAttributeValue(Xml.KEY_UNUSED));
		} else {
			return false;
		}
	}

	public void setFavorite(boolean value) {
		if (value)
			element.setAttribute(Xml.KEY_FAVORITE, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_FAVORITE);
	}

	public void setUnused(boolean value) {
		if (value)
			element.setAttribute(Xml.KEY_UNUSED, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_UNUSED);
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

		if (probes != null && probes.length > i && probes[i] != null) {
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
		if (element.getAttribute(Xml.KEY_VALUE) !=null)
			return Integer.parseInt(element.getAttributeValue(Xml.KEY_VALUE));
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
		if (element.getAttribute(Xml.KEY_BE) !=null && Util.isNotBlank(element.getAttributeValue(Xml.KEY_BE)))
			return element.getAttributeValue(Xml.KEY_BE);
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
