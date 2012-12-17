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

import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.XmlWriteable;
import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public class BeidhaendigerKampf implements XmlWriteable {

	private EquippedItem item1;
	private EquippedItem item2;

	private String name;

	private int set;

	/**
	 * 
	 */
	public BeidhaendigerKampf(EquippedItem item1, EquippedItem item2) {
		this.item1 = item1;
		this.item2 = item2;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSet() {
		return set;
	}

	public void setSet(int set) {
		this.set = set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml(org.jdom2.Element)
	 */
	@Override
	public void populateXml(Element element) {

		element.setAttribute(Xml.KEY_SET, Util.toString(item1.getSet()));

		if (item1.getNameId() < item2.getNameId())
			element.setAttribute(Xml.KEY_NAME, Hero.PREFIX_BK + item1.getNameId() + item2.getNameId());
		else
			element.setAttribute(Xml.KEY_NAME, Hero.PREFIX_BK + item2.getNameId() + item1.getNameId());

	}
}
