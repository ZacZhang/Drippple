package com.zaczhang.drippple;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;


public class DripppleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
