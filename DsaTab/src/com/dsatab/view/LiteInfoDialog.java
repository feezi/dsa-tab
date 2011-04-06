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
package com.dsatab.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebView;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;

public class LiteInfoDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private WebView webView;

	private String content;

	/**
	 * @param context
	 */
	public LiteInfoDialog(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param theme
	 */
	public LiteInfoDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public LiteInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnClickListener#onClick(android.content
	 * .DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			dialog.dismiss();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=" + DSATabApplication.getInstance().getPackageName()));
			getContext().startActivity(intent);
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;
		}

	}

	/**
	 * 
	 */
	protected void init() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setTitle(R.string.lite_title);
		setIcon(R.drawable.icon_lite);

		webView = (WebView) getLayoutInflater().inflate(R.layout.popup_webview, null);
		webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.label_buy_app), this);
		setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.label_no_thanks), this);

		if (content == null) {
			content = getContext().getResources().getString(R.string.lite_info);
		}
		webView.loadDataWithBaseURL("/fake", content, "text/html", "utf-8", null);

		setView(webView);

		setCancelable(true);
		setCanceledOnTouchOutside(true);
	}

	public void setFeature(String f) {
		setTitle(R.string.lite_feature_teaser_title);
		content = getContext().getResources().getString(R.string.lite_feature_teaser, f);

		if (webView != null) {
			webView.loadDataWithBaseURL("/fake", content, "text/html", "utf-8", null);
		}
	}

}
