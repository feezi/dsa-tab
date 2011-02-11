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
		this.element = element;
		this.hero = hero;

		type = AttributeType.valueOf(element.getAttribute(Xml.KEY_NAME));

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
		if (element.hasAttribute(Xml.KEY_DSATAB_VALUE)) {
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

			double oldLe = 0;
			if (type == AttributeType.Lebensenergie) {
				oldLe = hero.getLeRatio();
			}

			if (isDSATabValue()) {
				element.setAttribute(Xml.KEY_DSATAB_VALUE, value.toString());
			} else {
				if (element.hasAttribute(Xml.KEY_MOD))
					value -= Integer.parseInt(element.getAttribute(Xml.KEY_MOD));

				element.setAttribute(Xml.KEY_VALUE, Integer.toString(value - getBaseValue()));
			}

			if (type == AttributeType.Lebensenergie) {

				double newLe = hero.getLeRatio();

				if (oldLe >= 0.5 && newLe < 0.5)
					hero.fireModifierAddedEvent(hero.leModifier);
				else if (oldLe < 0.5 && newLe >= 0.5)
					hero.fireModifierRemovedEvent(hero.leModifier);
				else if (oldLe < 0.25 && newLe >= 0.25)
					hero.fireModifierChangedEvent(hero.leModifier);
				else if (oldLe < 0.33 && (newLe >= 0.33 || newLe < 0.25))
					hero.fireModifierChangedEvent(hero.leModifier);
				else if (oldLe < 0.5 && newLe < 0.33)
					hero.fireModifierChangedEvent(hero.leModifier);

			}

		} else
			element.removeAttribute(Xml.KEY_VALUE);
	}

	private boolean isDSATabValue() {
		return type == AttributeType.Lebensenergie || type == AttributeType.Karmaenergie
				|| type == AttributeType.Astralenergie || type == AttributeType.Ausdauer
				|| type == AttributeType.Ausweichen;
	}

	public int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			if (type == AttributeType.Lebensenergie) {
				baseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Konstitution) * 2 + hero
						.getAttributeValue(AttributeType.Körperkraft)) / 2.0);

			} else if (type == AttributeType.Astralenergie) {
				baseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
						+ hero.getAttributeValue(AttributeType.Intuition) + hero
						.getAttributeValue(AttributeType.Charisma)) / 2.0);

			} else if (type == AttributeType.Ausdauer) {
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
			max = hero.getAttribute(AttributeType.Lebensenergie).getReferenceValue();
			break;
		case Astralenergie:
			max = hero.getAttribute(AttributeType.Astralenergie).getReferenceValue();
			break;
		case Ausdauer:
			max = hero.getAttribute(AttributeType.Ausdauer).getReferenceValue();
			break;
		case Karmaenergie:
			max = hero.getAttribute(AttributeType.Karmaenergie).getReferenceValue();
			break;
		case Behinderung:
			max = 10;
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
