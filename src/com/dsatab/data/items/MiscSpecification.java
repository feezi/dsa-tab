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

import com.dsatab.R;

/**
 * 
 * 
 */
public class MiscSpecification extends ItemSpecification {

	/**
	 * @param item
	 */
	public MiscSpecification(Item item, ItemType type) {
		super(item, type, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#getName()
	 */
	@Override
	public String getName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#getInfo()
	 */
	@Override
	public String getInfo() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.data.items.ItemSpecification#setElement(org.jdom.Element)
	 */
	@Override
	public void setElement(Element element) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#getResourceId()
	 */
	@Override
	public int getResourceId() {
		switch (type) {
		case Behälter:
			return R.drawable.icon_bags;
		case Schmuck:
			return R.drawable.icon_special;
		case Kleidung:
			return R.drawable.icon_armor_cloth;
		case Sonstiges:
			return R.drawable.icon_misc;
		default:
			return R.drawable.icon_other;
		}
	}

}
