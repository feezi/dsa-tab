package com.dsatab.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.Purse;
import com.dsatab.data.Purse.Currency;
import com.dsatab.data.Purse.PurseUnit;
import com.dsatab.data.Value;
import com.gandulf.guilib.view.NumberPicker;
import com.gandulf.guilib.view.OnViewChangedListener;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class PurseActivity extends BaseMainActivity implements OnItemSelectedListener,
		OnViewChangedListener<NumberPicker> {

	private Spinner currencySpinner;

	private NumberPicker[] picker;
	private TextView[] labels;

	private Purse purse;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_purse);
		super.onCreate(savedInstanceState);

		currencySpinner = (Spinner) findViewById(R.id.sp_currency);
		currencySpinner.setAdapter(new SpinnerSimpleAdapter<Currency>(this, Currency.values()));
		currencySpinner.setOnItemSelectedListener(this);

		picker = new NumberPicker[4];
		picker[0] = (NumberPicker) findViewById(R.id.popup_purse_dukat);
		picker[0].setOnViewChangedListener(this);
		picker[1] = (NumberPicker) findViewById(R.id.popup_purse_silver);
		picker[1].setOnViewChangedListener(this);
		picker[2] = (NumberPicker) findViewById(R.id.popup_purse_heller);
		picker[2].setOnViewChangedListener(this);
		picker[3] = (NumberPicker) findViewById(R.id.popup_purse_kreuzer);
		picker[3].setOnViewChangedListener(this);

		for (int i = 0; i < picker.length; i++) {
			picker[i].setRange(0, 9999);
		}

		labels = new TextView[4];
		labels[0] = (TextView) findViewById(R.id.tv_currency1);
		labels[1] = (TextView) findViewById(R.id.tv_currency2);
		labels[2] = (TextView) findViewById(R.id.tv_currency3);
		labels[3] = (TextView) findViewById(R.id.tv_currency4);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMainActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	protected void onHeroLoaded(Hero hero) {
		super.onHeroLoaded(hero);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.ValueChangedListener#onValueChanged(com.dsatab
	 * .data.Value)
	 */
	@Override
	public void onValueChanged(Value value) {

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

}
