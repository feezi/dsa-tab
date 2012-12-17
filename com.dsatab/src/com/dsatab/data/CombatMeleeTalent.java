package com.dsatab.data;

import java.util.List;

import org.jdom2.Element;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

public class CombatMeleeTalent extends BaseCombatTalent {

	private CombatMeleeAttribute pa;

	private CombatMeleeAttribute at;

	private CombatTalentType type;

	public CombatMeleeTalent(Hero hero, CombatMeleeAttribute at, CombatMeleeAttribute pa) {
		super(hero);
		if (at != null)
			at.setCombatMeleeTalent(this);
		if (pa != null)
			pa.setCombatMeleeTalent(this);
		this.at = at;
		this.pa = pa;
	}

	public CombatTalentType getCombatTalentType() {
		return type;
	}

	protected void setCombatTalentType(CombatTalentType type) {

		this.type = type;

		if (type == null)
			Debug.verbose("No type found for " + getName());

		this.probeInfo.applyBePattern(type.getBe());

		// we have to set the talent again to refresh some values
		if (at != null)
			at.setCombatMeleeTalent(this);
		if (pa != null)
			pa.setCombatMeleeTalent(this);
	}

	public CombatMeleeAttribute getAttack() {
		return at;
	}

	public CombatMeleeAttribute getDefense() {
		return pa;
	}

	public void setName(String name) {
		super.setName(name);
		setCombatTalentType(CombatTalentType.byName(getName()));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Talent#populateXml(org.jdom2.Element)
	 */
	@Override
	public void populateXml(Element element) {
		if (Xml.KEY_KAMPFWERTE.equals(element.getName())) {
			List<Element> nodes = element.getChildren();

			for (Element node : nodes) {
				Element item = (Element) node;
				if (Xml.KEY_ATTACKE.equals(item.getName()))
					at.populateXml(item);
				else if (Xml.KEY_PARADE.equals(item.getName()))
					at.populateXml(item);
			}
		} else {
			super.populateXml(element);
		}
	}
}
