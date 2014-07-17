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
import java.util.Map;
import java.util.Map.Entry;

import org.litepal.litepalsample.R;
import org.litepal.tablemanager.Connector;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.DBUtility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TableStructureActivity extends Activity {

	public static final String TABLE_NAME = "table_name";

	private ListView mTableStructureListView;

	private ArrayAdapter<Entry<String, String>> mAdapter;

	private String mTableName;

	private List<Entry<String, String>> mList = new ArrayList<Entry<String, String>>();

	public static void actionStart(Context context, String tableName) {
		Intent intent = new Intent(context, TableStructureActivity.class);
		intent.putExtra(TABLE_NAME, tableName);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.table_structure_layout);
		mTableName = getIntent().getStringExtra(TABLE_NAME);
		mTableStructureListView = (ListView) findViewById(R.id.table_structure_listview);
		analyzeTableStructure();
		mAdapter = new MyArrayAdapter(this, 0, mList);
		mTableStructureListView.setAdapter(mAdapter);
	}

	private void analyzeTableStructure() {
		TableModel tableMode = DBUtility.findPragmaTableInfo(mTableName, Connector.getDatabase());
		Map<String, String> columnsMap = tableMode.getColumns();
		for (Entry<String, String> entry : columnsMap.entrySet()) {
			mList.add(entry);
		}
	}

	class MyArrayAdapter extends ArrayAdapter<Entry<String, String>> {

		public MyArrayAdapter(Context context, int textViewResourceId, List<Entry<String, String>> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			Entry<String, String> entry = getItem(position);
			String columnName = entry.getKey();
			String columnType = entry.getValue();
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(R.layout.structure_item, null);
			} else {
				view = convertView;
			}
			TextView text1 = (TextView) view.findViewById(R.id.text_1);
			text1.setText(columnName);
			TextView text2 = (TextView) view.findViewById(R.id.text_2);
			text2.setText(columnType);
			return view;
		}

	}

}