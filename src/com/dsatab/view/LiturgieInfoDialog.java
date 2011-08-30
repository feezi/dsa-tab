package com.dsatab.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Liturgie;

public class LiturgieInfoDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private Liturgie liturgie;

	private View popupcontent = null;

	public LiturgieInfoDialog(Context context) {
		super(context);
		init();
	}

	public Liturgie getLiturgie() {
		return liturgie;
	}

	public void setLiturgie(Liturgie liturgie) {
		this.liturgie = liturgie;

		setTitle(liturgie.getFullName());
		set(R.id.popup_liturgie_costs, liturgie.getCosts());
		set(R.id.popup_liturgie_effect, liturgie.getEffect());
		set(R.id.popup_liturgie_probe, Util.toProbe(liturgie.getErschwernis()));
		set(R.id.popup_liturgie_castduration, liturgie.getCastDurationDetailed());
		set(R.id.popup_liturgie_effectduration, liturgie.getEffectDuration());
		set(R.id.popup_liturgie_origin, liturgie.getOrigin());
		set(R.id.popup_liturgie_range, liturgie.getRangeDetailed());
		set(R.id.popup_liturgie_target, liturgie.getTargetDetailed());

	}

	private void init() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setCanceledOnTouchOutside(true);

		popupcontent = (View) LayoutInflater.from(getContext()).inflate(R.layout.popup_liturgie_info, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		setButton(AlertDialog.BUTTON_NEUTRAL, getContext().getString(R.string.label_ok), this);

		TableLayout table = (TableLayout) popupcontent.findViewById(R.id.popup_liturgie_table);

		int childCount = table.getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (i % 2 == 1) {
				table.getChildAt(i).setBackgroundResource(R.color.RowOdd);
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

	private void set(int tfid, String v) {
		if (popupcontent.findViewById(tfid) != null)
			((TextView) popupcontent.findViewById(tfid)).setText(v);
	}

}
