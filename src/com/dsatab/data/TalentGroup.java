package com.dsatab.data;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import com.dsatab.data.MetaTalent.MetaTalentType;
import com.dsatab.data.Talent.Flags;

public class TalentGroup {

	public static String[] NAHKAMPF_TALENTS = { "Dolche", "Hiebwaffen", "Raufen", "Ringen", "Säbel",
			"Anderthalbhänder", "Fechtwaffen", "Infanteriewaffen", "Kettenstäbe", "Kettenwaffen", "Lanzenreiten",
			Talent.PEITSCHE, "Schwerter", "Speere", "Stäbe", "Zweihandflegel", "Zweihandhiebwaffen",
			"Zweihandschwerter/-säbel", "Bastardstäbe" };

	public static String[] FERNKAMPF_TALENTS = { "Wurfmesser", "Armbrust", "Blasrohr", "Bogen", "Diskus", "Schleuder",
			"Wurfbeile", "Wurfspeere", "Belagerungswaffen" };

	public static String[] KÖRPER_TALENTS = { "Athletik", "Klettern", "Körperbeherrschung", "Schleichen", "Schwimmen",
			Talent.SELBSTBEHERRSCHUNG, Talent.SICH_VERSTECKEN, "Singen", Talent.SINNENSCHÄRFE, "Tanzen", "Zechen",
			"Akrobatik", "Fliegen", "Gaukeleien", "Reiten", "Skifahren", "Stimmen imitieren", "Taschendiebstahl" };

	public static String[] GESELLSCHAFT_TALENTS = { "Menschenkenntnis", "Überreden", "Betören", "Etikette",
			"Gassenwissen", "Lehren", "Sich verkleiden", "Überzeugen", "Galanterie", "Schauspielerei",
			"Schriftlicher Ausdruck" };

	public static String[] NATUR_TALENTS = { "Fährtensuchen", "Orientierung", "Wildnisleben", "Fallen stellen",
			"Fesseln/Entfesseln", "Fischen/Angeln", "Wettervorhersage", "Seefischerei" };

	public static String[] WISSEN_TALENTS = { "Götter und Kulte", "Rechnen", "Sagen und Legenden",
			"Brett-/Kartenspiel", "Geografie", "Geschichtswissen", "Gesteinskunde", "Heraldik", "Kriegskunst",
			"Kryptographie", "Magiekunde", "Mechanik", "Pflanzenkunde", "Philosophie", "Rechtskunde", "Schätzen",
			"Sprachenkunde", "Staatskunst", "Sternkunde", "Tierkunde", "Anatomie", "Baukunst", "Hüttenkunde",
			"Schiffbau" };

	public static String[] SPACHEN_TALENTS = { "Sprachen kennen", "Lesen/Schreiben" };

	public static String[] GABEN_TALENTS = { "Ritualkenntnis", Talent.GEFAHRENINSTINKT,
			Talent.LITURGIE_KENNTNIS_PREFIX, Talent.GEISTER_ANRUFEN, Talent.GEISTER_BANNEN, Talent.GEISTER_BINDEN,
			Talent.GEISTER_RUFEN };

	public static String[] HANDWERK_TALENTS = { "Heilkunde: Wunden", "Holzbearbeitung", "Kochen", "Lederarbeiten",
			"Malen/Zeichnen", "Schneidern", "Abrichten", "Boote fahren", "Eissegler fahren", "Fahrzeug lenken",
			"Falschspiel", "Grobschmied", "Heilkunde: Gift", "Heilkunde: Krankheiten", "Hundeschlitten fahren",
			"Kartographie", "Musizieren", "Schlösser knacken", "Stoffe Färben", "Tätowieren", "Töpfern", "Webkunst",
			"Ackerbau", "Alchimie", "Bergbau", "Bogenbau", "Brauer", "Drucker", "Feinmechanik",
			"Feuersteinbearbeitung", "Fleischer", "Gerber/Kürschner", "Glaskunst", "Handel", "Hauswirtschaft",
			"Heilkunde: Seele", "Instrumentenbauer", "Kapellmeister", "Kartografie", "Kristallzucht", "Maurer",
			"Metallguss", "Schnaps brennen", "Seefahrt", "Seiler", "Steinmetz", "Steinschneider/Juwelier",
			"Stellmacher", "Steuermann", "Viehzucht", "Winzer", "Zimmermann" };

	public static String[] META_TALENTS = { MetaTalentType.PirschAnsitzJagd.getName(),
			MetaTalentType.NahrungSammeln.getName(), MetaTalentType.Kräutersuchen.getName(),
			MetaTalentType.Wache.getName() };

	public enum TalentGroupType {
		Nahkampf(NAHKAMPF_TALENTS), Fernkampf(FERNKAMPF_TALENTS), Körperlich(KÖRPER_TALENTS), Gesellschaft(
				GESELLSCHAFT_TALENTS), Natur(NATUR_TALENTS), Wissen(WISSEN_TALENTS), Handwerk(HANDWERK_TALENTS), Sprachen(
				SPACHEN_TALENTS), Gaben(GABEN_TALENTS), Meta(META_TALENTS);

		private String[] talents;

		private TalentGroupType(String[] talents) {
			Arrays.sort(talents);
			this.talents = talents;
		}

		public boolean contains(String talentName) {

			if (this == TalentGroupType.Sprachen || this == TalentGroupType.Gaben) {
				for (String s : talents) {
					if (talentName.startsWith(s))
						return true;
				}
				return false;
			} else {
				return Arrays.binarySearch(talents, talentName) >= 0;
			}
		}
	}

	private List<Talent> talents = new LinkedList<Talent>();

	private TalentGroupType type;

	private EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

	public TalentGroup(TalentGroupType name) {
		this.type = name;
	}

	public void setTalents(List<Talent> talents) {
		this.talents = talents;
	}

	public boolean hasFlag(Flags flag) {
		return flags.contains(flag);
	}

	public void addFlag(Flags flag) {
		flags.add(flag);
	}

	public List<Talent> getTalents() {
		return talents;
	}

	public TalentGroupType getType() {
		return type;
	}

}