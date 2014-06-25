package org.litepal.litepalsample.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.litepal.litepalsample.R;
import org.litepal.util.BaseUtility;

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

	private ArrayAdapter<Field> adapter;

	private String mClassName;

	private List<Field> list = new ArrayList<Field>();

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
		analyzeModelStructure();
		adapter = new MyArrayAdapter(this, 0, list);
		modelStructureListView.setAdapter(adapter);
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
					list.add(field);
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
				view = LayoutInflater.from(getContext()).inflate(R.layout.model_structure_item,
						null);
			} else {
				view = convertView;
			}
			TextView text1 = (TextView) view.findViewById(R.id.text_1);
			text1.setText(field.getName());
			TextView text2 = (TextView) view.findViewById(R.id.text_2);
			text2.setText(field.getType().getName());
			return view;
		}

	}

}