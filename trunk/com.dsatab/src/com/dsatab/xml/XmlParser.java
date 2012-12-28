package com.dsatab.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.json.JSONException;
import org.xml.sax.InputSource;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.text.TextUtils.StringSplitter;

import com.bugsense.trace.BugSenseHandler;
import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.data.Advantage;
import com.dsatab.data.Art;
import com.dsatab.data.ArtInfo;
import com.dsatab.data.ArtType;
import com.dsatab.data.Attribute;
import com.dsatab.data.ChangeEvent;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.Connection;
import com.dsatab.data.CustomAttribute;
import com.dsatab.data.Event;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroBaseInfo;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.Purse.Currency;
import com.dsatab.data.Purse.PurseUnit;
import com.dsatab.data.SpecialFeature;
import com.dsatab.data.Spell;
import com.dsatab.data.SpellInfo;
import com.dsatab.data.Talent;
import com.dsatab.data.Talent.Flags;
import com.dsatab.data.TalentGroup;
import com.dsatab.data.TalentGroup.TalentGroupType;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.EventCategory;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.BeidhaendigerKampf;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Hand;
import com.dsatab.data.items.HuntingWeapon;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.ItemType;
import com.dsatab.data.items.MiscSpecification;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.UsageType;
import com.dsatab.data.items.Weapon;
import com.dsatab.exception.InconsistentDataException;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;
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
				else
					itemDao.create(item);
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

			w.setWmAt(Util.parseInteger(wmAt));
			w.setWmPa(Util.parseInteger(wmPa));

			w.setIni(Util.parseInteger(i.next())); // INI

			w.setBf(Util.parseInteger(i.next())); // BF

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
			w.setRs(pos, Util.parseInteger(i.next()));
		}

		if (i.hasNext()) {
			int zonenRs = Util.parseInteger(i.next());
			w.setZonenRs(zonenRs);
		}
		if (i.hasNext()) {
			Integer totalRs = Util.parseInteger(i.next());
			w.setTotalRs(totalRs);
		}
		if (i.hasNext()) {
			int stars = Util.parseInteger(i.next());
			w.setStars(stars);
		}

		if (i.hasNext()) {
			String mod = i.next();
			if (mod.contains("Z"))
				w.setZonenHalfBe(true);
		}
		if (i.hasNext()) {
			int pieces = Util.parseInteger(i.next());
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

	// private static void appendItem(BufferedWriter r, Item i, String category)
	// throws IOException {
	// int lineLength = i.getName().length();
	// r.append(i.getName());
	//
	// lineLength++;
	// r.append(";");
	// if (i.path != null) {
	// r.append(i.path);
	// lineLength += i.path.length();
	// }
	// r.append(";");
	// lineLength++;
	// if (i.getCategory() == null) {
	// r.append(category);
	// lineLength += category.length();
	// } else {
	// r.append(i.getCategory());
	// lineLength += i.getCategory().length();
	// }
	//
	// int totalpad = 70 - lineLength;
	//
	// while (totalpad > 0) {
	// r.append(" ");
	// totalpad--;
	// }
	//
	// r.append(";");
	// lineLength++;
	//
	// }

	// public static void writeItems() {
	// Map<String, Item> items = null;
	// // TODO read items from database;
	//
	// try {
	//
	// File itemsFile = new File(DSATabApplication.getDsaTabPath(),
	// "items_new.txt");
	// OutputStreamWriter itemsWriter = new OutputStreamWriter(new
	// FileOutputStream(itemsFile), ENCODING);
	// BufferedWriter itemsW = new BufferedWriter(itemsWriter, 1024 * 8);
	//
	// List<Item> its = new ArrayList<Item>(items.values());
	// Collections.sort(its);
	//
	// BufferedWriter r = itemsW;
	// String guessCategory = null;
	//
	// for (Item item : its) {
	//
	// for (ItemSpecification i : item.getSpecifications()) {
	// if (i instanceof Weapon) {
	//
	// Weapon w = (Weapon) i;
	//
	// guessCategory = null;
	// if (item.getCategory() == null) {
	//
	// if (w.getCombatTalentType() == CombatTalentType.Zweihandhiebwaffen) {
	// guessCategory = "Zweihandhiebwaffen und -flegel";
	// } else if (w.getCombatTalentType() == CombatTalentType.Zweihandflegel) {
	// guessCategory = "Zweihandhiebwaffen und -flegel";
	// } else if (w.getCombatTalentType() == CombatTalentType.Zweihandschwerter)
	// {
	// guessCategory = "Zweihandschwerter und -säbel";
	// } else if (w.getCombatTalentType() == CombatTalentType.Speere) {
	// guessCategory = "Speere und Stäbe";
	// } else if (w.getCombatTalentType() == CombatTalentType.Stäbe) {
	// guessCategory = "Speere und Stäbe";
	// } else if (w.getCombatTalentTypes().size() == 1) {
	// guessCategory = w.getCombatTalentType().name();
	// }
	//
	// }
	//
	// r.append("W;");
	// appendItem(r, item, guessCategory);
	// r.append(w.getTp());
	// r.append(";");
	// r.append(Util.toString(w.getTpKKMin()) + "/" +
	// Util.toString(w.getTpKKStep()));
	// r.append(";");
	// r.append(Util.toString(w.getWmAt()) + "/" + Util.toString(w.getWmPa()));
	// r.append(";");
	// r.append(Util.toString(w.getIni()));
	// r.append(";");
	// r.append(Util.toString(w.getBf()));
	// r.append(";");
	// r.append(w.getDistance());
	// r.append(";");
	// r.append(w.isTwoHanded() ? "Z" : "");
	// r.append(";");
	//
	// for (CombatTalentType t : w.getCombatTalentTypes()) {
	// r.append(t.name());
	// r.append(";");
	// }
	// } else if (i instanceof DistanceWeapon) {
	//
	// DistanceWeapon w = (DistanceWeapon) i;
	//
	// guessCategory = null;
	// if (item.getCategory() == null) {
	//
	// if (w.getCombatTalentType() == CombatTalentType.Armbrust) {
	// guessCategory = "Schusswaffen";
	// } else if (w.getCombatTalentType() == CombatTalentType.Bogen) {
	// guessCategory = "Schusswaffen";
	// } else if (w.getCombatTalentType() == CombatTalentType.Blasrohr) {
	// guessCategory = "Schusswaffen";
	// } else if (w.getCombatTalentType() == CombatTalentType.Wurfbeile) {
	// guessCategory = "Wurfwaffen";
	// } else if (w.getCombatTalentType() == CombatTalentType.Wurfmesser) {
	// guessCategory = "Wurfwaffen";
	// } else if (w.getCombatTalentType() == CombatTalentType.Wurfspeere) {
	// guessCategory = "Wurfwaffen";
	// } else if (w.getCombatTalentType() == CombatTalentType.Schleuder) {
	// guessCategory = "Wurfwaffen";
	// }
	//
	// }
	//
	// r.append("D;");
	// appendItem(r, item, guessCategory);
	// r.append(w.getTp());
	// r.append(";");
	// r.append(w.getDistances());
	// r.append(";");
	// r.append(w.getTpDistances());
	// r.append(";");
	// r.append(w.getCombatTalentType().name());
	// r.append(";");
	//
	// } else if (i instanceof Shield) {
	//
	// Shield w = (Shield) i;
	// guessCategory = null;
	//
	// if (item.getCategory() == null) {
	// if (w.isParadeWeapon()) {
	// guessCategory = "Dolche";
	// } else {
	// guessCategory = "Schilde";
	// }
	// }
	//
	// r.append("S;");
	// appendItem(r, item, guessCategory);
	// r.append(w.getWmAt() + "/" + w.getWmPa());
	// r.append(";");
	// r.append(Util.toString(w.getIni()));
	// r.append(";");
	// r.append(Util.toString(w.getBf()));
	// r.append(";");
	// r.append(w.isShield() ? "S" : "");
	// r.append(w.isParadeWeapon() ? "P" : "");
	// r.append(";");
	//
	// for (CombatTalentType t : w.getCombatTalentTypes()) {
	// r.append(t.name());
	// r.append(";");
	// }
	//
	// } else if (i instanceof Armor) {
	//
	// Armor w = (Armor) i;
	// r.append("A;");
	// appendItem(r, item, null);
	//
	// if (w.getTotalBe() < 10)
	// r.append(" ");
	// r.append(Util.toString(w.getTotalBe()));
	// r.append(";");
	//
	// for (Position pos :
	// DSATabApplication.getInstance().getConfiguration().getArmorPositions()) {
	// int rs = w.getRs(pos);
	// if (rs < 10)
	// r.append(" ");
	// r.append(Util.toString(rs));
	// r.append(";");
	// }
	//
	// r.append(Util.toString(w.getZonenRs()));
	// r.append(";");
	// r.append(Util.toString(w.getTotalRs()));
	// r.append(";");
	// r.append(Util.toString(w.getStars()));
	// r.append(";");
	// if (w.isZonenHalfBe()) {
	// r.append("Z");
	// r.append(";");
	// }
	//
	// } else {
	// r = itemsW;
	// r.append(i.getType().character());
	// r.append(";");
	// appendItem(r, item, null);
	// }
	//
	// r.append("\n");
	// }
	// }
	//
	// itemsW.close();
	// } catch (IOException e) {
	// Debug.error(e);
	// }
	// }

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

		w.setWmAt(Util.parseInteger(wmAt));
		w.setWmPa(Util.parseInteger(wmPa));

		w.setIni(Util.parseInteger(i.next()));
		w.setBf(Util.parseInteger(i.next()));

		String type = i.next().toLowerCase(Locale.GERMAN); // typ

		if (type.contains("p"))
			w.setParadeWeapon(true);
		if (type.contains("s"))
			w.setShield(true);

		while (i.hasNext()) { // type
			w.addCombatTalentType(CombatTalentType.valueOf(i.next()));
		}

		return w;

	}

	public static Document readDocument(InputStream in) throws JDOMException, IOException {
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

		org.jdom2.Document dom = saxBuilder.build(is);

		if (dom == null) {
			Debug.error("Error: DOM was null.");
		}

		return dom;
	}

	public static Hero readHero(String path, InputStream in) throws JDOMException, IOException, JSONException {

		Hero hero = null;

		Document dom = readDocument(in);

		Element heroElement = (Element) dom.getRootElement().getChild(Xml.KEY_HELD);
		// check for valid hero node
		if (heroElement == null) {
			throw new DsaTabRuntimeException("Invalid Hero xml file, could not find <" + Xml.KEY_HELD
					+ "> element with in root node");
		}
		hero = new Hero(path);

		String tabConfig = heroElement.getAttributeValue(Xml.TAB_CONFIG);
		hero.loadHeroConfiguration(tabConfig);

		hero.setName(heroElement.getAttributeValue(Xml.KEY_NAME));
		hero.setKey(heroElement.getAttributeValue(Xml.KEY_KEY));
		if (heroElement.getAttributeValue(Xml.KEY_PORTRAIT_PATH) != null) {
			hero.setProfileUri(Uri.parse(heroElement.getAttributeValue(Xml.KEY_PORTRAIT_PATH)));
		}

		Element xpElement = DomUtil.getChildByTagName(heroElement, Xml.KEY_BASIS, Xml.KEY_ABENTEUERPUNKTE);
		hero.getExperience().setValue(Util.parseInteger(xpElement.getAttributeValue(Xml.KEY_VALUE)));

		Element freeXpElement = DomUtil.getChildByTagName(heroElement, Xml.KEY_BASIS, Xml.KEY_FREIE_ABENTEUERPUNKTE);
		hero.getFreeExperience().setValue(Util.parseInteger(freeXpElement.getAttributeValue(Xml.KEY_VALUE)));

		fillAttributes(hero, heroElement);

		Element basisElement = heroElement.getChild(Xml.KEY_BASIS);
		if (basisElement == null)
			basisElement = heroElement;
		fillBaseInfo(hero, basisElement);

		fillTalents(hero, heroElement);
		fillSpells(hero, heroElement);
		fillArtsAndSpecialFeatures(hero, heroElement);
		fillAdvantages(hero, heroElement);
		fillItems(hero, heroElement);
		fillEquippedItems(hero, heroElement);
		fillPurse(hero, heroElement);
		fillEvents(hero, heroElement);
		fillConnections(hero, heroElement);
		hero.onPostHeroLoaded();
		return hero;
	}

	/**
	 * Depends on Talent, Spell, Arts
	 * 
	 * @param heldElement
	 */
	private static void fillAdvantages(Hero hero, Element heldElement) {

		List<Element> sfs = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_VORTEILE, Xml.KEY_VORTEIL);

		for (Element element : sfs) {
			Advantage adv = new Advantage();

			adv.setName(element.getAttributeValue(Xml.KEY_NAME));
			String value = element.getAttributeValue(Xml.KEY_VALUE);
			if (!TextUtils.isEmpty(value)) {
				adv.addValue(value);
			}
			adv.setComment(element.getAttributeValue(Xml.KEY_COMMENT));

			if (adv.getName().equals(Advantage.BEGABUNG_FUER_TALENT)) {
				Talent talent = hero.getTalent(adv.getValueAsString());
				if (talent != null) {
					talent.addFlag(Flags.Begabung);
				}
			} else if (adv.getName().equals(Advantage.TALENTSCHUB)) {
				Talent talent = hero.getTalent(adv.getValueAsString());
				if (talent != null) {
					talent.addFlag(Flags.Talentschub);
				}
			} else if (adv.getName().equals(Advantage.MEISTERHANDWERK)) {
				Talent talent = hero.getTalent(adv.getValueAsString());
				if (talent != null) {
					talent.addFlag(Flags.Meisterhandwerk);
				}
			} else if (adv.getName().equals(Advantage.BEGABUNG_FUER_TALENTGRUPPE)) {
				try {
					TalentGroupType groupType = TalentGroupType.valueOf(adv.getValueAsString());
					TalentGroup talentGroup = hero.getTalentGroups().get(groupType);
					if (talentGroup != null) {
						talentGroup.addFlag(Flags.Begabung);
					}
				} catch (Exception e) {
					Debug.warning("Begabung für [Talentgruppe], unknown talentgroup:" + adv.getValueAsString());
				}
			} else if (adv.getName().equals(Advantage.BEGABUNG_FUER_ZAUBER)) {
				Spell spell = hero.getSpells().get(adv.getValueAsString());
				if (spell != null) {
					spell.addFlag(com.dsatab.data.Spell.Flags.Begabung);
				}

			} else if (adv.getName().equals(Advantage.BEGABUNG_FUER_RITUAL)) {
				Art art = hero.getArt(adv.getValueAsString());
				if (art != null) {
					art.addFlag(com.dsatab.data.Art.Flags.Begabung);
				}

			} else if (adv.getName().equals(Advantage.UEBERNATUERLICHE_BEGABUNG)) {
				Spell spell = hero.getSpell(adv.getValueAsString());
				if (spell != null) {
					spell.addFlag(com.dsatab.data.Spell.Flags.ÜbernatürlicheBegabung);

				}
			}

			hero.addAdvantage(adv);
		}
	}

	/**
	 * Depends on Talents and Spells
	 * 
	 * @param hero
	 * @param heldElement
	 */
	private static void fillArtsAndSpecialFeatures(Hero hero, Element heldElement) {

		List<Element> sf = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_SONDERFERTIGKEITEN,
				Xml.KEY_SONDERFERTIGKEIT);

		for (Element element : sf) {

			String name = element.getAttributeValue(Xml.KEY_NAME).trim();
			ArtType type = ArtType.getTypeOfArt(name);
			if (type == null) {
				SpecialFeature specialFeature = new SpecialFeature();

				specialFeature.setName(element.getAttributeValue(Xml.KEY_NAME));
				specialFeature.setComment(element.getAttributeValue(Xml.KEY_KOMMENTAR));

				StringBuilder specialSB = new StringBuilder();

				List<Element> kulturChildren = element.getChildren(Xml.KEY_KULTUR);
				if (kulturChildren != null) {
					for (Element child : kulturChildren) {
						if (specialSB.length() == 0)
							specialSB.append(", ");
						specialSB.append(child.getAttributeValue(Xml.KEY_NAME));
					}
				}

				List<Element> auswahlChildren = element.getChildren(Xml.KEY_AUSWAHL);
				if (auswahlChildren != null) {
					for (Element child : auswahlChildren) {
						if (specialSB.length() == 0)
							specialSB.append(", ");
						specialSB.append(child.getAttributeValue(Xml.KEY_NAME));
					}
				}
				specialFeature.setAdditionalInfo(specialSB.toString());

				Element child = element.getChild(Xml.KEY_GEGENSTAND);
				if (child != null) {
					specialFeature.setParameter1(child.getAttributeValue(Xml.KEY_NAME));
				}

				if (specialFeature.getName().startsWith(SpecialFeature.TALENTSPEZIALISIERUNG_PREFIX)) {
					child = element.getChild(Xml.KEY_TALENT);
					if (child != null) {
						specialFeature.setParameter1(child.getAttributeValue(Xml.KEY_NAME));
					}
					child = element.getChild(Xml.KEY_SPEZIALISIERUNG);
					if (child != null) {
						specialFeature.setParameter2(child.getAttributeValue(Xml.KEY_NAME));
					}
				} else if (specialFeature.getName().startsWith(SpecialFeature.ZAUBERSPEZIALISIERUNG_PREFIX)) {
					child = element.getChild(Xml.KEY_ZAUBER);
					if (child != null) {
						specialFeature.setParameter1(child.getAttributeValue(Xml.KEY_NAME));
					}
					child = element.getChild(Xml.KEY_SPEZIALISIERUNG);
					if (child != null) {
						specialFeature.setParameter2(child.getAttributeValue(Xml.KEY_NAME));
					}
				}
				boolean add = true;

				if (specialFeature.getName().startsWith(SpecialFeature.TALENTSPEZIALISIERUNG_PREFIX)) {
					Talent talent = hero.getTalent(specialFeature.getParameter1());
					if (talent != null) {
						talent.setTalentSpezialisierung(specialFeature.getParameter2());
						add = false;
					}
				} else if (specialFeature.getName().startsWith(SpecialFeature.ZAUBERSPEZIALISIERUNG_PREFIX)) {
					Spell spell = hero.getSpell(specialFeature.getParameter1());
					if (spell != null) {
						spell.setZauberSpezialisierung(specialFeature.getParameter2());
						add = false;
					}
				} else if (specialFeature.getName().startsWith(Talent.RITUAL_KENNTNIS_PREFIX)) {
					// skipp specialfeature ritualkenntnis since it's listed as
					// talent anyway.
					add = false;
				}

				if (add) {
					hero.addSpecialFeature(specialFeature);
				}
			} else {
				Art art = new Art(hero);

				art.setUnused(Boolean.parseBoolean(element.getAttributeValue(Xml.KEY_UNUSED)));
				art.setFavorite(Boolean.parseBoolean(element.getAttributeValue(Xml.KEY_FAVORITE)));

				art.setName(element.getAttributeValue(Xml.KEY_NAME).trim());
				art.setEffect(element.getAttributeValue(Xml.KEY_WIRKUNG));
				art.setCastDuration(element.getAttributeValue(Xml.KEY_DAUER));
				art.setCosts(element.getAttributeValue(Xml.KEY_KOSTEN));

				if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_PROBE))) {
					art.setProbePattern(element.getAttributeValue(Xml.KEY_PROBE));
				}

				hero.addArt(art);
			}

		}
	}

	private static void fillBaseInfo(Hero hero, Element basisElement) {
		HeroBaseInfo info = hero.getBaseInfo();

		Element rasse = basisElement.getChild(Xml.KEY_RASSE);
		Element ausbildungen = basisElement.getChild(Xml.KEY_AUSBILDUNGEN);
		Element kultur = basisElement.getChild(Xml.KEY_KULTUR);
		Element aussehen = null, groesse = null;
		if (rasse != null) {
			aussehen = rasse.getChild(Xml.KEY_AUSSEHEN);
			groesse = rasse.getChild(Xml.KEY_GROESSE);
		}

		if (groesse != null) {
			info.setWeight(Util.parseInteger(groesse.getAttributeValue(Xml.KEY_GEWICHT)));
			info.setHeight(Util.parseInteger(groesse.getAttributeValue(Xml.KEY_VALUE)));
		}

		if (aussehen != null) {
			info.setAge(Util.parseInteger(aussehen.getAttributeValue(Xml.KEY_ALTER)));
			info.setEyeColor(aussehen.getAttributeValue(Xml.KEY_EYECOLOR));
			info.setHairColor(aussehen.getAttributeValue(Xml.KEY_HAIRCOLOR));

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				sb.append(aussehen.getAttributeValue(Xml.KEY_AUSSEHENTEXT_PREFIX + i));
				if (!TextUtils.isEmpty(aussehen.getAttributeValue(Xml.KEY_AUSSEHENTEXT_PREFIX + (i + 1))))
					sb.append(", ");
			}
			info.setLook(sb.toString());

			info.setTitle(aussehen.getAttributeValue(Xml.KEY_TITEL));

			info.setRank(aussehen.getAttributeValue(Xml.KEY_STAND));

		}

		if (rasse != null) {
			info.setRace(rasse.getAttributeValue(Xml.KEY_STRING));
		}

		if (ausbildungen != null) {
			List<Element> ausbildungElements = ausbildungen.getChildren();

			StringBuilder sb = new StringBuilder();

			for (Element ausbildung : ausbildungElements) {
				String value = ausbildung.getAttributeValue(Xml.KEY_STRING);
				if (!TextUtils.isEmpty(value)) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(value);
				}
			}
			info.setEducation(sb.toString());
		}

		if (kultur != null) {
			info.setCulture(kultur.getAttributeValue(Xml.KEY_STRING));
		}
	}

	private static void fillAttributes(Hero hero, Element heldElement) {

		List<Element> domAttributes = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_EIGENSCHAFTEN,
				Xml.KEY_EIGENSCHAFT);

		for (Element attributeElement : domAttributes) {

			Attribute attr = new Attribute(hero);
			attr.setName(attributeElement.getAttributeValue(Xml.KEY_NAME));
			attr.setType(AttributeType.valueOf(attributeElement.getAttributeValue(Xml.KEY_NAME)));
			attr.setValue(Util.parseInteger(attributeElement.getAttributeValue(Xml.KEY_VALUE)));
			attr.setMod(Util.parseInteger(attributeElement.getAttributeValue(Xml.KEY_MOD)));
			hero.addAttribute(attr);
		}

		for (CustomAttribute attr : hero.getHeroConfiguration().getAttributes()) {
			hero.addAttribute(attr);
		}

		if (!hero.hasAttribute(AttributeType.Lebensenergie_Aktuell)) {
			CustomAttribute le = new CustomAttribute(hero, AttributeType.Lebensenergie_Aktuell);
			le.setValue(hero.getAttributeValue(AttributeType.Lebensenergie));
			le.setReferenceValue(le.getValue());
			hero.addAttribute(le);
		}
		if (!hero.hasAttribute(AttributeType.Ausdauer_Aktuell)) {
			CustomAttribute le = new CustomAttribute(hero, AttributeType.Ausdauer_Aktuell);
			le.setValue(hero.getAttributeValue(AttributeType.Ausdauer));
			le.setReferenceValue(le.getValue());
			hero.addAttribute(le);
		}

		if (!hero.hasAttribute(AttributeType.Karmaenergie_Aktuell)) {
			CustomAttribute le = new CustomAttribute(hero, AttributeType.Karmaenergie_Aktuell);
			le.setValue(hero.getAttributeValue(AttributeType.Karmaenergie));
			le.setReferenceValue(le.getValue());
			hero.addAttribute(le);
		}
		if (!hero.hasAttribute(AttributeType.Astralenergie_Aktuell)) {
			CustomAttribute le = new CustomAttribute(hero, AttributeType.Astralenergie_Aktuell);
			le.setValue(hero.getAttributeValue(AttributeType.Astralenergie));
			le.setReferenceValue(le.getValue());
			hero.addAttribute(le);
		}

		if (!hero.hasAttribute(AttributeType.Behinderung)) {
			CustomAttribute be = new CustomAttribute(hero, AttributeType.Behinderung);
			hero.addAttribute(be);
		}
		if (!hero.hasAttribute(AttributeType.Ausweichen)) {
			CustomAttribute aw = new CustomAttribute(hero, AttributeType.Ausweichen);
			hero.addAttribute(aw);
		}
		if (!hero.hasAttribute(AttributeType.Geschwindigkeit)) {
			CustomAttribute gs = new CustomAttribute(hero, AttributeType.Geschwindigkeit);
			hero.addAttribute(gs);
		}

		if (!hero.hasAttribute(AttributeType.Initiative_Aktuell)) {
			CustomAttribute ini = new CustomAttribute(hero, AttributeType.Initiative_Aktuell);
			ini.setValue(0);
			hero.addAttribute(ini);
		}

		// if (!hero.hasAttribute(AttributeType.pa)) {
		// Element element = new Element(Xml.KEY_EIGENSCHAFT);
		// element.setAttribute(Xml.KEY_NAME, AttributeType.pa.name());
		//
		// int basefk = getAttributeValue(AttributeType.Intuition) +
		// getAttributeValue(AttributeType.Gewandtheit)
		// + getAttributeValue(AttributeType.Körperkraft);
		// basefk = Math.round(basefk / 5.0F);
		// element.setAttribute(Xml.KEY_VALUE, Util.toString(basefk));
		// element.setAttribute(Xml.KEY_MOD, "0");
		// this.attributes.put(AttributeType.pa, new Attribute(element, this));
		// }
		//
		// if (type == AttributeType.at && !this.attributes.containsKey(type)) {
		// Element element = new Element(Xml.KEY_EIGENSCHAFT);
		// element.setAttribute(Xml.KEY_NAME, AttributeType.at.name());
		//
		// int basefk = getAttributeValue(AttributeType.Mut) +
		// getAttributeValue(AttributeType.Gewandtheit)
		// + getAttributeValue(AttributeType.Körperkraft);
		// basefk = Math.round(basefk / 5.0F);
		// element.setAttribute(Xml.KEY_VALUE, Util.toString(basefk));
		// element.setAttribute(Xml.KEY_MOD, "0");
		// this.attributes.put(AttributeType.at, new Attribute(element, this));
		// }
		//
		// if (type == AttributeType.ini && !this.attributes.containsKey(type))
		// {
		// Element element = new Element(Xml.KEY_EIGENSCHAFT);
		// element.setAttribute(Xml.KEY_NAME, AttributeType.ini.name());
		//
		// int basefk = getAttributeValue(AttributeType.Mut) +
		// getAttributeValue(AttributeType.Mut)
		// + getAttributeValue(AttributeType.Intuition) +
		// getAttributeValue(AttributeType.Gewandtheit);
		// basefk = Math.round(basefk / 5.0F);
		// element.setAttribute(Xml.KEY_VALUE, Util.toString(basefk));
		// element.setAttribute(Xml.KEY_MOD, "0");
		// this.attributes.put(AttributeType.ini, new Attribute(element, this));
		// }

		if (!hero.hasAttribute(AttributeType.Entrueckung)) {
			CustomAttribute entr = new CustomAttribute(hero, AttributeType.Entrueckung);
			entr.setValue(0);
			hero.addAttribute(entr);
		}

		if (!hero.hasAttribute(AttributeType.Verzueckung)) {
			CustomAttribute entr = new CustomAttribute(hero, AttributeType.Verzueckung);
			entr.setValue(0);
			hero.addAttribute(entr);
		}

		if (!hero.hasAttribute(AttributeType.Erschoepfung)) {
			CustomAttribute entr = new CustomAttribute(hero, AttributeType.Erschoepfung);
			entr.setValue(0);
			hero.addAttribute(entr);
		}

	}

	private static Element getItemElement(Element held) {
		Element itemsNode = held.getChild(Xml.KEY_GEGENSTAENDE_AE);
		if (itemsNode != null) {
			itemsNode.setName(Xml.KEY_GEGENSTAENDE);
		} else {
			itemsNode = held.getChild(Xml.KEY_GEGENSTAENDE);
		}
		if (itemsNode == null) {
			itemsNode = held;
		}

		return itemsNode;
	}

	private static Element getConnectionsElement(Element heroElement) {
		Element connectionsElement = heroElement.getChild(Xml.KEY_VERBINDUNGEN);
		if (connectionsElement == null) {
			connectionsElement = new Element(Xml.KEY_VERBINDUNGEN);
			heroElement.addContent(connectionsElement);
		}
		return connectionsElement;
	}

	private static Element getEquippmentElement(Element held) {
		Element equippmentNode = held.getChild(Xml.KEY_AUSRUESTUNGEN_UE);
		if (equippmentNode != null) {
			// for newer android versions rename ausrüstung back to ü
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
				equippmentNode.setName(Xml.KEY_AUSRUESTUNGEN);
		} else {
			equippmentNode = held.getChild(Xml.KEY_AUSRUESTUNGEN);
		}
		// for older heldensoftware verson there was no ausrüstungen tag, just
		// the held tag use this one.
		if (equippmentNode == null) {
			equippmentNode = held;
		}

		return equippmentNode;

	}

	protected static void fillConnections(Hero hero, Element heroElement) {
		List<Element> connectionElements = DomUtil.getChildrenByTagName(heroElement, Xml.KEY_VERBINDUNGEN,
				Xml.KEY_VERBINDUNG);

		for (Element element : connectionElements) {

			Connection connection = new Connection();
			connection.setDescription(element.getAttributeValue(Xml.KEY_DESCRIPTION));
			connection.setSozialStatus(element.getAttributeValue(Xml.KEY_SO));
			connection.setName(element.getAttributeValue(Xml.KEY_NAME));
			hero.addConnection(connection);
		}

	}

	protected static void fillEvents(Hero hero, Element heroElement) {
		Element notiz = DomUtil.getChildByTagName(heroElement, Xml.KEY_BASIS, Xml.KEY_NOTIZ);

		if (notiz != null) {
			Event event = new Event(notiz);

			if (Xml.KEY_NOTIZ.equals(notiz.getName())) {

				event.setCategory(EventCategory.Heldensoftware);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i <= 11; i++) {
					String commentLine = notiz.getAttributeValue(Xml.KEY_NOTIZ_PREFIX + i);
					if (!TextUtils.isEmpty(commentLine)) {
						sb.append(commentLine);
					}
					sb.append("\n");
				}
				event.setComment(sb.toString().trim());
			}
			hero.getHeroConfiguration().addEvent(event);
		}

		Collections.sort(hero.getHeroConfiguration().getEvents(), Event.COMPARATOR);
	}

	protected static void fillPurse(Hero hero, Element heroElement) {
		Element purseElement = heroElement.getChild(Xml.KEY_GELDBOERSE);
		if (purseElement != null) {
			List<?> nodes = purseElement.getChildren(Xml.KEY_MUENZE);

			for (int i = 0; i < nodes.size(); i++) {
				Element m = (Element) nodes.get(i);
				PurseUnit w = PurseUnit.getByXmlName(m.getAttributeValue(Xml.KEY_NAME));
				Integer value = Util.parseInteger(m.getAttributeValue(Xml.KEY_ANZAHL));
				hero.getPurse().setCoins(w, value);
			}

			String active = purseElement.getAttributeValue(Xml.KEY_ACTIVE);
			if (active != null)
				hero.getPurse().setActiveCurrency(Currency.valueOf(active));
		}
	}

	protected static void fillEquippedItems(Hero hero, Element heroElement) {

		Element equippmentNode = getEquippmentElement(heroElement);

		for (int selectedSet = 0; selectedSet < Hero.MAXIMUM_SET_NUMBER; selectedSet++) {

			List<Element> equippedElements = DomUtil.getChildrenByTagName(equippmentNode, Xml.KEY_HELDENAUSRUESTUNG);

			List<Element> beidhaendigerKampfElements = new ArrayList<Element>();

			List<EquippedItem> secondaryItems = new ArrayList<EquippedItem>();

			for (int i = 0; i < equippedElements.size(); i++) {
				Element element = (Element) equippedElements.get(i);

				if (element.getAttribute(Xml.KEY_SET) != null) {
					if (Util.parseInt(element.getAttributeValue(Xml.KEY_SET)) != selectedSet)
						continue;
				}

				if (element.getAttributeValue(Xml.KEY_NAME).equals(Hero.JAGTWAFFE)) {
					HuntingWeapon huntingWeapon = new HuntingWeapon();
					huntingWeapon.setNumber(Util.parseInteger(element.getAttributeValue(Xml.KEY_NUMMER)));
					huntingWeapon.setSet(Util.parseInteger(element.getAttributeValue(Xml.KEY_SET)));
					hero.addHuntingWeapon(huntingWeapon);
					continue;
				}

				if (element.getAttributeValue(Xml.KEY_NAME).startsWith(Hero.PREFIX_BK)) {
					beidhaendigerKampfElements.add(element);
					continue;
				}

				String itemName = element.getAttributeValue(EquippedItem.WAFFENNAME);
				String itemSlot = element.getAttributeValue(Xml.KEY_SLOT);
				if (itemName == null)
					itemName = element.getAttributeValue(EquippedItem.SCHILDNAME);
				if (itemName == null)
					itemName = element.getAttributeValue(EquippedItem.RUESTUNGSNAME);

				Item item = hero.getItem(itemName, itemSlot);

				if (item == null) {
					BugSenseHandler.log(Debug.CATEGORY_DATA, new InconsistentDataException(
							"Unable to find an item with the name '" + itemName + "' in slot '" + itemSlot + "'."));
				}

				EquippedItem equippedItem = new EquippedItem(hero);

				if (element.getAttribute(Xml.KEY_CELL_NUMBER) != null)
					equippedItem.getItemInfo().setCellNumber(
							Util.parseInteger(element.getAttributeValue(Xml.KEY_CELL_NUMBER)));
				if (element.getAttribute(Xml.KEY_SCREEN) != null) {
					// there is only one inventory screen left...
					int screen = Util.parseInteger(element.getAttributeValue(Xml.KEY_SCREEN));
					if (screen > Hero.MAXIMUM_SET_NUMBER)
						screen = Hero.MAXIMUM_SET_NUMBER;
					equippedItem.getItemInfo().setScreen(screen);
				}

				if (element.getAttribute(Xml.KEY_HAND) != null) {
					equippedItem.setHand(Hand.valueOf(element.getAttributeValue(Xml.KEY_HAND)));
				}

				equippedItem.setSet(Util.parseInt(element.getAttributeValue(Xml.KEY_SET), 0));

				equippedItem.setSlot(itemSlot);
				equippedItem.setName(element.getAttributeValue(Xml.KEY_NAME));
				equippedItem.setTalentName(element.getAttributeValue(Xml.KEY_TALENT));

				equippedItem.setItem(item);

				if (element.getAttribute(Xml.KEY_VERWENDUNGSART) != null)
					equippedItem.setUsageType(UsageType.valueOf(element.getAttributeValue(Xml.KEY_VERWENDUNGSART)));

				equippedItem.setSchildIndex(Util.parseInteger(element.getAttributeValue(Xml.KEY_SCHILD)));

				String value = element.getAttributeValue(Xml.KEY_BEZEICHNER);
				if (TextUtils.isEmpty(value))
					equippedItem.setItemSpecificationLabel(null);
				else
					equippedItem.setItemSpecificationLabel(value);

				// fix wrong screen iteminfo
				if (equippedItem.getItemInfo().getScreen() == ItemLocationInfo.INVALID_POSITION) {
					equippedItem.getItemInfo().setScreen(equippedItem.getSet());
				}

				if (element.getAttributeValue(Xml.KEY_SCHILD) != null) {
					if (Util.parseInt(element.getAttributeValue(Xml.KEY_SCHILD)) > 0) {
						secondaryItems.add(equippedItem);
					}
				}

				if (equippedItem.getItem() != null) {
					hero.addEquippedItem(equippedItem);
				} else {
					Debug.warning("Skipped EquippedItem because Item was not found: " + itemName);
				}
			}

			// handle bk elements
			for (Iterator<Element> iter = beidhaendigerKampfElements.iterator(); iter.hasNext();) {

				Element element = iter.next();

				if (element.getAttribute(Xml.KEY_SET) != null) {
					if (Util.parseInt(element.getAttributeValue(Xml.KEY_SET)) != selectedSet)
						continue;
				}

				if (element.getAttributeValue(Xml.KEY_NAME).startsWith(Hero.PREFIX_BK)) {
					String bk = element.getAttributeValue(Xml.KEY_NAME);
					int bk1 = Util.parseInt(bk.substring(2, 3));
					int bk2 = Util.parseInt(bk.substring(3, 4));

					EquippedItem item1 = hero.getEquippedItem(selectedSet, Hero.PREFIX_NKWAFFE + bk1);
					EquippedItem item2 = hero.getEquippedItem(selectedSet, Hero.PREFIX_NKWAFFE + bk2);

					if (item2 != null && item1 != null) {
						item1.setSecondaryItem(item2);
						item2.setSecondaryItem(item1);
						hero.addBeidhaendigerKampf(item1, item2);
					} else {
						Debug.warning("Incorrect BeidhaendigerKampf setting " + bk);
						heroElement.removeContent(element);
						iter.remove();
					}
				}

			}

			for (EquippedItem equippedItem : secondaryItems) {
				if (equippedItem.getSchildIndex() != null) {
					if (equippedItem.getSchildIndex() > 0) {
						EquippedItem secondaryEquippedItem = hero.getEquippedItem(EquippedItem.NAME_PREFIX_SCHILD
								+ equippedItem.getSchildIndex());
						if (secondaryEquippedItem != null) {
							equippedItem.setSecondaryItem(secondaryEquippedItem);
							secondaryEquippedItem.setSecondaryItem(equippedItem);
						}
					}
				}
			}

		}
	}

	private static void fillItems(Hero hero, Element heroElement) {

		List<Element> itemsElements = DomUtil.getChildrenByTagName(heroElement, Xml.KEY_GEGENSTAENDE,
				Xml.KEY_GEGENSTAND);

		for (Element element : itemsElements) {

			if (element.getAttribute(Xml.KEY_NAME) != null) {

				Item item = DataManager.getItemByName(element.getAttributeValue(Xml.KEY_NAME));

				if (item != null) {
					item = (Item) item.duplicate();
				} else {
					Debug.warning("Item not found generating it:" + element.getAttributeValue(Xml.KEY_NAME));

					item = new Item();
					item.setName(element.getAttributeValue(Xml.KEY_NAME));
					item.addSpecification(new MiscSpecification(item, ItemType.Sonstiges));
					item.setId(UUID.randomUUID());
					item.setCategory("Sonstiges");
				}

				if (element.getAttribute(Xml.KEY_CELL_NUMBER) != null)
					item.getItemInfo().setCellNumber(Util.parseInteger(element.getAttributeValue(Xml.KEY_CELL_NUMBER)));
				if (element.getAttribute(Xml.KEY_SCREEN) != null) {
					// there is only one inventory screen left...
					int screen = Util.parseInteger(element.getAttributeValue(Xml.KEY_SCREEN));
					if (screen > Hero.MAXIMUM_SET_NUMBER)
						screen = Hero.MAXIMUM_SET_NUMBER;
					item.getItemInfo().setScreen(screen);
				}

				Element domallgemein = element.getChild(Xml.KEY_MOD_ALLGEMEIN);
				if (domallgemein != null) {
					Element name = domallgemein.getChild(Xml.KEY_NAME);
					if (name != null) {
						item.setTitle(name.getAttributeValue(Xml.KEY_VALUE));
					}
				}

				item.setCount(Util.parseInt(element.getAttributeValue(Xml.KEY_ANZAHL), 1));
				item.setSlot(element.getAttributeValue(Xml.KEY_SLOT));

				// fix invalid screen positions (items are always on the
				// last page if nothing else is defined
				if (item.getItemInfo().getScreen() == ItemLocationInfo.INVALID_POSITION) {
					item.getItemInfo().setScreen(Hero.MAXIMUM_SET_NUMBER);
				}

				for (ItemSpecification itemSpecification : item.getSpecifications()) {
					if (itemSpecification instanceof Armor) {
						Armor armor = (Armor) itemSpecification;

						Element ruestung = element.getChild(Xml.KEY_RUESTUNG);

						// revert changes made due to harmony parsing bug of
						// umlauts
						if (ruestung == null) {
							ruestung = element.getChild(Xml.KEY_RUESTUNG_UE);
							if (ruestung != null) {
								ruestung.setName(Xml.KEY_RUESTUNG);
							}
						}

						if (ruestung != null) {

							String be = DomUtil.getChildValue(ruestung, Xml.KEY_GESAMT_BE, Xml.KEY_VALUE);
							if (be != null) {
								armor.setTotalBe(Util.parseFloat(be));
							}
							for (Position pos : Position.values()) {
								String rs = DomUtil.getChildValue(ruestung, pos.name().toLowerCase(Locale.GERMAN),
										Xml.KEY_VALUE);
								if (rs != null) {
									armor.setRs(pos, Util.parseInteger(rs));
								}
							}

							String rs = DomUtil.getChildValue(ruestung, Xml.KEY_RS, Xml.KEY_VALUE);
							if (rs != null) {
								armor.setTotalRs(Util.parseInteger(rs));
							}

							rs = DomUtil.getChildValue(ruestung, Xml.KEY_STERNE, Xml.KEY_VALUE);
							if (rs != null) {
								armor.setStars(Util.parseInteger(rs));
							}

						}
					}

					if (itemSpecification instanceof DistanceWeapon) {
						DistanceWeapon distanceWeapon = (DistanceWeapon) itemSpecification;

						List<Element> waffen = element.getChildren(Xml.KEY_FERNKAMPWAFFE);

						Element child;
						for (Element waffe : waffen) {

							child = waffe.getChild(Xml.KEY_ENTFERNUNG);
							if (child != null) {
								for (int i = 0; i < DistanceWeapon.DISTANCE_COUNT; i++) {
									String value = child.getAttributeValue("E" + i);
									if (!TextUtils.isEmpty(value)) {
										distanceWeapon.setDistances(i, value);
									}
								}
							}

							child = waffe.getChild(Xml.KEY_TPMOD);
							if (child != null) {
								for (int i = 0; i < DistanceWeapon.DISTANCE_COUNT; i++) {
									String value = child.getAttributeValue("M" + i);
									if (!TextUtils.isEmpty(value)) {
										distanceWeapon.setTpDistances(i, value);
									}
								}
							}

							child = waffe.getChild(Xml.KEY_TREFFERPUNKTE);
							if (child != null) {
								String tp = child.getAttributeValue(Xml.KEY_TREFFERPUNKTE_MUL) + "W"
										+ child.getAttributeValue(Xml.KEY_TREFFERPUNKTE_DICE) + "+"
										+ child.getAttributeValue(Xml.KEY_TREFFERPUNKTE_SUM);
								distanceWeapon.setTp(tp);
							}
						}

					}

					if (itemSpecification instanceof Shield) {
						Shield shield = (Shield) itemSpecification;

						List<Element> waffen = element.getChildren(Xml.KEY_SCHILDWAFFE);

						for (Element waffe : waffen) {
							Element wm = waffe.getChild(Xml.KEY_WAFFENMODIF);
							if (wm != null) {
								shield.setWmAt(Util.parseInteger(wm.getAttributeValue(Xml.KEY_WAFFENMODIF_AT)));
								shield.setWmPa(Util.parseInteger(wm.getAttributeValue(Xml.KEY_WAFFENMODIF_PA)));
							}
							Element bf = waffe.getChild(Xml.KEY_BRUCHFAKTOR);
							if (bf != null) {
								shield.setBf(Util.parseInteger(bf.getAttributeValue(Xml.KEY_BRUCHFAKTOR_AKT)));
							}
							Element ini = waffe.getChild(Xml.KEY_INI_MOD);
							if (ini != null) {
								shield.setIni(Util.parseInteger(ini.getAttributeValue(Xml.KEY_INI_MOD_INI)));
							}
						}

					}

					if (itemSpecification instanceof Weapon) {
						Weapon weapon = (Weapon) itemSpecification;

						List<Element> waffen = element.getChildren(Xml.KEY_NAHKAMPWAFFE);

						for (Element waffe : waffen) {

							if (waffe.getAttribute(Xml.KEY_VARIANTE) != null) {

								int variante = Util.parseInteger(waffe.getAttributeValue(Xml.KEY_VARIANTE));
								if (variante == weapon.getVersion()) {
									Element trefferpunkte = waffe.getChild(Xml.KEY_TREFFERPUNKTE);
									if (trefferpunkte != null) {
										String tp = trefferpunkte.getAttributeValue(Xml.KEY_TREFFERPUNKTE_MUL) + "W"
												+ trefferpunkte.getAttributeValue(Xml.KEY_TREFFERPUNKTE_DICE) + "+"
												+ trefferpunkte.getAttributeValue(Xml.KEY_TREFFERPUNKTE_SUM);
										weapon.setTp(tp);
									}

									Element tpKK = waffe.getChild(Xml.KEY_TREFFERPUNKTE_KK);
									if (tpKK != null) {
										weapon.setTpKKMin(Util.parseInteger(tpKK
												.getAttributeValue(Xml.KEY_TREFFERPUNKTE_KK_MIN)));
										weapon.setTpKKStep(Util.parseInteger(tpKK
												.getAttributeValue(Xml.KEY_TREFFERPUNKTE_KK_STEP)));
									}
									Element wm = waffe.getChild(Xml.KEY_WAFFENMODIF);
									if (wm != null) {
										weapon.setWmAt(Util.parseInteger(wm.getAttributeValue(Xml.KEY_WAFFENMODIF_AT)));
										weapon.setWmPa(Util.parseInteger(wm.getAttributeValue(Xml.KEY_WAFFENMODIF_PA)));
									}
									Element bf = waffe.getChild(Xml.KEY_BRUCHFAKTOR);
									if (bf != null) {
										weapon.setBf(Util.parseInteger(bf.getAttributeValue(Xml.KEY_BRUCHFAKTOR_AKT)));
									}
									Element ini = waffe.getChild(Xml.KEY_INI_MOD);
									if (ini != null) {
										weapon.setIni(Util.parseInteger(ini.getAttributeValue(Xml.KEY_INI_MOD_INI)));
									}
								}
							}

						}

					}
				}

				hero.addItem(item);
			}
		}

	}

	protected static void fillSpells(Hero hero, Element heldElement) {
		List<Element> spellList = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_ZAUBERLISTE, Xml.KEY_ZAUBER);

		for (int i = 0; i < spellList.size(); i++) {
			Element element = (Element) spellList.get(i);
			Spell spell = new Spell(hero);

			spell.setProbePattern(element.getAttributeValue(Xml.KEY_PROBE));
			spell.setName(element.getAttributeValue(Xml.KEY_NAME));
			spell.setValue(Util.parseInteger(element.getAttributeValue(Xml.KEY_VALUE)));

			spell.setComments(element.getAttributeValue(Xml.KEY_ANMERKUNGEN));
			spell.setVariant(element.getAttributeValue(Xml.KEY_VARIANTE));

			spell.setUnused(Boolean.parseBoolean(element.getAttributeValue(Xml.KEY_UNUSED)));
			spell.setFavorite(Boolean.parseBoolean(element.getAttributeValue(Xml.KEY_FAVORITE)));

			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_HAUSZAUBER))
					&& Boolean.valueOf(element.getAttributeValue(Xml.KEY_HAUSZAUBER))) {
				spell.addFlag(com.dsatab.data.Spell.Flags.Hauszauber);
			}

			SpellInfo info = spell.getInfo();
			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_K)))
				info.setComplexity(element.getAttributeValue(Xml.KEY_K));

			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_ZAUBERKOMMENTAR)))
				info.setEffect(element.getAttributeValue(Xml.KEY_ZAUBERKOMMENTAR));

			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_KOSTEN)))
				info.setCosts(element.getAttributeValue(Xml.KEY_KOSTEN));

			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_REICHWEITE)))
				info.setRange(element.getAttributeValue(Xml.KEY_REICHWEITE));

			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_REPRESENTATION)))
				info.setRepresentation(element.getAttributeValue(Xml.KEY_REPRESENTATION));

			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_WIRKUNGSDAUER)))
				info.setEffectDuration(element.getAttributeValue(Xml.KEY_WIRKUNGSDAUER));

			if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_ZAUBERDAUER)))
				info.setCastDuration(element.getAttributeValue(Xml.KEY_ZAUBERDAUER));

			hero.addSpell(spell);
		}
	}

	protected static void fillTalents(Hero hero, Element heroElement) {

		List<Element> combatAttributesList = DomUtil.getChildrenByTagName(heroElement, Xml.KEY_KAMPF,
				Xml.KEY_KAMPFWERTE);

		List<Element> talentList = DomUtil.getChildrenByTagName(heroElement, Xml.KEY_TALENTLISTE, Xml.KEY_TALENT);
		Talent talent = null;
		boolean found = false;
		for (int i = 0; i < talentList.size(); i++) {
			Element element = (Element) talentList.get(i);

			talent = null;
			String talentName = element.getAttributeValue(Xml.KEY_NAME);
			int talentValue = Util.parseInt(element.getAttributeValue(Xml.KEY_VALUE));

			TalentGroupType talentGroupType = null;
			found = false;
			for (TalentGroupType type : TalentGroupType.values()) {
				if (type.contains(talentName)) {
					talentGroupType = type;
					found = true;

					CombatTalentType combatType = CombatTalentType.byName(talentName);
					if (combatType != null) {
						// add Peitsche as CombatTalent although
						// Heldensoftware doesn't treat is as one
						if (Talent.PEITSCHE.equals(talentName)) {
							CombatMeleeAttribute at = new CombatMeleeAttribute(hero);
							at.setName(CombatMeleeAttribute.ATTACKE);
							at.setValue(hero.getAttributeValue(AttributeType.at) + talentValue);

							talent = new CombatMeleeTalent(hero, at, null);
						} else if (combatType.isFk()) {
							talent = new CombatDistanceTalent(hero);
						} else {
							Element combatElement;
							for (Iterator<Element> iter = combatAttributesList.iterator(); iter.hasNext();) {
								combatElement = iter.next();
								String combatTalentName = combatElement.getAttributeValue(Xml.KEY_NAME);

								if (talentName.equals(combatTalentName)) {
									List<Element> nodes = combatElement.getChildren();

									CombatMeleeAttribute at = null, pa = null;
									for (Element node : nodes) {
										Element item = (Element) node;
										if (Xml.KEY_ATTACKE.equals(item.getName())) {
											at = new CombatMeleeAttribute(hero);
											at.setName(CombatMeleeAttribute.ATTACKE);
											at.setValue(Util.parseInteger(item.getAttributeValue(Xml.KEY_VALUE)));
										} else if (Xml.KEY_PARADE.equals(item.getName())) {
											pa = new CombatMeleeAttribute(hero);
											pa.setName(CombatMeleeAttribute.PARADE);
											pa.setValue(Util.parseInteger(item.getAttributeValue(Xml.KEY_VALUE)));
										}
									}
									talent = new CombatMeleeTalent(hero, at, pa);
									Debug.warning("Adding CombatTalent:" + combatTalentName);
									iter.remove();
									break;
								}
							}
						}
					}
					break;
				}
			}
			if (!found) {
				Debug.warning("No Talentgroup found for:" + talentName);
			}

			if (talent == null) {
				talent = new Talent(hero);
			}
			talent.setUnused(Boolean.parseBoolean(element.getAttributeValue(Xml.KEY_UNUSED)));
			talent.setFavorite(Boolean.parseBoolean(element.getAttributeValue(Xml.KEY_FAVORITE)));
			talent.setProbePattern(element.getAttributeValue(Xml.KEY_PROBE));
			talent.setProbeBe(element.getAttributeValue(Xml.KEY_BE));
			talent.setName(talentName);
			talent.setValue(talentValue);

			hero.addTalent(talentGroupType, talent);
		}

		// now add missing talents with combattalents
		for (Element combatElement : combatAttributesList) {

			String talentName = combatElement.getAttributeValue(Xml.KEY_NAME);
			Debug.warning("Adding missing CombatTalent:" + talentName);
			//
			List<Element> nodes = combatElement.getChildren();
			CombatMeleeAttribute at = null, pa = null;
			for (Element node : nodes) {
				Element item = (Element) node;
				if (Xml.KEY_ATTACKE.equals(item.getName())) {
					at = new CombatMeleeAttribute(hero);
					at.setName(CombatMeleeAttribute.ATTACKE);
					at.setValue(Util.parseInteger(item.getAttributeValue(Xml.KEY_VALUE)));
				} else if (Xml.KEY_PARADE.equals(item.getName())) {
					pa = new CombatMeleeAttribute(hero);
					pa.setName(CombatMeleeAttribute.PARADE);
					pa.setValue(Util.parseInteger(item.getAttributeValue(Xml.KEY_VALUE)));
				}
			}

			CombatMeleeTalent combatTalent = new CombatMeleeTalent(hero, at, pa);
			combatTalent.setName(talentName);

			hero.addTalent(null, combatTalent);
		}
	}

	public static void writeHero(Hero hero, Document dom, OutputStream out) throws IOException {
		if (hero == null)
			return;

		// Create an output formatter, and have it write the doc.
		XMLOutputter output = new XMLOutputter();
		output.output(dom, out);

	}

	/**
	 * 
	 */
	public static void onPreHeroSaved(Hero hero, Element heldElement) {
		Debug.verbose("Preparing hero to be saved. Populating data to XML.");

		Element equippmentNode = getEquippmentElement(heldElement);
		Element itemsNode = getItemElement(heldElement);

		Element ereignisse = heldElement.getChild(Xml.KEY_EREIGNISSE);

		if (hero.getPortraitUri() != null)
			heldElement.setAttribute(Xml.KEY_PORTRAIT_PATH, hero.getPortraitUri().toString());
		else
			heldElement.removeAttribute(Xml.KEY_PORTRAIT_PATH);

		List<Element> domAttributes = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_EIGENSCHAFTEN,
				Xml.KEY_EIGENSCHAFT);
		for (Element attribute : domAttributes) {
			hero.getAttribute(AttributeType.valueOf(attribute.getAttributeValue(Xml.KEY_NAME))).populateXml(attribute);

			Debug.verbose("Xml popuplate attr " + attribute);
		}

		List<Element> talentList = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_TALENTLISTE, Xml.KEY_TALENT);

		for (Element talent : talentList) {
			hero.getTalent(talent.getAttributeValue(Xml.KEY_NAME)).populateXml(talent);
			Debug.verbose("Xml popuplate talent " + talent);
		}

		List<Element> combatAttributesList = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_KAMPF,
				Xml.KEY_KAMPFWERTE);

		for (Element combatTalent : combatAttributesList) {
			hero.getCombatTalent(combatTalent.getAttributeValue(Xml.KEY_NAME)).populateXml(combatTalent);

			Debug.verbose("Xml popuplate combattalent " + combatTalent);
		}

		List<Element> spellList = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_ZAUBERLISTE, Xml.KEY_ZAUBER);

		for (Element spell : spellList) {
			hero.getSpell(spell.getAttributeValue(Xml.KEY_NAME)).populateXml(spell);

			Debug.verbose("Xml popuplate spell " + spell);
		}

		List<Element> sfs = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_SONDERFERTIGKEITEN,
				Xml.KEY_SONDERFERTIGKEIT);

		for (Element sf : sfs) {
			Art art = hero.getArt(Art.normalizeName(sf.getAttributeValue(Xml.KEY_NAME)));
			if (art != null) {
				art.populateXml(sf);

				Debug.verbose("Xml popuplate art " + sf);
			}
		}

		Element purseElement = heldElement.getChild(Xml.KEY_GELDBOERSE);
		if (purseElement != null) {
			hero.getPurse().populateXml(purseElement);
			Debug.verbose("Xml popuplate purse " + purseElement);
		}

		if (hero.getExperience() != null) {
			Element experienceElement = DomUtil.getChildByTagName(heldElement, Xml.KEY_BASIS, Xml.KEY_ABENTEUERPUNKTE);

			hero.getExperience().populateXml(experienceElement);

			Debug.verbose("Xml popuplate xp " + experienceElement);
		}

		if (hero.getFreeExperience() != null) {
			Element freeExperienceElement = DomUtil.getChildByTagName(heldElement, Xml.KEY_BASIS,
					Xml.KEY_FREIE_ABENTEUERPUNKTE);
			hero.getFreeExperience().populateXml(freeExperienceElement);

			Debug.verbose("Xml popuplate free xp " + freeExperienceElement);
		}

		List<Element> equippedElements = DomUtil.getChildrenByTagName(equippmentNode, Xml.KEY_HELDENAUSRUESTUNG);

		List<EquippedItem> allEuippedItems = new ArrayList<EquippedItem>(hero.getAllEquippedItems());
		List<Element> beidhaendigerKampElements = new ArrayList<Element>();
		List<Element> huntingWeaponElements = new ArrayList<Element>();

		for (Iterator<Element> iter = equippedElements.iterator(); iter.hasNext();) {
			Element itemElement = iter.next();
			if (itemElement.getAttributeValue(Xml.KEY_NAME).startsWith(Hero.PREFIX_BK)) {
				beidhaendigerKampElements.add(itemElement);
				continue;
			}

			if (itemElement.getAttributeValue(Xml.KEY_NAME).equals(Hero.JAGTWAFFE)) {
				huntingWeaponElements.add(itemElement);
				continue;
			}

			EquippedItem equippedItem = hero.getEquippedItem(Util.parseInt(itemElement.getAttributeValue(Xml.KEY_SET)),
					itemElement.getAttributeValue(Xml.KEY_NAME));
			if (equippedItem != null) {
				allEuippedItems.remove(equippedItem);
				equippedItem.populateXml(itemElement);
				Debug.verbose("Xml popuplate equippeditem " + itemElement);
			} else {
				Debug.verbose("Xml popuplate NO EQUIPPED ITEM found, removing it: " + itemElement);
				iter.remove();
			}
		}

		for (EquippedItem newItem : allEuippedItems) {
			Element element = new Element(Xml.KEY_HELDENAUSRUESTUNG);
			newItem.populateXml(element);

			equippmentNode.addContent(element);

		}

		// -- beidhändigerkampf
		List<BeidhaendigerKampf> allBhKamps = new ArrayList<BeidhaendigerKampf>(hero.getBeidhaendigerKampfs());
		for (Element bhElement : beidhaendigerKampElements) {
			boolean found = false;

			for (BeidhaendigerKampf bhKamp : hero.getBeidhaendigerKampfs()) {
				if (bhElement.getAttributeValue(Xml.KEY_NAME).equals(bhKamp.getName())) {
					allBhKamps.remove(bhKamp);
					bhKamp.populateXml(bhElement);
					found = true;
					break;
				}
			}

			if (!found) {
				equippmentNode.removeContent(bhElement);
			}
		}

		for (BeidhaendigerKampf bhKampf : allBhKamps) {
			Element bk = new Element(Xml.KEY_HELDENAUSRUESTUNG);
			bhKampf.populateXml(bk);

			equippmentNode.addContent(bk);
		}

		// hunting weapon
		for (int i = 0; i < Hero.MAXIMUM_SET_NUMBER; i++) {
			boolean found = false;
			for (Iterator<Element> iter = huntingWeaponElements.iterator(); iter.hasNext();) {
				Element element = iter.next();

				if (Util.parseInt(element.getAttributeValue(Xml.KEY_SET), -1) == i) {
					found = true;
					if (hero.getHuntingWeapons(i) != null)
						hero.getHuntingWeapons(i).populateXml(element);
					else
						iter.remove();
					break;
				}
			}

			if (!found) {
				Element element = new Element(Xml.KEY_HELDENAUSRUESTUNG);
				hero.getHuntingWeapons(i).populateXml(element);
				equippmentNode.addContent(element);
			}
		}

		// items

		List<Element> itemsElements = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_GEGENSTAENDE,
				Xml.KEY_GEGENSTAND);

		List<Item> allItems = new ArrayList<Item>(hero.getItems());

		for (Iterator<Element> iter = itemsElements.iterator(); iter.hasNext();) {
			Element itemelement = iter.next();
			Item item = hero.getItem(itemelement.getAttributeValue(Xml.KEY_NAME),
					itemelement.getAttributeValue(Xml.KEY_SLOT));

			if (item != null) {
				allItems.remove(item);
				item.populateXml(itemelement);
				Debug.verbose("Xml popuplate item " + itemelement);
			} else {
				Debug.verbose("Xml popuplate NO ITEM found remove it " + itemelement);
				iter.remove();
			}

		}

		for (Item newItem : allItems) {
			Element element = new Element(Xml.KEY_GEGENSTAND);
			newItem.populateXml(element);
			itemsNode.addContent(element);
		}

		// connections

		Element connectionsElement = getConnectionsElement(heldElement);
		List<Element> connectionElements = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_VERBINDUNGEN,
				Xml.KEY_VERBINDUNG);

		List<Connection> allConnections = new ArrayList<Connection>(hero.getConnections());

		for (Iterator<Element> iter = connectionElements.iterator(); iter.hasNext();) {
			Element element = iter.next();

			boolean found = false;
			for (Connection connection : hero.getConnections()) {
				if (connection.getName().equals(element.getAttributeValue(Xml.KEY_NAME))) {
					allConnections.remove(connection);
					connection.populateXml(element);
					found = true;
					break;
				}
			}

			if (!found) {
				iter.remove();
			}
		}

		for (Connection connection : allConnections) {
			Element element = new Element(Xml.KEY_VERBINDUNG);
			connection.populateXml(element);
			connectionsElement.addContent(element);
		}

		// events

		for (ChangeEvent changeEvent : hero.getChangeEvents()) {
			Element element = new Element(Xml.KEY_EREIGNIS);
			changeEvent.populateXml(element);
			ereignisse.addContent(element);
		}

	}
}
