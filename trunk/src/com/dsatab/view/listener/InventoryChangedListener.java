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
package com.dsatab.view.listener;

import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;

public interface InventoryChangedListener {
	public void onItemAdded(Item item);

	public void onItemRemoved(Item item);

	public void onItemEquipped(EquippedItem item);

	public void onItemUnequipped(EquippedItem item);
}
