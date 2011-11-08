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
package com.dsatab.activity;

import android.content.Intent;
import android.os.Bundle;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.fragment.NotesEditFragment.OnNotesEditListener;

public class NotesEditActivity extends BaseFragmentActivity implements OnNotesEditListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(DSATabApplication.getInstance().getCustomDialogTheme());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_notes_edit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.NotesEditFragment.OnNotesEditListener#onNoteCanceled
	 * ()
	 */
	@Override
	public void onNoteCanceled() {
		setResult(RESULT_CANCELED);
		finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.NotesEditFragment.OnNotesEditListener#onNoteSaved
	 * (android.os.Bundle)
	 */
	@Override
	public void onNoteSaved(Bundle data) {
		Intent intent = new Intent();
		intent.putExtras(data);
		setResult(RESULT_OK, intent);
		finish();
	}

}
