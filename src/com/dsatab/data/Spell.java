package com.dsatab.data;

import java.util.Comparator;

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Spell implements Probe, Value, Markable {

	public static final Comparator<Spell> NAME_COMPARATOR = new Comparator<Spell>() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Spell object1, Spell object2) {
			return object1.getName().compareTo(object2.getName());
		}

	};

	private Element element;

	private AttributeType[] probes;

	private Hero hero;

	public Spell(Hero hero, Element element) {
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
		if (element.getAttribute(Xml.KEY_FAVORITE) !=null) {
			return Boolean.valueOf(element.getAttributeValue(Xml.KEY_FAVORITE));
		} else {
			return false;
		}
	}

	public boolean isUnused() {
		if (element.getAttribute(Xml.KEY_UNUSED) !=null) {
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
		return null;
	}

	public Element getElement() {
		return element;
	}

	public String getNotes() {
		return element.getAttributeValue(Xml.KEY_ANMERKUNGEN);
	}

	public String getComments() {
		return element.getAttributeValue(Xml.KEY_ZAUBERKOMMENTAR);
	}

	public boolean isHouseSpell() {
		return Boolean.valueOf(element.getAttributeValue(Xml.KEY_HAUSZAUBER));
	}

	public String getCosts() {
		return element.getAttributeValue(Xml.KEY_KOSTEN);
	}

	public String getRange() {
		return element.getAttributeValue(Xml.KEY_REICHWEITE);
	}

	public String getRepresantation() {
		return element.getAttributeValue(Xml.KEY_REPRESENTATION);
	}

	public String getVariant() {
		return element.getAttributeValue(Xml.KEY_VARIANTE);
	}

	public String getSpellDuration() {
		return element.getAttributeValue(Xml.KEY_WIRKUNGSDAUER);
	}

	public String getCastDuration() {
		return element.getAttributeValue(Xml.KEY_ZAUBERDAUER);
	}

}
