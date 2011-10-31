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
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Advantage;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroBaseInfo;
import com.dsatab.data.SpecialFeature;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.PortraitViewDialog;
import com.gandulf.guilib.util.Debug;

public class CharacterFragment extends BaseFragment implements OnClickListener {

	private static final String PREF_SHOW_FEATURE_COMMENTS = "SHOW_COMMENTS";
	private static final String PREF_SHOW_BASEINFO = "SHOW_BASEINFO";

	private static final int ACTION_PHOTO = 1;
	private static final int ACTION_GALERY = 2;
	private static final int CONTEXTMENU_COMMENTS_TOGGLE = 14;

	private TextView tfSpecialFeatures, tfExperience, tfTotalLp, tfTotalAu, tfTotalAe, tfTotalKe, tfGs, tfWs;

	private View charAttributesList;

	private ImageButton detailsSwitch;

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
		Debug.verbose(getClass().getName() + " createView");
		View root = configureContainerView(inflater.inflate(R.layout.sheet_character, container, false));

		detailsSwitch = (ImageButton) root.findViewById(R.id.details_switch);

		charAttributesList = root.findViewById(R.id.gen_attributes);
		tfExperience = (TextView) root.findViewById(R.id.attr_abp);

		tfTotalAe = (TextView) root.findViewById(R.id.attr_total_ae);
		tfTotalKe = (TextView) root.findViewById(R.id.attr_total_ke);
		tfTotalLp = (TextView) root.findViewById(R.id.attr_total_lp);
		tfTotalAu = (TextView) root.findViewById(R.id.attr_total_au);

		tfGs = (TextView) root.findViewById(R.id.attr_gs);
		tfWs = (TextView) root.findViewById(R.id.attr_ws);

		tfSpecialFeatures = (TextView) root.findViewById(R.id.gen_specialfeatures);

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
			if (resultCode == Activity.RESULT_OK) {
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
					Bitmap yourSelectedImage = Util.decodeFile(new File(filePath), 300);

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
			String photoName = "photo" + getHero().getName();
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

	}

	@Override
	public void onModifierRemoved(Modificator value) {

	}

	@Override
	public void onModifierChanged(Modificator value) {

	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {

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
				fillAttributeValue((TextView) findViewById(R.id.attr_lp), AttributeType.Lebensenergie);
				break;
			case Lebensenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.attr_lp), AttributeType.Lebensenergie);
				fillAttributeValue(tfTotalLp, AttributeType.Lebensenergie_Total);
				break;
			case Astralenergie:
				fillAttributeValue((TextView) findViewById(R.id.attr_ae), AttributeType.Astralenergie);
				break;
			case Astralenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.attr_ae), AttributeType.Astralenergie);
				fillAttributeValue(tfTotalAe, AttributeType.Astralenergie_Total);
				break;
			case Ausdauer:
				fillAttributeValue((TextView) findViewById(R.id.attr_au), AttributeType.Ausdauer);
				break;
			case Ausdauer_Total:
				fillAttributeValue((TextView) findViewById(R.id.attr_au), AttributeType.Ausdauer);
				fillAttributeValue(tfTotalAu, AttributeType.Ausdauer_Total);
				break;
			case Karmaenergie:
				fillAttributeValue((TextView) findViewById(R.id.attr_ke), AttributeType.Karmaenergie);
				break;
			case Karmaenergie_Total:
				fillAttributeValue((TextView) findViewById(R.id.attr_ke), AttributeType.Karmaenergie);
				fillAttributeValue(tfTotalKe, AttributeType.Karmaenergie_Total);
				break;
			case Magieresistenz:
				fillAttributeValue((TextView) findViewById(R.id.attr_mr), AttributeType.Magieresistenz);
				break;
			case Sozialstatus:
				fillAttributeValue((TextView) findViewById(R.id.attr_so), AttributeType.Sozialstatus);
				break;
			case at:
				fillAttributeValue((TextView) findViewById(R.id.attr_at), AttributeType.at);
				break;
			case pa:
				fillAttributeValue((TextView) findViewById(R.id.attr_pa), AttributeType.pa);
				break;
			case fk:
				fillAttributeValue((TextView) findViewById(R.id.attr_fk), AttributeType.fk);
				break;
			case ini:
				fillAttributeValue((TextView) findViewById(R.id.attr_ini), AttributeType.ini);
				break;
			case Behinderung:
				fillAttributeValue((TextView) findViewById(R.id.attr_be), AttributeType.Behinderung);
				fillAttributeValue(tfGs, AttributeType.Geschwindigkeit);
				break;
			case Geschwindigkeit:
				fillAttributeValue(tfGs, AttributeType.Geschwindigkeit);
				break;
			case Gewandtheit:
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

		fillAttributeValue((TextView) findViewById(R.id.attr_ae), AttributeType.Astralenergie);
		fillAttributeValue((TextView) findViewById(R.id.attr_au), AttributeType.Ausdauer);
		fillAttributeValue((TextView) findViewById(R.id.attr_ke), AttributeType.Karmaenergie);
		fillAttributeValue((TextView) findViewById(R.id.attr_lp), AttributeType.Lebensenergie);
		fillAttributeValue((TextView) findViewById(R.id.attr_mr), AttributeType.Magieresistenz);
		fillAttributeValue((TextView) findViewById(R.id.attr_so), AttributeType.Sozialstatus);

		fillAttributeValue((TextView) findViewById(R.id.attr_at), AttributeType.at, false);
		fillAttributeValue((TextView) findViewById(R.id.attr_pa), AttributeType.pa, false);
		fillAttributeValue((TextView) findViewById(R.id.attr_fk), AttributeType.fk, false);
		fillAttributeValue((TextView) findViewById(R.id.attr_ini), AttributeType.ini, false);
		fillAttributeValue((TextView) findViewById(R.id.attr_be), AttributeType.Behinderung);

		fillAttributeLabel((TextView) findViewById(R.id.attr_at_label), AttributeType.at);
		fillAttributeLabel((TextView) findViewById(R.id.attr_pa_label), AttributeType.pa);
		fillAttributeLabel((TextView) findViewById(R.id.attr_fk_label), AttributeType.fk);
		fillAttributeLabel((TextView) findViewById(R.id.attr_ini_label), AttributeType.ini);
		fillAttributeLabel((TextView) findViewById(R.id.attr_be_label), AttributeType.Behinderung);

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

		Util.setText((TextView) findViewById(R.id.attr_st), hero.getLevel(), 0, null);

		fillAttributeValue(tfGs, AttributeType.Geschwindigkeit);

		int[] ws = hero.getWundschwelle();
		tfWs.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);

		updateBaseInfo(false);
		//

		fillSpecialFeatures(hero);
		registerForIconContextMenu(tfSpecialFeatures);

		// --
		ImageView portrait = (ImageView) findViewById(R.id.gen_portrait);
		portrait.setOnClickListener(this);
		updatePortrait(hero);

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

	/**
	 * @param hero
	 */
	private void fillSpecialFeatures(Hero hero) {

		boolean showComments = preferences.getBoolean(PREF_SHOW_FEATURE_COMMENTS, true);

		StyleableSpannableStringBuilder stringBuilder = new StyleableSpannableStringBuilder();

		if (!hero.getSpecialFeatures().isEmpty()) {
			stringBuilder.appendBold(getString(R.string.specialfeatures));
			stringBuilder.appendBold(": ");
			boolean first = true;
			for (SpecialFeature feature : hero.getSpecialFeatures()) {

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

		}

		if (!hero.getAdvantages().isEmpty()) {
			if (stringBuilder.length() > 0)
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
			if (stringBuilder.length() > 0)
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