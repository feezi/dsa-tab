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
import java.util.List;

import yuku.iconcontextmenu.IconContextMenu.IconContextMenuInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Advantage;
import com.dsatab.data.Attribute;
import com.dsatab.data.Experience;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroBaseInfo;
import com.dsatab.data.SpecialFeature;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.util.Debug;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.PortraitViewDialog;

public class CharacterFragment extends BaseAttributesFragment implements OnClickListener {

	private static final String PREF_SHOW_FEATURE_COMMENTS = "SHOW_COMMENTS";
	private static final String PREF_SHOW_BASEINFO = "SHOW_BASEINFO";

	private static final int ACTION_PHOTO = 1;
	private static final int ACTION_GALERY = 2;
	private static final int CONTEXTMENU_COMMENTS_TOGGLE = 14;

	private TextView tfSpecialFeatures, tfSpecialFeaturesTitle, tfAdvantages, tfAdvantagesTitle, tfDisadvantages,
			tfDisadvantgesTitle;
	private TextView tfExperience, tfTotalLe, tfTotalAu, tfTotalAe, tfTotalKe, tfAT, tfPA, tfFK, tfINI, tfBE, tfST;

	private View charAttributesList;

	private ImageButton detailsSwitch;

	private ImageView portraitView;

	/**
	 * 
	 */
	public CharacterFragment() {
		this.inverseColors = false;
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
		View root = configureContainerView(inflater.inflate(R.layout.sheet_character, container, false));

		detailsSwitch = (ImageButton) root.findViewById(R.id.details_switch);

		charAttributesList = root.findViewById(R.id.gen_attributes);
		tfExperience = (TextView) root.findViewById(R.id.attr_abp);

		tfTotalAe = (TextView) root.findViewById(R.id.attr_total_ae);
		tfTotalKe = (TextView) root.findViewById(R.id.attr_total_ke);
		tfTotalLe = (TextView) root.findViewById(R.id.attr_total_le);
		tfTotalAu = (TextView) root.findViewById(R.id.attr_total_au);

		tfAT = (TextView) root.findViewById(R.id.attr_at);
		tfPA = (TextView) root.findViewById(R.id.attr_pa);
		tfFK = (TextView) root.findViewById(R.id.attr_fk);
		tfINI = (TextView) root.findViewById(R.id.attr_ini);

		tfBE = (TextView) root.findViewById(R.id.attr_be);
		tfST = (TextView) root.findViewById(R.id.attr_st);

		tfSpecialFeatures = (TextView) root.findViewById(R.id.gen_specialfeatures);
		tfSpecialFeaturesTitle = (TextView) root.findViewById(R.id.gen_specialfeatures_title);

		tfAdvantages = (TextView) root.findViewById(R.id.gen_advantages);
		tfAdvantagesTitle = (TextView) root.findViewById(R.id.gen_advantages_title);

		tfDisadvantages = (TextView) root.findViewById(R.id.gen_disadvantages);
		tfDisadvantgesTitle = (TextView) root.findViewById(R.id.gen_disadvantages_title);

		portraitView = (ImageView) root.findViewById(R.id.gen_portrait);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		detailsSwitch.setOnClickListener(this);
		tfExperience.setOnClickListener(getBaseActivity().getEditListener());
		tfExperience.setOnLongClickListener(getBaseActivity().getEditListener());
		findViewById(R.id.gen_description).setOnClickListener(this);

		registerForIconContextMenu(portraitView);

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onCreateIconContextMenu(android.view
	 * .Menu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public Object onCreateIconContextMenu(Menu menu, View v, IconContextMenuInfo menuInfo) {
		switch (v.getId()) {
		case R.id.gen_specialfeatures:

			boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);
			MenuItem item = null;
			if (showComments) {
				item = menu.add(0, CONTEXTMENU_COMMENTS_TOGGLE, 0, R.string.menu_hide_comments).setIcon(
						R.drawable.ic_menu_view);
			} else {
				item = menu.add(0, CONTEXTMENU_COMMENTS_TOGGLE, 0, R.string.menu_show_comments).setIcon(
						R.drawable.ic_menu_view);
			}
			item.setEnabled(getHero() != null);
			break;

		case R.id.gen_portrait:
			if (getHero() != null) {
				MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.portrait_popupmenu, menu);

				if (getHero().getPortrait() == null) {
					menu.findItem(R.id.option_view_portrait).setVisible(false);
				}
			}
			break;
		}

		return super.onCreateIconContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onIconContextItemSelected(android.view
	 * .MenuItem, java.lang.Object)
	 */
	@Override
	public void onIconContextItemSelected(MenuItem item, Object info) {

		switch (item.getItemId()) {
		case CONTEXTMENU_COMMENTS_TOGGLE: {
			boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

			showComments = !showComments;
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_FEATURE_COMMENTS, showComments);
			edit.commit();

			fillSpecialFeatures(getHero());
			return;
		}
		case R.id.option_take_photo:
			Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(camera, ACTION_PHOTO);
			break;
		case R.id.option_pick_image:
			Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
			photoPickerIntent.setType("image/*");
			startActivityForResult(Intent.createChooser(photoPickerIntent, "Bild auswählen"), ACTION_GALERY);

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

		super.onIconContextItemSelected(item, info);
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
		case ACTION_GALERY:
			if (resultCode == Activity.RESULT_OK && getHero() != null) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null,
						null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				File file = new File(filePath);
				if (file.exists()) {
					Bitmap yourSelectedImage = Util.decodeBitmap(new File(filePath), 300);

					File outputfile = saveBitmap(yourSelectedImage);
					if (outputfile != null) {
						// set uri for currently selected player
						getHero().setPortraitUri(outputfile.toURI());

						updatePortrait(getHero());
					}
				}
			}

			break;
		case ACTION_PHOTO:

			if (resultCode == Activity.RESULT_OK) {

				// Retrieve image taking in camera activity
				Bundle b = data.getExtras();
				Bitmap pic = (Bitmap) b.get("data");

				if (pic != null) {

					File outputfile = saveBitmap(pic);
					if (outputfile != null) {
						// set uri for currently selected player
						getHero().setPortraitUri(outputfile.toURI());

						updatePortrait(getHero());
					}
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private File saveBitmap(Bitmap pic) {
		FileOutputStream fOut = null;
		try {

			String photoName = "photo" + Util.convertNonAscii(getHero().getName());
			fOut = getActivity().openFileOutput(photoName, Activity.MODE_PRIVATE);
			pic.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
			fOut.flush();

			File outputfile = getActivity().getFileStreamPath(photoName);
			return outputfile;
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
		return null;
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

		case R.id.gen_description:
		case R.id.details_switch:
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_BASEINFO, !preferences.getBoolean(PREF_SHOW_BASEINFO, true));
			edit.commit();
			updateBaseInfo(true);
			break;
		}

	}

	@Override
	public void onModifierAdded(Modificator value) {
		updateValues();
	}

	@Override
	public void onModifierRemoved(Modificator value) {
		updateValues();
	}

	@Override
	public void onModifierChanged(Modificator value) {
		updateValues();
	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {
		updateValues();
	}

	public void onValueChanged(Value value) {

		if (value == null) {
			return;
		}

		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {
			case Lebensenergie:
				fillAttributeValue(tfLE, attr);
				break;
			case Lebensenergie_Total:
				fillAttributeValue(tfTotalLe, attr);
				break;
			case Astralenergie:
				fillAttributeValue(tfAE, attr);
				break;
			case Astralenergie_Total:
				fillAttributeValue(tfTotalAe, attr);
				break;
			case Ausdauer:
				fillAttributeValue(tfAU, attr);
				break;
			case Ausdauer_Total:
				fillAttributeValue(tfTotalAu, attr);
				break;
			case Karmaenergie:
				fillAttributeValue(tfKE, attr);
				break;
			case Karmaenergie_Total:
				fillAttributeValue(tfTotalKe, attr);
				break;
			case Magieresistenz:
				fillAttributeValue(tfMR, attr);
				break;
			case Sozialstatus:
				fillAttributeValue(tfSO, attr);
				break;
			case at:
				fillAttributeValue(tfAT, attr);
				break;
			case pa:
				fillAttributeValue(tfPA, attr);
				break;
			case fk:
				fillAttributeValue(tfFK, attr);
				break;
			case ini:
				fillAttributeValue(tfINI, attr);
				break;
			case Behinderung:
				fillAttributeValue(tfBE, attr);
				break;
			case Geschwindigkeit:
				fillAttributeValue(tfGS, attr);
				break;
			case Gewandtheit:
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Konstitution:
			case Charisma:
				fillAttribute(attr, false);
				break;
			default:
				// do nothing
				break;
			}

		} else if (value instanceof Experience) {
			Util.setText(tfExperience, value);
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

		updateValues();

		Util.setText(tfExperience, hero.getExperience(), null);
		tfExperience.setTag(hero.getExperience());

		TextView tfExperienceLabel = (TextView) findViewById(R.id.attr_abp_label);
		tfExperienceLabel.setTag(hero.getExperience());
		tfExperienceLabel.setOnLongClickListener(getBaseActivity().getEditListener());

		fillAttributeValue(tfAE, AttributeType.Astralenergie);
		fillAttributeValue(tfAU, AttributeType.Ausdauer);
		fillAttributeValue(tfKE, AttributeType.Karmaenergie);
		fillAttributeValue(tfLE, AttributeType.Lebensenergie);
		fillAttributeValue(tfMR, AttributeType.Magieresistenz);
		fillAttributeValue(tfSO, AttributeType.Sozialstatus);

		fillAttributeLabel(tfLabelMR, AttributeType.Magieresistenz);
		fillAttributeLabel(tfLabelSO, AttributeType.Sozialstatus);

		fillAttributeLabel((TextView) findViewById(R.id.attr_at_label), AttributeType.at);
		fillAttributeLabel((TextView) findViewById(R.id.attr_pa_label), AttributeType.pa);
		fillAttributeLabel((TextView) findViewById(R.id.attr_fk_label), AttributeType.fk);
		fillAttributeLabel((TextView) findViewById(R.id.attr_ini_label), AttributeType.ini);
		fillAttributeLabel((TextView) findViewById(R.id.attr_be_label), AttributeType.Behinderung);

		fillAttributeValue(tfTotalLe, AttributeType.Lebensenergie_Total);
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

		Util.setText(tfST, hero.getLevel(), 0, null);

		int[] ws = hero.getWundschwelle();
		tfWS.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);

		updateBaseInfo(false);
		//

		fillSpecialFeatures(hero);
		registerForIconContextMenu(tfSpecialFeatures);

		// --
		ImageView portrait = (ImageView) findViewById(R.id.gen_portrait);
		portrait.setOnClickListener(this);
		updatePortrait(hero);

		TableLayout attribute2 = (TableLayout) findViewById(R.id.gen_attributes2);
		Util.applyRowStyle(attribute2);

	}

	protected void updateValues() {
		fillAttributesList(charAttributesList);

		fillAttributeValue(tfGS, AttributeType.Geschwindigkeit);

		fillAttributeValue(tfAT, AttributeType.at, false);
		fillAttributeValue(tfPA, AttributeType.pa, false);
		fillAttributeValue(tfFK, AttributeType.fk, false);
		fillAttributeValue(tfINI, AttributeType.ini, false);
		fillAttributeValue(tfBE, AttributeType.Behinderung);
	}

	protected void updateBaseInfo(boolean animate) {

		HeroBaseInfo baseInfo = getHero().getBaseInfo();

		boolean showDetails = preferences.getBoolean(PREF_SHOW_BASEINFO, true);

		if (showDetails) {
			detailsSwitch.setImageResource(R.drawable.expander_ic_maximized);
			Animation slideup = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);

			if (TextUtils.isEmpty(baseInfo.getAussehen())) {
				findViewById(R.id.row_aussehen).setVisibility(View.GONE);
			} else {
				((TextView) findViewById(R.id.gen_aussehen)).setText(baseInfo.getAussehen());
				if (animate)
					findViewById(R.id.row_aussehen).startAnimation(slideup);
				findViewById(R.id.row_aussehen).setVisibility(View.VISIBLE);
			}

			if (TextUtils.isEmpty(baseInfo.getTitel())) {
				findViewById(R.id.row_titel).setVisibility(View.GONE);
			} else {
				((TextView) findViewById(R.id.gen_titel)).setText(baseInfo.getTitel());
				if (animate)
					findViewById(R.id.row_titel).startAnimation(slideup);
				findViewById(R.id.row_titel).setVisibility(View.VISIBLE);
			}

			if (TextUtils.isEmpty(baseInfo.getStand())) {
				findViewById(R.id.row_stand).setVisibility(View.GONE);
			} else {
				((TextView) findViewById(R.id.gen_stand)).setText(baseInfo.getStand());
				if (animate)
					findViewById(R.id.row_stand).startAnimation(slideup);
				findViewById(R.id.row_stand).setVisibility(View.VISIBLE);
			}

			if (TextUtils.isEmpty(baseInfo.getKultur())) {
				findViewById(R.id.row_kultur).setVisibility(View.GONE);
			} else {
				((TextView) findViewById(R.id.gen_kultur)).setText(baseInfo.getKultur());
				if (animate)
					findViewById(R.id.row_kultur).startAnimation(slideup);
				findViewById(R.id.row_kultur).setVisibility(View.VISIBLE);
			}

		} else {
			((ImageButton) findViewById(R.id.details_switch)).setImageResource(R.drawable.expander_ic_minimized);

			findViewById(R.id.row_aussehen).setVisibility(View.GONE);
			findViewById(R.id.row_kultur).setVisibility(View.GONE);
			findViewById(R.id.row_stand).setVisibility(View.GONE);
			findViewById(R.id.row_titel).setVisibility(View.GONE);
		}

		((TextView) findViewById(R.id.gen_groesse)).setText(baseInfo.getGroesse() + " cm");
		((TextView) findViewById(R.id.gen_gewicht)).setText(baseInfo.getGewicht() + " Stein");
		((TextView) findViewById(R.id.gen_rasse)).setText(baseInfo.getRasse());
		((TextView) findViewById(R.id.gen_ausbildung)).setText(baseInfo.getAusbildung());
		((TextView) findViewById(R.id.gen_alter)).setText(Util.toString(baseInfo.getAlter()));
		((TextView) findViewById(R.id.gen_haar_augen)).setText(baseInfo.getHaarFarbe() + " / "
				+ baseInfo.getAugenFarbe());

	}

	protected void updatePortrait(Hero hero) {
		Bitmap drawable = hero.getPortrait();
		if (drawable != null)
			portraitView.setImageBitmap(drawable);
		else
			portraitView.setImageResource(R.drawable.profile_blank);
	}

	protected void fillAttributesList(View view) {

		fillAttributeValue(tfMU, AttributeType.Mut);
		fillAttributeValue(tfKL, AttributeType.Klugheit);
		fillAttributeValue(tfIN, AttributeType.Intuition);
		fillAttributeValue(tfCH, AttributeType.Charisma);
		fillAttributeValue(tfFF, AttributeType.Fingerfertigkeit);
		fillAttributeValue(tfGE, AttributeType.Gewandtheit, false);
		fillAttributeValue(tfKO, AttributeType.Konstitution);
		fillAttributeValue(tfKK, AttributeType.Körperkraft);

		fillAttributeLabel(tfLabelMU, AttributeType.Mut);
		fillAttributeLabel(tfLabelKL, AttributeType.Klugheit);
		fillAttributeLabel(tfLabelIN, AttributeType.Intuition);
		fillAttributeLabel(tfLabelCH, AttributeType.Charisma);
		fillAttributeLabel(tfLabelFF, AttributeType.Fingerfertigkeit);
		fillAttributeLabel(tfLabelGE, AttributeType.Gewandtheit);
		fillAttributeLabel(tfLabelKO, AttributeType.Konstitution);
		fillAttributeLabel(tfLabelKK, AttributeType.Körperkraft);
	}

	/**
	 * @param hero
	 */
	private void fillSpecialFeatures(Hero hero) {

		boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

		StyleableSpannableStringBuilder stringBuilder = new StyleableSpannableStringBuilder();

		if (!hero.getSpecialFeatures().isEmpty()) {
			boolean first = true;
			for (SpecialFeature feature : hero.getSpecialFeatures().values()) {

				if (!first) {
					stringBuilder.append(", ");
				} else {
					first = false;
				}
				stringBuilder.append(feature.toString());
				if (showComments && !TextUtils.isEmpty(feature.getComment())) {
					stringBuilder.appendColor(Color.GRAY, " (");
					stringBuilder.appendColor(Color.GRAY, feature.getComment());
					stringBuilder.appendColor(Color.GRAY, ")");
				}

			}
			tfSpecialFeaturesTitle.setVisibility(View.VISIBLE);
			tfSpecialFeatures.setVisibility(View.VISIBLE);
			tfSpecialFeatures.setText(stringBuilder);
		} else {
			tfSpecialFeatures.setVisibility(View.GONE);
			tfSpecialFeaturesTitle.setVisibility(View.GONE);
		}

		if (!hero.getAdvantages().isEmpty()) {
			stringBuilder.clear();
			boolean first = true;
			for (Advantage advantage : hero.getAdvantages().values()) {

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
			tfAdvantagesTitle.setVisibility(View.VISIBLE);
			tfAdvantages.setVisibility(View.VISIBLE);
			tfAdvantages.setText(stringBuilder);
		} else {
			tfAdvantages.setVisibility(View.GONE);
			tfAdvantagesTitle.setVisibility(View.GONE);
		}

		if (!hero.getDisadvantages().isEmpty()) {
			stringBuilder.clear();
			boolean first = true;
			for (Advantage disadvantage : hero.getDisadvantages().values()) {

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
			tfDisadvantgesTitle.setVisibility(View.VISIBLE);
			tfDisadvantages.setVisibility(View.VISIBLE);
			tfDisadvantages.setText(stringBuilder);
		} else {
			tfDisadvantages.setVisibility(View.GONE);
			tfDisadvantgesTitle.setVisibility(View.GONE);
		}

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