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
import java.util.StringTokenizer;

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

public class XmlParser {

	public static Map<String, Weapon> readWeapons() {

		Map<String, Weapon> weapons = new HashMap<String, Weapon>();

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets()
					.open("waffen.txt"), "Cp1252"), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
			CombatTalentType currentType = null;
			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				if (line.startsWith(":")) {
					currentType = CombatTalentType.valueOf(line.substring(1));
					continue;
				}

				splitter.setString(line);

				try {

					Weapon w = new Weapon();

					Iterator<String> i = splitter.iterator();

					w.setName(i.next().replace('_', ' '));

					if (weapons.containsKey(w.getName())) {
						Weapon origWeapon = weapons.get(w.getName());
						origWeapon.getCombatTalentTypes().add(currentType);
						continue;
					}

					w.setTp(i.next());

					String tpKK = i.next();
					String tpKKMin = tpKK.substring(0, tpKK.indexOf("/"));
					String tpKKStep = tpKK.substring(tpKK.indexOf("/") + 1);
					w.setTpKKMin(Integer.valueOf(tpKKMin));
					w.setTpKKStep(Integer.valueOf(tpKKStep));

					i.next(); // gewicht
					i.next(); // länge

					w.setBf(Util.parseInt(i.next()));
					w.setIni(Util.parseInt(i.next()));

					i.next(); // preis

					String wm = i.next();
					String wmAt = wm.substring(0, wm.indexOf("/"));
					String wmPa = wm.substring(wm.indexOf("/") + 1);

					w.setWmAt(Util.parseInt(wmAt));
					w.setWmPa(Util.parseInt(wmPa));

					String s1 = i.next();
					String s2 = i.next();

					if (i.hasNext()) {
						i.next();
					} else {
						s2 = s1;
						s1 = null;
					}

					if (s1 != null && s1.contains("z"))
						w.setTwoHanded(true);

					w.setDistance(s2.trim());
					w.getCombatTalentTypes().add(currentType);

					weapons.put(w.getName(), w);

				} catch (NumberFormatException e) {
					Debug.warning(line);
					e.printStackTrace();
				} catch (Throwable e) {
					Debug.warning(line);
					e.printStackTrace();
				}

			}

			r.close();
		} catch (IOException e) {
			Debug.error(e);
		}

		return weapons;

	}

	public static Map<String, DistanceWeapon> readDistanceWeapons() {

		Map<String, DistanceWeapon> weapons = new HashMap<String, DistanceWeapon>();
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets()
					.open("fernwaffen.txt"), "Cp1252"), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
			CombatTalentType currentType = null;
			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				if (line.startsWith(":")) {
					currentType = CombatTalentType.valueOf(line.substring(1));
					continue;
				}

				splitter.setString(line);

				try {
					DistanceWeapon w = new DistanceWeapon();
					w.setCombatTalentType(currentType);
					Iterator<String> i = splitter.iterator();

					w.setName(i.next().replace('_', ' '));
					w.setTp(i.next());
					w.setDistances(i.next());
					w.setTpDistances(i.next());

					weapons.put(w.getName(), w);
				} catch (NumberFormatException e) {
					Debug.warning(line);
					e.printStackTrace();
				} catch (Throwable e) {
					Debug.warning(line);
					e.printStackTrace();
				}

			}

			r.close();
		} catch (IOException e) {
			Debug.error(e);
		}

		return weapons;

	}

	private static void appendItem(BufferedWriter r, Item i, String category) throws IOException {
		r.append(i.getName());
		r.append(";");
		if (i.path != null)
			r.append(i.path);
		r.append(";");
		r.append(i.getCategory() == null ? category : i.getCategory());
		r.append(";");
		if (i.getType() != null)
			r.append(i.getType().name());
		r.append(";");
	}

	public static void writeItems() {
		Map<String, Item> items = readItems();

		try {

			File itemsFile = new File(DSATabApplication.getDsaTabPath(), "items.txt");
			OutputStreamWriter itemsWriter = new OutputStreamWriter(new FileOutputStream(itemsFile), "Cp1252");
			BufferedWriter itemsW = new BufferedWriter(itemsWriter, 1024 * 8);

			File f = new File(DSATabApplication.getDsaTabPath(), "itemsW.txt");
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "Cp1252");
			BufferedWriter weaponW = new BufferedWriter(writer, 1024 * 8);

			List<Item> its = new ArrayList<Item>(items.values());
			Collections.sort(its);

			BufferedWriter r = null;
			String guessCategory = null;

			for (Item i : its) {

				if (i instanceof Weapon) {

					r = weaponW;
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
					r.append(w.getTpKKMin() + "/" + w.getTpKKStep());
					r.append(";");
					r.append(w.getWmAt() + "/" + w.getWmPa());
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
					r = weaponW;

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

					r = weaponW;

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

					r = weaponW;

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
			weaponW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Item> readItems() {
		HashMap<String, Item> items = new HashMap<String, Item>();

		items.putAll(readWeapons());
		items.putAll(readDistanceWeapons());
		items.putAll(readShields());
		items.putAll(readArmors());

		fillCards(items);

		return items;
	}

	public static Map<String, Shield> readShields() {

		Map<String, Shield> weapons = new HashMap<String, Shield>();
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets()
					.open("schild.txt"), "Cp1252"), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
			CombatTalentType currentType = null;
			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				if (line.startsWith(":")) {
					currentType = CombatTalentType.valueOf(line.substring(1));
					continue;
				}
				splitter.setString(line);

				try {
					Shield w = new Shield();

					Iterator<String> i = splitter.iterator();

					w.setName(i.next().replace('_', ' '));

					String type = i.next().toLowerCase(); // typ

					if (type.contains("p"))
						w.setParadeWeapon(true);
					if (type.contains("s"))
						w.setShield(true);

					i.next(); // gewicht

					String wm = i.next();
					String wmAt = wm.substring(0, wm.indexOf("/"));
					String wmPa = wm.substring(wm.indexOf("/") + 1);

					w.setWmAt(Util.parseInt(wmAt));
					w.setWmPa(Util.parseInt(wmPa));

					w.setIni(Util.parseInt(i.next()));
					w.setBf(Util.parseInt(i.next()));

					w.getCombatTalentTypes().add(currentType);

					weapons.put(w.getName(), w);
				} catch (NumberFormatException e) {
					Debug.warning(line);
					e.printStackTrace();
				} catch (Throwable e) {
					Debug.warning(line);
					e.printStackTrace();
				}

			}

			r.close();
		} catch (IOException e) {
			Debug.error(e);
		}

		return weapons;

	}

	public static void fillCards(Map<String, Item> items) {

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets()
					.open("cards.txt"), "Cp1252"), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');

			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				splitter.setString(line);

				try {

					Iterator<String> i = splitter.iterator();

					String name = i.next();
					String typeString = i.next();
					String path = i.next();
					String category = i.next();

					ItemType type = ItemType.fromColor(typeString);

					Item card = items.get(name);

					if (card == null) {
						card = new Item();
						items.put(name, card);
					}
					card.setName(name);
					card.setType(type);
					card.setPath(path);
					card.setCategory(category);

				} catch (NumberFormatException e) {
					Debug.warning(line);
					e.printStackTrace();
				} catch (Throwable e) {
					Debug.warning(line);
					e.printStackTrace();
				}

			}

			r.close();
		} catch (IOException e) {
			Debug.error(e);
		}

	}

	public static Map<String, Armor> readArmors() {

		Map<String, Armor> weapons = new HashMap<String, Armor>();
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets()
					.open("ruestung.txt"), "Cp1252"), 1024 * 8);

			String line;

			ArmorType currentType = null;
			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				if (line.startsWith(":")) {
					currentType = ArmorType.valueOf(line.substring(1));
					continue;
				}
				StringTokenizer i = new StringTokenizer(line, " \t");

				try {
					Armor w = new Armor();
					w.setArmorType(currentType);

					w.setName(i.nextToken().replace('_', ' '));

					w.setBe(Util.parseDouble(i.nextToken()));

					for (Position pos : Position.values()) {
						if (!i.hasMoreTokens())
							break;
						w.setRs(pos, Util.parseInt(i.nextToken()));
					}

					weapons.put(w.getName(), w);
				} catch (NumberFormatException e) {
					Debug.warning(line);
					e.printStackTrace();
				} catch (Throwable e) {
					Debug.warning(line);
					e.printStackTrace();
				}

			}

			r.close();
		} catch (IOException e) {
			Debug.error(e);
		}

		return weapons;

	}

	public static Hero readHero(String path, InputStream in) {

		Hero hero = null;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(in);

			hero = new Hero(path, dom);
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
