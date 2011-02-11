package com.dsatab.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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

import android.text.TextUtils;
import android.text.TextUtils.StringSplitter;

import com.dsatab.activity.DSATabApplication;
import com.dsatab.common.Debug;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.Armor.ArmorType;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;

public class XmlParserNew {

	public static Map<String, Item> readItems() {

		Map<String, Item> items = new HashMap<String, Item>();

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets()
					.open("items.txt"), "Cp1252"), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');

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
					Armor w = readArmor(line, splitter);
					items.put(w.getName(), w);
				} else if (line.startsWith("S;")) {
					Shield w = readShield(line, splitter);
					items.put(w.getName(), w);
				} else {
					Item w = readItem(line, splitter);
					items.put(w.getName(), w);
				}

			}

			r.close();
		} catch (IOException e) {
			Debug.error(e);
		}

		return items;

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
			Debug.warning(line);
			e.printStackTrace();
		} catch (Throwable e) {
			Debug.warning(line);
			e.printStackTrace();
		}
		return null;
	}

	private static Armor readArmor(String line, StringSplitter splitter) {
		splitter.setString(line);

		Armor w = new Armor();

		Iterator<String> i = splitter.iterator();

		parseBase(w, i);

		w.setBe(Util.parseDouble(i.next()));

		for (Position pos : Position.values()) {
			if (!i.hasNext())
				break;
			w.setRs(pos, Util.parseInt(i.next()));
		}

		w.setArmorType(ArmorType.valueOf(i.next()));

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
			Debug.warning(line);
			e.printStackTrace();
		} catch (Throwable e) {
			Debug.warning(line);
			e.printStackTrace();
		}
		return null;
	}

	private static void appendItem(BufferedWriter r, Item i, String category) throws IOException {
		r.append(i.getName());
		r.append(";");
		if (i.path != null)
			r.append(i.path);
		r.append(";");
		r.append(i.getCategory() == null ? category : i.getCategory());
		r.append(";");
	}

	public static void writeItems() {
		Map<String, Item> items = readItems();

		try {

			File itemsFile = new File(DSATabApplication.getDsaTabPath(), "items.txt");
			OutputStreamWriter itemsWriter = new OutputStreamWriter(new FileOutputStream(itemsFile), "Cp1252");
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
					guessCategory = null;
					if (i.getCategory() == null) {
						if (w.getArmorType() == ArmorType.Torso) {
							guessCategory = "Oben";
						} else if (w.getArmorType() == ArmorType.Beine) {
							guessCategory = "Unten";
						} else if (w.getArmorType() == ArmorType.Helm) {
							guessCategory = "Helme";
						} else if (w.getArmorType() == ArmorType.Komplettrüstung) {
							guessCategory = "Torso";
						} else {
							guessCategory = "Sonstige Rüstungsteile";
						}
					}

					r.append("A;");
					appendItem(r, i, guessCategory);

					r.append(Util.toString(w.getBe()));
					r.append(";");

					for (Position pos : Position.values()) {
						r.append(Util.toString(w.getRs(pos)));
						r.append(";");
					}

					r.append(w.getArmorType().name());
					r.append(";");

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
			e.printStackTrace();
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

		item.setName(i.next().replace('_', ' '));

		item.setPath(i.next()); // path

		item.setCategory(i.next()); // category

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

	public static Hero readHero(String path, InputStream in) {

		Hero hero = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Debug.verbose("DomFactory created:" + factory.getClass().getName());
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Debug.verbose("DocumentBuilder created:" + builder.getClass().getName());
			Document dom = builder.parse(in);
			if (dom != null)
				Debug.verbose("Document sucessfully parsed");
			else {
				Debug.error("Error: DOM was null.");
			}
			hero = new Hero(path, dom);
			Debug.verbose("Hero object created: " + hero.toString());
		} catch (Exception e) {
			Debug.error(e);
		}

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
