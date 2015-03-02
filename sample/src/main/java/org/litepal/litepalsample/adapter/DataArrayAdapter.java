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

package org.litepal.litepalsample.adapter;

import java.util.List;

import org.litepal.litepalsample.util.Utility;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataArrayAdapter extends ArrayAdapter<List<String>> {

	public DataArrayAdapter(Context context, int textViewResourceId, List<List<String>> objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		List<String> dataList = getItem(position);
		LinearLayout layout;
		if (convertView == null) {
			layout = new LinearLayout(getContext());
		} else {
			layout = (LinearLayout) convertView;
		}
		layout.removeAllViews();
		int width = Utility.dp2px(getContext(), 100);
		int height = Utility.dp2px(getContext(), 30);
		for (String data : dataList) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
			TextView textView = new TextView(getContext());
			textView.setText(data);
			textView.setSingleLine(true);
			textView.setEllipsize(TruncateAt.END);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			layout.addView(textView, params);
		}
		return layout;
	}

}