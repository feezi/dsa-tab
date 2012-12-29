/**
 *  This file is part of DsaTabTest.
 *
 *  DsaTabTest is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTabTest is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTabTest.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.test;

import java.io.InputStream;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import android.test.InstrumentationTestCase;

import com.dsatab.data.Advantage;
import com.dsatab.data.Hero;
import com.dsatab.data.SpecialFeature;
import com.dsatab.data.Talent;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Hand;
import com.dsatab.data.items.Weapon;
import com.dsatab.util.Util;
import com.dsatab.xml.HeldenXmlParser;

/**
 * @author Ganymede
 * 
 */
public class XmlParserTest extends InstrumentationTestCase {

	XPathFactory factory;

	/**
	 * 
	 */
	public XmlParserTest() {
		factory = XPathFactory.instance();
	}

	protected Integer attrIntegerValue(String xpath, Document document) {
		return Util.parseInteger(attrValue(xpath, document));
	}

	protected String attrValue(String xpath, Document document) {
		XPathExpression<Attribute> xp = factory.compile(xpath, Filters.attribute());
		return xp.evaluateFirst(document).getValue();

	}

	public void testHeroLoading() throws Exception {

		final int TEST_GS = 5;
		final int TEST_LP = 28;
		final int TEST_AU = 38;
		final int TEST_MR = 4;
		final int TEST_AT = 8;
		final int TEST_PA = 8;
		final int TEST_FK = 8;
		final int TEST_INI = 11;
		final int TEST_BE = 2;
		final int TEST_RS = 3;
		final int TEST_AW = 7;

		// in = Global.gContext.openFileInput("testxml.xml");
		InputStream in = getInstrumentation().getContext().getAssets().open("minarax_menschenfreund.xml");

		Document dom = HeldenXmlParser.readDocument(in);
		in.close();

		in = getInstrumentation().getContext().getAssets().open("minarax_menschenfreund.xml");
		Hero hero = HeldenXmlParser.readHero("minarax_menschenfreund.xml", in);
		in.close();

		assertNotNull(hero);
		assertEquals(attrValue("/helden/held/@name", dom), hero.getName());
		assertEquals(attrValue("/helden/held/basis/ausbildungen/ausbildung/@string", dom), hero.getBaseInfo()
				.getAusbildung());

		for (AttributeType type : AttributeType.EIGENSCHAFTEN) {
			assertEquals(
					(int) attrIntegerValue(
							"/helden/held/eigenschaften/eigenschaft[@name='" + type.name() + "']/@value", dom)
							+ attrIntegerValue("/helden/held/eigenschaften/eigenschaft[@name='" + type.name()
									+ "']/@mod", dom), (int) hero.getAttributeValue(type));
		}

		assertEquals(TEST_LP, (int) hero.getAttributeValue(AttributeType.Lebensenergie_Aktuell));
		assertEquals(TEST_LP, (int) hero.getAttributeValue(AttributeType.Lebensenergie));
		assertEquals(TEST_AU, (int) hero.getAttributeValue(AttributeType.Ausdauer_Aktuell));
		assertEquals(TEST_AU, (int) hero.getAttributeValue(AttributeType.Ausdauer));
		assertNull(hero.getAttributeValue(AttributeType.Astralenergie_Aktuell));
		assertNull(hero.getAttributeValue(AttributeType.Astralenergie));
		assertNull(hero.getAttributeValue(AttributeType.Karmaenergie_Aktuell));
		assertNull(hero.getAttributeValue(AttributeType.Karmaenergie));

		assertEquals(TEST_MR, (int) hero.getAttributeValue(AttributeType.Magieresistenz));

		assertEquals(
				(int) attrIntegerValue(
						"/helden/held/eigenschaften/eigenschaft[@name='" + AttributeType.Sozialstatus.name()
								+ "']/@value", dom)
						+ attrIntegerValue("/helden/held/eigenschaften/eigenschaft[@name='"
								+ AttributeType.Sozialstatus.name() + "']/@mod", dom),
				(int) hero.getAttributeValue(AttributeType.Sozialstatus));

		assertEquals(TEST_AT, (int) hero.getAttributeValue(AttributeType.at));
		assertEquals(TEST_PA, (int) hero.getAttributeValue(AttributeType.pa));
		assertEquals(TEST_FK, (int) hero.getAttributeValue(AttributeType.fk));
		assertEquals(TEST_INI, (int) hero.getAttributeValue(AttributeType.ini));

		assertEquals(attrIntegerValue("/helden/held/basis/abenteuerpunkte/@value", dom), hero.getExperience()
				.getValue());
		assertEquals(attrIntegerValue("/helden/held/basis/freieabenteuerpunkte/@value", dom), hero.getFreeExperience()
				.getValue());

		assertEquals(TEST_BE, (int) hero.getAttributeValue(AttributeType.Behinderung));

		assertEquals(TEST_RS, (int) hero.getArmorRs());

		assertEquals(TEST_GS, (int) hero.getModifiedValue(AttributeType.Geschwindigkeit, true, true));

		assertTrue(hero.hasFeature(SpecialFeature.D�MMERNGSSICHT));
		assertTrue(hero.hasFeature(Advantage.GUTES_GEDAECHTNIS));
		assertTrue(hero.hasFeature(SpecialFeature.ZWERGENWUCHS));

		assertTrue(hero.hasFeature(Advantage.EITELKEIT));
		assertEquals(7, (int) hero.getDisadvantage(Advantage.EITELKEIT).getValue());

		assertTrue(hero.hasFeature(SpecialFeature.RUESTUNGSGEWOEHNUNG_1));
		assertEquals("F�nflagenharnisch", hero.getSpecialFeature(SpecialFeature.RUESTUNGSGEWOEHNUNG_1).getParameter1());

		assertTrue(hero.hasFeature(SpecialFeature.KULTURKUNDE));

		assertEquals(TEST_AW, (int) hero.getModifiedValue(AttributeType.Ausweichen, true, true));

		assertEquals(21, (int) hero.getCombatTalent(Talent.ARMBRUST).getValue());

		assertEquals(attrIntegerValue("/helden/held/talentliste/talent[@name='Dolche']/@value", dom), hero
				.getCombatTalent(Talent.DOLCHE).getValue());

		assertEquals(attrIntegerValue("/helden/held/talentliste/talent[@name='Akrobatik']/@value", dom), hero
				.getTalent(Talent.AKROBATIK).getValue());

		assertEquals(attrIntegerValue("/helden/held/kampf/kampfwerte[@name='Dolche']/attacke/@value", dom), hero
				.getCombatTalent(Talent.DOLCHE).getAttack().getValue());
		assertEquals(attrIntegerValue("/helden/held/kampf/kampfwerte[@name='Dolche']/parade/@value", dom), hero
				.getCombatTalent(Talent.DOLCHE).getDefense().getValue());

		EquippedItem langSchwert = hero.getEquippedItem("nkwaffe1");
		assertEquals(11,
				langSchwert.getCombatProbeAttacke().getValue() + hero.getModifier(langSchwert.getCombatProbeAttacke()));
		assertEquals(16,
				langSchwert.getCombatProbeDefense().getValue() + hero.getModifier(langSchwert.getCombatProbeDefense()));
		assertEquals(CombatTalentType.Schwerter, langSchwert.getCombatProbeAttacke().getCombatTalent()
				.getCombatTalentType());
		assertEquals(CombatTalentType.Schwerter, langSchwert.getCombatProbeDefense().getCombatTalent()
				.getCombatTalentType());
		assertTrue(langSchwert.getItemSpecification() instanceof Weapon);
		assertEquals(hero.getCombatTalent(CombatTalentType.Schwerter.name()), langSchwert.getTalent());
		assertEquals(Hand.rechts, langSchwert.getHand());

		EquippedItem drachenzahn = hero.getEquippedItem("nkwaffe2");
		assertEquals(-1,
				drachenzahn.getCombatProbeAttacke().getValue() + hero.getModifier(drachenzahn.getCombatProbeAttacke()));
		assertEquals(-1,
				drachenzahn.getCombatProbeDefense().getValue() + hero.getModifier(drachenzahn.getCombatProbeDefense()));
		assertEquals(CombatTalentType.Dolche, drachenzahn.getCombatProbeAttacke().getCombatTalent()
				.getCombatTalentType());
		assertEquals(CombatTalentType.Dolche, drachenzahn.getCombatProbeDefense().getCombatTalent()
				.getCombatTalentType());
		assertTrue(drachenzahn.getItemSpecification() instanceof Weapon);
		assertEquals(hero.getCombatTalent(CombatTalentType.Dolche.name()), drachenzahn.getTalent());
		assertEquals(Hand.links, drachenzahn.getHand());

	}
}
