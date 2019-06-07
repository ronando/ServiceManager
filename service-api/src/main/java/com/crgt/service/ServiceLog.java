package com.crgt.service;

public class ServiceLog {
    private static String TAG = "ServiceManager";
    public static boolean enable = false;

    public static void v(String msg) {
        if (enable) {
            android.util.Log.v(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (enable) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (enable) {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (enable) {
            android.util.Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (enable) {
            android.util.Log.e(TAG, msg);
        }
    }
}
