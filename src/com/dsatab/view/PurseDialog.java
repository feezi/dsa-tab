package com.dsatab.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.data.Hero;
import com.dsatab.data.Purse;
import com.dsatab.data.Purse.Currency;
import com.dsatab.data.Purse.PurseUnit;
import com.gandulf.guilib.view.NumberPicker;
import com.gandulf.guilib.view.OnViewChangedListener;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class PurseDialog extends AlertDialog implements OnItemSelectedListener, OnViewChangedListener<NumberPicker> {

	private Spinner currencySpinner;

	private NumberPicker[] picker;
	private TextView[] labels;

	private Purse purse;

	private Hero hero;

	public PurseDialog(Context context, Hero hero) {
		super(context);
		this.hero = hero;
		init();
	}

	@Override
	protected void onStart() {
		if (DSATabApplication.getInstance().getHero() != null) {
			purse = hero.getPurse();

			if (purse.getActiveCurrency() == null) {
				purse.setActiveCurrency(Currency.Mittelreich);
			}

			int index = -1;
			Currency[] values = Currency.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].equals(purse.getActiveCurrency())) {
					index = i;
					break;
				}
			}
			currencySpinner.setSelection(index);

			updateCurrency(purse.getActiveCurrency());

		}

	}

	private void updateCurrency(Currency c) {

		PurseUnit[] units = c.units();
		for (int i = 0; i < units.length; i++) {
			picker[i].setVisibility(View.VISIBLE);
			picker[i].setTag(units[i]);
			picker[i].setCurrent(purse.getCoins(units[i]));

			labels[i].setVisibility(View.VISIBLE);
			labels[i].setText(units[i].xmlName());
		}

		for (int i = units.length; i < 4; i++) {
			picker[i].setVisibility(View.GONE);
			picker[i].setTag(null);
			labels[i].setVisibility(View.GONE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android
	 * .widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Currency cur = (Currency) parent.getItemAtPosition(position);
		purse.setActiveCurrency(cur);
		updateCurrency(cur);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android
	 * .widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {

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
		PurseUnit unit = (PurseUnit) picker.getTag();
		purse.setCoins(unit, picker.getCurrent());
	}

	private void init() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setCanceledOnTouchOutside(true);

		setTitle("Geldbörse");

		final RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_purse, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		setView(popupcontent);

		currencySpinner = (Spinner) popupcontent.findViewById(R.id.sp_currency);
		currencySpinner.setAdapter(new SpinnerSimpleAdapter<Currency>(getContext(), Currency.values()));
		currencySpinner.setOnItemSelectedListener(this);

		picker = new NumberPicker[4];
		picker[0] = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_dukat);
		picker[0].setOnViewChangedListener(this);
		picker[1] = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_silver);
		picker[1].setOnViewChangedListener(this);
		picker[2] = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_heller);
		picker[2].setOnViewChangedListener(this);
		picker[3] = (NumberPicker) popupcontent.findViewById(R.id.popup_purse_kreuzer);
		picker[3].setOnViewChangedListener(this);

		for (int i = 0; i < picker.length; i++) {
			picker[i].setRange(0, 9999);
		}

		labels = new TextView[4];
		labels[0] = (TextView) popupcontent.findViewById(R.id.tv_currency1);
		labels[1] = (TextView) popupcontent.findViewById(R.id.tv_currency2);
		labels[2] = (TextView) popupcontent.findViewById(R.id.tv_currency3);
		labels[3] = (TextView) popupcontent.findViewById(R.id.tv_currency4);

	}

}
