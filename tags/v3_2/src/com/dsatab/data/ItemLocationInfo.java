/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.data;

import java.io.Serializable;

import org.jdom.Element;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

public class ItemLocationInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = -7504593992133518605L;

	public static final int INVALID_POSITION = -1;

	private Element element = null;

	private int screen = INVALID_POSITION;

	/**
	 * Indicates the position of the associated cell.
	 */
	private int cellNumber = INVALID_POSITION;

	/**
	 * 
	 */
	public ItemLocationInfo() {

	}

	public int getScreen() {
		return screen;
	}

	public void setScreen(int screen) {
		this.screen = screen;
		if (element != null)
			element.setAttribute(Xml.KEY_SCREEN, Util.toString(screen));

	}

	public int getCellNumber() {
		return cellNumber;
	}

	public void setCellNumber(int cellX) {
		this.cellNumber = cellX;
		if (element != null)
			element.setAttribute(Xml.KEY_CELL_NUMBER, Util.toString(cellX));
	}

	public void setElement(Element element) {
		this.element = element;

		if (element == null)
			return;

		if (element.getAttribute(Xml.KEY_CELL_NUMBER) != null)
			setCellNumber(Util.parseInt(element.getAttributeValue(Xml.KEY_CELL_NUMBER)));
		if (element.getAttribute(Xml.KEY_SCREEN) != null) {
			// there is only one inventory screen left...
			int screen = Util.parseInt(element.getAttributeValue(Xml.KEY_SCREEN));
			if (screen > Hero.MAXIMUM_SET_NUMBER)
				screen = Hero.MAXIMUM_SET_NUMBER;
			setScreen(screen);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return cellNumber + " on " + screen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (o == null)
			return false;

		if (!o.getClass().equals(this.getClass()))
			return false;

		ItemLocationInfo i = (ItemLocationInfo) o;

		return i.cellNumber == cellNumber && i.screen == screen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
