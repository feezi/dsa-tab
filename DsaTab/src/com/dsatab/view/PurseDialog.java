package com.dsatab.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.activity.MainActivity;
import com.dsatab.data.Purse;
import com.dsatab.data.Purse.PurseUnit;

public class PurseDialog extends Dialog {

	private NumberPicker dukat, silver, heller, kreuzer;

	public PurseDialog(MainActivity context) {
		super(context, R.style.EditDialog);
		init();
	}

	@Override
	protected void onStart() {
		if (DSATabApplication.getInstance().getHero() != null) {
			Purse purse = DSATabApplication.getInstance().getHero().getPurse();

			dukat.setCurrent(purse.getCoins(PurseUnit.Dukat));
			silver.setCurrent(purse.getCoins(PurseUnit.Silbertaler));
			heller.setCurrent(purse.getCoins(PurseUnit.Heller));
			kreuzer.setCurrent(purse.getCoins(PurseUnit.Kreuzer));
		}

	}

	private void init() {
		setCanceledOnTouchOutside(true);

		setTitle("Geldbörse");

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.popup_purse,
				null, false);
		addContentView(popupcontent, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		dukat = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_dukat);
		silver = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_silver);
		heller = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_heller);
		kreuzer = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_kreuzer);

		setOnDismissListener(new Dialog.OnDismissListener() {

			public void onDismiss(DialogInterface dialog) {
				if (DSATabApplication.getInstance().getHero() != null) {
					Purse purse = DSATabApplication.getInstance().getHero().getPurse();

					purse.setCoins(PurseUnit.Dukat, dukat.getCurrent());
					purse.setCoins(PurseUnit.Silbertaler, silver.getCurrent());
					purse.setCoins(PurseUnit.Heller, heller.getCurrent());
					purse.setCoins(PurseUnit.Kreuzer, kreuzer.getCurrent());
				}
			}
		});
	}

}
