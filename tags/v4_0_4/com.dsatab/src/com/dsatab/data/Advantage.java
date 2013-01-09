package com.dsatab.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.text.TextUtils;

import com.dsatab.util.Util;

public class Advantage {

	private String name;
	private String comment;
	private List<String> values;

	public static final String AUSDAUERND = "Ausdauernd";
	public static final String NATUERLICHER_RUESTUNGSSCHUTZ = "Natürlicher Rüstungsschutz";
	public static final String FESTE_MATRIX = "Feste Matrix";
	public static final String WILDE_MAGIE = "Wilde Magie";
	public static final String TOLLPATSCH = "Tollpatsch";
	public static final String MAGIEDILLETANT = "Magiedilletant";
	public static final String ENTFERNUNGSSINN = "Entfernungssinn";
	public static final String MONDSUECHTIG = "Mondsüchtig";
	public static final String BEIDHAENDIG = "Beidhändig";
	public static final String GUTES_GEDAECHTNIS = "Gutes Gedächtnis";
	public static final String EITELKEIT = "Eitelkeit";

	public static final String VOLLZAUBERER = "Vollzauberer";
	public static final String UNBEWUSSTER_VIERTELZAUBERER = "Unbewusster Viertelzauberer";
	public static final String VIERTELZAUBERER = "Viertelzauberer";
	public static final String HALBZAUBERER = "Halbzauberer";

	public static final String MEISTERHANDWERK = "Meisterhandwerk";

	private static final String[] VORTEILE = { "Amtsadel", "Adlige Abstammung", "Adliges Erbe", "Affinität zu ",
			"Akademische Ausbildung (Gelehrter)", "Akademische Ausbildung (Krieger)",
			"Akademische Ausbildung (Magier)", "Altersresistenz", "Astrale Regeneration", "Astralmacht", AUSDAUERND,
			"Ausdauernder Zauberer", "Ausrüstungsvorteil", "Balance", "Begabung für [Merkmal]",
			"Begabung für [Ritual]", "Begabung für [Talent]", "Begabung für [Talentgruppe]", "Begabung für [Zauber]",
			BEIDHAENDIG, "Beseelte Knochenkeule", "Besonderer Besitz", "Breitgefächerte Bildung", "Dämmerungssicht",
			"Eidetisches Gedächtnis", "Eigeboren", "Eisenaffine Aura", "Eisern", "Empathie", ENTFERNUNGSSINN,
			"Ererbte Knochenkeule", "Feenfreund", FESTE_MATRIX, "Früher Vertrauter", "Flink", "Gebildet",
			"Gefahreninstinkt", "Geräuschhexerei", "Geweiht [Angrosch]", "Geweiht [Gravesh]",
			"Geweiht [nicht-alveranische Gottheit]", "Geweiht [H'Ranga]", "Geweiht [zwölfgöttliche Kirche]", "Glück",
			"Glück im Spiel", "Gutaussehend", "Guter Ruf", GUTES_GEDAECHTNIS, HALBZAUBERER, "Herausragende Balance",
			"Herausragende Eigenschaft", "Herausragender Sechster Sinn", "Herausragender Sinn",
			"Herausragendes Aussehen", "Hitzeresistenz", "Hohe Lebenskraft", "Hohe Magieresistenz",
			"Immunität gegen Gift", "Immunität gegen Krankheiten", "Innerer Kompass", "Kälteresistenz", "Kampfrausch",
			"Koboldfreund", "Kräfteschub", "Talentschub", "Linkshänder", "Machtvoller Vertrauter", MAGIEDILLETANT,
			"Magiegespür", MEISTERHANDWERK, "Nachtsicht", NATUERLICHER_RUESTUNGSSCHUTZ, "Natürliche Waffen",
			"Niedrige Schlechte Eigenschaft", "Ortskenntnis", "Prophezeien", "Resistenz gegen Gift",
			"Resistenz gegen Krankheiten", "Richtungssinn", "Schlangenmensch", "Schnelle Heilung", "Schutzgeist",
			"Schwer zu verzaubern", "Soziale Anpassungsfähigkeit", "Sprachgefühl", "Tierfreund", "Tierempathie (alle)",
			"Tierempathie (speziell)", "Titularadel", "Übernatürliche Begabung", "Unbeschwertes Zaubern",
			"Verbindungen", "Verhüllte Aura", "Veteran", VIERTELZAUBERER, UNBEWUSSTER_VIERTELZAUBERER, VOLLZAUBERER,
			"Vom Schicksal begünstigt", "Wesen der Nacht", "Wolfskind", "Wohlklang", "Zäher Hund", "Zauberhaar",
			"Zeitgefühl", "Zusätzliche Gliedmaßen", "Zweistimmiger Gesang", "Zwergennase" };

	public static final String[] NACHTEILE = { "Aberglaube", "Agrimothwahn", "Albino", "Angst vor Insekten",
			"Angst vor Menschenmassen", "Angst vor Spinnen", "Angst vor Reptilien", "Angst vor Pelztieren",
			"Angst vor Wasser", "Angst vor Feuer", "Angst vor Nagetieren", "Animalische Magie", "Arkanophobie",
			"Arroganz", "Artefaktgebunden", "Astraler Block", "Autoritätsgläubig", "Behäbig", "Blutdurst",
			"Blutrausch", "Brünstigkeit", "Charyptophilie", "Dunkelangst", "Elfische Weltsicht", "Einarmig",
			"Einäugig", "Einbeinig", "Einbildungen", "Eingeschränkte Elementarnähe", "Eingeschränkter Sinn",
			"Einhändig", EITELKEIT, "Farbenblind", "Feind", "Feste Gewohnheit", "Festgefügtes Denken", "Fettleibig",
			"Fluch der Finsternis", "Geiz", "Gerechtigkeitswahn", "Gesucht", "Glasknochen", "Goldgier", "Grausamkeit",
			"Größenwahn", "Heimwehkrank", "Herrschsucht", "Hitzeempfindlichkeit", "Höhenangst", "Impulsiv",
			"Jagdfieber", "Jähzorn", "Kältestarre", "Kälteempfindlichkeit", "Kein Vertrauter", "Kleinwüchsig",
			"Körpergebundene Kraft", "Konstruktionswahn", "Krankhafte Nekromantie", "Krankhafte Reinlichkeit",
			"Krankheitsanfällig", "Kristallgebunden", "Kurzatmig", "Lahm", "Landangst", "Lästige Mindergeister",
			"Lichtempfindlich", "Lichtscheu", "Madas Fluch", "Medium", "Meeresangst", "Miserable Eigenschaft",
			MONDSUECHTIG, "Moralkodex [Angrosch-Kult]", "Moralkodex [Badalikaner]", "Moralkodex [Boron-Kirche]",
			"Moralkodex [Bund des wahren Glaubens]", "Moralkodex [Dreischwesternorden]", "Moralkodex [Efferd-Kirche]",
			"Moralkodex [Firun-Kirche]", "Moralkodex [Hesinde-Kirche]", "Moralkodex [H'Szint-Kult]",
			"Moralkodex [Ifirn-Kirche]", "Moralkodex [Ingerimm-Kirche]", "Moralkodex [Kor-Kirche]",
			"Moralkodex [Nandus-Kirche]", "Moralkodex [Peraine-Kirche]", "Moralkodex [Phex-Kirche]",
			"Moralkodex [Praios-Kirche]", "Moralkodex [Rahja-Kirche]", "Moralkodex [Rondra-Kirche]",
			"Moralkodex [Swafnir-Kult]", "Moralkodex [Travia-Kirche]", "Moralkodex [Heshinja]",
			"Moralkodex [Tsa-Kirche]", "Moralkodex [Zsahh-Kult]", "Moralkodex [DDZ]", "Morbidität", "Nachtblind",
			"Nagrachwahn", "Nahrungsrestriktion", "Neid", "Neugier", "Niedrige Astralkraft", "Niedrige Lebenskraft",
			"Niedrige Magieresistenz", "Pechmagnet", "Platzangst", "Prinzipientreue", "Rachsucht", "Randgruppe",
			"Raubtiergeruch", "Raumangst", "Rückschlag", "Ruhelosigkeit", "Schlaflosigkeit", "Schlafstörungen",
			"Schlafwandler", "Schlechte Regeneration", "Schlechter Ruf", "Schneller alternd", "Schöpferwahn",
			"Schulden", "Schwache Ausstrahlung", "Schwacher Astralkörper", "Schwanzlos", "Seffer Manich",
			"Sensibler Geruchssinn", "Selbstgespräche", "Sippenlosigkeit", "Sonnensucht", "Speisegebote",
			"Sprachfehler", "Spielsucht", "Spruchhemmung", "Stigma", "Streitsucht", "Stubenhocker", "Sucht",
			"Thesisgebunden", TOLLPATSCH, "Totenangst", "Trägheit", "Treulosigkeit", "Übler Geruch",
			"Unangenehme Stimme", "Unansehnlich", "Unfähigkeit für [Merkmal]", "Unfähigkeit für [Talent]",
			"Unfähigkeit für [Talentgruppe]", "Unfrei", "Ungebildet", "Unstet",
			"Unverträglichkeit mit verarbeitetem Metall", "Vergesslichkeit", "Verpflichtungen", "Verschwendungssucht",
			"Verwöhnt", "Vorurteile", "Vorurteile (stark)", "Wahnvorstellungen", "Wahrer Name", "Weltfremd",
			"Widerwärtiges Aussehen", WILDE_MAGIE, "Zielschwierigkeiten", "Zögerlicher Zauberer", "Zwergenwuchs",
			"Hoher Amtsadel", "Comes", "Erstgeborener Comes", "Sacerdos" };

	static {
		Arrays.sort(NACHTEILE);
		Arrays.sort(VORTEILE);
	}

	public static final String BEGABUNG_FUER_PREFIX = "Begabung für";

	public static final String BEGABUNG_FUER_TALENT = "Begabung für [Talent]";
	public static final String BEGABUNG_FUER_TALENTGRUPPE = "Begabung für [Talentgruppe]";
	public static final String BEGABUNG_FUER_ZAUBER = "Begabung für [Zauber]";
	public static final String BEGABUNG_FUER_RITUAL = "Begabung für [Zauber]";
	public static final String TALENTSCHUB = "Talentschub";

	public static final String UEBERNATUERLICHE_BEGABUNG = "Übernatürliche Begabung";

	public static boolean isVorteil(String name) {
		return Arrays.binarySearch(VORTEILE, name) >= 0;
	}

	public static boolean isNachteil(String name) {
		return Arrays.binarySearch(NACHTEILE, name) >= 0;
	}

	public Advantage() {
		this.values = new ArrayList<String>();
	}

	public Advantage(String name) {
		this();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getValue() {
		if (values.size() == 1)
			return Util.parseInteger(values.get(0));
		else
			return null;
	}

	public String getValueAsString() {
		if (values.size() == 1)
			return values.get(0);
		else
			return null;
	}

	@Override
	public String toString() {
		if (values.isEmpty()) {
			return getName();
		} else if (values.size() == 1) {
			return getName() + " " + values.get(0);
		} else {
			return getName() + " " + values.toString();
		}
	}

	public void addValue(String value) {
		if (!TextUtils.isEmpty(value))
			this.values.add(value);
	}
}