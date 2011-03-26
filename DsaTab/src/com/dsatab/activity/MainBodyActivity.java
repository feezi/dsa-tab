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

import android.os.Bundle;
import android.view.View;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.view.BodyLayout;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class MainBodyActivity extends BaseMainActivity {

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

		findViewById(R.id.body_set1).setOnClickListener(this);
		findViewById(R.id.body_set2).setOnClickListener(this);
		findViewById(R.id.body_set3).setOnClickListener(this);

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
		case R.id.body_set1:
			selectItemSet(0);
			break;
		case R.id.body_set2:
			selectItemSet(1);
			break;
		case R.id.body_set3:
			selectItemSet(2);
			break;

		}
	}

	/**
	 * @param i
	 */
	private void selectItemSet(int i) {
		getHero().setActiveSet(i);
		getHero().resetArmorAttributes();

		findViewById(R.id.body_set1).setSelected(i == 0);
		findViewById(R.id.body_set2).setSelected(i == 1);
		findViewById(R.id.body_set3).setSelected(i == 2);

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

		findViewById(R.id.body_set1).setSelected(hero.getActiveSet() == 0);
		findViewById(R.id.body_set2).setSelected(hero.getActiveSet() == 1);
		findViewById(R.id.body_set3).setSelected(hero.getActiveSet() == 2);

		hero.addValueChangedListener(bodyLayout);
	}

	protected void onHeroUnloaded(Hero hero) {
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

	}

}
