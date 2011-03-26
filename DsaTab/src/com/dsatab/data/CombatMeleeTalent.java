package com.dsatab.data;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.xml.Xml;

public class CombatMeleeTalent implements CombatTalent {

	private Element element;

	private CombatMeleeAttribute pa;

	private CombatMeleeAttribute at;

	private CombatTalentType type;

	public CombatMeleeTalent(Hero hero, Element element) {
		this.element = element;
		this.type = CombatTalentType.byName(getName());

		NodeList n = element.getChildNodes();
		for (int i = 0; i < n.getLength(); i++) {
			Node node = (Node) n.item(i);

			if (node instanceof Element) {
				Element item = (Element) node;
				if (item.getNodeName().equals(Xml.KEY_ATTACKE))
					at = new CombatMeleeAttribute(hero, this, item);
				else if (item.getNodeName().equals(Xml.KEY_PARADE))
					pa = new CombatMeleeAttribute(hero, this, item);
			}
		}
	}

	public String getName() {
		return element.getAttribute(Xml.KEY_NAME);
	}

	public CombatTalentType getType() {
		return type;
	}

	public CombatMeleeAttribute getAttack() {
		return at;
	}

	public CombatMeleeAttribute getDefense() {
		return pa;
	}

	public Position getPosition(int w20) {

		switch (getType()) {

		case Dolche:
		case Fechtwaffen:
			return Position.messer_dolch_stich[w20];
		case Hiebwaffen:
		case Kettenwaffen:
		case Kettenstäbe:
			return Position.hieb_ketten[w20];
		case Schwerter:
		case Säbel:
			return Position.schwert_saebel[w20];
		case Speere:
			return Position.stangen_zweih_stich[w20];
		case Stäbe:
		case Zweihandflegel:
		case Anderthalbhänder:
		case Infanteriewaffen:
		case Zweihandhiebwaffen:
		case Zweihandschwerter:
			return Position.stangen_zweih_hieb[w20];
		case Raufen:
		case Ringen:
			return Position.box_rauf_hruru[w20];
		case Peitsche:
			return Position.fern_wurf[w20];
		default:
			return Position.fern_wurf[w20];
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
