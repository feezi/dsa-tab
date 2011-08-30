package com.dsatab.common;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.Markable;
import com.dsatab.data.Probe;
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

	/**
	 * 
	 */
	private static final String DRAWABLE = "drawable";

	private static final String PLUS = "+";
	private static final String MINUS = "-";
	private static final String NULL = "null";

	private static final List<String> ROMANS = Arrays.asList("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
			"IX", "X");

	private static NumberFormat effectFormat = NumberFormat.getNumberInstance();
	static {
		effectFormat.setMaximumFractionDigits(1);
	}

	public static int getDrawableByName(String name) {
		return DSATabApplication.getInstance().getResources()
				.getIdentifier(name, DRAWABLE, DSATabApplication.getInstance().getPackageName());

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

	public static void applyRowStyle(Markable markable, View row, int position) {
		if (position % 2 == 1) {
			if (markable.isFavorite())
				row.setBackgroundResource(R.drawable.list_row_odd_fav);
			else if (markable.isUnused()) {
				row.setBackgroundResource(R.drawable.list_row_odd_unused);
			} else {
				row.setBackgroundResource(R.drawable.list_row_odd);
			}
		} else {
			if (markable.isFavorite())
				row.setBackgroundResource(R.drawable.list_row_even_fav);
			else if (markable.isUnused()) {
				row.setBackgroundResource(R.drawable.list_row_even_unused);
			} else {
				row.setBackgroundResource(R.drawable.list_row_even);
			}
		}
	}

	public static void setVisibility(View view, boolean visible, View expander) {
		if (visible && view.getVisibility() != View.VISIBLE) {

			view.setVisibility(View.VISIBLE);
			// weight of text5 is added to text1 if invisible
			((LinearLayout.LayoutParams) expander.getLayoutParams()).weight -= ((LinearLayout.LayoutParams) view
					.getLayoutParams()).weight;
		}

		if (!visible && view.getVisibility() == View.VISIBLE) {
			view.setVisibility(View.GONE);
			// weight of text5 is added to text1 if invisible
			((LinearLayout.LayoutParams) expander.getLayoutParams()).weight += ((LinearLayout.LayoutParams) view
					.getLayoutParams()).weight;
		}
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
				int beMinus = Util.parseInt(beModifier.substring(3));
				return value - Math.max(0, (be - beMinus));
			} catch (NumberFormatException e) {
				Debug.error(e);
				return value;
			}
		} else if (beModifier.startsWith("BEX")) {
			try {
				int beMulti = Util.parseInt(beModifier.substring(3));
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

	public static void setTextColor(TextView tf, Value value, int modifier) {
		if (value.getValue() != null && value.getReferenceValue() != null) {

			if (value.getValue() < value.getReferenceValue() || modifier < 0)
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed));
			else if (value.getValue() > value.getReferenceValue() || modifier > 0)
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen));
			else
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueBlack));
		} else {
			tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueBlack));
		}
	}

	public static void setTextColor(TextView tf, int modifier) {

		if (modifier == 0) {
			tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueBlack));
		} else if (modifier < 0)
			tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed));
		else if (modifier > 0)
			tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen));

	}

	public static void setText(TextView tf, Value value) {
		setText(tf, value, null);
	}

	public static void setText(TextView tf, Value value, String prefix) {
		setText(tf, value, 0, prefix);
	}

	public static void setText(TextView tf, Value value, int modifier, String prefix) {
		if (value.getValue() != null) {
			if (prefix != null)
				tf.setText(prefix + Util.toString(value.getValue() + modifier));
			else
				tf.setText(Util.toString(value.getValue() + modifier));
		} else {
			tf.setText("");
		}
		setTextColor(tf, value, modifier);
	}

	public static void setText(TextView tf, Integer value, int modifier, String prefix) {
		if (value != null) {

			value += modifier;

			if (prefix != null)
				tf.setText(prefix + Util.toString(value));
			else
				tf.setText(Util.toString(value));
		} else {
			tf.setText("");
		}
		setTextColor(tf, modifier);
	}

	public static void appendValue(Hero hero, StyleableSpannableStringBuilder title, AttributeType type) {

		Integer value1 = hero.getAttributeValue(type);
		if (value1 != null) {
			int modifier = hero.getModificator(type);

			int color;
			if (modifier < 0)
				color = R.color.ValueRed;
			else if (modifier > 0)
				color = R.color.ValueGreen;
			else
				color = R.color.ValueBlack;

			title.append(" (");
			title.appendColor(DSATabApplication.getInstance().getResources().getColor(color),
					Util.toString(value1 + modifier));
			title.append(")");
		}

	}

	public static void appendValue(Hero hero, StyleableSpannableStringBuilder title, Probe probe1, Probe probe2) {

		Integer value1 = null, value2 = null;

		if (probe1 != null)
			value1 = probe1.getValue();

		if (probe2 != null)
			value2 = probe2.getValue();

		if (value1 != null || value2 != null)
			title.append(" (");

		if (value1 != null) {
			int modifier = hero.getModificator(probe1);

			int color;
			if (modifier < 0)
				color = R.color.ValueRed;
			else if (modifier > 0)
				color = R.color.ValueGreen;
			else
				color = R.color.ValueBlack;

			title.appendColor(DSATabApplication.getInstance().getResources().getColor(color),
					Util.toString(value1 + modifier));
		}

		if (value2 != null) {

			if (value1 != null)
				title.append("/");

			int modifier = hero.getModificator(probe2);

			int color;
			if (modifier < 0)
				color = R.color.ValueRed;
			else if (modifier > 0)
				color = R.color.ValueGreen;
			else
				color = R.color.ValueBlack;

			title.appendColor(DSATabApplication.getInstance().getResources().getColor(color),
					Util.toString(value2 + modifier));
		}

		if (value1 != null || value2 != null)
			title.append(")");
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
		if (probe == null)
			return null;

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
		if (distance == null)
			return null;
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

	public static String toProbe(Integer value) {
		if (value != null)
			return (value >= 0 ? "+" : "") + Integer.toString(value);
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

			if (object1.hasSpecification(Weapon.class)) {
				Weapon weapon = object1.getSpecification(Weapon.class);
				type1 = weapon.getCombatTalentType();
			} else if (object1.hasSpecification(DistanceWeapon.class)) {
				DistanceWeapon weapon = object1.getSpecification(DistanceWeapon.class);
				type1 = weapon.getCombatTalentType();
			} else if (object1.hasSpecification(Armor.class)) {
				atype1 = object1.getCategory();
			}

			if (object2.hasSpecification(Weapon.class)) {
				Weapon weapon = object1.getSpecification(Weapon.class);
				type2 = weapon.getCombatTalentType();
			} else if (object2.hasSpecification(DistanceWeapon.class)) {
				DistanceWeapon weapon = object1.getSpecification(DistanceWeapon.class);
				type2 = weapon.getCombatTalentType();
			} else if (object2.hasSpecification(Armor.class)) {
				atype2 = object2.getCategory();
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

			if (equippedItem.getItem().hasSpecification(Weapon.class) && equippedItem.getSecondaryItem() != null) {
				EquippedItem secondaryEquippedItem = equippedItem.getSecondaryItem();

				if (secondaryEquippedItem.getItem().hasSpecification(Shield.class)
						|| (secondaryEquippedItem.getItem().hasSpecification(Weapon.class) && secondaryEquippedItem
								.getHand() == Hand.links)) {
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

	/**
	 * @param next
	 * @return
	 */
	public static int gradeToInt(String next) {
		if (next != null)
			return ROMANS.indexOf(next.toUpperCase());
		else
			return -1;
	}

	public static String intToGrade(int grade) {
		if (grade >= 0 && grade < ROMANS.size()) {
			return ROMANS.get(grade);
		} else {
			return Util.toString(grade);
		}
	}

	public static boolean equalsOrNull(Object o1, Object o2) {
		return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));
	}
}
