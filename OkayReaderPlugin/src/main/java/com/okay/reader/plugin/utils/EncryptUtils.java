package com.okay.reader.plugin.utils;

import android.content.Context;
import android.util.Base64;

/**
 * Created by qiaodong on 18-7-20.
 */

public class EncryptUtils {
    public static boolean isAuth(Context context, String key) {
        String result = getEncryptStr(context);
        LogUtils.d("qd","=====result="+result);
        LogUtils.d("qd","=====keyyyy="+key);
        if (key.equals(result)) {
            LogUtils.d("qd","success...........");
            return true;
        } else {
            LogUtils.d("qd","faile...........");
            return false;
        }
    }

    private static String getEncryptStr(Context context) {
        String packageName = context.getPackageName();
        String encrypt = JNICall.encrypt(packageName);
        String result = new String(Base64.encode(encrypt.getBytes(), Base64.DEFAULT));
        LogUtils.d("packagename=" + context.getPackageName() + " result=" + result + " encrypt=" + encrypt);
        return result;
    }
}
