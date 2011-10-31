/**
 *  This file is part of Risk.
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
 *  along with Risk.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.dsatab.R;

public class ModificatorEditActivity extends BaseActivity implements OnClickListener {

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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_edit_modificator);

		cbActive = (CheckBox) findViewById(R.id.popup_edit_active);
		etName = (EditText) findViewById(R.id.popup_edit_name);
		etRules = (EditText) findViewById(R.id.popup_edit_info);
		etComment = (EditText) findViewById(R.id.popup_edit_comment);

		Button ok = (Button) findViewById(R.id.popup_edit_ok);
		ok.setOnClickListener(this);
		Button cancel = (Button) findViewById(R.id.popup_edit_cancel);
		cancel.setOnClickListener(this);

		if (getIntent() != null) {
			etName.setText(getIntent().getStringExtra(INTENT_NAME));
			etRules.setText(getIntent().getStringExtra(INTENT_RULES));
			etComment.setText(getIntent().getStringExtra(INTENT_COMMENT));
			cbActive.setChecked(getIntent().getBooleanExtra(INTENT_ACTIVE, true));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.popup_edit_ok:
			Intent data = new Intent();

			if (getIntent() != null) {
				data.putExtra(INTENT_ID, getIntent().getSerializableExtra(INTENT_ID));
			}
			data.putExtra(INTENT_NAME, etName.getText().toString());
			data.putExtra(INTENT_COMMENT, etComment.getText().toString());
			data.putExtra(INTENT_RULES, etRules.getText().toString());
			data.putExtra(INTENT_ACTIVE, cbActive.isChecked());
			setResult(RESULT_OK, data);
			finish();
			break;
		case R.id.popup_edit_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}

}