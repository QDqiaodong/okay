package com.okay.reader.plugin.utils;

/**
 * Created by qiaodong on 18-7-7.
 */

public class JNICall {

    static {
        System.loadLibrary("TestJNI");
    }
    public static native String encrypt(String inputStr);
}
