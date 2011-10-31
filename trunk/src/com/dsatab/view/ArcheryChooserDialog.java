package com.dsatab.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.MainActivity;
import com.dsatab.common.Util;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.SpecialFeature;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class ArcheryChooserDialog extends AlertDialog implements android.view.View.OnClickListener,
		DialogInterface.OnClickListener {

	private int[] distanceProbe;
	private int[] sizeProbe;
	private int[] modificationValues;

	private final int SCHNELLSCHUSS_INDEX = 13;

	private EquippedItem equippedItem;

	private Spinner distanceSpinner, sizeSpinner;

	private TextView text1, text2, probeValue;

	private Button btnOthers;

	private ImageButton iconLeft, iconRight;

	private AlertDialog othersDialog = null;

	private int erschwernis = 0;

	private int otherErschwernis = 0;

	private MainActivity main;

	public ArcheryChooserDialog(MainActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainActivity getMain() {
		return main;
	}

	public EquippedItem getWeapon() {
		return equippedItem;
	}

	public void setWeapon(EquippedItem weapon) {
		this.equippedItem = weapon;

		Item item = equippedItem.getItem();
		if (weapon != null) {
			text1.setText(item.getTitle());
			text2.setText(item.getInfo());
		}
		iconLeft.setImageResource(item.getResourceId());
		iconLeft.setOnClickListener(this);
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
		switch (which) {
		case BUTTON_POSITIVE:
			accept();
		case BUTTON_NEGATIVE:
			dismiss();
			break;
		}

	}

	private void accept() {
		CombatProbe combatProbe = new CombatProbe(main.getHero(), equippedItem, true);
		combatProbe.getProbeInfo().setErschwernis(erschwernis + otherErschwernis);

		dismiss();
		main.checkProbe(combatProbe);
	}

	public void onClick(View v) {
		if (v == iconLeft) {
			accept();
		} else if (v == btnOthers) {
			if (othersDialog == null)
				initOthersDialog();
			othersDialog.show();
		}
	}

	private void updateProbeValue() {
		erschwernis = 0;

		if (distanceSpinner.getSelectedItemPosition() != Spinner.INVALID_POSITION)
			erschwernis += distanceProbe[distanceSpinner.getSelectedItemPosition()];

		if (sizeSpinner.getSelectedItemPosition() != Spinner.INVALID_POSITION)
			erschwernis += sizeProbe[sizeSpinner.getSelectedItemPosition()];

		probeValue.setText(Util.toProbe(erschwernis)
				+ (otherErschwernis != 0 ? " " + Util.toProbe(otherErschwernis) : ""));
	}

	@Override
	protected void onStart() {

		String[] distances = getContext().getResources().getStringArray(R.array.archeryDistance);
		if (equippedItem != null && equippedItem.getItem().hasSpecification(DistanceWeapon.class)) {
			DistanceWeapon item = (DistanceWeapon) equippedItem.getItem().getSpecification(DistanceWeapon.class);

			String from, to;

			for (int i = 0; i < distances.length; i++) {
				to = item.getDistance(i);

				if (to != null) {
					distances[i] += " (";
					if (i > 0) {
						from = item.getDistance(i - 1);
						distances[i] += from;
					}

					distances[i] += " bis " + to + "m)";
				}
			}
		}

		SpinnerAdapter distanceAdapter = new SpinnerSimpleAdapter<String>(getContext(), distances);
		distanceSpinner.setAdapter(distanceAdapter);

		super.onStart();
	}

	private void init() {

		distanceProbe = getContext().getResources().getIntArray(R.array.archeryDistanceValues);
		sizeProbe = getContext().getResources().getIntArray(R.array.archerySizeValues);
		modificationValues = getContext().getResources().getIntArray(R.array.archeryModificationValues);

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_archery, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		distanceSpinner = (Spinner) popupcontent.findViewById(R.id.archery_distance);
		distanceSpinner.setPrompt("Entfernung");
		distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateProbeValue();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});
		sizeSpinner = (Spinner) popupcontent.findViewById(R.id.archery_size);
		sizeSpinner.setPrompt("Größe");
		sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateProbeValue();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		probeValue = (TextView) popupcontent.findViewById(R.id.archery_probe_value);

		text1 = (TextView) popupcontent.findViewById(android.R.id.text1);
		text2 = (TextView) popupcontent.findViewById(android.R.id.text2);
		text1.setTextColor(Color.parseColor("#dddddd"));
		text2.setTextColor(Color.parseColor("#dddddd"));

		iconLeft = (ImageButton) popupcontent.findViewById(android.R.id.icon1);
		iconLeft.setVisibility(View.VISIBLE);
		iconLeft.setFocusable(true);
		iconLeft.setClickable(true);
		iconRight = (ImageButton) popupcontent.findViewById(android.R.id.icon2);
		iconRight.setVisibility(View.GONE);

		btnOthers = (Button) popupcontent.findViewById(R.id.archery_others);
		btnOthers.setOnClickListener(this);

		setButton(BUTTON_POSITIVE, "Angreifen", this);
		setButton(BUTTON_NEGATIVE, getContext().getString(R.string.label_cancel), this);
	}

	private void initOthersDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.modifikatoren);
		builder.setPositiveButton(R.string.label_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				updateProbeValue();
			}
		});
		builder.setNegativeButton(R.string.label_cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				otherErschwernis = 0;
				updateProbeValue();
			}
		});

		String[] modificationStrings = getContext().getResources().getStringArray(R.array.archeryModificationStrings);
		if (getMain().getHero().hasFeature(SpecialFeature.MEISTERSCHUETZE)) {
			modificationStrings[SCHNELLSCHUSS_INDEX] = "Schnellschuß +0";
			modificationValues[SCHNELLSCHUSS_INDEX] = 0;

		} else if (getMain().getHero().hasFeature(SpecialFeature.SCHARFSCHUETZE)) {
			modificationStrings[SCHNELLSCHUSS_INDEX] = "Schnellschuß +1";
			modificationValues[SCHNELLSCHUSS_INDEX] = 1;
		}

		builder.setMultiChoiceItems(modificationStrings, null, new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if (isChecked)
					otherErschwernis += modificationValues[which];
				else {
					otherErschwernis -= modificationValues[which];
				}
			}
		});
		othersDialog = builder.create();
	}
}
