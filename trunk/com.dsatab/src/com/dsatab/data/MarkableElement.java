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

import org.jdom2.Element;

import com.dsatab.xml.Xml;

/**
 * @author Ganymede
 * 
 */
public abstract class MarkableElement extends BaseProbe implements Markable, XmlWriteable {

	private boolean unused, favorite;

	public boolean isUnused() {
		return unused;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean value) {
		this.favorite = value;
		if (favorite)
			this.unused = false;
	}

	public void setUnused(boolean value) {
		this.unused = value;
		if (unused)
			this.favorite = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml()
	 */
	@Override
	public void populateXml(Element element) {
		if (favorite)
			element.setAttribute(Xml.KEY_FAVORITE, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_FAVORITE);

		if (unused)
			element.setAttribute(Xml.KEY_UNUSED, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_UNUSED);
	}

}
