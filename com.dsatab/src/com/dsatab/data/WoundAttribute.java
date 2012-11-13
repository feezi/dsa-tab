package com.dsatab.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.dsatab.DSATabApplication;
import com.dsatab.DsaTabConfiguration.WoundType;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.data.modifier.AbstractModificator;
import com.dsatab.util.Debug;

public class WoundAttribute extends AbstractModificator implements JSONable {

	private static final String FIELD_POSITION = "position";
	private static final String FIELD_VALUE = "value";
	private static final String FIELD_ACTIVE = "active";

	private Position position;

	private Integer value;

	public WoundAttribute(Hero hero, Position position) {
		super(hero, true);
		this.position = position;
		this.value = 0;

	}

	public WoundAttribute(Hero hero, JSONObject json) throws JSONException {
		super(hero);
		this.value = json.getInt(FIELD_VALUE);
		this.position = Position.valueOf(json.getString(FIELD_POSITION));

		if (json.has(FIELD_ACTIVE))
			active = json.getBoolean(FIELD_ACTIVE);
		else
			active = true;
	}

	public String getName() {
		return position.getName();
	}

	@Override
	public String getModificatorName() {
		return "Wunde " + getName() + " x" + getValue();
	}

	@Override
	public String getModificatorInfo() {
		String info = null;
		if (DSATabApplication.getInstance().getConfiguration().getWoundType() == WoundType.Trefferzonen) {

			switch (getPosition()) {
			case Kopf:
				info = "MU,KL,IN,INI -2";
				break;
			case Bauch:
				info = "KO,KK,GS,INI,AT,PA -1; +1W6 SP";
				break;
			case Brust:
				info = "KO,KK,AT,PA -1; +1W6 SP";
				break;
			case LeftLowerArm:
			case RightLowerArm:
				info = "KK,FF,AT,PA -2";
				break;
			case UpperLeg:
			case LowerLeg:
				info = "GE,INI,AT,PA -2; GS -1";
				break;
			default:
				// do nothing
				break;
			}
		} else {
			info = "AT,PA,FK,GE,INI -2; GS -1";
		}

		return info;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		Integer oldValue = getValue();
		this.value = value;

		if ((oldValue == null || oldValue == 0) && value != null && value > 0) {
			hero.fireModifierAddedEvent(this);
		} else if (oldValue != null && oldValue > 0 && (value == null || value == 0)) {
			hero.fireModifierRemovedEvent(this);
		} else {
			hero.fireModifierChangedEvent(this);
		}
	}

	@Override
	public Modifier getModifier(AttributeType type) {
		if (isActive()) {
			int modifier = 0;
			if (DSATabApplication.getInstance().getConfiguration().getWoundType() == WoundType.Trefferzonen) {

				switch (getPosition()) {
				case Kopf:
					if (type == AttributeType.Mut || type == AttributeType.Klugheit || type == AttributeType.Intuition
							|| type == AttributeType.ini || type == AttributeType.Initiative_Aktuell) {
						modifier += -2 * getValue();
					}
					break;
				case Bauch:
					if (type == AttributeType.Körperkraft || type == AttributeType.at || type == AttributeType.fk
							|| type == AttributeType.pa || type == AttributeType.Ausweichen) {
						modifier += -1 * getValue();
					} else if (type == AttributeType.Geschwindigkeit) {
						modifier += -1 * getValue();
					}
					break;
				case Brust:
					if (type == AttributeType.Konstitution || type == AttributeType.Körperkraft
							|| type == AttributeType.at || type == AttributeType.fk || type == AttributeType.pa
							|| type == AttributeType.Ausweichen) {
						modifier += -1 * getValue();
					}
					break;
				case LeftLowerArm:
				case RightLowerArm:
					if (type == AttributeType.Fingerfertigkeit || type == AttributeType.Körperkraft
							|| type == AttributeType.at || type == AttributeType.fk || type == AttributeType.pa
							|| type == AttributeType.Ausweichen) {
						modifier += -2 * getValue();
					}
					break;
				case UpperLeg:
				case LowerLeg:
					if (type == AttributeType.Gewandtheit || type == AttributeType.ini
							|| type == AttributeType.Initiative_Aktuell || type == AttributeType.at
							|| type == AttributeType.pa || type == AttributeType.fk || type == AttributeType.Ausweichen) {
						modifier += -2 * getValue();
					} else if (type == AttributeType.Geschwindigkeit) {
						modifier += -1 * getValue();
					}
					break;
				default:
					// do nothing
					break;
				}
			} else {
				if (type == AttributeType.at || type == AttributeType.pa || type == AttributeType.fk
						|| type == AttributeType.Gewandtheit || type == AttributeType.Initiative_Aktuell
						|| type == AttributeType.Ausweichen) {
					modifier += -2 * getValue();
				} else if (type == AttributeType.Geschwindigkeit) {
					modifier += -1 * getValue();
				}
			}
			return new Modifier(modifier, getModificatorName(), getModificatorInfo());

		} else {
			return null;
		}
	}

	@Override
	public Modifier getModifier(Probe probe) {
		if (isActive()) {
			int modifier = 0;

			if (probe instanceof Attribute) {
				Attribute attr = (Attribute) probe;
				return getModifier(attr.getType());
			} else if (probe instanceof CombatDistanceTalent || probe instanceof CombatShieldTalent
					|| probe instanceof CombatMeleeAttribute || probe instanceof CombatProbe) {

				if (DSATabApplication.getInstance().getConfiguration().getWoundType() == WoundType.Trefferzonen) {
					switch (getPosition()) {
					case Kopf:
						break;
					case Bauch:
					case Brust:
						modifier += -1 * getValue();
						break;
					case LeftLowerArm:
					case RightLowerArm:

						if (probe instanceof CombatProbe) {
							CombatProbe combatProbe = (CombatProbe) probe;

							EquippedItem equippedItem = combatProbe.getEquippedItem();

							if (equippedItem != null && equippedItem.getItemSpecification() instanceof Weapon) {
								Weapon w = (Weapon) equippedItem.getItemSpecification();

								if (w.isTwoHanded()) {
									modifier += -1 * getValue();
									Debug.verbose("Zweihandwaffen Handwunde AT/PA-1*" + getValue());
									break;
								} else {
									if (getPosition() == Position.LeftLowerArm) {
										Debug.verbose("Angriff/Parade mit Hauptwaffe und Wunde auf linkem Arm ignoriert");
										break;
									}
								}
							}
							if (equippedItem != null && equippedItem.getItemSpecification() instanceof Shield) {
								// Shield w = (Shield)
								// combatProbe.getEquippedItem().getItem();
								if (getPosition() == Position.RightLowerArm) {
									Debug.verbose("Angriff/Parade mit Schildwaffe und Wunde auf rechtem Arm ignoriert");
									break;
								}
							}
						}
						Debug.verbose(" Wunde auf Arm AT/PA -2*" + getValue());
						modifier += -2 * getValue();
						break;
					case UpperLeg:
					case LowerLeg:
						modifier += -2 * getValue();
						break;
					default:
						// do nothing
						break;
					}
				} else {
					modifier += -2 * getValue();
				}
			}
			return new Modifier(modifier, getModificatorName(), getModificatorInfo());
		} else {
			return null;
		}
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject out = new JSONObject();

		out.put(FIELD_POSITION, position.name());
		out.put(FIELD_VALUE, value);
		out.put(FIELD_ACTIVE, isActive());

		return out;
	}

}
