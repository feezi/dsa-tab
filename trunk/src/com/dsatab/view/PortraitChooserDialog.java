package com.dsatab.view;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.activity.MainCharacterActivity;

public class PortraitChooserDialog extends AlertDialog implements AdapterView.OnItemClickListener {

	private MainCharacterActivity main;

	private List<URI> portraitPaths;

	public PortraitChooserDialog(MainCharacterActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainCharacterActivity getMain() {
		return main;
	}

	private void init() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setTitle("Wähle ein Portrait...");

		File mapDir = new File(DSATabApplication.getDsaTabPath(), "portraits");
		if (!mapDir.exists())
			mapDir.mkdirs();

		File[] files = mapDir.listFiles();
		if (files != null) {
			portraitPaths = new ArrayList<URI>(files.length);

			for (File file : files) {
				if (file.isFile()) {
					portraitPaths.add(file.toURI());
				}
			}
		}

		if (portraitPaths == null || portraitPaths.isEmpty()) {
			Toast.makeText(getContext(),
					"Keine Portraits gefunden. Kopiere sie auf deine SD-Karte unter dsatab/portraits",
					Toast.LENGTH_LONG).show();
			dismiss();
		}

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_portrait_chooser, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		final GridView list = (GridView) popupcontent.findViewById(R.id.popup_portrait_chooser_list);
		PortraitAdapter adapter = new PortraitAdapter(getContext(), R.layout.popup_portrait_chooser_item, portraitPaths);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		getMain().setPortraitFile(portraitPaths.get(position));
		dismiss();
	}

	class PortraitAdapter extends ArrayAdapter<URI> {

		public PortraitAdapter(Context context, int resource, int textViewResourceId, List<URI> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public PortraitAdapter(Context context, int resource, int textViewResourceId, URI[] objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public PortraitAdapter(Context context, int resource, int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}

		public PortraitAdapter(Context context, int textViewResourceId, List<URI> objects) {
			super(context, textViewResourceId, objects);
		}

		public PortraitAdapter(Context context, int textViewResourceId, URI[] objects) {
			super(context, textViewResourceId, objects);
		}

		public PortraitAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView tv = null;
			if (convertView instanceof ImageView) {
				tv = (ImageView) convertView;
			} else {
				tv = (ImageView) getLayoutInflater().inflate(R.layout.popup_portrait_chooser_item, null);
			}

			URI file = getItem(position);
			tv.setImageURI(Uri.parse(file.toString()));

			return tv;
		}
	}

}
