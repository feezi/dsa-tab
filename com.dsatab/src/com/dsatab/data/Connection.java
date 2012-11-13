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

import java.util.Comparator;

import org.jdom.Element;

import com.dsatab.data.enums.EventCategory;
import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public class Connection {

	public static Comparator<Connection> NAME_COMPARATOR = new Comparator<Connection>() {
		@Override
		public int compare(Connection object1, Connection object2) {
			return object1.getName().compareTo(object2.getName());
		}
	};

	private Element element;

	/**
	 * 
	 */
	public Connection() {
		this(new Element(Xml.KEY_VERBINDUNG));
	}

	/**
	 * 
	 */
	public Connection(Element element) {
		this.element = element;
	}

	public EventCategory getCategory() {
		return EventCategory.Bekanntschaft;
	}

	public Element getElement() {
		return element;
	}

	public String getDescription() {
		return element.getAttributeValue(Xml.KEY_DESCRIPTION);
	}

	public void setDescription(String value) {
		element.setAttribute(Xml.KEY_DESCRIPTION, value);
	}

	public String getName() {
		return element.getAttributeValue(Xml.KEY_NAME);
	}

	public void setName(String value) {
		element.setAttribute(Xml.KEY_NAME, value);
	}

	public String getSozialStatus() {
		return element.getAttributeValue(Xml.KEY_SO);
	}

	public void setSozialStatus(String value) {
		element.setAttribute(Xml.KEY_SO, value);
	}

}
