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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.commonsware.cwac.merge.MergeAdapter;
import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.NotesEditActivity;
import com.dsatab.data.Connection;
import com.dsatab.data.Event;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.ConnectionAdapter;
import com.dsatab.data.adapter.EventAdapter;
import com.dsatab.data.enums.EventCategory;
import com.dsatab.util.Debug;

public class NotesFragment extends BaseFragment implements OnItemClickListener, OnMultiChoiceClickListener {

	public static final int ACTION_EDIT = 1;

	private static final int GROUP_NOTES = 2;

	private static final int CONTEXTMENU_DELETEITEM = 1;
	private static final int CONTEXTMENU_EDITITEM = 2;

	private MediaRecorder mediaRecorder;
	private MediaPlayer mediaPlayer;

	private File recordingsDir;

	private ListView listView;

	private MergeAdapter mergeAdapter;
	private EventAdapter notesListAdapter;
	private ConnectionAdapter connectionsAdapter;

	private Set<EventCategory> categoriesSelected;
	private EventCategory[] categories;

	private Object selectedObject = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_note_add, Menu.NONE,
				"Notiz hinzufügen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		item.setIcon(R.drawable.ic_menu_add);

		item = menu.add(Menu.NONE, R.id.option_note_record, Menu.NONE, "Notiz aufnehmen");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		item.setIcon(R.drawable.ic_menu_mic);

		item = menu.add(Menu.NONE, R.id.option_note_filter, Menu.NONE, "Filtern");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		item.setIcon(R.drawable.ic_menu_filter);

		super.onCreateOptionsMenu(menu, inflater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.
	 * actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		if (item.getItemId() == R.id.option_note_add) {
			editEvent(null, null);
			return true;
		} else if (item.getItemId() == R.id.option_note_record) {
			recordEvent();
			return true;
		} else if (item.getItemId() == R.id.option_note_filter) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			String[] categoryNames = new String[categories.length];
			boolean[] categoriesSet = new boolean[categories.length];

			for (int i = 0; i < categories.length; i++) {
				categoryNames[i] = categories[i].name();
				if (categoriesSelected.contains(categories[i]))
					categoriesSet[i] = true;
			}

			builder.setMultiChoiceItems(categoryNames, categoriesSet, this);
			builder.setTitle("Filtern");
			builder.setIcon(R.drawable.ic_menu_search);

			builder.show().setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					notesListAdapter.filter(null, new ArrayList<EventCategory>(categoriesSelected));
					connectionsAdapter.filter(null, new ArrayList<EventCategory>(categoriesSelected));
				}
			});
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
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
		return configureContainerView(inflater.inflate(R.layout.sheet_notes, container, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

		recordingsDir = DSATabApplication.getDirectory(DSATabApplication.DIR_RECORDINGS);
		if (!recordingsDir.exists())
			recordingsDir.mkdirs();

		listView = (ListView) findViewById(android.R.id.list);
		// notes
		registerForContextMenu(listView);

		// notes
		listView.setOnItemClickListener(this);

		categories = EventCategory.values();
		categoriesSelected = new HashSet<EventCategory>(Arrays.asList(categories));
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
		notesListAdapter = new EventAdapter(getActivity(), getHero().getEvents());

		connectionsAdapter = new ConnectionAdapter(getActivity(), getHero().getConnections());

		mergeAdapter = new MergeAdapter();
		mergeAdapter.addAdapter(notesListAdapter);
		mergeAdapter.addAdapter(connectionsAdapter);

		listView.setAdapter(mergeAdapter);
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

			menu.add(GROUP_NOTES, CONTEXTMENU_EDITITEM, 1, getString(R.string.menu_edit_item));
			menu.add(GROUP_NOTES, CONTEXTMENU_DELETEITEM, 2, getString(R.string.menu_delete_item));

			if (menuInfo instanceof AdapterContextMenuInfo) {
				AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
				Object obj = listView.getItemAtPosition(adapterMenuInfo.position);

				if (obj instanceof Event) {
					Event event = (Event) obj;
					if (event.getCategory() == EventCategory.Heldensoftware) {
						menu.findItem(CONTEXTMENU_DELETEITEM).setEnabled(false);
					}
				}
			}
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

		if (item.getGroupId() == GROUP_NOTES) {
			if (item.getMenuInfo() instanceof AdapterContextMenuInfo) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				Object obj = listView.getItemAtPosition(menuInfo.position);

				if (obj instanceof Event) {
					Event event = (Event) obj;
					if (item.getItemId() == CONTEXTMENU_DELETEITEM) {
						Debug.verbose("Deleting " + event.getComment());
						getHero().removeEvent(event);
						notesListAdapter.remove(event);
						notesListAdapter.notifyDataSetChanged();
						return true;
					} else if (item.getItemId() == CONTEXTMENU_EDITITEM) {
						Debug.verbose("Editing " + event.getComment());
						editEvent(event);
						return true;
					}
				} else if (obj instanceof Connection) {
					Connection connection = (Connection) obj;
					if (item.getItemId() == CONTEXTMENU_DELETEITEM) {
						Debug.verbose("Deleting " + connection.getName());
						getHero().removeConnection(connection);
						connectionsAdapter.remove(connection);
						return true;
					} else if (item.getItemId() == CONTEXTMENU_EDITITEM) {
						Debug.verbose("Editing " + connection.getName());
						editConnection(connection);
						return true;
					}
				}
			}
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

		Object obj = listView.getItemAtPosition(position);

		if (obj instanceof Event) {
			Event event = (Event) obj;

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
	}

	private void recordEvent() {
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
					if (mediaRecorder != null) {
						mediaRecorder.stop();
						mediaRecorder.reset();
					}

					File nowAudio = new File(recordingsDir, System.currentTimeMillis() + ".3gp");
					currentAudio.renameTo(nowAudio);

					editEvent(null, nowAudio.getAbsolutePath());
				}
			});

			builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mediaRecorder != null) {
						mediaRecorder.stop();
						mediaRecorder.reset();
					}
					currentAudio.delete();
				}
			});

			builder.show();
		} catch (IllegalStateException e) {
			Debug.error(e);
		} catch (IOException e) {
			Debug.error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnMultiChoiceClickListener#onClick(android
	 * .content.DialogInterface, int, boolean)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (isChecked)
			categoriesSelected.add(categories[which]);
		else
			categoriesSelected.remove(categories[which]);
	}

	private void editEvent(final Event event) {
		editEvent(event, event.getAudioPath());
	}

	private void editEvent(final Event event, final String audioPath) {

		selectedObject = event;

		Intent intent = new Intent(getActivity(), NotesEditActivity.class);
		if (event != null) {
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_TEXT, event.getComment());
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_NAME, event.getName());
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_CATEGORY, event.getCategory());
		}
		if (audioPath != null) {
			intent.putExtra(NotesEditFragment.INTENT_NAME_AUDIO_PATH, audioPath);
		}
		startActivityForResult(intent, ACTION_EDIT);

	}

	private void editConnection(final Connection event) {
		selectedObject = event;
		Intent intent = new Intent(getActivity(), NotesEditActivity.class);
		if (event != null) {
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_TEXT, event.getDescription());
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_NAME, event.getName());
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_SOZIALSTATUS, event.getSozialStatus());
			intent.putExtra(NotesEditFragment.INTENT_NAME_EVENT_CATEGORY, event.getCategory());
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
			String name = data.getStringExtra(NotesEditFragment.INTENT_NAME_EVENT_NAME);
			String sozialstatus = data.getStringExtra(NotesEditFragment.INTENT_NAME_EVENT_SOZIALSTATUS);
			String audioPath = data.getStringExtra(NotesEditFragment.INTENT_NAME_AUDIO_PATH);
			EventCategory category = (EventCategory) data
					.getSerializableExtra(NotesEditFragment.INTENT_NAME_EVENT_CATEGORY);

			if (category == EventCategory.Bekanntschaft) {
				if (selectedObject instanceof Event) {
					getHero().removeEvent((Event) selectedObject);
					notesListAdapter.remove((Event) selectedObject);
					selectedObject = null;
					notesListAdapter.notifyDataSetChanged();
				}

				if (selectedObject instanceof Connection) {
					Connection selectedEvent = (Connection) selectedObject;
					selectedEvent.setDescription(comment.trim());
					selectedEvent.setName(name);
					selectedEvent.setSozialStatus(sozialstatus);
				} else if (selectedObject == null) {
					Connection connection = new Connection();
					connection.setName(name);
					connection.setDescription(comment);
					connection.setSozialStatus(sozialstatus);
					getHero().addConnection(connection);
					connectionsAdapter.add(connection);
				}

				connectionsAdapter.sort(Connection.NAME_COMPARATOR);
				connectionsAdapter.refilter();
				connectionsAdapter.notifyDataSetChanged();

			} else {
				if (selectedObject instanceof Connection) {
					getHero().removeConnection((Connection) selectedObject);
					connectionsAdapter.remove((Connection) selectedObject);
					selectedObject = null;
					connectionsAdapter.notifyDataSetChanged();
				}

				if (selectedObject instanceof Event) {
					Event selectedEvent = (Event) selectedObject;
					selectedEvent.setName(name);
					selectedEvent.setComment(comment);
					selectedEvent.setAudioPath(audioPath);
					selectedEvent.setCategory(category);
				} else if (selectedObject == null) {
					Event selectedEvent = new Event();
					selectedEvent.setName(name);
					selectedEvent.setCategory(category);
					selectedEvent.setComment(comment);
					selectedEvent.setAudioPath(audioPath);
					getHero().addEvent(selectedEvent);
					notesListAdapter.add(selectedEvent);
				}

				notesListAdapter.sort(Event.COMPARATOR);
				notesListAdapter.refilter();
				notesListAdapter.notifyDataSetChanged();
			}

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
