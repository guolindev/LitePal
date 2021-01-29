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
import android.content.res.AssetManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.litepal.LitePalApplication;
import org.litepal.exceptions.ParseConfigurationFileException;
import org.litepal.litepalsample.R;
import org.litepal.litepalsample.adapter.StringArrayAdapter;
import org.litepal.util.Const;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ModelListActivity extends AppCompatActivity {

    private List<String> mList = new ArrayList<>();

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, ModelListActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_list_layout);
        ListView mModelListView = findViewById(R.id.model_listview);
		populateMappingClasses();
        StringArrayAdapter mAdapter = new StringArrayAdapter(this, 0, mList);
        mModelListView.setAdapter(mAdapter);
        mModelListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
				ModelStructureActivity.actionStart(ModelListActivity.this, mList.get(index));
			}
		});
	}

	private void populateMappingClasses() {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			xmlPullParser.setInput(getInputStream(), "UTF-8");
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName = xmlPullParser.getName();
				switch (eventType) {
				case XmlPullParser.START_TAG: {
					if ("mapping".equals(nodeName)) {
						String className = xmlPullParser.getAttributeValue("", "class");
						mList.add(className);
					}
					break;
				}
				default:
					break;
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParseConfigurationFileException(
					ParseConfigurationFileException.FILE_FORMAT_IS_NOT_CORRECT);
		} catch (IOException e) {
			throw new ParseConfigurationFileException(ParseConfigurationFileException.IO_EXCEPTION);
		}
	}

	private InputStream getInputStream() throws IOException {
		AssetManager assetManager = LitePalApplication.getContext().getAssets();
		String[] fileNames = assetManager.list("");
		if (fileNames != null && fileNames.length > 0) {
			for (String fileName : fileNames) {
				if (Const.Config.CONFIGURATION_FILE_NAME.equalsIgnoreCase(fileName)) {
					return assetManager.open(fileName, AssetManager.ACCESS_BUFFER);
				}
			}
		}
		throw new ParseConfigurationFileException(
				ParseConfigurationFileException.CAN_NOT_FIND_LITEPAL_FILE);
	}

}