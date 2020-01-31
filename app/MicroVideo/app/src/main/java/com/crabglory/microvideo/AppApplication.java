package com.crabglory.microvideo;

import android.app.Application;
import android.content.Context;

import com.crabglory.microvideo.micro.Constants;

public class AppApplication extends Application {
    private static Context mContext;



    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        Constants.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }

    public static Context getContext() {
        return mContext;
    }
}
