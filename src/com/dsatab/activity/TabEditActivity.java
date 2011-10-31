/**
 *  This file is part of Risk.
 *
 *  Risk is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Risk is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Risk.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.activity;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Spinner;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.fragment.ArtFragment;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.BodyFragment;
import com.dsatab.fragment.CharacterFragment;
import com.dsatab.fragment.DocumentsFragment;
import com.dsatab.fragment.FightFragment;
import com.dsatab.fragment.ItemsFragment;
import com.dsatab.fragment.ItemsListFragment;
import com.dsatab.fragment.MapFragment;
import com.dsatab.fragment.NotesFragment;
import com.dsatab.fragment.PurseFragment;
import com.dsatab.fragment.SpellFragment;
import com.dsatab.fragment.TalentFragment;
import com.dsatab.view.GlossyImageButton;
import com.gandulf.guilib.view.adapter.SpinnerSimpleAdapter;

public class TabEditActivity extends BaseActivity implements OnItemClickListener, OnClickListener,
		OnItemSelectedListener {

	public static final String INTENT_ICON = "icon";
	public static final String INTENT_PRIMARY_CLASS = "class1";
	public static final String INTENT_SECONDARY_CLASS = "class2";
	public static final String INTENT_TAB_INDEX = "index";
	public static final String INTENT_DICE_SLIDER = "diceslider";

	private GridView gridView;

	private Spinner spinner1, spinner2;

	private CheckBox diceslider;

	private List<String> activities;

	private List<Class<? extends BaseFragment>> activityValues;

	private int selectedPosition = 0;

	private int tabIndex = -1;

	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_edit_tab);

		List<Integer> avatars = DSATabApplication.getInstance().getConfiguration().getTabIcons();

		diceslider = (CheckBox) findViewById(R.id.popup_edit_diceslider);

		gridView = (GridView) findViewById(android.R.id.list);
		gridView.setAdapter(new AvatarAdaper(this, avatars));
		gridView.setOnItemClickListener(this);

		spinner1 = (Spinner) findViewById(R.id.popup_edit_primary);

		activities = Arrays.asList("Keine", "Charakterbogen", "Talente", "Zaubersprüche", "Künste", "Wunden",
				"Kampfschirm", "Ausrüstung (Bilder)", "Ausrüstung (Liste)", "Notizen", "Geldbörse", "Karte",
				"Dokumente");

		activityValues = Arrays.asList(null, CharacterFragment.class, TalentFragment.class, SpellFragment.class,
				ArtFragment.class, BodyFragment.class, FightFragment.class, ItemsFragment.class,
				ItemsListFragment.class, NotesFragment.class, PurseFragment.class, MapFragment.class,
				DocumentsFragment.class);

		SpinnerSimpleAdapter<String> adapter = new SpinnerSimpleAdapter<String>(this, activities);
		spinner1.setAdapter(adapter);
		spinner1.setOnItemSelectedListener(this);

		spinner2 = (Spinner) findViewById(R.id.popup_edit_secondary);
		spinner2.setAdapter(adapter);
		spinner2.setOnItemSelectedListener(this);

		Button ok = (Button) findViewById(R.id.popup_edit_ok);
		ok.setOnClickListener(this);
		Button cancel = (Button) findViewById(R.id.popup_edit_cancel);
		cancel.setOnClickListener(this);

		if (getIntent() != null) {
			int icon = getIntent().getIntExtra(INTENT_ICON, 0);
			selectedPosition = avatars.indexOf(icon);

			Class<? extends BaseFragment> clazz1 = (Class<? extends BaseFragment>) getIntent().getSerializableExtra(
					INTENT_PRIMARY_CLASS);
			spinner1.setSelection(activityValues.indexOf(clazz1));

			Class<? extends BaseFragment> clazz2 = (Class<? extends BaseFragment>) getIntent().getSerializableExtra(
					INTENT_SECONDARY_CLASS);
			spinner2.setSelection(activityValues.indexOf(clazz2));

			diceslider.setChecked(getIntent().getBooleanExtra(INTENT_DICE_SLIDER, true));

			tabIndex = getIntent().getIntExtra(INTENT_TAB_INDEX, -1);
		}
		if (selectedPosition < 0)
			selectedPosition = 0;

		gridView.setItemChecked(selectedPosition, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if (gridView.getChildAt(selectedPosition) != null)
			gridView.getChildAt(selectedPosition).setBackgroundResource(R.drawable.button_selected_patch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.popup_edit_ok:
			Intent data = new Intent();

			Class<? extends BaseFragment> clazz1 = activityValues.get(spinner1.getSelectedItemPosition());
			Class<? extends BaseFragment> clazz2 = activityValues.get(spinner2.getSelectedItemPosition());

			data.putExtra(INTENT_PRIMARY_CLASS, clazz1);
			data.putExtra(INTENT_SECONDARY_CLASS, clazz2);
			data.putExtra(INTENT_ICON, ((Integer) gridView.getItemAtPosition(selectedPosition)).intValue());
			data.putExtra(INTENT_DICE_SLIDER, diceslider.isChecked());
			data.putExtra(INTENT_TAB_INDEX, tabIndex);
			setResult(RESULT_OK, data);
			Util.hideKeyboard(v);
			finish();
			break;
		case R.id.popup_edit_cancel:
			setResult(RESULT_CANCELED);
			Util.hideKeyboard(v);
			finish();
			break;
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
	public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android
	 * .widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> adapter) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		selectedPosition = position;
		gridView.setItemChecked(position, true);
	}

	static class AvatarAdaper extends ArrayAdapter<Integer> {

		LayoutInflater inflater;

		public AvatarAdaper(Context context, List<Integer> objects) {
			super(context, 0, objects);

			inflater = LayoutInflater.from(getContext());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			GlossyImageButton imageButton = null;
			if (convertView instanceof GlossyImageButton) {
				imageButton = (GlossyImageButton) convertView;
			} else {
				imageButton = (GlossyImageButton) inflater.inflate(R.layout.hero_tab, parent, false);

			}
			imageButton.setFocusable(false);
			imageButton.setClickable(false);
			imageButton.setImageResource(getItem(position));

			return imageButton;
		}

	}

}