/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.fragment;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.adapter.FileAdapter;

public class DocumentsFragment extends BaseFragment implements OnItemClickListener {

	private ListView listView;
	private FileAdapter documentsListAdapter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return configureContainerView(inflater.inflate(R.layout.sheet_documents, container, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		File pdfsDir = DSATabApplication.getDirectory(DSATabApplication.DIR_PDFS);
		if (!pdfsDir.exists())
			pdfsDir.mkdirs();

		listView = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(listView);
		listView.setOnItemClickListener(this);

		File[] pdfFiles = pdfsDir.listFiles();
		List<File> documents;
		if (pdfFiles != null) {
			documents = Arrays.asList(pdfFiles);
		} else
			documents = Collections.emptyList();

		TextView empty = (TextView) findViewById(android.R.id.empty);

		if (documents.isEmpty()) {
			String path = pdfsDir.getAbsolutePath();
			path = path.replace(DSATabApplication.SD_CARD_PATH_PREFIX, "");

			empty.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			empty.setText(Util.getText(R.string.message_documents_empty, path));
		} else {

			Collections.sort(documents, new Util.FileNameComparator());
			empty.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}

		documentsListAdapter = new FileAdapter(getActivity(), android.R.layout.simple_list_item_1, documents);
		listView.setAdapter(documentsListAdapter);

		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = (File) listView.getItemAtPosition(position);

		if (file.exists()) {
			Uri path = Uri.fromFile(file);
			Intent intent = new Intent(Intent.ACTION_VIEW);

			if (file.getName().toLowerCase().endsWith(".pdf"))
				intent.setDataAndType(path, "application/pdf");
			else {
				intent.setData(path);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(getActivity(), "Keine App zum Betrachten von " + file.getName() + " gefunden",
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
