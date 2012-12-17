package com.dsatab.data;

import org.jdom2.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Attribute extends BaseProbe implements Value, XmlWriteable, Cloneable {

	/**
	 * 
	 */
	private static final String CONSTANT_BE = "BE";

	protected AttributeType type;

	protected Hero hero;

	protected Integer referenceValue;

	protected Integer originalBaseValue;
	protected Integer currentBaseValue;
	protected Integer value;
	protected Integer mod;
	protected Integer coreValue;
	protected String name;

	private boolean lazyInit = false;

	public Attribute(Hero hero) {
		this.hero = hero;
	}

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
		if (this.type != null) {
			if (this.type == AttributeType.Ausweichen) {
				probeInfo.setErschwernis(0);
			}
			if (this.type.hasBe()) {
				probeInfo.applyBePattern(CONSTANT_BE);
			}
		}
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

	public void setName(String name) {
		this.name = name;
	}

	private void lazyInit() {
		if (lazyInit)
			return;

		if (value != null && mod != null) {

			// value and mod of 0 means not able to use it
			if ((type == AttributeType.Karmaenergie || type == AttributeType.Astralenergie
					|| type == AttributeType.Karmaenergie_Total || type == AttributeType.Astralenergie_Total)
					&& value == 0 && mod == 0) {

				if ((type == AttributeType.Astralenergie || type == AttributeType.Astralenergie_Total)
						&& hero.hasFeature(Advantage.MAGIEDILLETANT)) {
					value = 0;
				} else {
					value = null;
				}
			} else {
				value += mod;
			}
		}

		if (value != null)
			value += getBaseValue();
		if (getReferenceValue() == null)
			setReferenceValue(value);

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
		if (!lazyInit)
			lazyInit();
		return value;
	}

	public Integer getMod() {
		return mod;
	}

	public void setMod(Integer mod) {
		this.mod = mod;
	}

	public void populateXml(Element element) {
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
		if (!Util.equalsOrNull(this.value, value)) {
			this.value = value;
			hero.fireValueChangedEvent(this);
		}
	}

	public boolean isDSATabValue() {
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

	public boolean checkValue() {
		boolean changed = false;
		Integer value = getValue();
		if (value != null) {
			int max = getMaximum();
			int min = getMinimum();
			if (value > max) {
				setValue(max);
				changed = true;
			} else if (value < min) {
				setValue(min);
				changed = true;
			}
		}
		return changed;
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
				default:
					// do nothing
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Attribute clone() {
		try {
			return (Attribute) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}