package com.okay.reader.plugin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by ZhanTao on 9/21/16.
 */
public class ViewUtils {

    private static EnhancedHandler handler;

    private static WeakReference<Thread> thread_ref;
    private static boolean initialized = false;


    private ViewUtils() {
    }
    /**
     * Initialize class.
     */
    public static void initInMainThread() {
        if (!initialized) {
            handler = new EnhancedHandler();
            thread_ref = new WeakReference<Thread>(Thread.currentThread());
            initialized = true;
        }
    }

    /**
     * Clear class fields when this is called.
     */
    public static void destroy() {
        handler = null;
        initialized = false;
    }

    /**
     * Run in handler thread.
     *
     * @param runnable
     */
    public static void runInHandlerThread(Runnable runnable) {
        if (handler != null)
            handler.runInHandlerThread(runnable);
    }

    /**
     * Post a in handler thread.
     *
     * @param runnable
     */
    public static void runInHandlerThreadDelay(Runnable runnable) {
        if (handler != null)
            handler.runInHandlerThreadDelay(runnable);
    }

    /**
     * Call a method or command in handler thread.
     *
     * @param <T>
     * @param callable
     * @param defaultValue
     * @return T
     */
    public static <T> T callInHandlerThread(Callable<T> callable, T defaultValue) {
        if (handler != null)
            return handler.callInHandlerThread(callable, defaultValue);
        return defaultValue;
    }

    /**
     * Post methods or commands in handler thread.
     *
     * @param <T>
     * @param callable
     * @return Future<T>
     */
    public static <T> Future<T> postCallable(Callable<T> callable) {
        if (handler != null)
            return handler.postCallable(callable);
        return null;
    }

    /**
     * Post a command in handler thread with specific timespan.
     *
     * @param r
     * @param delayMillis
     */
    public static void postDelayed(Runnable r, long delayMillis) {
        if (handler != null)
            handler.postDelayed(r, delayMillis);
    }

    /**
     * @param r
     */
    public static void removeRunnable(Runnable r) {
        if (handler != null)
            handler.removeCallbacks(r);
    }

    /**
     * Post a command in handler thread.
     *
     * @param r
     */
    public static void post(Runnable r) {
        if (handler != null)
            handler.post(r);
    }

    /**
     * Check whether it's main thread.
     *
     * @return
     */
    public static boolean isMainThread() {
        if (!initialized)
            return true; // always return true if the app is not initialized
        // properly
        if (thread_ref != null)
            return thread_ref.get() == Thread.currentThread();
        return false;
    }


    public static Bitmap doScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        if (null != bitmap) {
//            ViewUtils.showToast("get screen shot!", Toast.LENGTH_LONG);
            return bitmap;
        } else {
            try {
                bitmap = Bitmap.createBitmap(view.getWidth(),
                        view.getHeight(), Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError oom) {
                try {
                    bitmap = Bitmap.createBitmap(view.getWidth(),
                            view.getHeight(), Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError oom2) {
                    oom2.printStackTrace();
                }
            }

            if (bitmap != null) {
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
            }

            return bitmap;
        }
    }


    public static void setImageResource(Context context, ImageView imageView, int resId) {
        try {
            imageView.setImageResource(resId);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            try {
                imageView.setImageBitmap(ViewUtils.readBitmap(context, resId));
            } catch (OutOfMemoryError e1) {
                e1.printStackTrace();
            }
        }
    }


    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitmap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }


    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }
        return hasNavigationBar;
    }


    public static String getString(int id) {
        return AppUtils.getAppContext().getString(id);
    }

    public static String[] getStringArray(int id) {
        return AppUtils.getAppContext().getResources().getStringArray(id);
    }


    /**
     * Show SoftKeyboard of system whenever needed.
     *
     * @param text
     */
    public static void showSoftKeyboard(EditText text) {
        try {
            text.requestFocusFromTouch();
            ((InputMethodManager) text.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(text, 0);
        } catch (Exception e) {
            LogUtils.e("Failed to show soft keyboard e.toString="+e.toString());
        }
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm && imm.isActive()) {
            View view = activity.getCurrentFocus();
            if (null != view) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void gone(final View... views) {
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public static void visible(final View... views) {
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    public static boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

}
