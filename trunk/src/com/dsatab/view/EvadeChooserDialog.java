package com.dsatab.view;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.MainFightActivity;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.enums.AttributeType;

public class EvadeChooserDialog extends AlertDialog implements android.view.View.OnClickListener,
		Dialog.OnDismissListener {

	private int[] distanceValues;
	private int[] enemyValues;
	private int[] modificationValues;

	private Spinner distanceSpinner, enemySpinner;

	private TextView text1, text2, probeValue;

	private Button btnOthers;

	private ImageButton iconLeft, iconRight;

	private AlertDialog othersDialog;

	private int erschwernis = 0;

	private int otherErschwernis = 0;

	private boolean doubleDK = false;

	private MainFightActivity main;

	public EvadeChooserDialog(MainFightActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainFightActivity getMain() {
		return main;
	}

	public void onClick(View v) {
		if (v == iconLeft) {

			Attribute ausweichen = main.getHero().getAttribute(AttributeType.Ausweichen);
			ausweichen.setErschwernis(erschwernis + otherErschwernis);

			dismiss();
			main.fillAusweichen();
			main.checkProbe(ausweichen);
		} else if (v == btnOthers) {
			if (othersDialog == null)
				initOthersDialog();
			othersDialog.show();
		}
	}

	private void updateProbeValue() {
		erschwernis = 0;

		if (distanceSpinner.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
			erschwernis += distanceValues[distanceSpinner.getSelectedItemPosition()];
			if (doubleDK) {
				erschwernis += distanceValues[distanceSpinner.getSelectedItemPosition()];
			}
		}
		if (enemySpinner.getSelectedItemPosition() != Spinner.INVALID_POSITION)
			erschwernis += enemyValues[enemySpinner.getSelectedItemPosition()];

		probeValue.setText(Util.toProbe(erschwernis)
				+ (otherErschwernis != 0 ? " " + Util.toProbe(otherErschwernis) : ""));
	}

	@Override
	protected void onStart() {

		Attribute ausweichen = main.getHero().getAttribute(AttributeType.Ausweichen);
		ausweichen.setErschwernis(0);

		if (ausweichen != null) {
			text1.setText(ausweichen.getName());
			text2.setText(getContext().getString(R.string.ausweichen_info, ausweichen.getValue(),
					ausweichen.getErschwernis()));
		}
		super.onStart();
	}

	private void init() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		distanceValues = getContext().getResources().getIntArray(R.array.evadeDistanceValues);
		enemyValues = getContext().getResources().getIntArray(R.array.evadeEnemyValues);
		modificationValues = getContext().getResources().getIntArray(R.array.evadeModificationValues);

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.popup_evade,
				null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		distanceSpinner = (Spinner) popupcontent.findViewById(R.id.evade_distance);
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
		enemySpinner = (Spinner) popupcontent.findViewById(R.id.evade_enemy);
		enemySpinner.setPrompt("Gegner");
		enemySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateProbeValue();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		probeValue = (TextView) popupcontent.findViewById(R.id.evade_probe_value);

		text1 = (TextView) popupcontent.findViewById(android.R.id.text1);
		text2 = (TextView) popupcontent.findViewById(android.R.id.text2);
		text1.setTextColor(Color.parseColor("#dddddd"));
		text2.setTextColor(Color.parseColor("#dddddd"));

		iconLeft = (ImageButton) popupcontent.findViewById(android.R.id.icon1);
		iconRight = (ImageButton) popupcontent.findViewById(android.R.id.icon2);

		iconLeft.setOnClickListener(this);
		iconLeft.setVisibility(View.VISIBLE);
		iconLeft.setClickable(true);
		iconLeft.setFocusable(true);
		iconLeft.setImageResource(R.drawable.icon_ausweichen);

		iconRight.setVisibility(View.GONE);

		setOnDismissListener(this);

		btnOthers = (Button) popupcontent.findViewById(R.id.evade_others);
		btnOthers.setOnClickListener(this);

	}

	public void onDismiss(DialogInterface dialog) {
		Attribute ausweichen = main.getHero().getAttribute(AttributeType.Ausweichen);
		ausweichen.setErschwernis(erschwernis + otherErschwernis);

		dismiss();
		main.fillAusweichen();
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

		String[] modificationStrings = getContext().getResources().getStringArray(R.array.evadeModificationStrings);

		builder.setMultiChoiceItems(modificationStrings, null, new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if (which == 0) {
					doubleDK = isChecked;
				} else {
					if (isChecked)
						otherErschwernis += modificationValues[which];
					else {
						otherErschwernis -= modificationValues[which];
					}
				}
			}
		});
		othersDialog = builder.create();
	}
}
