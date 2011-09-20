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

import android.content.Intent;
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
import com.dsatab.activity.BaseMainActivity;
import com.dsatab.activity.DsaPreferenceActivity;
import com.dsatab.common.Util;
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

	private TextView tfName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.attributes_list, container, false);
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
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		tfName = (TextView) findViewById(R.id.attr_name);
		if (tfName != null) {
			Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/harrington.ttf");
			tfName.setTypeface(tf);
			tfName.setOnClickListener(this);
		}

		fillAttributeLabel((TextView) findViewById(R.id.attr_le_label), AttributeType.Lebensenergie);
		fillAttributeLabel((TextView) findViewById(R.id.attr_au_label), AttributeType.Ausdauer);
		fillAttributeLabel((TextView) findViewById(R.id.attr_ke_label), AttributeType.Karmaenergie);
		fillAttributeLabel((TextView) findViewById(R.id.attr_ae_label), AttributeType.Astralenergie);

		updateView();

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onAttachListener(com.dsatab.data.Hero)
	 */
	@Override
	protected void onAttachListener(Hero hero) {
		super.onAttachListener(hero);
		if (hero != null)
			hero.addHeroChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onDetachListener(com.dsatab.data.Hero)
	 */
	@Override
	protected void onDetachListener(Hero hero) {
		super.onDetachListener(hero);
		if (hero != null)
			hero.removeHeroChangedListener(this);
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

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_NAME, true)) {
			findViewById(R.id.attr_name).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.attr_name).setVisibility(View.GONE);
		}

		int visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_LE, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_le).setVisibility(visible);
		findViewById(R.id.attr_le_label).setVisibility(visible);

		visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_AU, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_au).setVisibility(visible);
		findViewById(R.id.attr_au_label).setVisibility(visible);

		visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_AE, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_ae).setVisibility(visible);
		findViewById(R.id.attr_ae_label).setVisibility(visible);

		visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_KE, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_ke).setVisibility(visible);
		findViewById(R.id.attr_ke_label).setVisibility(visible);

		visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_BE, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_be).setVisibility(visible);
		findViewById(R.id.attr_be_label).setVisibility(visible);

		visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_MR, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_mr).setVisibility(visible);
		findViewById(R.id.attr_mr_label).setVisibility(visible);

		visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_GS, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_gs).setVisibility(visible);
		findViewById(R.id.attr_gs_label).setVisibility(visible);

		visible = preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_WS, true) ? View.VISIBLE : View.GONE;
		findViewById(R.id.attr_ws).setVisibility(visible);
		findViewById(R.id.attr_ws_label).setVisibility(visible);
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
				TextView tfWs = (TextView) findViewById(R.id.attr_ws);
				if (tfWs != null && getHero() != null) {
					int[] ws = getHero().getWundschwelle();
					tfWs.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);
				}
				// no break because we have to call fillAttribute too!!!
			case Mut:
			case Klugheit:
			case Intuition:
			case Körperkraft:
			case Fingerfertigkeit:
			case Gewandtheit:
			case Charisma:
				fillAttribute(getView(), attr);
				break;
			case Lebensenergie:
				fillAttributeValue((TextView) findViewById(R.id.attr_le), AttributeType.Lebensenergie);
				break;
			case Ausdauer:
				fillAttributeValue((TextView) findViewById(R.id.attr_au), AttributeType.Ausdauer);
				break;
			case Karmaenergie:
				fillAttributeValue((TextView) findViewById(R.id.attr_ke), AttributeType.Karmaenergie);
				break;
			case Astralenergie:
				fillAttributeValue((TextView) findViewById(R.id.attr_ae), AttributeType.Astralenergie);
				break;
			case Behinderung:
				fillAttributeValue((TextView) findViewById(R.id.attr_be), AttributeType.Behinderung);
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
		fillAttributesList(findViewById(R.id.gen_attributes));
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
		fillAttributesList(findViewById(R.id.gen_attributes));
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
		fillAttributesList(findViewById(R.id.gen_attributes));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onModifiersChanged(java.util.List)
	 */
	@Override
	public void onModifiersChanged(List<Modificator> values) {
		super.onModifiersChanged(values);
		fillAttributesList(findViewById(R.id.gen_attributes));
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

		fillAttributesList(getView());

		fillAttributeValue((TextView) findViewById(R.id.attr_le), AttributeType.Lebensenergie);
		fillAttributeLabel((TextView) findViewById(R.id.attr_le_label), AttributeType.Lebensenergie);
		fillAttributeValue((TextView) findViewById(R.id.attr_au), AttributeType.Ausdauer);
		fillAttributeLabel((TextView) findViewById(R.id.attr_au_label), AttributeType.Ausdauer);

		if (hero.getAttributeValue(AttributeType.Karmaenergie) == null) {
			findViewById(R.id.attr_ke).setVisibility(View.GONE);
			findViewById(R.id.attr_ke_label).setVisibility(View.GONE);
		} else if (preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_KE, true)) {
			fillAttributeValue((TextView) findViewById(R.id.attr_ke), AttributeType.Karmaenergie);
			fillAttributeLabel((TextView) findViewById(R.id.attr_ke_label), AttributeType.Karmaenergie);
			findViewById(R.id.attr_ke).setVisibility(View.VISIBLE);
			findViewById(R.id.attr_ke_label).setVisibility(View.VISIBLE);
		}

		if (hero.getAttributeValue(AttributeType.Astralenergie) == null) {
			findViewById(R.id.attr_ae).setVisibility(View.GONE);
			findViewById(R.id.attr_ae_label).setVisibility(View.GONE);
		} else if (preferences.getBoolean(DsaPreferenceActivity.KEY_HEADER_AE, true)) {
			fillAttributeValue((TextView) findViewById(R.id.attr_ae), AttributeType.Astralenergie);
			fillAttributeLabel((TextView) findViewById(R.id.attr_ae_label), AttributeType.Astralenergie);
			findViewById(R.id.attr_ae).setVisibility(View.VISIBLE);
			findViewById(R.id.attr_ae_label).setVisibility(View.VISIBLE);
		}

		Util.setText((TextView) findViewById(R.id.attr_gs), hero.getGs(), 0, null);

		fillAttributeValue((TextView) findViewById(R.id.attr_be), AttributeType.Behinderung);
		fillAttributeLabel((TextView) findViewById(R.id.attr_be_label), AttributeType.Behinderung);

		TextView tfWs = (TextView) findViewById(R.id.attr_ws);
		if (tfWs != null) {
			int[] ws = hero.getWundschwelle();
			tfWs.setText(ws[0] + "/" + ws[1] + "/" + ws[2]);
		}
	}

	protected void fillAttributesList(View view) {
		fillAttributeValue((TextView) view.findViewById(R.id.attr_mu), AttributeType.Mut);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_kl), AttributeType.Klugheit);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_in), AttributeType.Intuition);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ch), AttributeType.Charisma);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ff), AttributeType.Fingerfertigkeit);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ge), AttributeType.Gewandtheit);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ko), AttributeType.Konstitution);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_kk), AttributeType.Körperkraft);

		fillAttributeLabel((TextView) view.findViewById(R.id.attr_mu_label), AttributeType.Mut);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_kl_label), AttributeType.Klugheit);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_in_label), AttributeType.Intuition);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ch_label), AttributeType.Charisma);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ff_label), AttributeType.Fingerfertigkeit);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ge_label), AttributeType.Gewandtheit);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ko_label), AttributeType.Konstitution);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_kk_label), AttributeType.Körperkraft);
	}

	protected void fillAttributeValue(TextView tv, AttributeType type) {
		if (getHero() == null)
			return;

		if (tv == null)
			return;

		Attribute attribute = getHero().getAttribute(type);
		int modifier = getHero().getModificator(type);
		if (attribute != null) {

			Util.setText(tv, attribute.getValue(), modifier, null);
			tv.setTag(attribute);

			if (!tv.isLongClickable()) {

				if (type == AttributeType.Lebensenergie || type == AttributeType.Lebensenergie_Total
						|| type == AttributeType.Karmaenergie || type == AttributeType.Karmaenergie_Total
						|| type == AttributeType.Astralenergie || type == AttributeType.Astralenergie_Total
						|| type == AttributeType.Ausdauer || type == AttributeType.Ausdauer_Total
						|| type == AttributeType.Behinderung) {
					tv.setOnClickListener(getBaseActivity().getEditListener());
				} else if (type.probable()) {
					tv.setOnClickListener(getBaseActivity().getProbeListener());
				}
				tv.setOnLongClickListener(getBaseActivity().getEditListener());
			}
		}
	}

	protected void fillAttribute(View view, Attribute attr) {
		switch (attr.getType()) {
		case Mut:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_mu), AttributeType.Mut);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_mu_label), AttributeType.Mut);
			break;
		case Klugheit:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_kl), AttributeType.Klugheit);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_kl_label), AttributeType.Klugheit);
			break;
		case Intuition:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_in), AttributeType.Intuition);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_in_label), AttributeType.Intuition);
			break;
		case Charisma:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ch), AttributeType.Charisma);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ch_label), AttributeType.Charisma);
			break;
		case Fingerfertigkeit:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ff), AttributeType.Fingerfertigkeit);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ff_label), AttributeType.Fingerfertigkeit);
			break;
		case Gewandtheit:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ge), AttributeType.Gewandtheit);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ge_label), AttributeType.Gewandtheit);
			break;
		case Konstitution:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ko), AttributeType.Konstitution);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ko_label), AttributeType.Konstitution);
			break;
		case Körperkraft:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_kk), AttributeType.Körperkraft);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_kk_label), AttributeType.Körperkraft);
			break;
		}
	}

	public void onHeroUnloaded(Hero hero) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BaseMainActivity.ACTION_PREFERENCES) {
			updateView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
