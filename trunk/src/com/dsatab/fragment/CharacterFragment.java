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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Advantage;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Markable;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.PortraitViewDialog;
import com.gandulf.guilib.util.Debug;

public class CharacterFragment extends BaseFragment implements OnClickListener {

	private static final String PREF_SHOW_FEATURE_COMMENTS = "SHOW_COMMENTS";

	private static final int ACTION_PHOTO = 1;
	private static final int CONTEXTMENU_COMMENTS_TOGGLE = 14;

	private TextView tfSpecialFeatures, tfExperience, tfTotalLp, tfTotalAu, tfTotalAe, tfTotalKe, tfGs, tfWs;

	private View charAttributesList;

	private Markable selectedTalent;

	private ImageView portraitView;

	private Map<Value, TextView[]> tfValues = new HashMap<Value, TextView[]>(50);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sheet_character, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		charAttributesList = findViewById(R.id.gen_attributes);

		tfExperience = (TextView) findViewById(R.id.gen_abp);
		tfExperience.setOnClickListener(getBaseActivity().getEditListener());
		tfExperience.setOnLongClickListener(getBaseActivity().getEditListener());

		tfTotalAe = (TextView) findViewById(R.id.gen_total_ae);
		tfTotalKe = (TextView) findViewById(R.id.gen_total_ke);
		tfTotalLp = (TextView) findViewById(R.id.gen_total_lp);
		tfTotalAu = (TextView) findViewById(R.id.gen_total_au);

		tfGs = (TextView) findViewById(R.id.gen_gs);
		tfWs = (TextView) findViewById(R.id.gen_ws);

		tfSpecialFeatures = (TextView) findViewById(R.id.gen_specialfeatures);

		portraitView = (ImageView) findViewById(R.id.gen_portrait);

		registerForContextMenu(portraitView);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		switch (v.getId()) {
		case R.id.gen_specialfeatures:
			MenuItem item = menu.add(0, CONTEXTMENU_COMMENTS_TOGGLE, 0, R.string.menu_show_hide_comments);
			item.setEnabled(getHero() != null);
			break;

		case R.id.gen_portrait:
			if (getHero() != null) {
				menu.setHeaderTitle(getHero().getName());

				MenuInflater inflater = new MenuInflater(getActivity());
				inflater.inflate(R.menu.portrait_popupmenu, menu);

				if (getHero().getPortrait() == null) {
					menu.findItem(R.id.option_view_portrait).setVisible(false);
				}
			}
			break;
		}

		super.onCreateContextMenu(menu, v, menuInfo);

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
				// combatTalentAdapter.notifyDataSetChanged();
				selectedTalent = null;
			}
			return true;
		case R.id.option_mark_unused:
			if (selectedTalent != null) {
				selectedTalent.setUnused(true);
				// combatTalentAdapter.notifyDataSetChanged();
				selectedTalent = null;
			}
			return true;
		case R.id.option_unmark:
			if (selectedTalent != null) {
				selectedTalent.setFavorite(false);
				selectedTalent.setUnused(false);
				// combatTalentAdapter.notifyDataSetChanged();
				selectedTalent = null;
			}
			return true;
		case R.id.option_take_photo:
			Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(camera, ACTION_PHOTO);
			break;
		case R.id.option_view_portrait:
			showPortrait();
			break;
		case R.id.option_pick_avatar:
			PortraitChooserDialog pdialog = new PortraitChooserDialog(getBaseActivity());
			pdialog.show();
			break;
		// case R.id.option_edit_value:
		// int position = ((AdapterContextMenuInfo)
		// item.getMenuInfo()).position;
		// BaseCombatTalent talent = combatTalentAdapter.getItem(position);
		// showEditPopup(talent);
		// break;
		}

		return super.onContextItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case ACTION_PHOTO:

			if (resultCode == Activity.RESULT_OK) {

				// Retrieve image taking in camera activity
				Bundle b = data.getExtras();
				Bitmap pic = (Bitmap) b.get("data");

				if (pic != null) {
					// Store the image on the phone for later retrieval
					FileOutputStream fOut = null;
					try {
						String photoName = "photo" + getHero().getName();
						fOut = getActivity().openFileOutput(photoName, Activity.MODE_PRIVATE);
						pic.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
						fOut.flush();

						File outputfile = getActivity().getFileStreamPath(photoName);

						// set uri for currently selected player
						getHero().setPortraitUri(outputfile.toURI());

						updatePortrait(getHero());
					} catch (FileNotFoundException e) {
						Debug.error(e);
					} catch (IOException e) {
						Debug.error(e);
					} finally {
						if (fOut != null) {
							try {
								fOut.close();
							} catch (IOException e) {
							}
						}
					}
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showPortrait() {
		Bitmap portrait = getHero().getPortrait();

		if (portrait != null) {
			PortraitViewDialog viewDialog = new PortraitViewDialog(getBaseActivity());
			viewDialog.show();
		} else {
			PortraitChooserDialog pdialog = new PortraitChooserDialog(getBaseActivity());
			pdialog.show();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gen_portrait:

			if (getHero() == null)
				return;

			if (getHero().getPortrait() == null) {
				v.showContextMenu();
			} else {
				showPortrait();
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
				fillAttributeValue(tfTotalLp, AttributeType.Lebensenergie_Total);
				break;
			case Astralenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
				break;
			case Astralenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.gen_ae), AttributeType.Astralenergie);
				fillAttributeValue(tfTotalAe, AttributeType.Astralenergie_Total);
				break;
			case Ausdauer:
				fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
				break;
			case Ausdauer_Total:
				fillAttributeValue((TextView) findViewById(R.id.gen_au), AttributeType.Ausdauer);
				fillAttributeValue(tfTotalAu, AttributeType.Ausdauer_Total);
				break;
			case Karmaenergie:
				fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
				break;
			case Karmaenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.gen_ke), AttributeType.Karmaenergie);
				fillAttributeValue(tfTotalKe, AttributeType.Karmaenergie_Total);
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
	public void onHeroLoaded(Hero hero) {

		fillAttributesList(charAttributesList);

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

		fillAttributeValue(tfTotalLp, AttributeType.Lebensenergie_Total);
		fillAttributeValue(tfTotalAu, AttributeType.Ausdauer_Total);

		if (hero.getAttributeValue(AttributeType.Karmaenergie) == null) {
			findViewById(R.id.row_ke).setVisibility(View.GONE);
		} else {
			fillAttributeValue(tfTotalKe, AttributeType.Karmaenergie_Total);
			findViewById(R.id.row_ke).setVisibility(View.VISIBLE);
		}

		if (hero.getAttributeValue(AttributeType.Astralenergie) == null) {
			findViewById(R.id.row_ae).setVisibility(View.GONE);

		} else {
			fillAttributeValue(tfTotalAe, AttributeType.Astralenergie_Total);
			findViewById(R.id.row_ae).setVisibility(View.VISIBLE);
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

		fillSpecialFeatures(hero);
		registerForContextMenu(tfSpecialFeatures);

		// --
		ImageView portrait = (ImageView) findViewById(R.id.gen_portrait);
		portrait.setOnClickListener(this);
		updatePortrait(hero);

	}

	protected void updatePortrait(Hero hero) {
		Bitmap drawable = hero.getPortrait();
		if (drawable != null)
			portraitView.setImageBitmap(drawable);
		else
			portraitView.setImageResource(R.drawable.profile_blank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroUnloaded(Hero hero) {

	}

	/**
	 * @param hero
	 */
	private void fillSpecialFeatures(Hero hero) {

		boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

		StyleableSpannableStringBuilder stringBuilder = new StyleableSpannableStringBuilder();

		stringBuilder.appendBold(getString(R.string.specialfeatures));
		stringBuilder.appendBold(": ");
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

		tfSpecialFeatures.setText(stringBuilder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.view.listener.HeroChangedListener#onPortraitChanged()
	 */
	@Override
	public void onPortraitChanged() {
		updatePortrait(getHero());
	}

}