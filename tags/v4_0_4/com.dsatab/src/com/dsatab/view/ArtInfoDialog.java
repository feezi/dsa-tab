package com.dsatab.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.data.Art;
import com.dsatab.data.ArtInfo;

public class ArtInfoDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private Art art;

	private View popupcontent = null;

	public ArtInfoDialog(Context context) {
		super(context);
		init();
	}

	public Art getArt() {
		return art;
	}

	public void setArt(Art art) {
		this.art = art;

		ArtInfo info = art.getInfo();

		StyleableSpannableStringBuilder sb = new StyleableSpannableStringBuilder();

		sb.append(art.getFullName());
		if (art.hasFlag(Art.Flags.Begabung)) {
			sb.appendWithStyle(new RelativeSizeSpan(0.5f), " (Begabung)");
		}
		setTitle(sb);

		set(R.id.popup_liturgie_type, art.getType().getName());
		set(R.id.popup_liturgie_costs, art.getCosts());
		set(R.id.popup_liturgie_effect, art.getEffect());
		set(R.id.popup_liturgie_probe, art.getProbeInfo().toString());
		if (info != null) {
			set(R.id.popup_liturgie_castduration, info.getCastDurationDetailed());
			set(R.id.popup_liturgie_effectduration, info.getEffectDuration());
			set(R.id.popup_liturgie_origin, info.getOrigin());
			set(R.id.popup_liturgie_range, info.getRangeDetailed());
			set(R.id.popup_liturgie_target, info.getTargetDetailed());
			set(R.id.popup_liturgie_merkmal, info.getSource());
			set(R.id.popup_liturgie_source, info.getMerkmale());
		}

	}

	private void init() {
		setCanceledOnTouchOutside(true);

		popupcontent = (View) LayoutInflater.from(getContext()).inflate(R.layout.popup_art_info, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		setButton(AlertDialog.BUTTON_NEUTRAL, getContext().getString(R.string.label_ok), this);

		TableLayout table = (TableLayout) popupcontent.findViewById(R.id.popup_liturgie_table);

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

	private void set(int tfid, String v) {
		if (popupcontent.findViewById(tfid) != null) {
			((TextView) popupcontent.findViewById(tfid)).setText(v);
		}
	}

}
