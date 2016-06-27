package com.skyoung09.legomain;

import com.skyoung09.legolib.AppInit;

import android.app.Application;

/**
 * Created by zhangxiaobo02 on 16/6/13.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppInit.getInstance().init(this);
    }
}
