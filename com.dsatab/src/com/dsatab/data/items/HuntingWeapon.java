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
package com.dsatab.data.items;

import org.jdom2.Element;

import com.dsatab.data.Hero;
import com.dsatab.data.XmlWriteable;
import com.dsatab.util.Util;
import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public class HuntingWeapon implements XmlWriteable {

	private Integer number;
	private Integer set;

	/**
	 * 
	 */
	public HuntingWeapon() {

	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getSet() {
		return set;
	}

	public void setSet(Integer set) {
		this.set = set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml(org.jdom2.Element)
	 */
	@Override
	public void populateXml(Element element) {
		if (number != null)
			element.setAttribute(Xml.KEY_NUMMER, Util.toString(number));

		element.setAttribute(Xml.KEY_NAME, Hero.JAGTWAFFE);

		if (set != null)
			element.setAttribute(Xml.KEY_SET, Util.toString(set));

	}
}
