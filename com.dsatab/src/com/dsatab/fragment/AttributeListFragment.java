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

import java.util.List;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.PortraitChooserDialog;
import com.dsatab.view.PortraitViewDialog;
import com.dsatab.view.listener.HeroChangedListener;

/**
 * @author Ganymede
 * 
 */
public class AttributeListFragment extends BaseFragment implements HeroChangedListener, OnClickListener {

	public static final String TAG = "attributeListFragment";

	private TextView tfName;

	private TextView tfLabelLE, tfLabelAU, tfLabelKE, tfLabelAE, tfLabelBE, tfLabelGS, tfLabelWS, tfLabelMR;
	private TextView tfLE, tfAU, tfKE, tfAE, tfBE, tfGS, tfWS, tfMR;

	private TextView tfLabelMU, tfLabelKL, tfLabelIN, tfLabelCH, tfLabelFF, tfLabelGE, tfLabelKO, tfLabelKK;
	private TextView tfMU, tfKL, tfIN, tfCH, tfFF, tfGE, tfKO, tfKK;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.attributes_list, container, false);

		tfName = (TextView) view.findViewById(R.id.attr_name);
		tfLabelAE = (TextView) view.findViewById(R.id.attr_ae_label);
		tfLabelAU = (TextView) view.findViewById(R.id.attr_au_label);
		tfLabelKE = (TextView) view.findViewById(R.id.attr_ke_label);
		tfLabelLE = (TextView) view.findViewById(R.id.attr_le_label);
		tfLabelBE = (TextView) view.findViewById(R.id.attr_be_label);
		tfLabelGS = (TextView) view.findViewById(R.id.attr_gs_label);
		tfLabelWS = (TextView) view.findViewById(R.id.attr_ws_label);
		tfLabelMR = (TextView) view.findViewById(R.id.attr_mr_label);

		tfLabelMU = (TextView) view.findViewById(R.id.attr_mu_label);
		tfLabelKL = (TextView) view.findViewById(R.id.attr_kl_label);
		tfLabelIN = (TextView) view.findViewById(R.id.attr_in_label);
		tfLabelCH = (TextView) view.findViewById(R.id.attr_ch_label);
		tfLabelFF = (TextView) view.findViewById(R.id.attr_ff_label);
		tfLabelGE = (TextView) view.findViewById(R.id.attr_ge_label);
		tfLabelKO = (TextView) view.findViewById(R.id.attr_ko_label);
		tfLabelKK = (TextView) view.findViewById(R.id.attr_kk_label);

		tfAE = (TextView) view.findViewById(R.id.attr_ae);
		tfAU = (TextView) view.findViewById(R.id.attr_au);
		tfKE = (TextView) view.findViewById(R.id.attr_ke);
		tfLE = (TextView) view.findViewById(R.id.attr_le);
		tfBE = (TextView) view.findViewById(R.id.attr_be);
		tfGS = (TextView) view.findViewById(R.id.attr_gs);
		tfWS = (TextView) view.findViewById(R.id.attr_ws);
		tfMR = (TextView) view.findViewById(R.id.attr_mr);

		tfMU = (TextView) view.findViewById(R.id.attr_mu);
		tfKL = (TextView) view.findViewById(R.id.attr_kl);
		tfIN = (TextView) view.findViewById(R.id.attr_in);
		tfCH = (TextView) view.findViewById(R.id.attr_ch);
		tfFF = (TextView) view.findViewById(R.id.attr_ff);
		tfGE = (TextView) view.findViewById(R.id.attr_ge);
		tfKO = (TextView) view.findViewById(R.id.attr_ko);
		tfKK = (TextView) view.findViewById(R.id.attr_kk);

		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onConfigurationChanged(android.content
	 * .res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (tfName != null) {
			Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/harrington.ttf");
			tfName.setTypeface(tf);
			tfName.setOnClickListener(this);
		}

		fillAttributeLabel(tfLabelLE, AttributeType.Lebensenergie);
		fillAttributeLabel(tfLabelAU, AttributeType.Ausdauer);
		fillAttributeLabel(tfLabelKE, AttributeType.Karmaenergie);
		fillAttributeLabel(tfLabelAE, AttributeType.Astralenergie);

		updateView();

		super.onActivityCreated(savedInstanceState);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.attr_name:

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

	private void updateView() {
		SharedPreferences preferences = DSATabApplication.getPreferences();

		if (preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_NAME, true)) {
			tfName.setVisibility(View.VISIBLE);
		} else {
			tfName.setVisibility(View.GONE);
		}

		int visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_LE, true) ? View.VISIBLE : View.GONE;
		tfLE.setVisibility(visible);
		tfLabelLE.setVisibility(visible);

		visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_AU, true) ? View.VISIBLE : View.GONE;
		tfAU.setVisibility(visible);
		tfLabelAU.setVisibility(visible);

		visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_AE, true) ? View.VISIBLE : View.GONE;
		if (visible == View.VISIBLE && getHero() != null
				&& getHero().getAttributeValue(AttributeType.Astralenergie) != null) {
			tfAE.setVisibility(visible);
			tfLabelAE.setVisibility(visible);
		} else {
			tfAE.setVisibility(View.GONE);
			tfLabelAE.setVisibility(View.GONE);
		}

		visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_KE, true) ? View.VISIBLE : View.GONE;
		if (visible == View.VISIBLE && getHero() != null
				&& getHero().getAttributeValue(AttributeType.Karmaenergie) != null) {
			tfKE.setVisibility(visible);
			tfLabelKE.setVisibility(visible);
		} else {
			tfKE.setVisibility(View.GONE);
			tfLabelKE.setVisibility(View.GONE);
		}

		visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_BE, true) ? View.VISIBLE : View.GONE;
		tfBE.setVisibility(visible);
		tfLabelBE.setVisibility(visible);

		visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_MR, true) ? View.VISIBLE : View.GONE;
		tfMR.setVisibility(visible);
		tfLabelMR.setVisibility(visible);

		visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_GS, true) ? View.VISIBLE : View.GONE;
		tfGS.setVisibility(visible);
		tfLabelGS.setVisibility(visible);

		visible = preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_WS, true) ? View.VISIBLE : View.GONE;
		tfWS.setVisibility(visible);
		tfLabelWS.setVisibility(visible);
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
	 * @see
	 * com.dsatab.view.listener.ValueChangedListener#onValueChanged(com.dsatab
	 * .data.Value)
	 */
	@Override
	public void onValueChanged(Value value) {
		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;

			switch (attr.getType()) {
			case Konstitution:
				if (tfWS != null && getHero() != null) {
					int[] ws = getHero().getWundschwelle();
					tfWS.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);
				}
				// no break because we have to call fillAttribute too!!!
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Gewandtheit:
			case Charisma:
				fillAttribute(attr);
				break;
			case Lebensenergie:
				fillAttributeValue(tfLE, AttributeType.Lebensenergie, null, true, true);
				break;
			case Ausdauer:
				fillAttributeValue(tfAU, AttributeType.Ausdauer, null, true, true);
				break;
			case Karmaenergie:
				fillAttributeValue(tfKE, AttributeType.Karmaenergie, null, true, true);
				break;
			case Astralenergie:
				fillAttributeValue(tfAE, AttributeType.Astralenergie, null, true, true);
				break;
			case Behinderung:
				fillAttributeValue(tfBE, AttributeType.Behinderung, null, true, true);
				fillAttributeValue(tfGS, AttributeType.Geschwindigkeit, null, true, true);
				break;
			case Geschwindigkeit:
				fillAttributeValue(tfGS, AttributeType.Geschwindigkeit, null, true, true);
				break;
			default:
				break;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onModifierAdded(com.dsatab.data.modifier
	 * .Modificator)
	 */
	@Override
	public void onModifierAdded(Modificator value) {
		super.onModifierAdded(value);
		fillAttributesList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onModifierChanged(com.dsatab.data.modifier
	 * .Modificator)
	 */
	@Override
	public void onModifierChanged(Modificator value) {
		super.onModifierChanged(value);
		fillAttributesList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onModifierRemoved(com.dsatab.data.modifier
	 * .Modificator)
	 */
	@Override
	public void onModifierRemoved(Modificator value) {
		super.onModifierRemoved(value);
		fillAttributesList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onModifiersChanged(java.util.List)
	 */
	@Override
	public void onModifiersChanged(List<Modificator> values) {
		super.onModifiersChanged(values);
		fillAttributesList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {
		if (tfName != null)
			tfName.setText(hero.getName());

		fillAttributesList();
	}

	protected void fillAttributesList() {

		fillAttributeValue(tfMU, AttributeType.Mut, null, true, true);
		fillAttributeValue(tfKL, AttributeType.Klugheit, null, true, true);
		fillAttributeValue(tfIN, AttributeType.Intuition, null, true, true);
		fillAttributeValue(tfCH, AttributeType.Charisma, null, true, true);
		fillAttributeValue(tfFF, AttributeType.Fingerfertigkeit, null, true, true);
		fillAttributeValue(tfGE, AttributeType.Gewandtheit, null, false, true);
		fillAttributeValue(tfKO, AttributeType.Konstitution, null, true, true);
		fillAttributeValue(tfKK, AttributeType.Körperkraft, null, true, true);

		fillAttributeLabel(tfLabelMU, AttributeType.Mut);
		fillAttributeLabel(tfLabelKL, AttributeType.Klugheit);
		fillAttributeLabel(tfLabelIN, AttributeType.Intuition);
		fillAttributeLabel(tfLabelCH, AttributeType.Charisma);
		fillAttributeLabel(tfLabelFF, AttributeType.Fingerfertigkeit);
		fillAttributeLabel(tfLabelGE, AttributeType.Gewandtheit);
		fillAttributeLabel(tfLabelKO, AttributeType.Konstitution);
		fillAttributeLabel(tfLabelKK, AttributeType.Körperkraft);

		fillAttributeValue(tfLE, AttributeType.Lebensenergie, null, true, true);
		fillAttributeLabel(tfLabelLE, AttributeType.Lebensenergie);

		fillAttributeValue(tfAU, AttributeType.Ausdauer, null, true, true);
		fillAttributeLabel(tfLabelAU, AttributeType.Ausdauer);

		fillAttributeValue(tfMR, AttributeType.Magieresistenz, null, true, true);
		fillAttributeLabel(tfLabelMR, AttributeType.Magieresistenz);

		final Hero hero = getHero();

		if (hero.getAttributeValue(AttributeType.Karmaenergie) == null) {
			tfKE.setVisibility(View.GONE);
			tfLabelKE.setVisibility(View.GONE);
		} else if (preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_KE, true)) {
			fillAttributeValue(tfKE, AttributeType.Karmaenergie, null, true, true);
			fillAttributeLabel(tfLabelKE, AttributeType.Karmaenergie);
			tfKE.setVisibility(View.VISIBLE);
			tfLabelKE.setVisibility(View.VISIBLE);
		}

		if (hero.getAttributeValue(AttributeType.Astralenergie) == null) {
			tfAE.setVisibility(View.GONE);
			tfLabelAE.setVisibility(View.GONE);
		} else if (preferences.getBoolean(BasePreferenceActivity.KEY_HEADER_AE, true)) {
			fillAttributeValue(tfAE, AttributeType.Astralenergie, null, true, true);
			fillAttributeLabel(tfLabelAE, AttributeType.Astralenergie);
			tfAE.setVisibility(View.VISIBLE);
			tfLabelAE.setVisibility(View.VISIBLE);
		}

		fillAttributeLabel(tfLabelGS, AttributeType.Geschwindigkeit);
		fillAttributeValue(tfGS, AttributeType.Geschwindigkeit, null, true, true);

		fillAttributeValue(tfBE, AttributeType.Behinderung, null, true, true);
		fillAttributeLabel(tfLabelBE, AttributeType.Behinderung);

		if (tfWS != null) {
			int[] ws = hero.getWundschwelle();
			tfWS.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);
		}

	}

	protected void fillAttribute(Attribute attr) {
		switch (attr.getType()) {
		case Mut:
			fillAttributeValue(tfMU, AttributeType.Mut, null, true, true);
			fillAttributeLabel(tfLabelMU, AttributeType.Mut);
			break;
		case Klugheit:
			fillAttributeValue(tfKL, AttributeType.Klugheit, null, true, true);
			fillAttributeLabel(tfLabelKL, AttributeType.Klugheit);
			break;
		case Intuition:
			fillAttributeValue(tfIN, AttributeType.Intuition, null, true, true);
			fillAttributeLabel(tfLabelIN, AttributeType.Intuition);
			break;
		case Charisma:
			fillAttributeValue(tfCH, AttributeType.Charisma, null, true, true);
			fillAttributeLabel(tfLabelCH, AttributeType.Charisma);
			break;
		case Fingerfertigkeit:
			fillAttributeValue(tfFF, AttributeType.Fingerfertigkeit, null, true, true);
			fillAttributeLabel(tfLabelFF, AttributeType.Fingerfertigkeit);
			break;
		case Gewandtheit:
			fillAttributeValue(tfGE, AttributeType.Gewandtheit, null, false, true);
			fillAttributeLabel(tfLabelGE, AttributeType.Gewandtheit);
			break;
		case Konstitution:
			fillAttributeValue(tfKO, AttributeType.Konstitution, null, true, true);
			fillAttributeLabel(tfLabelKO, AttributeType.Konstitution);
			break;
		case Körperkraft:
			fillAttributeValue(tfKK, AttributeType.Körperkraft, null, true, true);
			fillAttributeLabel(tfLabelKK, AttributeType.Körperkraft);
			break;
		default:
			break;
		}
	}

	public void onHeroUnloaded(Hero hero) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onSharedPreferenceChanged(android.content
	 * .SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.startsWith("header_")) {
			updateView();
		}
		super.onSharedPreferenceChanged(sharedPreferences, key);
	}

}
