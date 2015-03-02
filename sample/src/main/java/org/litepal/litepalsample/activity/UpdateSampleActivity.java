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
import org.litepal.litepalsample.adapter.DataArrayAdapter;
import org.litepal.litepalsample.model.Singer;
import org.litepal.tablemanager.Connector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class UpdateSampleActivity extends Activity implements OnClickListener {

	private EditText mSingerIdEdit;

	private EditText mSingerNameEdit;

	private EditText mSingerAgeEdit;

	private EditText mNameToUpdateEdit;

	private EditText mAgeToUpdateEdit;

	private ProgressBar mProgressBar;

	private Button mUpdateBtn1;

	private Button mUpdateBtn2;

	private ListView mDataListView;

	private DataArrayAdapter mAdapter;

	private List<List<String>> mList = new ArrayList<List<String>>();

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, UpdateSampleActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_sample_layout);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mSingerIdEdit = (EditText) findViewById(R.id.singer_id_edit);
		mSingerNameEdit = (EditText) findViewById(R.id.singer_name_edit);
		mSingerAgeEdit = (EditText) findViewById(R.id.singer_age_edit);
		mNameToUpdateEdit = (EditText) findViewById(R.id.name_to_update);
		mAgeToUpdateEdit = (EditText) findViewById(R.id.age_to_update);
		mUpdateBtn1 = (Button) findViewById(R.id.update_btn1);
		mUpdateBtn2 = (Button) findViewById(R.id.update_btn2);
		mDataListView = (ListView) findViewById(R.id.data_list_view);
		mUpdateBtn1.setOnClickListener(this);
		mUpdateBtn2.setOnClickListener(this);
		mAdapter = new DataArrayAdapter(this, 0, mList);
		mDataListView.setAdapter(mAdapter);
		populateDataFromDB();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.update_btn1:
			try {
				Singer singer = new Singer();
				singer.setName(mSingerNameEdit.getText().toString());
				singer.setAge(Integer.parseInt(mSingerAgeEdit.getText().toString()));
				int rowsAffected = singer
						.update(Long.parseLong(mSingerIdEdit.getText().toString()));
				Toast.makeText(
						this,
						String.format(getString(R.string.number_of_rows_affected),
								String.valueOf(rowsAffected)), Toast.LENGTH_SHORT).show();
				populateDataFromDB();
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, getString(R.string.error_param_is_not_valid),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.update_btn2:
			try {
				Singer singer = new Singer();
				singer.setName(mSingerNameEdit.getText().toString());
				singer.setAge(Integer.parseInt(mSingerAgeEdit.getText().toString()));
				int rowsAffected = singer.updateAll("name=? and age=?", mNameToUpdateEdit.getText()
						.toString(), mAgeToUpdateEdit.getText().toString());
				Toast.makeText(
						this,
						String.format(getString(R.string.number_of_rows_affected),
								String.valueOf(rowsAffected)), Toast.LENGTH_SHORT).show();
				populateDataFromDB();
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, getString(R.string.error_param_is_not_valid),
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	private void populateDataFromDB() {
		mProgressBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				mList.clear();
				List<String> columnList = new ArrayList<String>();
				columnList.add("id");
				columnList.add("name");
				columnList.add("age");
				columnList.add("ismale");
				mList.add(columnList);
				Cursor cursor = null;
				try {
					cursor = Connector.getDatabase().rawQuery("select * from singer order by id",
							null);
					if (cursor.moveToFirst()) {
						do {
							long id = cursor.getLong(cursor.getColumnIndex("id"));
							String name = cursor.getString(cursor.getColumnIndex("name"));
							int age = cursor.getInt(cursor.getColumnIndex("age"));
							int isMale = cursor.getInt(cursor.getColumnIndex("ismale"));
							List<String> stringList = new ArrayList<String>();
							stringList.add(String.valueOf(id));
							stringList.add(name);
							stringList.add(String.valueOf(age));
							stringList.add(String.valueOf(isMale));
							mList.add(stringList);
						} while (cursor.moveToNext());
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (cursor != null) {
						cursor.close();
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mProgressBar.setVisibility(View.GONE);
							mAdapter.notifyDataSetChanged();
						}
					});
				}
			}
		}).start();
	}

}