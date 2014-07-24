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
import org.litepal.litepalsample.model.Singer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SaveSampleActivity extends Activity implements OnClickListener {

	private Button saveBtn;
	
	public static void actionStart(Context context) {
		Intent intent = new Intent(context, SaveSampleActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_sample_layout);
		saveBtn = (Button) findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save_btn:
			Singer singer = new Singer();
			singer.setName("Taylor Swift");
			singer.setAge(25);
			singer.setMale(false);
			singer.save();
			break;
		default:
			break;
		}
	}

}
