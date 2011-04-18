/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.data.enums;

import com.dsatab.R;

/**
 * @author Seraphim
 * 
 */
public enum EventCategory {
	Misc(R.drawable.m_icon_misc), Abenteuer(R.drawable.m_icon_abenteuer), Held(R.drawable.m_icon_held), Bekanntschaft(R.drawable.m_icon_bekanntschaft), Freundschaft(
			R.drawable.m_icon_freundschaft), Tat(R.drawable.m_icon_tat);

	private int iconId;

	private EventCategory(int drawableId) {
		this.iconId = drawableId;
	}

	public int getDrawableId() {
		return iconId;
	}

}
