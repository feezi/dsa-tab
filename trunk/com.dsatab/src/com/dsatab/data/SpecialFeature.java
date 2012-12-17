package com.dsatab.data;

import android.text.TextUtils;

public class SpecialFeature {

	public static final String AUSWEICHEN_1 = "Ausweichen I";
	public static final String AUSWEICHEN_2 = "Ausweichen II";
	public static final String AUSWEICHEN_3 = "Ausweichen III";

	public static final String AUFMERKSAMKEIT = "Aufmerksamkeit";

	public static final String DÄMMERNGSSICHT = "Dämmerungssicht";
	public static final String NACHTSICHT = "Nachtsicht";
	public static final String HERRAUSRAGENDER_SINN = "Herausragender Sinn";
	public static final String EINGESCHRÄNKTER_SINN = "Eingeschränkter Sinn";

	public static final String EINÄUGIG = "Einäugig";
	public static final String EINBILDUNGEN = "Einbildungen";
	public static final String DUNKELANGST = "Dunkelangst";
	public static final String NACHTBLIND = "Nachtblind";

	public static final String LINKHAND = "Linkhand";
	public static final String UNSTET = "Unstet";

	public static final String PARIERWAFFEN_1 = "Parierwaffen I";
	public static final String PARIERWAFFEN_2 = "Parierwaffen II";

	public static final String SCHILDKAMPF_1 = "Schildkampf I";
	public static final String SCHILDKAMPF_2 = "Schildkampf II";
	public static final String SCHILDKAMPF_3 = "Schildkampf III";
	public static final String MEISTERSCHUETZE = "Meisterschütze";
	public static final String SCHARFSCHUETZE = "Scharfschütze";

	public static final String WK_GLADIATORENSTIL = "Waffenloser Kampfstil: Gladiatorenstil";
	public static final String WK_HAMMERFAUST = "Waffenloser Kampfstil: Hammerfaust";
	public static final String WK_MERCENARIO = "Waffenloser Kampfstil: Mercenario";
	public static final String WK_HRURUZAT = "Waffenloser Kampfstil: Hruruzat";
	public static final String WK_UNAUER_SCHULE = "Waffenloser Kampfstil: Unauer Schule";
	public static final String WK_BORNLAENDISCH = "Waffenloser Kampfstil: Bornländisch";
	public static final String BEIDHAENDIGER_KAMPF_1 = "Beidhändiger Kampf I";
	public static final String BEIDHAENDIGER_KAMPF_2 = "Beidhändiger Kampf II";
	public static final String FLINK = "Flink";
	public static final String BEHAEBIG = "Behäbig";
	public static final String EINBEINIG = "Einbeinig";
	public static final String KLEINWUECHSIG = "Kleinwüchsig";
	public static final String LAHM = "Lahm";
	public static final String ZWERGENWUCHS = "Zwergenwuchs";
	public static final String RUESTUNGSGEWOEHNUNG_3 = "Rüstungsgewöhnung III";
	public static final String RUESTUNGSGEWOEHNUNG_2 = "Rüstungsgewöhnung II";
	public static final String RUESTUNGSGEWOEHNUNG_1 = "Rüstungsgewöhnung I";
	public static final String KULTURKUNDE = "Kulturkunde";
	public static final String GLASKNOCHEN = "Glasknochen";
	public static final String EISERN = "Eisern";
	public static final String GEFAESS_DER_STERNE = "Gefäß der Sterne";

	public static final String KAMPFREFLEXE = "Kampfreflexe";
	public static final String KAMPFGESPUER = "Kampfgespür";

	public static final String KLINGENTAENZER = "Klingentänzer";

	public static final String TALENTSPEZIALISIERUNG_PREFIX = "Talentspezialisierung ";
	public static final String ZAUBERSPEZIALISIERUNG_PREFIX = "Zauberspezialisierung ";

	private String name, additionalInfo, parameter1, parameter2, comment;

	public SpecialFeature() {

	}

	public String getComment() {
		return comment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	/**
	 * Return additional parameters, depending on type of special feature.
	 * Rüstungsgewöhnung: gegenstand <br/>
	 * Talentspezialisierung: talent <br />
	 * 
	 * @return
	 */
	public String getParameter1() {
		return parameter1;
	}

	/**
	 * Return additional parameters, depending on type of special feature.
	 * 
	 * Talentspezialisierung: spezialisierung
	 * 
	 * @return
	 */
	public String getParameter2() {
		return parameter2;
	}

	public void setParameter1(String parameter1) {
		this.parameter1 = parameter1;
	}

	public void setParameter2(String parameter2) {
		this.parameter2 = parameter2;
	}

	@Override
	public String toString() {

		if (name.equals(SpecialFeature.RUESTUNGSGEWOEHNUNG_1)) {
			return name + " (" + parameter1 + ")";
		} else if (!TextUtils.isEmpty(additionalInfo)) {
			return name + " (" + additionalInfo + ")";
		} else {
			return name;
		}
	}

}