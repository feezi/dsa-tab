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
package com.dsatab.view;

import com.dsatab.data.Markable;

/**
 * 
 * 
 */
public class FightFilterSettings implements FilterSettings {

	private boolean showArmor, showModifier, showEvade;

	/**
	 * Basic constructor
	 */
	public FightFilterSettings() {

	}

	public FightFilterSettings(boolean armor, boolean modifier, boolean evade) {
		this.showModifier = modifier;
		this.showArmor = armor;
		this.showEvade = evade;
	}

	public boolean isShowArmor() {
		return showArmor;
	}

	public void setShowArmor(boolean showNormal) {
		this.showArmor = showNormal;
	}

	public boolean isShowModifier() {
		return showModifier;
	}

	public void setShowModifier(boolean showFavorite) {
		this.showModifier = showFavorite;
	}

	public boolean isShowEvade() {
		return showEvade;
	}

	public void setShowEvade(boolean showUnused) {
		this.showEvade = showUnused;
	}

	public boolean equals(FightFilterSettings settings) {
		return equals(settings.isShowArmor(), settings.isShowModifier(), settings.isShowEvade());
	}

	public boolean equals(boolean showArmor, boolean showModifier, boolean showEvade) {
		return this.showModifier == showModifier && this.showArmor == showArmor && this.showEvade == showEvade;
	}

	public boolean isAllVisible() {
		return showModifier && showArmor && showEvade;
	}

	public boolean isVisible(Markable mark) {
		return (showModifier && mark.isFavorite()) || (showEvade && mark.isUnused())
				|| (showArmor && !mark.isFavorite() && !mark.isUnused());
	}

	public void set(FightFilterSettings settings) {
		set(settings.isShowArmor(), settings.isShowModifier(), settings.isShowEvade());
	}

	/**
	 * @param checked
	 * @param checked2
	 * @param checked3
	 */
	public void set(boolean armor, boolean modifer, boolean evade) {
		this.showModifier = modifer;
		this.showArmor = armor;
		this.showEvade = evade;

	}

}
