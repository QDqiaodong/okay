package com.okay.testjni.jni;

/**
 * Created by qiaodong on 18-7-7.
 */

public class JNICall {

    static {
        System.loadLibrary("TestJNI");
    }

    public static native String getStringFromJNI();

    public static native String encrypt(String inputStr);

    public static native void decrypt(byte[] bytes);
}
