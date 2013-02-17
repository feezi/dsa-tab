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
package com.dsatab.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.widget.Toast;

import com.dsatab.DsaTabApplication;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.AbstractModificator;
import com.dsatab.data.modifier.RulesModificator.ModificatorType;
import com.dsatab.util.Debug;
import com.dsatab.util.Util;

/**
 * @author Ganymede
 * 
 */
public class CustomModificator extends AbstractModificator implements JSONable {

	private static final String POSTFIX_TP = " tp";

	public static final String KEY_AT = "at";
	public static final String KEY_FK = "fk";
	public static final String KEY_PA = "pa";

	public static final String KEY_ZAUBER = "zauber";
	public static final String KEY_EIGENSCHAFTEN = "eigenschaften";
	public static final String KEY_TALENTE = "talente";
	public static final String KEY_LITURGIEN = "liturgien";

	private static final List<String> VALID_KEYS = new ArrayList<String>();

	private static final String FIELD_NAME = "name";
	private static final String FIELD_RULES = "rules";
	private static final String FIELD_COMMENT = "comment";
	private static final String FIELD_ACTIVE = "active";

	static {
		for (AttributeType type : AttributeType.values())
			VALID_KEYS.add(type.code().toLowerCase(Locale.GERMAN));

		VALID_KEYS.add(KEY_ZAUBER);
		VALID_KEYS.add(KEY_TALENTE);
		VALID_KEYS.add(KEY_LITURGIEN);
	}

	private UUID id;

	private Map<String, Integer> modMap;

	private String name, rules, comment;

	/**
	 * @param hero
	 */
	public CustomModificator(Hero hero) {
		super(hero, true);
		this.id = UUID.randomUUID();
	}

	public CustomModificator(Hero hero, JSONObject json) throws JSONException {
		super(hero);
		this.id = UUID.randomUUID();

		this.name = json.getString(FIELD_NAME);
		this.rules = json.getString(FIELD_RULES);
		this.comment = json.getString(FIELD_COMMENT);

		if (json.has(FIELD_ACTIVE))
			this.active = json.getBoolean(FIELD_ACTIVE);
		else
			this.active = true;

	}

	public UUID getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.Modificator#getModificatorName()
	 */
	@Override
	public String getModificatorName() {
		return name;
	}

	public void setModificatorName(String name) {
		this.name = name;
	}

	public String getModificatorInfo() {
		if (TextUtils.isEmpty(comment))
			return rules;
		else
			return rules + "(" + comment + ")";
	}

	public String getRules() {
		return rules;
	}

	public void setRules(String info) {
		this.rules = info;
		modMap = null;
		fireModificatorChanged();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String info) {
		this.comment = info;
	}

	private Map<String, Integer> getModMap() {
		if (modMap == null) {
			modMap = new HashMap<String, Integer>();

			String rules = getRules();

			if (!TextUtils.isEmpty(rules)) {

				StringBuilder errors = new StringBuilder();

				StringTokenizer st = new StringTokenizer(rules, ",");
				String token = null;
				while (st.hasMoreTokens()) {
					try {
						token = st.nextToken();

						int index = token.lastIndexOf("+");
						if (index < 0)
							index = token.lastIndexOf("-");

						String key = token.substring(0, index).trim().toLowerCase(Locale.GERMAN);
						String value = token.substring(index);

						// if (VALID_KEYS.contains(key)) {
						// revert value since we deal with erschwernis
						// intern, but declare it as bonus outside
						modMap.put(key, Util.parseInteger(value));
						// } else {
						// modMap.put(key, Util.parseInt(value));

						// if (errors.length() > 0)
						// errors.append(", ");
						//
						// errors.append(token);
						// }
					} catch (Exception e) {
						Debug.warning("Couldn't parse string for modifikators:" + token);
						if (errors.length() > 0)
							errors.append(", ");

						errors.append(token);
					}
				}
				if (errors.length() > 0) {
					Toast.makeText(DsaTabApplication.getInstance().getApplicationContext(),
							"Folgende Regeln konnten nicht verarbeitet werden: " + errors, Toast.LENGTH_LONG).show();
				}
			}
		}

		return modMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.Modificator#fulfills()
	 */
	@Override
	public boolean fulfills() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.Modificator#affects(com.dsatab.data.enums.
	 * AttributeType)
	 */
	@Override
	public boolean affects(AttributeType type) {
		boolean result = false;
		if (type == AttributeType.Lebensenergie) {
			type = AttributeType.Lebensenergie_Aktuell;
		} else if (type == AttributeType.Astralenergie) {
			type = AttributeType.Astralenergie_Aktuell;
		} else if (type == AttributeType.Karmaenergie) {
			type = AttributeType.Karmaenergie_Aktuell;
		} else if (type == AttributeType.Ausdauer) {
			type = AttributeType.Ausdauer_Aktuell;
		} else if (type == AttributeType.Lebensenergie_Aktuell || type == AttributeType.Astralenergie_Aktuell
				|| type == AttributeType.Karmaenergie_Aktuell || type == AttributeType.Ausdauer_Aktuell) {
			type = null;
		}
		if (type != null) {
			result = containsModifier(type.code());

			if (!result && AttributeType.isEigenschaft(type)) {
				result = containsModifier(KEY_EIGENSCHAFTEN);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.Modificator#getAffectedModifierTypes()
	 */
	@Override
	public List<ModificatorType> getAffectedModifierTypes() {
		return Arrays.asList(ModificatorType.ALL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.Modificator#affects(com.dsatab.data.Probe)
	 */
	@Override
	public boolean affect(Probe probe) {
		boolean result = false;
		if (probe instanceof CombatDistanceTalent) {
			result = containsModifier(probe.getName());
			if (!result)
				result = containsModifier(KEY_FK);
		} else if (probe instanceof Spell) {
			result = containsModifier(probe.getName());
			if (!result)
				result = containsModifier(KEY_ZAUBER);
		} else if (probe instanceof Art) {
			result = containsModifier(probe.getName());
			if (!result)
				result = containsModifier(KEY_LITURGIEN);
		} else if (probe instanceof CombatShieldTalent) {
			result = containsModifier(probe.getName());
			if (!result)
				result = containsModifier(KEY_PA);
		} else if (probe instanceof CombatMeleeAttribute) {
			result = containsModifier(probe.getName());
			if (!result) {
				CombatMeleeAttribute meleeAttribute = (CombatMeleeAttribute) probe;
				if (meleeAttribute.isAttack())
					result = containsModifier(KEY_AT);
				else
					result = containsModifier(KEY_PA);
			}
		} else if (probe instanceof CombatProbe) {
			result = containsModifier(probe.getName());
			if (!result) {
				CombatProbe combatProbe = (CombatProbe) probe;
				if (combatProbe.getCombatTalent() instanceof CombatDistanceTalent)
					result = containsModifier(KEY_FK);
				else if (combatProbe.isAttack())
					result = containsModifier(KEY_AT);
				else
					result = containsModifier(KEY_PA);
			}
		} else if (probe instanceof Talent) {
			result = containsModifier(probe.getName());
			if (!result)
				result = containsModifier(KEY_TALENTE);
		}

		return result;
	}

	private boolean containsModifier(String key) {
		if (key == null)
			return false;

		key = key.toLowerCase(Locale.GERMAN);

		return getModMap().containsKey(key);
	}

	private Integer getModifier(String key) {
		if (key == null)
			return null;

		key = key.toLowerCase(Locale.GERMAN);
		return getModMap().get(key);
	}

	public int getModifierValue(EquippedItem item) {
		if (isActive()) {
			Integer modifier = getModifier(item.getItem().getName() + POSTFIX_TP);

			if (modifier == null && item.getTalent() != null)
				modifier = getModifier(item.getTalent().getType().xmlName() + POSTFIX_TP);

			if (item.getItemSpecification() instanceof Weapon) {
				if (modifier == null)
					modifier = getModifier(KEY_AT + POSTFIX_TP);
			} else if (item.getItemSpecification() instanceof DistanceWeapon) {
				if (modifier == null)
					modifier = getModifier(KEY_FK + POSTFIX_TP);
			}

			if (modifier != null) {
				return modifier;
			}
		}
		return 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.data.modifier.AbstractModificator#getModifier(com.dsatab.data
	 * .Probe)
	 */
	@Override
	public int getModifierValue(Probe probe) {
		if (isActive()) {

			Integer modifier = null;
			if (probe instanceof Attribute) {
				Attribute attribute = (Attribute) probe;
				return getModifierValue(attribute.getType());
				// combatDistancetalent has to come before talent since its a
				// talent too, but needs special handling
			} else if (probe instanceof CombatDistanceTalent) {
				modifier = getModifier(probe.getName());
				if (modifier == null)
					modifier = getModifier(KEY_FK);
			} else if (probe instanceof Spell) {
				modifier = getModifier(probe.getName());
				if (modifier == null)
					modifier = getModifier(KEY_ZAUBER);
			} else if (probe instanceof Art) {
				modifier = getModifier(probe.getName());
				if (modifier == null)
					modifier = getModifier(KEY_LITURGIEN);
			} else if (probe instanceof CombatShieldTalent) {
				modifier = getModifier(probe.getName());
				if (modifier == null)
					modifier = getModifier(KEY_PA);
			} else if (probe instanceof CombatMeleeAttribute) {
				modifier = getModifier(probe.getName());
				if (modifier == null) {
					CombatMeleeAttribute meleeAttribute = (CombatMeleeAttribute) probe;
					if (meleeAttribute.isAttack())
						modifier = getModifier(KEY_AT);
					else
						modifier = getModifier(KEY_PA);
				}
			} else if (probe instanceof CombatProbe) {
				modifier = getModifier(probe.getName());
				if (modifier == null) {
					CombatProbe combatProbe = (CombatProbe) probe;

					if (combatProbe.getCombatTalent() instanceof CombatDistanceTalent) {
						modifier = getModifier(KEY_FK);
					} else if (combatProbe.isAttack())
						modifier = getModifier(KEY_AT);
					else
						modifier = getModifier(KEY_PA);
				}
			} else if (probe instanceof Talent) {
				modifier = getModifier(probe.getName());
				if (modifier == null)
					modifier = getModifier(KEY_TALENTE);
			}

			if (modifier != null) {
				return modifier;
			}
		}
		return 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.data.modifier.AbstractModificator#getModifier(com.dsatab.data
	 * .enums.AttributeType)
	 */
	@Override
	public int getModifierValue(AttributeType type) {
		if (isActive()) {

			Integer modifier = getModifier(type.code());
			if (modifier == null && AttributeType.isEigenschaft(type)) {
				modifier = getModifier(KEY_EIGENSCHAFTEN);
			}

			if (modifier == null && type == AttributeType.Lebensenergie) {
				modifier = getModifier(AttributeType.Lebensenergie_Aktuell.code());
			} else if (modifier == null && type == AttributeType.Astralenergie) {
				modifier = getModifier(AttributeType.Astralenergie_Aktuell.code());
			} else if (modifier == null && type == AttributeType.Karmaenergie) {
				modifier = getModifier(AttributeType.Karmaenergie_Aktuell.code());
			} else if (modifier == null && type == AttributeType.Ausdauer) {
				modifier = getModifier(AttributeType.Ausdauer_Aktuell.code());
			}

			if (type == AttributeType.Lebensenergie_Aktuell || type == AttributeType.Astralenergie_Aktuell
					|| type == AttributeType.Karmaenergie_Aktuell || type == AttributeType.Ausdauer_Aktuell) {
				modifier = null;
			}

			if (modifier != null) {
				return modifier;
			}
		}
		return 0;
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject out = new JSONObject();

		out.put(FIELD_NAME, name);
		out.put(FIELD_RULES, rules);
		out.put(FIELD_COMMENT, comment);
		out.put(FIELD_ACTIVE, isActive());
		return out;
	}
}
