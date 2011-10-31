/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.data;

import java.util.List;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public class HeroBaseInfo {

	private Element basisElement, rasse, aussehen, ausbildungen, kultur, groesse;

	/**
	 * 
	 */
	public HeroBaseInfo(Element basisElement) {

		this.basisElement = basisElement;

		rasse = basisElement.getChild(Xml.KEY_RASSE);
		ausbildungen = basisElement.getChild(Xml.KEY_AUSBILDUNGEN);
		kultur = basisElement.getChild(Xml.KEY_KULTUR);
		if (rasse != null) {
			aussehen = rasse.getChild(Xml.KEY_AUSSEHEN);
			groesse = rasse.getChild(Xml.KEY_GROESSE);
		}

	}

	public Integer getGewicht() {

		if (groesse != null) {
			return Util.parseInt(groesse.getAttributeValue(Xml.KEY_GEWICHT));
		}

		return null;
	}

	public Integer getGroesse() {

		if (groesse != null) {
			return Util.parseInt(groesse.getAttributeValue(Xml.KEY_VALUE));
		}

		return null;
	}

	public Integer getAlter() {

		if (aussehen != null) {
			return Util.parseInt(aussehen.getAttributeValue(Xml.KEY_ALTER));
		}

		return null;

	}

	public String getAugenFarbe() {

		if (aussehen != null) {
			return aussehen.getAttributeValue(Xml.KEY_EYECOLOR);
		}

		return null;
	}

	public String getHaarFarbe() {

		if (aussehen != null) {
			return aussehen.getAttributeValue(Xml.KEY_HAIRCOLOR);
		}

		return null;
	}

	public String getAusbildung() {

		if (ausbildungen != null) {
			@SuppressWarnings("unchecked")
			List<Element> ausbildungElements = ausbildungen.getChildren();

			StringBuilder sb = new StringBuilder();

			for (Element ausbildung : ausbildungElements) {
				String value = ausbildung.getAttributeValue(Xml.KEY_STRING);
				if (!TextUtils.isEmpty(value)) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(value);
				}
			}
			return sb.toString();
		}

		Element ausbildung = basisElement.getChild(Xml.KEY_AUSBILDUNG);
		if (ausbildung != null) {
			String value = ausbildung.getAttributeValue(Xml.KEY_STRING);
			if (!TextUtils.isEmpty(value))
				return value;
		}

		Element profession = basisElement.getChild(Xml.KEY_PROFESSION);
		if (profession != null) {
			String value = profession.getAttributeValue(Xml.KEY_STRING);
			if (!TextUtils.isEmpty(value))
				return value;
		}

		return null;

	}

	public String getAussehen() {

		if (aussehen != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				sb.append(aussehen.getAttributeValue(Xml.KEY_AUSSEHENTEXT_PREFIX + i));
				if (!TextUtils.isEmpty(aussehen.getAttributeValue(Xml.KEY_AUSSEHENTEXT_PREFIX + (i + 1))))
					sb.append(", ");
			}
			return sb.toString();

		}
		return null;
	}

	public String getTitel() {

		if (aussehen != null) {
			return aussehen.getAttributeValue(Xml.KEY_TITEL);
		}

		return null;
	}

	public String getStand() {

		if (aussehen != null) {
			return aussehen.getAttributeValue(Xml.KEY_STAND);
		}

		return null;
	}

	public String getRasse() {

		if (rasse != null) {
			return rasse.getAttributeValue(Xml.KEY_STRING);
		} else
			return null;
	}

	public String getKultur() {

		if (kultur != null) {
			return kultur.getAttributeValue(Xml.KEY_STRING);
		} else
			return null;
	}
}
