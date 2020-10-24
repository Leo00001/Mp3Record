package com.baiyuas.media;

import android.app.Application;

/**
 * @author 拜雨
 * @date 2020-10
 */
public class App extends Application {
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
