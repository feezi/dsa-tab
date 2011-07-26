package com.dsatab.data;

import org.jdom.Element;

import com.dsatab.xml.Xml;

public class SpecialFeature {

	public static final String AUSWEICHEN_1 = "Ausweichen I";
	public static final String AUSWEICHEN_2 = "Ausweichen II";
	public static final String AUSWEICHEN_3 = "Ausweichen III";

	public static final String LINKHAND = "Linkhand";

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

	private String name, kultur, gegenstand;

	public SpecialFeature(Element element) {
		this.name = element.getAttributeValue(Xml.KEY_NAME);

		Element gegenstand = element.getChild(Xml.KEY_KULTUR);
		if (gegenstand != null) {
			this.kultur = gegenstand.getAttributeValue(Xml.KEY_NAME);
		}

		gegenstand = element.getChild(Xml.KEY_GEGENSTAND);
		if (gegenstand != null) {
			this.gegenstand = gegenstand.getAttributeValue(Xml.KEY_NAME);
		}
	}

	public String getName() {
		return name;
	}

	public String getKultur() {
		return kultur;
	}

	public String getGegenstand() {
		return gegenstand;
	}

	@Override
	public String toString() {

		if (getName().equals(SpecialFeature.RUESTUNGSGEWOEHNUNG_1)) {
			return getName() + " (" + getGegenstand() + ")";
		} else if (getName().equals(SpecialFeature.KULTURKUNDE)) {
			return getName() + " (" + getKultur() + ")";
		} else {
			return getName();
		}
	}

}
