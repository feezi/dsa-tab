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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dsatab.R;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero.CombatStyle;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.view.NumberPicker;

public class InlineEditDialog extends AlertDialog implements android.view.View.OnClickListener,
		OnCheckedChangeListener, Dialog.OnDismissListener {

	private Value value;

	private NumberPicker editText;
	private TextView combatStyleText;
	private SeekBar editSeek;
	private Button editReset, editOk;
	private ToggleButton combatStyleBtn;

	public InlineEditDialog(Context context, Value value) {
		super(context);
		init();
		setValue(value);
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;

		if (value != null) {
			editSeek.setMax(value.getMaximum() - value.getMinimum());

			int currentValue = value.getValue();
			editText.setRange(value.getMinimum(), value.getMaximum());
			editText.setCurrent(currentValue);
			editSeek.setProgress(currentValue - value.getMinimum());

			int visible = View.GONE;
			if (value instanceof Attribute) {
				Attribute attr = (Attribute) value;
				if (attr.getType() == AttributeType.Behinderung) {
					visible = View.VISIBLE;
					combatStyleBtn.setChecked(attr.getHero().getCombatStyle() == CombatStyle.Offensive);
				}
			}

			combatStyleText.setVisibility(visible);
			combatStyleBtn.setVisibility(visible);

			editReset.setEnabled(value.getReferenceValue() != null);
		}
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == combatStyleBtn) {

			if (value instanceof Attribute) {
				Attribute attr = (Attribute) value;
				if (isChecked)
					attr.getHero().setCombatStyle(CombatStyle.Offensive);
				else
					attr.getHero().setCombatStyle(CombatStyle.Defensive);
			}
		}

	}

	public void onClick(View v) {
		if (v == editReset) {
			editText.setCurrent(value.getReferenceValue());
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

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.popup_edit,
				null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		editText = (NumberPicker) popupcontent.findViewById(R.id.popup_edit_text);
		editSeek = (SeekBar) popupcontent.findViewById(R.id.popup_edit_seek);
		editReset = (Button) popupcontent.findViewById(R.id.popup_edit_reset);
		editOk = (Button) popupcontent.findViewById(R.id.popup_edit_ok);

		combatStyleText = (TextView) popupcontent.findViewById(R.id.popup_edit_combat_style_label);
		combatStyleBtn = (ToggleButton) popupcontent.findViewById(R.id.popup_edit_combat_style);
		combatStyleBtn.setTextOn(getContext().getText(R.string.offensive));
		combatStyleBtn.setTextOff(getContext().getText(R.string.defensive));
		combatStyleBtn.setOnCheckedChangeListener(this);

		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					InlineEditDialog.this.dismiss();
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
		});

		editReset.setOnClickListener(this);
		editOk.setOnClickListener(this);

		editSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					editText.setCurrent(progress + value.getMinimum());
				}
			}
		});

	}

	public void onDismiss(DialogInterface dialog) {

		try {
			int currentValue = editText.getCurrent();
			value.setValue(currentValue);
		} catch (NumberFormatException e) {
			Debug.error(e);
		}
	}

}
