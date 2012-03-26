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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.adapter.GalleryImageAdapter;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.CardView;
import com.dsatab.view.FastAnimationSet;
import com.dsatab.view.FastTranslateAnimation;
import com.dsatab.view.ItemChooserDialog;
import com.dsatab.view.ItemListItem;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.util.Debug;

public class ItemChooserFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener {

	public static final String INTENT_EXTRA_ITEM_CELL = "itemCell";
	public static final String INTENT_EXTRA_ITEM_NAME = "itemName";
	public static final String INTENT_EXTRA_ITEM_ID = "itemID";
	public static final String INTENT_EXTRA_EQUIPPED_ITEM_ID = "equippedItemID";
	public static final String INTENT_EXTRA_ITEM_TYPE = "itemType";
	public static final String INTENT_EXTRA_ITEM_CATEGORY = "itemCategory";
	public static final String INTENT_EXTRA_ITEM = "item";
	public static final String INTENT_EXTRA_SEARCHABLE = "searchable";
	public static final String INTENT_EXTRA_CATEGORY_SELECTABLE = "categorySelectale";

	public static final String INTENT_EXTRA_ARMOR_POSITION = "position";

	private Gallery gallery;
	private CardView imageView;
	private ItemListItem itemView;

	private ImageButton[] categoryButtons;

	private ImageButton searchButton;
	private View searchContainer;
	private AutoCompleteTextView searchText;

	private int cellNumber;

	private GalleryImageAdapter imageAdapter;

	private ItemChooserDialog itemChooserDialog;

	private Item selectedCard = null;
	private Item foundItem = null;

	private ItemSpecification selectedItemSpecification = null;

	private ItemType cardType;

	private AnimationSet mInAnimation;
	private AnimationSet mOutAnimation;
	private Animation mHandleInAnimation;
	private Animation mHandleOutAnimation;

	private static final int ORIENTATION_HORIZONTAL = 1;
	private static final int ANIMATION_DURATION = 200;

	boolean categorySelectable = true;
	boolean searchable = true;

	private OnItemChooserListener onItemChooserListener;

	private Drawable itemDrawable;

	private DataSetObserver dataSetObserver;

	public interface OnItemChooserListener {
		public void onItemSelected(Item item, int cellNumber);

		public void onItemCanceled();
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
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sheet_item, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnItemChooserListener) {
			onItemChooserListener = (OnItemChooserListener) activity;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		onItemChooserListener = null;
		imageAdapter.unregisterDataSetObserver(dataSetObserver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		String itemCategory = null;

		Bundle extra = getActivity().getIntent().getExtras();
		if (extra != null) {
			foundItem = (Item) extra.getSerializable(INTENT_EXTRA_ITEM);

			cellNumber = extra.getInt(INTENT_EXTRA_ITEM_CELL, ItemLocationInfo.INVALID_POSITION);
			categorySelectable = extra.getBoolean(INTENT_EXTRA_CATEGORY_SELECTABLE, true);
			searchable = extra.getBoolean(INTENT_EXTRA_SEARCHABLE, true);

			if (foundItem == null) {
				String itemName = extra.getString(INTENT_EXTRA_ITEM_NAME);
				UUID itemId = (UUID) extra.getSerializable(INTENT_EXTRA_ITEM_ID);
				UUID equippedItemId = (UUID) extra.getSerializable(INTENT_EXTRA_EQUIPPED_ITEM_ID);

				String itemType = extra.getString(INTENT_EXTRA_ITEM_TYPE);
				itemCategory = extra.getString(INTENT_EXTRA_ITEM_CATEGORY);

				cardType = ItemType.Waffen;
				if (itemType != null) {
					cardType = ItemType.valueOf(itemType);
				}

				if (itemId != null) {
					Hero hero = DSATabApplication.getInstance().getHero();
					foundItem = hero.getItem(itemId);
				}

				if (equippedItemId != null) {
					Hero hero = DSATabApplication.getInstance().getHero();
					EquippedItem selectedEquippedItem = hero.getEquippedItem(equippedItemId);
					foundItem = selectedEquippedItem.getItem();
					selectedItemSpecification = selectedEquippedItem.getItemSpecification();
				}
				if (foundItem == null && !TextUtils.isEmpty(itemName)) {
					foundItem = DataManager.getItemByName(itemName);
				}

				if (foundItem != null) {
					if (selectedItemSpecification == null) {
						selectedItemSpecification = foundItem.getSpecifications().get(0);
					}

					cardType = selectedItemSpecification.getType();
					itemCategory = foundItem.getCategory();
					Debug.verbose("Displaying " + itemName + " " + cardType + "/" + itemCategory + " : " + foundItem);
				}
			} else {
				if (selectedItemSpecification == null) {
					selectedItemSpecification = foundItem.getSpecifications().get(0);
				}
				cardType = selectedItemSpecification.getType();
				itemCategory = foundItem.getCategory();
			}
		}

		gallery = (Gallery) findViewById(R.id.gal_gallery);
		imageView = (CardView) findViewById(R.id.gal_imageView);
		imageView.setHighQuality(true);
		itemView = (ItemListItem) findViewById(R.id.inc_gal_item_view);
		itemView.setTextColor(Color.BLACK);
		itemView.setBackgroundColor(getResources().getColor(R.color.Brighter));

		imageView.setOnClickListener(this);
		imageView.setOnLongClickListener(this);

		if (extra != null && extra.containsKey(INTENT_EXTRA_ARMOR_POSITION)) {
			Position pos = (Position) extra.getSerializable(INTENT_EXTRA_ARMOR_POSITION);
			Hero hero = DSATabApplication.getInstance().getHero();

			List<EquippedItem> equippedItems = hero.getArmor(pos);
			List<Item> items = new ArrayList<Item>(equippedItems.size());
			for (int i = 0; i < equippedItems.size(); i++) {
				items.add(equippedItems.get(i).getItem());
			}
			imageAdapter = new GalleryImageAdapter(getActivity(), items);
		} else {

			if (searchable) {
				imageAdapter = new GalleryImageAdapter(getActivity(), DataManager.getItems());
				imageAdapter.filter(cardType, itemCategory, null);
			} else {
				if (foundItem != null)
					imageAdapter = new GalleryImageAdapter(getActivity(), Arrays.asList(foundItem));
				else
					imageAdapter = new GalleryImageAdapter(getActivity(), new ArrayList<Item>(0));
			}
		}
		gallery.setAdapter(imageAdapter);
		gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Item card = (Item) gallery.getItemAtPosition(position);
				foundItem = null;
				showCard(card, null, false);
			}
		});

		if (categorySelectable) {
			gallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					showItemChooserPopup();
					return true;
				}
			});
		}

		if (imageAdapter.getCount() == 0) {
			Toast.makeText(getActivity(), "Keine Einträge gefunden", Toast.LENGTH_SHORT).show();
			cancel();
		} else {
			int index = 0;
			if (foundItem != null) {
				index = imageAdapter.getPositionByName(foundItem);
			}
			Debug.verbose("Showing index " + index);
			gallery.setSelection(index, false);

		}

		dataSetObserver = new DataSetObserver() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.database.DataSetObserver#onChanged()
			 */
			@Override
			public void onChanged() {
				if (foundItem == null) {
					Item item = null;
					if (imageAdapter.getCount() > 0)
						item = imageAdapter.getItem(0);

					if (item != null) {
						showCard(item, null, true);
					}
				} else {
					int index = imageAdapter.getPosition(foundItem);
					if (index >= 0)
						gallery.setSelection(index, false);
				}
			}
		};

		imageAdapter.registerDataSetObserver(dataSetObserver);

		categoryButtons = new ImageButton[8];
		// imagebuttons
		ImageButton weaponButton = (ImageButton) findViewById(R.id.body_attack_button);
		ImageButton shieldButton = (ImageButton) findViewById(R.id.body_defense_button);
		ImageButton distanceButton = (ImageButton) findViewById(R.id.body_distance_button);
		ImageButton armorButton = (ImageButton) findViewById(R.id.body_armor_button);
		ImageButton itemsButton = (ImageButton) findViewById(R.id.body_items_button);
		ImageButton clothButton = (ImageButton) findViewById(R.id.body_cloth_button);
		ImageButton specialButton = (ImageButton) findViewById(R.id.body_special_button);
		ImageButton bagsButton = (ImageButton) findViewById(R.id.body_bags_button);

		categoryButtons[0] = weaponButton;
		categoryButtons[1] = shieldButton;
		categoryButtons[2] = distanceButton;
		categoryButtons[3] = armorButton;
		categoryButtons[4] = itemsButton;
		categoryButtons[5] = clothButton;
		categoryButtons[6] = specialButton;
		categoryButtons[7] = bagsButton;

		searchButton = (ImageButton) findViewById(R.id.body_search_button);
		searchText = (AutoCompleteTextView) findViewById(R.id.body_autosearch);
		searchContainer = findViewById(R.id.body_search_container);

		if (categorySelectable) {

			for (ImageButton button : categoryButtons) {
				button.setOnClickListener(this);
				button.setOnLongClickListener(this);
			}

			weaponButton.setTag(ItemType.Waffen);
			shieldButton.setTag(ItemType.Schilde);
			distanceButton.setTag(ItemType.Fernwaffen);
			armorButton.setTag(ItemType.Rüstung);
			itemsButton.setTag(ItemType.Sonstiges);
			clothButton.setTag(ItemType.Kleidung);
			specialButton.setTag(ItemType.Schmuck);
			bagsButton.setTag(ItemType.Behälter);

			searchButton.setOnClickListener(this);
			searchContainer.setVisibility(View.GONE);

			List<String> itemNames = new ArrayList<String>(DataManager.getItemsMap().keySet());
			Collections.sort(itemNames);

			final ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_dropdown_item_1line, itemNames);
			searchText.setAdapter(arrAdapter);
			createAnimations();

			searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						closeSearch();
					}
				}
			});
			searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					searchText.clearFocus();

					Item item = DataManager.getItemByName(arrAdapter.getItem(position));
					if (item != null) {

						Util.hideKeyboard(searchText);
						chooseType(item.getSpecifications().get(0).getType(), item.getCategory(), item);
					}
				}
			});

		} else {
			for (ImageButton button : categoryButtons) {
				button.setVisibility(View.GONE);
			}

			searchButton.setVisibility(View.GONE);
			searchContainer.setVisibility(View.GONE);
		}

		if (searchable && imageAdapter.getCount() > 1) {
			gallery.setVisibility(View.VISIBLE);
		} else {
			gallery.setVisibility(View.GONE);
		}

		if (foundItem != null)
			showCard(foundItem, selectedItemSpecification, true);
		else if (gallery.getSelectedItem() != null) {
			showCard((Item) gallery.getSelectedItem(), null, true);
		}

		super.onActivityCreated(savedInstanceState);
	}

	public OnItemChooserListener getOnItemChooserListener() {
		return onItemChooserListener;
	}

	public void setOnItemChooserListener(OnItemChooserListener itemChooserListener) {
		this.onItemChooserListener = itemChooserListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {

	}

	private void createAnimations() {
		int mOrientation = ORIENTATION_HORIZONTAL;

		if (mInAnimation == null) {
			mInAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mInAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));

			if (mOrientation == ORIENTATION_HORIZONTAL) {
				animationSet.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
						Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f));
			} else {
				animationSet.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
						Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f));
			}
			animationSet.setDuration(ANIMATION_DURATION);
		}
		if (mHandleInAnimation == null) {
			mHandleInAnimation = new AlphaAnimation(0.0f, 1.0f);
			mHandleInAnimation.setDuration(ANIMATION_DURATION);
		}
		if (mOutAnimation == null) {
			mOutAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mOutAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
			if (mOrientation == ORIENTATION_HORIZONTAL) {
				animationSet.addAnimation(new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE,
						0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f));
			} else {
				animationSet.addAnimation(new FastTranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f));
			}
			animationSet.setDuration(ANIMATION_DURATION);
		}
		if (mHandleOutAnimation == null) {
			mHandleOutAnimation = new AlphaAnimation(1.0f, 0.0f);
			mHandleOutAnimation.setFillAfter(true);
			mHandleOutAnimation.setDuration(ANIMATION_DURATION);
		}
	}

	private void showItemChooserPopup() {
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
					Item selectedItem = (Item) parent.getAdapter().getItem(position);
					chooseType(selectedItem.getSpecifications().get(0).getType(), selectedItem.getCategory(),
							selectedItem);
					itemChooserDialog.dismiss();
				}
			});
		}

		itemChooserDialog.show(cardType);
	}

	private void toggleSearch() {
		if (searchContainer.getVisibility() == View.VISIBLE)
			closeSearch();
		else
			openSearch();
	}

	private void openSearch() {
		searchText.setText("");
		searchContainer.startAnimation(mInAnimation);
		searchContainer.setVisibility(View.VISIBLE);
		searchText.requestFocus();
		searchButton.setSelected(true);
	}

	private void closeSearch() {
		searchContainer.startAnimation(mOutAnimation);
		searchContainer.setVisibility(View.INVISIBLE);
		searchText.clearFocus();
		searchButton.setSelected(false);

		Util.hideKeyboard(searchText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		if (v == searchButton) {
			toggleSearch();
		} else if (v == imageView) {
			if (selectedCard != null) {
				selectCard(selectedCard);
			} else {
				Debug.verbose("Nothing Selected.");
				cancel();
			}
		} else if (v.getTag() instanceof ItemType) {
			ItemType cardType = (ItemType) v.getTag();
			chooseType(cardType, null, null);
		}

	}

	public boolean onLongClick(View v) {

		if (v == imageView) {
			if (selectedCard != null) {
				selectCard(selectedCard);
			} else {
				Debug.verbose("Nothing Selected.");
				cancel();
			}
			return true;
		} else if (v.getTag() instanceof ItemType) {
			ItemType cardType = (ItemType) v.getTag();
			openSubCategoriesDialog(cardType);
			return true;
		}
		return false;
	}

	private void selectCard(Item item) {
		if (onItemChooserListener != null)
			onItemChooserListener.onItemSelected(item, cellNumber);
	}

	private void cancel() {
		if (onItemChooserListener != null)
			onItemChooserListener.onItemCanceled();
	}

	private void showCard(Item card, ItemSpecification itemSpecification, boolean animate) {

		selectedCard = card;
		if (itemSpecification != null)
			selectedItemSpecification = itemSpecification;
		else
			selectedItemSpecification = selectedCard.getSpecifications().get(0);

		imageView.setItem(selectedCard);
		itemView.setItem(selectedCard, selectedItemSpecification);
		itemView.setVisibility(View.VISIBLE);

		if (animate) {
			int index = imageAdapter.getPosition(card);
			if (index >= 0)
				gallery.setSelection(index);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
	 * android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.accept_abort_menu, menu);
		if (categorySelectable) {
			inflater.inflate(R.menu.gallery_menu, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	private void chooseType(ItemType type, String category, Item item) {
		this.cardType = type;

		for (ImageButton button : categoryButtons) {
			button.setSelected(cardType.equals(button.getTag()));
		}

		foundItem = item;
		gallery.setVisibility(View.VISIBLE);
		imageAdapter.filter(cardType, category, null);

		if (foundItem != null)
			showCard(foundItem, null, true);
	}

	private void openSubCategoriesDialog(final ItemType cardType) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final String[] subCategories = DataManager.getCardCategories(cardType).toArray(new String[0]);
		builder.setItems(subCategories, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				chooseType(cardType, subCategories[which], null);
				dialog.dismiss();
			}
		});
		builder.setTitle("Unterkategorie auswählen");
		builder.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.option_load_subtype) {
			openSubCategoriesDialog(cardType);
			return true;
		} else if (item.getItemId() == R.id.option_accept) {
			if (selectedCard != null) {
				selectCard(selectedCard);
			}
			return true;
		} else if (item.getItemId() == R.id.option_cancel) {
			cancel();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
