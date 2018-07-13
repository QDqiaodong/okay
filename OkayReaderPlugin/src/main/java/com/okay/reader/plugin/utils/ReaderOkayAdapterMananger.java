package com.okay.reader.plugin.utils;

import android.app.OKAYAdapterManager;
import android.content.Context;

/**
 * Created by qiaodong on 18-6-13.
 */

public class ReaderOkayAdapterMananger {



    private OKAYAdapterManager mOkayAdapterManager;

    private ReaderOkayAdapterMananger() {

    }

    private static class SingletonPatternHolder {
        private static ReaderOkayAdapterMananger instance = new ReaderOkayAdapterMananger();
    }

    public static ReaderOkayAdapterMananger getInstance() {
        return SingletonPatternHolder.instance;
    }

    public void init(Context context) {
        mOkayAdapterManager = (OKAYAdapterManager) context.getSystemService("okay");
    }

    public void performOpenEinkTouch(Context context) {
        if (Constant.isDoubleScreen) {
            if (AppUtils.isSwtcon2()) {
                if (mOkayAdapterManager != null) {
                    mOkayAdapterManager.setEinkMode(1);
                    mOkayAdapterManager.setEinkDither(1);
                    mOkayAdapterManager.enableEinkForceUpdate();
                }
                SystemPropertyUtils.setSystemProperty(context, "sys.close.subInvalid", "1");
            } else {
                SystemPropertyUtils.setSystemProperty(context, "sys.eink.Appmode", "11"); //图片用MODE_GLD16
                SystemPropertyUtils.setSystemProperty(context, "sys.close.subPen", "0"); //笔触开关
                SystemPropertyUtils.setSystemProperty(context, "sys.close.subKey", "0");//电容开关
                SystemPropertyUtils.setSystemProperty(context, "sys.close.subTp", "0");//eink屏幕手触开关
                SystemPropertyUtils.setSystemProperty(context, "sys.close.subInvalid", "1");
            }
        }
    }

    public void performResumeEinkTouch(Context context) {
        if (Constant.isDoubleScreen) {
            int angleMode = android.provider.Settings.Global.getInt(context.getContentResolver(),
                    "okay_angle_mode", 0);
            if (angleMode > 0) {
                if (AppUtils.isSwtcon2()) {
                    //nothing
                } else {
                    SystemPropertyUtils.setSystemProperty(context, "sys.close.subPen", "1");
                    SystemPropertyUtils.setSystemProperty(context, "sys.close.subKey", "1");
                    SystemPropertyUtils.setSystemProperty(context, "sys.close.subTp", "1");
                }
            }
            SystemPropertyUtils.setSystemProperty(context, "sys.close.subInvalid", "0");
        }
    }

    public void forceUpdate() {
        if (AppUtils.isSwtcon2() && !Constant.isShowInMainSceen) {
            if (mOkayAdapterManager != null) {
                mOkayAdapterManager.enableEinkForceUpdate();
            }
        }
    }
}
