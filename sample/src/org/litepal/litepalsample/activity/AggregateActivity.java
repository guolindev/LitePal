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

import org.litepal.litepalsample.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AggregateActivity extends Activity implements OnClickListener {

	private Button mCountSampleBtn;

	private Button mMaxSampleBtn;

	private Button mMinSampleBtn;

	private Button mAverageSampleBtn;
	
	private Button mSumSampleBtn;

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, AggregateActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aggregate_layout);
		mCountSampleBtn = (Button) findViewById(R.id.count_sample_btn);
		mMaxSampleBtn = (Button) findViewById(R.id.max_sample_btn);
		mMinSampleBtn = (Button) findViewById(R.id.min_sample_btn);
		mAverageSampleBtn = (Button) findViewById(R.id.average_sample_btn);
		mSumSampleBtn = (Button) findViewById(R.id.sum_sample_btn);
		mCountSampleBtn.setOnClickListener(this);
		mMaxSampleBtn.setOnClickListener(this);
		mMinSampleBtn.setOnClickListener(this);
		mAverageSampleBtn.setOnClickListener(this);
		mSumSampleBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.count_sample_btn:
			CountSampleActivity.actionStart(this);
			break;
		case R.id.max_sample_btn:
			break;
		case R.id.min_sample_btn:
			break;
		case R.id.average_sample_btn:
			break;
		case R.id.sum_sample_btn:
			break;
		default:
			break;
		}
	}

}