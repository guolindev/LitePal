/*
 * Copyright (C)  Tony Green, Litepal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litepal.litepalsample.activity;

import java.util.ArrayList;
import java.util.List;

import org.litepal.litepalsample.R;
import org.litepal.litepalsample.adapter.StringArrayAdapter;
import org.litepal.tablemanager.Connector;
import org.litepal.util.DBUtility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

public class TableListActivity extends Activity {

	private ProgressBar mProgressBar;

	private ListView mTableListview;

	private StringArrayAdapter mAdapter;

	private List<String> mList = new ArrayList<String>();

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, TableListActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_list_layout);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mTableListview = (ListView) findViewById(R.id.table_listview);
		mAdapter = new StringArrayAdapter(this, 0, mList);
		mTableListview.setAdapter(mAdapter);
		populateTables();
		mTableListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
				TableStructureActivity.actionStart(TableListActivity.this, mList.get(index));
			}
		});
	}

	private void populateTables() {
		mProgressBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<String> tables = DBUtility.findAllTableNames(Connector.getDatabase());
				for (String table : tables) {
					if (table.equalsIgnoreCase("android_metadata")
							|| table.equalsIgnoreCase("sqlite_sequence")
							|| table.equalsIgnoreCase("table_schema")) {
						continue;
					}
					mList.add(table);
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mProgressBar.setVisibility(View.GONE);
						mAdapter.notifyDataSetChanged();
					}
				});
			}
		}).start();
	}

}