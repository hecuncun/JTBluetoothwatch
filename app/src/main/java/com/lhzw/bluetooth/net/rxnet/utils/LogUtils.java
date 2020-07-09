package com.lhzw.bluetooth.net.rxnet.utils;

import android.util.Log;

import com.lhzw.bluetooth.net.rxnet.RxNet;


/**
 * @author LvQiSheng
 * @date 2019/6/14
 */
public class LogUtils {
    private static final String TAG = "RxNet";

    public static void d(String msg) {
        if (RxNet.enableLog) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (RxNet.enableLog) {
            Log.i(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (RxNet.enableLog) {
            Log.e(TAG, msg);
        }
    }

}
