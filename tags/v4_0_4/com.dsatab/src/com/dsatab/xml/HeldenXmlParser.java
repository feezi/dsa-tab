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
package com.dsatab.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
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

import com.bugsense.trace.BugSenseHandler;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.data.Advantage;
import com.dsatab.data.Art;
import com.dsatab.data.Attribute;
import com.dsatab.data.BaseCombatTalent;
import com.dsatab.data.ChangeEvent;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.CombatShieldTalent;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.Connection;
import com.dsatab.data.CustomAttribute;
import com.dsatab.data.Event;
import com.dsatab.data.Hero;
import com.dsatab.data.HeroBaseInfo;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.Markable;
import com.dsatab.data.Purse;
import com.dsatab.data.Purse.Currency;
import com.dsatab.data.Purse.PurseUnit;
import com.dsatab.data.SpecialFeature;
import com.dsatab.data.Spell;
import com.dsatab.data.SpellInfo;
import com.dsatab.data.Talent;
import com.dsatab.data.Talent.Flags;
import com.dsatab.data.TalentGroup;
import com.dsatab.data.enums.ArtType;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.EventCategory;
import com.dsatab.data.enums.Position;
import com.dsatab.data.enums.TalentGroupType;
import com.dsatab.data.items.Armor;
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

/**
 * Xml Reader and Writer for the Heldensoftware HeldenXML
 * 
 * @author Gandulf
 * 
 */
public class HeldenXmlParser {

	public static final String RUESTUNGSNAME = "ruestungsname";

	public static final String SCHILDNAME = "schildname";

	public static final String WAFFENNAME = "waffenname";

	public static final String ENCODING = "UTF-8";

	public static Document readDocument(InputStream in) throws JDOMException, IOException {
		SAXBuilder saxBuilder = new SAXBuilder();

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
			Event event = new Event();
			event.setCategory(EventCategory.Heldensoftware);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i <= 11; i++) {
				String commentLine = notiz.getAttributeValue(Xml.KEY_NOTIZ_PREFIX + i);
				if (!TextUtils.isEmpty(commentLine)) {
					sb.append(commentLine);
				}
				sb.append("\n");
			}
			event.setComment(sb.toString());
			hero.getHeroConfiguration().addEvent(event);
		}

		Collections.sort(hero.getHeroConfiguration().getEvents(), Event.COMPARATOR);

		// public ChangeEvent(Element element) {
		// time = new
		// Date(Util.parseLong(element.getAttributeValue(Xml.KEY_TIME)));
		// xps =
		// Util.parseInteger(element.getAttributeValue(Xml.KEY_ABENTEUERPUNKTE_UPPER));
		// oldValue = Util.parseInteger(element.getAttributeValue(Xml.KEY_ALT));
		// newValue = Util.parseInteger(element.getAttributeValue(Xml.KEY_NEU));
		// info = element.getAttributeValue(Xml.KEY_INFO);
		// object = element.getAttributeValue(Xml.KEY_OBJ);
		// version = element.getAttributeValue(Xml.KEY_VERSION);
		// text = element.getAttributeValue(Xml.KEY_TEXT);
		// }
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

				String itemName = element.getAttributeValue(WAFFENNAME);
				String itemSlot = element.getAttributeValue(Xml.KEY_SLOT);
				if (itemName == null)
					itemName = element.getAttributeValue(SCHILDNAME);
				if (itemName == null)
					itemName = element.getAttributeValue(RUESTUNGSNAME);

				Item item = hero.getItem(itemName, itemSlot);

				if (item == null) {
					BugSenseHandler.sendExceptionMessage(Debug.CATEGORY_DATA, itemName, new InconsistentDataException(
							"Unable to find an item with the name '" + itemName + "' in slot '" + itemSlot + "'."));
				}

				UsageType usageType = null;
				String name = element.getAttributeValue(Xml.KEY_NAME);
				int set = Util.parseInt(element.getAttributeValue(Xml.KEY_SET), 0);

				if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_VERWENDUNGSART))) {
					usageType = UsageType.valueOf(element.getAttributeValue(Xml.KEY_VERWENDUNGSART));
				}
				String bezeichner = element.getAttributeValue(Xml.KEY_BEZEICHNER);

				ItemSpecification itemSpecification = EquippedItem.getItemSpecification(hero, name, item, usageType,
						bezeichner);

				CombatTalent combatTalent = null;
				if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_TALENT))) {
					combatTalent = hero.getCombatTalent(element.getAttributeValue(Xml.KEY_TALENT));
				} else {
					combatTalent = EquippedItem.getCombatTalent(hero, usageType, set, name, itemSpecification);
				}

				EquippedItem equippedItem = new EquippedItem(hero, combatTalent, item, itemSpecification);

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

				if (!TextUtils.isEmpty(element.getAttributeValue(Xml.KEY_HAND))) {
					equippedItem.setHand(Hand.valueOf(element.getAttributeValue(Xml.KEY_HAND)));
				}

				equippedItem.setSet(set);
				equippedItem.setSlot(itemSlot);
				equippedItem.setName(element.getAttributeValue(Xml.KEY_NAME));
				equippedItem.setItemSpecification(itemSpecification);
				equippedItem.setSchildIndex(Util.parseInteger(element.getAttributeValue(Xml.KEY_SCHILD)));

				equippedItem.setUsageType(usageType);
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

						item1.setBeidhändigerKampf(true);
						item2.setBeidhändigerKampf(true);
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

	private static void writeAttribute(Hero hero, Attribute attr, Element element) {
		if (element != null) {
			if (attr.getValue() != null) {
				Integer newValue = attr.getValue();
				if (attr.getMod() != null) {
					newValue -= attr.getMod();
				}
				newValue -= attr.getBaseValue();
				Integer oldValue = Util.parseInteger(element.getAttributeValue(Xml.KEY_VALUE));
				if (!Util.equalsOrNull(newValue, oldValue)) {
					ChangeEvent changeEvent = new ChangeEvent(newValue, oldValue, attr.getType().name(),
							"Eigenschaft geändert");
					hero.addChangeEvent(changeEvent);
					element.setAttribute(Xml.KEY_VALUE, Integer.toString(newValue));
				}
			} else {
				element.removeAttribute(Xml.KEY_VALUE);
			}
		}
	}

	private static void writeCombatTalent(Hero hero, BaseCombatTalent talent, Element element) {

		if (talent instanceof CombatMeleeTalent) {
			CombatMeleeTalent meleeTalent = (CombatMeleeTalent) talent;
			if (Xml.KEY_KAMPFWERTE.equals(element.getName())) {
				List<Element> nodes = element.getChildren();

				for (Element node : nodes) {
					Element item = (Element) node;
					if (Xml.KEY_ATTACKE.equals(item.getName()))
						writeCombatMeleeAttribute(hero, meleeTalent.getAttack(), item);
					else if (Xml.KEY_PARADE.equals(item.getName()))
						writeCombatMeleeAttribute(hero, meleeTalent.getDefense(), item);
				}
			} else {
				writeTalent(hero, talent, element);
			}
		} else {
			writeTalent(hero, talent, element);
		}
	}

	/**
	 * @param attr
	 * @param item
	 */
	private static void writeCombatMeleeAttribute(Hero hero, CombatMeleeAttribute attr, Element element) {
		if (attr.hasValue()) {
			Integer newValue = attr.getValue();
			Integer oldValue = Util.parseInteger(element.getAttributeValue(Xml.KEY_VALUE));
			if (!Util.equalsOrNull(newValue, oldValue)) {
				ChangeEvent changeEvent = new ChangeEvent(newValue, oldValue, attr.getName(), "Kampfwerte geändert");
				hero.addChangeEvent(changeEvent);
				element.setAttribute(Xml.KEY_VALUE, newValue.toString());
			}
		} else {
			element.removeAttribute(Xml.KEY_VALUE);
		}
	}

	private static void writeSpell(Hero hero, Spell spell, Element element) {
		writeMarkable(spell, element);

		if (spell.getValue() != null) {
			Integer newValue = spell.getValue();
			Integer oldValue = Util.parseInteger(element.getAttributeValue(Xml.KEY_VALUE));
			if (!Util.equalsOrNull(newValue, oldValue)) {
				ChangeEvent changeEvent = new ChangeEvent(newValue, oldValue, spell.getName(), "Zauber geändert");
				hero.addChangeEvent(changeEvent);
				element.setAttribute(Xml.KEY_VALUE, Integer.toString(spell.getValue()));
			}
		} else {
			element.removeAttribute(Xml.KEY_VALUE);
		}

		element.setAttribute(Xml.KEY_ANMERKUNGEN, Xml.toString(spell.getComments()));
		element.setAttribute(Xml.KEY_VARIANTE, Xml.toString(spell.getVariant()));
	}

	private static void writeTalent(Hero hero, Talent talent, Element element) {
		writeMarkable(talent, element);

		if (talent instanceof CombatDistanceTalent) {
			CombatDistanceTalent distanceTalent = (CombatDistanceTalent) talent;

			if (distanceTalent.getValue() != null) {
				Integer newValue = distanceTalent.getValue() - distanceTalent.getBaseValue();
				Integer oldValue = Util.parseInteger(element.getAttributeValue(Xml.KEY_VALUE));
				if (!Util.equalsOrNull(newValue, oldValue)) {
					ChangeEvent changeEvent = new ChangeEvent(newValue, oldValue, talent.getName(), "Talent geändert");
					hero.addChangeEvent(changeEvent);
					element.setAttribute(Xml.KEY_VALUE, Integer.toString(newValue));
				}
			} else {
				element.removeAttribute(Xml.KEY_VALUE);
			}
		} else if (talent.getValue() != null) {
			Integer newValue = talent.getValue();
			Integer oldValue = Util.parseInteger(element.getAttributeValue(Xml.KEY_VALUE));
			if (!Util.equalsOrNull(newValue, oldValue)) {
				ChangeEvent changeEvent = new ChangeEvent(newValue, oldValue, talent.getName(), "Talent geändert");
				hero.addChangeEvent(changeEvent);
				element.setAttribute(Xml.KEY_VALUE, Integer.toString(newValue));
			}
		} else {
			element.removeAttribute(Xml.KEY_VALUE);
		}
	}

	private static void writePurse(Purse purse, Element element) {

		if (purse.getActiveCurrency() != null)
			element.setAttribute(Xml.KEY_ACTIVE, purse.getActiveCurrency().name());
		else
			element.removeAttribute(Xml.KEY_ACTIVE);

		for (Entry<Purse.PurseUnit, Integer> entry : purse.getCoins().entrySet()) {
			boolean found = false;
			for (Element p : DomUtil.getChildrenByTagName(element, Xml.KEY_MUENZE)) {
				if (entry.getKey().xmlName().equals(p.getAttributeValue(Xml.KEY_NAME))) {
					if (entry.getValue() != null)
						p.setAttribute(Xml.KEY_ANZAHL, entry.getValue().toString());
					else
						p.setAttribute(Xml.KEY_ANZAHL, "0");

					found = true;
					break;
				}
			}
			if (found == false) {
				Element m = new Element(Xml.KEY_MUENZE);
				m.setAttribute(Xml.KEY_WAEHRUNG, entry.getKey().currency().xmlName());
				m.setAttribute(Xml.KEY_NAME, entry.getKey().xmlName());
				if (entry.getValue() != null)
					m.setAttribute(Xml.KEY_ANZAHL, entry.getValue().toString());
				else
					m.setAttribute(Xml.KEY_ANZAHL, "0");

				element.addContent(m);
			}
		}
	}

	private static void writeMarkable(Markable markable, Element element) {
		if (markable.isFavorite())
			element.setAttribute(Xml.KEY_FAVORITE, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_FAVORITE);

		if (markable.isUnused())
			element.setAttribute(Xml.KEY_UNUSED, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_UNUSED);
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
			writeAttribute(hero, hero.getAttribute(AttributeType.valueOf(attribute.getAttributeValue(Xml.KEY_NAME))),
					attribute);
			Debug.verbose("Xml popuplate attr " + attribute);
		}

		List<Element> talentList = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_TALENTLISTE, Xml.KEY_TALENT);

		for (Element talentElement : talentList) {
			writeTalent(hero, hero.getTalent(talentElement.getAttributeValue(Xml.KEY_NAME)), talentElement);
			Debug.verbose("Xml popuplate talent " + talentElement);
		}

		List<Element> combatAttributesList = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_KAMPF,
				Xml.KEY_KAMPFWERTE);

		for (Element combatTalent : combatAttributesList) {
			writeCombatTalent(hero, hero.getCombatTalent(combatTalent.getAttributeValue(Xml.KEY_NAME)), combatTalent);
			Debug.verbose("Xml popuplate combattalent " + combatTalent);
		}

		List<Element> spellList = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_ZAUBERLISTE, Xml.KEY_ZAUBER);

		for (Element spell : spellList) {
			writeSpell(hero, hero.getSpell(spell.getAttributeValue(Xml.KEY_NAME)), spell);
			Debug.verbose("Xml popuplate spell " + spell);
		}

		List<Element> sfs = DomUtil.getChildrenByTagName(heldElement, Xml.KEY_SONDERFERTIGKEITEN,
				Xml.KEY_SONDERFERTIGKEIT);

		for (Element sf : sfs) {
			Art art = hero.getArt(Art.normalizeName(sf.getAttributeValue(Xml.KEY_NAME)));
			if (art != null) {
				writeArt(art, sf);
				Debug.verbose("Xml popuplate art " + sf);
			}
		}

		Element purseElement = heldElement.getChild(Xml.KEY_GELDBOERSE);
		if (purseElement != null) {
			writePurse(hero.getPurse(), purseElement);
			Debug.verbose("Xml popuplate purse " + purseElement);
		}

		if (hero.getExperience() != null) {
			Element experienceElement = DomUtil.getChildByTagName(heldElement, Xml.KEY_BASIS, Xml.KEY_ABENTEUERPUNKTE);

			if (hero.getExperience().getValue() != null) {
				experienceElement.setAttribute(Xml.KEY_VALUE, Util.toString(hero.getExperience().getValue()));
			} else
				experienceElement.removeAttribute(Xml.KEY_VALUE);

			Debug.verbose("Xml popuplate xp " + experienceElement);
		}

		if (hero.getFreeExperience() != null) {
			Element freeExperienceElement = DomUtil.getChildByTagName(heldElement, Xml.KEY_BASIS,
					Xml.KEY_FREIE_ABENTEUERPUNKTE);

			if (hero.getFreeExperience().getValue() != null) {
				freeExperienceElement.setAttribute(Xml.KEY_VALUE, Util.toString(hero.getFreeExperience().getValue()));
			} else
				freeExperienceElement.removeAttribute(Xml.KEY_VALUE);

			Debug.verbose("Xml popuplate free xp " + freeExperienceElement);
		}

		List<Element> equippedElements = DomUtil.getChildrenByTagName(equippmentNode, Xml.KEY_HELDENAUSRUESTUNG);

		List<EquippedItem> allEquippedItems = new ArrayList<EquippedItem>(hero.getAllEquippedItems());
		List<Element> huntingWeaponElements = new ArrayList<Element>();

		for (Iterator<Element> iter = equippedElements.iterator(); iter.hasNext();) {
			Element itemElement = iter.next();

			// remove all old once and add the new
			if (itemElement.getAttributeValue(Xml.KEY_NAME).startsWith(Hero.PREFIX_BK)) {
				iter.remove();
				continue;
			}

			if (itemElement.getAttributeValue(Xml.KEY_NAME).equals(Hero.JAGTWAFFE)) {
				huntingWeaponElements.add(itemElement);
				continue;
			}

			EquippedItem equippedItem = hero.getEquippedItem(Util.parseInt(itemElement.getAttributeValue(Xml.KEY_SET)),
					itemElement.getAttributeValue(Xml.KEY_NAME));
			if (equippedItem != null) {
				allEquippedItems.remove(equippedItem);
				writeEquippedItem(hero, equippedItem, itemElement);
				Debug.verbose("Xml popuplate equippeditem " + itemElement);
			} else {
				Debug.verbose("Xml popuplate NO EQUIPPED ITEM found, removing it: " + itemElement);
				iter.remove();
			}
		}

		for (EquippedItem newItem : allEquippedItems) {
			Element element = new Element(Xml.KEY_HELDENAUSRUESTUNG);
			writeEquippedItem(hero, newItem, element);

			equippmentNode.addContent(element);

		}

		// -- beidhändigerkampf
		for (EquippedItem equippedItem1 : hero.getAllEquippedItems()) {
			if (equippedItem1.isBeidhändigerKampf() && equippedItem1.getSecondaryItem() != null) {

				EquippedItem equippedItem2 = equippedItem1.getSecondaryItem();

				if (equippedItem2 != null && equippedItem2.isBeidhändigerKampf()
						&& equippedItem1.getNameId() < equippedItem2.getNameId()) {
					Element bk = new Element(Xml.KEY_HELDENAUSRUESTUNG);
					writeBeidhändigerKampf(equippedItem1, equippedItem2, bk);
					equippmentNode.addContent(bk);
				}
			}
		}

		// hunting weapon
		for (int i = 0; i < Hero.MAXIMUM_SET_NUMBER; i++) {
			boolean found = false;
			for (Iterator<Element> iter = huntingWeaponElements.iterator(); iter.hasNext();) {
				Element element = iter.next();

				if (Util.parseInt(element.getAttributeValue(Xml.KEY_SET), -1) == i) {
					found = true;
					if (hero.getHuntingWeapons(i) != null)
						writeHuntingWeapon(hero.getHuntingWeapons(i), element);
					else
						iter.remove();
					break;
				}
			}

			if (!found) {
				Element element = new Element(Xml.KEY_HELDENAUSRUESTUNG);
				writeHuntingWeapon(hero.getHuntingWeapons(i), element);

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
				writeItem(item, itemelement);
				Debug.verbose("Xml popuplate item " + itemelement);
			} else {
				Debug.verbose("Xml popuplate NO ITEM found remove it " + itemelement);
				iter.remove();
			}

		}

		for (Item newItem : allItems) {
			Element element = new Element(Xml.KEY_GEGENSTAND);

			writeItem(newItem, element);
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

					writeConnection(connection, element);

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
			writeConnection(connection, element);
			connectionsElement.addContent(element);
		}

		// events
		for (ChangeEvent changeEvent : hero.getChangeEvents()) {
			Element element = new Element(Xml.KEY_EREIGNIS);
			writeChangeEvent(changeEvent, element);
			ereignisse.addContent(element);
		}

		Element notiz = DomUtil.getChildByTagName(heldElement, Xml.KEY_BASIS, Xml.KEY_NOTIZ);
		for (Event event : hero.getEvents()) {
			writeEvent(event, notiz);
		}

	}

	/**
	 * @param art
	 * @param sf
	 */
	private static void writeArt(Art art, Element element) {
		writeMarkable(art, element);
	}

	private static void writeEvent(Event event, Element element) {
		if (event.getCategory() == EventCategory.Heldensoftware && Xml.KEY_NOTIZ.equals(element.getName())) {
			String[] events = event.getComment().split("\\r?\\n");

			for (int i = 0; i < events.length; i++) {
				element.setAttribute(Xml.KEY_NOTIZ_PREFIX + i, events[i]);
			}
			// fill up empty values if necessary
			for (int i = events.length; i <= 11; i++) {
				element.setAttribute(Xml.KEY_NOTIZ_PREFIX + i, "");
			}
		}
	}

	/**
	 * @param newItem
	 * @param element
	 */
	private static void writeEquippedItem(Hero hero, EquippedItem equippedItem, Element element) {
		if (equippedItem.getHand() != null)
			element.setAttribute(Xml.KEY_HAND, equippedItem.getHand().name());

		Item item = equippedItem.getItem();
		if (item != null) {
			ItemSpecification itemSpecification = equippedItem.getItemSpecification();
			if (itemSpecification instanceof Weapon || itemSpecification instanceof DistanceWeapon) {
				element.setAttribute(WAFFENNAME, item.getName());
				element.removeAttribute(SCHILDNAME);
				element.removeAttribute(RUESTUNGSNAME);
			} else if (itemSpecification instanceof Shield) {
				element.setAttribute(SCHILDNAME, item.getName());
				element.removeAttribute(WAFFENNAME);
				element.removeAttribute(RUESTUNGSNAME);
			} else if (itemSpecification instanceof Armor) {
				element.setAttribute(RUESTUNGSNAME, item.getName());
				element.removeAttribute(SCHILDNAME);
				element.removeAttribute(WAFFENNAME);
			}
		}

		element.setAttribute(Xml.KEY_SET, Util.toString(equippedItem.getSet()));
		if (equippedItem.getSlot() != null) {
			element.setAttribute(Xml.KEY_SLOT, equippedItem.getSlot());
		}

		if (equippedItem.getName() == null) {
			if (element.getAttribute(Xml.KEY_NAME) == null) {
				String namePrefix = null;

				if (item.hasSpecification(Weapon.class)) {
					namePrefix = EquippedItem.NAME_PREFIX_NK;
				}
				if (item.hasSpecification(DistanceWeapon.class)) {
					namePrefix = EquippedItem.NAME_PREFIX_FK;
				}
				if (item.hasSpecification(Shield.class)) {
					namePrefix = EquippedItem.NAME_PREFIX_SCHILD;
				}
				if (item.hasSpecification(Armor.class)) {
					namePrefix = EquippedItem.NAME_PREFIX_RUESTUNG;
				}

				// find first free slot
				int i = 1;
				while (hero.getEquippedItem(namePrefix + i) != null) {
					i++;
				}
				element.setAttribute(Xml.KEY_NAME, namePrefix + i);
			}
		} else {
			element.setAttribute(Xml.KEY_NAME, equippedItem.getName());
		}

		if (equippedItem.getTalent() != null)
			if (equippedItem.getTalent() instanceof CombatShieldTalent) {
				element.removeAttribute(Xml.KEY_TALENT);
			} else {
				element.setAttribute(Xml.KEY_TALENT, equippedItem.getTalent().getName());
			}
		else
			element.removeAttribute(Xml.KEY_TALENT);

		if (equippedItem.getUsageType() != null)
			element.setAttribute(Xml.KEY_VERWENDUNGSART, equippedItem.getUsageType().name());
		else
			element.removeAttribute(Xml.KEY_VERWENDUNGSART);

		if (TextUtils.isEmpty(equippedItem.getItemSpecification().getSpecificationLabel())) {
			if (equippedItem.getItemSpecification() instanceof Weapon)
				element.setAttribute(Xml.KEY_BEZEICHNER, "");
			else
				element.removeAttribute(Xml.KEY_BEZEICHNER);
		} else {
			element.setAttribute(Xml.KEY_BEZEICHNER, equippedItem.getItemSpecification().getSpecificationLabel());
		}

		if (equippedItem.getSchildIndex() != null) {
			element.setAttribute(Xml.KEY_SCHILD, Util.toString(equippedItem.getSchildIndex()));
		}

	}

	/**
	 * @param bhKampf
	 * @param bk
	 */
	private static void writeBeidhändigerKampf(EquippedItem item1, EquippedItem item2, Element element) {
		element.setAttribute(Xml.KEY_SET, Util.toString(item1.getSet()));

		if (item1.getNameId() < item2.getNameId())
			element.setAttribute(Xml.KEY_NAME, Hero.PREFIX_BK + item1.getNameId() + item2.getNameId());
		else
			element.setAttribute(Xml.KEY_NAME, Hero.PREFIX_BK + item2.getNameId() + item1.getNameId());

	}

	/**
	 * @param newItem
	 * @param element
	 */
	private static void writeItem(Item item, Element element) {
		element.setAttribute(Xml.KEY_NAME, item.getName());
		element.setAttribute(Xml.KEY_ANZAHL, Integer.toString(item.getCount()));
		element.setAttribute(Xml.KEY_SLOT, item.getSlot());

		if (item.getItemInfo() != null) {
			writeItemInfo(item.getItemInfo(), element);
		}

	}

	/**
	 * @param itemInfo
	 * @param element
	 */
	private static void writeItemInfo(ItemLocationInfo itemInfo, Element element) {
		if (itemInfo.getScreen() < Hero.MAXIMUM_SET_NUMBER) {
			element.setAttribute(Xml.KEY_SCREEN, Util.toString(itemInfo.getScreen()));
		} else {
			element.removeAttribute(Xml.KEY_SCREEN);
		}

		if (itemInfo.getCellNumber() >= 0) {
			element.setAttribute(Xml.KEY_CELL_NUMBER, Util.toString(itemInfo.getCellNumber()));
		} else {
			element.removeAttribute(Xml.KEY_CELL_NUMBER);
		}
	}

	/**
	 * @param changeEvent
	 * @param element
	 */
	private static void writeChangeEvent(ChangeEvent changeEvent, Element element) {
		element.setAttribute(Xml.KEY_TIME, Xml.toString(changeEvent.getTime().getTime()));
		element.setAttribute(Xml.KEY_ABENTEUERPUNKTE_UPPER, Xml.toString(changeEvent.getExperiencePoints()));
		element.setAttribute(Xml.KEY_ALT, Xml.toString(changeEvent.getOldValue()));
		element.setAttribute(Xml.KEY_NEU, Xml.toString(changeEvent.getNewValue()));
		if (!TextUtils.isEmpty(changeEvent.getInfo())) {
			element.setAttribute(Xml.KEY_INFO, Xml.toString(changeEvent.getInfo()));
		} else {
			element.removeAttribute(Xml.KEY_INFO);
		}
		element.setAttribute(Xml.KEY_OBJ, Xml.toString(changeEvent.getObject()));
		element.setAttribute(Xml.KEY_VERSION, Xml.toString(changeEvent.getVersion()));
		element.setAttribute(Xml.KEY_TEXT, Xml.toString(changeEvent.getText()));

	}

	/**
	 * @param connection
	 * @param element
	 */
	private static void writeConnection(Connection connection, Element element) {
		element.setAttribute(Xml.KEY_DESCRIPTION, connection.getDescription());
		element.setAttribute(Xml.KEY_SO, connection.getSozialStatus());
		element.setAttribute(Xml.KEY_NAME, connection.getName());
	}

	/**
	 * @param huntingWeapons
	 * @param element
	 */
	private static void writeHuntingWeapon(HuntingWeapon huntingWeapon, Element element) {
		if (huntingWeapon.getNumber() != null)
			element.setAttribute(Xml.KEY_NUMMER, Util.toString(huntingWeapon.getNumber()));

		element.setAttribute(Xml.KEY_NAME, Hero.JAGTWAFFE);

		if (huntingWeapon.getSet() != null)
			element.setAttribute(Xml.KEY_SET, Util.toString(huntingWeapon.getSet()));

	}
}
