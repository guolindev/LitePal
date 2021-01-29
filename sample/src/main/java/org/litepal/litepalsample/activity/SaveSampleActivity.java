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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.litepal.litepalsample.R;
import org.litepal.litepalsample.adapter.DataArrayAdapter;
import org.litepal.litepalsample.model.Singer;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

public class SaveSampleActivity extends AppCompatActivity implements OnClickListener {

	private EditText mSingerNameEdit;

	private EditText mSingerAgeEdit;

	private EditText mSingerGenderEdit;

	private ProgressBar mProgressBar;

    private ListView mDataListView;

	private DataArrayAdapter mAdapter;

	private List<List<String>> mList = new ArrayList<>();

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, SaveSampleActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_sample_layout);
		mProgressBar = findViewById(R.id.progress_bar);
		mSingerNameEdit = findViewById(R.id.singer_name_edit);
		mSingerAgeEdit = findViewById(R.id.singer_age_edit);
		mSingerGenderEdit = findViewById(R.id.singer_gender_edit);
        Button mSaveBtn = findViewById(R.id.save_btn);
		mDataListView = findViewById(R.id.data_list_view);
		mSaveBtn.setOnClickListener(this);
		mAdapter = new DataArrayAdapter(this, 0, mList);
		mDataListView.setAdapter(mAdapter);
		populateDataFromDB();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save_btn:
			try {
				Singer singer = new Singer();
				singer.setName(mSingerNameEdit.getText().toString());
				singer.setAge(Integer.parseInt(mSingerAgeEdit.getText().toString()));
				singer.setMale(Boolean.parseBoolean(mSingerGenderEdit.getText().toString()));
				singer.save();
				refreshListView(singer.getId(), singer.getName(), singer.getAge(),
						singer.isMale() ? 1 : 0);
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

	private void refreshListView(long id, String name, int age, int isMale) {
		List<String> stringList = new ArrayList<String>();
		stringList.add(String.valueOf(id));
		stringList.add(name);
		stringList.add(String.valueOf(age));
		stringList.add(String.valueOf(isMale));
		mList.add(stringList);
		mAdapter.notifyDataSetChanged();
		mDataListView.setSelection(mList.size());
	}

}