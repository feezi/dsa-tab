package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Attribute implements Probe, Value {

	/**
	 * 
	 */
	private static final String CONSTANT_BE = "BE";
	/**
	 * 
	 */

	private Element element;

	private AttributeType type;

	private Hero hero;

	private Integer referenceValue;

	private Integer erschwernis;

	public Attribute(Element element) {
		this(element, null);
	}

	public Attribute(Element element, Hero hero) {
		this(element, null, hero);
	}

	public Attribute(Element element, AttributeType type, Hero hero) {
		this.element = element;
		this.hero = hero;

		if (type == null)
			type = AttributeType.valueOf(element.getAttribute(Xml.KEY_NAME));

		this.type = type;

		referenceValue = getCoreValue();

		if (type == AttributeType.Ausweichen)
			erschwernis = 0;
	}

	public AttributeType getType() {
		return type;
	}

	public Hero getHero() {
		return hero;
	}

	public String getName() {
		switch (type) {
		case pa:
			return "Parade Basiswert";
		case at:
			return "Attacke Basiswert";
		case fk:
			return "Fernkampf Basiswert";
		case ini:
			return "Initiative";
		case Lebensenergie:
			return "Lebensenergie aktuell";
		case Ausdauer:
			return "Ausdauer aktuell";
		case Astralenergie:
			return "Astralenergie aktuell";
		case Karmaenergie:
			return "Karmaenergie aktuell";
		default:
			return element.getAttribute(Xml.KEY_NAME);
		}

	}

	public String getProbe() {
		return null;
	}

	public ProbeType getProbeType() {
		return ProbeType.TwoOfThree;
	}

	@Override
	public Integer getProbeBonus() {
		return null;
	}

	public Integer getProbeValue(int i) {
		return hero.getAttributeValue(type);
	}

	public Integer getValue() {
		if (isDSATabValue() && element.hasAttribute(Xml.KEY_DSATAB_VALUE)) {
			return Integer.parseInt(element.getAttribute(Xml.KEY_DSATAB_VALUE));
		} else {
			return getCoreValue();
		}

	}

	private Integer getCoreValue() {
		Integer value = null;

		if (element.hasAttribute(Xml.KEY_VALUE)) {
			value = Integer.parseInt(element.getAttribute(Xml.KEY_VALUE));
			int mod = 0;
			if (element.hasAttribute(Xml.KEY_MOD) && Util.isNotBlank(element.getAttribute(Xml.KEY_MOD)))
				mod = Integer.parseInt(element.getAttribute(Xml.KEY_MOD));

			// value and mod of 0 means not able to use it
			if ((type == AttributeType.Karmaenergie || type == AttributeType.Astralenergie) && value == 0 && mod == 0)
				return null;

			value += mod;

			return value + getBaseValue();
		} else {
			if (getType() == AttributeType.Ausweichen) {

				value = 0;
				if (hero.hasFeature(SpecialFeature.AUSWEICHEN_1))
					value += 3;
				if (hero.hasFeature(SpecialFeature.AUSWEICHEN_2))
					value += 3;
				if (hero.hasFeature(SpecialFeature.AUSWEICHEN_3))
					value += 3;

				Talent athletik = hero.getTalent(Talent.ATHLETIK);
				if (athletik.getValue() >= 9) {
					value += (athletik.getValue() - 9) / 3;
				}

				return value + getBaseValue();
			}

		}
		return null;
	}

	public void setValue(Integer value) {
		if (value != null) {

			if (isDSATabValue()) {
				element.setAttribute(Xml.KEY_DSATAB_VALUE, value.toString());
			} else {
				if (element.hasAttribute(Xml.KEY_MOD))
					value -= Integer.parseInt(element.getAttribute(Xml.KEY_MOD));

				element.setAttribute(Xml.KEY_VALUE, Integer.toString(value - getBaseValue()));
			}

		} else
			element.removeAttribute(Xml.KEY_VALUE);

		hero.fireValueChangedEvent(this);
	}

	private boolean isDSATabValue() {
		return type == AttributeType.Lebensenergie || type == AttributeType.Karmaenergie
				|| type == AttributeType.Astralenergie || type == AttributeType.Ausdauer
				|| type == AttributeType.Ausweichen;
	}

	public int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			if (type == AttributeType.Lebensenergie || type == AttributeType.Lebensenergie_Total) {
				baseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Konstitution) * 2 + hero
						.getAttributeValue(AttributeType.Körperkraft)) / 2.0);

			} else if (type == AttributeType.Astralenergie || type == AttributeType.Astralenergie_Total) {

				if (hero.hasFeature(SpecialFeature.GEFAESS_DER_STERNE)) {
					baseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
							+ hero.getAttributeValue(AttributeType.Intuition)
							+ hero.getAttributeValue(AttributeType.Charisma) + hero
							.getAttributeValue(AttributeType.Charisma)) / 2.0);
				} else {
					baseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
							+ hero.getAttributeValue(AttributeType.Intuition) + hero
							.getAttributeValue(AttributeType.Charisma)) / 2.0);
				}

			} else if (type == AttributeType.Ausdauer || type == AttributeType.Ausdauer_Total) {
				baseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
						+ hero.getAttributeValue(AttributeType.Konstitution) + hero
						.getAttributeValue(AttributeType.Gewandtheit)) / 2.0);

			} else if (type == AttributeType.Magieresistenz) {
				baseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
						+ hero.getAttributeValue(AttributeType.Klugheit) + hero
						.getAttributeValue(AttributeType.Konstitution)) / 5.0);
			} else if (type == AttributeType.Ausweichen) {
				baseValue = (int) hero.getAttributeValue(AttributeType.pa);
			}
		}

		return baseValue;
	}

	public void setReferenceValue(Integer referenceValue) {
		this.referenceValue = referenceValue;
	}

	public Integer getReferenceValue() {

		switch (type) {
		case at: {
			int mu = hero.getAttributeValue(AttributeType.Mut);
			int ge = hero.getAttributeValue(AttributeType.Gewandtheit);
			int kk = hero.getAttributeValue(AttributeType.Körperkraft);
			return (int) Math.round((mu + ge + kk) / 5.0);
		}
		case pa: {
			int in = hero.getAttributeValue(AttributeType.Intuition);
			int ge = hero.getAttributeValue(AttributeType.Gewandtheit);
			int kk = hero.getAttributeValue(AttributeType.Körperkraft);
			return (int) Math.round((in + ge + kk) / 5.0);
		}
		case fk: {
			int in = hero.getAttributeValue(AttributeType.Intuition);
			int ff = hero.getAttributeValue(AttributeType.Fingerfertigkeit);
			int kk = hero.getAttributeValue(AttributeType.Körperkraft);
			return (int) Math.round((in + ff + kk) / 5.0);
		}
		case ini: {
			int mu = hero.getAttributeValue(AttributeType.Mut);
			int in = hero.getAttributeValue(AttributeType.Intuition);
			int ge = hero.getAttributeValue(AttributeType.Gewandtheit);
			return (int) Math.round((mu + mu + in + ge) / 5.0);
		}

		case Ausdauer_Total:
		case Karmaenergie_Total:
		case Astralenergie_Total:
		case Lebensenergie_Total:
			return null;
		case Behinderung:
			return hero.getArmorBe();
		default:
			return referenceValue;
		}
	}

	public int getMinimum() {
		switch (type) {
		case Lebensenergie:
			return -10;
		default:
			return 0;
		}
	}

	public int getMaximum() {
		int max = 0;

		switch (type) {
		case Lebensenergie:
			max = hero.getAttributeValue(AttributeType.Lebensenergie_Total);
			break;
		case Astralenergie:
			max = hero.getAttributeValue(AttributeType.Astralenergie_Total);
			break;
		case Ausdauer:
			max = hero.getAttributeValue(AttributeType.Ausdauer_Total);
			break;
		case Karmaenergie:
			max = hero.getAttributeValue(AttributeType.Karmaenergie_Total);
			break;
		case Behinderung:
			max = 15;
			break;
		case Mut:
		case Klugheit:
		case Intuition:
		case Charisma:
		case Fingerfertigkeit:
		case Gewandtheit:
		case Konstitution:
		case Körperkraft:
			max = 25;
			break;
		case Lebensenergie_Total:
		case Astralenergie_Total:
		case Ausdauer_Total:
		case Karmaenergie_Total:
			max = 200;
			break;
		default:
			max = 99;
			break;
		}

		return max;
	}

	public String getBe() {
		if (type.hasBe())
			return CONSTANT_BE;
		else
			return null;
	}

	public Integer getErschwernis() {
		return erschwernis;
	}

	public void setErschwernis(Integer erschwernis) {
		this.erschwernis = erschwernis;
	}

	@Override
	public String toString() {
		return getName();
	}
}
