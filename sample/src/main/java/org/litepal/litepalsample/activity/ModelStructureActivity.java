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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.litepalsample.R;
import org.litepal.util.BaseUtility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ModelStructureActivity extends AppCompatActivity {

	public static final String CLASS_NAME = "class_name";

    private String mClassName;

	private List<Field> mList = new ArrayList<>();

	public static void actionStart(Context context, String className) {
		Intent intent = new Intent(context, ModelStructureActivity.class);
		intent.putExtra(CLASS_NAME, className);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.model_structure_layout);
		mClassName = getIntent().getStringExtra(CLASS_NAME);
        ListView mModelStructureListView = findViewById(R.id.model_structure_listview);
		analyzeModelStructure();
        ArrayAdapter<Field> mAdapter = new MyArrayAdapter(this, 0, mList);
		mModelStructureListView.setAdapter(mAdapter);
	}

	private void analyzeModelStructure() {
		Class<?> dynamicClass = null;
		try {
			dynamicClass = Class.forName(mClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Field[] fields = dynamicClass.getDeclaredFields();
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isPrivate(modifiers) && !Modifier.isStatic(modifiers)) {
				Class<?> fieldTypeClass = field.getType();
				String fieldType = fieldTypeClass.getName();
				if (BaseUtility.isFieldTypeSupported(fieldType)) {
					mList.add(field);
				}
			}
		}
	}

	class MyArrayAdapter extends ArrayAdapter<Field> {

		public MyArrayAdapter(Context context, int textViewResourceId, List<Field> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			Field field = getItem(position);
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(R.layout.model_structure_item, null);
			} else {
				view = convertView;
			}
			TextView text1 = view.findViewById(R.id.text_1);
			text1.setText(field.getName());
			TextView text2 = view.findViewById(R.id.text_2);
			text2.setText(field.getType().getName());
			return view;
		}

	}

}