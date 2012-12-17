package com.dsatab.data;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.EnumSet;

import org.jdom2.Element;

import android.text.TextUtils;

import com.dsatab.DSATabApplication;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.util.Debug;
import com.dsatab.xml.Xml;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

public class Spell extends MarkableElement implements Value {

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

	private static SelectArg nameArg;
	private static PreparedQuery<SpellInfo> nameQuery;

	private Hero hero;
	private String name;
	private Integer value;
	private SpellInfo info;

	private String comments;
	private String variant;

	private String zauberSpezialisierung;

	public enum Flags {
		Begabung, ÜbernatürlicheBegabung, Hauszauber
	}

	private EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

	private static void initQueries() {
		if (nameArg != null)
			return;

		try {
			nameArg = new SelectArg();

			nameQuery = DSATabApplication.getInstance().getDBHelper().getRuntimeDao(SpellInfo.class).queryBuilder()
					.where().eq("name", nameArg).prepare();

		} catch (SQLException e) {
			Debug.error(e);
		}

	}

	public Spell(Hero hero) {
		super();
		this.hero = hero;

		initQueries();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;

		Debug.warning("Searching for spell info :" + name);
		nameArg.setValue(name);
		this.info = DSATabApplication.getInstance().getDBHelper().getRuntimeDao(SpellInfo.class)
				.queryForFirst(nameQuery);

		if (info == null) {
			Debug.warning("No  spell info found for " + name);
		}

		if (info == null) {
			info = new SpellInfo();
			info.setName(name);
		}
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

	public void setProbePattern(String pattern) {
		this.probeInfo.applyProbePattern(pattern);
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

	public String getComments() {
		if (TextUtils.isEmpty(comments))
			return info != null ? info.getComments() : null;
		else
			return comments;
	}

	public void setComments(String comment) {
		this.comments = comment;

	}

	public String getVariant() {
		if (TextUtils.isEmpty(variant))
			return info != null ? info.getVariant() : null;
		else
			return variant;
	}

	public void setVariant(String s) {
		this.variant = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml()
	 */
	@Override
	public void populateXml(Element element) {
		super.populateXml(element);

		if (value != null)
			element.setAttribute(Xml.KEY_VALUE, Integer.toString(value));
		else
			element.removeAttribute(Xml.KEY_VALUE);

		element.setAttribute(Xml.KEY_ANMERKUNGEN, comments);
		element.setAttribute(Xml.KEY_VARIANTE, variant);

	}

}
