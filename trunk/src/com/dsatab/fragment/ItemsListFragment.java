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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.EquippedItemListAdapter;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.ItemChooserDialog;
import com.dsatab.xml.DataManager;

public class ItemsListFragment extends BaseFragment implements OnItemClickListener,
		DialogInterface.OnMultiChoiceClickListener {

	private static final int GROUP_INVENTORY = 1;
	private static final int ACTION_CHOOSE_CARD = 2;
	private static final int ACTION_SHOW_CARD = 1;

	private static final int CONTEXTMENU_REMOVE = 10;
	private static final int CONTEXTMENU_SHOW = 11;

	private ListView itemList;

	private EquippedItemListAdapter itemAdpater;

	private Item selectedItem;

	private ItemChooserDialog itemChooserDialog;

	private Set<ItemType> categoriesSelected;
	private ItemType[] categories;

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

					hero.addItem(getBaseActivity(), item, null, -1, new Hero.ItemAddedCallback() {

						@Override
						public void onItemAdded(Item item) {
							itemAdpater.add(item);
							itemAdpater.notifyDataSetChanged();
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see com.dsatab.data.Hero.ItemAddedCallback#
						 * onEquippedItemAdded
						 * (com.dsatab.data.items.EquippedItem)
						 */
						@Override
						public void onEquippedItemAdded(EquippedItem item) {
							itemAdpater.notifyDataSetChanged();
						}
					});

				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == GROUP_INVENTORY) {
			switch (item.getItemId()) {
			case CONTEXTMENU_REMOVE:
				if (selectedItem != null) {
					itemAdpater.remove(selectedItem);
					getHero().removeItem(selectedItem);
					selectedItem = null;
				}
				return true;

			case CONTEXTMENU_SHOW:
				if (selectedItem != null) {
					selectItem(selectedItem);
					selectedItem = null;
				}
				return true;
			}
		}
		return super.onContextItemSelected(item);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_item_add, Menu.NONE,
				"Gegenstand hinzufügen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		item.setIcon(R.drawable.ic_menu_add);

		item = menu.add(Menu.NONE, R.id.option_item_filter, Menu.NONE, "Filtern");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		item.setIcon(R.drawable.ic_menu_filter);

		super.onCreateOptionsMenu(menu, inflater);
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
		if (item.getItemId() == R.id.option_item_add) {
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
			builder.setIcon(R.drawable.ic_menu_filter);

			builder.show().setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					itemAdpater.filter(new ArrayList<ItemType>(categoriesSelected), null, null);
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
	 * android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu
	 * , android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		if (v == itemList && menuInfo instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			Item item = itemAdpater.getItem(info.position);

			selectedItem = item;

			if (selectedItem.hasImage()) {
				menu.add(GROUP_INVENTORY, CONTEXTMENU_SHOW, 0, getString(R.string.menu_view_item));
			}
			menu.add(GROUP_INVENTORY, CONTEXTMENU_REMOVE, 1, getString(R.string.menu_delete_item));

		}
		super.onCreateContextMenu(menu, v, menuInfo);
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
		registerForContextMenu(itemList);

		categories = ItemType.values();
		categoriesSelected = new HashSet<ItemType>(Arrays.asList(categories));

		super.onActivityCreated(savedInstanceState);
	}

	private void fillBodyItems(Hero hero) {

		// has to be called to fill equippeditems of all items
		getHero().getAllEquippedItems();

		itemAdpater = new EquippedItemListAdapter(getActivity(), getHero(), getHero().getItems());
		itemList.setAdapter(itemAdpater);

	}

	private void selectItem(Item itemCard) {
		selectedItem = itemCard;
		if (selectedItem != null) {
			Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
			Item item = selectedItem.getItem();
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_ID, item.getId());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_NAME, item.getName());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CATEGORY, item.getCategory());

			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ITEM_CELL, itemCard.getItemInfo().getCellNumber());

			startActivityForResult(intent, ACTION_SHOW_CARD);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		parent.showContextMenu();
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
			itemChooserDialog = new ItemChooserDialog(getActivity(), DSATabApplication.getInstance().getHero());
			itemChooserDialog.setShowOwnItems(false);

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
					Item item = (Item) parent.getAdapter().getItem(position);

					if (item != null) {
						item = item.duplicate();
						getHero().addItem(getActivity(), item, null);
						itemAdpater.add(item);
					}

					itemChooserDialog.dismiss();
				}
			});
		}

		if (itemAdpater.getFilter().getTypes() != null && !itemAdpater.getFilter().getTypes().isEmpty()) {
			itemChooserDialog.setItemTypes(itemAdpater.getFilter().getTypes());
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
		itemAdpater.refilter();
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
		itemAdpater.notifyDataSetChanged();
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
		itemAdpater.refilter();
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
		itemAdpater.notifyDataSetChanged();
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
		itemAdpater.notifyDataSetChanged();
	}

}
