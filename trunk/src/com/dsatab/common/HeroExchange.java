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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EncodingUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.activity.BaseMenuActivity;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.activity.DsaPreferenceActivity;
import com.dsatab.data.Hero;
import com.dsatab.xml.XmlParserNew;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.ErrorHandler;

public class HeroExchange implements OnCancelListener, OnCheckedChangeListener {

	public static final int RESULT_OK = 1;
	public static final int RESULT_ERROR = 2;
	public static final int RESULT_CANCELED = 3;
	public static final int RESULT_EMPTY = 4;

	public static final String PREF_LAST_HERO_KEY = "LAST_HERO_KEY";
	public static final String PREF_LAST_HERO_OWNER = "LAST_HERO_OWNER";

	private static final String DEFAULT_USERNAME = "gastlogin";
	private static final String DEFAULT_PASSWORD = "gastlogin";
	private BaseMenuActivity context;

	private ProgressDialog progressDialog;
	private Dialog importDialog;

	private ImportHeroTask fileTask;

	private Exception caughtException = null;

	public HeroExchange(BaseMenuActivity context) {
		this.context = context;
	}

	public boolean isConfigured() {
		final SharedPreferences preferences = DSATabApplication.getPreferences();

		if (preferences.contains(DsaPreferenceActivity.KEY_EXCHANGE_USERNAME)
				&& preferences.contains(DsaPreferenceActivity.KEY_EXCHANGE_PASSWORD)
				&& preferences.contains(DsaPreferenceActivity.KEY_EXCHANGE_PROVIDER)) {

			String user = preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_USERNAME, "");
			String password = preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_USERNAME, "");
			String provider = preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_USERNAME, "");

			return !TextUtils.isEmpty(user) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(provider);
		}

		return false;
	}

	public void importHero() {

		final SharedPreferences preferences = DSATabApplication.getPreferences();

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final View popupContent = LayoutInflater.from(context).inflate(R.layout.popup_import, null);

		final EditText heldenKey = (EditText) popupContent.findViewById(R.id.et_heldenkey);
		final EditText heldenOwner = (EditText) popupContent.findViewById(R.id.et_heldenowner);
		final RadioGroup importGroup = (RadioGroup) popupContent.findViewById(R.id.rg_import);
		importGroup.setOnCheckedChangeListener(this);

		heldenKey.setText(preferences.getString(PREF_LAST_HERO_KEY, ""));
		heldenOwner.setText(preferences.getString(PREF_LAST_HERO_OWNER, ""));

		DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
					break;
				case DialogInterface.BUTTON_POSITIVE:

					if (importGroup.getCheckedRadioButtonId() == R.id.rb_current_hero) {
						Hero hero = DSATabApplication.getInstance().getHero();
						if (hero != null) {
							importHero(hero.getKey(), null);
						}
					} else {
						String key = heldenKey.getText().toString();
						String owner = heldenOwner.getText().toString();
						importHero(key, owner);
					}
					close();

					break;
				}
			}
		};
		builder.setTitle("Held importieren");

		popupContent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		builder.setView(popupContent);
		builder.setPositiveButton("Importieren", clickListener);
		builder.setNegativeButton("Abbrechen", clickListener);
		importDialog = builder.show();

		final RadioButton importCurrent = (RadioButton) popupContent.findViewById(R.id.rb_current_hero);
		importCurrent.setChecked(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.RadioGroup.OnCheckedChangeListener#onCheckedChanged(android
	 * .widget.RadioGroup, int)
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group.getId() == R.id.rg_import) {

			if (checkedId == R.id.rb_new_hero) {
				((LinearLayout) importDialog.findViewById(R.id.ll_newhero)).setVisibility(View.VISIBLE);
			} else {
				((LinearLayout) importDialog.findViewById(R.id.ll_newhero)).setVisibility(View.GONE);
			}
		}

	}

	private void importHero(String key, String owner) {

		final SharedPreferences preferences = DSATabApplication.getPreferences();

		if (TextUtils.isEmpty(key)) {
			Toast.makeText(context, "Heldenkey ungültig", Toast.LENGTH_SHORT);
		} else {

			Editor editor = preferences.edit();
			editor.putString(PREF_LAST_HERO_KEY, key);
			editor.putString(PREF_LAST_HERO_OWNER, owner);
			editor.commit();

			StringBuilder sb = new StringBuilder();
			sb.append(preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_PROVIDER,
					DsaPreferenceActivity.DEFAULT_EXCHANGE_PROVIDER));
			sb.append("index.php?login=");
			sb.append(preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_USERNAME, DEFAULT_USERNAME));
			sb.append("&password=");
			sb.append(preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_PASSWORD, DEFAULT_PASSWORD));
			sb.append("&action=downloadheld&hkey=");
			sb.append(key);

			if (!TextUtils.isEmpty(owner)) {
				sb.append("&masterLogin=");
				sb.append(owner);
			}

			downloadHero(sb.toString());

		}
	}

	public void close() {

		if (importDialog != null && importDialog.isShowing())
			importDialog.cancel();

		importDialog = null;
	}

	public void downloadHero(String inPath) {

		progressDialog = ProgressDialog.show(context, context.getString(R.string.download),
				"Daten werden von Helden-Austausch Server geladen");

		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(this);
		fileTask = new ImportHeroTask();
		fileTask.execute(inPath);
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
		if (fileTask != null)
			fileTask.cancel(true);
	}

	public void exportHero(Hero hero) {

		SharedPreferences preferences = DSATabApplication.getPreferences();
		File zipFile = null;
		try {
			zipFile = new File(DSATabApplication.getDsaTabPath(), "exchange.zip");

			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			XmlParserNew.writeHero(hero, fos);

			String filename = null;
			if (hero.getPath() != null) {
				File heroPath = new File(hero.getPath());
				filename = heroPath.getName();
			} else {
				filename = hero.getName() + ".xml";
			}

			ZipEntry entry = new ZipEntry(filename);
			zos.putNextEntry(entry);

			zos.write(fos.toByteArray());

			zos.closeEntry();
			zos.close();

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_PROVIDER,
					DsaPreferenceActivity.DEFAULT_EXCHANGE_PROVIDER) + "index.php");

			MultipartEntity entity = new MultipartEntity();

			entity.addPart(
					"login",
					new StringBody(preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_USERNAME, DEFAULT_USERNAME)));
			entity.addPart(
					"password",
					new StringBody(preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_PASSWORD, DEFAULT_PASSWORD)));
			entity.addPart("hkey", new StringBody(hero.getKey()));
			entity.addPart("name", new StringBody(hero.getName()));
			entity.addPart("data", new FileBody(zipFile, "application/zip"));
			entity.addPart("nsc", new StringBody("false"));
			entity.addPart("MAX_FILE_SIZE", new StringBody("5120"));

			entity.addPart("scope", new StringBody("privat"));

			entity.addPart(
					"masterlogin",
					new StringBody(preferences.getString(DsaPreferenceActivity.KEY_EXCHANGE_USERNAME, DEFAULT_USERNAME)));
			entity.addPart("action", new StringBody("uploadheld"));
			httppost.setEntity(entity);

			HttpResponse response = httpclient.execute(httppost);

			ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
			response.getEntity().writeTo(bos2);

			Toast.makeText(context, Html.fromHtml(EncodingUtils.getString(bos2.toByteArray(), "UTF-8")),
					Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			Toast.makeText(context, "Held konnte nicht exportiert werden.", Toast.LENGTH_LONG);
			ErrorHandler.handleError(e, context);
		}
	}

	private InputStream openHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		HttpURLConnection httpConn = (HttpURLConnection) conn;
		httpConn.setAllowUserInteraction(false);
		httpConn.setInstanceFollowRedirects(true);
		httpConn.setRequestMethod("GET");
		httpConn.connect();

		response = httpConn.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK) {
			in = httpConn.getInputStream();
		}

		return in;
	}

	class ImportHeroTask extends AsyncTask<String, String, Integer> {

		private File innerFile = null;

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
					// Open the ZipInputStream
					InputStream is = openHttpConnection(inpath);

					if (is == null) {
						Toast.makeText(context, "Konnte keine Verbindung zum Austausch Server herstellen.",
								Toast.LENGTH_SHORT).show();
						return RESULT_ERROR;
					}
					inputStream = new ZipInputStream(is);

					boolean hasContent = false;

					// Loop through all the files and folders
					for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {

						hasContent = true;

						if (isCancelled()) {
							return RESULT_CANCELED;
						}

						publishProgress(entry.getName());

						Debug.verbose("Extracting: " + entry.getName() + "...");

						innerFile = new File(baseDir, entry.getName());
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

					if (!hasContent) {
						return RESULT_EMPTY;
					}

				} catch (Exception e) {
					Debug.error(e);
					caughtException = e;
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

			if (progressDialog != null) {
				if (progressDialog.isShowing())
					progressDialog.dismiss();
				progressDialog = null;
			}
			fileTask = null;

			switch (result) {
			case RESULT_OK:
				Toast.makeText(context, R.string.download_finished, Toast.LENGTH_SHORT).show();

				if (innerFile != null && innerFile.isFile() && innerFile.getName().endsWith(".xml"))
					context.loadHero(innerFile.getAbsolutePath());
				else
					Toast.makeText(
							context,
							"Konnte Heldendatei nicht öffnen, ungültige Datei "
									+ (innerFile != null ? innerFile.getName() : ""), Toast.LENGTH_SHORT).show();
				break;
			case RESULT_CANCELED:
				Toast.makeText(context, R.string.download_canceled, Toast.LENGTH_SHORT).show();
				break;
			case RESULT_EMPTY:
				Toast.makeText(context, "Konnte keine Heldendatei am Helden-Austausch Server finden.",
						Toast.LENGTH_SHORT).show();
				break;
			case RESULT_ERROR:
				Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
				if (caughtException != null) {
					ErrorHandler.handleError(caughtException, context);
				}
				break;
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

	};
}
