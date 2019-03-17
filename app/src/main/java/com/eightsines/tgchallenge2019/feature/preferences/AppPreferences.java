package com.eightsines.tgchallenge2019.feature.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {
    private static final String KEY_NIGHT_MODE = "NIGHT_MODE";

    private SharedPreferences sp;

    public AppPreferences(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public boolean isNightMode() {
        return sp.getBoolean(KEY_NIGHT_MODE, false);
    }

    public void setNightMode(boolean value) {
        sp.edit().putBoolean(KEY_NIGHT_MODE, value).apply();
    }
}
