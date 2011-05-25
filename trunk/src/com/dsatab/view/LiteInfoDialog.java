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

import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.dsatab.R;
import com.dsatab.activity.BaseMenuActivity;
import com.dsatab.activity.PaypalSettings;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalPayment;

public class LiteInfoDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private WebView webView;

	private AlertDialog paypalDialog;
	private String content;

	private Activity activity;

	/**
	 * @param context
	 */
	public LiteInfoDialog(Activity context) {
		super(context);
		this.activity = context;
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

			if (paypalDialog == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

				// -- Paypal button
				PayPal paypal = PayPal.getInstance();

				if (paypal == null || !paypal.isLibraryInitialized()) {
					paypal = PayPal.initWithAppID(activity, PaypalSettings.PAYPAL_APP_ID, PaypalSettings.PAYPAL_MODE);
				}

				final CheckoutButton paypalButton = paypal.getCheckoutButton(activity, PayPal.BUTTON_194x37,
						CheckoutButton.TEXT_DONATE);

				paypalButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (paypalDialog.isShowing())
							paypalDialog.dismiss();

						BigDecimal amount = (BigDecimal) v.getTag();
						if (amount != null) {
							PayPalPayment payment = new PayPalPayment();
							payment.setSubtotal(amount);
							payment.setDescription("Eine kleine Spende für DsaTab");
							payment.setCurrencyType("EUR");
							payment.setRecipient(PaypalSettings.PAYPAL_RECIPIENT);
							payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);
							payment.setPaymentSubtype(PayPal.PAYMENT_SUBTYPE_DONATIONS);
							Intent checkoutIntent = PayPal.getInstance().checkout(payment, activity);
							activity.startActivityForResult(checkoutIntent, BaseMenuActivity.ACTION_PAYPAL);
						}
					}
				});
				// --
				LinearLayout contentView = (LinearLayout) getLayoutInflater().inflate(R.layout.popup_paypal, null);

				RadioButton koffee = (RadioButton) contentView.findViewById(R.id.pp_option_coffee);
				RadioButton beer = (RadioButton) contentView.findViewById(R.id.pp_option_beer);
				RadioButton diceset = (RadioButton) contentView.findViewById(R.id.pp_option_diceset);

				RadioGroup group = (RadioGroup) contentView.findViewById(R.id.pp_option_group);

				OnCheckedChangeListener listener = new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
						case R.id.pp_option_coffee:
							paypalButton.setTag(new BigDecimal("1.0"));
							break;
						case R.id.pp_option_beer:
							paypalButton.setTag(new BigDecimal("3.0"));
							break;
						case R.id.pp_option_diceset:
							paypalButton.setTag(new BigDecimal("6.0"));
							break;
						default:
							paypalButton.setTag(new BigDecimal("1.0"));
							break;
						}

					}
				};

				group.setOnCheckedChangeListener(listener);
				beer.setChecked(true);

				LinearLayout buttons = (LinearLayout) contentView.findViewById(R.id.pp_buttons);
				buttons.addView(paypalButton);

				builder.setView(contentView);
				builder.setTitle("Via Paypal spenden");
				paypalDialog = builder.show();
			} else {
				paypalDialog.show();
			}
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

		View contentView = (View) getLayoutInflater().inflate(R.layout.popup_webview, null);

		webView = (WebView) contentView.findViewById(R.id.info_web);

		contentView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.label_donate), this);
		setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.label_no_thanks), this);

		if (content == null) {
			content = getContext().getResources().getString(R.string.lite_info);
		}
		webView.loadDataWithBaseURL("/fake", content, "text/html", "utf-8", null);

		setView(contentView);

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
