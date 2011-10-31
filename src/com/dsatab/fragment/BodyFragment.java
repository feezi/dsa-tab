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

import android.content.Intent;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.activity.MainActivity;
import com.dsatab.common.Util;
import com.dsatab.data.ArmorAttribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.WoundAttribute;
import com.dsatab.view.BodyLayout;

/**
 * @author Ganymede
 * 
 */
public class BodyFragment extends BaseFragment implements OnClickListener {

	private BodyLayout bodyLayout;

	ImageButton setButton;

	TextView totalRs, totalBe;

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
		setButton = (ImageButton) root.findViewById(R.id.fight_set);

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

		setButton.setOnClickListener(this);

		bodyLayout.setOnArmorClickListener(this);
		bodyLayout.setOnArmorLongClickListener(getBaseActivity().getEditListener());
		bodyLayout.setOnWoundClickListener(this);

		super.onActivityCreated(savedInstanceState);
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

		switch (v.getId()) {
		case R.id.fight_set:
			getHero().setActiveSet(getHero().getNextActiveSet());
			break;
		}

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

			Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ARMOR_POSITION, value.getPosition());
			intent.putExtra(ItemChooserFragment.INTENT_EXTRA_CATEGORY_SELECTABLE, false);
			startActivity(intent);

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
	}

	private void updateView() {

		LevelListDrawable drawable = (LevelListDrawable) setButton.getDrawable();
		drawable.setLevel(getHero().getActiveSet());

		totalRs.setText(Util.toString(getHero().getArmorRs()));
		totalBe.setText(Util.toString(getHero().getArmorBe()));
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
		}
		super.onValueChanged(value);
	}

}
