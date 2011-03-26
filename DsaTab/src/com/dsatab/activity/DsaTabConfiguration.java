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
package com.dsatab.activity;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dsatab.data.enums.Position;

/**
 *  
 */
public class DsaTabConfiguration {

	private Context context;

	public enum ArmorType {
		ZonenRuestung("Zonenrüstung"), GesamtRuestung("Gesamte Rüstung");

		private String title;

		private ArmorType(String t) {
			title = t;
		}

		public String title() {
			return title;
		}
	}

	public DsaTabConfiguration(Context context) {
		this.context = context;
	}

	public boolean isHouseRules() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(DsaPreferenceActivity.KEY_HOUSE_RULES, false);
	}

	public List<Position> getArmorPositions() {
		if (isHouseRules()) {
			return Position.ARMOR_POSITIONS_HOUSE;
		} else
			return Position.ARMOR_POSITIONS;
	}

	public List<Position> getWoundPositions() {
		if (isHouseRules()) {
			return Position.WOUND_POSITIONS;
		} else
			return Position.WOUND_POSITIONS;
	}

	public ArmorType getArmorType() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return ArmorType.valueOf(preferences.getString(DsaPreferenceActivity.KEY_ARMOR_TYPE,
				ArmorType.ZonenRuestung.name()));
	}
}
