package com.dsatab.view;

import java.io.File;
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
import com.dsatab.common.Util;

public class PortraitChooserDialog extends AlertDialog implements AdapterView.OnItemClickListener {

	private MainActivity main;

	private List<Uri> portraitPaths;

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

		File portraitDir = DSATabApplication.getDirectory(DSATabApplication.DIR_PORTRAITS);
		if (!portraitDir.exists())
			portraitDir.mkdirs();

		File[] files = portraitDir.listFiles();
		if (files != null) {
			portraitPaths = new ArrayList<Uri>(files.length);

			for (File file : files) {
				if (file.isFile()) {
					portraitPaths.add(Uri.fromFile(file));
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
			return;
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

	class PortraitAdapter extends ArrayAdapter<Uri> {

		public PortraitAdapter(Context context, List<Uri> objects) {
			super(context, 0, objects);
		}

		public PortraitAdapter(Context context, Uri[] objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView tv = null;
			if (convertView instanceof ImageView) {
				tv = (ImageView) convertView;
			} else {
				tv = new ImageView(getContext());
				tv.setScaleType(ScaleType.CENTER_CROP);
			}

			Uri file = getItem(position);
			tv.setImageBitmap(Util.decodeBitmap(file, 200));

			return tv;
		}
	}

}
