package com.dsatab.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.text.TextUtils;
import android.text.TextUtils.StringSplitter;

import com.dsatab.activity.DSATabApplication;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.ErrorHandler;

public class XmlParserNew {

	public static Map<String, Item> readItems() {

		Map<String, Item> items = new HashMap<String, Item>();

		try {
			readItems("items.txt", items);

			if (DSATabApplication.getInstance().getConfiguration().isHouseRules()) {
				readItems("items_armor_house.txt", items);
			} else {
				readItems("items_armor.txt", items);
			}
		} catch (IOException e) {
			Debug.error(e);
			ErrorHandler.handleError(e, DSATabApplication.getInstance().getBaseContext());
		}

		return items;

	}

	private static void readItems(String file, Map<String, Item> items) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets().open(file),
					"UTF-8"), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');

			List<Position> armorPositions = DSATabApplication.getInstance().getConfiguration().getArmorPositions();

			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				// Debug.verbose(line);
				if (line.startsWith("W;")) {
					Weapon w = readWeapon(line, splitter);
					items.put(w.getName(), w);
				} else if (line.startsWith("D;")) {
					DistanceWeapon w = readDistanceWeapon(line, splitter);
					items.put(w.getName(), w);
				} else if (line.startsWith("A;")) {
					Armor w = readArmor(line, splitter, armorPositions);
					if (w != null)
						items.put(w.getName(), w);
				} else if (line.startsWith("S;")) {
					Shield w = readShield(line, splitter);
					items.put(w.getName(), w);
				} else {
					Item w = readItem(line, splitter);
					items.put(w.getName(), w);
				}

			}
		} finally {
			try {
				if (r != null)
					r.close();
			} catch (IOException e) {
			}
		}
	}

	private static Weapon readWeapon(String line, StringSplitter splitter) {
		splitter.setString(line);

		try {

			Weapon w = new Weapon();

			Iterator<String> i = splitter.iterator();

			parseBase(w, i);

			w.setTp(i.next()); // TP

			String tpKK = i.next(); // TPKK
			String tpKKMin = tpKK.substring(0, tpKK.indexOf("/"));
			String tpKKStep = tpKK.substring(tpKK.indexOf("/") + 1);
			w.setTpKKMin(Integer.valueOf(tpKKMin));
			w.setTpKKStep(Integer.valueOf(tpKKStep));

			String wm = i.next(); // WM
			String wmAt = wm.substring(0, wm.indexOf("/"));
			String wmPa = wm.substring(wm.indexOf("/") + 1);

			w.setWmAt(Util.parseInt(wmAt));
			w.setWmPa(Util.parseInt(wmPa));

			w.setIni(Util.parseInt(i.next())); // INI

			w.setBf(Util.parseInt(i.next())); // BF

			w.setDistance(i.next().trim()); // distance

			String twohanded = i.next(); // twohanded
			if (twohanded != null && twohanded.contains("z"))
				w.setTwoHanded(true);

			while (i.hasNext()) { // type
				w.getCombatTalentTypes().add(CombatTalentType.valueOf(i.next()));
			}

			return w;

		} catch (NumberFormatException e) {
			Debug.error(line, e);
		}
		return null;
	}

	private static Armor readArmor(String line, StringSplitter splitter, List<Position> armorPositions) {
		splitter.setString(line);

		Armor w = new Armor();

		Iterator<String> i = splitter.iterator();

		parseBase(w, i);

		w.setBe(Util.parseFloat(i.next()));

		for (Position pos : armorPositions) {
			if (!i.hasNext())
				break;
			w.setRs(pos, Util.parseInt(i.next()));
		}

		if (i.hasNext()) {
			int zonenRs = Util.parseInt(i.next());
			w.setZonenRs(zonenRs);
		}
		if (i.hasNext()) {
			Integer totalRs = Util.parseInt(i.next());
			w.setTotalRs(totalRs);
		}
		if (i.hasNext()) {
			int stars = Util.parseInt(i.next());
			w.setStars(stars);
		}

		if (i.hasNext()) {
			String mod = i.next();
			if (mod.contains("Z"))
				w.setZonenHalfBe(true);
		}
		return w;
	}

	private static DistanceWeapon readDistanceWeapon(String line, StringSplitter splitter) {
		splitter.setString(line);

		try {
			DistanceWeapon w = new DistanceWeapon();

			Iterator<String> i = splitter.iterator();

			parseBase(w, i);

			w.setTp(i.next());
			w.setDistances(i.next());
			w.setTpDistances(i.next());

			while (i.hasNext()) { // type
				w.setCombatTalentType(CombatTalentType.valueOf(i.next()));
			}

			return w;
		} catch (NumberFormatException e) {
			Debug.error(line, e);
		}
		return null;
	}

	private static void appendItem(BufferedWriter r, Item i, String category) throws IOException {
		int lineLength = i.getName().length();
		r.append(i.getName());

		lineLength++;
		r.append(";");
		if (i.path != null) {
			r.append(i.path);
			lineLength += i.path.length();
		}
		r.append(";");
		lineLength++;
		if (i.getCategory() == null) {
			r.append(category);
			lineLength += category.length();
		} else {
			r.append(i.getCategory());
			lineLength += i.getCategory().length();
		}

		int totalpad = 70 - lineLength;

		while (totalpad > 0) {
			r.append(" ");
			totalpad--;
		}

		r.append(";");
		lineLength++;

	}

	public static void writeItems() {
		Map<String, Item> items = readItems();

		try {

			File itemsFile = new File(DSATabApplication.getDsaTabPath(), "items_new.txt");
			OutputStreamWriter itemsWriter = new OutputStreamWriter(new FileOutputStream(itemsFile), "UTF-8");
			BufferedWriter itemsW = new BufferedWriter(itemsWriter, 1024 * 8);

			List<Item> its = new ArrayList<Item>(items.values());
			Collections.sort(its);

			BufferedWriter r = itemsW;
			String guessCategory = null;

			for (Item i : its) {

				if (i instanceof Weapon) {

					Weapon w = (Weapon) i;

					guessCategory = null;
					if (i.getCategory() == null) {

						if (w.getCombatTalentType() == CombatTalentType.Zweihandhiebwaffen) {
							guessCategory = "Zweihandhiebwaffen und -flegel";
						} else if (w.getCombatTalentType() == CombatTalentType.Zweihandflegel) {
							guessCategory = "Zweihandhiebwaffen und -flegel";
						} else if (w.getCombatTalentType() == CombatTalentType.Zweihandschwerter) {
							guessCategory = "Zweihandschwerter und -säbel";
						} else if (w.getCombatTalentType() == CombatTalentType.Speere) {
							guessCategory = "Speere und Stäbe";
						} else if (w.getCombatTalentType() == CombatTalentType.Stäbe) {
							guessCategory = "Speere und Stäbe";
						} else if (w.getCombatTalentTypes().size() == 1) {
							guessCategory = w.getCombatTalentType().name();
						}

					}

					r.append("W;");
					appendItem(r, i, guessCategory);
					r.append(w.getTp());
					r.append(";");
					r.append(Util.toString(w.getTpKKMin()) + "/" + Util.toString(w.getTpKKStep()));
					r.append(";");
					r.append(Util.toString(w.getWmAt()) + "/" + Util.toString(w.getWmPa()));
					r.append(";");
					r.append(Util.toString(w.getIni()));
					r.append(";");
					r.append(Util.toString(w.getBf()));
					r.append(";");
					r.append(w.getDistance());
					r.append(";");
					r.append(w.isTwoHanded() ? "Z" : "");
					r.append(";");

					for (CombatTalentType t : w.getCombatTalentTypes()) {
						r.append(t.name());
						r.append(";");
					}
				} else if (i instanceof DistanceWeapon) {

					DistanceWeapon w = (DistanceWeapon) i;

					guessCategory = null;
					if (i.getCategory() == null) {

						if (w.getCombatTalentType() == CombatTalentType.Armbrust) {
							guessCategory = "Schusswaffen";
						} else if (w.getCombatTalentType() == CombatTalentType.Bogen) {
							guessCategory = "Schusswaffen";
						} else if (w.getCombatTalentType() == CombatTalentType.Blasrohr) {
							guessCategory = "Schusswaffen";
						} else if (w.getCombatTalentType() == CombatTalentType.Wurfbeile) {
							guessCategory = "Wurfwaffen";
						} else if (w.getCombatTalentType() == CombatTalentType.Wurfmesser) {
							guessCategory = "Wurfwaffen";
						} else if (w.getCombatTalentType() == CombatTalentType.Wurfspeere) {
							guessCategory = "Wurfwaffen";
						} else if (w.getCombatTalentType() == CombatTalentType.Schleuder) {
							guessCategory = "Wurfwaffen";
						}

					}

					r.append("D;");
					appendItem(r, i, guessCategory);
					r.append(w.getTp());
					r.append(";");
					r.append(w.getDistances());
					r.append(";");
					r.append(w.getTpDistances());
					r.append(";");
					r.append(w.getCombatTalentType().name());
					r.append(";");

				} else if (i instanceof Shield) {

					Shield w = (Shield) i;
					guessCategory = null;

					if (i.getCategory() == null) {
						if (w.isParadeWeapon()) {
							guessCategory = "Dolche";
						} else {
							guessCategory = "Schilde";
						}
					}

					r.append("S;");
					appendItem(r, i, guessCategory);
					r.append(w.getWmAt() + "/" + w.getWmPa());
					r.append(";");
					r.append(Util.toString(w.getIni()));
					r.append(";");
					r.append(Util.toString(w.getBf()));
					r.append(";");
					r.append(w.isShield() ? "S" : "");
					r.append(w.isParadeWeapon() ? "P" : "");
					r.append(";");

					for (CombatTalentType t : w.getCombatTalentTypes()) {
						r.append(t.name());
						r.append(";");
					}

				} else if (i instanceof Armor) {

					Armor w = (Armor) i;
					r.append("A;");
					appendItem(r, i, null);

					if (w.getBe() < 10)
						r.append(" ");
					r.append(Util.toString(w.getBe()));
					r.append(";");

					for (Position pos : DSATabApplication.getInstance().getConfiguration().getArmorPositions()) {
						int rs = w.getRs(pos);
						if (rs < 10)
							r.append(" ");
						r.append(Util.toString(rs));
						r.append(";");
					}

					r.append(Util.toString(w.getZonenRs()));
					r.append(";");
					r.append(Util.toString(w.getTotalRs()));
					r.append(";");
					r.append(Util.toString(w.getStars()));
					r.append(";");
					if (w.isZonenHalfBe()) {
						r.append("Z");
						r.append(";");
					}

				} else {
					r = itemsW;
					r.append(i.getType().character());
					r.append(";");
					appendItem(r, i, null);
				}

				r.append("\n");
			}

			itemsW.close();
		} catch (IOException e) {
			Debug.error(e);
		}
	}

	private static Item readItem(String line, StringSplitter splitter) {
		splitter.setString(line);

		Item w = new Item();

		Iterator<String> i = splitter.iterator();

		parseBase(w, i);

		return w;
	}

	private static void parseBase(Item item, Iterator<String> i) {

		String typeString = i.next();// itemtype
		if (!TextUtils.isEmpty(typeString))
			item.setType(ItemType.fromCharacter(typeString.charAt(0))); // type

		item.setName(i.next().replace('_', ' ').trim());

		item.setPath(i.next().trim()); // path

		item.setCategory(i.next().trim()); // category

	}

	private static Shield readShield(String line, StringSplitter splitter) {
		splitter.setString(line);
		Shield w = new Shield();

		Iterator<String> i = splitter.iterator();

		parseBase(w, i);

		String wm = i.next();
		String wmAt = wm.substring(0, wm.indexOf("/"));
		String wmPa = wm.substring(wm.indexOf("/") + 1);

		w.setWmAt(Util.parseInt(wmAt));
		w.setWmPa(Util.parseInt(wmPa));

		w.setIni(Util.parseInt(i.next()));
		w.setBf(Util.parseInt(i.next()));

		String type = i.next().toLowerCase(); // typ

		if (type.contains("p"))
			w.setParadeWeapon(true);
		if (type.contains("s"))
			w.setShield(true);

		while (i.hasNext()) { // type
			w.getCombatTalentTypes().add(CombatTalentType.valueOf(i.next()));
		}

		return w;

	}

	public static void normalize(File f) {

		try {
			FileReader reader = new FileReader(f);
			BufferedReader r = new BufferedReader(reader);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			OutputStreamWriter out = new OutputStreamWriter(outputStream, "UTF-8");
			BufferedWriter writer = new BufferedWriter(out);

			String line = null;
			boolean intag = false;

			while ((line = r.readLine()) != null) {

				char[] chars = line.toCharArray();

				for (int i = 0; i < chars.length; i++) {

					if (chars[i] == '<' && (i < chars.length - 2 && chars[i + 1] != '!'))
						intag = true;
					else if (chars[i] == '>')
						intag = false;
					else if (chars[i] == ' ')
						intag = false;
					else if (chars[i] == 'ü' && intag) {
						writer.write("ue");
						continue;
					}

					writer.write(chars[i]);
				}

				writer.write("\n");

			}
			writer.close();
			r.close();

			OutputStreamWriter fileOut = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
			fileOut.write(new String(outputStream.toByteArray()));
			fileOut.close();
		} catch (FileNotFoundException e) {
			Debug.error(e);
			ErrorHandler.handleError(e, DSATabApplication.getInstance().getBaseContext());
		} catch (IOException e) {
			Debug.error(e);
			ErrorHandler.handleError(e, DSATabApplication.getInstance().getBaseContext());
		}

	}

	public static Hero readHero(String path, InputStream in) throws Exception {

		Hero hero = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Debug.verbose("DomFactory created:" + factory.getClass().getName());

		DocumentBuilder builder = factory.newDocumentBuilder();
		Debug.verbose("DocumentBuilder created:" + builder.getClass().getName());
		InputStreamReader isr = new InputStreamReader(in, "UTF-8");
		InputSource is = new InputSource();
		is.setCharacterStream(isr);
		is.setEncoding("UTF-8");
		Document dom = builder.parse(is);
		if (dom != null)
			Debug.verbose("Document sucessfully parsed");
		else {
			Debug.error("Error: DOM was null.");
		}
		hero = new Hero(path, dom);
		Debug.verbose("Hero object created: " + hero.toString());

		return hero;
	}

	public static void writeHero(Hero hero, OutputStream out) {
		if (hero == null)
			return;

		LegacyXmlWriter.writeHero(hero, out);
		// if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1)
		// LegacyXmlWriter.writeHero(hero, out);
		// else
		// NativeXmlWriter.writeHero(hero, out);

	}

}
