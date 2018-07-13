package com.okay.reader.plugin.utils;

import android.util.Log;

/**
 * Class to help Logging when debug.
 */
public class LogUtils {
    public static final String LOG_ID = "okay_reader";
    public static boolean DEBUG = true;

    public static void setIsDebug(boolean isDebug){
        DEBUG = isDebug;
    }

    public static void d(String tag, String outMessage) {
        if (DEBUG) {
            Log.d(tag, outMessage);
        }
    }

    public static void d(String outMessage) {
        if (DEBUG) {
            Log.d(LOG_ID, outMessage);
        }
    }

    public static void e(String tag, String outMessage) {
        if (DEBUG) {
            Log.e(tag, outMessage);
        }
    }

    public static void e(String outMessage) {
        if (DEBUG) {
            Log.e(LOG_ID, outMessage);
        }
    }

    public static void w(String tag, String outMessage) {
        if (DEBUG) {
            Log.d(tag, outMessage);
        }
    }

    public static void w(String outMessage) {
        if (DEBUG) {
            Log.w(LOG_ID, outMessage);
        }
    }

    public static void i(String tag, String outMessage) {
        if (DEBUG) {
            Log.i(tag, outMessage);
        }
    }

    public static void i(String outMessage) {
        if (DEBUG) {
            Log.i(LOG_ID, outMessage);
        }
    }

    public static void v(String tag, String outMessage) {
        if (DEBUG) {
            Log.v(tag, outMessage);
        }
    }

    public void v(String outMessage) {
        if (DEBUG) {
            Log.v(LOG_ID, outMessage);
        }
    }
}
