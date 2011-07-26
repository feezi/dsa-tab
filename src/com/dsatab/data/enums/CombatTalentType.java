package com.dsatab.data.enums;

public enum CombatTalentType {

	Anderthalbhänder(-2), Armbrust(-5, true), Bastardstäbe(-2, false), Belagerungswaffen(null, true), Blasrohr(-5, true), Bogen(
			-3, true), Diskus(-3, true), Dolche(-1), Fechtwaffen(-1), Hiebwaffen(-4), Infanteriewaffen(-3), Kettenstäbe(
			-1), Kettenwaffen(-3), Lanzenreiten(null, true), Peitsche(-1), Raufen(0), Ringen(0), Schleuder(-2, true), Schwerter(
			-2), Speere(-3), Stäbe(-2), Säbel(-2), Wurfbeile(-2, true), Wurfmesser(-3, true), Wurfspeere(-2, true), Zweihandflegel(
			-3), Zweihandhiebwaffen(-3), Zweihandschwerter(-2, false, "Zweihandschwerter/-säbel");

	private Integer be;

	private boolean fk;

	private String name;

	private CombatTalentType() {
		this(null, false, null);
	}

	private CombatTalentType(Integer be) {
		this(be, false, null);
	}

	private CombatTalentType(Integer be, boolean fk) {
		this(be, fk, null);
	}

	private CombatTalentType(Integer be, boolean fk, String name) {
		this.be = be;
		this.fk = fk;

		if (name == null)
			this.name = name();
		else
			this.name = name;
	}

	public String getBe() {
		if (be != null) {
			if (be < 0)
				return "BE" + be;
			else if (be > 0)
				return "BE+" + be;
			else
				return "BE";
		} else {
			return null;
		}
	}

	public boolean isFk() {
		return fk;
	}

	public String getName() {
		return this.name;
	}

	public static CombatTalentType byName(String name) {
		for (CombatTalentType t : CombatTalentType.values()) {
			if (name.equals(t.getName())) {
				return t;
			}

		}
		return null;
	}

}
