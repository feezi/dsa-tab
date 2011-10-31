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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.MainActivity;

public class PortraitChooserDialog extends AlertDialog implements AdapterView.OnItemClickListener {

	private MainActivity main;

	private List<URI> portraitPaths;

	public PortraitChooserDialog(MainActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainActivity getMain() {
		return main;
	}

	private void init() {
		setTitle("Wähle ein Portrait...");

		File portraitDir = new File(DSATabApplication.getDsaTabPath(), DSATabApplication.DIR_PORTRAITS);
		if (!portraitDir.exists())
			portraitDir.mkdirs();

		File[] files = portraitDir.listFiles();
		if (files != null) {
			portraitPaths = new ArrayList<URI>(files.length);

			for (File file : files) {
				if (file.isFile()) {
					portraitPaths.add(file.toURI());
				}
			}
		}

		if (portraitPaths == null || portraitPaths.isEmpty()) {

			String path = portraitDir.getAbsolutePath().replace(DSATabApplication.SD_CARD_PATH_PREFIX, "");

			Toast.makeText(
					getContext(),
					"Keine Portraits gefunden. Kopiere deine eigenen auf deine SD-Karte unter \"" + path
							+ "\" oder lade die Standardportraits in den Einstellungen herunter.", Toast.LENGTH_LONG)
					.show();
			dismiss();
		}

		setCanceledOnTouchOutside(true);

		View popupcontent = LayoutInflater.from(getContext()).inflate(R.layout.popup_portrait_chooser, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setView(popupcontent);

		final GridView list = (GridView) popupcontent.findViewById(R.id.popup_portrait_chooser_list);
		PortraitAdapter adapter = new PortraitAdapter(getContext(), portraitPaths);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		getMain().getHero().setPortraitUri(portraitPaths.get(position));
		dismiss();
	}

	class PortraitAdapter extends ArrayAdapter<URI> {

		public PortraitAdapter(Context context, List<URI> objects) {
			super(context, 0, objects);
		}

		public PortraitAdapter(Context context, URI[] objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView tv = null;
			if (convertView instanceof ImageView) {
				tv = (ImageView) convertView;
			} else {
				tv = new ImageView(getContext());
				tv.setScaleType(ScaleType.CENTER_INSIDE);
			}

			URI file = getItem(position);
			tv.setImageURI(Uri.parse(file.toString()));

			return tv;
		}
	}

}
