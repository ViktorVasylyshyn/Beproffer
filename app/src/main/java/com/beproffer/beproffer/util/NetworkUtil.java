package com.beproffer.beproffer.util;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkUtil {

    private NetworkUtil() {
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
