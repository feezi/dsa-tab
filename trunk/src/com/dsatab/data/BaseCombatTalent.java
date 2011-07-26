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

import org.jdom.Element;

/**
 * 
 * 
 */
public abstract class BaseCombatTalent extends Talent implements CombatTalent {

	public static final Comparator<BaseCombatTalent> NAME_COMPARATOR = new Comparator<BaseCombatTalent>() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(BaseCombatTalent object1, BaseCombatTalent object2) {
			return object1.getName().compareTo(object2.getName());
		}

	};

	protected Element combatElement;

	/**
	 * 
	 */
	public BaseCombatTalent(Hero hero, Element element, Element combatElement) {
		super(hero, element);
		this.combatElement = combatElement;
	}

	public abstract String getBe();

}
