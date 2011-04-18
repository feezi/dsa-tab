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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.common.WrapMotionEvent;
import com.dsatab.data.Value;

public class MapActivity extends BaseMainActivity implements OnTouchListener {

	private static final String PREF_KEY_LAST_MAP_COORDINATES = "lastMapCoordinates";
	private static final String PREF_KEY_LAST_MAP = "lastMap";

	public enum TouchMode {
		None, Drag, Zoom;
	}

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	private TouchMode mode = TouchMode.None;

	// Points for distance calc...
	private PointF start = new PointF();
	private PointF middle = new PointF();

	private float oldDistance = 1f;

	private String[] mapFiles;
	private String[] mapNames;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.activity.BaseMainActivity#setupDiceSilder()
	 */
	@Override
	protected void setupDiceSilder() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_hero_maps);

		super.onCreate(savedInstanceState);
		ImageView view = (ImageView) findViewById(R.id.imageView);
		view.setOnTouchListener(this);

		List<String> mapFiles = new ArrayList<String>();
		List<String> mapNames = new ArrayList<String>();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		File mapDir = new File(DSATabApplication.getDsaTabPath(), "maps");
		if (!mapDir.exists())
			mapDir.mkdirs();

		File[] files = mapDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					mapFiles.add(file.getName());
					mapNames.add(file.getName().replace("-", " ").substring(0, file.getName().length() - 4));
				}
			}
		}

		if (mapFiles.isEmpty()) {
			String path = mapDir.getAbsolutePath();
			path = path.replace("/sdcard/", "");
			Toast.makeText(
					this,
					"Kein Kartenmaterial gefunden. Scan deine Karten ein und kopiere sie auf deine SD-Karte unter "
							+ path, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		this.mapFiles = mapFiles.toArray(new String[0]);
		this.mapNames = mapNames.toArray(new String[0]);

		String lastMap = preferences.getString(PREF_KEY_LAST_MAP, null);

		if (lastMap == null && !mapFiles.isEmpty()) {
			lastMap = mapFiles.get(0);
			// Work around a Cupcake bug
			matrix.setTranslate(1f, 1f);
		}

		if (lastMap != null) {
			Bitmap bm = BitmapFactory.decodeFile(DSATabApplication.getDsaTabPath() + "maps/" + lastMap);
			view.setImageBitmap(bm);

			String coords = preferences.getString(PREF_KEY_LAST_MAP_COORDINATES, null);
			if (coords != null) {
				matrix.setValues(Util.parseFloats(coords));
			}
		}
		view.setImageMatrix(matrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.view.listener.ValueChangedListener#onValueChanged(com.dsatab
	 * .data.Value)
	 */
	@Override
	public void onValueChanged(Value value) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();

		float[] values = new float[9];
		matrix.getValues(values);
		edit.putString(PREF_KEY_LAST_MAP_COORDINATES, Util.toString(values));
		edit.commit();

		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.option_choose_map) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Karte auswählen");
			builder.setItems(mapNames, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ImageView view = (ImageView) findViewById(R.id.imageView);

					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MapActivity.this);

					Editor edit = preferences.edit();
					edit.putString(PREF_KEY_LAST_MAP, mapFiles[which]);
					edit.commit();

					Bitmap bm = BitmapFactory.decodeFile(DSATabApplication.getDsaTabPath() + "maps/" + mapFiles[which]);
					view.setImageBitmap(bm);
					dialog.dismiss();
				}
			});

			builder.show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View v, MotionEvent rawEvent) {
		WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);

		ImageView view = (ImageView) v;

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = TouchMode.Drag;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDistance = spacing(event);

			if (oldDistance > 10f) {
				savedMatrix.set(matrix);
				midPoint(middle, event);
				mode = TouchMode.Zoom;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = TouchMode.None;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == TouchMode.Drag) {
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			} else if (mode == TouchMode.Zoom) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDistance;
					matrix.postScale(scale, scale, middle.x, middle.y);
				}
			}
			break;
		}
		view.setImageMatrix(matrix);
		return true;
	}

	/** Determine the space between the first two fingers */
	private float spacing(WrapMotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, WrapMotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}
