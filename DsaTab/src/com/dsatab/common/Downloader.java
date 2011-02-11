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
package com.dsatab.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;

public class Downloader implements DialogInterface.OnCancelListener {

	public static final int RESULT_OK = 1;
	public static final int RESULT_ERROR = 2;
	public static final int RESULT_CANCELED = 3;

	private Context context;

	private ProgressDialog dialog;

	private List<String> todo = new LinkedList<String>();

	private DownloadTask task;

	private OnDownloadCompletedListener onDownloadCompletedListener;

	public interface OnDownloadCompletedListener {

		public void onComplete(int result);
	}

	/**
	 * 
	 */
	public Downloader(Context context) {
		this.context = context;
	}

	private InputStream openHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception ex) {
			Debug.error(ex);
			throw new IOException("Error connecting");
		}
		return in;
	}

	public void addPath(String path) {
		todo.add(path);
	}

	public void download() {

		dialog = ProgressDialog.show(context, context.getString(R.string.download),
				context.getString(R.string.download_message));

		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnCancelListener(this);
		task = new DownloadTask();
		task.execute();
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
		if (task != null)
			task.cancel(true);
	}

	public void close() {
		todo.clear();
		if (dialog != null) {
			if (dialog.isShowing())
				dialog.dismiss();
			dialog = null;
		}

		task = null;
	}

	public OnDownloadCompletedListener getOnDownloadCompletedListener() {
		return onDownloadCompletedListener;
	}

	public void setOnDownloadCompletedListener(OnDownloadCompletedListener onDownloadCompletedListener) {
		this.onDownloadCompletedListener = onDownloadCompletedListener;
	}

	class DownloadTask extends AsyncTask<String, String, Integer> {

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

			for (String path : todo) {

				ZipInputStream inputStream = null;
				try {
					// Open the ZipInputStream
					inputStream = new ZipInputStream(openHttpConnection(path));

					// Loop through all the files and folders
					for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {

						if (isCancelled()) {
							return RESULT_CANCELED;
						}

						publishProgress(entry.getName());

						Debug.verbose("Extracting: " + entry.getName() + "...");

						File innerFile = new File(baseDir, entry.getName());
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
								return RESULT_ERROR;
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
						return RESULT_CANCELED;
					}

				} catch (Exception e) {
					Debug.error(e);
					return RESULT_ERROR;
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
				return RESULT_CANCELED;
			else
				return RESULT_OK;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Integer result) {

			close();
			switch (result) {
			case RESULT_OK:
				Toast.makeText(context, R.string.download_finished, Toast.LENGTH_SHORT).show();
				break;
			case RESULT_CANCELED:
				Toast.makeText(context, R.string.download_canceled, Toast.LENGTH_SHORT).show();
				break;
			case RESULT_ERROR:
				Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
			}

			if (onDownloadCompletedListener != null)
				onDownloadCompletedListener.onComplete(result);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(String... values) {
			if (dialog != null)
				dialog.setMessage(values[0]);
		}

	};

}
