package com.okay.testjni;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.okay.testjni.jni.JNICall;

public class MainActivity extends AppCompatActivity {

    private byte[] mBytes;
    private static final String TEST_STR = "i love okay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testEncrypt(TEST_STR);
        testDeEncrypt(mBytes);
        String encrypt = JNICall.encrypt(TEST_STR);
        Log.d("TAG","encrypt=="+encrypt);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String str = JNICall.getStringFromJNI();
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void testEncrypt(String inputStr) {
        mBytes = inputStr.getBytes();
        String s = new String(mBytes);
        byte[] encrypt = EncryptUtils.encrypt(mBytes);
        Log.d("TAG", "加密: " + Base64.encodeToString(encrypt, Base64.DEFAULT));
    }

    private void testDeEncrypt(byte[] bytes) {
        byte[] decrypt = EncryptUtils.decrypt(bytes);
        Log.d("TAG", "解密:" + new String(decrypt));
    }
}

