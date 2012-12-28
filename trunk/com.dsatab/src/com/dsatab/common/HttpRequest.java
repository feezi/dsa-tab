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

/**
 * @author Ganymede
 *
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;

import com.gandulf.guilib.util.Debug;

public class HttpRequest {

	AndroidHttpClient httpClient;
	HttpContext localContext;
	private String ret;

	HttpResponse response = null;
	HttpPost httpPost = null;
	HttpGet httpGet = null;

	public HttpRequest() {
		HttpParams myParams = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(myParams, 10000);
		HttpConnectionParams.setSoTimeout(myParams, 10000);
		httpClient = AndroidHttpClient.newInstance(null);
		localContext = new BasicHttpContext();
	}

	public void abort() {
		try {
			if (httpClient != null) {
				httpPost.abort();
			}
		} catch (Exception e) {
			Debug.error(e);
		}
	}

	public String sendPost(String url, String data) {
		return sendPost(url, data, null);
	}

	public String sendJSONPost(String url, JSONObject data) {
		return sendPost(url, data.toString(), "application/json");
	}

	public String sendPost(String url, String data, String contentType) {
		ret = null;

		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);

		httpPost = new HttpPost(url);
		response = null;

		StringEntity tmp = null;

		Debug.verbose("Setting httpPost headers");

		httpPost.setHeader("User-Agent", "SET YOUR USER AGENT STRING HERE");
		httpPost.setHeader("Accept",
				"text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

		if (contentType != null) {
			httpPost.setHeader("Content-Type", contentType);
		} else {
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		}

		try {
			tmp = new StringEntity(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Debug.verbose("HttpUtils : UnsupportedEncodingException : " + e);
		}

		httpPost.setEntity(tmp);

		Debug.verbose(url + "?" + data);

		try {
			response = httpClient.execute(httpPost, localContext);

			if (response != null) {
				ret = EntityUtils.toString(response.getEntity());
			}
		} catch (Exception e) {
			Debug.verbose("HttpUtils: " + e);
		}

		Debug.verbose("Returning value:" + ret);

		return ret;
	}

	public String sendGet(String url) {
		httpGet = new HttpGet(url);

		try {
			response = httpClient.execute(httpGet);
		} catch (Exception e) {
			Debug.error(e);
		}

		// int status = response.getStatusLine().getStatusCode();

		// we assume that the response body contains the error message
		try {
			ret = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			Debug.error(e);
		}

		return ret;
	}

	public InputStream getHttpStream(String urlString) throws IOException {
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
		} catch (Exception e) {
			throw new IOException("Error connecting");
		} // end try-catch

		return in;
	}
}