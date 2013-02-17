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

/**
 * @author Ganymede
 * 
 */
public enum ArtType {

	Liturige(Art.LITURGIE_PREFIX, TalentType.Liturgiekenntnis), Ritual(Art.RITUAL_PREFIX, TalentType.Ritualkenntnis), RitualSeher(
			Art.RITUAL_SEHER_PREFIX, TalentType.Ritualkenntnis), Stabzauber(Art.STABZAUBER_PREFIX,
			TalentType.RitualkenntnisGildenmagie), Schalenzauber(Art.SCHALENZAUBER_PREFIX,
			TalentType.RitualkenntnisAlchimist), SchlangenringZauber(Art.SCHLANGENRING_ZAUBER_PEFIX,
			TalentType.RitualkenntnisGeode), Schuppenbeutel(Art.SCHUPPENBEUTEL_PREFIX,
			TalentType.RitualkenntnisKristallomantie), Trommelzauber(Art.TROMMELZAUBER_PREFIX,
			TalentType.RitualkenntnisDerwisch), Runen(Art.RUNEN_PREFIX), Kugelzauber(Art.KUGELZAUBER_PREFIX), KristallomantischesRitual(
			Art.KRISTALLOMANTISCHES_RITUAL_PREFIX, TalentType.RitualkenntnisKristallomantie), Hexenfluch(
			Art.HEXENFLUCH_PREFIX, TalentType.RitualkenntnisHexe), GabeDesOdun(Art.GABE_DES_ODUN_PREFIX,
			TalentType.RitualkenntnisDurroDun), DruidischesHerrschaftsritual(Art.DRUIDISCHES_HERRSCHAFTSRITUAL_PREFIX,
			TalentType.RitualkenntnisDruide), DruidischesDolchritual(Art.DRUIDISCHES_DOLCHRITUAL_PREFIX,
			TalentType.RitualkenntnisDruide), Zaubertanz(Art.ZAUBERTANZ_PREFIX, TalentType.RitualkenntnisZaubertänzer), Zauberzeichen(
			Art.ZAUBERZEICHEN_PREFIX), ZibiljaRitual(Art.ZIBILJA_RITUAL_PREFIX, TalentType.RitualkenntnisZibilja);

	public static final Map<String, ArtType> artMappings = new HashMap<String, ArtType>();
	static {
		artMappings.put("Apport", ArtType.Stabzauber);
		artMappings.put("Bannschwert", ArtType.Stabzauber);
		artMappings.put("Die Gestalt aus Rauch", ArtType.Ritual);
		artMappings.put("Kristallkraft bündeln", ArtType.KristallomantischesRitual);
	}

	private String prefix;

	private TalentType talentType;

	private ArtType(String prefix) {
		this.prefix = prefix;
		this.talentType = null;
	}

	private ArtType(String prefix, TalentType talentType) {
		this.prefix = prefix;
		this.talentType = talentType;
	}

	public String prefix() {
		return prefix;
	}

	public TalentType talentType() {
		return talentType;
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
