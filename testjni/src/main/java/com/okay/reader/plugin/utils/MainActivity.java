package com.okay.reader.plugin.utils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.okay.testjni.R;

public class MainActivity extends AppCompatActivity {

    /**
     * 到时候替换成别人的包名即可
     */
    private static final String TEST_STR = "i love okay";
    private String mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getEncryptStr();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private String getEncryptStr() {
        String encrypt = JNICall.encrypt(TEST_STR);
        String result = new String(Base64.encode(encrypt.getBytes(), Base64.DEFAULT));
        Log.d("TAG", "JNI result==" + result);
        return result;
    }

    private void testEncrypt(String inputStr) {
        int key = 0x12;
        char[] chars = inputStr.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ key);
        }
        mResult = String.valueOf(chars);
        Log.d("TAG", "加密testEncrypt=" + mResult);
    }

    private void testDecrypt(String inputStr) {
        char[] chars = inputStr.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ 0x12);
        }
        String result = String.valueOf(chars);
        Log.d("TAG", "解密testDecrypt=" + result);
    }
}

