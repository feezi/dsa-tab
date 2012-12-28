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
package com.dsatab.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.EquippedItemListAdapter;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.ItemChooserDialog;
import com.dsatab.xml.DataManager;

public class ItemsListFragment extends BaseListFragment implements OnItemClickListener,
		DialogInterface.OnMultiChoiceClickListener {

	private static final int ACTION_CHOOSE_CARD = 2;
	private static final int ACTION_SHOW_CARD = 1;

	private ListView itemList;

	private EquippedItemListAdapter itemAdapter;

	private Item selectedItem;

	private ItemChooserDialog itemChooserDialog;

	private Set<ItemType> categoriesSelected;
	private ItemType[] categories;

	private final class ItemsActionMode implements ActionMode.Callback {
		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			boolean notifyChanged = false;

			SparseBooleanArray checkedPositions = itemList.getCheckedItemPositions();
			if (checkedPositions != null) {
				for (int i = checkedPositions.size() - 1; i >= 0; i--) {
					if (checkedPositions.valueAt(i)) {
						Item selectedItem = itemAdapter.getItem(checkedPositions.keyAt(i));
						switch (item.getItemId()) {
						case R.id.option_delete:
							getHero().removeItem(selectedItem);
							notifyChanged = false;
							break;
						case R.id.option_view:
							selectItem(selectedItem);
							mode.finish();
							return true;
						case R.id.option_equipped:
							return false;
						case R.id.option_equipped_set1:
							getHero().addEquippedItem(getActivity(), selectedItem, null, null, 0, null);
							break;
						case R.id.option_equipped_set2:
							getHero().addEquippedItem(getActivity(), selectedItem, null, null, 1, null);
							break;
						case R.id.option_equipped_set3:
							getHero().addEquippedItem(getActivity(), selectedItem, null, null, 2, null);
							break;
						}

					}

				}
				if (notifyChanged) {
					itemAdapter.notifyDataSetChanged();
				}
			}
			mode.finish();
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.item_list_popupmenu, menu);
			mode.setTitle("Ausrüstung");
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
			itemList.clearChoices();
			itemAdapter.notifyDataSetChanged();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.actionbarsherlock.view.ActionMode.Callback#onPrepareActionMode
		 * (com.actionbarsherlock.view.ActionMode,
		 * com.actionbarsherlock.view.Menu)
		 */
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			SparseBooleanArray checkedPositions = itemList.getCheckedItemPositions();
			int selected = 0;
			boolean hasImage = false;
			boolean isEquippable = true;
			boolean changed = false;
			com.actionbarsherlock.view.MenuItem view = menu.findItem(R.id.option_view);
			com.actionbarsherlock.view.MenuItem equipped = menu.findItem(R.id.option_equipped);
			if (checkedPositions != null) {
				for (int i = checkedPositions.size() - 1; i >= 0; i--) {
					if (checkedPositions.valueAt(i)) {
						Item selectedItem = itemAdapter.getItem(checkedPositions.keyAt(i));
						selected++;
						hasImage |= selectedItem.hasImage();
						isEquippable &= selectedItem.isEquipable();
					}
				}
			}

			mode.setSubtitle(selected + " ausgewählt");

			if (selected == 1 && hasImage) {
				if (!view.isEnabled()) {
					view.setEnabled(true);
					changed = true;
				}
			} else {
				if (view.isEnabled()) {
					view.setEnabled(false);
					changed = true;
				}
			}

			if (isEquippable) {
				if (!equipped.isEnabled()) {
					equipped.setEnabled(true);
					changed = true;
				}
			} else {
				if (equipped.isEnabled()) {
					equipped.setEnabled(false);
					changed = true;
				}
			}
			return changed;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {
		fillBodyItems(hero);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ACTION_CHOOSE_CARD && resultCode == Activity.RESULT_OK) {

			Item item = null;

			UUID id = (UUID) data.getSerializableExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_ID);
			String cardName = data.getStringExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_NAME);

			Hero hero = getHero();

			if (id != null) {
				item = hero.getItem(id);
			}
			if (item == null && !TextUtils.isEmpty(cardName)) {

				// on a set page check whether he already has the item and reuse
				// it
				// if (getActiveSet() >= 0) {
				// item = hero.getItem(cardName);
				// }
				if (item == null) {
					Item card = DataManager.getItemByName(cardName);
					item = (Item) card.duplicate();
				}
			}

			if (item != null) {

				if (selectedItem != null && selectedItem.getItem().equals(item.getName())) {
					// the icon is already in the screen no need to add it
					// again
				} else {
					hero.addItem(getBaseActivity(), item, null, -1, null);
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mCallback = new ItemsActionMode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.item_list_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.
	 * actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		if (item.getItemId() == R.id.option_item_add_list) {
			showItemPopup();
			return true;
		} else if (item.getItemId() == R.id.option_item_filter) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			String[] categoryNames = new String[categories.length];
			boolean[] categoriesSet = new boolean[categories.length];

			for (int i = 0; i < categories.length; i++) {
				categoryNames[i] = categories[i].name();
				if (categoriesSelected.contains(categories[i]))
					categoriesSet[i] = true;
			}

			builder.setMultiChoiceItems(categoryNames, categoriesSet, this);
			builder.setTitle("Filtern");
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show().setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					itemAdapter.filter(new ArrayList<ItemType>(categoriesSelected), null, null);
				}
			});
			return true;

		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return configureContainerView(inflater.inflate(R.layout.sheet_items_list, container, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		itemList = (ListView) findViewById(android.R.id.list);
		itemList.setOnItemClickListener(this);
		itemList.setOnItemLongClickListener(this);
		itemList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		categories = ItemType.values();
		categoriesSelected = new HashSet<ItemType>(Arrays.asList(categories));

		super.onActivityCreated(savedInstanceState);
	}

	private void fillBodyItems(Hero hero) {
		itemAdapter = new EquippedItemListAdapter(getActivity(), getHero(), getHero().getItems());
		itemList.setAdapter(itemAdapter);
		refreshEmptyView(itemAdapter);
	}

	private void selectItem(Item itemCard) {
		selectedItem = itemCard;
		if (selectedItem != null) {
			Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			Item item = selectedItem.getItem();
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_ID, item.getId());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_NAME, item.getName());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CATEGORY, item.getCategory());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CELL, itemCard.getItemInfo().getCellNumber());

			getActivity().startActivityForResult(intent, ACTION_SHOW_CARD);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mMode == null) {
			itemList.setItemChecked(position, false);
		} else {
			super.onItemClick(parent, view, position, id);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnMultiChoiceClickListener#onClick(android
	 * .content.DialogInterface, int, boolean)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (isChecked)
			categoriesSelected.add(categories[which]);
		else
			categoriesSelected.remove(categories[which]);
	}

	private void showItemPopup() {
		if (itemChooserDialog == null) {
			itemChooserDialog = new ItemChooserDialog(getActivity());
			itemChooserDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * android.widget.AdapterView.OnItemClickListener#onItemClick
				 * (android.widget.AdapterView, android.view.View, int, long)
				 */
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Item item = itemChooserDialog.getItem(position);

					if (item != null) {
						item = item.duplicate();
						getHero().addItem(item);
					}
					itemChooserDialog.dismiss();
				}
			});
		}

		if (itemAdapter.getFilter().getTypes() != null && !itemAdapter.getFilter().getTypes().isEmpty()) {
			itemChooserDialog.setItemTypes(itemAdapter.getFilter().getTypes());
		}

		itemChooserDialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemAdded(com.dsatab
	 * .data.items.Item)
	 */
	@Override
	public void onItemAdded(Item item) {
		itemAdapter.add(item);
		refreshEmptyView(itemAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemChanged(com.dsatab
	 * .data.items.EquippedItem)
	 */
	@Override
	public void onItemChanged(EquippedItem item) {
		itemAdapter.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemRemoved(com.dsatab
	 * .data.items.Item)
	 */
	@Override
	public void onItemRemoved(Item item) {
		itemAdapter.remove(item);
		refreshEmptyView(itemAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemEquipped(com.
	 * dsatab.data.items.EquippedItem)
	 */
	@Override
	public void onItemEquipped(EquippedItem item) {
		itemAdapter.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.InventoryChangedListener#onItemUnequipped(com
	 * .dsatab.data.items.EquippedItem)
	 */
	@Override
	public void onItemUnequipped(EquippedItem item) {
		itemAdapter.notifyDataSetChanged();
	}

}
