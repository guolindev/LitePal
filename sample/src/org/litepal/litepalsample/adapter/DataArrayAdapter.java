package org.litepal.litepalsample.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
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
		for (String data : dataList) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 50);
			TextView textView = new TextView(getContext());
			textView.setText(data);
			textView.setEllipsize(TruncateAt.END);
			layout.addView(textView, params);
		}
		return layout;
	}

}