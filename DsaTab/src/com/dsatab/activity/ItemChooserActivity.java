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
package com.dsatab.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.data.adapter.GalleryImageAdapter;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.FastAnimationSet;
import com.dsatab.view.FastTranslateAnimation;
import com.dsatab.view.ItemChooserDialog;
import com.dsatab.view.ItemListItem;
import com.dsatab.view.drag.ItemInfo;
import com.dsatab.xml.DataManager;
import com.gandulf.guilib.util.Debug;

public class ItemChooserActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

	public static final String INTENT_EXTRA_ITEM_X = "itemX";
	public static final String INTENT_EXTRA_ITEM_Y = "itemY";
	public static final String INTENT_EXTRA_ITEM_NAME = "itemName";
	public static final String INTENT_EXTRA_ITEM_TYPE = "itemType";
	public static final String INTENT_EXTRA_ITEM_CATEGORY = "itemCategory";
	public static final String INTENT_EXTRA_ITEM = "item";

	private Gallery gallery;
	private ImageView imageView;
	private ItemListItem itemView;

	private ImageButton searchButton;
	private AutoCompleteTextView searchText;

	private int itemX, itemY;

	private GalleryImageAdapter imageAdapter;

	private ItemChooserDialog itemChooserDialog;

	private Item selectedCard = null;

	private ItemType cardType;

	private AnimationSet mInAnimation;
	private AnimationSet mOutAnimation;
	private Animation mHandleInAnimation;
	private Animation mHandleOutAnimation;

	private static final int ORIENTATION_HORIZONTAL = 1;
	private static final int TRANSITION_DURATION = 250;
	private static final int ANIMATION_DURATION = 200;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.items_details);

		Item foundItem = null;
		String itemCategory;

		foundItem = (Item) getIntent().getSerializableExtra(INTENT_EXTRA_ITEM);
		itemX = getIntent().getIntExtra(INTENT_EXTRA_ITEM_X, ItemInfo.INVALID_POSITION);
		itemY = getIntent().getIntExtra(INTENT_EXTRA_ITEM_Y, ItemInfo.INVALID_POSITION);

		if (foundItem == null) {
			String itemName = getIntent().getStringExtra(INTENT_EXTRA_ITEM_NAME);
			String itemType = getIntent().getStringExtra(INTENT_EXTRA_ITEM_TYPE);
			itemCategory = getIntent().getStringExtra(INTENT_EXTRA_ITEM_CATEGORY);

			cardType = ItemType.Waffen;
			if (itemType != null) {
				cardType = ItemType.valueOf(itemType);
			}

			if (!TextUtils.isEmpty(itemName)) {
				foundItem = DataManager.getItemByName(itemName);
				if (foundItem != null) {
					cardType = foundItem.getType();
					itemCategory = foundItem.getCategory();
				}

				Debug.verbose("Displaying " + itemName + " " + cardType + "/" + itemCategory + " : " + foundItem);
			}
		} else {
			cardType = foundItem.getType();
			itemCategory = foundItem.getCategory();
		}

		gallery = (Gallery) findViewById(R.id.gal_gallery);
		imageView = (ImageView) findViewById(R.id.gal_imageView);
		itemView = (ItemListItem) findViewById(R.id.inc_gal_item_view);
		itemView.setTextColor(Color.BLACK);
		itemView.setBackgroundColor(getResources().getColor(R.color.Brighter));

		imageView.setOnClickListener(this);
		imageView.setOnLongClickListener(this);

		imageAdapter = new GalleryImageAdapter(this, cardType, itemCategory);
		gallery.setAdapter(imageAdapter);
		gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Item card = (Item) gallery.getItemAtPosition(position);
				showCard(card, false);
			}
		});

		gallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				showItemChooserPopup();
				return true;
			}
		});

		if (imageAdapter.getCount() == 0) {
			Toast.makeText(this, "Keine Einträge gefunden", Toast.LENGTH_SHORT).show();
			ItemChooserActivity.this.finish();
			return;
		} else {
			int index = 0;
			if (foundItem != null) {
				index = imageAdapter.getPosition(foundItem);
			}
			Debug.verbose("Showing index " + index);
			gallery.setSelection(index);
		}

		// imagebuttons
		ImageButton weaponButton = (ImageButton) findViewById(R.id.body_attack_button);
		ImageButton shieldButton = (ImageButton) findViewById(R.id.body_defense_button);
		ImageButton distanceButton = (ImageButton) findViewById(R.id.body_distance_button);
		ImageButton armorButton = (ImageButton) findViewById(R.id.body_armor_button);
		ImageButton itemsButton = (ImageButton) findViewById(R.id.body_items_button);
		ImageButton clothButton = (ImageButton) findViewById(R.id.body_cloth_button);
		ImageButton specialButton = (ImageButton) findViewById(R.id.body_special_button);
		ImageButton bagsButton = (ImageButton) findViewById(R.id.body_bags_button);

		weaponButton.setOnClickListener(this);
		weaponButton.setOnLongClickListener(this);
		weaponButton.setTag(ItemType.Waffen);
		shieldButton.setOnClickListener(this);
		shieldButton.setOnLongClickListener(this);
		shieldButton.setTag(ItemType.Schilde);
		distanceButton.setOnClickListener(this);
		distanceButton.setOnLongClickListener(this);
		distanceButton.setTag(ItemType.Fernwaffen);
		armorButton.setOnClickListener(this);
		armorButton.setOnLongClickListener(this);
		armorButton.setTag(ItemType.Rüstung);
		itemsButton.setOnClickListener(this);
		itemsButton.setOnLongClickListener(this);
		itemsButton.setTag(ItemType.Sonstiges);
		clothButton.setOnClickListener(this);
		clothButton.setOnLongClickListener(this);
		clothButton.setTag(ItemType.Kleidung);
		specialButton.setOnClickListener(this);
		specialButton.setOnLongClickListener(this);
		specialButton.setTag(ItemType.Special);
		bagsButton.setOnClickListener(this);
		bagsButton.setOnLongClickListener(this);
		bagsButton.setTag(ItemType.Behälter);

		Item card = (Item) gallery.getSelectedItem();
		if (card != null)
			showCard(card, true);

		searchButton = (ImageButton) findViewById(R.id.body_search_button);
		searchButton.setOnClickListener(this);
		searchText = (AutoCompleteTextView) findViewById(R.id.body_autosearch);
		searchText.setVisibility(View.GONE);

		List<String> itemNames = new ArrayList<String>(DataManager.getItemsMap().keySet());
		Collections.sort(itemNames);

		final ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(this,
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

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);

					chooseType(item.getType(), item.getCategory(), item);
				}
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {

		if (searchText.getVisibility() == View.VISIBLE) {
			closeSearch();
		} else {
			super.onBackPressed();
		}
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
			itemChooserDialog = new ItemChooserDialog(this);
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
					chooseType(selectedItem.getType(), selectedItem.getCategory(), selectedItem);
					itemChooserDialog.dismiss();
				}
			});
		}

		itemChooserDialog.show(cardType);
	}

	private void toggleSearch() {
		if (searchText.getVisibility() == View.VISIBLE)
			closeSearch();
		else
			openSearch();
	}

	private void openSearch() {
		searchText.setText("");
		searchText.startAnimation(mInAnimation);
		searchText.setVisibility(View.VISIBLE);
		searchText.requestFocus();
	}

	private void closeSearch() {
		searchText.startAnimation(mOutAnimation);
		searchText.setVisibility(View.INVISIBLE);
		searchText.clearFocus();
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
				setResult(RESULT_CANCELED);
			}
			finish();
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
				setResult(RESULT_CANCELED);
			}
			finish();
			return true;
		} else if (v.getTag() instanceof ItemType) {
			ItemType cardType = (ItemType) v.getTag();
			openSubCategoriesDialog(cardType);
			return true;
		}
		return false;
	}

	private void selectCard(Item card) {
		Debug.verbose("Selected " + card.getName());
		Intent intent = new Intent();
		intent.putExtra(INTENT_EXTRA_ITEM_TYPE, card.getType().name());
		intent.putExtra(INTENT_EXTRA_ITEM_NAME, card.getName());
		intent.putExtra(INTENT_EXTRA_ITEM_CATEGORY, card.getCategory());
		intent.putExtra(INTENT_EXTRA_ITEM, card);

		intent.putExtra(INTENT_EXTRA_ITEM_X, itemX);
		intent.putExtra(INTENT_EXTRA_ITEM_Y, itemY);

		setResult(RESULT_OK, intent);
	}

	private void showCard(Item card, boolean animate) {

		selectedCard = card;

		File hqFile = card.getHQFile();

		Bitmap bitmap = null;
		if (hqFile != null && hqFile.isFile()) {
			bitmap = BitmapFactory.decodeFile(hqFile.getAbsolutePath());
		} else {
			File lqFile = card.getFile();

			if (lqFile != null && lqFile.isFile()) {
				bitmap = BitmapFactory.decodeFile(lqFile.getAbsolutePath());
			}
		}

		imageView.setImageBitmap(bitmap);

		Item item = DataManager.getItemByName(card.getName());
		if (item != null) {
			itemView.setItem(item);
			itemView.setVisibility(View.VISIBLE);
		} else {
			itemView.setVisibility(View.GONE);
		}

		if (animate) {
			int index = imageAdapter.getPosition(item);
			if (index >= 0)
				gallery.setSelection(index);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_menu, menu);
		return true;
	}

	private void chooseType(ItemType type, String category, Item item) {
		this.cardType = type;

		gallery.setVisibility(View.VISIBLE);
		imageAdapter = new GalleryImageAdapter(ItemChooserActivity.this, cardType, category);
		gallery.setAdapter(imageAdapter);

		if (item == null && imageAdapter.getCount() > 0)
			item = imageAdapter.getItem(0);

		if (item != null) {
			showCard(item, true);
		}
	}

	private void openSubCategoriesDialog(final ItemType cardType) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final String[] subCategories = DataManager.getCardCategories(cardType).toArray(new String[0]);
		builder.setSingleChoiceItems(subCategories, 0, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				chooseType(cardType, subCategories[which], null);
				dialog.dismiss();
			}
		});
		builder.setTitle("Unterkategorie auswählen");
		builder.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.option_load_subtype) {
			openSubCategoriesDialog(cardType);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
