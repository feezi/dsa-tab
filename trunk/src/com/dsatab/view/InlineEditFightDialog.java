package com.dsatab.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.Value;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.NumberPicker;
import com.gandulf.guilib.view.OnViewChangedListener;

public class InlineEditFightDialog extends AlertDialog implements android.view.View.OnClickListener,
		Dialog.OnDismissListener, OnViewChangedListener<NumberPicker> {

	private CombatMeleeTalent talent;
	private Value valueTotal;

	private CombatMeleeAttribute valueAt;
	private CombatMeleeAttribute valuePa;

	private NumberPicker editText;
	private NumberPicker editAt;
	private NumberPicker editPa;
	private Button editReset, editOk;

	private TextView textFreeValue;

	public InlineEditFightDialog(Context context, CombatMeleeTalent value) {
		super(context);
		init();
		setValue(value);
	}

	public CombatMeleeTalent getValue() {
		return talent;
	}

	public void setValue(CombatMeleeTalent combatTalent) {
		this.talent = combatTalent;

		valueTotal = combatTalent;
		valueAt = combatTalent.getAttack();
		valuePa = combatTalent.getDefense();

		editText.setRange(valueTotal.getMinimum(), valueTotal.getMaximum());
		editText.setCurrent(valueTotal.getValue());

		if (valueAt != null) {
			editAt.setCurrent(valueAt.getValue());
			editAt.setRange(valueAt.getMinimum(), valueAt.getMaximum());
			editAt.setEnabled(true);
		} else {
			editAt.setEnabled(false);
		}

		if (valuePa != null) {
			editPa.setCurrent(valuePa.getValue());
			editPa.setRange(valuePa.getMinimum(), valuePa.getMaximum());
			editPa.setEnabled(true);
		} else {
			editPa.setEnabled(false);
		}
		editReset.setEnabled(valueTotal.getReferenceValue() != null);

		updateView();
	}

	private void updateView() {

		int free = editText.getCurrent();
		if (editAt.isEnabled()) {
			free -= (editAt.getCurrent() - valueAt.getBaseValue());
		}
		if (editPa.isEnabled()) {
			free -= (editPa.getCurrent() - valuePa.getBaseValue());
		}
		textFreeValue.setText(Util.toString(free));
	}

	public void onClick(View v) {
		if (v == editReset) {
			valueTotal.setValue(valueTotal.getReferenceValue());
			editText.setCurrent(valueTotal.getReferenceValue());
			if (valueAt != null) {
				valueAt.setValue(valueAt.getReferenceValue());
				editAt.setCurrent(valueAt.getReferenceValue());
			}
			if (valuePa != null) {
				valuePa.setValue(valuePa.getReferenceValue());
				editPa.setCurrent(valuePa.getReferenceValue());
			}
			dismiss();
		}
		if (v == editOk) {
			dismiss();
		}
	}

	private void init() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_edit_fight, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		editText = (NumberPicker) popupcontent.findViewById(R.id.popup_edit_text);
		editAt = (NumberPicker) popupcontent.findViewById(R.id.popup_edit_at);
		editPa = (NumberPicker) popupcontent.findViewById(R.id.popup_edit_pa);

		editText.setOnViewChangedListener(this);
		editAt.setOnViewChangedListener(this);
		editPa.setOnViewChangedListener(this);

		textFreeValue = (TextView) popupcontent.findViewById(R.id.popup_edit_free_value);

		editReset = (Button) popupcontent.findViewById(R.id.popup_edit_reset);
		editOk = (Button) popupcontent.findViewById(R.id.popup_edit_ok);

		OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					InlineEditFightDialog.this.dismiss();
					return true;
				}

				if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					if (v.getSelectionEnd() == 0) {
						v.getEditableText().delete(0, 0);
						return true;
					}
				}
				return false;
			}
		};
		editText.setOnEditorActionListener(editorActionListener);
		editPa.setOnEditorActionListener(editorActionListener);
		editAt.setOnEditorActionListener(editorActionListener);

		editReset.setOnClickListener(this);
		editOk.setOnClickListener(this);

	}

	public void onDismiss(DialogInterface dialog) {

		try {
			editText.validate();
			editAt.validate();
			editPa.validate();

			valueTotal.setValue(editText.getCurrent());
			if (valueAt != null)
				valueAt.setValue(editAt.getCurrent());
			if (valuePa != null)
				valuePa.setValue(editPa.getCurrent());

		} catch (NumberFormatException e) {
			Debug.error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gandulf.guilib.view.OnViewChangedListener#onChanged(android.view.
	 * View, int, int)
	 */
	@Override
	public void onChanged(NumberPicker picker, int oldVal, int newVal) {
		if (picker == editText && valueTotal != null)
			valueTotal.setValue(newVal);
		if (picker == editAt && valueAt != null)
			valueAt.setValue(newVal);
		if (picker == editPa && valuePa != null)
			valuePa.setValue(newVal);

		updateView();
	}

}
