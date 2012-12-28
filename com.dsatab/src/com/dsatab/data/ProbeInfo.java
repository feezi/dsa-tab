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
package com.dsatab.data;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.dsatab.data.enums.AttributeType;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;

/**
 * @author Ganymede
 * 
 */
public class ProbeInfo implements Cloneable {

	// e.g.: MU/IN/KL (+5) or (MU/IN/KL) or +5
	private static final Pattern PROBE_PATTERN = Pattern.compile(
			"(\\(?([a-z-]{2})/([a-z-]{2})/([a-z-]{2})\\)?)?\\s*\\(?([+-]\\d+)?\\)?", Pattern.CASE_INSENSITIVE);

	private AttributeType[] attributeTypes;
	private String attributesString;

	/**
	 * Returns the probe modification positive values means the probe is more
	 * difficult, negative values simplifies the probe
	 * 
	 * @return
	 */
	private Integer erschwernis;

	private static final int BE_FLAG_MULTIPLY = 64;
	private static final int BE_FLAG_ADDITION = 128;
	private static final int BE_FLAG_SUBTRACTION = 256;
	private static final int BE_FLAG_NONE = -1;

	private int beFlag;

	private String bePattern;

	public ProbeInfo() {
		beFlag = BE_FLAG_NONE;
	}

	/**
	 * 
	 */
	public ProbeInfo(AttributeType[] attrs) {
		this.attributeTypes = attrs;
	}

	/**
	 * 
	 */
	public ProbeInfo(AttributeType[] attrs, Integer erschwernis) {
		this.attributeTypes = attrs;
		this.erschwernis = erschwernis;
	}

	public AttributeType[] getAttributeTypes() {
		return attributeTypes;
	}

	public void applyProbePattern(String s) {

		if (s != null) {
			Matcher matcher = PROBE_PATTERN.matcher(s.trim());

			if (matcher.matches()) {
				if (!TextUtils.isEmpty(matcher.group(1))) {
					attributeTypes = new AttributeType[3];
					attributeTypes[0] = AttributeType.byCode(matcher.group(2));
					attributeTypes[1] = AttributeType.byCode(matcher.group(3));
					attributeTypes[2] = AttributeType.byCode(matcher.group(4));
				}
				erschwernis = Util.parseInteger(matcher.group(5));
			} else {
				Debug.warning("No probe match found for " + s);
			}
		} else {
			attributesString = null;
			attributeTypes = null;
		}
	}

	public void applyBePattern(Integer beModifier) {
		if (beModifier == null) {
			beFlag = BE_FLAG_NONE;
			bePattern = null;
		} else if (beModifier > 0) {
			beFlag = BE_FLAG_ADDITION + beModifier;
			bePattern = "BE+" + beModifier;
		} else if (beModifier < 0) {
			beFlag = BE_FLAG_SUBTRACTION + Math.abs(beModifier);
			bePattern = "BE" + beModifier;
		} else {
			beFlag = BE_FLAG_ADDITION + 0;
			bePattern = "BE";
		}
	}

	public void applyBePattern(String beModifier) {
		bePattern = beModifier;

		if (beModifier == null) {
			beFlag = BE_FLAG_NONE;
		} else {
			beModifier = beModifier.toUpperCase(Locale.GERMAN);
			if ("BE".equalsIgnoreCase(beModifier)) {
				beFlag = BE_FLAG_ADDITION;
			} else if (beModifier.startsWith("BE-")) {
				try {
					int beMinus = Util.parseInteger(beModifier.substring(3));
					beFlag = BE_FLAG_SUBTRACTION + Math.abs(beMinus);
				} catch (NumberFormatException e) {
					Debug.error(e);
				}
			} else if (beModifier.startsWith("BEX")) {
				try {
					int beMulti = Util.parseInteger(beModifier.substring(3));
					beFlag = BE_FLAG_MULTIPLY + beMulti;
				} catch (NumberFormatException e) {
					Debug.error(e);
				}
			} else if ("0->BE".equalsIgnoreCase(beModifier)) {
				beFlag = BE_FLAG_NONE;
			} else {
				Debug.warning("Could not parse beModifier " + beModifier);
			}
		}
	}

	public String getBe() {
		return bePattern;
	}

	public boolean hasBe() {
		return beFlag != BE_FLAG_NONE;
	}

	public int getBe(int value, int be) {

		if (beFlag == BE_FLAG_NONE) {
			return value;
		} else if (beFlag >= BE_FLAG_SUBTRACTION) {
			return value - (Math.max(0, be - (beFlag - BE_FLAG_SUBTRACTION)));
		} else if (beFlag >= BE_FLAG_ADDITION) {
			return value - (be + (beFlag - BE_FLAG_ADDITION));
		} else if (beFlag >= BE_FLAG_MULTIPLY) {
			return value - (be * (beFlag - BE_FLAG_MULTIPLY));
		} else
			return 0;
	}

	/**
	 * Returns the probe modification positive values means the probe is more
	 * difficult, negative values simplifies the probe
	 * 
	 * @return
	 */
	public Integer getErschwernis() {
		return erschwernis;
	}

	public String getAttributesString() {
		if (attributeTypes == null)
			return null;
		else {
			if (attributesString == null) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				if (attributeTypes[0] == null)
					sb.append("--");
				else
					sb.append(attributeTypes[0].code());

				sb.append("/");
				if (attributeTypes[1] == null)
					sb.append("--");
				else
					sb.append(attributeTypes[1].code());

				sb.append("/");
				if (attributeTypes[2] == null)
					sb.append("--");
				else
					sb.append(attributeTypes[2].code());

				sb.append(")");

				attributesString = sb.toString();
			}
			return attributesString;
		}
	}

	public void setAttributeTypes(AttributeType[] attributeTypes) {
		this.attributeTypes = attributeTypes;
		this.attributesString = null;
	}

	public void setErschwernis(Integer erschwernis) {
		this.erschwernis = erschwernis;
	}

	public String toString() {

		if (attributeTypes != null && erschwernis != null)
			return getAttributesString() + " " + Util.toProbe(erschwernis);

		if (attributeTypes != null && erschwernis == null)
			return getAttributesString();

		if (attributeTypes == null && erschwernis != null)
			return Util.toProbe(erschwernis);

		return null;

	}

	public static ProbeInfo parse(String s) {
		ProbeInfo info = new ProbeInfo();
		if (s != null) {
			info.applyProbePattern(s);
		}
		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ProbeInfo clone() {
		try {
			return (ProbeInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			Debug.error(e);
			return null;
		}
	}
}
