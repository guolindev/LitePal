package org.litepal.litepalsample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ManageTablesActivity extends Activity {

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, ManageTablesActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

}
