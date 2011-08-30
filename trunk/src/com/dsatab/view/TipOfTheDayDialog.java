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

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import com.dsatab.R;

public class TipOfTheDayDialog extends Dialog implements android.view.View.OnClickListener {

	public static boolean tipShown = false;

	private WebView webView;

	private static final int TIP_COUNT = 3;

	private Integer currentTip;

	private Random rnd;

	/**
	 * @param context
	 */
	public TipOfTheDayDialog(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param theme
	 */
	public TipOfTheDayDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public TipOfTheDayDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
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
		case R.id.tip_ok:
			this.dismiss();
			break;
		}

	}

	/**
	 * 
	 */
	protected void init() {

		rnd = new Random();

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setTitle("Wusstest du schon?");

		LayoutInflater inflater = LayoutInflater.from(getContext());

		View popupcontent = inflater.inflate(R.layout.popup_tip_today, null, false);

		webView = (WebView) popupcontent.findViewById(R.id.tip_web);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		popupcontent.findViewById(R.id.tip_next).setOnClickListener(this);
		popupcontent.findViewById(R.id.tip_prev).setOnClickListener(this);
		popupcontent.findViewById(R.id.tip_ok).setOnClickListener(this);

		// setButton(DialogInterface.BUTTON_NEUTRAL,
		// getContext().getString(R.string.label_ok), this);

		setContentView(popupcontent);

		setCancelable(true);
		setCanceledOnTouchOutside(true);

		randomTip();
	}

	public void randomTip() {
		showTip(rnd.nextInt(TIP_COUNT));
	}

	public void showTip(int number) {

		currentTip = number;
		// String content = ResUtil.loadAssestToString("tips/tip_" + number +
		// ".html", getContext());
		// webView.loadDataWithBaseURL("file:///android_asset/tips", content,
		// "text/html", "utf-8", null);
		webView.loadUrl("file:///android_asset/tips/tip_" + number + ".html");
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
