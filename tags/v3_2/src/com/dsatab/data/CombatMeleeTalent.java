package com.dsatab.data;

import java.util.List;

import org.jdom.Element;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.xml.Xml;

public class CombatMeleeTalent extends BaseCombatTalent {

	private CombatMeleeAttribute pa;

	private CombatMeleeAttribute at;

	private CombatTalentType type;

	public CombatMeleeTalent(Hero hero, Element element, Element combatElement) {
		super(hero, element, combatElement);
		this.type = CombatTalentType.byName(getName());

		@SuppressWarnings("unchecked")
		List<Element> nodes = combatElement.getChildren();

		for (Element node : nodes) {
			Element item = (Element) node;
			if (Xml.KEY_ATTACKE.equals(item.getName()))
				at = new CombatMeleeAttribute(hero, this, item);
			else if (Xml.KEY_PARADE.equals(item.getName()))
				pa = new CombatMeleeAttribute(hero, this, item);
		}

		probeInfo.applyBePattern(type.getBe());
	}

	public CombatTalentType getCombatTalentType() {
		return type;
	}

	public CombatMeleeAttribute getAttack() {
		return at;
	}

	public CombatMeleeAttribute getDefense() {
		return pa;
	}

	public Position getPosition(int w20) {

		if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_MORE_TARGET_ZONES,
				false)) {

			switch (type) {

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
		} else {
			return Position.official[w20];
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
