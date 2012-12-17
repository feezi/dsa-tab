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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import android.text.TextUtils;
import android.text.TextUtils.StringSplitter;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.common.Util;
import com.dsatab.data.ArtInfo;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroLoader;
import com.dsatab.data.SpellInfo;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.ItemType;
import com.dsatab.data.items.MiscSpecification;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.util.Debug;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

public class XmlParser {

	public static final String ENCODING = "UTF-8";

	public static void fillItems() {

		try {
			readItems("items.txt");

			if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_MORE_WOUND_ZONES,
					false)) {
				readItems("items_armor_house.txt");
			} else {
				readItems("items_armor.txt");
			}
		} catch (IOException e) {
			throw new DsaTabRuntimeException("Could not parse items from items.txt", e);
		}

	}

	public static void fillArts() {

		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets().open("arts.txt"),
					ENCODING), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');

			RuntimeExceptionDao<ArtInfo, Integer> artDao = DSATabApplication.getInstance().getDBHelper()
					.getRuntimeDao(ArtInfo.class);

			Iterator<String> i = null;

			ArtInfo item = null;
			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				try {
					item = new ArtInfo();
					splitter.setString(line);
					i = splitter.iterator();

					item.setName(i.next().trim());
					if (i.hasNext())
						item.setGrade(Util.gradeToInt(i.next().trim()));
					if (i.hasNext())
						item.setTarget(i.next().trim());
					if (i.hasNext())
						item.setRange(i.next().trim());
					if (i.hasNext())
						item.setCastDuration(i.next().trim());
					if (i.hasNext())
						item.setEffect(i.next().trim());
					if (i.hasNext())
						item.setEffectDuration(i.next().trim());
					if (i.hasNext())
						item.setOrigin(i.next().trim());
					if (i.hasNext())
						item.setSource(i.next().trim());
					if (i.hasNext())
						item.setProbe(i.next().trim());
					if (i.hasNext())
						item.setMerkmale(i.next().trim());

					artDao.create(item);

				} catch (StringIndexOutOfBoundsException e) {
					Debug.warning("Could not parse:" + line);
				}

			}
		} catch (IOException e) {
			throw new DsaTabRuntimeException("Could not read arts from arts.txt", e);
		} finally {
			try {
				if (r != null)
					r.close();
			} catch (IOException e) {
			}
		}

	}

	public static void fillSpells() {

		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(
					DSATabApplication.getInstance().getAssets().open("zauber.txt"), ENCODING), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');

			Iterator<String> i = null;

			RuntimeExceptionDao<SpellInfo, Integer> spellDao = DSATabApplication.getInstance().getDBHelper()
					.getRuntimeDao(SpellInfo.class);

			SpellInfo item = null;
			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				try {
					item = new SpellInfo();
					splitter.setString(line);
					i = splitter.iterator();

					item.setName(i.next().trim());
					if (i.hasNext())
						item.setSource(i.next().trim());
					if (i.hasNext())
						item.setProbe(i.next().trim());
					if (i.hasNext())
						item.setComplexity(i.next().trim());
					if (i.hasNext())
						item.setRepresentation(i.next().trim());
					if (i.hasNext())
						item.setMerkmale(i.next().trim());
					if (i.hasNext())
						item.setCastDuration(i.next().trim());
					if (i.hasNext())
						item.setCosts(i.next().trim());
					if (i.hasNext())
						item.setTarget(i.next().trim());
					if (i.hasNext())
						item.setRange(i.next().trim());
					if (i.hasNext())
						item.setEffectDuration(i.next().trim());
					if (i.hasNext())
						item.setEffect(i.next().trim());

					spellDao.create(item);
				} catch (StringIndexOutOfBoundsException e) {
					Debug.warning("Could not parse:" + line);
				}

			}
		} catch (IOException e) {
			throw new DsaTabRuntimeException("Could nor read spells from zauber.txt", e);
		} finally {
			try {
				if (r != null)
					r.close();
			} catch (IOException e) {
			}
		}

	}

	private static void readItems(String file) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(DSATabApplication.getInstance().getAssets().open(file),
					ENCODING), 1024 * 8);

			String line;
			StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');

			List<Position> armorPositions = DSATabApplication.getInstance().getConfiguration().getArmorPositions();

			RuntimeExceptionDao<Weapon, Integer> weaponDao = DSATabApplication.getInstance().getDBHelper()
					.getRuntimeDao(Weapon.class);

			RuntimeExceptionDao<Shield, Integer> shieldDao = DSATabApplication.getInstance().getDBHelper()
					.getRuntimeDao(Shield.class);

			RuntimeExceptionDao<Armor, Integer> armorDao = DSATabApplication.getInstance().getDBHelper()
					.getRuntimeDao(Armor.class);

			RuntimeExceptionDao<DistanceWeapon, Integer> distanceWeaponDao = DSATabApplication.getInstance()
					.getDBHelper().getRuntimeDao(DistanceWeapon.class);

			RuntimeExceptionDao<MiscSpecification, Integer> miscspecDao = DSATabApplication.getInstance().getDBHelper()
					.getRuntimeDao(MiscSpecification.class);

			RuntimeExceptionDao<Item, UUID> itemDao = DSATabApplication.getInstance().getDBHelper().getItemDao();

			PreparedQuery<Item> nameQuery = null;
			SelectArg nameArg = null;
			try {
				nameArg = new SelectArg();
				nameQuery = itemDao.queryBuilder().where().eq("name", nameArg).prepare();
			} catch (SQLException e) {
				Debug.error(e);
			}

			Iterator<String> i = null;
			ItemType type = null;
			Item item = null;
			String specLabel, name;
			while ((line = r.readLine()) != null) {

				if (TextUtils.isEmpty(line) || line.startsWith("#"))
					continue;

				item = new Item();
				splitter.setString(line);
				i = splitter.iterator();
				type = parseBase(item, i);

				specLabel = null;
				name = item.getName();
				// we have a encoded specLabel added to the item name e.g.
				// [einhändig]
				if (name.contains("[")) {
					int startSpec = name.indexOf('[');
					int endSpec = name.indexOf(']', startSpec);
					if (endSpec == -1) {
						throw new DsaTabRuntimeException(
								"Malformed items.txt file: Opening item specificaton '[' without closing bracket found at item "
										+ name);
					}
					specLabel = name.substring(startSpec + 1, endSpec);
					name = name.substring(0, startSpec);

					item.setName(name);
				} else {
					specLabel = null;
				}

				nameArg.setValue(item.getName());
				Item existingItem = itemDao.queryForFirst(nameQuery);
				if (existingItem != null)
					item = existingItem;

				// Debug.verbose(line);
				if (line.startsWith("W;")) {
					Weapon w = readWeapon(item, i);
					w.setSpecificationLabel(specLabel);
					item.addSpecification(w);
					weaponDao.create(w);
				} else if (line.startsWith("D;")) {
					DistanceWeapon w = readDistanceWeapon(item, i);
					w.setSpecificationLabel(specLabel);
					item.addSpecification(w);
					distanceWeaponDao.create(w);
				} else if (line.startsWith("A;")) {
					Armor w = readArmor(item, i, armorPositions);
					w.setSpecificationLabel(specLabel);
					item.addSpecification(w);
					armorDao.create(w);
				} else if (line.startsWith("S;")) {
					Shield w = readShield(item, i);
					w.setSpecificationLabel(specLabel);
					item.addSpecification(w);
					shieldDao.create(w);
				} else {
					MiscSpecification m = new MiscSpecification(item, type);
					m.setSpecificationLabel(specLabel);
					item.addSpecification(m);
					miscspecDao.create(m);
				}

				if (existingItem == null)
					itemDao.create(item);
				else
					itemDao.update(item);

			}
		} finally {
			try {
				if (r != null)
					r.close();
			} catch (IOException e) {
			}
		}
	}

	private static Weapon readWeapon(Item item, Iterator<String> i) {

		try {

			Weapon w = new Weapon(item);

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
				w.addCombatTalentType(CombatTalentType.valueOf(i.next()));
			}

			return w;

		} catch (NumberFormatException e) {
			Debug.error(e);
		}
		return null;
	}

	private static Armor readArmor(Item item, Iterator<String> i, List<Position> armorPositions) {

		Armor w = new Armor(item);

		w.setTotalBe(Util.parseFloat(i.next()));

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
		if (i.hasNext()) {
			int pieces = Util.parseInt(i.next());
			w.setTotalPieces(pieces);
		}
		return w;
	}

	private static DistanceWeapon readDistanceWeapon(Item item, Iterator<String> i) {

		try {
			DistanceWeapon w = new DistanceWeapon(item);

			w.setTp(i.next());
			w.setDistances(i.next());
			w.setTpDistances(i.next());

			while (i.hasNext()) { // type
				w.setCombatTalentType(CombatTalentType.valueOf(i.next()));
			}

			return w;
		} catch (NumberFormatException e) {
			Debug.error(e);
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
		Map<String, Item> items = null;
		// TODO read items from database;

		try {

			File itemsFile = new File(DSATabApplication.getDsaTabPath(), "items_new.txt");
			OutputStreamWriter itemsWriter = new OutputStreamWriter(new FileOutputStream(itemsFile), ENCODING);
			BufferedWriter itemsW = new BufferedWriter(itemsWriter, 1024 * 8);

			List<Item> its = new ArrayList<Item>(items.values());
			Collections.sort(its);

			BufferedWriter r = itemsW;
			String guessCategory = null;

			for (Item item : its) {

				for (ItemSpecification i : item.getSpecifications()) {
					if (i instanceof Weapon) {

						Weapon w = (Weapon) i;

						guessCategory = null;
						if (item.getCategory() == null) {

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
						appendItem(r, item, guessCategory);
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
						if (item.getCategory() == null) {

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
						appendItem(r, item, guessCategory);
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

						if (item.getCategory() == null) {
							if (w.isParadeWeapon()) {
								guessCategory = "Dolche";
							} else {
								guessCategory = "Schilde";
							}
						}

						r.append("S;");
						appendItem(r, item, guessCategory);
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
						appendItem(r, item, null);

						if (w.getTotalBe() < 10)
							r.append(" ");
						r.append(Util.toString(w.getTotalBe()));
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
						appendItem(r, item, null);
					}

					r.append("\n");
				}
			}

			itemsW.close();
		} catch (IOException e) {
			Debug.error(e);
		}
	}

	private static ItemType parseBase(Item item, Iterator<String> i) {

		ItemType type = null;
		String typeString = i.next();// itemtype
		if (!TextUtils.isEmpty(typeString))
			type = ItemType.fromCharacter(typeString.charAt(0)); // type

		String name = i.next().replace('_', ' ').trim();
		item.setName(name);

		item.setPath(i.next().trim()); // path

		item.setCategory(i.next().trim()); // category

		return type;
	}

	private static Shield readShield(Item item, Iterator<String> i) {

		Shield w = new Shield(item);

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
			w.addCombatTalentType(CombatTalentType.valueOf(i.next()));
		}

		return w;

	}

	public static Hero readHero(String path, InputStream in, HeroLoader heroLoader) throws JDOMException, IOException {

		Hero hero = null;

		SAXBuilder saxBuilder = new SAXBuilder();

		// DocumentBuilderFactory factory =
		// DocumentBuilderFactory.newInstance();
		// Debug.verbose("DomFactory created:" + factory.getClass().getName());
		//
		// DocumentBuilder builder = factory.newDocumentBuilder();
		// Debug.verbose("DocumentBuilder created:" +
		// builder.getClass().getName());

		InputStreamReader isr = new InputStreamReader(in, ENCODING);
		InputSource is = new InputSource();
		is.setCharacterStream(isr);
		is.setEncoding(ENCODING);

		org.jdom.Document dom = saxBuilder.build(is);

		if (dom != null)
			Debug.verbose("Document sucessfully parsed");
		else {
			Debug.error("Error: DOM was null.");
		}
		hero = new Hero(path, dom, heroLoader);
		Debug.verbose("Hero object created: " + hero.toString());

		return hero;
	}

	public static void writeHero(Hero hero, OutputStream out) throws IOException {
		if (hero == null)
			return;

		// Create an output formatter, and have it write the doc.
		XMLOutputter output = new XMLOutputter();
		output.output(hero.getDocument(), out);

	}

}