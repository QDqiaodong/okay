package com.okay.reader.plugin.pdf.model.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.pdflib.MuPDFCore;
import com.okay.reader.plugin.pdf.ui.view.ImageViewAware;
import com.okay.reader.plugin.utils.AppUtils;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LogUtils;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static ImageLoader mInstance;

    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    private static final int DEAFULT_THREAD_COUNT = 1;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;
    /**
     * mSemaphorePoolThreadHandler :为了任务执行时，判断是否handler是否为空
     * mSemaphoreThreadPool :
     */
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreIsRunning = new Semaphore(1);
    private Semaphore mSemaphoreThreadPool;

    public enum Type {
        FIFO, LIFO
    }

//    private int currentItemPosition;

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    /**
     * 初始化
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        initBackThread();

        // 获取我们应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

        };

        // 创建定长的线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);

        mTaskQueue = new LinkedList<Runnable>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    /**
     * 初始化后台轮询线程
     */
    private void initBackThread() {
        // 后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                // 释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };

        mPoolThread.start();
    }

    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    public void loadImage(final ImageView imageView, final Bitmap bitmap, final MuPDFCore pdfCore, final String position, final View defaultBg, final ViewGroup root) {

        ImageViewAware imageViewAware = new ImageViewAware(imageView);
        imageViewAware.setTag(position);

        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                public void handleMessage(Message msg) {
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap destBm = holder.bitmap;
                    ImageViewAware imageViewAware = holder.imageView;
                    ImageView destImageView = imageViewAware == null ? null : (ImageView) imageViewAware.getWrappedView();
                    View defaultBg = holder.defaultBg;
                    ViewGroup rootView = holder.viewGroup;
                    String destPos = holder.pos;
                    LogUtils.d(TAG, "加载成功开始设置bitmap destImageView != null=" + (destImageView != null) +
                            " destPos=" + destPos + " destBm != null=" + (destBm != null)
                            + " destBm.isRecycled()=" + destBm.isRecycled()
                    );
                    if (destImageView != null && destImageView.getTag().toString().equals(destPos) && destBm != null && !destBm.isRecycled()) {
                        rootView.removeView(defaultBg);
                        LogUtils.d(TAG, "destImageView.getTag().toString()=" + destImageView.getTag().toString());
                        //Draw the progress on each picture
                        Context context = destImageView.getContext();
                        drawProgressOnPicture(context, destBm, destPos);
                        destImageView.setImageBitmap(destBm);
                    }
                }
            };
        }

        if (Constant.nCurrentPageIndex.get() == 0 && Integer.parseInt(position) > 1) {
            Constant.nCurrentPageIndex.set(Integer.parseInt(position));
        }
        mChangeMode = false;
        addTask(buildTask(imageViewAware, bitmap, pdfCore, position, defaultBg, root));
    }

    //TODO synchronized 这个同步一样可以保证每次只解析一个页面．但是listview的滑动会不流畅
    /*public synchronized void addTask(final ImageViewAware imageViewAware, final Bitmap bitmap, final MuPDFCore pdfCore, final String position, final View defaultBg, final ViewGroup viewGroup) {
        int nPosition = Integer.parseInt(position);
        if (bitmap != null && !bitmap.isRecycled()) {
            MuPDFCore.Cookie cookie = pdfCore.new Cookie();
            bitmap.eraseColor(Color.TRANSPARENT);
            pdfCore.drawPage(bitmap, nPosition, bitmap.getWidth(), bitmap.getHeight(), 0, 0, bitmap.getWidth(), bitmap.getHeight(), cookie);
            refreshBitmap(position, imageViewAware, bitmap, defaultBg, viewGroup);
        }
    }*/


    /**
     * 新建一个任务
     * <p/>
     * 此任务的核心就是渲染PDF
     */
    private Runnable buildTask(final ImageViewAware imageViewAware, final Bitmap bitmap, final MuPDFCore pdfCore, final String position, final View defaultBg, final ViewGroup viewGroup) {
        LogUtils.d(TAG + " buildTask position=" + position + " mChangeMode=" + mChangeMode + " currentPosition=" + Constant.nCurrentPageIndex.get());
        return new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG + " ----------run buildTask position=" + position + " mChangeMode=" + mChangeMode + " currentPosition=" + Constant.nCurrentPageIndex.get()
                        + " mTaskQueue.size()=" + mTaskQueue.size()
                );
                int nPosition = Integer.parseInt(position);
                if (!mChangeMode) {
                    if (nPosition >= (Constant.nCurrentPageIndex.get() - 2) && nPosition <= (Constant.nCurrentPageIndex.get() + 2)) {
                        try {
                            LogUtils.d(TAG + " mSemaphoreIsRunning.acquire position=" + position
                                    + ";Thread.currentThread()=" + Thread.currentThread().getName());
                            mSemaphoreIsRunning.acquire();
                            if (bitmap != null && !bitmap.isRecycled()) {
                                MuPDFCore.Cookie cookie = pdfCore.new Cookie();
                                bitmap.eraseColor(Color.TRANSPARENT);
                                pdfCore.drawPage(bitmap, nPosition, bitmap.getWidth(), bitmap.getHeight(), 0, 0, bitmap.getWidth(), bitmap.getHeight(), cookie);
                                refreshBitmap(position, imageViewAware, bitmap, defaultBg, viewGroup);
                            }

                            mSemaphoreThreadPool.release();
                            mSemaphoreIsRunning.release();
                        } catch (InterruptedException e) {
                            mSemaphoreThreadPool.release();
                            e.printStackTrace();
                        }
                    } else {
                        mSemaphoreThreadPool.release();
                    }
                } else {
                    LogUtils.d(TAG, "buildTask removeCallbacksAndMessages");
                    mUIHandler.removeCallbacksAndMessages(null);
                    mSemaphoreThreadPool.release();
                }
            }
        };
    }

    private void drawProgressOnPicture(Context context, Bitmap destBm, String destPos) {
        LogUtils.d(TAG, " destPos=" + destPos + " DOCUMENT_TOTAL_COUNT=" + Constant.DOCUMENT_TOTAL_COUNT);

        if (Constant.isShangXia) {
            int marginBottom;
            int marginRight;
            int progressSize;
            String progressText;
            Rect rect = new Rect();
            Canvas canvas = new Canvas(destBm);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            paint.setColor(context.getResources().getColor(R.color.text_color_gray));

            if (Constant.isShowInMainSceen) {
                progressSize = context.getResources().getDimensionPixelSize(R.dimen.progress_text_size_main_screen);
                marginBottom = context.getResources().getDimensionPixelOffset(R.dimen.progress_text_margin_bottom_main_screen);
            } else {
                progressSize = context.getResources().getDimensionPixelSize(R.dimen.progress_text_size);
                marginBottom = context.getResources().getDimensionPixelOffset(R.dimen.progress_text_margin_bottom);
            }
            paint.setTextSize(progressSize);
            LogUtils.d(TAG, "progressSize=" + progressSize + " marginBottom=" + marginBottom);

            if (AppUtils.isOrientationLandscape(context)) {
                progressText = String.valueOf(Integer.parseInt(destPos) + 1) + "/" + String.valueOf(Constant.DOCUMENT_TOTAL_COUNT);
                paint.getTextBounds(progressText, 0, progressText.length(), rect);
                canvas.drawText(progressText, destBm.getWidth() / 2 - rect.width() / 2, destBm.getHeight() - rect.height() - marginBottom, paint);
                canvas.drawBitmap(destBm, 0, 0, paint);
                LogUtils.d(TAG, "Landscape");
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(String.valueOf(Integer.parseInt(destPos) + 1) + "/" + String.valueOf(Constant.DOCUMENT_TOTAL_COUNT));
                buffer.append(String.valueOf(" "));
                int progress = (int) (Double.parseDouble(destPos) / Constant.DOCUMENT_TOTAL_COUNT * 100);
                buffer.append(String.valueOf("(" + progress) + "%)");
                progressText = buffer.toString();
                paint.getTextBounds(progressText, 0, progressText.length(), rect);

                if (Constant.isShowInMainSceen) {
                    marginRight = context.getResources().getDimensionPixelOffset(R.dimen.progress_text_margin_right_main_screen);
                } else {
                    marginRight = context.getResources().getDimensionPixelOffset(R.dimen.progress_text_margin_right);
                }
                canvas.drawText(progressText, destBm.getWidth() - rect.width() - marginRight, destBm.getHeight() - rect.height() - marginBottom, paint);
                canvas.drawBitmap(destBm, 0, 0, paint);
                LogUtils.d(TAG, "PORTRAIT progressText=" + progressText + " marginRight=" + marginRight);
            }


        }
    }

    /**
     * 从任务队列取出一个方法
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    private volatile boolean mChangeMode = false;

    public void changeMode() {
        mChangeMode = true;
    }


    public void acquireRunningSemaphore() {
        try {
            mSemaphoreIsRunning.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseRunningSemaphore() {
        mSemaphoreIsRunning.release();
    }

    private void refreshBitmap(final String path, final ImageViewAware imageView,
                               Bitmap bm, View defaultBg, ViewGroup viewGroup) {
        LogUtils.d(TAG, "refreshBitmap path=" + path + " currentPostion=" + Constant.nCurrentPageIndex.get());
        Message message = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.pos = path;
        holder.imageView = imageView;
        holder.defaultBg = defaultBg;
        holder.viewGroup = viewGroup;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        LogUtils.d(TAG + " addTask mTaskQueue.size=" + mTaskQueue.size());
        // if(mPoolThreadHandler==null)wait();
        try {
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();
        } catch (InterruptedException e) {
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    public void shutdownNow() {
        mThreadPool.shutdownNow();
    }

    public boolean isTerminated() {
        return mThreadPool.isTerminated();
    }

    private class ImgBeanHolder {
        ViewGroup viewGroup;
        Bitmap bitmap;
        ImageViewAware imageView;
        View defaultBg;
        String pos;
    }
}
