package com.dsatab.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
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
import com.dsatab.util.Debug;

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

	public static class FileNameComparator implements Comparator<File> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(File object1, File object2) {
			return object1.getName().compareToIgnoreCase(object2.getName());
		}
	};

	public static void hideKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) DSATabApplication.getInstance().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
	}

	private static final String PLAIN_ASCII = "AaEeIiOoUu" // grave
			+ "AaEeIiOoUuYy" // acute
			+ "AaEeIiOoUuYy" // circumflex
			+ "AaOoNn" // tilde
			+ "AaEeIiOoUuYy" // umlaut
			+ "Aa" // ring
			+ "Cc" // cedilla
			+ "OoUu" // double acute
	;

	private static final String UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
			+ "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
			+ "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
			+ "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
			+ "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" + "\u00C5\u00E5"
			+ "\u00C7\u00E7" + "\u0150\u0151\u0170\u0171";

	// remove accentued from a string and replace with ascii equivalent
	public static String convertNonAscii(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			int pos = UNICODE.indexOf(c);
			if (pos > -1) {
				sb.append(PLAIN_ASCII.charAt(pos));
			} else if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			}

		}
		return sb.toString();
	}

	public static Spanned getText(int resourceId, java.lang.Object... formatArgs) {
		return Html.fromHtml(String.format(
				Html.toHtml(new SpannedString(DSATabApplication.getInstance().getText(resourceId))), formatArgs));
	}

	public static Bitmap decodeBitmap(final File f, final int suggestedSize) {
		if (f == null) {
			return null;
		}
		if (f.exists() == false) {
			return null;
		}

		return decodeBitmap(Uri.fromFile(f), suggestedSize);
	}

	public static Bitmap decodeBitmap(Uri uri, int maxImageSize) {
		try {

			Bitmap b = null;
			InputStream is = null;
			BufferedInputStream bis = null;

			// Decode image size
			BitmapFactory.Options o;
			try {
				is = DSATabApplication.getInstance().getBaseContext().getContentResolver().openInputStream(uri);
				bis = new BufferedInputStream(is);

				o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(bis, null, o);
			} finally {
				if (is != null)
					is.close();
			}
			is = null;
			bis = null;

			// Find the correct scale value.It should be the power of 2
			final int requiredSize = maxImageSize;
			int widthTmp = o.outWidth, heightTmp = o.outHeight;
			int scale = 1;
			while (true) {
				if ((widthTmp / 2) < requiredSize || (heightTmp / 2) < requiredSize) {
					break;
				}
				widthTmp /= 2;
				heightTmp /= 2;
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			o2.inDither = false;
			o2.inInputShareable = true;
			o2.inPurgeable = true;
			o2.inTempStorage = new byte[32 * 1024];

			try {
				is = DSATabApplication.getInstance().getBaseContext().getContentResolver().openInputStream(uri);
				bis = new BufferedInputStream(is);
				b = BitmapFactory.decodeStream(bis, null, o2);
			} finally {
				if (is != null)
					is.close();
			}
			return b;

		} catch (final Throwable e) {
			Debug.error(e);
			System.gc();
			return null;
		}
	}

	public static String checkFileWriteAccess(File file) {
		String error = null;

		String MEDIA_MOUNTED = " Der häufigste Grund hierfür ist, dass die SD-Karte gerade vom PC verwendet wird. Trenne am besten das Kabel zwischen Smartphone und PC und versuche es erneut. ";

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_BAD_REMOVAL.equals(state)) {
			// Memory Card was removed before it was unmounted
			error = "SD-Karte wurde entfernt ohne das die unmounted wurde.";
		} else if (Environment.MEDIA_CHECKING.equals(state)) {
			// Memory Card is present and being disk-checked
			error = "SD-Karte ist vorhanden, wird aber gerade üerprüft.";
		} else if (Environment.MEDIA_MOUNTED.equals(state)) {
			// fine
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// Memory Card is present and mounted with readonly access
			error = "SD-Karte ist nur mit Leseberechtigungen gemounted." + MEDIA_MOUNTED;
		} else if (Environment.MEDIA_NOFS.equals(state)) {
			// Memory Card is present but is blank or using unsupported file
			// system
			error = "SD-Karte ist vorhanden, aber leer oder mit einem nicht unterstützten Dateisystem formatiert.";
		} else if (Environment.MEDIA_REMOVED.equals(state)) {
			// Memory Card is not present
			error = "Keine SD-Karte vrohanden.";
		} else if (Environment.MEDIA_SHARED.equals(state)) {
			// Memory Card is present but shared via USB mass storage
			error = "SD-Karte ist vorhanden, wird aber derzeit über den USB Massespeicher geteilt." + MEDIA_MOUNTED;
		} else if (Environment.MEDIA_UNMOUNTABLE.equals(state)) {
			// Memory Card is present but cannot be mounted
			error = "SD-Karte ist vorhanden, kann aber nicht gemounted werden." + MEDIA_MOUNTED;
		} else if (Environment.MEDIA_UNMOUNTED.equals(state)) {
			// Memory Card is present but not mounted
			error = "SD-Karte ist vorhadnen, derzeit aber nicht gemounted.";
		}

		if (error == null) {
			if (file.exists()) {
				if (!file.canWrite()) {
					error = "DsaTab erhielt keine Schreibrechte für folgende Datei:" + file.getAbsolutePath() + "."
							+ MEDIA_MOUNTED;
				}
			} else if (file.getParentFile().exists() && !file.getParentFile().canWrite()) {
				error = "DsaTab erhielt keine Schreibrechte für folgende Datei:" + file.getAbsolutePath() + ". "
						+ MEDIA_MOUNTED;
			}
		}

		return error;
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

	public static void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
			// if (view.getBackground() instanceof BitmapDrawable) {
			// BitmapDrawable bm = (BitmapDrawable) view.getBackground();
			// bm.getBitmap().recycle();
			// }
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			// AdapterViews, ListViews and potentially other ViewGroups
			// don’t support the removeAllViews operation
			if (view instanceof AdapterView) {
				// do nothing
			} else {
				try {
					((ViewGroup) view).removeAllViews();
				} catch (UnsupportedOperationException mayHappen) {
					// AdapterViews, ListViews and potentially other ViewGroups
					// don’t support the removeAllViews operation
				}
			}
		}

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

	public static Long parseLong(String s) {

		if (s == null)
			return null;

		s = s.trim();

		if (s.length() == 0 || MINUS.equals(s) || NULL.equals(s))
			return null;

		Long i;
		if (s.startsWith(PLUS))
			i = Long.valueOf(s.substring(1));
		else
			i = Long.valueOf(s);

		return i;
	}

	public static void applyRowStyle(Markable markable, View row, int position) {
		LevelListDrawable levelListDrawable;

		if (row.getBackground() instanceof LevelListDrawable) {
			levelListDrawable = (LevelListDrawable) row.getBackground();
		} else {
			row.setBackgroundResource(R.drawable.list_background_selector);
			levelListDrawable = (LevelListDrawable) row.getBackground();
		}

		int level = position % 2;
		if (markable.isFavorite())
			level += 2;
		else if (markable.isUnused())
			level += 4;

		levelListDrawable.setLevel(level);

	}

	public static void applyRowStyle(View row, int position) {
		LevelListDrawable levelListDrawable;

		if (row.getBackground() instanceof LevelListDrawable) {
			levelListDrawable = (LevelListDrawable) row.getBackground();
		} else {
			row.setBackgroundResource(R.drawable.list_background_selector);
			levelListDrawable = (LevelListDrawable) row.getBackground();
		}

		levelListDrawable.setLevel(position % 2);
	}

	public static void setVisibility(View view, boolean visible, View expander) {
		if (visible && view.getVisibility() != View.VISIBLE) {

			if (view.getVisibility() == View.GONE) {
				// weight of text5 is added to text1 if invisible
				((LinearLayout.LayoutParams) expander.getLayoutParams()).weight -= ((LinearLayout.LayoutParams) view
						.getLayoutParams()).weight;
			}

			view.setVisibility(View.VISIBLE);

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

	public static void setTextColor(TextView tf, Value value, int modifier) {
		setTextColor(tf, value, modifier, false);
	}

	public static void setTextColor(TextView tf, Value value, int modifier, boolean inverse) {
		if (value.getValue() != null) {
			if (modifier < 0 || (value.getReferenceValue() != null && value.getValue() < value.getReferenceValue()))
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed));
			else if (modifier > 0
					|| (value.getReferenceValue() != null && value.getValue() > value.getReferenceValue()))
				tf.setTextColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen));
			else {
				if (inverse)
					tf.setTextColor(tf.getContext().getResources().getColor(android.R.color.primary_text_dark));
				else
					tf.setTextColor(getThemeColors(tf.getContext(), android.R.attr.textColorPrimary));
			}
		} else {
			if (inverse)
				tf.setTextColor(DSATabApplication.getInstance().getResources()
						.getColor(android.R.color.primary_text_dark));
			else
				tf.setTextColor(getThemeColors(tf.getContext(), android.R.attr.textColorPrimary));
		}
	}

	public static int getThemeColors(Context context, int attr) {
		if (getThemeResourceId(context, attr) != 0)
			return DSATabApplication.getInstance().getResources().getColor(getThemeResourceId(context, attr));
		else
			return 0;
	}

	public static int getThemeResourceId(Context context, int attr) {
		TypedValue typedvalueattr = new TypedValue();
		if (context.getTheme().resolveAttribute(attr, typedvalueattr, true)) {
			return typedvalueattr.resourceId;
		} else {
			return 0;
		}
	}

	public static void setTextColor(TextView tf, int modifier, boolean inverse) {

		if (modifier == 0) {
			if (inverse) {
				tf.setTextColor(DSATabApplication.getInstance().getResources()
						.getColor(android.R.color.primary_text_dark));
			} else {
				tf.setTextColor(getThemeColors(tf.getContext(), android.R.attr.textColorPrimary));
			}
		} else if (modifier < 0)
			tf.setTextColor(tf.getResources().getColor(R.color.ValueRed));
		else if (modifier > 0)
			tf.setTextColor(tf.getResources().getColor(R.color.ValueGreen));

	}

	public static void setText(TextView tf, Value value) {
		setText(tf, value, null);
	}

	public static void setText(TextView tf, Value value, String prefix) {
		setText(tf, value, prefix, false);
	}

	public static void setText(TextView tf, Value value, String prefix, boolean inverseColors) {
		setText(tf, value != null ? value.getValue() : null, 0, prefix, inverseColors);
	}

	public static void setText(TextView tf, Value value, int modifier, String prefix, boolean inverse) {
		setText(tf, value != null ? value.getValue() : null, modifier, prefix, inverse);
	}

	public static void setText(TextView tf, Integer value, int modifier, String prefix) {
		setText(tf, value, modifier, prefix, false);
	}

	public static void setText(TextView tf, Integer value, int modifier, String prefix, boolean inverse) {
		if (value != null) {

			value += modifier;

			if (prefix != null) {
				tf.setText(prefix);
				tf.append(Util.toString(value));
			} else
				tf.setText(Util.toString(value));
		} else {
			tf.setText(null);
		}
		setTextColor(tf, modifier, inverse);
	}

	public static void appendValue(Hero hero, StyleableSpannableStringBuilder title, AttributeType type) {

		Integer value1 = hero.getAttributeValue(type);
		if (value1 != null) {
			int modifier = hero.getModifier(type);

			int color;
			if (modifier < 0)
				color = DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed);
			else if (modifier > 0)
				color = DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen);
			else {
				color = getThemeColors(DSATabApplication.getInstance(), android.R.attr.textColorPrimary);

			}
			title.append(" (");
			title.appendColor(color, Util.toString(value1 + modifier));
			title.append(")");
		}

	}

	public static void appendValue(Hero hero, StyleableSpannableStringBuilder title, Probe probe1, Probe probe2,
			boolean includeModifiers) {

		Integer value1 = null, value2 = null;

		if (probe1 != null)
			value1 = probe1.getValue();

		if (probe2 != null)
			value2 = probe2.getValue();

		if (value1 != null || value2 != null)
			title.append(" (");

		if (value1 != null) {
			int modifier = 0;
			int color = Color.TRANSPARENT;
			if (includeModifiers) {
				modifier = hero.getModifier(probe1);

				if (modifier < 0)
					color = DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed);
				else if (modifier > 0)
					color = DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen);
			}
			if (color != Color.TRANSPARENT)
				title.appendColor(color, Util.toString(value1 + modifier));
			else
				title.append(Util.toString(value1 + modifier));
		}

		if (value2 != null) {

			if (value1 != null)
				title.append("/");

			int modifier = 0;
			int color = Color.TRANSPARENT;
			if (includeModifiers) {
				modifier = hero.getModifier(probe2);

				if (modifier < 0)
					color = DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed);
				else if (modifier > 0)
					color = DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen);
			}

			if (color != Color.TRANSPARENT)
				title.appendColor(color, Util.toString(value2 + modifier));
			else
				title.append(Util.toString(value2 + modifier));
		}

		if (value1 != null || value2 != null)
			title.append(")");
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

			if (equippedItem.getItemSpecification() instanceof Weapon && equippedItem.getSecondaryItem() != null) {
				EquippedItem secondaryEquippedItem = equippedItem.getSecondaryItem();

				if (secondaryEquippedItem.getItemSpecification() instanceof Shield
						|| (secondaryEquippedItem.getItemSpecification() instanceof Weapon && secondaryEquippedItem
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
		if (!TextUtils.isEmpty(next))
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

	public static String getValue(String value, String defaultValue) {
		if (TextUtils.isEmpty(value))
			return defaultValue;
		else
			return value;
	}

	public static void inflateAcceptAbortMenu(Menu menu) {

		com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_accept, Menu.NONE,
				android.R.string.ok);

		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		item.setIcon(R.drawable.ic_action_done);

		item = menu.add(Menu.NONE, R.id.option_cancel, Menu.NONE, android.R.string.cancel);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		item.setIcon(R.drawable.ic_action_cancel);
	}

	public static InputStream openHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		HttpURLConnection httpConn = (HttpURLConnection) conn;
		httpConn.setAllowUserInteraction(false);
		httpConn.setInstanceFollowRedirects(true);
		httpConn.setRequestMethod("GET");
		httpConn.connect();

		response = httpConn.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK) {
			in = httpConn.getInputStream();
		}

		return in;
	}
}
