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

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.data.Attribute;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.util.Util;

/**
 * @author Ganymede
 * 
 */
public abstract class BaseAttributesFragment extends BaseFragment {

	protected TextView tfLabelLE, tfLabelAU, tfLabelKE, tfLabelAE, tfLabelBE, tfLabelGS, tfLabelWS, tfLabelMR;
	protected TextView tfLE, tfAU, tfKE, tfAE, tfBE, tfGS, tfWS, tfMR;

	protected TextView tfLabelMU, tfLabelKL, tfLabelIN, tfLabelCH, tfLabelFF, tfLabelGE, tfLabelKO, tfLabelKK;
	protected TextView tfMU, tfKL, tfIN, tfCH, tfFF, tfGE, tfKO, tfKK;

	protected TextView tfLabelSO;
	protected TextView tfSO;

	protected boolean inverseColors = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViews(view);
	}

	/**
	 * 
	 */
	protected void findViews(View view) {
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

		tfLabelSO = (TextView) view.findViewById(R.id.attr_so_label);
		tfSO = (TextView) view.findViewById(R.id.attr_so);

	}

	protected void fillAttributeLabel(View tv, AttributeType type) {

		if (!tv.isLongClickable()) {
			if (type == AttributeType.Behinderung || type == AttributeType.Sozialstatus
					|| type == AttributeType.Magieresistenz) {
				tv.setOnClickListener(getBaseActivity().getEditListener());
			} else if (type.probable()) {
				tv.setOnClickListener(getBaseActivity().getProbeListener());
			}
			tv.setOnLongClickListener(getBaseActivity().getEditListener());
		}
		tv.setTag(type);
	}

	protected void fillAttributeValue(TextView tv, Attribute attr) {
		fillAttributeValue(tv, attr, null, true, this.inverseColors);
	}

	protected void fillAttributeValue(TextView tv, Attribute attr, boolean inverseColors) {
		fillAttributeValue(tv, attr, null, true, inverseColors);
	}

	protected void fillAttributeValue(TextView tv, AttributeType type) {
		fillAttributeValue(tv, type, null, true, this.inverseColors);
	}

	protected void fillAttributeValue(TextView tv, AttributeType type, boolean includeBe) {
		fillAttributeValue(tv, type, null, includeBe, this.inverseColors);
	}

	protected void fillAttributeValue(TextView tv, AttributeType type, String prefix, boolean includeBe,
			boolean inverseColors) {
		if (getHero() == null || tv == null)
			return;

		Attribute attribute = getHero().getAttribute(type);

		fillAttributeValue(tv, attribute, prefix, includeBe, inverseColors);
	}

	protected void fillAttributeValue(TextView tv, Attribute attribute, String prefix, boolean includeBe,
			boolean inverseColors) {

		if (attribute != null) {

			int modifier = getHero().getModifier(attribute, includeBe, true);
			Util.setText(tv, attribute, modifier, prefix, inverseColors);
			tv.setTag(attribute);

			if (!tv.isLongClickable()) {

				if (attribute.getType().probable()) {
					tv.setOnClickListener(getBaseActivity().getProbeListener());
				} else if (attribute.getType().editable()) {
					tv.setOnClickListener(getBaseActivity().getEditListener());
				}

				if (attribute.getType().editable())
					tv.setOnLongClickListener(getBaseActivity().getEditListener());
			}
		} else {
			tv.setText(null);
		}
	}

	protected void fillAttribute(Attribute attr, boolean inverseColors) {
		switch (attr.getType()) {
		case Mut:
			fillAttributeValue(tfMU, AttributeType.Mut, null, true, inverseColors);
			break;
		case Klugheit:
			fillAttributeValue(tfKL, AttributeType.Klugheit, null, true, inverseColors);
			break;
		case Intuition:
			fillAttributeValue(tfIN, AttributeType.Intuition, null, true, inverseColors);
			break;
		case Charisma:
			fillAttributeValue(tfCH, AttributeType.Charisma, null, true, inverseColors);
			break;
		case Fingerfertigkeit:
			fillAttributeValue(tfFF, AttributeType.Fingerfertigkeit, null, true, inverseColors);
			break;
		case Gewandtheit:
			fillAttributeValue(tfGE, AttributeType.Gewandtheit, null, false, inverseColors);
			break;
		case Konstitution:
			fillAttributeValue(tfKO, AttributeType.Konstitution, null, true, inverseColors);
			break;
		case Körperkraft:
			fillAttributeValue(tfKK, AttributeType.Körperkraft, null, true, inverseColors);
			break;
		case Geschwindigkeit:
			fillAttributeValue(tfKK, AttributeType.Körperkraft, null, true, inverseColors);
		default:
			break;
		}
	}

}
