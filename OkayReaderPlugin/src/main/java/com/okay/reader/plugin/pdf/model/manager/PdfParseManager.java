package com.okay.reader.plugin.pdf.model.manager;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.okay.reader.plugin.pdf.PdfMainContract;
import com.okay.reader.plugin.pdf.pdflib.MuPDFCore;
import com.okay.reader.plugin.pdf.utils.PdfRendererParams;
import com.okay.reader.plugin.utils.AppUtils;
import com.okay.reader.plugin.utils.BitmapContainer;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LogUtils;
import com.okay.reader.plugin.utils.SimpleBitmapPool;

/**
 * Created by ZhanTao on 7/11/17.
 */

public class PdfParseManager {
    private static final String TAG = "PdfParseManager";

    public static final int FIRST_PAGE = 0;
    public static int DEFAULT_BITMAP_SIZE = 3;

    protected int mDestWidth;
    protected int mDestHeight;
    public BitmapContainer bitmapContainer;

    protected int bitmapSize;
    protected float mSourceScale;
    protected ImageLoader imageLoader;
    private MuPDFCore mPDFCore;
    private PointF mDefaultPage;
    private PdfMainContract.IView mPdfView;
    private HelpHandler mHandler;
    private int currLoadImageWidth;
    private int mLastPdfContainerHeight = -1;

    private PdfParseManager() {
    }

    private static class SingletonPatternHolder {
        private static final PdfParseManager pdfParseHelper = new PdfParseManager();
    }

    public static PdfParseManager getInstance() {
        return SingletonPatternHolder.pdfParseHelper;
    }

    public void initPath(String filePath, PdfMainContract.IView pdfView) {
        releaseAllBitmaps();
        this.mPdfView = pdfView;
        this.bitmapSize = DEFAULT_BITMAP_SIZE;

        if (imageLoader == null)
            imageLoader = ImageLoader.getInstance(1, ImageLoader.Type.LIFO);
        if (mPDFCore == null) {
            mPDFCore = openFile(filePath);
        }

        if (mPDFCore == null) {
            pdfView.showErrorPage();
            return;
        }

        if (bitmapContainer == null) {
            extractPdfParamsFromFirstPage();
        }

        if (mHandler == null) {
            mHandler = new HelpHandler();
        }
    }

    private MuPDFCore openFile(String path) {
        LogUtils.d(TAG, "openFile path=" + path);
        MuPDFCore core;
        try {
            core = new MuPDFCore(AppUtils.getAppContext(), path);
        } catch (Exception e) {
            LogUtils.d(TAG, "open file e.toString=" + e.toString());
            return null;
        } catch (OutOfMemoryError e) {
            LogUtils.d(TAG, "e.toString=" + e.toString());
            return null;
        }

        Constant.DOCUMENT_TOTAL_COUNT = core.countPages();
        LogUtils.d(TAG, "Constant.DOCUMENT_TOTAL_COUNT=" + Constant.DOCUMENT_TOTAL_COUNT);
        if (core != null && Constant.DOCUMENT_TOTAL_COUNT == 0) {
            core = null;
        }
        return core;
    }


    public MuPDFCore getMuPDFCore() {
        return mPDFCore;
    }

    public int getDestHeight() {
        return this.mDestHeight;
    }

    public int getDestWidth() {
        return this.mDestWidth;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void loadImage(final ImageView imageView, final int position, final View defaultBg, final ViewGroup root) {
        Bitmap bitmap = bitmapContainer.get(position);
        imageLoader.loadImage(imageView, bitmap, mPDFCore, String.valueOf(position), defaultBg, root);
        currLoadImageWidth = bitmap.getWidth();
        LogUtils.d(TAG, "loadImage position=" + position + " currentBitmapWidht=" + currLoadImageWidth);
    }

    public void reConfigure() {
        releaseAllBitmaps();
        configure();
    }

    public void configure() {
        LogUtils.d(TAG, "mLastPdfContainerHeight=" + mLastPdfContainerHeight + " height=" + AppUtils.getCurrentPdfContainerHeight()
                + " AppUtils.getNavigationBarHeight()=" + AppUtils.getNavigationBarHeight() + " AppUtils.isEPAD()=" + AppUtils.isEPAD()
        );

        //fix bug 30610,当navigationbar弹出的时候，listview会重新getview,因为加载页面有时间，所以会出现闪烁
        if (AppUtils.getNavigationBarHeight() != 0 && AppUtils.isEPAD()) {
            //当前pdf容器的高度为navigationbar弹出时的高度，并且上一次记录的高度是没有弹出navigationbar的高度
            if (AppUtils.getCurrentPdfContainerHeight() == AppUtils.getEpadPdfMinHeight()
                    && mLastPdfContainerHeight == AppUtils.getEpadPdfMaxHeight()) {
                if(mHandler != null){
                    mHandler.sendEmptyMessageDelayed(1, 200);
                }
                Constant.isNavigationBarShowing = true;
                //当前pdf容器的高度是没有弹出navigationbar的高度，并且上一次记录的高度是弹出navigatiobar的高度
            } else if (AppUtils.getCurrentPdfContainerHeight() == AppUtils.getEpadPdfMaxHeight()
                    && mLastPdfContainerHeight == AppUtils.getEpadPdfMinHeight()) {
                if(mHandler != null){
                    mHandler.sendEmptyMessageDelayed(1, 200);
                }
                Constant.isNavigationBarShowing = true;
            }
        }

        recalculation(mDefaultPage);
        PdfRendererParams params = new PdfRendererParams();
        params.setOffScreenSize(bitmapSize);
        params.setWidth(mDestWidth);
        params.setHeight(mDestHeight);
        bitmapContainer = new SimpleBitmapPool(params);
        LogUtils.d(TAG, "configure......bitmapContainer==null..." + (bitmapContainer == null));
        if (currLoadImageWidth != 0 && currLoadImageWidth != mDestWidth) {
            mPdfView.refresh();
        }

    }

    private void extractPdfParamsFromFirstPage() {
        AsyncTask<Void, Void, PointF> sizingTask = new AsyncTask<Void, Void, PointF>() {
            @Override
            protected PointF doInBackground(Void... arg0) {
                return mPDFCore.getPageSize(FIRST_PAGE);
            }

            @Override
            protected void onPostExecute(PointF result) {
                super.onPostExecute(result);
                mDefaultPage = result;
                mPdfView.update();
               // ToastUtil.getInstance().showToast("是否是流式"+mPDFCore.isPdfFlow());
               // ReaderOkayAdapterMananger.getInstance().setIsFLowMode(AppUtils.getAppContext(),mPDFCore.isPdfFlow());
            }
        };
        sizingTask.execute((Void) null);
        LogUtils.d(TAG, "extractPdfParamsFromFirstPage");
    }

    /**
     * 1.根据不同的浏览模式，计算当前pdf应该显示的宽和高
     * 2.bitmap的大小应该小于app可用内存的4分之
     */
    private void recalculation(PointF size) {
        if (size == null) {
            return;
        }

        if (Constant.isShangXia) {
            mSourceScale = AppUtils.getCurrentPdfContainerWidth() / size.x;
        } else {
            mSourceScale = Math.min(AppUtils.getCurrentPdfContainerWidth() / size.x, AppUtils.getCurrentPdfContainerHeight() / size.y);
        }
        Point newSize = new Point((int) (size.x * mSourceScale), (int) (size.y * mSourceScale));
        mDestWidth = newSize.x;
        mDestHeight = newSize.y;

        if (AppUtils.isEPAD() && AppUtils.getNavigationBarHeight() != 0) {
            if (AppUtils.getCurrentPdfContainerHeight() == AppUtils.getEpadPdfMaxHeight()) {
                mLastPdfContainerHeight = AppUtils.getEpadPdfMaxHeight();
            } else if (AppUtils.getCurrentPdfContainerHeight() == AppUtils.getEpadPdfMinHeight()) {
                mLastPdfContainerHeight = AppUtils.getEpadPdfMinHeight();
            }
        }
        LogUtils.d(TAG + "size.x= " + size + " mDestWidth=" + mDestWidth + " mDestHeight=" + mDestHeight +
                " CurrentPdfViewWidth()=" + AppUtils.getCurrentPdfContainerWidth() + " CurrentPdfViewHeight()="
                + AppUtils.getCurrentPdfContainerHeight()+" mLastPdfContainerHeight="+mLastPdfContainerHeight);
    }

    public void close() {
        releaseAllBitmaps();
        if (mPDFCore != null) {
            mPDFCore.onDestroy();
        }
        if (imageLoader != null) {
            imageLoader = null;
        }

        mPDFCore = null;
        mLastPdfContainerHeight = 0;

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void releaseAllBitmaps() {
        if (bitmapContainer != null) {
            bitmapContainer.clear();
        }
        bitmapContainer = null;
        LogUtils.d(TAG, "releaseAllBitmaps bitmapContainer==null=" + (bitmapContainer == null));
    }

    class HelpHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Constant.isNavigationBarShowing = false;
        }
    }
}
