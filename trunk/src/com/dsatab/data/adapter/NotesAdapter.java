package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.data.Event;

public class NotesAdapter extends ArrayAdapter<Event> {

	public NotesAdapter(Context context, int textViewResourceId, Event[] objects) {
		super(context, textViewResourceId, objects);

	}

	public NotesAdapter(Context context, int resource, int textViewResourceId, Event[] objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public NotesAdapter(Context context, int resource, int textViewResourceId, List<Event> objects) {
		super(context, resource, textViewResourceId, objects);

	}

	public NotesAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);

	}

	public NotesAdapter(Context context, int textViewResourceId, List<Event> objects) {
		super(context, textViewResourceId, objects);

	}

	public NotesAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		if (view instanceof TextView) {
			TextView editView = (TextView) view;
			Event e = getItem(position);

			if (e.getCategory() != null) {
				if (e.getAudioPath() != null)
					editView.setCompoundDrawablesWithIntrinsicBounds(e.getCategory().getDrawableId(), 0, android.R.drawable.ic_lock_silent_mode_off, 0);
				else
					editView.setCompoundDrawablesWithIntrinsicBounds(e.getCategory().getDrawableId(), 0, 0, 0);
			}
			editView.setText(e.getComment());

			if (position % 2 == 1) {
				editView.setBackgroundResource(R.color.RowOdd);
			} else {
				editView.setBackgroundResource(0);
			}
		}

		return view;
	}

}
