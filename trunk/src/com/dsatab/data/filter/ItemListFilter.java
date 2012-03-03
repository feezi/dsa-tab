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

import java.util.Arrays;
import java.util.List;

import android.text.TextUtils;

import com.dsatab.data.adapter.OpenArrayAdapter;
import com.dsatab.data.adapter.OpenFilter;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.ItemType;

/**
 * @author Ganymede
 * 
 */
public class ItemListFilter extends OpenFilter<Item> {

	private List<ItemType> types;

	private String category;

	/**
	 * 
	 */
	public ItemListFilter(OpenArrayAdapter<Item> list) {
		super(list);
	}

	public List<ItemType> getTypes() {
		return types;
	}

	public void setTypes(List<ItemType> type) {
		this.types = type;
	}

	public void setType(ItemType type) {
		if (type != null)
			this.types = Arrays.asList(type);
		else
			this.types = null;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	protected boolean isFilterSet() {
		return constraint != null || (types != null && !types.isEmpty()) || category != null;
	}

	public boolean filter(Item m) {
		boolean valid = true;
		if (types != null) {
			boolean found = false;

			for (ItemSpecification spec : m.getSpecifications()) {
				if (types.contains(spec.getType())) {
					found = true;
					break;
				}
			}

			valid &= found;
		}

		if (!TextUtils.isEmpty(category)) {
			valid &= category.equals(m.getCategory());
		}

		if (constraint != null) {
			valid &= m.getName().toLowerCase().startsWith(constraint);
		}

		return valid;
	}

}
