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

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.litepal.LitePal;
import org.litepal.LitePalDB;
import org.litepal.litepalsample.R;
import org.litepal.litepalsample.model.Album;
import org.litepal.litepalsample.model.Singer;
import org.litepal.litepalsample.model.Song;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "MainActivity";

    private Button mManageTableBtn;

    private Button mCrudBtn;

    private Button mAggregateBtn;

    private EditText mTargetDbNameEt;

    private Button mChangheDbBtn;
    private Button mDelDbBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mTargetDbNameEt = (EditText) findViewById(R.id.et_targetname);
        mManageTableBtn = (Button) findViewById(R.id.manage_table_btn);
        mCrudBtn = (Button) findViewById(R.id.crud_btn);
        mAggregateBtn = (Button) findViewById(R.id.aggregate_btn);
        mChangheDbBtn = (Button) findViewById(R.id.changedb_btn);
        mDelDbBtn= (Button)findViewById(R.id.deldb_btn);
        mManageTableBtn.setOnClickListener(this);
        mCrudBtn.setOnClickListener(this);
        mAggregateBtn.setOnClickListener(this);
        mChangheDbBtn.setOnClickListener(this);
        mDelDbBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.manage_table_btn:
                ManageTablesActivity.actionStart(this);
                break;
            case R.id.crud_btn:
                CRUDActivity.actionStart(this);
                break;
            case R.id.aggregate_btn:
                AggregateActivity.actionStart(this);
                break;
            case R.id.changedb_btn: {
                Editable text = mTargetDbNameEt.getText();
                if (!TextUtils.isEmpty(text)) {
                    long timeStart = System.currentTimeMillis();
                    LitePalDB litePalDB = new LitePalDB(text.toString(),1);
                    litePalDB.addClassName(Singer.class.getName());
                    litePalDB.addClassName(Album.class.getName());
                    litePalDB.addClassName(Song.class.getName());
                    Log.e("changeTimeS1", System.currentTimeMillis() - timeStart + "");
                    timeStart = System.currentTimeMillis();
                    LitePal.use(litePalDB,false);
                    Log.e("changeTimeS2", System.currentTimeMillis() - timeStart + "");
                }
            }
                break;
            case R.id.deldb_btn: {
                Editable text = mTargetDbNameEt.getText();
                if (!TextUtils.isEmpty(text)) {
                    long timeStart = System.currentTimeMillis();
                    LitePal.deleteDatabase(text.toString());
                    Log.e("deletime", System.currentTimeMillis() - timeStart + "");
                }
            }
                break;
            default:
                break;
        }
    }

}