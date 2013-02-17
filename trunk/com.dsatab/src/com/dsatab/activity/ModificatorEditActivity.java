/**
 *  This file is part of DsaTab.
 *
 *  Risk is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Risk is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.dsatab.DsaTabApplication;
import com.dsatab.R;

public class ModificatorEditActivity extends BaseFragmentActivity {

	public static final String INTENT_ID = "id";
	public static final String INTENT_NAME = "name";
	public static final String INTENT_COMMENT = "comment";
	public static final String INTENT_RULES = "rules";
	public static final String INTENT_ACTIVE = "active";

	private CheckBox cbActive;

	private EditText etName, etRules, etComment;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(DsaTabApplication.getInstance().getCustomTheme());
		applyPreferencesToTheme();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sheet_edit_modificator);

		cbActive = (CheckBox) findViewById(R.id.popup_edit_active);
		etName = (EditText) findViewById(R.id.popup_edit_name);
		etRules = (EditText) findViewById(R.id.popup_edit_info);
		etComment = (EditText) findViewById(R.id.popup_edit_comment);

		if (getIntent() != null) {
			etName.setText(getIntent().getStringExtra(INTENT_NAME));
			etRules.setText(getIntent().getStringExtra(INTENT_RULES));
			etComment.setText(getIntent().getStringExtra(INTENT_COMMENT));
			cbActive.setChecked(getIntent().getBooleanExtra(INTENT_ACTIVE, true));
		}

		// Inflate a "Done/Discard" custom action bar view.
		LayoutInflater inflater = LayoutInflater.from(this);
		final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_discard, null);
		customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				accept();
			}
		});
		customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});

		// Show the custom action bar view and hide the normal Home icon and
		// title.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

	}

	protected void accept() {
		Intent data = new Intent();

		if (getIntent() != null) {
			data.putExtra(INTENT_ID, getIntent().getSerializableExtra(INTENT_ID));
		}
		String name = etName.getText().toString();
		String comment = etComment.getText().toString();
		String rules = etRules.getText().toString();

		if (TextUtils.isEmpty(rules)) {
			etRules.setError("erforderlich");
			return;
		}
		data.putExtra(INTENT_NAME, name);
		data.putExtra(INTENT_COMMENT, comment);
		data.putExtra(INTENT_RULES, rules);
		data.putExtra(INTENT_ACTIVE, cbActive.isChecked());
		setResult(RESULT_OK, data);
		finish();
	}

	protected void cancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

}