/**
 * Copyright 2016 JustWayward Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okay.reader.plugin.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;

public class AppUtils {

    private static Context mContext;
    private static Thread mUiThread;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void init(Context context) {
        mContext = context;
        mUiThread = Thread.currentThread();
    }

    public static Context getAppContext() {
        return mContext;
    }

    public static AssetManager getAssets() {
        return mContext.getAssets();
    }

    public static Resources getResource() {
        return mContext.getResources();
    }

    public static boolean isUIThread() {
        return Thread.currentThread() == mUiThread;
    }

    public static void runOnUI(Runnable r) {
        sHandler.post(r);
    }

    public static void runOnUIDelayed(Runnable r, long delayMills) {
        sHandler.postDelayed(r, delayMills);
    }

    public static void removeRunnable(Runnable r) {
        if (r == null) {
            sHandler.removeCallbacksAndMessages(null);
        } else {
            sHandler.removeCallbacks(r);
        }
    }

    /**
     * 获取一张图片最大可设置的内存
     *
     * @param bitmapSize 当前内存中最大可存在的bitmap的数量
     * @return
     */
    public static int getPageMaxMemory(int bitmapSize) {
        mContext = mContext.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);//k
        int cacheSize = maxMemory / (bitmapSize + 1);
        LogUtils.d("AppUtils getAppMemory maxMemory=" + maxMemory + " 一张图片最大可用内存cacheSize(KB)=" + cacheSize);
        return cacheSize;
    }

    public static int getCurrentPdfContainerWidth() {
        if (Constant.isShowInMainSceen) {
            if (Constant.pdfContainerWidth <= 0) {
                return getAppContext().getResources().getDisplayMetrics().widthPixels;
            } else {
                return Constant.pdfContainerWidth;
            }
        } else {
            return getEPDDisplayMetrics().widthPixels;
        }

    }

    public static int getCurrentPdfContainerHeight() {
        if (Constant.isShowInMainSceen) {
            if (Constant.pdfContainerHeight <= 0) {
                return getAppContext().getResources().getDisplayMetrics().heightPixels;
            } else {
                return Constant.pdfContainerHeight;
            }
        } else {
            return getEPDDisplayMetrics().heightPixels;
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth() {
        if (Constant.isShowInMainSceen) {
            return AppUtils.getAppContext().getResources().getDisplayMetrics().widthPixels;
        } else {
            return getEPDDisplayMetrics().widthPixels;
        }
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight() {
        if (Constant.isShowInMainSceen) {

            return AppUtils.getAppContext().getResources().getDisplayMetrics().heightPixels;
        } else {
            return getEPDDisplayMetrics().heightPixels;
        }
    }

    public static boolean isSwtcon2() {
        return Build.DISPLAY.contains("OKUI_4.3");
    }

    private static DisplayMetrics getEPDDisplayMetrics() {
        Context context = AppUtils.getAppContext();
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager.getDisplays();
        Display display;
        if (presentationDisplays.length > 1) {
            display = presentationDisplays[1];
        } else {
            display = presentationDisplays[0];
        }
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        LogUtils.d("widthPixels=" + metrics.widthPixels + " heightPixels=" + metrics.heightPixels
                + " density=" + metrics.density + " " + " densityDpi=" + metrics.densityDpi);
        return metrics;
    }

    public static boolean isOrientationLandscape(Context context) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        }
        return false;
    }

    public static int getNavigationBarHeight() {
        Resources resources = mContext.getResources();
        int resIdShow = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        boolean hasNavigationBar = false;
        if (resIdShow > 0) {
            hasNavigationBar = resources.getBoolean(resIdShow);//是否显示底部navigationBar
        }
        if (hasNavigationBar) {
            int resIdNavigationBar = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            int navigationbarHeight = 0;
            if (resIdNavigationBar > 0) {
                navigationbarHeight = resources.getDimensionPixelSize(resIdNavigationBar);//navigationBar高度
                return navigationbarHeight;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = getAppContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getAppContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getEpadPdfMinHeight() {
        int epadHeight = 800;
        int result;
        if (isEPAD()) {
            result = epadHeight - Constant.MAIN_TITLE_HEIGHT - getNavigationBarHeight();
        } else {
            result = -1;
        }
        return result;
    }

    public static int getEpadPdfMaxHeight() {
        int epadHeight = 800;
        int result;
        if (isEPAD()) {
            result = epadHeight - Constant.MAIN_TITLE_HEIGHT;
        } else {
            result = -1;
        }
        return result;
    }

    public static boolean isEPAD() {
        if (Build.DISPLAY.startsWith("OKAY_EPAD_2")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置lcd屏幕的开关
     *
     * @param onOff 　　0:关　１：开
     */
    public static void setLCDSwitchOnOff(String onOff) {
        if (isSwtcon2()) {
            SystemPropertyUtils.setSystemProperty(mContext, "sys.lcd.state", onOff);
        } else {
            SystemPropertyUtils.setSystemProperty(mContext, "sys.primarypanel.enable", onOff); //打开LCD
        }

        if (onOff.equals("0")) {
            SystemPropertyUtils.setSystemProperty(mContext, "sys.close.mainTp", "1");
            SystemPropertyUtils.setSystemProperty(mContext, "sys.close.mainPen", "1");
        } else {
            SystemPropertyUtils.setSystemProperty(mContext, "sys.close.mainTp", "0");
            SystemPropertyUtils.setSystemProperty(mContext, "sys.close.mainPen", "0");
        }
    }

    /**
     * 优化不同设备的体验效果
     *
     * @return
     */
    public static int getDelayTime() {
        if (isEPAD()) {
            return 200;
        } else {
            return 0;
        }
    }
}

