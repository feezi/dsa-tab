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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
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

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.activity.DsaPreferenceActivity;
import com.dsatab.activity.DsaPreferenceActivityHC;
import com.dsatab.data.Hero;
import com.dsatab.util.Debug;

public class HeroExchange implements OnCheckedChangeListener {

	public static final int RESULT_OK = 1;
	public static final int RESULT_ERROR = 2;
	public static final int RESULT_CANCELED = 3;
	public static final int RESULT_EMPTY = 4;

	public static final String PREF_LAST_HERO_KEY = "LAST_HERO_KEY";
	public static final String PREF_LAST_HERO_OWNER = "LAST_HERO_OWNER";

	public static final String DEFAULT_USERNAME = "gastlogin";
	public static final String DEFAULT_PASSWORD = "gastlogin";

	private Context context;

	private Dialog importDialog;

	private OnHeroExchangeListener onHeroExchangeListener;

	public interface OnHeroExchangeListener {
		public void onHeroLoaded(String path);
	};

	public HeroExchange(Context context) {
		this.context = context;
	}

	public OnHeroExchangeListener getOnHeroExchangeListener() {
		return onHeroExchangeListener;
	}

	public void setOnHeroExchangeListener(OnHeroExchangeListener onHeroExchangeListener) {
		this.onHeroExchangeListener = onHeroExchangeListener;
	}

	private boolean isConfigured() {
		final SharedPreferences preferences = DSATabApplication.getPreferences();

		if (preferences.contains(BasePreferenceActivity.KEY_EXCHANGE_USERNAME)
				&& preferences.contains(BasePreferenceActivity.KEY_EXCHANGE_PASSWORD)
				&& preferences.contains(BasePreferenceActivity.KEY_EXCHANGE_PROVIDER)) {

			String user = preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_USERNAME, "");
			String password = preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_USERNAME, "");
			String provider = preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_USERNAME, "");

			return !TextUtils.isEmpty(user) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(provider);
		}
		return false;
	}

	public void importHero() {

		if (!checkSettings())
			return;

		final SharedPreferences preferences = DSATabApplication.getPreferences();

		final Hero hero = DSATabApplication.getInstance().getHero();

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final View popupContent = LayoutInflater.from(context).inflate(R.layout.popup_import, null);

		final EditText heldenKey = (EditText) popupContent.findViewById(R.id.et_heldenkey);
		final EditText heldenOwner = (EditText) popupContent.findViewById(R.id.et_heldenowner);
		final RadioGroup importGroup = (RadioGroup) popupContent.findViewById(R.id.rg_import);
		importGroup.setOnCheckedChangeListener(this);

		heldenKey.setText(preferences.getString(PREF_LAST_HERO_KEY, ""));
		heldenOwner.setText(preferences.getString(PREF_LAST_HERO_OWNER, ""));

		if (hero == null) {
			popupContent.findViewById(R.id.rb_current_hero).setEnabled(hero != null);
		}

		DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
					break;
				case DialogInterface.BUTTON_POSITIVE:

					if (importGroup.getCheckedRadioButtonId() == R.id.rb_current_hero) {
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
			Toast.makeText(context, "Heldenkey ungültig", Toast.LENGTH_SHORT).show();
		} else {

			Editor editor = preferences.edit();
			editor.putString(PREF_LAST_HERO_KEY, key);
			editor.putString(PREF_LAST_HERO_OWNER, owner);
			editor.commit();

			StringBuilder sb = new StringBuilder();
			sb.append(preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_PROVIDER,
					BasePreferenceActivity.DEFAULT_EXCHANGE_PROVIDER));
			sb.append("index.php?login=");
			sb.append(preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_USERNAME, DEFAULT_USERNAME));
			sb.append("&password=");
			sb.append(preferences.getString(BasePreferenceActivity.KEY_EXCHANGE_PASSWORD, DEFAULT_PASSWORD));
			sb.append("&action=downloadheld2&hkey=");
			sb.append(key);

			if (!TextUtils.isEmpty(owner)) {
				sb.append("&masterLogin=");
				sb.append(owner);
			}

			downloadHero(context, sb.toString(), key);

		}
	}

	public void close() {
		if (importDialog != null && importDialog.isShowing())
			importDialog.cancel();

		importDialog = null;
	}

	private void downloadHero(Context context, String inPath, String key) {
		ImportHeroTask importFileTask = new ImportHeroTask(context, key);
		importFileTask.setOnHeroExchangeListener(onHeroExchangeListener);
		importFileTask.execute(inPath);
	}

	private boolean checkSettings() {
		if (!isConfigured()) {

			Toast.makeText(context, "Bitte zuerst die Logindaten bei den Heldenaustausch Einstellungen angeben.",
					Toast.LENGTH_LONG).show();

			Intent intent;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				intent = new Intent(context, DsaPreferenceActivity.class);
			} else {
				intent = new Intent(context, DsaPreferenceActivityHC.class);
			}
			intent.putExtra(DsaPreferenceActivity.INTENT_PREF_SCREEN, DsaPreferenceActivity.SCREEN_EXCHANGE);

			context.startActivity(intent);
			return false;
		} else
			return true;
	}

	public void exportHero(Hero hero) {

		Debug.verbose("Exporting " + hero.getName());

		if (!checkSettings())
			return;

		Intent intent = new Intent(context, ExportHeroService.class);
		context.startService(intent);
	}

}
