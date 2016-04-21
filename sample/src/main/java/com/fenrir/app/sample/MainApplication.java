package com.fenrir.app.sample;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by yume on 16-4-21.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
