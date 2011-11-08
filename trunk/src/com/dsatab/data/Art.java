package com.dsatab.data;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;

import android.text.TextUtils;
import android.util.AndroidRuntimeException;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

public class Art extends MarkableElement implements Value, Markable {

	public static final Map<String, ArtType> artMappings = new HashMap<String, Art.ArtType>();
	static {
		artMappings.put("Apport", ArtType.Stabzauber);
		artMappings.put("Bannschwert", ArtType.Stabzauber);
		artMappings.put("Die Gestalt aus Rauch", ArtType.Ritual);
		artMappings.put("Kristallkraft bündeln", ArtType.KristallomantischesRitual);
	}

	public enum ArtType {
		Liturige(LITURGIE_PREFIX, Talent.LITURGIE_KENNTNIS_PREFIX), Ritual(RITUAL_PREFIX, Talent.RITUAL_KENNTNIS_PREFIX), RitualSeher(
				RITUAL_SEHER_PREFIX, Talent.RITUAL_KENNTNIS_PREFIX), Stabzauber(STABZAUBER_PREFIX,
				Talent.RITUAL_KENNTNIS_GILDENMAGIE), Schalenzauber(SCHALENZAUBER_PREFIX,
				Talent.RITUAL_KENNTNIS_ALCHEMIST), SchlangenringZauber(SCHLANGENRING_ZAUBER_PEFIX,
				Talent.RITUAL_KENNTNIS_GEODE), Schuppenbeutel(SCHUPPENBEUTEL_PREFIX,
				Talent.RITUAL_KENNTNIS_KRISTALLOMANTIE), Trommelzauber(TROMMELZAUBER_PREFIX,
				Talent.RITUAL_KENNTNIS_DERWISCH), Runen(RUNEN_PREFIX), Kugelzauber(KUGELZAUBER_PREFIX), KristallomantischesRitual(
				KRISTALLOMANTISCHES_RITUAL_PREFIX, Talent.RITUAL_KENNTNIS_KRISTALLOMANTIE), Hexenfluch(
				HEXENFLUCH_PREFIX, Talent.RITUAL_KENNTNIS_HEXE), GabeDesOdun(GABE_DES_ODUN_PREFIX,
				Talent.RITUAL_KENNTNIS_DURRO_DUN), DruidischesHerrschaftsritual(DRUIDISCHES_HERRSCHAFTSRITUAL_PREFIX,
				Talent.RITUAL_KENNTNIS_DRUIDE), DruidischesDolchritual(DRUIDISCHES_DOLCHRITUAL_PREFIX,
				Talent.RITUAL_KENNTNIS_DRUIDE), Zaubertanz(ZAUBERTANZ_PREFIX,
				Talent.RITUAL_KENNTNIS_ZAUBERTAENZER_PREFIX), Zauberzeichen(ZAUBERZEICHEN_PREFIX), ZibiljaRitual(
				ZIBILJA_RITUAL_PREFIX, Talent.RITUAL_KENNTNIS_ZIBILJA);

		private String prefix;

		private String talentName;

		private ArtType(String prefix) {
			this.prefix = prefix;
			this.talentName = null;
		}

		private ArtType(String prefix, String talentName) {
			this.prefix = prefix;
			this.talentName = talentName;
		}

		public String prefix() {
			return prefix;
		}

		public String talentName() {
			return talentName;
		}

		public String getName() {
			String name = prefix.trim();
			if (name.endsWith(":"))
				name = name.substring(0, name.length() - 1);

			return name;
		}

		public static ArtType getTypeOfArt(String artName) {
			ArtType[] types = ArtType.values();
			ArtType result = null;
			result = artMappings.get(artName);
			if (result == null) {
				for (ArtType type : types) {
					if (artName.startsWith(type.prefix)) {
						result = type;
						break;
					}
				}
			}
			return result;
		}

		public String truncateName(String artName) {
			if (artName.startsWith(prefix)) {
				return artName.substring(prefix.length()).trim();
			} else {
				return artName.trim();
			}
		}
	}

	public static final String LITURGIE_PREFIX = "Liturgie: ";
	public static final String RITUAL_PREFIX = "Ritual: ";
	public static final String RITUAL_SEHER_PREFIX = "Seher: ";
	public static final String STABZAUBER_PREFIX = "Stabzauber: ";
	public static final String SCHALENZAUBER_PREFIX = "Schalenzauber: ";
	public static final String SCHLANGENRING_ZAUBER_PEFIX = "Schlangenring-Zauber: ";
	public static final String SCHUPPENBEUTEL_PREFIX = "Schuppenbeutel: ";
	public static final String TROMMELZAUBER_PREFIX = "Trommelzauber: ";
	public static final String RUNEN_PREFIX = "Runen: ";
	public static final String KUGELZAUBER_PREFIX = "Kugelzauber: ";
	public static final String KRISTALLOMANTISCHES_RITUAL_PREFIX = "Kristallomantisches Ritual: ";
	public static final String HEXENFLUCH_PREFIX = "Hexenfluch: ";
	public static final String GABE_DES_ODUN_PREFIX = "Gabe des Odûn: ";
	public static final String DRUIDISCHES_HERRSCHAFTSRITUAL_PREFIX = "Druidisches Herrschaftsritual: ";
	public static final String DRUIDISCHES_DOLCHRITUAL_PREFIX = "Druidisches Dolchritual: ";
	public static final String ZAUBERTANZ_PREFIX = "Zaubertanz: ";
	public static final String ZAUBERZEICHEN_PREFIX = "Zauberzeichen: ";
	public static final String ZIBILJA_RITUAL_PREFIX = "Zibilja-Ritual: ";

	public static final Comparator<Art> NAME_COMPARATOR = new Comparator<Art>() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Art object1, Art object2) {
			return object1.getName().compareTo(object2.getName());
		}

	};

	private ArtType type;

	private Hero hero;

	private String name;

	private ArtInfo info;

	private Talent kenntnis;

	private boolean begabung;

	private boolean customProbe;

	public Art(Hero hero, Element element, Map<String, ArtInfo> artInfos) {
		super(element);
		this.hero = hero;

		name = element.getAttributeValue(Xml.KEY_NAME).trim();
		String grade = null;

		type = ArtType.getTypeOfArt(name);
		if (type != null) {
			name = type.truncateName(name);
		} else {
			throw new AndroidRuntimeException("Unknown Art type for: " + name);
		}

		// we have a grade specification in the name: Erdsegen (III)
		if (name.endsWith(")")) {
			grade = name.substring(name.lastIndexOf("(") + 1);
			grade = grade.substring(0, grade.length() - 1);

			// we acutally found a grade (I)
			if (Util.gradeToInt(grade) >= 0) {
				name = name.substring(0, name.lastIndexOf("(")).trim();
			} else {
				grade = null;
			}
		}

		if (grade != null) {
			info = artInfos.get(name + " " + grade);
		} else {
			info = artInfos.get(name);
		}

		switch (type) {
		case Ritual:
			kenntnis = hero.getTalent(Talent.GEISTER_ANRUFEN);
			break;
		default:
			if (type.talentName() != null)
				kenntnis = hero.getArtTalent(type.talentName());
			break;
		}

		if (kenntnis != null) {
			probeInfo = kenntnis.getProbeInfo().clone();
		}

		probeInfo.applyProbePattern(element.getAttributeValue(Xml.KEY_PROBE));

		if (probeInfo.getErschwernis() == null && info != null) {
			probeInfo.setErschwernis(info.getGrade() * 2 - 2);
		}

		checkCustomProbe();

		if (info == null) {
			Debug.warning("No info found for liturige:" + element.getAttributeValue(Xml.KEY_NAME).trim());
		}
	}

	public ArtType getType() {
		return type;
	}

	public String getFullName() {
		if (info != null)
			return info.getFullName();
		else
			return name;

	}

	public boolean isBegabung() {
		return begabung;
	}

	public void setBegabung(boolean begabung) {
		this.begabung = begabung;
	}

	public String getName() {
		if (info != null) {
			return info.getName();
		} else {
			return name;
		}
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

	private Talent getArtTalent() {
		return kenntnis;
	}

	public ProbeInfo getProbeInfo() {
		return probeInfo;
	}

	public boolean hasCustomProbe() {
		return customProbe;
	}

	private void checkCustomProbe() {
		if (getArtTalent() != null) {
			customProbe = !Arrays.equals(probeInfo.getAttributeTypes(), getArtTalent().getProbeInfo()
					.getAttributeTypes());
		} else {
			customProbe = true;
		}
	}

	@Override
	public ProbeType getProbeType() {
		return ProbeType.ThreeOfThree;
	}

	public Integer getProbeValue(int i) {
		if (probeInfo.getAttributeTypes() != null) {
			AttributeType type = probeInfo.getAttributeTypes()[i];
			return hero.getModifiedValue(type, false, false);
		} else if (getArtTalent() != null) {
			return getArtTalent().getProbeValue(i);
		} else {
			return null;
		}
	}

	@Override
	public Integer getProbeBonus() {
		return getValue();
	}

	public Integer getValue() {
		if (getArtTalent() != null)
			return getArtTalent().getValue();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Value#setValue(java.lang.Integer)
	 */
	@Override
	public void setValue(Integer value) {
		// todo cannot change value of liturgie
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

	public String getCosts() {

		String cost = element.getAttributeValue(Xml.KEY_KOSTEN);

		if (TextUtils.isEmpty(cost) && info != null) {
			cost = info.getCosts();
		}

		return cost;
	}

	public String getEffect() {
		String effect = element.getAttributeValue(Xml.KEY_WIRKUNG);

		if (info != null) {
			if (TextUtils.isEmpty(effect))
				effect = info.getEffect();
			else
				effect += (" - " + info.getEffect());
		}
		return effect;
	}

	public String getCastDuration() {

		String castduration = element.getAttributeValue(Xml.KEY_DAUER);

		if (TextUtils.isEmpty(castduration) && info != null) {
			castduration = info.getCastDuration();
		}

		return castduration;
	}

	public String getCastDurationDetailed() {

		String castduration = element.getAttributeValue(Xml.KEY_DAUER);

		if (TextUtils.isEmpty(castduration) && info != null) {
			castduration = info.getCastDurationDetailed();
		}

		return castduration;
	}

	public String getEffectDuration() {
		if (info != null)
			return info.getEffectDuration();
		else
			return null;
	}

	public int getGrade() {
		if (info != null)
			return info.getGrade();
		else
			return -1;
	}

	public String getOrigin() {
		if (info != null)
			return info.getOrigin();
		else
			return null;
	}

	public String getRange() {
		if (info != null)
			return info.getRange();
		else
			return null;
	}

	public String getRangeDetailed() {
		if (info != null)
			return info.getRangeDetailed();
		else
			return null;
	}

	public String getSource() {
		if (info != null)
			return info.getSource();
		else
			return null;
	}

	public String getTarget() {
		if (info != null)
			return info.getTarget();
		else
			return null;
	}

	public String getTargetDetailed() {
		if (info != null)
			return info.getTargetDetailed();
		else
			return null;
	}

}
