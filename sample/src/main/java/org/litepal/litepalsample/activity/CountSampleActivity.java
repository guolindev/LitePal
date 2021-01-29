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
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.litepalsample.R;
import org.litepal.litepalsample.model.Singer;

public class CountSampleActivity extends AppCompatActivity implements OnClickListener {

    private EditText mAgeEdit;

	private TextView mResultText;

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, CountSampleActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.count_sample_layout);
        Button mCountBtn1 = findViewById(R.id.count_btn1);
        Button mCountBtn2 = findViewById(R.id.count_btn2);
		mAgeEdit = findViewById(R.id.age_edit);
		mResultText = findViewById(R.id.result_text);
		mCountBtn1.setOnClickListener(this);
		mCountBtn2.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		int result = 0;
		switch (view.getId()) {
		case R.id.count_btn1:
			result = LitePal.count(Singer.class);
			mResultText.setText(String.valueOf(result));
			break;
		case R.id.count_btn2:
			try {
				result = LitePal.where("age > ?", mAgeEdit.getText().toString()).count(
						Singer.class);
				mResultText.setText(String.valueOf(result));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
		}
	}

}