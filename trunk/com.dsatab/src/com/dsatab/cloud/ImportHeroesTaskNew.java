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
package com.dsatab.cloud;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.cloud.HeroExchange.OnHeroExchangeListener;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.data.HeroFileInfo;
import com.dsatab.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class ImportHeroesTaskNew extends AsyncTask<String, String, Integer> implements OnCancelListener {

	private ProgressDialog progressDialog;

	private Exception caughtException = null;

	private String token = null;

	private Context context;

	private Document d;

	private OnHeroExchangeListener onHeroExchangeListener;

	/**
		 * 
		 */
	public ImportHeroesTaskNew(Context context, String token) {
		this.context = context;
		this.token = token;
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

		progressDialog = ProgressDialog.show(context, "Helden importieren",
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

		Helper.disableSSLCheck(); // nur für die Testphase, bis ein gültiges
									// Zertifikate vorhanden ist

		try {
			publishProgress("Verbinde mit Server...");

			// HeldenListe anfordern
			String stringHeldenliste = Helper.postrequest("action", "listhelden", "token", token);

			if (stringHeldenliste == null) {
				caughtException = new IOException("Konnte keine Verbindung zum Austausch Server herstellen.");
				return HeroExchange.RESULT_ERROR;
			}

			d = Helper.string2Doc(stringHeldenliste);

		} catch (Exception e) {
			Debug.error(e);
			caughtException = e;
			return HeroExchange.RESULT_ERROR;
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

			// Anzahl der Helden bestimmen
			int anzahl = Helper.getDaten(d, "/helden/held").getLength();
			// Die Namen der Helden anzeigen
			for (int i = 1; i <= anzahl; i++) {
				String name = Helper.getDatenAsString(d, "/helden/held[" + i + "]/name");
				String heldenid = Helper.getDatenAsString(d, "/helden/held[" + i + "]/heldenid");
				String heldenKey = Helper.getDatenAsString(d, "/helden/held[" + i + "]/heldenkey");
				String lastChange = Helper.getDatenAsString(d, "/helden/held[" + i + "]/heldlastchange");

				HeroFileInfo fileInfo = new HeroFileInfo(name, heldenid, heldenKey);
				if (onHeroExchangeListener != null) {
					onHeroExchangeListener.onHeroInfoLoaded(fileInfo);
				}

			}

			Toast.makeText(context, anzahl + " Helden erfolgreich importiert.", Toast.LENGTH_SHORT).show();
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
			throw new DsaTabRuntimeException("Could not import heroes", caughtException);
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
