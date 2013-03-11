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
package com.dsatab.data.enums;

/**
 * @author Ganymede
 * 
 */
public enum TalentType {

	Anderthalbhänder(-2), Armbrust(-5, true), Bastardstäbe(-2, false), Belagerungswaffen(null, true), Blasrohr(-5, true), Bogen(
			-3, true), Diskus(-3, true), Dolche(-1), Fechtwaffen(-1), Hiebwaffen(-4), Infanteriewaffen(-3), Kettenstäbe(
			-1), Kettenwaffen(-3), Lanzenreiten(null, true), Peitsche(-1), Raufen(0), Ringen(0), Schleuder(-2, true), Schwerter(
			-2), Speere(-3), Stäbe(-2), Säbel(-2), Wurfbeile(-2, true), Wurfmesser(-3, true), Wurfspeere(-2, true), Zweihandflegel(
			-3), Zweihandhiebwaffen(-3), Zweihandschwerter(-2, false, "Zweihandschwerter/-säbel"),
	//
	Athletik, Klettern, Körperbeherrschung, Schleichen, Schwimmen, Selbstbeherrschung, SichVerstecken("Sich verstecken"), Singen, Sinnenschärfe, Tanzen, Zechen, Akrobatik, Fliegen, Gaukeleien, Reiten, Skifahren, StimmenImitieren(
			"Stimmen imitieren"), Taschendiebstahl,
	//
	Menschenkenntnis, Überreden, Betören, Etikette, Gassenwissen, Lehren, SichVerkleiden("Sich verkleiden"), Überzeugen, Galanterie, Schauspielerei, SchriftlicherAusdruck(
			"Schriftlicher Ausdruck"),
	//
	Fährtensuchen, Orientierung, Wildnisleben, FallenStellen("Fallen stellen"), FesselnEntfesseln("Fesseln/Entfesseln"), FischenAngeln(
			"Fischen/Angeln"), Wettervorhersage, Seefischerei,
	//
	GötterUndKulte("Götter und Kulte"), Rechnen, SagenUndLegenden("Sagen und Legenden"), BrettKartenspiel(
			"Brett-/Kartenspiel"), Geografie, Geschichtswissen, Gesteinskunde, Heraldik, Kriegskunst, Kryptographie, Magiekunde, Mechanik, Pflanzenkunde, Philosophie, Rechtskunde, Schätzen, Sprachenkunde, Staatskunst, Sternkunde, Tierkunde, Anatomie, Baukunst, Hüttenkunde, Schiffbau,
	//
	SprachenKennen("Sprachen kennen"), LesenSchreiben("Lesen/Schreiben"),

	SprachenKennenAlaani("Sprachen kennen Alaani"), SprachenKennenAltImperialAureliani(
			"Sprachen kennen Alt-Imperial/Aureliani"), SprachenKennenAltesKemi("Sprachen kennen Altes Kemi"), SprachenKennenAngram(
			"Sprachen kennen Angram"), SprachenKennenAsdharia("Sprachen kennen Asdharia"), SprachenKennenAtak(
			"Sprachen kennen Atak"), SprachenKennenBosparano("Sprachen kennen Bosparano"), SprachenKennenDrachisch(
			"Sprachen kennen Drachisch"), SprachenKennenFerkina("Sprachen kennen Ferkina"), SprachenKennenGarethi(
			"Sprachen kennen Garethi"), SprachenKennenGoblinisch("Sprachen kennen Goblinisch"), SprachenKennenGrolmisch(
			"Sprachen kennen Grolmisch"), SprachenKennenHjaldingsch("Sprachen kennen Hjaldingsch"), SprachenKennenIsdira(
			"Sprachen kennen Isdira"), SprachenKennenKoboldisch("Sprachen kennen Koboldisch"), SprachenKennenMahrisch(
			"Sprachen kennen Mahrisch"), SprachenKennenMohisch("Sprachen kennen Mohisch"), SprachenKennenMolochisch(
			"Sprachen kennen Molochisch"), SprachenKennenNeckergesang("Sprachen kennen Neckergesang"), SprachenKennenNujuka(
			"Sprachen kennen Nujuka"), SprachenKennenOloarkh("Sprachen kennen Oloarkh"), SprachenKennenOloghaijan(
			"Sprachen kennen Ologhaijan"), SprachenKennenRissoal("Sprachen kennen Rissoal"), SprachenKennenRogolan(
			"Sprachen kennen Rogolan"), SprachenKennenRssahh("Sprachen kennen Rssahh"), SprachenKennenTrollisch(
			"Sprachen kennen Trollisch"), SprachenKennenUrtulamidya("Sprachen kennen Urtulamidya"), SprachenKennenTulamidya(
			"Sprachen kennen Tulamidya"), SprachenKennenWudu("Sprachen kennen Wudu"), SprachenKennenZLit(
			"Sprachen kennen Z'Lit"), SprachenKennenZelemja("Sprachen kennen Zelemja"), SprachenKennenZhayad(
			"Sprachen kennen Zhayad"), SprachenKennenZhulchammaqra("Sprachen kennen Zhulchammaqra"), SprachenKennenZyklopäisch(
			"Sprachen kennen Zyklopäisch"),
	//
	LesenSchreibenAltImperialeZeichen("Lesen/Schreiben (Alt-)Imperiale Zeichen"), LesenSchreibenAltesAlaani(
			"Lesen/Schreiben Altes Alaani"), LesenSchreibenAltesAmulashtra("Lesen/Schreiben Altes Amulashtra"), LesenSchreibenKuslikerZichen(
			"Lesen/Schreiben Kusliker Zeichen"), LesenSchreibenAltesKemi("Lesen/Schreiben Altes Kemi"), LesenSchreibenAngram(
			"Lesen/Schreiben Angram"), LesenSchreibenChrmk("Lesen/Schreiben Chrmk"), LesenSchreibenChuchas(
			"Lesen/Schreiben Chuchas"), LesenSchreibenGimarilGlyphen("Lesen/Schreiben Gimaril-Glyphen"), LesenSchreibenGjalskisch(
			"Lesen/Schreiben Gjalskisch"), LesenSchreibenHjaldingscheRunen("Lesen/Schreiben Hjaldingsche Runen"), LesenSchreibenIsdiraAsdharia(
			"Lesen/Schreiben Isdira/Asdharia"), LesenSchreibenMahrischeGlyphen("Lesen/Schreiben Mahrische Glyphen"), LesenSchreibenRogolan(
			"Lesen/Schreiben Rogolan"), LesenSchreibenTrollischeRaumbilderschrift(
			"Lesen/Schreiben Trollische Raumbilderschrift"), LesenSchreibenUrtulamidya("Lesen/Schreiben Urtulamidya"), LesenSchreibenWudu(
			"Lesen/Schreiben Wudu"), LesenSchreibenZhayad("Lesen/Schreiben Zhayad"), LesenSchreibenTulamidya(
			"Lesen/Schreiben Tulamidya"), LesenSchreibenNanduria("Lesen/Schreiben Nanduria"),
	//
	Ritualkenntnis, RitualkenntnisAlchimist("Ritualkenntnis: Alchimist"), RitualkenntnisDerwisch(
			"Ritualkenntnis: Derwisch"), RitualkenntnisDruide("Ritualkenntnis: Druide"), RitualkenntnisDurroDun(
			"Ritualkenntnis: Durro-Dûn"), RitualkenntnisGeode("Ritualkenntnis: Geode"), RitualkenntnisGildenmagie(
			"Ritualkenntnis: Gildenmagie"), RitualkenntnisHexe("Ritualkenntnis: Hexe"), RitualkenntnisKristallomantie(
			"Ritualkenntnis: Kristallomantie"), RitualkenntnisRunenzauberei("Ritualkenntnis: Runenzauberei"), RitualkenntnisScharlatan(
			"Ritualkenntnis: Scharlatan"), RitualkenntnisZaubertänzer("Ritualkenntnis: Zaubertänzer"), RitualkenntnisZaubertänzerHazaqi(
			"Ritualkenntnis: Zaubertänzer (Hazaqi)"), RitualkenntnisZaubertänzerMajuna(
			"Ritualkenntnis: Zaubertänzer (Majuna)"), RitualkenntnisZaubertänzernovadischeSharisad(
			"Ritualkenntnis: Zaubertänzer (novadische Sharisad)"), RitualkenntnisZaubertänzerTulamidischeSharisad(
			"Ritualkenntnis: Zaubertänzer (tulamidische Sharisad)"), RitualkenntnisZibilja("Ritualkenntnis: Zibilja"),
	//
	Liturgiekenntnis, LiturgiekenntnisAngrosch("Liturgiekenntnis (Angrosch)"), LiturgiekenntnisAves(
			"Liturgiekenntnis (Aves)"), LiturgiekenntnisBoron("Liturgiekenntnis (Boron)"), LiturgiekenntnisEfferd(
			"Liturgiekenntnis (Efferd)"), LiturgiekenntnisFirun("Liturgiekenntnis (Firun)"), LiturgiekenntnisGravesh(
			"Liturgiekenntnis (Gravesh)"), LiturgiekenntnisHRanga("Liturgiekenntnis (H'Ranga)"), LiturgiekenntnisHSzint(
			"Liturgiekenntnis (H'Szint)"), LiturgiekenntnisHesinde("Liturgiekenntnis (Hesinde)"), LiturgiekenntnisIfirn(
			"Liturgiekenntnis (Ifirn)"), LiturgiekenntnisIngerimm("Liturgiekenntnis (Ingerimm)"), LiturgiekenntnisKamaluq(
			"Liturgiekenntnis (Kamaluq)"), LiturgiekenntnisKor("Liturgiekenntnis (Kor)"), LiturgiekenntnisNandus(
			"Liturgiekenntnis (Nandus)"), LiturgiekenntnisPeraine("Liturgiekenntnis (Peraine)"), LiturgiekenntnisPhex(
			"Liturgiekenntnis (Phex)"), LiturgiekenntnisPraios("Liturgiekenntnis (Praios)"), LiturgiekenntnisRahja(
			"Liturgiekenntnis (Rahja)"), LiturgiekenntnisRondra("Liturgiekenntnis (Rondra)"), LiturgiekenntnisSwafnir(
			"Liturgiekenntnis (Swafnir)"), LiturgiekenntnisTairach("Liturgiekenntnis (Tairach)"), LiturgiekenntnisTravia(
			"Liturgiekenntnis (Travia)"), LiturgiekenntnisTsa("Liturgiekenntnis (Tsa)"), LiturgiekenntnisZsahh(
			"Liturgiekenntnis (Zsahh)"),

	//
	Gefahreninstinkt, GeisterRufen("Geister rufen"), GeisterBannen("Geister bannen"), GeisterBinden("Geister binden"), GeisterAufnehmen(
			"Geister aufnehmen"),
	//
	HeilkundeWunden("Heilkunde: Wunden"), Holzbearbeitung, Kochen, Lederarbeiten, MalenZeichnen("Malen/Zeichnen"), Schneidern, Abrichten, BooteFahren(
			"Boote fahren"), EisseglerFahren("Eissegler fahren"), FahrzeugLenken("Fahrzeug lenken"), Falschspiel, Grobschmied, HeilkundeGift(
			"Heilkunde: Gift"), HeilkundeKrankheiten("Heilkunde: Krankheiten"), HundeschlittenFahren(
			"Hundeschlitten fahren,"), Kartographie, Musizieren, SchlösserKnacken("Schlösser knacken"), StoffeFärben(
			"Stoffe Färben"), Tätowieren, Töpfern, Webkunst, Ackerbau, Alchimie, Bergbau, Bogenbau, Brauer, Drucker, Feinmechanik, Feuersteinbearbeitung, Fleischer, GerberKürschner(
			"Gerber/Kürschner"), Glaskunst, Handel, Hauswirtschaft, HeilkundeSeele("Heilkunde: Seele"), Instrumentenbauer, Kapellmeister, Kartografie, Kristallzucht, Maurer, Metallguss, SchnapsBrennen(
			"Schnaps brennen"), Seefahrt, Seiler, Steinmetz, SteinschneiderJuwelier("Steinschneider/Juwelier"), Stellmacher, Steuermann, Viehzucht, Winzer, Zimmermann,
	//
	PirschAnsitzJagd("Pirsch- und Ansitzjagd"), NahrungSammeln("Nahrung sammeln"), Kräutersuchen("Kräutersuche"), Wache(
			"Wache halten");

	private Integer be;

	private boolean fk;

	private String xmlName;

	private TalentType() {
	}

	private TalentType(String xmlName) {
		this(null, false, xmlName);
	}

	private TalentType(Integer be) {
		this(be, false, null);
	}

	private TalentType(Integer be, boolean fk) {
		this(be, fk, null);
	}

	private TalentType(Integer be, boolean fk, String name) {
		this.be = be;
		this.fk = fk;
		this.xmlName = name;
	}

	public String xmlName() {
		if (xmlName != null)
			return xmlName;
		else
			return name();
	}

	public Integer getBe() {
		return be;
	}

	public boolean isFk() {
		return fk;
	}

	public static TalentType byXmlName(String code) {

		if (code == null)
			return null;

		for (TalentType attr : TalentType.values()) {
			if (attr.xmlName().equals(code)) {
				return attr;
			}
		}
		throw new IllegalArgumentException(code + " cannot be transformed into en TalentType enum");
	}

}