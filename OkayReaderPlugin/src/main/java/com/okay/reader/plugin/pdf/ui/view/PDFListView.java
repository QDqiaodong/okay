/**
 * Copyright 2016 JustWayward Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okay.reader.plugin.pdf.ui.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.model.manager.PdfParseManager;
import com.okay.reader.plugin.pdf.ui.adapter.PDFListAdapter;
import com.okay.reader.plugin.utils.AppUtils;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LogUtils;
import com.okay.reader.plugin.utils.ReaderOkayAdapterMananger;
import com.okay.reader.plugin.utils.SharedPreferencesUtil;


public class PDFListView extends ListView implements AbsListView.OnScrollListener, BasePDFView {
    private static final String TAG = "PDFListView";
    private static final int MSG_TYPE_PRE_PAGE = 1;
    private static final int MSG_TYPE_NEXT_PAGE = 2;

    protected Context context;
    private PDFListAdapter mPdfPagerAdapter;
    private PageChangeListener mPageChangeListener;
    private DataChangedListener mDataChangeListener;

    /**
     * 正常，翻页模式
     */
    private static final int NORMAL_PAGE_MODE = 1;
    /**
     * 当前listview，可见条目有两页时。
     */
    private int currentItem;
    private int mCurrentItem;
    private int scrollDistance;
    private HelpHandler mHandler;

    /**
     * 页码变更次数　是5的倍数就强刷一次（因为会有残影）
     */
    private static final int MAX_PAGE_CHANGE_COUNT = 5;
    private int currPdfChangeCount;
    private int mLastCurItem;

    public PDFListView(Context context) {
        super(context);
        this.context = context;
        mHandler = new HelpHandler();
        init();
    }

    protected void init() {
        setClickable(true);
        this.setOnScrollListener(this);
        this.setSelector(android.R.color.transparent);
        setDivider();
        initAdapter(context);
    }

    protected void initAdapter(Context context) {
        mPdfPagerAdapter = new PDFListAdapter.Builder(context)
                .create();
        setAdapter(mPdfPagerAdapter);
        this.notifyDataChanged();
    }

    public void setDivider() {
        if (Constant.isShowInMainSceen) {
            this.setDivider(new ColorDrawable(context.getResources().getColor(R.color.pdf_listview_divider_color)));
        } else {
            this.setDivider(new ColorDrawable(context.getResources().getColor(R.color.pdf_listview_divider_presentation_color)));
        }
        int height = (int) context.getResources().getDimension(R.dimen.pdf_listview_divder_height);
        this.setDividerHeight(height);
    }

    private void removeDivider() {
        this.setDivider(null);
        this.setDividerHeight(0);
    }

    @Override
    public int getCount() {
        if (mPdfPagerAdapter != null)
            return mPdfPagerAdapter.getCount();
        return 0;
    }

    @Override
    protected void layoutChildren() {
        try {
            super.layoutChildren();
        } catch (IllegalStateException e) {
            LogUtils.d(TAG, "okay reader make sure your adapter calls notifyDataSetChanged()when its content changes.");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        LogUtils.d(TAG, "dispatchTouchEvent Constant.IS_SHOWING_GUIDE=" + Constant.IS_SHOWING_GUIDE);
        if (ev.getAction() == MotionEvent.ACTION_MOVE && !Constant.isShowInMainSceen && Constant.IS_SHOWING_GUIDE)
            return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public int getCurrentItem() {
        return mCurrentItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mLastCurItem = mCurrentItem;
        //Step1
        if (mPageChangeListener != null) {
            mPageChangeListener.scrollToBottom(false);
        }
        if (this.getDivider() == null && !Constant.isBottomViewShowing) {
            setDivider();
        }

        //step 2
        //当前屏幕，只有一页显示
        if (visibleItemCount == 1) {
            currentItem = firstVisibleItem;
        } else if (visibleItemCount == 2) {
            //当前屏幕，显示两页
            View firstView = view.getChildAt(0);
            final int[] location0 = new int[2];
            firstView.getLocationOnScreen(location0);

            View secondView = view.getChildAt(1);
            final int[] location1 = new int[2];
            secondView.getLocationOnScreen(location1);
            //说明第二个可见条目在屏幕正中间的下方
            if (location1[1] >= AppUtils.getCurrentPdfContainerHeight() / 2) {
                currentItem = firstVisibleItem;
            } else {
                currentItem = firstVisibleItem + 1;
            }
        } else if (visibleItemCount >= 3) {
            currentItem = firstVisibleItem + 1;
        }


        //step 3
        mCurrentItem = currentItem;
        if (!Constant.isShowInMainSceen && mCurrentItem != mLastCurItem) {
            currPdfChangeCount++;
            if ((currPdfChangeCount % MAX_PAGE_CHANGE_COUNT) == 0) {
                LogUtils.d(TAG, "每切换５页，则副屏幕强制刷新一次 currPdfChangeCount=" + currPdfChangeCount);
                ReaderOkayAdapterMananger.getInstance().forceUpdate();
            }

        }
        if (mPageChangeListener != null && Constant.isShangXia) {
            mPageChangeListener.onPageChange(mCurrentItem);
        }

        //step 4
        if (visibleItemCount >= 1) {
            View currentView = view.getChildAt(0);
            if (currentItem == firstVisibleItem) {
                currentView = view.getChildAt(0);
            } else if (currentItem == firstVisibleItem + 1) {
                currentView = view.getChildAt(1);
            }
            final int[] _location = new int[2];
            currentView.getLocationOnScreen(_location);
            int scroll = _location[1];
            //获取状态栏高度
            int titleBar = Constant.isShowInMainSceen ? Constant.MAIN_TITLE_HEIGHT : Constant.PRENT_TITLE_HEIGHT;
            scrollDistance = scroll - titleBar;
            Log.d(TAG, "onScroll: location0[1]=" + _location[1] + " scrollDistance" + scrollDistance + " titleBar=" + titleBar);
        }

        //step 5
        if ((totalItemCount - Constant.DOCUMENT_TOTAL_COUNT == 1) && (firstVisibleItem == Constant.DOCUMENT_TOTAL_COUNT - 1) && visibleItemCount == 2) {
            View footerView = view.getChildAt(1);
            final int[] location0 = new int[2];
            footerView.getLocationOnScreen(location0);
            Constant.isBottomViewShowing = true;
            Constant.bottomViewYLocation = location0[1];

            removeDivider();
        } else {
            Constant.bottomViewYLocation = -1;
            Constant.isBottomViewShowing = false;
        }
        if (mPageChangeListener != null && Constant.isShangXia) {
            mPageChangeListener.scrollToBottom(Constant.isBottomViewShowing);
        }

        LogUtils.d(TAG, "onScroll firstVisibleItem=" + firstVisibleItem + " totalItemCount=" + totalItemCount
                + " count=" + getCount() + " mCurrentItem=" + mCurrentItem + " visibleItemCount=" + visibleItemCount);

    }

    @Override
    public void prePage() {
        /*this.post(new Runnable() {
            @Override
            public void run() {
                PDFListView.this.scrollListBy(-AppUtils.getCurrentPdfContainerHeight());
            }
        });*/
        if (mHandler.hasMessages(MSG_TYPE_PRE_PAGE)) {
            mHandler.removeMessages(MSG_TYPE_PRE_PAGE);
        }
        mHandler.sendEmptyMessageDelayed(MSG_TYPE_PRE_PAGE, 100);
    }

    @Override
    public void nextPage() {
        if (mHandler.hasMessages(MSG_TYPE_NEXT_PAGE)) {
            mHandler.removeMessages(MSG_TYPE_NEXT_PAGE);
        }
        mHandler.sendEmptyMessageDelayed(MSG_TYPE_NEXT_PAGE, 100);
        //PDFListView.this.scrollListBy(AppUtils.getCurrentPdfContainerHeight());
        /*this.post(new Runnable() {
            @Override
            public void run() {
                PDFListView.this.scrollListBy(AppUtils.getCurrentPdfContainerHeight());
            }
        });*/
    }

    class HelpHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TYPE_PRE_PAGE:
                    PDFListView.this.scrollListBy(-AppUtils.getCurrentPdfContainerHeight());
                    break;
                case MSG_TYPE_NEXT_PAGE:
                    PDFListView.this.scrollListBy(AppUtils.getCurrentPdfContainerHeight());
                    break;
            }
        }
    }

    @Override
    public void goTo(final int pageNum) {
        if (pageNum < 0 || pageNum > getCount() - 1) {
            return;
        }
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                float scrollPercent = SharedPreferencesUtil.getInstance().getFloat(Constant.SCROLL_PRECENT, 0);
                int relScroll = (int) (scrollPercent * PdfParseManager.getInstance().getDestHeight());
                PDFListView.this.setSelectionFromTop(pageNum, relScroll); //listview数据加载完才会生效,利用handleDataChanged此方法监听数据加载完
                mCurrentItem = pageNum;
                LogUtils.d(TAG + " 跳转到某一页 currentItem=" + mCurrentItem + " relScroll=" + relScroll);
            }
        }, AppUtils.getDelayTime());
    }


    /**
     * 数据加载完的回调
     */
    @Override
    protected void handleDataChanged() {
        super.handleDataChanged();
        LogUtils.d(TAG, "handleDataChanged");
        if (mDataChangeListener != null) {
            mDataChangeListener.onSuccess();
        }
    }

    public void saveScroll() {
        float percent = (float) scrollDistance / PdfParseManager.getInstance().getDestHeight();
        LogUtils.d(TAG, "saveScroll scrollDistance=" + scrollDistance + " pdfPageHeight=" + PdfParseManager.getInstance().getDestHeight() + " percent=" + percent);
        SharedPreferencesUtil.getInstance().putFloat(Constant.SCROLL_PRECENT, percent);
    }


    @Override
    public void notifyDataChanged() {
        mPdfPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void scrollTo(final int scroll) {
        this.post(new Runnable() {
            @Override
            public void run() {
                PDFListView.this.scrollListBy(scroll);
            }
        });
    }


    public interface PageChangeListener {
        void onPageChange(int pageIndex);

        void scrollToBottom(boolean isBottom);
    }

    public void setPageChangeListener(PageChangeListener listener) {
        mPageChangeListener = listener;
    }

    public interface DataChangedListener {
        public void onSuccess();
    }

    public void setDataChangedListener(DataChangedListener dataChangedListener) {
        this.mDataChangeListener = dataChangedListener;
    }

}
