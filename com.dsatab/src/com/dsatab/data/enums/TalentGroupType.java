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
package com.dsatab.data.enums;

import java.util.Arrays;

import com.dsatab.data.TalentGroup;

/**
 * @author Ganymede
 * 
 */
public enum TalentGroupType {
	Nahkampf(TalentGroup.NAHKAMPF_TALENTS), Fernkampf(TalentGroup.FERNKAMPF_TALENTS), Körperlich(
			TalentGroup.KÖRPER_TALENTS), Gesellschaft(TalentGroup.GESELLSCHAFT_TALENTS), Natur(
			TalentGroup.NATUR_TALENTS), Wissen(TalentGroup.WISSEN_TALENTS), Handwerk(TalentGroup.HANDWERK_TALENTS), Sprachen(
			TalentGroup.SPACHEN_TALENTS), Gaben(TalentGroup.GABEN_TALENTS), Meta(TalentGroup.META_TALENTS);

	private TalentType[] talents;

	private TalentGroupType(TalentType[] talents) {
		Arrays.sort(talents);
		this.talents = talents;
	}

	public boolean contains(TalentType talentName) {
		return Arrays.binarySearch(talents, talentName) >= 0;
	}
}