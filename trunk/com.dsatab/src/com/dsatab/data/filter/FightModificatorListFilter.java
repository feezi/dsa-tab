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
package com.dsatab.data.filter;

import com.dsatab.data.adapter.OpenArrayAdapter;
import com.dsatab.data.adapter.OpenFilter;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.FightFilterSettings;

/**
 * @author Ganymede
 * 
 */
public class FightModificatorListFilter extends OpenFilter<Modificator> {

	private FightFilterSettings settings;

	/**
	 * 
	 */
	public FightModificatorListFilter(OpenArrayAdapter<Modificator> list) {
		super(list);
	}

	public FightFilterSettings getSettings() {
		return settings;
	}

	public void setSettings(FightFilterSettings settings) {
		this.settings = settings;
	}

	protected boolean isFilterSet() {
		return constraint != null || (settings != null && !settings.isAllVisible());
	}

	public boolean filter(Modificator m) {
		boolean valid = true;
		if (settings != null) {
			valid &= settings.isShowModifier();
		}

		if (constraint != null) {
			valid &= m.getModificatorName().toLowerCase().startsWith(constraint);
		}

		return valid;
	}

}
