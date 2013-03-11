package com.dsatab.data;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import com.dsatab.data.Talent.Flags;
import com.dsatab.data.enums.TalentGroupType;
import com.dsatab.data.enums.TalentType;

public class TalentGroup {

	public static TalentType[] NAHKAMPF_TALENTS = { TalentType.Dolche, TalentType.Hiebwaffen, TalentType.Raufen,
			TalentType.Ringen, TalentType.Säbel, TalentType.Anderthalbhänder, TalentType.Fechtwaffen,
			TalentType.Infanteriewaffen, TalentType.Kettenstäbe, TalentType.Kettenwaffen, TalentType.Lanzenreiten,
			TalentType.Peitsche, TalentType.Schwerter, TalentType.Speere, TalentType.Stäbe, TalentType.Zweihandflegel,
			TalentType.Zweihandhiebwaffen, TalentType.Zweihandschwerter, TalentType.Bastardstäbe };

	public static TalentType[] FERNKAMPF_TALENTS = { TalentType.Wurfmesser, TalentType.Armbrust, TalentType.Blasrohr,
			TalentType.Bogen, TalentType.Diskus, TalentType.Schleuder, TalentType.Wurfbeile, TalentType.Wurfspeere,
			TalentType.Belagerungswaffen };

	public static TalentType[] KÖRPER_TALENTS = { TalentType.Athletik, TalentType.Klettern,
			TalentType.Körperbeherrschung, TalentType.Schleichen, TalentType.Schwimmen, TalentType.Selbstbeherrschung,
			TalentType.SichVerstecken, TalentType.Singen, TalentType.Sinnenschärfe, TalentType.Tanzen,
			TalentType.Zechen, TalentType.Akrobatik, TalentType.Fliegen, TalentType.Gaukeleien, TalentType.Reiten,
			TalentType.Skifahren, TalentType.StimmenImitieren, TalentType.Taschendiebstahl };

	public static TalentType[] GESELLSCHAFT_TALENTS = { TalentType.Menschenkenntnis, TalentType.Überreden,
			TalentType.Betören, TalentType.Etikette, TalentType.Gassenwissen, TalentType.Lehren,
			TalentType.SichVerkleiden, TalentType.Überzeugen, TalentType.Galanterie, TalentType.Schauspielerei,
			TalentType.SchriftlicherAusdruck };

	public static TalentType[] NATUR_TALENTS = { TalentType.Fährtensuchen, TalentType.Orientierung,
			TalentType.Wildnisleben, TalentType.FallenStellen, TalentType.FesselnEntfesseln, TalentType.FischenAngeln,
			TalentType.Wettervorhersage, TalentType.Seefischerei };

	public static TalentType[] WISSEN_TALENTS = { TalentType.GötterUndKulte, TalentType.Rechnen,
			TalentType.SagenUndLegenden, TalentType.BrettKartenspiel, TalentType.Geografie,
			TalentType.Geschichtswissen, TalentType.Gesteinskunde, TalentType.Heraldik, TalentType.Kriegskunst,
			TalentType.Kryptographie, TalentType.Magiekunde, TalentType.Mechanik, TalentType.Pflanzenkunde,
			TalentType.Philosophie, TalentType.Rechtskunde, TalentType.Schätzen, TalentType.Sprachenkunde,
			TalentType.Staatskunst, TalentType.Sternkunde, TalentType.Tierkunde, TalentType.Anatomie,
			TalentType.Baukunst, TalentType.Hüttenkunde, TalentType.Schiffbau };

	public static TalentType[] SPACHEN_TALENTS = { TalentType.SprachenKennen, TalentType.LesenSchreiben,
			TalentType.SprachenKennenAlaani, TalentType.SprachenKennenAltImperialAureliani,
			TalentType.SprachenKennenAltesKemi, TalentType.SprachenKennenAngram, TalentType.SprachenKennenAsdharia,
			TalentType.SprachenKennenAtak, TalentType.SprachenKennenBosparano, TalentType.SprachenKennenDrachisch,
			TalentType.SprachenKennenFerkina, TalentType.SprachenKennenGarethi, TalentType.SprachenKennenGoblinisch,
			TalentType.SprachenKennenGrolmisch, TalentType.SprachenKennenHjaldingsch, TalentType.SprachenKennenIsdira,
			TalentType.SprachenKennenKoboldisch, TalentType.SprachenKennenMahrisch, TalentType.SprachenKennenMohisch,
			TalentType.SprachenKennenMolochisch, TalentType.SprachenKennenNeckergesang,
			TalentType.SprachenKennenNujuka, TalentType.SprachenKennenOloarkh, TalentType.SprachenKennenOloghaijan,
			TalentType.SprachenKennenRissoal, TalentType.SprachenKennenRogolan, TalentType.SprachenKennenRssahh,
			TalentType.SprachenKennenTrollisch, TalentType.SprachenKennenUrtulamidya, TalentType.SprachenKennenWudu,
			TalentType.SprachenKennenZLit, TalentType.SprachenKennenZelemja, TalentType.SprachenKennenZhayad,
			TalentType.SprachenKennenZhulchammaqra, TalentType.SprachenKennenZyklopäisch,
			TalentType.SprachenKennenTulamidya, TalentType.SprachenKennenThorwalsch,
			TalentType.LesenSchreibenAltImperialeZeichen, TalentType.LesenSchreibenAltesAlaani,
			TalentType.LesenSchreibenAltesAmulashtra, TalentType.LesenSchreibenAltesKemi,
			TalentType.LesenSchreibenAngram, TalentType.LesenSchreibenChrmk, TalentType.LesenSchreibenChuchas,
			TalentType.LesenSchreibenGimarilGlyphen, TalentType.LesenSchreibenGjalskisch,
			TalentType.LesenSchreibenHjaldingscheRunen, TalentType.LesenSchreibenIsdiraAsdharia,
			TalentType.LesenSchreibenMahrischeGlyphen, TalentType.LesenSchreibenRogolan,
			TalentType.LesenSchreibenTrollischeRaumbilderschrift, TalentType.LesenSchreibenUrtulamidya,
			TalentType.LesenSchreibenWudu, TalentType.LesenSchreibenZhayad, TalentType.LesenSchreibenKuslikerZichen,
			TalentType.LesenSchreibenTulamidya, TalentType.LesenSchreibenNanduria };

	public static TalentType[] GABEN_TALENTS = { TalentType.Ritualkenntnis, TalentType.Gefahreninstinkt,
			TalentType.Liturgiekenntnis, TalentType.GeisterAufnehmen, TalentType.GeisterBannen,
			TalentType.GeisterBinden, TalentType.GeisterRufen,

			TalentType.RitualkenntnisAlchimist, TalentType.RitualkenntnisDerwisch, TalentType.RitualkenntnisDruide,
			TalentType.RitualkenntnisDurroDun, TalentType.RitualkenntnisGeode, TalentType.RitualkenntnisGildenmagie,
			TalentType.RitualkenntnisHexe, TalentType.RitualkenntnisKristallomantie,
			TalentType.RitualkenntnisRunenzauberei, TalentType.RitualkenntnisScharlatan,
			TalentType.RitualkenntnisZaubertänzer, TalentType.RitualkenntnisZaubertänzerHazaqi,
			TalentType.RitualkenntnisZaubertänzerMajuna, TalentType.RitualkenntnisZaubertänzernovadischeSharisad,
			TalentType.RitualkenntnisZaubertänzerTulamidischeSharisad, TalentType.RitualkenntnisZibilja,
			TalentType.LiturgiekenntnisAngrosch, TalentType.LiturgiekenntnisAves, TalentType.LiturgiekenntnisBoron,
			TalentType.LiturgiekenntnisEfferd, TalentType.LiturgiekenntnisFirun, TalentType.LiturgiekenntnisGravesh,
			TalentType.LiturgiekenntnisHRanga, TalentType.LiturgiekenntnisHSzint, TalentType.LiturgiekenntnisHesinde,
			TalentType.LiturgiekenntnisIfirn, TalentType.LiturgiekenntnisIngerimm, TalentType.LiturgiekenntnisKamaluq,
			TalentType.LiturgiekenntnisKor, TalentType.LiturgiekenntnisNandus, TalentType.LiturgiekenntnisPeraine,
			TalentType.LiturgiekenntnisPhex, TalentType.LiturgiekenntnisPraios, TalentType.LiturgiekenntnisRahja,
			TalentType.LiturgiekenntnisRondra, TalentType.LiturgiekenntnisSwafnir, TalentType.LiturgiekenntnisTairach,
			TalentType.LiturgiekenntnisTravia, TalentType.LiturgiekenntnisTsa, TalentType.LiturgiekenntnisZsahh };

	public static TalentType[] HANDWERK_TALENTS = { TalentType.HeilkundeWunden, TalentType.Holzbearbeitung,
			TalentType.Kochen, TalentType.Lederarbeiten, TalentType.MalenZeichnen, TalentType.Schneidern,
			TalentType.Abrichten, TalentType.BooteFahren, TalentType.EisseglerFahren, TalentType.FahrzeugLenken,
			TalentType.Falschspiel, TalentType.Grobschmied, TalentType.HeilkundeGift, TalentType.HeilkundeKrankheiten,
			TalentType.HundeschlittenFahren, TalentType.Kartographie, TalentType.Musizieren,
			TalentType.SchlösserKnacken, TalentType.StoffeFärben, TalentType.Tätowieren, TalentType.Töpfern,
			TalentType.Webkunst, TalentType.Ackerbau, TalentType.Alchimie, TalentType.Bergbau, TalentType.Bogenbau,
			TalentType.Brauer, TalentType.Drucker, TalentType.Feinmechanik, TalentType.Feuersteinbearbeitung,
			TalentType.Fleischer, TalentType.GerberKürschner, TalentType.Glaskunst, TalentType.Handel,
			TalentType.Hauswirtschaft, TalentType.HeilkundeSeele, TalentType.Instrumentenbauer,
			TalentType.Kapellmeister, TalentType.Kartografie, TalentType.Kristallzucht, TalentType.Maurer,
			TalentType.Metallguss, TalentType.SchnapsBrennen, TalentType.Seefahrt, TalentType.Seiler,
			TalentType.Steinmetz, TalentType.SteinschneiderJuwelier, TalentType.Stellmacher, TalentType.Steuermann,
			TalentType.Viehzucht, TalentType.Winzer, TalentType.Zimmermann };

	public static TalentType[] META_TALENTS = { TalentType.PirschAnsitzJagd, TalentType.NahrungSammeln,
			TalentType.Kräutersuchen, TalentType.Wache };

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
