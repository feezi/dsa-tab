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

import android.content.Context;

import com.flurry.android.FlurryAgent;

/**
 * 
 * 
 */
public class AnalyticsManager {

	public static final String PAGE_CHARACTER = "character";
	public static final String PAGE_TALENTS = "talents";
	public static final String PAGE_SPELLS = "spells";
	public static final String PAGE_ARMOR_WOUNDS = "armorWounds";
	public static final String PAGE_FIGHT = "fight";
	public static final String PAGE_PURSE = "purse";
	public static final String PAGE_MAP = "map";
	public static final String PAGE_NOTES = "notes";
	public static final String PAGE_ITEMS = "items";

	private static boolean enabled = true;

	public static void setEnabled(boolean b) {
		enabled = b;
		FlurryAgent.setLogEnabled(b);
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void startSession(Context context) {
		if (enabled)
			FlurryAgent.onStartSession(context, DSATabApplication.FLURRY_APP_ID);
	}

	public static void endSession(Context context) {
		if (enabled)
			FlurryAgent.onEndSession(context);
	}

	public static void onEvent(String event) {
		if (enabled)
			FlurryAgent.onEvent(event);
	}
}
