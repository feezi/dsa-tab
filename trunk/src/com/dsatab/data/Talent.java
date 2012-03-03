package com.dsatab.data;

import java.util.EnumSet;

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;

public class Talent extends MarkableElement implements Value, XmlWriteable {

	public static final String ATHLETIK = "Athletik";

	public static final String WILDNISLEBEN = "Wildnisleben";
	public static final String TIERKUNDE = "Tierkunde";
	public static final String FÄHRTENSUCHEN = "Fährtensuchen";
	public static final String SCHLEICHEN = "Schleichen";
	public static final String SINNENSCHÄRFE = "Sinnenschärfe";
	public static final String PFLANZENKUNDE = "Pflanzenkunde";
	public static final String SELBSTBEHERRSCHUNG = "Selbstbeherrschung";
	public static final String SICH_VERSTECKEN = "Sich verstecken";
	public static final String GEFAHRENINSTINKT = "Gefahreninstinkt";

	public static final String GEISTER_RUFEN = "Geister rufen";
	public static final String GEISTER_BANNEN = "Geister bannen";
	public static final String GEISTER_BINDEN = "Geister binden";
	public static final String GEISTER_ANRUFEN = "Geister aufnehmen";

	public static final String LITURGIE_KENNTNIS_PREFIX = "Liturgiekenntnis";
	public static final String RITUAL_KENNTNIS_PREFIX = "Ritualkenntnis:";

	public static final String RITUAL_KENNTNIS_KRISTALLOMANTIE = "Ritualkenntnis: Kristallomantie";
	public static final String RITUAL_KENNTNIS_RUNENZAUBEREI = "Ritualkenntnis: Runenzauberei";
	public static final String RITUAL_KENNTNIS_ZIBILJA = "Ritualkenntnis: Zibilja";
	public static final String RITUAL_KENNTNIS_ALCHEMIST = "Ritualkenntnis: Alchimist";
	public static final String RITUAL_KENNTNIS_DERWISCH = "Ritualkenntnis: Derwisch";
	public static final String RITUAL_KENNTNIS_DRUIDE = "Ritualkenntnis: Druide";
	public static final String RITUAL_KENNTNIS_DURRO_DUN = "Ritualkenntnis: Durro-Dûn";
	public static final String RITUAL_KENNTNIS_GEODE = "Ritualkenntnis: Geode";
	public static final String RITUAL_KENNTNIS_SCHARLATEN = "Ritualkenntnis: Scharlatan";
	public static final String RITUAL_KENNTNIS_GILDENMAGIE = "Ritualkenntnis: Gildenmagie";
	public static final String RITUAL_KENNTNIS_HEXE = "Ritualkenntnis: Hexe";
	public static final String RITUAL_KENNTNIS_ZAUBERTAENZER_PREFIX = "Ritualkenntnis: Zaubertänzer";

	protected Hero hero;

	private Integer value;

	private String name;

	private String talentSpezialisierung;

	public enum Flags {
		Meisterhandwerk, Begabung, Talentschub, ÜbernatürlicheBegabung
	}

	private EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

	public Talent(Hero hero, Element element) {
		super(element);
		this.hero = hero;
		if (element != null) {
			this.probeInfo.applyProbePattern(element.getAttributeValue(Xml.KEY_PROBE));
			this.probeInfo.applyBePattern(element.getAttributeValue(Xml.KEY_BE));
			this.name = element.getAttributeValue(Xml.KEY_NAME);
			this.value = Util.parseInt(element.getAttributeValue(Xml.KEY_VALUE));
		}
	}

	public String getName() {
		return name;
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

	@Override
	public ProbeType getProbeType() {
		return ProbeType.ThreeOfThree;
	}

	public String getTalentSpezialisierung() {
		return talentSpezialisierung;
	}

	public void setTalentSpezialisierung(String talentSpezialisierung) {
		this.talentSpezialisierung = talentSpezialisierung;
	}

	public boolean hasFlag(Flags flag) {
		return flags.contains(flag);
	}

	public void addFlag(Flags flag) {
		flags.add(flag);
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

	@Override
	public String toString() {
		return getName();
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
