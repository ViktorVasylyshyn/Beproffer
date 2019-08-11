package com.beproffer.beproffer.util;

import android.os.Build;

public class OsVersionInfo {

    private OsVersionInfo() {}

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
