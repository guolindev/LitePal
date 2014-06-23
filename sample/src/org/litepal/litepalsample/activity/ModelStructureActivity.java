package org.litepal.litepalsample.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.litepal.litepalsample.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ModelStructureActivity extends Activity {
	
	public static final String CLASS_NAME = "class_name";

	private ListView modelStructureListView;
	
	private ArrayAdapter<String> adapter;
	
	private String mClassName;

	private List<String> list = new ArrayList<String>();
	
	public static void actionStart(Context context, String className) {
		Intent intent = new Intent(context, ModelStructureActivity.class);
		intent.putExtra(CLASS_NAME, className);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_structure_layout);
		mClassName = getIntent().getStringExtra(CLASS_NAME);
		modelStructureListView = (ListView) findViewById(R.id.model_structure_listview);
		populateMappingClasses();
		adapter = new MyArrayAdapter(this, 0, list);
		modelStructureListView.setAdapter(adapter);
	}
	
	private void analyzeModelStructure() {
		try {
			Class <?> c = Class.forName(mClassName);
			Field [] fields = c.getDeclaredFields();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void populateMappingClasses() {
		for (int i = 0; i < 50; i++) {
			list.add("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		}
	}

	class MyArrayAdapter extends ArrayAdapter<String> {

		public MyArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = LayoutInflater.from(ModelStructureActivity.this).inflate(R.layout.list_view_item,
						null);
			} else {
				view = convertView;
			}
			TextView textView = (TextView) view.findViewById(R.id.text_1);
			textView.setText(getItem(position));
			return view;
		}

	}

}