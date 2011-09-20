package com.dsatab.data;

import java.util.Comparator;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.xml.DataManager;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

public class Liturgie implements Probe, Value, Markable {

	public static final Comparator<Liturgie> NAME_COMPARATOR = new Comparator<Liturgie>() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Liturgie object1, Liturgie object2) {
			return object1.getName().compareTo(object2.getName());
		}

	};

	private Element element;

	private AttributeType[] probes;

	private Hero hero;

	private String name;

	private LiturgieInfo info;

	public Liturgie(Hero hero, Element element) {
		this.element = element;
		this.hero = hero;

		String name = element.getAttributeValue(Xml.KEY_NAME).trim();
		String grade = null;

		if (name.startsWith(SpecialFeature.LITURGIE_PREFIX)) {
			name = name.substring(SpecialFeature.LITURGIE_PREFIX.length()).trim();
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
			info = DataManager.getLiturgieByName(name, Util.gradeToInt(grade));
		} else {
			info = DataManager.getLiturgieByName(name);
		}
		if (info == null)
			Debug.warning("No info found for liturige:" + element.getAttributeValue(Xml.KEY_NAME).trim());
	}

	public String getFullName() {
		if (info != null)
			return info.getFullName();
		else
			return name;

	}

	public String getName() {
		if (info != null) {
			return info.getName();
		} else {
			if (name == null) {
				name = element.getAttributeValue(Xml.KEY_NAME);

				if (name.startsWith(SpecialFeature.LITURGIE_PREFIX)) {
					name = name.substring(SpecialFeature.LITURGIE_PREFIX.length());
				}
			}
		}

		return name;
	}

	private Talent getLiturigeKenntnis() {
		Talent kenntnis = null;
		if (hero != null) {
			kenntnis = hero.getLiturgieKenntnis();
		}

		return kenntnis;
	}

	public String getProbe() {
		if (getLiturigeKenntnis() != null)
			return getLiturigeKenntnis().getProbe();
		else
			return null;
	}

	public boolean isFavorite() {
		if (element.getAttribute(Xml.KEY_FAVORITE) != null) {
			return Boolean.valueOf(element.getAttributeValue(Xml.KEY_FAVORITE));
		} else {
			return false;
		}
	}

	public boolean isUnused() {
		if (element.getAttribute(Xml.KEY_UNUSED) != null) {
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
		String erschwernis = element.getAttributeValue(Xml.KEY_PROBE);

		Integer e = null;
		if (!TextUtils.isEmpty(erschwernis)) {
			try {
				e = Util.parseInt(erschwernis);
			} catch (NumberFormatException e1) {
				Debug.warn(e1);
				e = null;
			}
		}

		if (e == null) {
			if (info != null) {
				e = info.getGrade() * 2 - 2;
			} else {
				e = null;
			}
		}
		return e;
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
		if (getLiturigeKenntnis() != null)
			return getLiturigeKenntnis().getValue();
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

	public String getBe() {
		if (getLiturigeKenntnis() != null)
			return getLiturigeKenntnis().getBe();
		else
			return null;
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
