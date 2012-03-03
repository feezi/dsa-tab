package com.dsatab.data;

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Attribute extends BaseProbe implements Value, XmlWriteable {

	/**
	 * 
	 */
	private static final String CONSTANT_BE = "BE";
	/**
	 * 
	 */

	private Element element;

	protected AttributeType type;

	protected Hero hero;

	protected Integer referenceValue;

	protected transient Integer originalBaseValue;
	protected transient Integer currentBaseValue;
	protected transient Integer value;
	protected transient Integer coreValue;
	protected transient String name;

	private boolean lazyInit = false;

	public Attribute(Element element, Hero hero) {
		this(element, null, hero);
	}

	public Attribute(Element element, AttributeType defaultType, Hero hero) {
		this.element = element;
		this.hero = hero;
		this.type = defaultType;
		if (element != null) {
			this.name = element.getAttributeValue(Xml.KEY_NAME);
			if (this.type == null) {
				this.type = AttributeType.valueOf(element.getAttributeValue(Xml.KEY_NAME));
			}
			if (isDSATabValue()) {
				value = Util.parseInt(element.getAttributeValue(Xml.KEY_DSATAB_VALUE));
			}
		}

		if (this.type == AttributeType.Ausweichen)
			probeInfo.setErschwernis(0);

		if (this.type.hasBe())
			probeInfo.applyBePattern(CONSTANT_BE);

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
			return name;
		}

	}

	private void lazyInit() {
		if (lazyInit)
			return;

		if (value == null) {
			value = getCoreValue();
		}

		if (this.type == AttributeType.Astralenergie || this.type == AttributeType.Karmaenergie) {
			if (getCoreValue() == null)
				value = null;
		}
		lazyInit = true;
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
		lazyInit();
		return value;
	}

	private Integer getCoreValue() {
		if (coreValue == null) {

			org.jdom.Attribute valueAttribute = element.getAttribute(Xml.KEY_VALUE);

			if (valueAttribute != null) {
				coreValue = Integer.parseInt(valueAttribute.getValue());
				int mod = 0;

				org.jdom.Attribute modAttribute = element.getAttribute(Xml.KEY_MOD);
				if (modAttribute != null && Util.isNotBlank(modAttribute.getValue()))
					mod = Integer.parseInt(modAttribute.getValue());

				// value and mod of 0 means not able to use it
				if ((type == AttributeType.Karmaenergie || type == AttributeType.Astralenergie) && coreValue == 0
						&& mod == 0) {

					if (type == AttributeType.Astralenergie && hero.hasFeature(Advantage.MAGIEDILLETANT)) {
						coreValue = 0;
					} else {
						coreValue = null;
					}
				} else {
					coreValue += mod;
				}

			} else {
				if (getType() == AttributeType.Ausweichen) {

					coreValue = 0;
					if (hero.hasFeature(SpecialFeature.AUSWEICHEN_1))
						coreValue += 3;
					if (hero.hasFeature(SpecialFeature.AUSWEICHEN_2))
						coreValue += 3;
					if (hero.hasFeature(SpecialFeature.AUSWEICHEN_3))
						coreValue += 3;

					Talent athletik = hero.getTalent(Talent.ATHLETIK);
					if (athletik != null && athletik.getValue() >= 9) {
						coreValue += (athletik.getValue() - 9) / 3;
					}
				}
			}
		}
		if (coreValue != null)
			return coreValue + getBaseValue();
		else
			return null;
	}

	public void populateXml() {
		if (element != null) {
			if (getValue() != null) {
				if (isDSATabValue()) {
					element.setAttribute(Xml.KEY_DSATAB_VALUE, value.toString());
				} else {
					int modValue = value;
					if (element.getAttribute(Xml.KEY_MOD) != null) {
						modValue -= Integer.parseInt(element.getAttributeValue(Xml.KEY_MOD));
					}
					element.setAttribute(Xml.KEY_VALUE, Integer.toString(modValue - getBaseValue()));
				}
			} else {
				element.removeAttribute(Xml.KEY_VALUE);
			}
		}
	}

	public void setValue(Integer value) {
		Integer oldValue = getValue();
		this.value = value;

		if (oldValue != this.value)
			hero.fireValueChangedEvent(this);
	}

	private boolean isDSATabValue() {
		return type == AttributeType.Lebensenergie || type == AttributeType.Karmaenergie
				|| type == AttributeType.Astralenergie || type == AttributeType.Ausdauer
				|| type == AttributeType.Ausweichen;
	}

	/**
	 * Checks wether the base value has changed
	 * 
	 * @return
	 */
	public boolean checkBaseValue() {
		currentBaseValue = null;

		int currentBaseValue = getBaseValue();

		if (currentBaseValue != this.originalBaseValue) {
			this.originalBaseValue = currentBaseValue;
			hero.fireValueChangedEvent(this);
			return true;
		} else {
			return false;
		}
	}

	public int getBaseValue() {

		if (currentBaseValue == null) {
			currentBaseValue = 0;
			if (hero != null) {

				switch (type) {
				case Lebensenergie:
				case Lebensenergie_Total:
					currentBaseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Konstitution) * 2 + hero
							.getAttributeValue(AttributeType.Körperkraft)) / 2.0);
					break;
				case Astralenergie:
				case Astralenergie_Total:
					if (hero.hasFeature(SpecialFeature.GEFAESS_DER_STERNE)) {
						currentBaseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
								+ hero.getAttributeValue(AttributeType.Intuition)
								+ hero.getAttributeValue(AttributeType.Charisma) + hero
								.getAttributeValue(AttributeType.Charisma)) / 2.0);
					} else {
						currentBaseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
								+ hero.getAttributeValue(AttributeType.Intuition) + hero
								.getAttributeValue(AttributeType.Charisma)) / 2.0);
					}
					break;
				case Ausdauer:
				case Ausdauer_Total:
					currentBaseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
							+ hero.getAttributeValue(AttributeType.Konstitution) + hero
							.getAttributeValue(AttributeType.Gewandtheit)) / 2.0);
					break;
				case Magieresistenz:
					currentBaseValue = (int) Math.round((hero.getAttributeValue(AttributeType.Mut)
							+ hero.getAttributeValue(AttributeType.Klugheit) + hero
							.getAttributeValue(AttributeType.Konstitution)) / 5.0);

					// int mrmod = 0;
					// if (hero.getAttribute(AttributeType.Astralenergie) !=
					// null) {
					// Element e =
					// hero.getAttribute(AttributeType.Astralenergie).element;
					// if
					// (!TextUtils.isEmpty(e.getAttributeValue(Xml.KEY_MRMOD)))
					// {
					// mrmod =
					// Util.parseInt(e.getAttributeValue(Xml.KEY_MRMOD));
					// }
					// currentBaseValue += mrmod;
					// }
					break;
				case Ausweichen:
					currentBaseValue = (int) hero.getAttributeValue(AttributeType.pa);
					break;
				}
			}
		}

		if (this.originalBaseValue == null)
			this.originalBaseValue = currentBaseValue;

		return currentBaseValue;
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
			if (referenceValue == null)
				referenceValue = getCoreValue();

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
		case Sozialstatus:
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

	@Override
	public String toString() {
		return getName();
	}
}
