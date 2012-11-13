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

import java.util.Date;

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public class ChangeEvent {

	// <ereignis Abenteuerpunkte="-4" Alt="1" Info="Gegenseitiges Lehren"
	// Neu="2" obj="Wettervorhersage" text="Talent steigern"
	// time="1309649750436" version="5.1.3"/>

	private Element element;

	/**
	 * 
	 */
	public ChangeEvent() {
		this.element = new Element(Xml.KEY_EREIGNIS);
	}

	public ChangeEvent(Element element) {
		this.element = element;
	}

	public Integer getExperiencePoints() {
		return Util.parseInt(element.getAttributeValue(Xml.KEY_ABENTEUERPUNKTE_UPPER));
	}

	public void setExperiencePoints(Integer xp) {
		element.setAttribute(Xml.KEY_ABENTEUERPUNKTE_UPPER, Util.toString(xp));
	}

	public Integer getOldValue() {
		return Util.parseInt(element.getAttributeValue(Xml.KEY_ALT));
	}

	public void setOldValue(Integer v) {
		element.setAttribute(Xml.KEY_ALT, Util.toString(v));
	}

	public Integer getNewValue() {
		return Util.parseInt(element.getAttributeValue(Xml.KEY_NEU));
	}

	public void setNewValue(Integer v) {
		element.setAttribute(Xml.KEY_NEU, Util.toString(v));
	}

	public String getInfo() {
		return element.getAttributeValue(Xml.KEY_INFO);
	}

	public void setInfo(String v) {
		element.setAttribute(Xml.KEY_INFO, v);
	}

	public String getText() {
		return element.getAttributeValue(Xml.KEY_TEXT);
	}

	public void setText(String v) {
		element.setAttribute(Xml.KEY_TEXT, v);
	}

	public String getVersion() {
		return element.getAttributeValue(Xml.KEY_VERSION);
	}

	public void setVersion(String v) {
		element.setAttribute(Xml.KEY_VERSION, v);
	}

	public String getObject() {
		return element.getAttributeValue(Xml.KEY_OBJ);
	}

	public void setObject(String v) {
		element.setAttribute(Xml.KEY_OBJ, v);
	}

	public Date getTime() {
		return new Date(Util.parseLong(element.getAttributeValue(Xml.KEY_TIME)));
	}

	public void setTime(Date v) {
		element.setAttribute(Xml.KEY_TIME, Util.toString(v.getTime()));
	}

	public Element getElement() {
		return element;
	}

}
