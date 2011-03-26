package com.dsatab.data;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

public class Purse {

	public enum Currency {
		AlAnfa("Al'Anfa", PurseUnit.Doublone, PurseUnit.Oreal, PurseUnit.KleinerOreal, PurseUnit.Dirham), Vallusa(
				PurseUnit.Witten, PurseUnit.Stüber, PurseUnit.Flindrich), Trahelien(PurseUnit.Suvar, PurseUnit.Hedsch,
				PurseUnit.Chryskl), Xeranien(PurseUnit.Borbaradstaler, PurseUnit.Zholvari, PurseUnit.Splitter), Bornland(
				PurseUnit.Batzen, PurseUnit.Groschen, PurseUnit.Deut), Mittelreich(PurseUnit.Dukat,
				PurseUnit.Silbertaler, PurseUnit.Heller, PurseUnit.Kreuzer), Aranien(PurseUnit.Dinar,
				PurseUnit.Schekel, PurseUnit.Hallah, PurseUnit.Kurush), Zwerge(PurseUnit.Zwergentaler), Kalifat(
				PurseUnit.Marawedi, PurseUnit.Zechine, PurseUnit.Muwlat), Horasreich(PurseUnit.Horasdor), Amazonen(
				PurseUnit.Amazonenkronen);

		private String name;

		private PurseUnit[] purseUnits;

		private Currency(PurseUnit... purseUnits) {
			this(null, purseUnits);
		}

		public String xmlName() {
			return name;
		}

		private Currency(String name, PurseUnit... purseUnits) {
			if (name == null)
				name = name();

			this.name = name;
			this.purseUnits = purseUnits;
		}

		public PurseUnit[] units() {
			return purseUnits;
		}

		public static Currency getByXmlName(String name) {
			for (Currency c : Currency.values()) {
				if (c.name.equals(name))
					return c;
			}
			return null;
		}
	}

	public enum PurseUnit {
		Dukat, Silbertaler, Heller, Kreuzer, Doublone, Flindrich, Hedsch, Dirham, Splitter, Zholvari, Batzen, Dinar, Stüber, KleinerOreal(
				"Kleiner Oreal"), Deut, Hallah, Borbaradstaler, Groschen, Zwergentaler, Kurush, Muwlat, Horasdor, Witten, Marawedi, Amazonenkronen, Schekel, Oreal, Suvar, Zechine, Chryskl(
				"Ch'ryskl");

		private String name;

		private PurseUnit() {
			this(null);
		}

		private PurseUnit(String name) {
			if (name == null)
				name = name();

			this.name = name;
		}

		public String xmlName() {
			return name;
		}

		public static PurseUnit getByXmlName(String name) {
			for (PurseUnit c : PurseUnit.values()) {
				if (c.name.equals(name))
					return c;
			}
			return null;
		}
	}

	private Currency activeCurrency;

	private Element element;

	private Map<PurseUnit, Element> coins;

	public Purse(Element element) {
		this.element = element;
		coins = new HashMap<PurseUnit, Element>(4);

		NodeList nodes = element.getElementsByTagName(Xml.KEY_MUENZE);

		for (int i = 0; i < nodes.getLength(); i++) {
			Element m = (Element) nodes.item(i);
			PurseUnit w = PurseUnit.getByXmlName(m.getAttribute(Xml.KEY_NAME));
			coins.put(w, m);
		}
	}

	public Currency getActiveCurrency() {
		return activeCurrency;
	}

	public void setActiveCurrency(Currency activeCurrency) {
		this.activeCurrency = activeCurrency;
	}

	public void setCoins(PurseUnit w, int value) {

		Element m = coins.get(w);

		if (m == null) {
			m = element.getOwnerDocument().createElement(Xml.KEY_MUENZE);
			m.setAttribute(Xml.KEY_WAEHRUNG, Currency.Mittelreich.xmlName());
			m.setAttribute(Xml.KEY_NAME, w.xmlName());
			element.appendChild(m);

			coins.put(w, m);
		}
		m.setAttribute("anzahl", Util.toString(value));
	}

	public int getCoins(PurseUnit w) {
		Element m = coins.get(w);
		if (m != null)
			return Util.parseInt(m.getAttribute(Xml.KEY_ANZAHL));
		else
			return 0;
	}

}
