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
package com.dsatab.fragment;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.activity.MainActivity;
import com.dsatab.common.Util;
import com.dsatab.data.ArmorAttribute;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.WoundAttribute;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.view.BodyLayout;

/**
 * @author Ganymede
 * 
 */
public class BodyFragment extends BaseFragment implements OnClickListener {

	private BodyLayout bodyLayout;

	TextView totalRs, totalBe;

	ImageView bodyBackground;

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
		super.onCreateOptionsMenu(menu, inflater);

		if (menu.findItem(R.id.option_fight_set) == null) {
			com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_fight_set, Menu.NONE, "Set");
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setIcon(R.drawable.ic_menu_set);
			if (item.getIcon() instanceof LevelListDrawable) {
				((LevelListDrawable) item.getIcon()).setLevel(getHero().getActiveSet());
			}
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
		View root = configureContainerView(inflater.inflate(R.layout.sheet_body, container, false));

		bodyLayout = (BodyLayout) root.findViewById(R.id.body_layout);

		bodyBackground = (ImageView) root.findViewById(R.id.body_background);

		totalRs = (TextView) root.findViewById(R.id.body_total_rs);
		totalBe = (TextView) root.findViewById(R.id.body_total_be);

		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		bodyLayout.setOnArmorClickListener(this);
		bodyLayout.setOnArmorLongClickListener(getBaseActivity().getEditListener());
		bodyLayout.setOnWoundClickListener(this);

		updateBackground();

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onSharedPreferenceChanged(android.content
	 * .SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);

		if (BasePreferenceActivity.KEY_STYLE_BG_WOUNDS_PATH.equals(key)) {
			updateBackground();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {

		bodyLayout.setOnArmorClickListener(null);
		bodyLayout.setOnArmorLongClickListener(null);
		bodyLayout.setOnWoundClickListener(null);

		super.onDestroyView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {
		bodyLayout.setWoundAttributes(hero.getWounds());
		bodyLayout.setArmorAttributes(hero.getArmorAttributes());
		updateView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		// wounds
		if (v.getTag() instanceof WoundAttribute) {

			ImageView iv = (ImageButton) v;
			WoundAttribute attribute = (WoundAttribute) v.getTag();

			if (iv.isSelected()) {
				attribute.setValue(attribute.getValue() - 1);
			} else {
				attribute.setValue(attribute.getValue() + 1);
			}
			iv.setSelected(!iv.isSelected());
			iv.setBackgroundResource(R.drawable.icon_wound_btn);
		}
		// armor
		else if (v.getTag() instanceof ArmorAttribute) {
			ArmorAttribute value = (ArmorAttribute) v.getTag();

			List<EquippedItem> equippedItems = getHero().getArmor(value.getPosition());
			if (!equippedItems.isEmpty()) {
				Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ARMOR_POSITION, value.getPosition());
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_CATEGORY_SELECTABLE, false);
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_SEARCHABLE, false);
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "Keine Einträge gefunden", Toast.LENGTH_SHORT).show();
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MainActivity.ACTION_PREFERENCES) {
			updateView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onActiveSetChanged(int, int)
	 */
	@Override
	public void onActiveSetChanged(int newSet, int oldSet) {
		super.onActiveSetChanged(newSet, oldSet);
		updateView();
		bodyLayout.setArmorAttributes(getHero().getArmorAttributes());
		getActivity().supportInvalidateOptionsMenu();
	}

	private void updateView() {
		totalRs.setText(Util.toString(getHero().getArmorRs()));
		totalBe.setText(Util.toString(getHero().getArmorBe()));
	}

	private void updateBackground() {
		SharedPreferences preferences = DSATabApplication.getPreferences();
		if (preferences.contains(BasePreferenceActivity.KEY_STYLE_BG_WOUNDS_PATH)) {
			String filePath = preferences.getString(BasePreferenceActivity.KEY_STYLE_BG_WOUNDS_PATH, null);
			bodyBackground.setImageDrawable(Drawable.createFromPath(filePath));
		} else {
			bodyBackground.setImageResource(R.drawable.character);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onValueChanged(com.dsatab.data.Value)
	 */
	@Override
	public void onValueChanged(Value value) {
		if (value instanceof ArmorAttribute) {
			ArmorAttribute rs = (ArmorAttribute) value;
			bodyLayout.setArmorAttribute(rs);
		} else if (value instanceof Attribute) {
			Attribute attribute = (Attribute) value;

			switch (attribute.getType()) {
			case Behinderung:
				totalBe.setText(Util.toString(getHero().getArmorBe()));
				break;
			default:
				// do nothing
				break;
			}
		}
		super.onValueChanged(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onItemEquipped(com.dsatab.data.items
	 * .EquippedItem)
	 */
	@Override
	public void onItemEquipped(EquippedItem item) {
		if (item.isArmor()) {
			totalRs.setText(Util.toString(getHero().getArmorRs()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onItemUnequipped(com.dsatab.data.items
	 * .EquippedItem)
	 */
	@Override
	public void onItemUnequipped(EquippedItem item) {
		if (item.isArmor()) {
			totalRs.setText(Util.toString(getHero().getArmorRs()));
		}
	}

}
