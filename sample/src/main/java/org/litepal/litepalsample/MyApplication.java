package org.litepal.litepalsample;

import android.app.Application;

import org.litepal.LitePalApplication;

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        LitePalApplication.initialize(this);
    }
}
