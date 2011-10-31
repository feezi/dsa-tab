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

public class FilterDialog extends AlertDialog implements android.view.View.OnClickListener,
		DialogInterface.OnClickListener {

	public static final String PREF_KEY_TALENT_FAVORITE = "SHOW_FAVORITE_TALENT";
	public static final String PREF_KEY_TALENT_NORMAL = "SHOW_NORMAL_TALENT";
	public static final String PREF_KEY_TALENT_UNUSED = "SHOW_UNUSED_TALENT";

	public static final String PREF_KEY_SPELL_FAVORITE = "SHOW_FAVORITE_SPELL";
	public static final String PREF_KEY_SPELL_NORMAL = "SHOW_NORMAL_SPELL";
	public static final String PREF_KEY_SPELL_UNUSED = "SHOW_UNUSED_SPELL";

	public static final String PREF_KEY_ART_FAVORITE = "SHOW_FAVORITE_LITURGIE";
	public static final String PREF_KEY_ART_NORMAL = "SHOW_NORMAL_LITURGIE";
	public static final String PREF_KEY_ART_UNUSED = "SHOW_UNUSED_LITURGIE";

	public static final String PREF_KEY_SHOW_ARMOR = "show_armor";
	public static final String PREF_KEY_SHOW_MODIFIER = "show_modifier";
	public static final String PREF_KEY_SHOW_EVADE = "show_evade";

	private CheckBox talentFav, talentNormal, talentUnused;
	private CheckBox spellFav, spellNormal, spellUnused;
	private CheckBox liturgieFav, liturgieNormal, liturgieUnused;

	private CheckBox armor, modifier, evade;

	private MainActivity main;

	public FilterDialog(MainActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainActivity getMain() {
		return main;
	}

	public void onClick(View v) {

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

		talentFav.setChecked(pref.getBoolean(PREF_KEY_TALENT_FAVORITE, true));
		talentNormal.setChecked(pref.getBoolean(PREF_KEY_TALENT_NORMAL, true));
		talentUnused.setChecked(pref.getBoolean(PREF_KEY_TALENT_UNUSED, false));

		spellFav = (CheckBox) content.findViewById(R.id.cb_spell_favorites);
		spellNormal = (CheckBox) content.findViewById(R.id.cb_spell_normal);
		spellUnused = (CheckBox) content.findViewById(R.id.cb_spell_unused);

		spellFav.setChecked(pref.getBoolean(PREF_KEY_SPELL_FAVORITE, true));
		spellNormal.setChecked(pref.getBoolean(PREF_KEY_SPELL_NORMAL, true));
		spellUnused.setChecked(pref.getBoolean(PREF_KEY_SPELL_UNUSED, false));

		liturgieFav = (CheckBox) content.findViewById(R.id.cb_liturgie_favorites);
		liturgieNormal = (CheckBox) content.findViewById(R.id.cb_liturgie_normal);
		liturgieUnused = (CheckBox) content.findViewById(R.id.cb_liturgie_unused);

		liturgieFav.setChecked(pref.getBoolean(PREF_KEY_ART_FAVORITE, true));
		liturgieNormal.setChecked(pref.getBoolean(PREF_KEY_ART_NORMAL, true));
		liturgieUnused.setChecked(pref.getBoolean(PREF_KEY_ART_UNUSED, false));

		armor = (CheckBox) content.findViewById(R.id.cb_show_armor);
		modifier = (CheckBox) content.findViewById(R.id.cb_show_modifier);
		evade = (CheckBox) content.findViewById(R.id.cb_show_evade);

		armor.setChecked(pref.getBoolean(PREF_KEY_SHOW_ARMOR, true));
		modifier.setChecked(pref.getBoolean(PREF_KEY_SHOW_MODIFIER, true));
		evade.setChecked(pref.getBoolean(PREF_KEY_SHOW_EVADE, false));

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

			edit.putBoolean(PREF_KEY_SPELL_FAVORITE, spellFav.isChecked());
			edit.putBoolean(PREF_KEY_SPELL_NORMAL, spellNormal.isChecked());
			edit.putBoolean(PREF_KEY_SPELL_UNUSED, spellUnused.isChecked());

			edit.putBoolean(PREF_KEY_ART_FAVORITE, liturgieFav.isChecked());
			edit.putBoolean(PREF_KEY_ART_NORMAL, liturgieNormal.isChecked());
			edit.putBoolean(PREF_KEY_ART_UNUSED, liturgieUnused.isChecked());

			edit.putBoolean(PREF_KEY_SHOW_ARMOR, armor.isChecked());
			edit.putBoolean(PREF_KEY_SHOW_EVADE, evade.isChecked());
			edit.putBoolean(PREF_KEY_SHOW_MODIFIER, modifier.isChecked());

			edit.commit();

			this.dismiss();
		} else if (which == DialogInterface.BUTTON_NEUTRAL) {
			// do nothing
			this.dismiss();
		}

	}

}
