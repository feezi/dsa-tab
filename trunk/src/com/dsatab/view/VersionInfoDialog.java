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
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebView;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.gandulf.guilib.util.ResUtil;

/**
 * @author Ganymede
 * 
 */
public class VersionInfoDialog extends AlertDialog {

	private WebView webView;

	private String content;

	/**
	 * @param context
	 */
	public VersionInfoDialog(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param theme
	 */
	public VersionInfoDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public VersionInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	protected void init() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setTitle(R.string.news_title);
		setIcon(R.drawable.icon);

		View contentView = (View) getLayoutInflater().inflate(R.layout.popup_webview, null);

		webView = (WebView) contentView.findViewById(R.id.info_web);

		contentView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		setButton(getContext().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		if (hasContent()) {
			webView.loadDataWithBaseURL("/fake", content, "text/html", "utf-8", null);
		}

		setView(contentView);
	}

	public boolean hasContent() {
		return !TextUtils.isEmpty(content);
	}

	public void setSeenVersion(Integer seenVersion) {

		int version = DSATabApplication.getInstance().getPackageVersion();
		if (version > seenVersion) {

			StringBuilder summary = new StringBuilder();

			while (version > seenVersion) {

				int stringId = ResUtil.getString(R.raw.class, "news_" + version);
				if (stringId > 0) {
					String content = ResUtil.loadResToString(stringId, getContext());
					if (content != null)
						summary.append(content);
				}
				version--;
			}

			if (!TextUtils.isEmpty(summary)) {
				content = summary.toString();
			} else {
				content = null;
			}

			if (webView != null && content != null) {
				webView.loadDataWithBaseURL("/fake", content, "text/html", "utf-8", null);
			}

		}
	}

}
