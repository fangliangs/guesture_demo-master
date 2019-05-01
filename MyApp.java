package com.example.android.tflitecamerademo;

import android.app.Application;
import android.content.Context;

import com.vondear.rxtool.RxTool;

public class MyApp extends Application {
    private static Context context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        RxTool.init(this);
    }


    public static Context getContext() {
        return context;
    }


}
