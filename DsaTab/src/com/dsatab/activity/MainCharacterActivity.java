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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Advantage;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.listener.ModifierChangedListener;
import com.dsatab.view.listener.ValueChangedListener;

public class MainCharacterActivity extends BaseMainActivity implements ValueChangedListener, ModifierChangedListener {

	private static final String PREF_SHOW_FEATURE_COMMENTS = "SHOW_COMMENTS";

	private static final int CONTEXTMENU_COMMENTS_TOGGLE = 14;

	private TextView tfSpecialFeatures, tfExperience, tfLabelAe, tfLabelKe, tfTotalLp, tfTotalAu, tfTotalAe, tfTotalKe,
			tfGs, tfWs;

	private View charAttributesList;

	private TableLayout tblCombatAttributes;

	private Map<Value, TextView[]> tfValues = new HashMap<Value, TextView[]>(50);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_character);
		super.onCreate(savedInstanceState);

		TextView tfName = (TextView) findViewById(R.id.gen_name);
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/harrington.ttf");
		tfName.setTypeface(tf);
		tfName.setOnClickListener(this);

		tfExperience = (TextView) findViewById(R.id.gen_abp);
		((TableRow.LayoutParams) tfExperience.getLayoutParams()).span = 2;

		tblCombatAttributes = (TableLayout) findViewById(R.id.gen_combat_attributes);

		tfLabelAe = (TextView) findViewById(R.id.gen_label_ae);
		tfLabelKe = (TextView) findViewById(R.id.gen_label_ke);

		tfTotalAe = (TextView) findViewById(R.id.gen_total_ae);
		tfTotalKe = (TextView) findViewById(R.id.gen_total_ke);
		tfTotalLp = (TextView) findViewById(R.id.gen_total_lp);
		tfTotalAu = (TextView) findViewById(R.id.gen_total_au);

		tfGs = (TextView) findViewById(R.id.gen_gs);
		tfWs = (TextView) findViewById(R.id.gen_ws);

		tfSpecialFeatures = (TextView) findViewById(R.id.gen_specialfeatures);

		charAttributesList = findViewById(R.id.gen_attributes);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v == tfSpecialFeatures) {
			menu.add(0, CONTEXTMENU_COMMENTS_TOGGLE, 0, R.string.menu_show_hide_comments);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CONTEXTMENU_COMMENTS_TOGGLE) {
			boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

			showComments = !showComments;
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_FEATURE_COMMENTS, showComments);
			edit.commit();

			fillSpecialFeatures(getHero());
		}

		return super.onContextItemSelected(item);
	}

	public void onClick(View v) {
		super.onClick(v);

		if (v.getId() == R.id.gen_name) {
			PortraitChooserDialog dialog = new PortraitChooserDialog(this);
			dialog.show();
		}

	}

	@Override
	public void onModifierAdded(Modificator value) {
		tfGs.setText(Util.toString(getHero().getGs()));
	}

	@Override
	public void onModifierRemoved(Modificator value) {
		tfGs.setText(Util.toString(getHero().getGs()));
	}

	@Override
	public void onModifierChanged(Modificator value) {
		tfGs.setText(Util.toString(getHero().getGs()));
	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {
		tfGs.setText(Util.toString(getHero().getGs()));
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		TextView[] tvs = tfValues.get(value);
		if (tvs != null) {
			for (TextView tf : tvs) {
				Util.setText(tf, value);
			}
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {
			case Lebensenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_lp), AttributeType.Lebensenergie);
				break;
			case Astralenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
				break;
			case Ausdauer:
				fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
				break;
			case Karmaenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
				break;
			case Magieresistenz:
				fillAttributeValue((TextView) findViewById(R.id.gen_mr), AttributeType.Magieresistenz);
				break;
			case Sozialstatus:
				fillAttributeValue((TextView) findViewById(R.id.gen_so), AttributeType.Sozialstatus);
				break;
			case at:
				fillAttributeValue((TextView) findViewById(R.id.gen_at), AttributeType.at);
				break;
			case pa:
				fillAttributeValue((TextView) findViewById(R.id.gen_pa), AttributeType.pa);
				break;
			case fk:
				fillAttributeValue((TextView) findViewById(R.id.gen_fk), AttributeType.fk);
				break;
			case ini:
				fillAttributeValue((TextView) findViewById(R.id.gen_ini), AttributeType.ini);
				break;
			case Behinderung:
				fillAttributeValue((TextView) findViewById(R.id.gen_be), AttributeType.Behinderung);
				tfGs.setText(Util.toString(getHero().getGs()));
				break;
			case Gewandtheit:
				tfGs.setText(Util.toString(getHero().getGs()));
				// no break since attribute value has to be set too
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Konstitution:
			case Charisma:
				fillAttribute(charAttributesList, attr);
				break;
			}

		}
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

		((TextView) findViewById(R.id.gen_name)).setText(hero.getName());

		tfExperience.setText(Util.toString(hero.getExperience()));

		fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
		fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
		fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
		fillAttributeValue((TextView) findViewById(R.id.gen_lp), AttributeType.Lebensenergie);
		fillAttributeValue((TextView) findViewById(R.id.gen_mr), AttributeType.Magieresistenz);
		fillAttributeValue((TextView) findViewById(R.id.gen_so), AttributeType.Sozialstatus);

		fillAttributeValue((TextView) findViewById(R.id.gen_at), AttributeType.at);
		fillAttributeValue((TextView) findViewById(R.id.gen_pa), AttributeType.pa);
		fillAttributeValue((TextView) findViewById(R.id.gen_fk), AttributeType.fk);
		fillAttributeValue((TextView) findViewById(R.id.gen_ini), AttributeType.ini);
		fillAttributeValue((TextView) findViewById(R.id.gen_be), AttributeType.Behinderung);

		fillAttributeLabel((TextView) findViewById(R.id.gen_at_label), AttributeType.at);
		fillAttributeLabel((TextView) findViewById(R.id.gen_pa_label), AttributeType.pa);
		fillAttributeLabel((TextView) findViewById(R.id.gen_fk_label), AttributeType.fk);
		fillAttributeLabel((TextView) findViewById(R.id.gen_ini_label), AttributeType.ini);
		fillAttributeLabel((TextView) findViewById(R.id.gen_be_label), AttributeType.Behinderung);

		tfTotalLp.setText(" / " + Util.toString(hero.getAttribute(AttributeType.Lebensenergie).getReferenceValue()));
		tfTotalAu.setText(" / " + Util.toString(hero.getAttribute(AttributeType.Ausdauer).getReferenceValue()));

		if (hero.getAttributeValue(AttributeType.Karmaenergie) == null) {
			findViewById(R.id.gen_ke).setVisibility(View.GONE);
			tfLabelKe.setVisibility(View.GONE);
			tfTotalKe.setVisibility(View.GONE);
		} else {
			tfTotalKe.setText(" / " + Util.toString(hero.getAttribute(AttributeType.Karmaenergie).getReferenceValue()));
			tfLabelKe.setVisibility(View.VISIBLE);
			tfTotalKe.setVisibility(View.VISIBLE);
		}

		if (hero.getAttributeValue(AttributeType.Astralenergie) == null) {
			findViewById(R.id.gen_ae).setVisibility(View.GONE);
			tfLabelAe.setVisibility(View.GONE);
			tfTotalAe.setVisibility(View.GONE);
		} else {
			tfTotalAe
					.setText(" / " + Util.toString(hero.getAttribute(AttributeType.Astralenergie).getReferenceValue()));
			tfLabelAe.setVisibility(View.VISIBLE);
			tfTotalAe.setVisibility(View.VISIBLE);
		}

		tfGs.setText(Util.toString(hero.getGs()));

		int[] ws = hero.getWundschwelle();
		tfWs.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);

		((TextView) findViewById(R.id.gen_groesse)).setText(hero.getGroesse() + " cm");
		((TextView) findViewById(R.id.gen_gewicht)).setText(hero.getGewicht() + " Stein");
		((TextView) findViewById(R.id.gen_herkunft)).setText(hero.getHerkunft());
		((TextView) findViewById(R.id.gen_ausbildung)).setText(hero.getAusbildung());
		((TextView) findViewById(R.id.gen_alter)).setText(Util.toString(hero.getAlter()));
		((TextView) findViewById(R.id.gen_haar_augen)).setText(hero.getHaarFarbe() + " / " + hero.getAugenFarbe());
		//
		fillAttributesList(charAttributesList);

		fillSpecialFeatures(hero);
		registerForContextMenu(tfSpecialFeatures);

		loadCombatTalents(hero);

		hero.addModifierChangedListener(this);
		hero.addValueChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroUnloaded(Hero hero) {
		super.onHeroLoaded(hero);
		hero.removeModifierChangedListener(this);
		hero.removeValueChangeListener(this);
	}

	/**
	 * @param hero
	 */
	private void fillSpecialFeatures(Hero hero) {

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

		StyleableSpannableStringBuilder stringBuilder = new StyleableSpannableStringBuilder();

		stringBuilder.append(TextUtils.join(", ", hero.getSpecialFeatures()));

		if (!hero.getAdvantages().isEmpty()) {
			stringBuilder.append("\n");
			stringBuilder.appendBold(getString(R.string.advantages));
			stringBuilder.appendBold(": ");

			boolean first = true;
			for (Advantage advantage : hero.getAdvantages()) {

				if (!first) {
					stringBuilder.append(", ");
				} else {
					first = false;
				}
				stringBuilder.append(advantage.getName());
				if (showComments && !TextUtils.isEmpty(advantage.getComment())) {
					stringBuilder.appendColor(Color.GRAY, " (");
					stringBuilder.appendColor(Color.GRAY, advantage.getComment());
					stringBuilder.appendColor(Color.GRAY, ")");
				}

			}

		}

		if (!hero.getDisadvantages().isEmpty()) {
			stringBuilder.append("\n");
			stringBuilder.appendBold(getString(R.string.disadvantages));
			stringBuilder.appendBold(": ");

			boolean first = true;
			for (Advantage advantage : hero.getDisadvantages()) {

				if (!first) {
					stringBuilder.append(", ");
				} else {
					first = false;
				}
				stringBuilder.append(advantage.getName());
				if (showComments && !TextUtils.isEmpty(advantage.getComment())) {
					stringBuilder.appendColor(Color.GRAY, " (");
					stringBuilder.appendColor(Color.GRAY, advantage.getComment());
					stringBuilder.appendColor(Color.GRAY, ")");
				}

			}
		}

		tfSpecialFeatures.setText(stringBuilder.toString());
	}

	private void loadCombatTalents(Hero hero2) {

		TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		// fill combat attributes
		tblCombatAttributes.removeAllViews();
		int rowCount = 0;
		TableLayout currentTable = tblCombatAttributes;

		for (CombatMeleeTalent element : getHero().getCombatMeleeTalents()) {
			rowCount++;

			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.combat_talent_row, null);

			TextView talentLabel = (TextView) row.findViewById(R.id.combat_talent_name);
			talentLabel.setText(element.getName());

			TextView talentBe = (TextView) row.findViewById(R.id.combat_talent_be);
			talentBe.setText(element.getType().getBe());

			TextView talentValueAt = (TextView) row.findViewById(R.id.combat_talent_at);
			if (element.getAttack() != null || element.getAttack().getValue() != null) {
				talentValueAt.setText(Integer.toString(element.getAttack().getValue()));
				talentValueAt.setOnClickListener(probeListener);
				talentValueAt.setOnLongClickListener(editListener);

				talentValueAt.setTag(R.id.TAG_KEY_VALUE, element.getAttack());
				talentValueAt.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), element, true));
			}

			tfValues.put(element.getAttack(), new TextView[] { talentValueAt });

			TextView talentValuePa = (TextView) row.findViewById(R.id.combat_talent_pa);

			if (element.getDefense() != null && element.getDefense().getValue() != null) {
				talentValuePa.setText(Integer.toString(element.getDefense().getValue()));
				talentValuePa.setOnClickListener(probeListener);
				talentValuePa.setOnLongClickListener(editListener);
				talentValuePa.setTag(R.id.TAG_KEY_VALUE, element.getDefense());
				talentValuePa.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), element, false));
			}
			tfValues.put(element.getDefense(), new TextView[] { talentValuePa });

			if (rowCount % 2 == 1) {
				row.setBackgroundResource(R.color.RowOdd);
			}

			currentTable.addView(row, tableLayout);
		}

		for (CombatDistanceTalent element : getHero().getCombatDistanceTalents()) {
			rowCount++;

			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.combat_talent_row, null);

			row.setOnClickListener(probeListener);
			row.setOnLongClickListener(editListener);
			row.setTag(R.id.TAG_KEY_VALUE, element);
			row.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), element, true));

			TextView talentLabel = (TextView) row.findViewById(R.id.combat_talent_name);
			talentLabel.setText(element.getName());

			TextView talentBe = (TextView) row.findViewById(R.id.combat_talent_be);
			talentBe.setText(element.getBe());

			TextView talentValueAt = (TextView) row.findViewById(R.id.combat_talent_at);
			talentValueAt.setText(Integer.toString(element.getValue()));
			tfValues.put(element, new TextView[] { talentValueAt });

			TextView talentValuePa = (TextView) row.findViewById(R.id.combat_talent_pa);
			talentValuePa.setVisibility(View.INVISIBLE);

			if (rowCount % 2 == 1) {
				row.setBackgroundResource(R.color.RowOdd);
			}

			currentTable.addView(row, tableLayout);
		}

	}

	public void setPortraitFile(String drawableId) {
		Editor editor = preferences.edit();
		editor.putString(getHero().getPath(), drawableId);
		editor.commit();
	}

}