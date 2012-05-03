package com.dsatab.data;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.DSATabApplication;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.common.Util;
import com.dsatab.data.Talent.Flags;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.util.Debug;
import com.dsatab.xml.Xml;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

public class Art extends MarkableElement implements Value, Markable {

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

	private static SelectArg nameArg, gradeArg;
	private static PreparedQuery<ArtInfo> nameQuery, nameGradeQuery;

	private ArtType type;

	private Hero hero;

	private String name;

	private ArtInfo info;

	private Talent kenntnis;

	private EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

	private boolean customProbe;

	public Art(Hero hero, Element element) {
		super(element);
		this.hero = hero;

		name = element.getAttributeValue(Xml.KEY_NAME).trim();
		String grade = null;

		type = ArtType.getTypeOfArt(name);
		if (type != null) {
			name = type.truncateName(name);
		} else {
			throw new DsaTabRuntimeException("Unknown Art type for: " + name);
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

		// TODO Check for art that do not have a grade on first grade. to get
		// loaded right!!!
		initQueries();
		nameArg.setValue(name);
		if (grade == null) {
			info = DSATabApplication.getInstance().getDBHelper().getRuntimeDao(ArtInfo.class).queryForFirst(nameQuery);
		} else {
			gradeArg.setValue(grade);
			info = DSATabApplication.getInstance().getDBHelper().getRuntimeDao(ArtInfo.class)
					.queryForFirst(nameGradeQuery);

			// if we find no art with grade, try without
			if (info == null) {
				info = DSATabApplication.getInstance().getDBHelper().getRuntimeDao(ArtInfo.class)
						.queryForFirst(nameQuery);
				if (info != null)
					Debug.warning("Art with grade could not be found using the one without grade: " + name);
			}
		}

		if (info == null) {
			Debug.warning("No unique art found for " + name + " : " + grade);
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

		if (info != null && !TextUtils.isEmpty(info.getProbe())) {
			probeInfo.applyProbePattern(info.getProbe());
		}

		if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_PROBE))) {
			probeInfo.applyProbePattern(element.getAttributeValue(Xml.KEY_PROBE));
		}

		if (probeInfo.getErschwernis() == null && info != null && info.getGrade() >= 0) {
			probeInfo.setErschwernis(info.getGrade() * 2 - 2);
		}

		checkCustomProbe();

		if (info == null) {
			Debug.warning("No info found for liturige:" + element.getAttributeValue(Xml.KEY_NAME).trim());
		}
	}

	private static void initQueries() {
		if (nameArg != null)
			return;

		try {
			nameArg = new SelectArg();
			gradeArg = new SelectArg();

			nameQuery = DSATabApplication.getInstance().getDBHelper().getRuntimeDao(ArtInfo.class).queryBuilder()
					.where().eq("name", nameArg).prepare();

			nameGradeQuery = DSATabApplication.getInstance().getDBHelper().getRuntimeDao(ArtInfo.class).queryBuilder()
					.where().eq("name", nameArg).and().eq("grade", gradeArg).prepare();

		} catch (SQLException e) {
			Debug.error(e);
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

	public boolean hasFlag(Flags flag) {
		return flags.contains(flag);
	}

	public void addFlag(Flags flag) {
		flags.add(flag);
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

	public String getMerkmale() {
		if (info != null)
			return info.getMerkmale();
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
