package com.dsatab.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.data.Spell;

public class SpellInfoDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private Spell spell;

	private View popupcontent = null;

	public SpellInfoDialog(Context context) {
		super(context);
		init();
	}

	public Spell getSpell() {
		return spell;
	}

	public void setSpell(Spell spell) {
		this.spell = spell;
		if (spell != null) {
			setTitle(spell.getName());
			set(R.id.popup_spell_castduration, spell.getCastDuration());
			set(R.id.popup_spell_row_comment, R.id.popup_spell_comment, spell.getComments());
			set(R.id.popup_spell_costs, spell.getCosts());
			set(R.id.popup_spell_effect, spell.getEffect());
			set(R.id.popup_spell_target, spell.getTarget());
			set(R.id.popup_spell_range, spell.getRange());
			set(R.id.popup_spell_effectduration, spell.getEffectDuration());
			set(R.id.popup_spell_representation, spell.getRepresantation());
			set(R.id.popup_spell_row_variant, R.id.popup_spell_variant, spell.getVariant());
			set(R.id.popup_spell_source, spell.getSource());
			set(R.id.popup_spell_complexity, spell.getComplexity());
			set(R.id.popup_spell_merkmal, spell.getMerkmale());
		}
	}

	private void init() {
		setCanceledOnTouchOutside(true);

		popupcontent = (View) LayoutInflater.from(getContext()).inflate(R.layout.popup_spell_info, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		setButton(AlertDialog.BUTTON_NEUTRAL, getContext().getString(R.string.label_ok), this);

		TableLayout table = (TableLayout) popupcontent.findViewById(R.id.popup_spell_table);

		int childCount = table.getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (i % 2 == 1) {
				table.getChildAt(i).setBackgroundResource(R.color.RowOdd);
			} else {
				table.getChildAt(i).setBackgroundResource(R.color.RowEven);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnClickListener#onClick(android.content
	 * .DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == AlertDialog.BUTTON_NEUTRAL) {
			dialog.dismiss();
		}

	}

	private void set(int rowId, int tfid, String v) {
		if (TextUtils.isEmpty(v))
			popupcontent.findViewById(rowId).setVisibility(View.GONE);
		else {
			popupcontent.findViewById(rowId).setVisibility(View.VISIBLE);
			set(tfid, v);
		}
	}

	private void set(int tfid, String v) {
		if (popupcontent.findViewById(tfid) != null)
			((TextView) popupcontent.findViewById(tfid)).setText(v);
	}

}
