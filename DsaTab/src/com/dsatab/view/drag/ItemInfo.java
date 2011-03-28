/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dsatab.view.drag;

import java.io.Serializable;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7504593992133518605L;

	public static final int INVALID_POSITION = -1;

	private Element element = null;

	/**
	 * Iindicates the screen in which the shortcut appears.
	 */
	private int screen = INVALID_POSITION;

	/**
	 * Indicates the X position of the associated cell.
	 */
	private int cellX = INVALID_POSITION;

	/**
	 * Indicates the Y position of the associated cell.
	 */
	private int cellY = INVALID_POSITION;

	/**
	 * 
	 */
	public ItemInfo() {

	}

	public int getScreen() {
		return screen;
	}

	public void setScreen(int screen) {
		this.screen = screen;
		element.setAttribute(Xml.KEY_SCREEN, Util.toString(screen));

	}

	public int getCellX() {
		return cellX;
	}

	public void setCellX(int cellX) {
		this.cellX = cellX;
		if (element != null)
			element.setAttribute(Xml.KEY_CELL_X, Util.toString(cellX));
	}

	public int getCellY() {
		return cellY;
	}

	public void setCellY(int cellY) {
		this.cellY = cellY;
		if (element != null)
			element.setAttribute(Xml.KEY_CELL_Y, Util.toString(cellY));
	}

	public int getSpanX() {
		return 1;
	}

	public void setSpanX(int spanX) {
		// this.spanX = spanX;
		element.setAttribute(Xml.KEY_SPAN_X, Util.toString(spanX));
	}

	public int getSpanY() {
		return 1;
	}

	public void setSpanY(int spanY) {
		// this.spanY = spanY;
		element.setAttribute(Xml.KEY_SPAN_Y, Util.toString(spanY));
	}

	public void setElement(Element element) {
		this.element = element;

		if (element == null)
			return;

		if (element.hasAttribute(Xml.KEY_CELL_X))
			setCellX(Util.parseInt(element.getAttribute(Xml.KEY_CELL_X)));
		if (element.hasAttribute(Xml.KEY_CELL_Y))
			setCellY(Util.parseInt(element.getAttribute(Xml.KEY_CELL_Y)));
		if (element.hasAttribute(Xml.KEY_SCREEN))
			setScreen(Util.parseInt(element.getAttribute(Xml.KEY_SCREEN)));
		if (element.hasAttribute(Xml.KEY_SPAN_X))
			setSpanX(Util.parseInt(element.getAttribute(Xml.KEY_SPAN_X)));
		if (element.hasAttribute(Xml.KEY_SPAN_Y))
			setSpanY(Util.parseInt(element.getAttribute(Xml.KEY_SPAN_Y)));
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

		ItemInfo i = (ItemInfo) o;

		return i.cellX == cellX && i.cellY == cellY && i.screen == screen && i.getSpanX() == getSpanX()
				&& i.getSpanY() == getSpanY();
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
