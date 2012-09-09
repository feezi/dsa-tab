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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EncodingUtils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.Hero;
import com.dsatab.util.Debug;
import com.dsatab.xml.XmlParser;

/**
 * @author Ganymede
 * 
 */
public class ExportHeroService extends IntentService {

	public static final int HERO_EXCHANGE_ID = 2;

	private NotificationManager notificationManager;

	public ExportHeroService() {
		super("ExportHeroService");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		ExportHeroTask exportHeroTask = new ExportHeroTask();
		exportHeroTask.execute(DSATabApplication.getInstance().getHero());
	}

	class ExportHeroTask extends AsyncTask<Hero, String, Integer> {

		private String exportResponse;
		private Exception caughtException;
		private NotificationCompat.Builder notification;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {

			notification = new NotificationCompat.Builder(ExportHeroService.this);
			notification.setAutoCancel(false);
			notification.setContentTitle("DsaTab Held exportieren");
			notification.setSmallIcon(android.R.drawable.stat_notify_sync);
			notification.setTicker("Held wird exportiert...");
			notification.setWhen(System.currentTimeMillis());
			notification.setContentText("Daten werden zum Helden-Austausch Server geschickt");
			notification.setOngoing(true);

			notificationManager.notify(HERO_EXCHANGE_ID, notification.getNotification());

			super.onPreExecute();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Integer doInBackground(Hero... params) {

			SharedPreferences preferences = DSATabApplication.getPreferences();
			File zipFile = null;
			try {
				zipFile = new File(DSATabApplication.getDsaTabPath(), "exchange.zip");

				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

				for (Hero hero : params) {

					publishProgress("Erstelle Heldenpaket...");

					ByteArrayOutputStream fos = new ByteArrayOutputStream();
					hero.onPreHeroSaved();
					XmlParser.writeHero(hero, fos);
					hero.onPostHeroSaved();
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
					HttpPost httppost = new HttpPost(preferences.getString(
							BasePreferenceActivity.KEY_EXCHANGE_PROVIDER,
							BasePreferenceActivity.DEFAULT_EXCHANGE_PROVIDER)
							+ "index.php");

					MultipartEntity entity = new MultipartEntity();

					entity.addPart(
							"login",
							new StringBody(preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_USERNAME,
									HeroExchange.DEFAULT_USERNAME)));
					entity.addPart(
							"password",
							new StringBody(preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_PASSWORD,
									HeroExchange.DEFAULT_PASSWORD)));
					entity.addPart("hkey", new StringBody(hero.getKey()));
					entity.addPart("name", new StringBody(hero.getName()));
					entity.addPart("data", new FileBody(zipFile, "application/zip"));
					entity.addPart("nsc", new StringBody("false"));
					entity.addPart("MAX_FILE_SIZE", new StringBody("5120"));

					entity.addPart("scope", new StringBody("privat"));

					entity.addPart(
							"masterlogin",
							new StringBody(preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_USERNAME,
									HeroExchange.DEFAULT_USERNAME)));
					entity.addPart("action", new StringBody("uploadheld"));
					httppost.setEntity(entity);

					if (isCancelled()) {
						return HeroExchange.RESULT_CANCELED;
					}

					publishProgress("Verbinde mit Server......");
					HttpResponse response = httpclient.execute(httppost);

					ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
					response.getEntity().writeTo(bos2);

					exportResponse = Html.fromHtml(EncodingUtils.getString(bos2.toByteArray(), "UTF-8")).toString();
					return HeroExchange.RESULT_OK;
				}

			} catch (Exception e) {
				Debug.error(e);
				caughtException = e;
				return HeroExchange.RESULT_ERROR;
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			notification.setOngoing(false);
			notification.setAutoCancel(true);

			switch (result) {
			case HeroExchange.RESULT_OK:

				if (TextUtils.isEmpty(exportResponse) || exportResponse.startsWith("OK")) {
					notification.setContentText("Held erfolgreich exportiert.");
					notificationManager.notify(HERO_EXCHANGE_ID, notification.getNotification());
				} else {
					notification.setContentText(exportResponse);
					notification.setTicker(exportResponse);
					notificationManager.notify(HERO_EXCHANGE_ID, notification.getNotification());
				}

				break;
			case HeroExchange.RESULT_CANCELED:
				notification.setContentText(getString(R.string.download_canceled));
				notificationManager.notify(HERO_EXCHANGE_ID, notification.getNotification());
				break;
			case HeroExchange.RESULT_EMPTY:
				notification.setContentText("Konnte keine Heldendatei am Helden-Austausch Server finden.");
				notificationManager.notify(HERO_EXCHANGE_ID, notification.getNotification());
				break;
			case HeroExchange.RESULT_ERROR:
				notification.setContentText("Held konnte nicht exportiert werden.");
				notification.setTicker("Held konnte nicht exportiert werden.");
				notificationManager.notify(HERO_EXCHANGE_ID, notification.getNotification());

				if (caughtException != null) {
					throw new DsaTabRuntimeException("Could not export hero.", caughtException);
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
			notification.setContentText(values[0]);
			notificationManager.notify(HERO_EXCHANGE_ID, notification.getNotification());
		}

	}
}
