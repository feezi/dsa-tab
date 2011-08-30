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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.BaseMainActivity;
import com.dsatab.activity.ItemChooserActivity;
import com.dsatab.common.Util;
import com.dsatab.data.ArmorAttribute;
import com.dsatab.data.Hero;
import com.dsatab.data.WoundAttribute;
import com.dsatab.view.BodyLayout;

/**
 * @author Ganymede
 * 
 */
public class BodyFragment extends BaseFragment implements OnClickListener, OnLongClickListener {

	private BodyLayout bodyLayout;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sheet_body, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		bodyLayout = (BodyLayout) getView().findViewById(R.id.body_layout);

		getView().findViewById(R.id.fight_set).setOnClickListener(this);

		bodyLayout.setOnArmorClickListener(this);
		bodyLayout.setOnArmorLongClickListener(this);
		bodyLayout.setOnWoundClickListener(this);

		super.onActivityCreated(savedInstanceState);
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

		ImageButton fightSet = (ImageButton) getView().findViewById(R.id.fight_set);
		LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
		drawable.setLevel(getHero().getActiveSet());

		((TextView) getView().findViewById(R.id.body_total_rs)).setText(Util.toString(hero.getArmorRs()));
		((TextView) getView().findViewById(R.id.body_total_be)).setText(Util.toString(hero.getArmorBe()));

		hero.addHeroChangedListener(bodyLayout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		Hero hero = getHero();
		if (hero != null)
			hero.removeHeroChangedListener(bodyLayout);
	}

	public void onHeroUnloaded(Hero hero) {
		if (hero != null)
			hero.removeHeroChangedListener(bodyLayout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		// armor
		if (v.getTag() instanceof ArmorAttribute) {
			ArmorAttribute value = (ArmorAttribute) v.getTag();
			if (DSATabApplication.getInstance().isLiteVersion()) {

				tease("<strong>Mal eben schnell einen Wert steigern?</strong> Mit der Vollversion von DsaTab können Eigenschaften, Talente, Zauber, Rüstungsschutz und noch vieles mehr einfach und bequem editiert werden. Getätigte Änderungen werden in der XML Datei nachgezogen und können somit auch wieder in die Helden-Software importiert werden, falls notwendig. ");
			} else {
				getBaseActivity().showEditPopup(value);
			}
		}
		return false;
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

			if (iv.isSelected())
				iv.setBackgroundResource(R.drawable.icon_wound_s);
			else
				iv.setBackgroundResource(R.drawable.icon_wound_btn);
		}
		// armor
		else if (v.getTag() instanceof ArmorAttribute) {
			ArmorAttribute value = (ArmorAttribute) v.getTag();
			if (DSATabApplication.getInstance().isLiteVersion()) {

				tease("<strong>Was hab ich hier schnell nochmal an?</strong> Ein Klick genug und schon kannst du dir deine Rüstungsgegenstände an dieser Stelle ansehen. ");
			} else {
				Intent intent = new Intent(getActivity(), ItemChooserActivity.class);
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_ARMOR_POSITION, value.getPosition());
				intent.putExtra(ItemChooserFragment.INTENT_EXTRA_CATEGORY_SELECTABLE, false);
				startActivity(intent);
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
		if (requestCode == BaseMainActivity.ACTION_PREFERENCES) {
			((TextView) getView().findViewById(R.id.body_total_rs)).setText(Util.toString(getHero().getArmorRs()));
			((TextView) getView().findViewById(R.id.body_total_be)).setText(Util.toString(getHero().getArmorBe()));
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

		ImageButton fightSet = (ImageButton) getView().findViewById(R.id.fight_set);
		LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
		drawable.setLevel(getHero().getActiveSet());
	}

}
