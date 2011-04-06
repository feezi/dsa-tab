package com.dsatab.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.activity.MainCharacterActivity;

public class PortraitChooserDialog extends Dialog {

	private MainCharacterActivity main;

	private List<File> portraitPaths;

	public PortraitChooserDialog(MainCharacterActivity context) {
		super(context, R.style.EditDialog);
		this.main = context;
		init();
	}

	public PortraitChooserDialog(MainCharacterActivity context, int theme) {
		super(context, theme);
		this.main = context;
		init();

	}

	protected MainCharacterActivity getMain() {
		return main;
	}

	private void init() {

		setTitle("Wähle ein Portrait...");

		File mapDir = new File(DSATabApplication.getDsaTabPath(), "portraits");
		if (!mapDir.exists())
			mapDir.mkdirs();

		File[] files = mapDir.listFiles();

		portraitPaths = new ArrayList<File>(files.length);

		for (File file : files) {
			if (file.isFile()) {

				portraitPaths.add(file);

			}
		}

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_portrait_chooser, null, false);
		addContentView(popupcontent, new LayoutParams(
				(int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.75), LayoutParams.WRAP_CONTENT));

		final GridView list = (GridView) popupcontent.findViewById(R.id.popup_portrait_chooser_list);
		PortraitAdapter adapter = new PortraitAdapter(getContext(), R.layout.popup_portrait_chooser_item, portraitPaths);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new PortraitChooserListener(this));
	}

	class PortraitAdapter extends ArrayAdapter<File> {

		public PortraitAdapter(Context context, int resource, int textViewResourceId, List<File> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public PortraitAdapter(Context context, int resource, int textViewResourceId, File[] objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public PortraitAdapter(Context context, int resource, int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}

		public PortraitAdapter(Context context, int textViewResourceId, List<File> objects) {
			super(context, textViewResourceId, objects);
		}

		public PortraitAdapter(Context context, int textViewResourceId, File[] objects) {
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

			File file = getItem(position);
			Drawable drawable = Drawable.createFromPath(file.getAbsolutePath());
			tv.setImageDrawable(drawable);

			return tv;
		}
	}

	class PortraitChooserListener implements AdapterView.OnItemClickListener {

		private Dialog dialog;

		public PortraitChooserListener(Dialog dialog) {
			this.dialog = dialog;
		}

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			getMain().setPortraitFile(portraitPaths.get(position).getName());
			dialog.dismiss();
		}

	}

}
