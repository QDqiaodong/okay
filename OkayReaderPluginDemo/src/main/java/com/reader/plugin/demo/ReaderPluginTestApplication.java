package com.reader.plugin.demo;

import android.app.Application;

import com.okay.reader.plugin.PDFManager;


/**
 * Created by qiaodong on 17-12-2.
 */

public class ReaderPluginTestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PDFManager.getInstance().init(this);
    }
}
