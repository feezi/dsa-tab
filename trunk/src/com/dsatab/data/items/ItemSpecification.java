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

import org.jdom.Element;

public abstract class ItemSpecification {

	protected Item item;

	protected ItemType type;

	protected String specificationLabel;

	protected int version;

	public ItemSpecification(Item item, ItemType type, int version) {
		this.item = item;
		this.type = type;
		this.version = version;
	}

	public abstract String getInfo();

	public abstract String getName();

	public String getSpecificationLabel() {
		return specificationLabel;
	}

	public void setSpecificationLabel(String specificationLabel) {
		this.specificationLabel = specificationLabel;
	}

	public abstract void setElement(Element element);

	public abstract int getResourceId();

	public ItemType getType() {
		return type;
	}

	public int getVersion() {
		return version;
	}

	void setVersion(int version) {
		this.version = version;
	}

}
