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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
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
import com.dsatab.data.Markable;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.LiteInfoDialog;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.VersionInfoDialog;
import com.dsatab.view.listener.ModifierChangedListener;
import com.dsatab.view.listener.ValueChangedListener;

public class MainCharacterActivity extends BaseMainActivity implements ValueChangedListener, ModifierChangedListener,
		DialogInterface.OnDismissListener {

	private static final String PREF_SHOW_FEATURE_COMMENTS = "SHOW_COMMENTS";

	private static final String PREF_KEY_SHOW_FAVORITE = "SHOW_FAVORITE";
	private static final String PREF_KEY_SHOW_NORMAL = "SHOW_NORMAL";
	private static final String PREF_KEY_SHOW_UNUSED = "SHOW_UNUSED";

	private static final int CONTEXTMENU_COMMENTS_TOGGLE = 14;

	private TextView tfSpecialFeatures, tfExperience, tfLabelAe, tfLabelKe, tfTotalLp, tfTotalAu, tfTotalAe, tfTotalKe,
			tfGs, tfWs;

	private View charAttributesList;

	private TableLayout tblCombatAttributes;

	private LiteInfoDialog liteDialog;

	private VersionInfoDialog newsDialog;

	private Markable selectedTalent;

	private FilterSettings filterSettings;

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
		tfExperience.setOnClickListener(editListener);
		tfExperience.setOnLongClickListener(editListener);

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

		SharedPreferences pref = getPreferences(MODE_PRIVATE);

		filterSettings = new FilterSettings();
		filterSettings.set(pref.getBoolean(PREF_KEY_SHOW_FAVORITE, true), pref.getBoolean(PREF_KEY_SHOW_NORMAL, true),

		pref.getBoolean(PREF_KEY_SHOW_UNUSED, false));
		if (!showNewsInfoPopup())
			showLiteInfoPopup();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onCreateOptionsMenu(android.view
	 * .Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.talent_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onOptionsItemSelected(android.view
	 * .MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.option_filter) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle("Talente filtern");
			builder.setIcon(android.R.drawable.ic_menu_view);
			View content = getLayoutInflater().inflate(R.layout.popup_filter, null);

			final CheckBox fav = (CheckBox) content.findViewById(R.id.cb_show_favorites);
			final CheckBox normal = (CheckBox) content.findViewById(R.id.cb_show_normal);
			final CheckBox unused = (CheckBox) content.findViewById(R.id.cb_show_unused);

			SharedPreferences pref = getPreferences(MODE_PRIVATE);

			fav.setChecked(pref.getBoolean(PREF_KEY_SHOW_FAVORITE, true));
			normal.setChecked(pref.getBoolean(PREF_KEY_SHOW_NORMAL, true));
			unused.setChecked(pref.getBoolean(PREF_KEY_SHOW_UNUSED, false));

			builder.setView(content);

			DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {

						SharedPreferences pref = getPreferences(MODE_PRIVATE);
						Editor edit = pref.edit();

						edit.putBoolean(PREF_KEY_SHOW_FAVORITE, fav.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_NORMAL, normal.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_UNUSED, unused.isChecked());

						edit.commit();

						filterSettings.set(fav.isChecked(), normal.isChecked(), unused.isChecked());
						loadCombatTalents(getHero());
					} else if (which == DialogInterface.BUTTON_NEUTRAL) {
						// do nothing
					}

				}
			};

			builder.setPositiveButton(R.string.label_ok, clickListener);
			builder.setNegativeButton(R.string.label_cancel, clickListener);

			builder.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		switch (v.getId()) {
		case R.id.combat_talent_name:

			if (v.getTag() instanceof CombatMeleeTalent) {
				selectedTalent = (CombatMeleeTalent) v.getTag();
			} else if (v.getTag() instanceof CombatDistanceTalent) {
				selectedTalent = (CombatDistanceTalent) v.getTag();
			}

			if (selectedTalent != null) {

				getMenuInflater().inflate(R.menu.talent_popupmenu, menu);

				menu.setHeaderTitle(selectedTalent.getName());
				menu.findItem(R.id.option_unmark).setVisible(selectedTalent.isFavorite() || selectedTalent.isUnused());
				menu.findItem(R.id.option_mark_favorite).setVisible(!selectedTalent.isFavorite());
				menu.findItem(R.id.option_mark_unused).setVisible(!selectedTalent.isUnused());

				menu.findItem(R.id.option_view_details).setVisible(false);
				menu.findItem(R.id.option_edit_value).setVisible(false);
			}
			break;

		case R.id.gen_specialfeatures:
			menu.add(0, CONTEXTMENU_COMMENTS_TOGGLE, 0, R.string.menu_show_hide_comments);
			break;
		}

		super.onCreateContextMenu(menu, v, menuInfo);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnDismissListener#onDismiss(android.content
	 * .DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {

		if (dialog == newsDialog) {
			newsDialog = null;
			showLiteInfoPopup();
		} else if (dialog == liteDialog) {
			liteDialog = null;
			showNewsInfoPopup();
		}
	}

	private boolean showLiteInfoPopup() {

		if (DSATabApplication.getInstance().isLiteVersion() && !DSATabApplication.getInstance().liteShown) {

			liteDialog = new LiteInfoDialog(this);
			liteDialog.setOwnerActivity(this);
			liteDialog.setOnDismissListener(this);
			liteDialog.show();

			DSATabApplication.getInstance().liteShown = true;
			return true;
		} else {
			return false;
		}

	}

	private boolean showNewsInfoPopup() {

		if (DSATabApplication.getInstance().newsShown)
			return false;

		int seenVersion = preferences.getInt(DsaPreferenceActivity.KEY_NEWS_VERSION, 0);

		newsDialog = new VersionInfoDialog(this);
		newsDialog.setSeenVersion(seenVersion);
		newsDialog.setOnDismissListener(this);
		if (newsDialog.hasContent()) {
			newsDialog.show();

			int version = DSATabApplication.getInstance().getPackageVersion();

			Editor edit = preferences.edit();
			edit.putInt(DsaPreferenceActivity.KEY_NEWS_VERSION, version);
			edit.commit();
			DSATabApplication.getInstance().newsShown = true;

			return true;
		} else {
			return false;
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXTMENU_COMMENTS_TOGGLE: {
			boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

			showComments = !showComments;
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_FEATURE_COMMENTS, showComments);
			edit.commit();

			fillSpecialFeatures(getHero());
			return true;
		}
		case R.id.option_mark_favorite:
			if (selectedTalent != null) {
				selectedTalent.setFavorite(true);
				loadCombatTalents(getHero());
				selectedTalent = null;
			}
			return true;
		case R.id.option_mark_unused:
			if (selectedTalent != null) {
				selectedTalent.setUnused(true);
				loadCombatTalents(getHero());
			}
			selectedTalent = null;
			return true;
		case R.id.option_unmark:
			if (selectedTalent != null) {
				selectedTalent.setFavorite(false);
				selectedTalent.setUnused(false);
				loadCombatTalents(getHero());
			}
			selectedTalent = null;
			return true;
		}

		return super.onContextItemSelected(item);
	}

	public void onClick(View v) {
		super.onClick(v);

		switch (v.getId()) {
		case R.id.gen_name:
		case R.id.gen_portrait:

			Drawable portrait = getHero().getPortrait();

			if (portrait != null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle(getHero().getName());

				ImageView iv = new ImageView(this);
				iv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				iv.setImageDrawable(portrait);
				iv.setScaleType(ScaleType.CENTER_INSIDE);
				builder.setView(iv);

				DialogInterface.OnClickListener clickListener = new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (which == DialogInterface.BUTTON_NEGATIVE) {
							dialog.dismiss();
							PortraitChooserDialog pdialog = new PortraitChooserDialog(MainCharacterActivity.this);
							pdialog.show();
						} else if (which == DialogInterface.BUTTON_POSITIVE) {
							dialog.dismiss();
						}

					}
				};

				builder.setNegativeButton(R.string.label_edit, clickListener);
				builder.setPositiveButton(R.string.label_ok, clickListener);
				builder.show();
			} else {
				PortraitChooserDialog pdialog = new PortraitChooserDialog(MainCharacterActivity.this);
				pdialog.show();
			}
			break;

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
			case Lebensenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.gen_lp), AttributeType.Lebensenergie);
				fillAttributeValue(tfTotalLp, AttributeType.Lebensenergie_Total, " / ");
				break;
			case Astralenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
				break;
			case Astralenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
				fillAttributeValue(tfTotalAe, AttributeType.Astralenergie_Total, " / ");
				break;
			case Ausdauer:
				fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
				break;
			case Ausdauer_Total:
				fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
				fillAttributeValue(tfTotalAu, AttributeType.Ausdauer_Total, " / ");
				break;
			case Karmaenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
				break;
			case Karmaenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
				fillAttributeValue(tfTotalKe, AttributeType.Karmaenergie_Total, " / ");
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

		Util.setText(tfExperience, hero.getExperience(), null);
		tfExperience.setTag(hero.getExperience());
		tfValues.put(hero.getExperience(), new TextView[] { tfExperience });

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

		fillAttributeValue(tfTotalLp, AttributeType.Lebensenergie_Total, " / ");
		fillAttributeValue(tfTotalAu, AttributeType.Ausdauer_Total, " / ");

		if (hero.getAttributeValue(AttributeType.Karmaenergie) == null) {
			findViewById(R.id.gen_ke).setVisibility(View.GONE);
			tfLabelKe.setVisibility(View.GONE);
			tfTotalKe.setVisibility(View.GONE);
		} else {
			fillAttributeValue(tfTotalKe, AttributeType.Karmaenergie_Total, " / ");
			findViewById(R.id.gen_ke).setVisibility(View.VISIBLE);
			tfLabelKe.setVisibility(View.VISIBLE);
			tfTotalKe.setVisibility(View.VISIBLE);
		}

		if (hero.getAttributeValue(AttributeType.Astralenergie) == null) {
			findViewById(R.id.gen_ae).setVisibility(View.GONE);
			tfLabelAe.setVisibility(View.GONE);
			tfTotalAe.setVisibility(View.GONE);
		} else {
			fillAttributeValue(tfTotalAe, AttributeType.Astralenergie_Total, " / ");
			findViewById(R.id.gen_ae).setVisibility(View.VISIBLE);
			tfLabelAe.setVisibility(View.VISIBLE);
			tfTotalAe.setVisibility(View.VISIBLE);
		}

		((TextView) findViewById(R.id.gen_st)).setText(Util.toString(hero.getLevel()));

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

		ImageView portrait = (ImageView) findViewById(R.id.gen_portrait);
		portrait.setOnClickListener(this);
		updatePortrait(hero);

		hero.addModifierChangedListener(this);

	}

	protected void updatePortrait(Hero hero) {
		ImageView portrait = (ImageView) findViewById(R.id.gen_portrait);

		Drawable drawable = hero.getPortrait();
		if (drawable != null)
			portrait.setImageDrawable(drawable);
		else
			portrait.setImageResource(R.drawable.profile_blank);
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
		if (hero != null)
			hero.removeModifierChangedListener(this);
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
				stringBuilder.append(advantage.toString());
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
			for (Advantage disadvantage : hero.getDisadvantages()) {

				if (!first) {
					stringBuilder.append(", ");
				} else {
					first = false;
				}
				stringBuilder.append(disadvantage.toString());
				if (showComments && !TextUtils.isEmpty(disadvantage.getComment())) {
					stringBuilder.appendColor(Color.GRAY, " (");
					stringBuilder.appendColor(Color.GRAY, disadvantage.getComment());
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

		for (CombatMeleeTalent meleeTalent : getHero().getCombatMeleeTalents()) {

			if (!filterSettings.isVisible(meleeTalent))
				continue;
			rowCount++;

			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.combat_talent_row, null);

			TextView talentLabel = (TextView) row.findViewById(R.id.combat_talent_name);
			talentLabel.setText(meleeTalent.getName());
			talentLabel.setTag(meleeTalent);
			registerForContextMenu(talentLabel);

			TextView talentBe = (TextView) row.findViewById(R.id.combat_talent_be);
			talentBe.setText(meleeTalent.getType().getBe());

			TextView talentValueAt = (TextView) row.findViewById(R.id.combat_talent_at);
			if (meleeTalent.getAttack() != null || meleeTalent.getAttack().getValue() != null) {
				talentValueAt.setText(Integer.toString(meleeTalent.getAttack().getValue()));
				talentValueAt.setOnClickListener(probeListener);
				talentValueAt.setOnLongClickListener(editListener);

				talentValueAt.setTag(R.id.TAG_KEY_VALUE, meleeTalent.getAttack());
				talentValueAt.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), meleeTalent, true));
			}

			tfValues.put(meleeTalent.getAttack(), new TextView[] { talentValueAt });

			TextView talentValuePa = (TextView) row.findViewById(R.id.combat_talent_pa);

			if (meleeTalent.getDefense() != null && meleeTalent.getDefense().getValue() != null) {
				talentValuePa.setText(Integer.toString(meleeTalent.getDefense().getValue()));
				talentValuePa.setOnClickListener(probeListener);
				talentValuePa.setOnLongClickListener(editListener);
				talentValuePa.setTag(R.id.TAG_KEY_VALUE, meleeTalent.getDefense());
				talentValuePa.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), meleeTalent, false));
			}
			tfValues.put(meleeTalent.getDefense(), new TextView[] { talentValuePa });

			Util.applyRowStyle(meleeTalent, row, rowCount);

			currentTable.addView(row, tableLayout);
		}

		for (CombatDistanceTalent element : getHero().getCombatDistanceTalents()) {

			if (!filterSettings.isVisible(element))
				continue;

			rowCount++;

			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.combat_talent_row, null);

			row.setOnClickListener(probeListener);
			row.setOnLongClickListener(editListener);
			row.setTag(R.id.TAG_KEY_VALUE, element);
			row.setTag(R.id.TAG_KEY_PROBE, new CombatProbe(getHero(), element, true));

			TextView talentLabel = (TextView) row.findViewById(R.id.combat_talent_name);
			talentLabel.setText(element.getName());
			talentLabel.setTag(element);
			registerForContextMenu(talentLabel);

			TextView talentBe = (TextView) row.findViewById(R.id.combat_talent_be);
			talentBe.setText(element.getBe());

			TextView talentValueAt = (TextView) row.findViewById(R.id.combat_talent_at);
			talentValueAt.setText(Integer.toString(element.getValue()));
			tfValues.put(element, new TextView[] { talentValueAt });

			TextView talentValuePa = (TextView) row.findViewById(R.id.combat_talent_pa);
			talentValuePa.setVisibility(View.INVISIBLE);

			Util.applyRowStyle(element, row, rowCount);

			currentTable.addView(row, tableLayout);
		}

	}

	public void setPortraitFile(String drawableName) {
		Editor editor = preferences.edit();
		editor.putString(getHero().getPath(), drawableName);
		editor.commit();

		updatePortrait(getHero());
	}

}