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

import org.jdom2.Element;

import com.dsatab.data.enums.EventCategory;
import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public class Connection implements XmlWriteable {

	public static Comparator<Connection> NAME_COMPARATOR = new Comparator<Connection>() {
		@Override
		public int compare(Connection object1, Connection object2) {
			return object1.getName().compareTo(object2.getName());
		}
	};

	private String description, name, sozialStatus;

	/**
	 * 
	 */
	public Connection() {

	}

	public EventCategory getCategory() {
		return EventCategory.Bekanntschaft;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getSozialStatus() {
		return sozialStatus;
	}

	public void setSozialStatus(String value) {
		this.sozialStatus = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml(org.jdom2.Element)
	 */
	@Override
	public void populateXml(Element element) {
		element.setAttribute(Xml.KEY_DESCRIPTION, description);
		element.setAttribute(Xml.KEY_SO, sozialStatus);
		element.setAttribute(Xml.KEY_NAME, name);
	}

}
