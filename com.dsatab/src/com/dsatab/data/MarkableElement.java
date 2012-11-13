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

import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public abstract class MarkableElement extends BaseProbe implements Markable {

	protected Element element;

	/**
	 * 
	 */
	public MarkableElement(Element element) {
		this.element = element;
	}

	public boolean isUnused() {
		if (element != null && element.getAttribute(Xml.KEY_UNUSED) != null) {
			return Boolean.valueOf(element.getAttributeValue(Xml.KEY_UNUSED));
		} else {
			return false;
		}
	}

	public boolean isFavorite() {
		if (element != null && element.getAttribute(Xml.KEY_FAVORITE) != null) {
			return Boolean.valueOf(element.getAttributeValue(Xml.KEY_FAVORITE));
		} else {
			return false;
		}
	}

	public void setFavorite(boolean value) {
		if (value)
			element.setAttribute(Xml.KEY_FAVORITE, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_FAVORITE);
	}

	public void setUnused(boolean value) {
		if (value)
			element.setAttribute(Xml.KEY_UNUSED, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_UNUSED);
	}
}
