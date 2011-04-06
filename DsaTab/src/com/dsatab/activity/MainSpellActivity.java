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

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Spell;
import com.dsatab.data.Value;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class MainSpellActivity extends BaseMainActivity {

	private TableLayout tblSpell1;

	private View spellAttributeList;

	private Map<Value, TextView[]> tfValues = new HashMap<Value, TextView[]>(50);

	/**
	 * 
	 */
	public MainSpellActivity() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_spell);
		super.onCreate(savedInstanceState);

		tblSpell1 = (TableLayout) findViewById(R.id.gen_spell_table1);
		spellAttributeList = findViewById(R.id.inc_spell_attributes_list);

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

		loadHeroSpells(hero);
		fillAttributesList(spellAttributeList);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroUnloaded(Hero hero) {
		super.onHeroUnloaded(hero);
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Konstitution:
			case Charisma:
				fillAttribute(spellAttributeList, attr);
				break;
			}

		}

		TextView[] tvs = tfValues.get(value);
		if (tvs != null) {
			for (TextView tf : tvs) {
				Util.setText(tf, value);
			}
		}
	}

	private void loadHeroSpells(Hero hero2) {

		TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		// fill spells
		int rowCount = 0;
		tblSpell1.removeAllViews();
		for (Spell spell : getHero().getSpells()) {
			rowCount++;
			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.spell_row, null);

			row.setOnClickListener(probeListener);
			row.setOnLongClickListener(editListener);
			row.setTag(spell);

			TextView spellLabel = (TextView) row.findViewById(R.id.spell_row_name);
			spellLabel.setText(spell.getName());

			TextView spellProbe = (TextView) row.findViewById(R.id.spell_row_probe);
			spellProbe.setText(spell.getProbe());

			TextView spellValue = (TextView) row.findViewById(R.id.spell_row_value);
			if (spell.getValue() != null) {
				spellValue.setText(Integer.toString(spell.getValue()));
			} else {
				Debug.warning(spell.getName() + " has no value");
			}
			tfValues.put(spell, new TextView[] { spellValue });

			if (rowCount % 2 == 1) {
				row.setBackgroundResource(R.color.RowOdd);
			}

			tblSpell1.addView(row, tableLayout);
		}

	}

}
