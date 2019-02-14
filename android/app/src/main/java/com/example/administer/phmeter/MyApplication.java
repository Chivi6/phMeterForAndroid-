package com.example.administer.phmeter;

import android.app.Application;
import android.content.Context;
import org.litepal.LitePal;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
        LitePal.initialize(this);
    }

    public static Context getContext(){
        return context;
    }
}
