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
package com.dsatab.activity;

import android.content.Intent;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.ArmorAttribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.WoundAttribute;
import com.dsatab.view.BodyLayout;
import com.gandulf.guilib.util.Debug;

/**
 * 
 * 
 */
public class MainBodyActivity extends BaseMainActivity implements OnLongClickListener {

	private BodyLayout bodyLayout;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_body);
		super.onCreate(savedInstanceState);

		bodyLayout = (BodyLayout) findViewById(R.id.body_layout);

		findViewById(R.id.fight_set).setOnClickListener(this);

		bodyLayout.setOnArmorClickListener(this);
		bodyLayout.setOnArmorLongClickListener(this);
		bodyLayout.setOnWoundClickListener(this);

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
				showEditPopup(value);
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
		super.onClick(v);

		switch (v.getId()) {
		case R.id.fight_set:
			selectItemSet(getHero().getNextActiveSet());
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
				Intent intent = new Intent(this, ItemChooserActivity.class);
				intent.putExtra(ItemChooserActivity.INTENT_EXTRA_ARMOR_POSITION, value.getPosition());
				intent.putExtra(ItemChooserActivity.INTENT_EXTRA_CATEGORY_SELECTABLE, false);
				startActivity(intent);
			}
		}

	}

	/**
	 * @param i
	 */
	private void selectItemSet(int i) {
		if (getHero() != null) {
			getHero().setActiveSet(i);
			getHero().resetArmorAttributes();

			ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
			LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
			drawable.setLevel(getHero().getActiveSet());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ACTION_PREFERENCES) {

			((TextView) findViewById(R.id.body_total_rs)).setText(Util.toString(getHero().getArmorRs()));
			((TextView) findViewById(R.id.body_total_be)).setText(Util.toString(getHero().getArmorBe()));

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		super.onHeroLoaded(hero);

		Debug.verbose("Hero loaded " + hero.getName() + " " + hero.getArmorAttributes().size());

		bodyLayout.setWoundAttributes(hero.getWounds());
		bodyLayout.setArmorAttributes(hero.getArmorAttributes());

		ImageButton fightSet = (ImageButton) findViewById(R.id.fight_set);
		LevelListDrawable drawable = (LevelListDrawable) fightSet.getDrawable();
		drawable.setLevel(getHero().getActiveSet());

		((TextView) findViewById(R.id.body_total_rs)).setText(Util.toString(hero.getArmorRs()));
		((TextView) findViewById(R.id.body_total_be)).setText(Util.toString(hero.getArmorBe()));

		hero.addValueChangedListener(bodyLayout);
	}

	protected void onHeroUnloaded(Hero hero) {
		super.onHeroUnloaded(hero);
		if (hero != null)
			hero.removeValueChangeListener(bodyLayout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.ValueChangedListener#onValueChanged(com.dsatab
	 * .data.Value)
	 */
	@Override
	public void onValueChanged(Value value) {
		if (value instanceof ArmorAttribute) {
			((TextView) findViewById(R.id.body_total_rs)).setText(Util.toString(getHero().getArmorRs()));
			((TextView) findViewById(R.id.body_total_be)).setText(Util.toString(getHero().getArmorBe()));
		}
	}

}