package com.dsatab.data;

import java.util.Comparator;
import java.util.Map;

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Spell extends MarkableElement implements Value, XmlWriteable {

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

	private Hero hero;
	private String name;
	private Integer value;
	private SpellInfo info;

	private boolean begabung;

	public Spell(Hero hero, Element element, Map<String, SpellInfo> spellInfos) {
		super(element);
		this.hero = hero;

		this.probeInfo.applyProbePattern(element.getAttributeValue(Xml.KEY_PROBE));
		this.name = element.getAttributeValue(Xml.KEY_NAME);
		this.value = Util.parseInt(element.getAttributeValue(Xml.KEY_VALUE));
		this.info = spellInfos.get(name);
		if (info == null)
			info = new SpellInfo();
	}

	public String getName() {
		return name;
	}

	public boolean isBegabung() {
		return begabung;
	}

	public void setBegabung(boolean begabung) {
		this.begabung = begabung;
	}

	public SpellInfo getInfo() {
		return info;
	}

	@Override
	public ProbeType getProbeType() {
		return ProbeType.ThreeOfThree;
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
		if (probeInfo.getAttributeTypes() != null) {
			AttributeType type = probeInfo.getAttributeTypes()[i];
			return hero.getModifiedValue(type, false, false);
		} else {
			return null;
		}
	}

	@Override
	public Integer getProbeBonus() {
		return getValue();
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		Integer oldValue = getValue();
		this.value = value;

		if (oldValue != this.value)
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

	public Element getElement() {
		return element;
	}

	public String getSource() {
		return info.getSource();
	}

	public String getComments() {
		return element.getAttributeValue(Xml.KEY_ANMERKUNGEN);
	}

	public String getComplexity() {
		return info.getComplexity();
	}

	public String getTarget() {
		return info.getTargetDetailed();
	}

	public String getMerkmale() {
		return info.getMerkmale();
	}

	public String getEffect() {
		return Util.getValue(element.getAttributeValue(Xml.KEY_ZAUBERKOMMENTAR), info.getEffect());
	}

	public boolean isHouseSpell() {
		return Boolean.valueOf(element.getAttributeValue(Xml.KEY_HAUSZAUBER));
	}

	public String getCosts() {
		return Util.getValue(element.getAttributeValue(Xml.KEY_KOSTEN), info.getCosts());
	}

	public String getRange() {
		return Util.getValue(element.getAttributeValue(Xml.KEY_REICHWEITE), info.getRangeDetailed());
	}

	public String getRepresantation() {
		return Util.getValue(element.getAttributeValue(Xml.KEY_REPRESENTATION), info.getRepresentation());
	}

	public String getVariant() {
		return element.getAttributeValue(Xml.KEY_VARIANTE);
	}

	public String getEffectDuration() {
		return Util.getValue(element.getAttributeValue(Xml.KEY_WIRKUNGSDAUER), info.getEffectDurationDetailed());

	}

	public String getCastDuration() {
		return Util.getValue(element.getAttributeValue(Xml.KEY_ZAUBERDAUER), info.getCastDurationDetailed());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml()
	 */
	@Override
	public void populateXml() {
		if (element != null) {
			if (value != null)
				element.setAttribute(Xml.KEY_VALUE, Integer.toString(value));
			else
				element.removeAttribute(Xml.KEY_VALUE);
		}
	}

}
