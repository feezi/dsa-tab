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

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.NotesEditActivity;
import com.dsatab.data.Event;
import com.dsatab.data.Hero;
import com.dsatab.data.NotesComparator;
import com.dsatab.data.adapter.EventCatgoryAdapter;
import com.dsatab.data.adapter.NotesAdapter;
import com.dsatab.data.enums.EventCategory;
import com.gandulf.guilib.util.Debug;

public class NotesFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	public static final int ACTION_EDIT = 1;

	private static final int CONTEXTMENU_DELETEITEM = 1;
	private static final int CONTEXTMENU_EDITITEM = 2;
	private static final int CONTEXTMENU_SORT_NOTES = 3;

	private MediaRecorder mediaRecorder;
	private MediaPlayer mediaPlayer;

	private File recordingsDir;

	private ListView listView;

	private Spinner notesCategorySpinner;

	private NotesAdapter notesListAdapter;

	private Event selectedEvent = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sheet_notes, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

		recordingsDir = new File(DSATabApplication.getDsaTabPath(), DSATabApplication.DIR_RECORDINGS);
		if (!recordingsDir.exists())
			recordingsDir.mkdirs();

		ImageButton speak = (ImageButton) findViewById(R.id.notes_btn_mic_add);
		speak.setOnClickListener(this);

		listView = (ListView) findViewById(android.R.id.list);
		// notes
		registerForContextMenu(listView);

		notesCategorySpinner = (Spinner) findViewById(R.id.notes_spn_category);
		notesCategorySpinner.setAdapter(new EventCatgoryAdapter(getActivity(), android.R.layout.simple_spinner_item,
				EventCategory.values()));
		ImageButton notesAddButton = (ImageButton) findViewById(R.id.notes_btn_add);
		notesAddButton.setOnClickListener(this);

		// notes
		listView.setOnItemClickListener(this);

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {
		notesListAdapter = new NotesAdapter(getActivity(), android.R.layout.simple_list_item_1, getHero().getEvents());
		listView.setAdapter(notesListAdapter);
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

	/**
	 * 
	 */
	private void initMediaPlayer() {
		// init player
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	private void initMediaRecorder() {
		// init recorder
		mediaRecorder = new MediaRecorder();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list) {
			menu.add(0, CONTEXTMENU_EDITITEM, 1, getString(R.string.menu_edit_item));
			menu.add(0, CONTEXTMENU_DELETEITEM, 2, getString(R.string.menu_delete_item));
			menu.add(0, CONTEXTMENU_SORT_NOTES, 3, getString(R.string.menu_sort_items));
		}

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CONTEXTMENU_DELETEITEM) {

			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			Event event = (Event) listView.getItemAtPosition(menuInfo.position);

			Debug.verbose("Deleting " + event.getComment());
			getHero().removeEvent(event);
			notesListAdapter.remove(event);
			notesListAdapter.notifyDataSetChanged();
			return true;
		} else if (item.getItemId() == CONTEXTMENU_EDITITEM) {

			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			Event event = (Event) listView.getItemAtPosition(menuInfo.position);

			Debug.verbose("Editing " + event.getComment());
			editEvent(event);
			return true;
		} else if (item.getItemId() == CONTEXTMENU_SORT_NOTES) {
			notesListAdapter.sort(new NotesComparator());
			notesListAdapter.notifyDataSetChanged();
			return true;
		}
		return super.onContextItemSelected(item);
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

		Event event = (Event) listView.getItemAtPosition(position);

		if (event.getAudioPath() != null) {

			try {
				if (mediaPlayer == null)
					initMediaPlayer();

				mediaPlayer.setDataSource(event.getAudioPath());
				mediaPlayer.prepare();
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.stop();
						mp.reset();
					}
				});
			} catch (IllegalArgumentException e) {
				Debug.error(e);
			} catch (IllegalStateException e) {
				Debug.error(e);
			} catch (IOException e) {
				Debug.error(e);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.notes_btn_mic_add) {

			try {
				final File currentAudio = new File(recordingsDir, "last.3gp");

				if (mediaRecorder == null)
					initMediaRecorder();

				mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

				mediaRecorder.setOutputFile(currentAudio.getAbsolutePath());
				mediaRecorder.prepare();
				mediaRecorder.start(); // Recording is now started

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

				builder.setIcon(android.R.drawable.ic_btn_speak_now);
				builder.setTitle(R.string.recording);
				builder.setMessage(R.string.recording_message);

				builder.setPositiveButton(R.string.label_save, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mediaRecorder.stop();
						mediaRecorder.reset();

						File nowAudio = new File(recordingsDir, System.currentTimeMillis() + ".3gp");
						currentAudio.renameTo(nowAudio);

						editEvent(null, nowAudio.getAbsolutePath());
					}
				});

				builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mediaRecorder.stop();
						mediaRecorder.reset();
						currentAudio.delete();
					}
				});

				builder.show();
			} catch (IllegalStateException e) {
				Debug.error(e);
			} catch (IOException e) {
				Debug.error(e);
			}

		} else if (v.getId() == R.id.notes_btn_add) {
			editEvent(null, null);
		}

	}

	private void editEvent(final Event event) {
		editEvent(event, event.getAudioPath());
	}

	private void editEvent(final Event event, final String audioPath) {

		selectedEvent = event;

		Intent intent = new Intent(getActivity(), NotesEditActivity.class);
		if (event != null) {
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_TEXT, event.getComment());
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_CATEGORY, event.getCategory());
		}
		if (audioPath != null) {
			intent.putExtra(NotesEditFragment.INTENT_NAME_AUDIO_PATH, audioPath);
		}
		startActivityForResult(intent, ACTION_EDIT);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ACTION_EDIT && resultCode == Activity.RESULT_OK) {

			String comment = data.getStringExtra(NotesEditFragment.INTENT_NAME_EVENT_TEXT);
			String audioPath = data.getStringExtra(NotesEditFragment.INTENT_NAME_AUDIO_PATH);
			EventCategory category = (EventCategory) data
					.getSerializableExtra(NotesEditFragment.INTENT_NAME_EVENT_CATEGORY);

			if (selectedEvent == null) {
				if (comment.trim().length() > 0) {
					selectedEvent = getHero().addEvent(category, comment, audioPath);
				}
			} else {
				if (comment.trim().length() > 0) {
					selectedEvent.setComment(comment.trim());
					selectedEvent.setAudioPath(audioPath);
					selectedEvent.setCategory(category);
				} else {
					getHero().removeEvent(selectedEvent);
				}
			}

			((ArrayAdapter<?>) listView.getAdapter()).notifyDataSetChanged();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onEventChanged(Event e) {
		((ArrayAdapter<?>) listView.getAdapter()).notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {

		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (mediaRecorder != null) {
			mediaRecorder.release();
			mediaRecorder = null;
		}

		super.onPause();
	}
}
