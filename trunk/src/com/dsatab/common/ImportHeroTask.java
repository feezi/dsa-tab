/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.common.HeroExchange.OnHeroExchangeListener;
import com.dsatab.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class ImportHeroTask extends AsyncTask<String, String, Integer> implements OnCancelListener {

	private ProgressDialog progressDialog;

	private Exception caughtException = null;

	private String heroKey;
	private File innerFile = null;

	private Context context;

	private OnHeroExchangeListener onHeroExchangeListener;

	/**
		 * 
		 */
	public ImportHeroTask(Context context, String key) {
		this.context = context;
		this.heroKey = key;
	}

	public OnHeroExchangeListener getOnHeroExchangeListener() {
		return onHeroExchangeListener;
	}

	public void setOnHeroExchangeListener(OnHeroExchangeListener onHeroExchangeListener) {
		this.onHeroExchangeListener = onHeroExchangeListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressDialog = ProgressDialog.show(context, "Held importieren",
				"Daten werden von Helden-Austausch Server geladen");

		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Integer doInBackground(String... params) {

		boolean cancel = false;
		// Create a directory in the SDCard to store the files
		File baseDir = new File(DSATabApplication.getDsaTabPath());
		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}

		for (String inpath : params) {

			Debug.verbose("Downloading " + inpath);

			ZipInputStream inputStream = null;
			try {

				publishProgress("Verbinde mit Server...");
				// Open the ZipInputStream
				InputStream is = Util.openHttpConnection(inpath);

				if (is == null) {
					caughtException = new IOException("Konnte keine Verbindung zum Austausch Server herstellen.");
					return HeroExchange.RESULT_ERROR;
				}
				inputStream = new ZipInputStream(is);

				boolean hasContent = false;

				// Loop through all the files and folders
				for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {

					hasContent = true;

					if (isCancelled()) {
						return HeroExchange.RESULT_CANCELED;
					}

					publishProgress("Entpacke " + entry.getName() + "...");

					Debug.verbose("Extracting: " + entry.getName() + "...");

					innerFile = new File(baseDir, heroKey + "-" + entry.getName());
					// if (innerFile.exists()) {
					// innerFile.delete();
					// }

					// Check if it is a folder
					if (entry.isDirectory()) {
						// Its a folder, create that folder
						innerFile.mkdirs();
					} else {
						// Create a file output stream
						BufferedOutputStream bufferedOutputStream = null;
						try {
							FileOutputStream outputStream = new FileOutputStream(innerFile.getAbsolutePath());
							final int BUFFER = 2048;

							// Buffer the output to the file
							bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER);

							// Write the contents
							int count = 0;
							byte[] data = new byte[BUFFER];
							while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
								bufferedOutputStream.write(data, 0, count);
							}

							// Flush and close the buffers
							bufferedOutputStream.flush();
							bufferedOutputStream.close();
						} catch (Exception e) {
							Debug.error(e);
							caughtException = e;
							return HeroExchange.RESULT_ERROR;
						} finally {
							if (bufferedOutputStream != null)
								bufferedOutputStream.close();
						}
					}

					// Close the current entry
					inputStream.closeEntry();
				}
				inputStream.close();

				if (isCancelled()) {
					return HeroExchange.RESULT_CANCELED;
				}

				if (!hasContent) {
					return HeroExchange.RESULT_EMPTY;
				}

			} catch (Exception e) {
				Debug.error(e);
				caughtException = e;
				return HeroExchange.RESULT_ERROR;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}
		}

		if (isCancelled() || cancel)
			return HeroExchange.RESULT_CANCELED;
		else
			return HeroExchange.RESULT_OK;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Integer result) {

		if (progressDialog != null) {
			if (progressDialog.isShowing())
				progressDialog.dismiss();
			progressDialog = null;
		}

		switch (result) {
		case HeroExchange.RESULT_OK:
			if (innerFile != null && innerFile.isFile() && innerFile.getName().endsWith(".xml")) {
				if (onHeroExchangeListener != null) {
					onHeroExchangeListener.onHeroLoaded(innerFile.getAbsolutePath());
				} else {
					Toast.makeText(context, "Held erfolgreich importiert.", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(
						context,
						"Konnte Heldendatei nicht öffnen, ungültige Datei "
								+ (innerFile != null ? innerFile.getName() : ""), Toast.LENGTH_SHORT).show();
			}

			break;
		case HeroExchange.RESULT_CANCELED:
			Toast.makeText(context, R.string.download_canceled, Toast.LENGTH_SHORT).show();
			break;
		case HeroExchange.RESULT_EMPTY:
			Toast.makeText(context, "Konnte keine Heldendatei am Helden-Austausch Server finden.", Toast.LENGTH_SHORT)
					.show();
			break;
		case HeroExchange.RESULT_ERROR:
			Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
			throw new DsaTabRuntimeException("Could not import hero from " + innerFile, caughtException);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(String... values) {
		if (progressDialog != null)
			progressDialog.setMessage(values[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnCancelListener#onCancel(android.content
	 * .DialogInterface)
	 */
	@Override
	public void onCancel(DialogInterface dialog) {

		cancel(true);

	}

}
