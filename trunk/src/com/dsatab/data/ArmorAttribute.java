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

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.Position;
import com.dsatab.xml.Xml;

public class ArmorAttribute implements Value {

	private Element element;

	private Hero hero;

	public ArmorAttribute(Element element) {
		this(element, null);
	}

	public ArmorAttribute(Element element, Hero hero) {
		this.element = element;
		this.hero = hero;
	}

	public String getName() {
		return getPosition().getName();
	}

	public Position getPosition() {
		return Position.valueOf(element.getAttributeValue(Xml.KEY_NAME));
	}

	public Integer getValue() {

		if (element.getAttribute(Xml.KEY_VALUE) != null) {
			return Util.parseInt(element.getAttributeValue(Xml.KEY_VALUE));
		} else {
			return 0;
		}
	}

	public void setValue(Integer value) {
		if (value != null) {
			element.setAttribute(Xml.KEY_VALUE, Integer.toString(value));
		} else
			element.removeAttribute(Xml.KEY_VALUE);

		hero.fireValueChangedEvent(this);
	}

	public Integer getReferenceValue() {
		return hero.getArmorRs(getPosition());
	}

	public int getMinimum() {
		return 0;
	}

	public int getMaximum() {
		return 10;
	}

}
