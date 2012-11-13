package com.dsatab.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.MainActivity;

public class FilterDialog extends AlertDialog implements DialogInterface.OnClickListener {

	public static final String PREF_KEY_TALENT_FAVORITE = "SHOW_FAVORITE_TALENT";
	public static final String PREF_KEY_TALENT_NORMAL = "SHOW_NORMAL_TALENT";
	public static final String PREF_KEY_TALENT_UNUSED = "SHOW_UNUSED_TALENT";

	public static final String PREF_KEY_TALENT_MODIFIERS = "SHOW_MODIFIERS_TALENT";

	public static final String PREF_KEY_SPELL_FAVORITE = "SHOW_FAVORITE_SPELL";
	public static final String PREF_KEY_SPELL_NORMAL = "SHOW_NORMAL_SPELL";
	public static final String PREF_KEY_SPELL_UNUSED = "SHOW_UNUSED_SPELL";
	public static final String PREF_KEY_SPELL_MODIFIERS = "SHOW_MODIFIERS_SPELL";

	public static final String PREF_KEY_ART_FAVORITE = "SHOW_FAVORITE_LITURGIE";
	public static final String PREF_KEY_ART_NORMAL = "SHOW_NORMAL_LITURGIE";
	public static final String PREF_KEY_ART_UNUSED = "SHOW_UNUSED_LITURGIE";
	public static final String PREF_KEY_ART_MODIFIERS = "SHOW_MODIFIERS_LITURGIE";

	public static final String PREF_KEY_SHOW_ARMOR = "show_armor";
	public static final String PREF_KEY_SHOW_MODIFIER = "show_modifier";
	public static final String PREF_KEY_INCLUDE_MODIFIER = "include_modifier";
	public static final String PREF_KEY_SHOW_EVADE = "show_evade";

	private CheckBox talentFav, talentNormal, talentUnused, talentModifier;
	private CheckBox spellFav, spellNormal, spellUnused, spellModifier;
	private CheckBox liturgieFav, liturgieNormal, liturgieUnused, liturgieModifier;

	private CheckBox armor, modifier, evade, includeModifier;

	private boolean filterListVisible = true;
	private boolean filterFightVisible = true;

	private MainActivity main;

	public FilterDialog(MainActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainActivity getMain() {
		return main;
	}

	public void setFilterListVisibile(boolean v) {
		filterListVisible = v;

		if (findViewById(R.id.popup_filter_list) != null) {
			findViewById(R.id.popup_filter_list).setVisibility(filterListVisible ? View.VISIBLE : View.GONE);
		}
	}

	public void setFilterFightVisibile(boolean v) {
		filterFightVisible = v;

		if (findViewById(R.id.popup_filter_fight) != null) {
			findViewById(R.id.popup_filter_fight).setVisibility(filterFightVisible ? View.VISIBLE : View.GONE);
		}
	}

	private void init() {

		setCanceledOnTouchOutside(true);
		setTitle("Filtereinstellungen");
		setIcon(android.R.drawable.ic_menu_view);

		View content = LayoutInflater.from(getContext()).inflate(R.layout.popup_filter, null, false);

		content.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setView(content);

		SharedPreferences pref = DSATabApplication.getPreferences();

		talentFav = (CheckBox) content.findViewById(R.id.cb_talent_favorites);
		talentNormal = (CheckBox) content.findViewById(R.id.cb_talent_normal);
		talentUnused = (CheckBox) content.findViewById(R.id.cb_talent_unused);
		talentModifier = (CheckBox) content.findViewById(R.id.cb_talent_modifiers);

		talentFav.setChecked(pref.getBoolean(PREF_KEY_TALENT_FAVORITE, true));
		talentNormal.setChecked(pref.getBoolean(PREF_KEY_TALENT_NORMAL, true));
		talentUnused.setChecked(pref.getBoolean(PREF_KEY_TALENT_UNUSED, false));
		talentModifier.setChecked(pref.getBoolean(PREF_KEY_TALENT_MODIFIERS, true));

		spellFav = (CheckBox) content.findViewById(R.id.cb_spell_favorites);
		spellNormal = (CheckBox) content.findViewById(R.id.cb_spell_normal);
		spellUnused = (CheckBox) content.findViewById(R.id.cb_spell_unused);
		spellModifier = (CheckBox) content.findViewById(R.id.cb_spell_modifiers);

		spellFav.setChecked(pref.getBoolean(PREF_KEY_SPELL_FAVORITE, true));
		spellNormal.setChecked(pref.getBoolean(PREF_KEY_SPELL_NORMAL, true));
		spellUnused.setChecked(pref.getBoolean(PREF_KEY_SPELL_UNUSED, false));
		spellModifier.setChecked(pref.getBoolean(PREF_KEY_SPELL_MODIFIERS, true));

		liturgieFav = (CheckBox) content.findViewById(R.id.cb_liturgie_favorites);
		liturgieNormal = (CheckBox) content.findViewById(R.id.cb_liturgie_normal);
		liturgieUnused = (CheckBox) content.findViewById(R.id.cb_liturgie_unused);
		liturgieModifier = (CheckBox) content.findViewById(R.id.cb_liturgie_modifiers);

		liturgieFav.setChecked(pref.getBoolean(PREF_KEY_ART_FAVORITE, true));
		liturgieNormal.setChecked(pref.getBoolean(PREF_KEY_ART_NORMAL, true));
		liturgieUnused.setChecked(pref.getBoolean(PREF_KEY_ART_UNUSED, false));
		liturgieModifier.setChecked(pref.getBoolean(PREF_KEY_ART_MODIFIERS, true));

		armor = (CheckBox) content.findViewById(R.id.cb_show_armor);
		modifier = (CheckBox) content.findViewById(R.id.cb_show_modifier);
		evade = (CheckBox) content.findViewById(R.id.cb_show_evade);
		includeModifier = (CheckBox) content.findViewById(R.id.cb_include_modifier);

		armor.setChecked(pref.getBoolean(PREF_KEY_SHOW_ARMOR, true));
		modifier.setChecked(pref.getBoolean(PREF_KEY_SHOW_MODIFIER, true));
		evade.setChecked(pref.getBoolean(PREF_KEY_SHOW_EVADE, false));
		includeModifier.setChecked(pref.getBoolean(PREF_KEY_INCLUDE_MODIFIER, true));

		setButton(BUTTON_POSITIVE, getContext().getString(R.string.label_ok), this);
		setButton(BUTTON_NEGATIVE, getContext().getString(R.string.label_cancel), this);

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {

			SharedPreferences pref = DSATabApplication.getPreferences();
			Editor edit = pref.edit();

			edit.putBoolean(PREF_KEY_TALENT_FAVORITE, talentFav.isChecked());
			edit.putBoolean(PREF_KEY_TALENT_NORMAL, talentNormal.isChecked());
			edit.putBoolean(PREF_KEY_TALENT_UNUSED, talentUnused.isChecked());
			edit.putBoolean(PREF_KEY_TALENT_MODIFIERS, talentModifier.isChecked());

			edit.putBoolean(PREF_KEY_SPELL_FAVORITE, spellFav.isChecked());
			edit.putBoolean(PREF_KEY_SPELL_NORMAL, spellNormal.isChecked());
			edit.putBoolean(PREF_KEY_SPELL_UNUSED, spellUnused.isChecked());
			edit.putBoolean(PREF_KEY_SPELL_MODIFIERS, spellModifier.isChecked());

			edit.putBoolean(PREF_KEY_ART_FAVORITE, liturgieFav.isChecked());
			edit.putBoolean(PREF_KEY_ART_NORMAL, liturgieNormal.isChecked());
			edit.putBoolean(PREF_KEY_ART_UNUSED, liturgieUnused.isChecked());
			edit.putBoolean(PREF_KEY_ART_MODIFIERS, liturgieModifier.isChecked());

			edit.putBoolean(PREF_KEY_SHOW_ARMOR, armor.isChecked());
			edit.putBoolean(PREF_KEY_SHOW_EVADE, evade.isChecked());
			edit.putBoolean(PREF_KEY_SHOW_MODIFIER, modifier.isChecked());
			edit.putBoolean(PREF_KEY_INCLUDE_MODIFIER, includeModifier.isChecked());

			edit.commit();

			this.dismiss();
		} else if (which == DialogInterface.BUTTON_NEUTRAL) {
			// do nothing
			this.dismiss();
		}

	}

}
