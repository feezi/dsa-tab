package com.dsatab.data;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

public class Purse {

	private static final String VALUE_MITTELREICH = "Mittelreich";

	public enum PurseUnit {
		Dukat, Silbertaler, Heller, Kreuzer
	}

	private Element element;

	private Map<PurseUnit, Element> coins;

	public Purse(Element element) {
		this.element = element;
		coins = new HashMap<PurseUnit, Element>(4);

		NodeList nodes = element.getElementsByTagName(Xml.KEY_MUENZE);

		for (int i = 0; i < nodes.getLength(); i++) {
			Element m = (Element) nodes.item(i);
			PurseUnit w = PurseUnit.valueOf(m.getAttribute(Xml.KEY_NAME));
			coins.put(w, m);
		}
	}

	public void setCoins(PurseUnit w, int value) {

		Element m = coins.get(w);

		if (m == null) {
			m = element.getOwnerDocument().createElement(Xml.KEY_MUENZE);
			m.setAttribute(Xml.KEY_WAEHRUNG, VALUE_MITTELREICH);
			m.setAttribute(Xml.KEY_NAME, w.name());
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
