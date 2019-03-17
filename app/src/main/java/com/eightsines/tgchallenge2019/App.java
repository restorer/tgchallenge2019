package com.eightsines.tgchallenge2019;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.eightsines.tgchallenge2019.feature.store.AppStore;

public class App extends Application {
    private static App instance;
    private AppStore store = new AppStore();

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public AppStore getStore() {
        return store;
    }

    public static App getInstance() {
        return instance;
    }
}
