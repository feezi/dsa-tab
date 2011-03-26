package com.dsatab.data.enums;

public enum AttributeType {
	Mut("MU", false, true), Klugheit("KL", false, true), Intuition("IN", false, true), Charisma("CH", false, true), Fingerfertigkeit("FF", true, true), Gewandtheit(
			"GE", true, true), Konstitution("KO", false, true), Körperkraft("KK", false, true), Sozialstatus, Lebensenergie, Ausdauer, Astralenergie, Karmaenergie, Magieresistenz, ini(
			"INI", true, true), at("AT", true, true), pa("PA", true, true), fk("FK", true, true), Behinderung("BE", false), Ausweichen("BE", true, true);

	private String code = null;
	private boolean be;
	private boolean probable = false;

	private AttributeType() {

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

	private AttributeType(String code, boolean be, boolean probe) {
		this.code = code;
		this.be = be;
		this.probable = probe;
	}

	private AttributeType(String code, boolean be) {
		this.code = code;
		this.be = be;
	}

	public static AttributeType byCode(String code) {

		code = code.toUpperCase();

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
}
