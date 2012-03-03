/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.xml;

/**
 * @author Seraphim
 * 
 */
public interface Xml {
	public static final String KEY_VALUE = "value";
	public static final String KEY_NAME = "name";
	public static final String KEY_MOD = "mod";
	public static final String KEY_GROSSE_MEDIDATION = "grossemeditation";

	public static final String KEY_TEXT = "text";
	public static final String KEY_TIME = "time";
	public static final String KEY_VERSION = "version";

	public static final String KEY_KOMMENTAR = "kommentar";
	public static final String KEY_DSATAB_VALUE = "dsatab_value";
	public static final String KEY_GELDBOERSE = "geldboerse";
	public static final String KEY_HELDENAUSRUESTUNG = "heldenausruestung";
	public static final String KEY_SET = "set";
	public static final String KEY_ANZAHL = "anzahl";
	public static final String KEY_SLOT = "slot";
	public static final String KEY_GEGENSTAND = "gegenstand";
	public static final String KEY_HELD = "held";
	public static final String KEY_EIGENSCHAFT = "eigenschaft";
	public static final String KEY_ABENTEUERPUNKTE = "abenteuerpunkte";
	public static final String KEY_FREIE_ABENTEUERPUNKTE = "freieabenteuerpunkte";

	public static final String KEY_SONDERFERTIGKEITEN = "sf";
	public static final String KEY_SONDERFERTIGKEIT = "sonderfertigkeit";

	public static final String KEY_VORTEILE = "vt";
	public static final String KEY_VORTEIL = "vorteil";
	public static final String KEY_NACHTEIL = "nachteil";
	public static final String KEY_EREIGNIS = "ereignis";
	public static final String KEY_EREIGNISSE = "ereignisse";
	public static final String KEY_ABENTEUERPUNKTE_UPPER = "Abenteuerpunkte";
	public static final String KEY_OBJ = "obj";
	public static final String KEY_TALENT = "talent";

	public static final String KEY_KAMPF = "kampf";
	public static final String KEY_KAMPFWERTE = "kampfwerte";
	public static final String KEY_ZAUBER = "zauber";
	public static final String KEY_MUENZE = "muenze";
	public static final String KEY_WAEHRUNG = "waehrung";
	public static final String KEY_KULTUR = "kultur";
	public static final String KEY_PROBE = "probe";
	public static final String KEY_BE = "be";
	public static final String KEY_VERWENDUNGSART = "verwendungsArt";
	public static final String KEY_HAND = "hand";
	public static final String KEY_SCHILD = "schild";
	public static final String KEY_GROESSE = "groesse";
	public static final String KEY_ALTER = "alter";
	public static final String KEY_EYECOLOR = "augenfarbe";
	public static final String KEY_HAIRCOLOR = "haarfarbe";
	public static final String KEY_AUSSEHEN = "aussehen";
	public static final String KEY_GEWICHT = "gewicht";
	public static final String KEY_COMMENT = "kommentar";
	public static final String KEY_CARD_ID = "card_id";
	public static final String KEY_PATH = "path";

	public static final String KEY_PARADE = "parade";
	public static final String KEY_ATTACKE = "attacke";
	public static final String KEY_CELL_NUMBER = "cellNumber";
	public static final String KEY_SCREEN = "screen";

	public static final String KEY_RUESTUNG = "Rüstung";
	public static final String KEY_RUESTUNG_UE = "Ruestung";
	public static final String KEY_GESAMT_BE = "gesbe";
	public static final String KEY_RS = "rs";
	public static final String KEY_STERNE = "sterne";
	public static final String KEY_TEILE = "teile";
	public static final String KEY_PROFESSION = "profession";
	public static final String KEY_STRING = "string";
	public static final String KEY_AUSBILDUNGEN = "ausbildungen";
	public static final String KEY_AUSBILDUNG = "ausbildung";
	public static final String KEY_RASSE = "rasse";

	public static final String KEY_SCHILDWAFFE = "Schild";

	public static final String KEY_FERNKAMPWAFFE = "Fernkampfwaffe";
	public static final String KEY_TPMOD = "tpmod";

	public static final String KEY_NAHKAMPWAFFE = "Nahkampfwaffe";
	public static final String KEY_TREFFERPUNKTE = "trefferpunkte";
	public static final String KEY_TREFFERPUNKTE_MUL = "mul";
	public static final String KEY_TREFFERPUNKTE_DICE = "w";
	public static final String KEY_TREFFERPUNKTE_SUM = "sum";
	public static final String KEY_TREFFERPUNKTE_KK = "tpkk";
	public static final String KEY_TREFFERPUNKTE_KK_MIN = "kk";
	public static final String KEY_TREFFERPUNKTE_KK_STEP = "schrittweite";
	public static final String KEY_WAFFENMODIF = "wm";
	public static final String KEY_WAFFENMODIF_PA = "pa";
	public static final String KEY_WAFFENMODIF_AT = "at";
	public static final String KEY_BRUCHFAKTOR = "bf";
	public static final String KEY_BRUCHFAKTOR_AKT = "akt";
	public static final String KEY_INI_MOD = "inimod";
	public static final String KEY_INI_MOD_INI = "ini";
	public static final String KEY_MOD_ALLGEMEIN = "modallgemein";
	public static final String KEY_ANMERKUNGEN = "anmerkungen";
	public static final String KEY_HAUSZAUBER = "hauszauber";
	public static final String KEY_KOSTEN = "kosten";
	public static final String KEY_REICHWEITE = "reichweite";
	public static final String KEY_REPRESENTATION = "repraesentation";
	public static final String KEY_VARIANTE = "variante";
	public static final String KEY_WIRKUNGSDAUER = "wirkungsdauer";
	public static final String KEY_ZAUBERDAUER = "zauberdauer";
	public static final String KEY_ZAUBERKOMMENTAR = "zauberkommentar";
	public static final String KEY_KEY = "key";

	public static final String KEY_FAVORITE = "fav";
	public static final String KEY_UNUSED = "unused";
	public static final String KEY_PORTRAIT_PATH = "portrait_path";

	public static final String KEY_EIGENSCHAFTEN = "eigenschaften";
	public static final String KEY_AUSRUESTUNGEN_UE = "ausruestungen";
	public static final String KEY_AUSRUESTUNGEN = "ausrüstungen";
	public static final String KEY_GEGENSTAENDE_AE = "gegenstaende";
	public static final String KEY_GEGENSTAENDE = "gegenstände";
	public static final String KEY_ZAUBERLISTE = "zauberliste";
	public static final String KEY_TALENTLISTE = "talentliste";
	public static final String KEY_BASIS = "basis";
	public static final String KEY_BEZEICHNER = "bezeichner";

	public static final String KEY_DAUER = "dauer";
	public static final String KEY_WIRKUNG = "wirkung";
	public static final String TAB_CONFIG = "tabConfig";
	public static final String KEY_AUSWAHL = "auswahl";
	public static final String KEY_NUMMER = "nummer";

	public static final String KEY_VERBINDUNGEN = "verbindungen";
	public static final String KEY_VERBINDUNG = "verbindung";
	public static final String KEY_DESCRIPTION = "beschreibung";
	public static final String KEY_SO = "so";

	public static final String KEY_NOTIZ_PREFIX = "notiz";
	public static final String KEY_NOTIZ = "notiz";
	public static final String KEY_AUSSEHENTEXT_PREFIX = "aussehentext";
	public static final String KEY_TITEL = "titel";
	public static final String KEY_STAND = "stand";
	public static final String KEY_ACTIVE = "active";
	public static final String KEY_SPEZIALISIERUNG = "spezialisierung";
	public static final String KEY_K = "k";
	public static final String KEY_MRMOD = "mrmod";
	public static final String KEY_GESBE = "gesbe";
	public static final String KEY_ENTFERNUNG = "entfernung";

}
