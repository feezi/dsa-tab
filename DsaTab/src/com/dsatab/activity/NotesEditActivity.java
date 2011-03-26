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
package com.dsatab.activity;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
public class NotesEditActivity extends Activity implements OnClickListener {

	public static final String INTENT_NAME_EVENT_CATEGORY = "eventCategory";

	public static final String INTENT_NAME_EVENT_TEXT = "eventText";

	public static final String INTENT_NAME_AUDIO_PATH = "audioPath";

	private EditText editText;
	private Spinner categorySpn;

	private String audioPath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_notes_edit);

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		editText = (EditText) findViewById(R.id.popup_notes_edit_text);
		categorySpn = (Spinner) findViewById(R.id.popup_notes_spn_category);

		categorySpn.setAdapter(new EventCatgoryAdapter(this, android.R.layout.simple_spinner_item, EventCategory
				.values()));

		EventCategory category = (EventCategory) getIntent().getSerializableExtra(INTENT_NAME_EVENT_CATEGORY);
		String event = getIntent().getStringExtra(INTENT_NAME_EVENT_TEXT);
		audioPath = getIntent().getStringExtra(INTENT_NAME_AUDIO_PATH);

		categorySpn.setSelection(Arrays.asList(EventCategory.values()).indexOf(category));
		editText.setText(event);

		// builder.setIcon(android.R.drawable.ic_menu_edit);
		// builder.setTitle("Notiz erstellen");

		Button saveButton = (Button) findViewById(R.id.popup_notes_save);
		Button cancelButton = (Button) findViewById(R.id.popup_notes_cancel);
		saveButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

	}

	public Hero getHero() {
		return DSATabApplication.getInstance().getHero();
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

			Intent intent = new Intent();
			intent.putExtra(INTENT_NAME_EVENT_TEXT, editText.getText().toString());
			intent.putExtra(INTENT_NAME_EVENT_CATEGORY, category);
			intent.putExtra(INTENT_NAME_AUDIO_PATH, audioPath);
			setResult(RESULT_OK, intent);
			finish();
		} else if (v.getId() == R.id.popup_notes_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		}

	}

}
