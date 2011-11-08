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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.common.Util;
import com.dsatab.common.WrapMotionEvent;
import com.dsatab.data.Hero;
import com.dsatab.map.MapTileProviderLocal;
import com.gandulf.guilib.util.AbstractDownloader;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.DownloaderWrapper;

public class MapFragment extends BaseFragment implements OnTouchListener, OnClickListener {

	/**
	 * 
	 */
	private static final String OSM_AVENTURIEN = "OSM_AVENTURIEN";

	private static final String PREF_KEY_LAST_MAP_COORDINATES = "lastMapCoordinates";
	private static final String PREF_KEY_LAST_MAP = "lastMap";

	private static final String PREF_KEY_OSM_ASK = "osm.askdownload";
	private static final String PREF_KEY_OSM_ZOOM = "osm.zoomLevel";
	private static final String PREF_KEY_OSM_LATITUDE = "osm.lat";
	private static final String PREF_KEY_OSM_LONGITUDE = "osm.lon";
	private static final int DEFAULT_OSM_ZOOM = 2;

	public enum TouchMode {
		None, Drag, Zoom;
	}

	private ImageView imageMapView;
	private ImageButton chooseMap;
	private MapView osmMapView;

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	private TouchMode mode = TouchMode.None;

	// Points for distance calc...
	private PointF start = new PointF();
	private PointF middle = new PointF();

	private float oldDistance = 1f;

	private String[] mapFiles;
	private String[] mapNames;

	private BitmapDrawable bitmap;

	private LoadImageTask loadImageTask;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.sheet_map, container, false);

		File osmMapDir = DSATabApplication.getDirectory(DSATabApplication.DIR_OSM_MAPS);
		ITileSource tileSource = TileSourceFactory.getTileSource(DSATabApplication.TILESOURCE_AVENTURIEN);
		MapTileProviderLocal tileProvider = new MapTileProviderLocal(osmMapDir.getAbsolutePath(), getActivity(),
				tileSource);
		osmMapView = new MapView(getActivity(), 256, new DefaultResourceProxyImpl(getActivity()), tileProvider);
		osmMapView.setUseDataConnection(false);
		osmMapView.setBuiltInZoomControls(true);
		osmMapView.setMultiTouchControls(true);
		osmMapView.getController().setZoom(DEFAULT_OSM_ZOOM);
		root.addView(osmMapView, 0);

		imageMapView = (ImageView) root.findViewById(R.id.imageView);
		chooseMap = (ImageButton) root.findViewById(R.id.open_map);
		return configureContainerView(root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (loadImageTask != null) {
			loadImageTask.cancel(true);
			loadImageTask = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		chooseMap.setOnClickListener(this);
		imageMapView.setOnTouchListener(this);

		List<String> mapFiles = new ArrayList<String>();
		List<String> mapNames = new ArrayList<String>();

		File mapDir = new File(DSATabApplication.getDsaTabPath(), DSATabApplication.DIR_MAPS);
		File osmMapDir = new File(DSATabApplication.getDsaTabPath(), DSATabApplication.DIR_OSM_MAPS);

		if (!mapDir.exists())
			mapDir.mkdirs();

		File[] files = mapDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {

				filename = filename.toLowerCase();

				return filename.endsWith(".jpg") || filename.endsWith(".gif") || filename.endsWith(".png")
						|| filename.endsWith(".jpeg") || filename.endsWith(".bmp");
			}
		});
		if (files != null) {
			Arrays.sort(files, new Util.FileNameComparator());
			for (File file : files) {
				if (file.isFile()) {
					mapFiles.add(file.getName());
					mapNames.add(file.getName().replace("-", " ").substring(0, file.getName().length() - 4));
				}
			}
		}

		files = osmMapDir.listFiles();
		if (files != null && files.length > 0) {
			mapFiles.add(OSM_AVENTURIEN);
			mapNames.add("Aventurien (GoogleMaps)");
		} else {
			if (preferences.getBoolean(PREF_KEY_OSM_ASK, true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Neue Aventurien Karte");
				builder.setMessage("Es gibt jetzt eine GoogleMaps ähnliche Karte von ganz Aventurien. Willst du dir das Kartenpaket (ca. 10MB) aus dem Internet herunterladen?");
				builder.setCancelable(true);
				builder.setPositiveButton("Herunterladen", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						AbstractDownloader downloader = DownloaderWrapper.getInstance(
								DSATabApplication.getDsaTabPath(), getActivity());
						downloader.addPath(BasePreferenceActivity.PATH_OSM_MAP_PACK);
						downloader.downloadZip();
					}
				});
				builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Editor edit = preferences.edit();
						edit.putBoolean(PREF_KEY_OSM_ASK, false);
						edit.commit();
						dialog.dismiss();

					}
				});
				builder.show();
			}

		}

		TextView empty = (TextView) findViewById(android.R.id.empty);

		if (mapFiles.isEmpty()) {
			String path = mapDir.getAbsolutePath();
			path = path.replace(DSATabApplication.SD_CARD_PATH_PREFIX, "");

			empty.setVisibility(View.VISIBLE);
			imageMapView.setVisibility(View.GONE);
			osmMapView.setVisibility(View.GONE);
			chooseMap.setVisibility(View.GONE);

			empty.setText(Util.getText(R.string.message_map_empty, path));

			this.mapFiles = null;
			this.mapNames = null;

		} else {

			empty.setVisibility(View.GONE);

			this.mapFiles = mapFiles.toArray(new String[0]);
			this.mapNames = mapNames.toArray(new String[0]);

		}
		super.onActivityCreated(savedInstanceState);
	}

	private void loadMap(String filePath) {
		Debug.verbose("loading map " + filePath);
		Debug.heap();
		unloadMap();

		if (OSM_AVENTURIEN.equals(filePath)) {
			imageMapView.setVisibility(View.GONE);
			osmMapView.setVisibility(View.VISIBLE);
		} else {
			osmMapView.setVisibility(View.GONE);
			imageMapView.setVisibility(View.VISIBLE);

			loadImageTask = new LoadImageTask();
			loadImageTask.execute(filePath);

		}
	}

	private class LoadImageTask extends AsyncTask<String, String, Bitmap> {

		ProgressBar progress;
		TextView progressText;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			progress = (ProgressBar) findViewById(R.id.map_progress);
			progressText = (TextView) findViewById(R.id.map_progress_text);

			progress.setVisibility(View.VISIBLE);
			progressText.setVisibility(View.VISIBLE);
			progress.setIndeterminate(true);

		}

		protected Bitmap doInBackground(String... urls) {
			File file = new File(DSATabApplication.getDirectory(DSATabApplication.DIR_MAPS), urls[0]);
			publishProgress(file.getName());
			return Util.decodeFile(file, 1000);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(String... values) {

		}

		protected void onPostExecute(Bitmap result) {
			if (isCancelled()) {
				onCancelled(result);
				return;
			}
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);

			if (result != null) {
				Toast.makeText(getActivity(), "Karte geladen.", Toast.LENGTH_SHORT).show();
				bitmap = new BitmapDrawable(getResources(), result);
				imageMapView.setImageDrawable(bitmap);
				imageMapView.setImageMatrix(matrix);
			} else {
				Toast.makeText(getActivity(), "Konnte Karte nicht laden.", Toast.LENGTH_SHORT).show();
			}

			loadImageTask = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
		 */
		protected void onCancelled(Bitmap result) {
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);

			loadImageTask = null;
		}
	}

	private void unloadMap() {

		if (loadImageTask != null) {
			loadImageTask.cancel(true);
			loadImageTask = null;
		}

		if (bitmap != null) {
			imageMapView.setImageDrawable(null);
			bitmap.setCallback(null);
			if (bitmap.getBitmap() != null)
				bitmap.getBitmap().recycle();
			bitmap = null;
		}
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
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {

		Editor edit = preferences.edit();

		float[] values = new float[9];
		matrix.getValues(values);
		edit.putString(PREF_KEY_LAST_MAP_COORDINATES, Util.toString(values));
		edit.putInt(PREF_KEY_OSM_ZOOM, osmMapView.getZoomLevel());
		IGeoPoint center = osmMapView.getMapCenter();
		edit.putInt(PREF_KEY_OSM_LATITUDE, center.getLatitudeE6());
		edit.putInt(PREF_KEY_OSM_LATITUDE, center.getLongitudeE6());
		edit.commit();

		// clear bitmap
		Debug.verbose("Freeing map bitmap");
		unloadMap();

		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();

		String lastMap = preferences.getString(PREF_KEY_LAST_MAP, null);

		if (mapFiles != null && mapFiles.length > 0) {
			if (lastMap == null) {
				lastMap = mapFiles[0];
				// Work around a Cupcake bug
				matrix.setTranslate(1f, 1f);
			}
		} else {
			lastMap = null;
		}

		osmMapView.getController().setZoom(preferences.getInt(PREF_KEY_OSM_ZOOM, DEFAULT_OSM_ZOOM));

		int latitude = preferences.getInt(PREF_KEY_OSM_LATITUDE, -1);
		int longitude = preferences.getInt(PREF_KEY_OSM_LONGITUDE, -1);
		if (latitude != -1 && longitude != -1) {
			IGeoPoint center = new GeoPoint(latitude, longitude);
			osmMapView.getController().setCenter(center);
		}

		if (!TextUtils.isEmpty(lastMap)) {
			Debug.verbose("Reloading map bitmap");

			String coords = preferences.getString(PREF_KEY_LAST_MAP_COORDINATES, null);
			if (coords != null) {
				matrix.setValues(Util.parseFloats(coords));
			}
			loadMap(lastMap);
		}
	}

	private void showMapChooser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Karte auswählen");
		builder.setItems(mapNames, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editor edit = preferences.edit();
				edit.putString(PREF_KEY_LAST_MAP, mapFiles[which]);
				edit.commit();

				matrix = new Matrix();
				matrix.setTranslate(1.0f, 1.0f);

				loadMap(mapFiles[which]);

				dialog.dismiss();
			}
		});

		builder.show();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.open_map:
			showMapChooser();
			break;
		}

	}
}
