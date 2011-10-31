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
public class ListFilterSettings implements FilterSettings {

	private boolean showNormal, showFavorite, showUnused;

	/**
	 * Basic constructor
	 */
	public ListFilterSettings() {

	}

	public ListFilterSettings(boolean fav, boolean normal, boolean unused) {
		this.showFavorite = fav;
		this.showNormal = normal;
		this.showUnused = unused;
	}

	public boolean isShowNormal() {
		return showNormal;
	}

	public void setShowNormal(boolean showNormal) {
		this.showNormal = showNormal;
	}

	public boolean isShowFavorite() {
		return showFavorite;
	}

	public void setShowFavorite(boolean showFavorite) {
		this.showFavorite = showFavorite;
	}

	public boolean isShowUnused() {
		return showUnused;
	}

	public void setShowUnused(boolean showUnused) {
		this.showUnused = showUnused;
	}

	public boolean equals(ListFilterSettings settings) {
		return equals(settings.isShowFavorite(), settings.isShowNormal(), settings.isShowUnused());
	}

	public boolean equals(boolean showFavorite, boolean showNormal, boolean showUnused) {
		return this.showFavorite == showFavorite && this.showNormal == showNormal && this.showUnused == showUnused;
	}

	public boolean isAllVisible() {
		return showFavorite && showNormal && showUnused;
	}

	public boolean isVisible(Markable mark) {
		return (showFavorite && mark.isFavorite()) || (showUnused && mark.isUnused())
				|| (showNormal && !mark.isFavorite() && !mark.isUnused());
	}

	public void set(ListFilterSettings settings) {
		set(settings.isShowFavorite(), settings.isShowNormal(), settings.isShowUnused());
	}

	/**
	 * @param checked
	 * @param checked2
	 * @param checked3
	 */
	public void set(boolean fav, boolean normal, boolean unused) {
		this.showFavorite = fav;
		this.showNormal = normal;
		this.showUnused = unused;

	}

}
