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

import java.util.Arrays;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.EventCatgoryAdapter;
import com.dsatab.data.enums.EventCategory;

/**
 * @author Seraphim
 * 
 */
public class NotesEditFragment extends BaseFragment implements OnClickListener {

	public static final String INTENT_NAME_EVENT_CATEGORY = "eventCategory";

	public static final String INTENT_NAME_EVENT_TEXT = "eventText";

	public static final String INTENT_NAME_AUDIO_PATH = "audioPath";

	private EditText editText;
	private Spinner categorySpn;

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
		return inflater.inflate(R.layout.sheet_notes_edit, container, false);
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

		editText = (EditText) findViewById(R.id.popup_notes_edit_text);
		categorySpn = (Spinner) findViewById(R.id.popup_notes_spn_category);

		categorySpn.setAdapter(new EventCatgoryAdapter(getActivity(), android.R.layout.simple_spinner_item,
				EventCategory.values()));

		Bundle extra = getActivity().getIntent().getExtras();
		if (extra != null) {
			EventCategory category = (EventCategory) extra.getSerializable(INTENT_NAME_EVENT_CATEGORY);
			String event = extra.getString(INTENT_NAME_EVENT_TEXT);
			audioPath = extra.getString(INTENT_NAME_AUDIO_PATH);
			categorySpn.setSelection(Arrays.asList(EventCategory.values()).indexOf(category));
			editText.setText(event);
		}

		// builder.setIcon(android.R.drawable.ic_menu_edit);
		// builder.setTitle("Notiz erstellen");

		Button saveButton = (Button) findViewById(R.id.popup_notes_save);
		Button cancelButton = (Button) findViewById(R.id.popup_notes_cancel);
		saveButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

		super.onActivityCreated(savedInstanceState);
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
	 * com.dsatab.fragment.BaseFragment#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroUnloaded(Hero hero) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.popup_notes_save) {

			final EventCategory category = (EventCategory) categorySpn.getSelectedItem();

			EditText editText = (EditText) findViewById(R.id.popup_notes_edit_text);

			if (onNotesEditListener != null) {
				Bundle data = new Bundle(3);
				data.putString(INTENT_NAME_EVENT_TEXT, editText.getText().toString());
				data.putSerializable(INTENT_NAME_EVENT_CATEGORY, category);
				data.putString(INTENT_NAME_AUDIO_PATH, audioPath);

				onNotesEditListener.onNoteSaved(data);
			}

		} else if (v.getId() == R.id.popup_notes_cancel) {
			if (onNotesEditListener != null)
				onNotesEditListener.onNoteCanceled();

		}

	}

}
