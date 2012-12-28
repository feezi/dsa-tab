package com.dsatab.data.enums;

import java.util.Locale;

public enum AttributeType {

	Mut("MU", false, true), Klugheit("KL", false, true), Intuition("IN", false, true), Charisma("CH", false, true), Fingerfertigkeit(
			"FF", false, true), Gewandtheit("GE", true, true), Konstitution("KO", false, true), Körperkraft("KK",
			false, true), Sozialstatus("SO"), Lebensenergie_Aktuell("LE"), Lebensenergie("LE Total"), Ausdauer_Aktuell("AU"), Ausdauer(
			"AU Total"), Astralenergie_Aktuell("AE"), Astralenergie("AE"), Karmaenergie_Aktuell("KE"), Karmaenergie(
			"KE Total"), Magieresistenz("MR"), ini("INI", true, true), Initiative_Aktuell("INI", true, false), at("AT",
			true, true, false), pa("PA", true, true, false), fk("FK", true, true, false), Behinderung("BE", false), Ausweichen(
			"AW", true, true), Geschwindigkeit("GS", true, false, false), Entrueckung("ENT", false), Verzueckung("VZ",
			false), Erschoepfung("ERS", false);

	public static final AttributeType[] EIGENSCHAFTEN = { Mut, Klugheit, Intuition, Charisma, Fingerfertigkeit,
			Gewandtheit, Konstitution, Körperkraft };

	private String code = null;
	private boolean be;
	private boolean probable = false;
	private boolean editable = true;

	private AttributeType() {
		this(null, false, false);
	}

	private AttributeType(String code) {
		this(code, false);
	}

	private AttributeType(String code, boolean be) {
		this(code, false, false);
	}

	private AttributeType(String code, boolean be, boolean probe) {
		this(code, be, probe, true);
	}

	private AttributeType(String code, boolean be, boolean probe, boolean editable) {
		this.code = code;
		this.be = be;
		this.probable = probe;
		this.editable = editable;
	}

	public static boolean isFight(AttributeType type) {

		switch (type) {
		case at:
		case pa:
		case fk:
		case ini:
		case Initiative_Aktuell:
			return true;
		default:
			return false;
		}
	}

	public static boolean isEigenschaft(AttributeType type) {
		switch (type) {
		case Mut:
		case Klugheit:
		case Intuition:
		case Charisma:
		case Fingerfertigkeit:
		case Gewandtheit:
		case Konstitution:
		case Körperkraft:
			return true;
		default:
			return false;

		}
	}

	public boolean hasBe() {
		return be;
	}

	public static AttributeType byCode(String code) {

		if (code == null)
			return null;

		code = code.toUpperCase(Locale.US);

		for (AttributeType attr : AttributeType.values()) {
			if (attr.code != null && attr.code.equals(code)) {
				return attr;
			}
		}

		return null;
	}

	public boolean probable() {
		return probable;
	}

	public boolean editable() {
		return editable;
	}

	public String code() {
		return code;
	}
}
