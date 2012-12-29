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

/**
 * @author Ganymede
 * 
 */
public class BeidhaendigerKampf {

	private EquippedItem item1;
	private EquippedItem item2;

	private String name;

	private int set;

	/**
	 * 
	 */
	public BeidhaendigerKampf(EquippedItem item1, EquippedItem item2) {
		this.item1 = item1;
		this.item2 = item2;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSet() {
		return set;
	}

	public void setSet(int set) {
		this.set = set;
	}

	public EquippedItem getItem1() {
		return item1;
	}

	public EquippedItem getItem2() {
		return item2;
	}

}
