package com.dsatab.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.MainFightActivity;
import com.dsatab.common.Util;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.SpecialFeature;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class ArcheryChooserDialog extends AlertDialog implements android.view.View.OnClickListener {

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

	private MainFightActivity main;

	public ArcheryChooserDialog(MainFightActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainFightActivity getMain() {
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
		iconRight.setVisibility(View.GONE);
	}

	public void onClick(View v) {
		if (v == iconLeft) {
			CombatProbe combatProbe = new CombatProbe(main.getHero(), equippedItem, true);
			combatProbe.setErschwernis(erschwernis + otherErschwernis);
			dismiss();
			main.checkProbe(combatProbe);
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
		if (equippedItem != null && equippedItem.getItem() instanceof DistanceWeapon) {
			DistanceWeapon item = (DistanceWeapon) equippedItem.getItem();

			for (int i = 0; i < distances.length; i++) {
				distances[i] += " (";
				if (i > 0)
					distances[i] += item.getDistance(i - 1);
				distances[i] += " bis " + item.getDistance(i) + "m)";
			}
		}

		SpinnerAdapter distanceAdapter = new SpinnerSimpleAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, distances);
		distanceSpinner.setAdapter(distanceAdapter);

		super.onStart();
	}

	private void init() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

		iconLeft = (ImageButton) popupcontent.findViewById(R.id.icon_left);
		iconRight = (ImageButton) popupcontent.findViewById(R.id.icon_right);

		btnOthers = (Button) popupcontent.findViewById(R.id.archery_others);
		btnOthers.setOnClickListener(this);
	}

	private void initOthersDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.modifikatoren);
		builder.setPositiveButton("Ok", new OnClickListener() {
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
