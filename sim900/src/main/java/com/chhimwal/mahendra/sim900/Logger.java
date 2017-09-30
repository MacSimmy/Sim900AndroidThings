package com.chhimwal.mahendra.sim900;

import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by mahendra on 28/9/17.
 */

public class Logger {

    public static final int DEBUG = 0;
    public static final int ERROR = 1;
    public static final int NONE = -1;

    @Retention(SOURCE)
    @IntDef({DEBUG, ERROR, NONE})
    @interface LEVEL {
    }

    @LEVEL
    private static int mLevel = NONE;

    public static void setLogLevel(@LEVEL int level) {
        mLevel = level;
    }

    static void d(String TAG, String message) {
        if (mLevel >= DEBUG) {
            Log.d(TAG, message);
        }
    }

    static void e(String TAG, String message) {
        if (mLevel >= ERROR) {
            Log.e(TAG, message);
        }
    }
}
