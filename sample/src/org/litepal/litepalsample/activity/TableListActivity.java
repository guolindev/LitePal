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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

public class TableListActivity extends Activity {
	
	private ListView tableListview;

	private StringArrayAdapter adapter;

	private List<String> list = new ArrayList<String>();
	
	public static void actionStart(Context context) {
		Intent intent = new Intent(context, TableListActivity.class);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_list_layout);
		tableListview = (ListView) findViewById(R.id.table_listview);
		adapter = new StringArrayAdapter(this, 0, list);
		tableListview.setAdapter(adapter);
	}
	
}