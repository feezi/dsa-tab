package com.dsatab.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.MainActivity;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.enums.AttributeType;

public class EvadeChooserDialog extends AlertDialog implements android.view.View.OnClickListener,
		DialogInterface.OnClickListener, OnItemClickListener {

	private int[] distanceValues;
	private int[] enemyValues;
	private int[] modificationValues;

	private Spinner distanceSpinner, enemySpinner;

	private TextView text1, text2;

	private int erschwernis = 0;

	private int otherErschwernis = 0;

	private boolean doubleDK = false;

	private ListView othersList;

	private MainActivity main;

	public EvadeChooserDialog(MainActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainActivity getMain() {
		return main;
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
			break;
		case BUTTON_NEGATIVE:
			dismiss();
			break;
		}

	}

	protected void accept() {
		Attribute ausweichen = main.getHero().getAttribute(AttributeType.Ausweichen);
		ausweichen.getProbeInfo().setErschwernis(erschwernis + otherErschwernis);

		main.getHero().fireValueChangedEvent(ausweichen);
		dismiss();
		main.checkProbe(ausweichen);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case android.R.id.icon1:
			accept();
			break;
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

		text2.setText("Modifikator " + Util.toProbe(erschwernis)
				+ (otherErschwernis != 0 ? " " + Util.toProbe(otherErschwernis) : ""));
	}

	@Override
	protected void onStart() {

		Attribute ausweichen = main.getHero().getAttribute(AttributeType.Ausweichen);
		ausweichen.getProbeInfo().setErschwernis(0);

		if (ausweichen != null) {
			StyleableSpannableStringBuilder title = new StyleableSpannableStringBuilder();
			title.append(ausweichen.getName());
			Util.appendValue(main.getHero(), title, ausweichen, null, true);
			text1.setText(title);
			text2.setText("Modifikator " + ausweichen.getProbeInfo().getErschwernis());
		}
		super.onStart();
	}

	private void init() {
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

		text1 = (TextView) popupcontent.findViewById(android.R.id.text1);
		text2 = (TextView) popupcontent.findViewById(android.R.id.text2);

		ImageButton iconLeft = (ImageButton) popupcontent.findViewById(android.R.id.icon1);
		iconLeft.setOnClickListener(this);
		iconLeft.setVisibility(View.VISIBLE);
		iconLeft.setClickable(true);
		iconLeft.setFocusable(true);
		iconLeft.setImageResource(R.drawable.icon_ausweichen);

		popupcontent.findViewById(android.R.id.icon2).setVisibility(View.GONE);

		othersList = (ListView) popupcontent.findViewById(R.id.evade_others);

		String[] modificationStrings = getContext().getResources().getStringArray(R.array.evadeModificationStrings);

		othersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		othersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_multiple_choice,
				modificationStrings));

		othersList.setOnItemClickListener(this);

		setButton(BUTTON_POSITIVE, "Ausweichen", this);
		setButton(BUTTON_NEGATIVE, getContext().getString(R.string.label_cancel), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		CheckedTextView checkedText = (CheckedTextView) view;
		// the state seems to geet updated after this call, so invert it to get
		// the correct value here
		boolean checked = !checkedText.isChecked();
		if (position == 0) {
			doubleDK = checked;
		} else {
			if (checked)
				otherErschwernis += modificationValues[position];
			else {
				otherErschwernis -= modificationValues[position];
			}
		}
		updateProbeValue();
	}
}
