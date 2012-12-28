/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.EventCatgoryAdapter;
import com.dsatab.data.enums.EventCategory;
import com.dsatab.util.Util;

/**
 * @author Seraphim
 * 
 */
public class NotesEditFragment extends BaseFragment implements OnItemSelectedListener {

	public static final String INTENT_NAME_EVENT_CATEGORY = "eventCategory";
	public static final String INTENT_NAME_EVENT_TEXT = "eventText";
	public static final String INTENT_NAME_EVENT_NAME = "eventNAme";
	public static final String INTENT_NAME_EVENT_SOZIALSTATUS = "eventSo";

	public static final String INTENT_NAME_AUDIO_PATH = "audioPath";

	private EventCatgoryAdapter categoryAdapter;

	private EditText editComment;
	private EditText editName;
	private EditText editSozialStatus;

	private Spinner categorySpn;

	private EventCategory category;

	private String audioPath;

	private OnNotesEditListener onNotesEditListener;

	public interface OnNotesEditListener {

		public void onNoteSaved(Bundle data);

		public void onNoteCanceled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return configureContainerView(inflater.inflate(R.layout.sheet_notes_edit, container, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnNotesEditListener) {
			onNotesEditListener = (OnNotesEditListener) activity;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		onNotesEditListener = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

		editComment = (EditText) findViewById(R.id.popup_notes_edit_text);
		editName = (EditText) findViewById(R.id.popup_notes_edit_name);
		editSozialStatus = (EditText) findViewById(R.id.popup_notes_edit_so);

		categorySpn = (Spinner) findViewById(R.id.popup_notes_spn_category);

		List<EventCategory> categories = new ArrayList<EventCategory>(Arrays.asList(EventCategory.values()));
		categories.remove(EventCategory.Heldensoftware);
		categoryAdapter = new EventCatgoryAdapter(getActivity(), android.R.layout.simple_spinner_item, categories);

		categorySpn.setAdapter(categoryAdapter);
		categorySpn.setOnItemSelectedListener(this);

		Bundle extra = getActivity().getIntent().getExtras();
		if (extra != null) {
			category = (EventCategory) extra.getSerializable(INTENT_NAME_EVENT_CATEGORY);
			if (category == null)
				category = EventCategory.Misc;

			String event = extra.getString(INTENT_NAME_EVENT_TEXT);
			String name = extra.getString(INTENT_NAME_EVENT_NAME);
			String sozial = extra.getString(INTENT_NAME_EVENT_SOZIALSTATUS);

			audioPath = extra.getString(INTENT_NAME_AUDIO_PATH);
			if (category == EventCategory.Heldensoftware)
				categorySpn.setVisibility(View.GONE);
			else
				categorySpn.setSelection(categoryAdapter.getPosition(category));

			editComment.setText(event);
			editName.setText(name);
			editSozialStatus.setText(sozial);
		}

		updateView();

		super.onActivityCreated(savedInstanceState);
	}

	private void updateView() {
		if (category != null) {

			if (category == EventCategory.Bekanntschaft)
				editSozialStatus.setVisibility(View.VISIBLE);
			else
				editSozialStatus.setVisibility(View.GONE);

			if (category.hasName())
				editName.setVisibility(View.VISIBLE);
			else
				editName.setVisibility(View.GONE);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		if (parent.getId() == R.id.popup_notes_spn_category) {
			category = (EventCategory) categorySpn.getSelectedItem();
			updateView();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android
	 * .widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		category = null;
		updateView();
	}

	/**
	 * 
	 */
	public void cancel() {
		if (onNotesEditListener != null)
			onNotesEditListener.onNoteCanceled();
	}

	/**
	 * 
	 */
	public Bundle accept() {
		EditText editText = (EditText) findViewById(R.id.popup_notes_edit_text);

		Bundle data = new Bundle(5);
		data.putString(INTENT_NAME_EVENT_TEXT, editText.getText().toString());
		data.putString(INTENT_NAME_EVENT_NAME, editName.getText().toString());
		data.putString(INTENT_NAME_EVENT_SOZIALSTATUS, editSozialStatus.getText().toString());
		data.putSerializable(INTENT_NAME_EVENT_CATEGORY, category);
		data.putString(INTENT_NAME_AUDIO_PATH, audioPath);
		if (onNotesEditListener != null) {
			onNotesEditListener.onNoteSaved(data);
		}
		Util.hideKeyboard(editText);

		return data;

	}

}
