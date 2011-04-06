package com.dsatab.common;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import android.view.Gravity;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Hand;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.gandulf.guilib.util.Debug;

public class Util {

	private static final String PLUS = "+";
	private static final String MINUS = "-";
	private static final String NULL = "null";

	private static NumberFormat effectFormat = NumberFormat.getNumberInstance();
	static {
		effectFormat.setMaximumFractionDigits(1);
	}

	public static int getDrawableByName(String name) {
		try {
			return com.dsatab.R.drawable.class.getDeclaredField(name).getInt(null);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static boolean isBlank(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	public static float[] parseFloats(String s) {
		StringTokenizer st = new StringTokenizer(s, " ");
		int count = st.countTokens();
		float[] floats = new float[count];

		for (int i = 0; i < count; i++) {
			floats[i] = Float.parseFloat(st.nextToken());
		}
		return floats;
	}

	public static String toString(float[] floats) {
		StringBuilder sb = new StringBuilder();
		for (float f : floats) {
			sb.append(Float.toString(f));
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	public static Integer parseInt(String s) {

		if (s == null)
			return null;

		s = s.trim();

		if (s.length() == 0 || MINUS.equals(s) || NULL.equals(s))
			return null;

		Integer i;
		if (s.startsWith(PLUS))
			i = Integer.valueOf(s.substring(1));
		else
			i = Integer.valueOf(s);

		return i;
	}

	public static Double parseDouble(String s) {
		if (s == null)
			return null;

		s = s.trim();

		if (s.length() == 0 || MINUS.equals(s) || NULL.equals(s))
			return null;

		Double i;

		if (s.startsWith(PLUS)) {
			s = s.substring(1);
		}

		try {
			i = effectFormat.parse(s).doubleValue();
		} catch (ParseException e) {
			i = Double.valueOf(s);
		}

		return i;
	}

	public static Float parseFloat(String s) {
		if (s == null)
			return null;

		s = s.trim();

		if (s.length() == 0 || MINUS.equals(s) || NULL.equals(s))
			return null;

		Float i;

		if (s.startsWith(PLUS)) {
			s = s.substring(1);
		}

		try {
			i = effectFormat.parse(s).floatValue();
		} catch (ParseException e) {
			i = Float.valueOf(s);
		}

		return i;
	}

	public static int modifyBe(int value, String beModifier, int be) {
		if (beModifier == null) {
			return value;
		}

		beModifier = beModifier.toUpperCase();

		if ("BE".equalsIgnoreCase(beModifier)) {
			return value - be;
		} else if (beModifier.startsWith("BE-")) {
			try {
				int beMinus = Integer.parseInt(beModifier.substring(3));
				return value - Math.max(0, (be - beMinus));
			} catch (NumberFormatException e) {
				Debug.error(e);
				return value;
			}
		} else if (beModifier.startsWith("BEX")) {
			try {
				int beMulti = Integer.parseInt(beModifier.substring(3));
				return value - (be * beMulti);
			} catch (NumberFormatException e) {
				Debug.error(e);
				return value;
			}
		} else if ("0->BE".equalsIgnoreCase(beModifier)) {
			return value;
		} else {
			Debug.warning("Could not parse beModifier " + beModifier + " be was " + be);
			return value;
		}
	}

	public static void setTextColor(TextView tf, Value value) {
		if (value.getValue() != null && value.getReferenceValue() != null) {

			if (value.getValue() < value.getReferenceValue())
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed));
			else if (value.getValue() > value.getReferenceValue())
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen));
			else
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueBlack));
		} else {
			tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueBlack));
		}
	}

	public static void setText(TextView tf, Value value) {
		setText(tf, value, null);
	}

	public static void setText(TextView tf, Value value, String prefix) {
		if (value.getValue() != null) {
			if (prefix != null)
				tf.setText(prefix + Util.toString(value.getValue()));
			else
				tf.setText(Util.toString(value.getValue()));
		} else {
			tf.setText("");
		}
		setTextColor(tf, value);
	}

	public static void applyTextValueStyle(TextView tv) {
		tv.setTextAppearance(DSATabApplication.getInstance(), R.style.TextValue);
		tv.setMinimumWidth(DSATabApplication.getInstance().getResources()
				.getDimensionPixelSize(R.dimen.text_value_width));
		tv.setGravity(Gravity.CENTER);

	}

	/*
	 * public static double getProbePercentage(Probe probe, int modifier) {
	 * 
	 * double v = 0;
	 * 
	 * int t = 0; if (probe.getProbeBonus() != null) { t = probe.getProbeBonus()
	 * + modifier; } else t = modifier;
	 * 
	 * Debug.verbose("T=" + t);
	 * 
	 * // see Wege des Meisters page 170 if (t <= 0) { v = Math.min(20,
	 * probe.getProbeValue(0) + t); for (int i = 1; i < 3; i++) { v *=
	 * Math.min(20, probe.getProbeValue(i) + t); } } else {
	 * 
	 * v = Math.min(20, probe.getProbeValue(0)); for (int i = 1; i < 3; i++) { v
	 * *= Math.min(20, probe.getProbeValue(i)); }
	 * 
	 * // E i=1 - 3 for (int i = 0; i < 3; i++) {
	 * 
	 * int ti = Math.min(20 - probe.getProbeValue(i), t);
	 * 
	 * Debug.verbose("T" + i + "=" + ti); // E n=1 - Ti for (int n = 1; n <= ti;
	 * n++) { v += (Math.min(20, probe.getProbeValue(i % 3) - n) * Math.min(20,
	 * probe.getProbeValue((i + 1) % 3) - n)); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * return v / 8000; }
	 */

	public static AttributeType[] splitProbeString(String probe) {
		probe = probe.trim();
		if (probe.startsWith("("))
			probe = probe.substring(1);

		if (probe.endsWith(")"))
			probe = probe.substring(0, probe.length() - 1);

		String[] probes = probe.split("/");

		AttributeType[] types = new AttributeType[probes.length];

		for (int i = 0; i < probes.length; i++) {
			types[i] = AttributeType.byCode(probes[i]);
		}
		return types;
	}

	public static String[] splitDistanceString(String distance) {
		distance = distance.trim();
		if (distance.startsWith("("))
			distance = distance.substring(1);

		if (distance.endsWith(")"))
			distance = distance.substring(0, distance.length() - 1);

		String[] distances = distance.split("/");

		return distances;
	}

	public static String toString(Integer wmAt) {
		if (wmAt != null)
			return Integer.toString(wmAt);
		else
			return MINUS;

	}

	public static String toString(Double value) {
		if (value != null)
			return effectFormat.format(value);
		else
			return MINUS;
	}

	public static String toString(Float value) {
		if (value != null)
			return effectFormat.format(value);
		else
			return MINUS;
	}

	public static String toProbe(Integer wmAt) {
		if (wmAt != null)
			return (wmAt > 0 ? "+" : "") + Integer.toString(wmAt);
		else
			return "";

	}

	static class EquippedItemComparator implements Comparator<EquippedItem> {

		private static final List<String> SORT_ORDER = Arrays.asList("fk", "nk", "sc", "ja", "ru");

		@Override
		public int compare(EquippedItem object1, EquippedItem object2) {

			String name1 = object1.getName();
			String name2 = object2.getName();

			int index1 = SORT_ORDER.indexOf(name1.substring(0, 2));
			int index2 = SORT_ORDER.indexOf(name2.substring(0, 2));

			int compare = Integer.valueOf(index1).compareTo(index2) * 10000
					+ object1.getName().compareTo(object2.getName());
			return compare;
		}

	}

	static class ItemComparator implements Comparator<Item> {

		@Override
		public int compare(Item object1, Item object2) {

			CombatTalentType type1 = null, type2 = null;
			String atype1 = null, atype2 = null;

			if (object1 instanceof Weapon) {
				type1 = ((Weapon) object1).getCombatTalentType();
			} else if (object1 instanceof DistanceWeapon) {
				type1 = ((DistanceWeapon) object1).getCombatTalentType();
			} else if (object1 instanceof Armor) {
				atype1 = ((Armor) object1).getCategory();
			}

			if (object2 instanceof Weapon) {
				type2 = ((Weapon) object2).getCombatTalentType();
			} else if (object2 instanceof DistanceWeapon) {
				type2 = ((DistanceWeapon) object2).getCombatTalentType();
			} else if (object2 instanceof Armor) {
				atype2 = ((Armor) object2).getCategory();
			}

			int compareType = 0;
			if (type1 != null && type2 != null)
				compareType = type1.compareTo(type2);

			if (atype1 != null && atype2 != null)
				compareType = atype1.compareTo(atype2);

			int compareName = object1.getName().compareTo(object2.getName());
			return compareType * 10000 + compareName;
		}
	}

	public static void sort(List<EquippedItem> equippedItems) {

		Collections.sort(equippedItems, new EquippedItemComparator());

		for (int i = 0; i < equippedItems.size(); i++) {

			EquippedItem equippedItem = equippedItems.get(i);

			if (equippedItem.getItem() instanceof Weapon && equippedItem.getSecondaryItem() != null) {
				EquippedItem secondaryEquippedItem = equippedItem.getSecondaryItem();

				if (secondaryEquippedItem.getItem() instanceof Shield
						|| (secondaryEquippedItem.getItem() instanceof Weapon && secondaryEquippedItem.getHand() == Hand.links)) {
					equippedItems.remove(secondaryEquippedItem);

					int index = equippedItems.indexOf(equippedItem);
					equippedItems.add(index + 1, secondaryEquippedItem);
				}
			}
		}
	}

	public static void sortItems(List<Item> items) {
		Collections.sort(items, new ItemComparator());
	}
}
