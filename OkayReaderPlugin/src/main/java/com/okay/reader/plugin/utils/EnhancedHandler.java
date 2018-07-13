package com.okay.reader.plugin.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


/**
 * @author Guo Ming
 */
public class EnhancedHandler extends Handler {

    private static final String TAG = "EnhancedHandler";

    private final Thread _thread;

    public EnhancedHandler() {
        super();

        _thread = Thread.currentThread();

//        if (CardConfig.DEV_BUILD) {
//            final Class<? extends Handler> klass = getClass();
//            if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass())
//                    && (klass.getModifiers() & Modifier.STATIC) == 0) {
//                LogUtils.w("The following Handler class should be static or leaks might occur: %s", klass.getCanonicalName());
//            }
//        }
    }

    public boolean testThread() {
        return _thread == Thread.currentThread();
    }

    public <T> Future<T> postCallable(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        post(task);
        return task;
    }

    public void runInHandlerThread(Runnable runnable) {
        if (Thread.currentThread() != _thread)
            post(runnable);
        else
            try {
                runnable.run();
            } catch (Throwable e) {
                Log.e(TAG, "Error occurred in handler run thread");
            }
    }

    public void runInHandlerThreadDelay(Runnable runnable) {
        post(runnable);
    }

    public <T> T callInHandlerThread(Callable<T> callable, T defaultValue) {
        T result = null;
        try {
            if (Thread.currentThread() != _thread)
                result = postCallable(callable).get();
            else
                result = callable.call();
        } catch (Throwable e) {
            Log.e(TAG, "Error occurred in handler call thread");
        }

        return result == null ? defaultValue : result;
    }

    @Override
    public void dispatchMessage(Message msg) {
        Runnable callback = msg.getCallback();
        if (callback != null) {
            try {
                callback.run();
            } catch (Throwable e) {
                Log.e(TAG, "Error occurred in handler thread, dispatchMessage");
            }
        } else {
            handleMessage(msg);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }
}
