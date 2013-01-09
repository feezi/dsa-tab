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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.dsatab.R;
import com.dsatab.common.StyleableSpannableStringBuilder;
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
import com.dsatab.util.Util;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.PortraitViewDialog;

public class CharacterFragment extends BaseAttributesFragment implements OnClickListener, OnLongClickListener {

	private static final String PREF_SHOW_FEATURE_COMMENTS = "SHOW_COMMENTS";
	private static final String PREF_SHOW_BASEINFO = "SHOW_BASEINFO";

	private static final int ACTION_PHOTO = 1;
	private static final int ACTION_GALERY = 2;

	private TextView tfSpecialFeatures, tfSpecialFeaturesTitle, tfAdvantages, tfAdvantagesTitle, tfDisadvantages,
			tfDisadvantgesTitle;
	private TextView tfExperience, tfTotalLe, tfTotalAu, tfTotalAe, tfTotalKe, tfAT, tfPA, tfFK, tfINI, tfST;
	private TextView tfLabelExperience, tfLabelAT, tfLabelPA, tfLabelFK, tfLabelINI;

	private View charAttributesList;
	private ImageButton detailsSwitch;

	private ImageView portraitView;

	protected ActionMode mMode;

	protected ActionMode.Callback mCallback = new PortraitActionMode();

	private final class PortraitActionMode implements ActionMode.Callback {
		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {

			switch (item.getItemId()) {
			case R.id.option_take_photo:
				Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				getActivity().startActivityForResult(camera, ACTION_PHOTO);
				break;
			case R.id.option_pick_image:
				Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
				photoPickerIntent.setType("image/*");
				getActivity().startActivityForResult(Intent.createChooser(photoPickerIntent, "Bild auswählen"),
						ACTION_GALERY);
				break;
			case R.id.option_view_portrait:
				showPortrait();
				break;
			case R.id.option_pick_avatar:
				PortraitChooserDialog pdialog = new PortraitChooserDialog(getBaseActivity());
				if (!pdialog.isEmpty()) {
					pdialog.show();
				}
				break;
			}
			mode.finish();
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			if (getHero() != null) {
				mode.getMenuInflater().inflate(R.menu.portrait_popupmenu, menu);
				mode.setTitle("Portrait");
				mode.setSubtitle(null);
				if (getHero().getPortrait() == null) {
					menu.findItem(R.id.option_view_portrait).setVisible(false);
				}
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.actionbarsherlock.view.ActionMode.Callback#onPrepareActionMode
		 * (com.actionbarsherlock.view.ActionMode,
		 * com.actionbarsherlock.view.Menu)
		 */
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

	}

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
		tfLabelExperience = (TextView) root.findViewById(R.id.attr_abp_label);

		tfTotalAe = (TextView) root.findViewById(R.id.attr_total_ae);
		tfTotalKe = (TextView) root.findViewById(R.id.attr_total_ke);
		tfTotalLe = (TextView) root.findViewById(R.id.attr_total_le);
		tfTotalAu = (TextView) root.findViewById(R.id.attr_total_au);

		tfAT = (TextView) root.findViewById(R.id.attr_at);
		tfPA = (TextView) root.findViewById(R.id.attr_pa);
		tfFK = (TextView) root.findViewById(R.id.attr_fk);
		tfINI = (TextView) root.findViewById(R.id.attr_ini);
		tfST = (TextView) root.findViewById(R.id.attr_st);

		tfLabelAT = (TextView) root.findViewById(R.id.attr_at_label);
		tfLabelPA = (TextView) root.findViewById(R.id.attr_pa_label);
		tfLabelFK = (TextView) root.findViewById(R.id.attr_fk_label);
		tfLabelINI = (TextView) root.findViewById(R.id.attr_ini_label);

		tfSpecialFeatures = (TextView) root.findViewById(R.id.gen_specialfeatures);
		tfSpecialFeaturesTitle = (TextView) root.findViewById(R.id.gen_specialfeatures_title);

		tfAdvantages = (TextView) root.findViewById(R.id.gen_advantages);
		tfAdvantagesTitle = (TextView) root.findViewById(R.id.gen_advantages_title);

		tfDisadvantages = (TextView) root.findViewById(R.id.gen_disadvantages);
		tfDisadvantgesTitle = (TextView) root.findViewById(R.id.gen_disadvantages_title);

		portraitView = (ImageView) root.findViewById(R.id.gen_portrait);

		Util.applyRowStyle((TableLayout) root.findViewById(R.id.gen_attributes));

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		detailsSwitch.setOnClickListener(this);
		tfExperience.setOnClickListener(getBaseActivity().getEditListener());
		tfExperience.setOnLongClickListener(getBaseActivity().getEditListener());
		findViewById(R.id.gen_description).setOnClickListener(this);

		tfSpecialFeatures.setOnClickListener(this);
		tfSpecialFeaturesTitle.setOnClickListener(this);
		tfAdvantages.setOnClickListener(this);
		tfAdvantagesTitle.setOnClickListener(this);
		tfDisadvantages.setOnClickListener(this);
		tfDisadvantgesTitle.setOnClickListener(this);

		portraitView.setOnLongClickListener(this);
		portraitView.setOnClickListener(this);

		fillAttributeLabel((View) tfLabelMR.getParent(), AttributeType.Magieresistenz);
		fillAttributeLabel((View) tfLabelSO.getParent(), AttributeType.Sozialstatus);
		fillAttributeLabel((View) tfLabelBE.getParent(), AttributeType.Behinderung);

		fillAttributeLabel(tfLabelAT, AttributeType.at);
		fillAttributeLabel(tfLabelPA, AttributeType.pa);
		fillAttributeLabel(tfLabelFK, AttributeType.fk);
		fillAttributeLabel(tfLabelINI, AttributeType.ini);

		super.onActivityCreated(savedInstanceState);
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
				if (cursor != null) {
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
				} else {
					Toast.makeText(getActivity(),
							"Konnte Bild nicht öffnen. Verwende die Standard Gallerie um eine bild auszuwählen.",
							Toast.LENGTH_LONG).show();
				}
			}

			break;
		case ACTION_PHOTO:

			if (resultCode == Activity.RESULT_OK && getHero() != null) {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.gen_portrait:
			if (getHero() == null)
				return false;

			if (mMode == null) {
				mMode = ((SherlockFragmentActivity) getActivity()).startActionMode(mCallback);
				customizeActionModeCloseButton();
				mMode.invalidate();
			}
			return true;
		}
		return false;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gen_portrait:

			if (getHero() == null)
				return;

			if (getHero().getPortrait() == null) {
				onLongClick(v);
			} else {
				showPortrait();
			}
			break;
		case R.id.gen_specialfeatures:
		case R.id.gen_specialfeatures_title:
		case R.id.gen_advantages:
		case R.id.gen_advantages_title:
		case R.id.gen_disadvantages:
		case R.id.gen_disadvantages_title: {
			boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

			showComments = !showComments;
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_FEATURE_COMMENTS, showComments);
			edit.commit();

			fillSpecialFeatures(getHero());
			break;
		}
		case R.id.gen_description:
		case R.id.details_switch: {
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_BASEINFO, !preferences.getBoolean(PREF_SHOW_BASEINFO, true));
			edit.commit();
			updateBaseInfo(true);
			break;
		}
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
			case Lebensenergie_Aktuell:
				fillAttributeValue(tfLE, attr);
				break;
			case Lebensenergie:
				fillAttributeValue(tfTotalLe, attr);
				break;
			case Astralenergie_Aktuell:
				fillAttributeValue(tfAE, attr);
				break;
			case Astralenergie:
				fillAttributeValue(tfTotalAe, attr);
				break;
			case Ausdauer_Aktuell:
				fillAttributeValue(tfAU, attr);
				break;
			case Ausdauer:
				fillAttributeValue(tfTotalAu, attr);
				break;
			case Karmaenergie_Aktuell:
				fillAttributeValue(tfKE, attr);
				break;
			case Karmaenergie:
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

		View xpRow = (View) tfLabelExperience.getParent();
		xpRow.setTag(hero.getExperience());
		xpRow.setOnLongClickListener(getBaseActivity().getEditListener());
		xpRow.setOnClickListener(getBaseActivity().getEditListener());

		fillAttributeValue(tfAE, AttributeType.Astralenergie_Aktuell);
		fillAttributeValue(tfAU, AttributeType.Ausdauer_Aktuell);
		fillAttributeValue(tfKE, AttributeType.Karmaenergie_Aktuell);
		fillAttributeValue(tfLE, AttributeType.Lebensenergie_Aktuell);
		fillAttributeValue(tfMR, AttributeType.Magieresistenz);
		fillAttributeValue(tfSO, AttributeType.Sozialstatus);

		fillAttributeValue(tfTotalLe, AttributeType.Lebensenergie);
		fillAttributeValue(tfTotalAu, AttributeType.Ausdauer);

		if (hero.getAttributeValue(AttributeType.Karmaenergie) == null
				|| hero.getAttributeValue(AttributeType.Karmaenergie) == 0) {
			findViewById(R.id.row_ke).setVisibility(View.GONE);
		} else {
			fillAttributeValue(tfTotalKe, AttributeType.Karmaenergie);
			findViewById(R.id.row_ke).setVisibility(View.VISIBLE);
		}

		if (hero.getAttributeValue(AttributeType.Astralenergie) == null
				|| hero.getAttributeValue(AttributeType.Astralenergie) == 0) {
			findViewById(R.id.row_ae).setVisibility(View.GONE);
		} else {
			fillAttributeValue(tfTotalAe, AttributeType.Astralenergie);
			findViewById(R.id.row_ae).setVisibility(View.VISIBLE);
		}

		Util.setText(tfST, hero.getLevel(), 0, null);

		int[] ws = hero.getWundschwelle();
		tfWS.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);

		updateBaseInfo(false);
		//

		fillSpecialFeatures(hero);

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
			detailsSwitch.setImageResource(Util.getThemeResourceId(getActivity(), R.attr.imgExpanderClose));
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
			detailsSwitch.setImageResource(Util.getThemeResourceId(getActivity(), R.attr.imgExpanderOpen));

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
			portraitView.setImageResource(R.drawable.profile_picture);
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

		fillAttributeLabel((View) tfLabelMU.getParent(), AttributeType.Mut);
		fillAttributeLabel((View) tfLabelKL.getParent(), AttributeType.Klugheit);
		fillAttributeLabel((View) tfLabelIN.getParent(), AttributeType.Intuition);
		fillAttributeLabel((View) tfLabelCH.getParent(), AttributeType.Charisma);
		fillAttributeLabel((View) tfLabelFF.getParent(), AttributeType.Fingerfertigkeit);
		fillAttributeLabel((View) tfLabelGE.getParent(), AttributeType.Gewandtheit);
		fillAttributeLabel((View) tfLabelKO.getParent(), AttributeType.Konstitution);
		fillAttributeLabel((View) tfLabelKK.getParent(), AttributeType.Körperkraft);
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