package com.dsatab.data;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.common.Util;
import com.dsatab.data.Talent.Flags;
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

	private String comments;
	private String variant;
	private boolean houseSpell;

	private String zauberSpezialisierung;

	private EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

	public Spell(Hero hero, Element element, Map<String, SpellInfo> spellInfos) {
		super(element);
		this.hero = hero;

		this.probeInfo.applyProbePattern(element.getAttributeValue(Xml.KEY_PROBE));
		this.name = element.getAttributeValue(Xml.KEY_NAME);
		this.value = Util.parseInt(element.getAttributeValue(Xml.KEY_VALUE));
		this.info = spellInfos.get(name);

		if (info == null) {
			info = new SpellInfo();
			info.setName(name);
		}

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_K)))
			info.setComplexity(element.getAttributeValue(Xml.KEY_K));

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_ZAUBERKOMMENTAR)))
			info.setEffect(element.getAttributeValue(Xml.KEY_ZAUBERKOMMENTAR));

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_KOSTEN)))
			info.setCosts(element.getAttributeValue(Xml.KEY_KOSTEN));

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_REICHWEITE)))
			info.setRange(element.getAttributeValue(Xml.KEY_REICHWEITE));

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_REPRESENTATION)))
			info.setRepresentation(element.getAttributeValue(Xml.KEY_REPRESENTATION));

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_WIRKUNGSDAUER)))
			info.setEffectDuration(element.getAttributeValue(Xml.KEY_WIRKUNGSDAUER));

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_ZAUBERDAUER)))
			info.setCastDuration(element.getAttributeValue(Xml.KEY_ZAUBERDAUER));

		this.comments = element.getAttributeValue(Xml.KEY_ANMERKUNGEN);
		this.variant = element.getAttributeValue(Xml.KEY_VARIANTE);
		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_HAUSZAUBER)))
			this.houseSpell = Boolean.valueOf(element.getAttributeValue(Xml.KEY_HAUSZAUBER));
	}

	public String getName() {
		return name;
	}

	public boolean hasFlag(Flags flag) {
		return flags.contains(flag);
	}

	public void addFlag(Flags flag) {
		flags.add(flag);
	}

	public SpellInfo getInfo() {
		return info;
	}

	@Override
	public ProbeType getProbeType() {
		return ProbeType.ThreeOfThree;
	}

	public String getZauberSpezialisierung() {
		return zauberSpezialisierung;
	}

	public void setZauberSpezialisierung(String zauberSpezialisierung) {
		this.zauberSpezialisierung = zauberSpezialisierung;
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
		return comments;
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
		return info.getEffect();
	}

	public boolean isHouseSpell() {
		return houseSpell;
	}

	public String getCosts() {
		return info.getCosts();
	}

	public String getRange() {
		return info.getRangeDetailed();
	}

	public String getRepresantation() {
		return info.getRepresentation();
	}

	public String getVariant() {
		return variant;
	}

	public String getEffectDuration() {
		return info.getEffectDurationDetailed();

	}

	public String getCastDuration() {
		return info.getCastDurationDetailed();
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
