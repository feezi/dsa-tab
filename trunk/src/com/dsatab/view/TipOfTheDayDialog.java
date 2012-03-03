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

import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;

import com.dsatab.DSATabApplication;
import com.dsatab.R;

public class TipOfTheDayDialog extends AlertDialog implements android.view.View.OnClickListener,
		DialogInterface.OnDismissListener, DialogInterface.OnClickListener {

	public static final String PREF_SHOW_TIPS = "_showTips";
	public static boolean tipShown = false;

	private WebView webView;

	private static final int TIP_COUNT = 6;

	private Integer currentTip;

	private Random rnd;

	private SharedPreferences preferences;

	/**
	 * @param context
	 */
	public TipOfTheDayDialog(Context context) {
		super(context);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tip_next:
			nextTip();
			break;
		case R.id.tip_prev:
			previousTip();
			break;
		}

	}

	/**
	 * 
	 */
	protected void init() {

		rnd = new Random();
		preferences = DSATabApplication.getPreferences();
		setTitle("Wusstest du schon?");

		LayoutInflater inflater = LayoutInflater.from(getContext());

		View popupcontent = inflater.inflate(R.layout.popup_tip_today, null, false);

		webView = (WebView) popupcontent.findViewById(R.id.tip_web);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		popupcontent.findViewById(R.id.tip_next).setOnClickListener(this);
		popupcontent.findViewById(R.id.tip_prev).setOnClickListener(this);

		CheckBox show = (CheckBox) popupcontent.findViewById(R.id.tip_show);
		show.setChecked(preferences.getBoolean(PREF_SHOW_TIPS, true));

		setView(popupcontent);

		setButton(BUTTON_POSITIVE, getContext().getString(R.string.label_ok), this);

		if (DSATabApplication.getInstance().isLiteVersion()) {
			setButton(BUTTON_NEGATIVE, getContext().getString(R.string.label_donate), this);
		}

		setOnDismissListener(this);
		setCancelable(true);
		setCanceledOnTouchOutside(true);

		randomTip();
	}

	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case BUTTON_POSITIVE:
			dialog.dismiss();
			break;
		case BUTTON_NEGATIVE:
			dialog.dismiss();
			Uri uriUrl = Uri.parse(DSATabApplication.PAYPAL_DONATION_URL);
			final Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			getContext().startActivity(launchBrowser);
			break;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnDismissListener#onDismiss(android.content
	 * .DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		CheckBox show = (CheckBox) findViewById(R.id.tip_show);
		if (show.isChecked() != preferences.getBoolean(PREF_SHOW_TIPS, true)) {
			Editor edit = preferences.edit();
			edit.putBoolean(PREF_SHOW_TIPS, show.isChecked());
			edit.commit();
		}
	}

	public void randomTip() {
		showTip(rnd.nextInt(TIP_COUNT));
	}

	public void showTip(int number) {

		currentTip = number;
		webView.loadUrl("file:///android_asset/tips/tip_" + number + ".html");

		int titleId = getContext().getResources().getIdentifier("tip_" + number + "_title", "string",
				DSATabApplication.getInstance().getPackageName());
		if (titleId > 0) {
			setTitle("Tip: " + getContext().getString(titleId));
		} else {
			setTitle("Tip des Tages");
		}
	}

	public void nextTip() {
		if (currentTip < TIP_COUNT - 1)
			showTip(currentTip + 1);
		else
			showTip(0);
	}

	public void previousTip() {
		if (currentTip > 0)
			showTip(currentTip - 1);
		else
			showTip(TIP_COUNT - 1);
	}

}
