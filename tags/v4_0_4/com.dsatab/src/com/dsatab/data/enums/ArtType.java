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

import java.util.HashMap;
import java.util.Map;

import com.dsatab.data.Art;
import com.dsatab.data.Talent;

/**
 * @author Ganymede
 * 
 */
public enum ArtType {

	Liturige(Art.LITURGIE_PREFIX, Talent.LITURGIE_KENNTNIS_PREFIX), Ritual(Art.RITUAL_PREFIX,
			Talent.RITUAL_KENNTNIS_PREFIX), RitualSeher(Art.RITUAL_SEHER_PREFIX, Talent.RITUAL_KENNTNIS_PREFIX), Stabzauber(
			Art.STABZAUBER_PREFIX, Talent.RITUAL_KENNTNIS_GILDENMAGIE), Schalenzauber(Art.SCHALENZAUBER_PREFIX,
			Talent.RITUAL_KENNTNIS_ALCHEMIST), SchlangenringZauber(Art.SCHLANGENRING_ZAUBER_PEFIX,
			Talent.RITUAL_KENNTNIS_GEODE), Schuppenbeutel(Art.SCHUPPENBEUTEL_PREFIX,
			Talent.RITUAL_KENNTNIS_KRISTALLOMANTIE), Trommelzauber(Art.TROMMELZAUBER_PREFIX,
			Talent.RITUAL_KENNTNIS_DERWISCH), Runen(Art.RUNEN_PREFIX), Kugelzauber(Art.KUGELZAUBER_PREFIX), KristallomantischesRitual(
			Art.KRISTALLOMANTISCHES_RITUAL_PREFIX, Talent.RITUAL_KENNTNIS_KRISTALLOMANTIE), Hexenfluch(
			Art.HEXENFLUCH_PREFIX, Talent.RITUAL_KENNTNIS_HEXE), GabeDesOdun(Art.GABE_DES_ODUN_PREFIX,
			Talent.RITUAL_KENNTNIS_DURRO_DUN), DruidischesHerrschaftsritual(Art.DRUIDISCHES_HERRSCHAFTSRITUAL_PREFIX,
			Talent.RITUAL_KENNTNIS_DRUIDE), DruidischesDolchritual(Art.DRUIDISCHES_DOLCHRITUAL_PREFIX,
			Talent.RITUAL_KENNTNIS_DRUIDE), Zaubertanz(Art.ZAUBERTANZ_PREFIX,
			Talent.RITUAL_KENNTNIS_ZAUBERTAENZER_PREFIX), Zauberzeichen(Art.ZAUBERZEICHEN_PREFIX), ZibiljaRitual(
			Art.ZIBILJA_RITUAL_PREFIX, Talent.RITUAL_KENNTNIS_ZIBILJA);

	public static final Map<String, ArtType> artMappings = new HashMap<String, ArtType>();
	static {
		artMappings.put("Apport", ArtType.Stabzauber);
		artMappings.put("Bannschwert", ArtType.Stabzauber);
		artMappings.put("Die Gestalt aus Rauch", ArtType.Ritual);
		artMappings.put("Kristallkraft bündeln", ArtType.KristallomantischesRitual);
	}

	private String prefix;

	private String talentName;

	private ArtType(String prefix) {
		this.prefix = prefix;
		this.talentName = null;
	}

	private ArtType(String prefix, String talentName) {
		this.prefix = prefix;
		this.talentName = talentName;
	}

	public String prefix() {
		return prefix;
	}

	public String talentName() {
		return talentName;
	}

	public String getName() {
		String name = prefix.trim();
		if (name.endsWith(":"))
			name = name.substring(0, name.length() - 1);

		return name;
	}

	public static ArtType getTypeOfArt(String artName) {
		ArtType result = artMappings.get(artName);
		if (result == null) {
			ArtType[] types = ArtType.values();
			for (ArtType type : types) {
				if (artName.startsWith(type.prefix)) {
					result = type;
					break;
				}
			}
		}
		return result;
	}

	public String truncateName(String artName) {
		if (artName.startsWith(prefix)) {
			return artName.substring(prefix.length()).trim();
		} else {
			return artName.trim();
		}
	}
}
