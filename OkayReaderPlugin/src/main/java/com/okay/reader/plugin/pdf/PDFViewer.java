package com.okay.reader.plugin.pdf;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.model.manager.PdfParseManager;
import com.okay.reader.plugin.pdf.presenter.PdfMainPresenter;
import com.okay.reader.plugin.pdf.ui.task.BaseTask;
import com.okay.reader.plugin.pdf.ui.task.ChangeModeTask;
import com.okay.reader.plugin.pdf.ui.task.OrientationChangeTask;
import com.okay.reader.plugin.pdf.ui.task.ScreenChangeTask;
import com.okay.reader.plugin.pdf.ui.view.BasePDFView;
import com.okay.reader.plugin.pdf.ui.view.MainLayout;
import com.okay.reader.plugin.pdf.ui.view.PDFListView;
import com.okay.reader.plugin.pdf.ui.view.PDFViewPager;
import com.okay.reader.plugin.utils.AppUtils;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LogUtils;
import com.okay.reader.plugin.utils.ReaderOkayAdapterMananger;
import com.okay.reader.plugin.utils.SharedPreferencesUtil;

/**
 * Created by qiaodong on 17-9-14.
 */

public class PDFViewer extends FrameLayout implements MainLayout.OnCenterClickListener, PdfMainContract.IView {

    private static final String TAG = "PDFViewer";
    private static final String ACTION_NOTIFICATION_BROADCAST = "android.intent.action.NOTIFICATION_BROADCAST";
    /**
     * A public action sent by AlarmService when the alarm has started.
     */
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    /**
     * A public action sent by AlarmService when the alarm has stopped for any reason.
     */
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";
    /**
     * 0 -180 度接收到通知，5秒没有操作，主动熄灭LCD屏幕
     */
    private static final int MSG_SET_LCD_DISABLE = 1;
    /**
     * 闹钟关闭的消息
     */
    private static final int MSG_HANDLE_ALARM_DONE = 2;
    /**
     * 阅读器失去焦点的消息
     */
    private static final int MSG_HANDLE_LOST_WINDOW_FOCUS = 3;
    /**
     * 长按电源键
     */
    public static final String POWER_PRESSED_ACTION = "android.intent.action.POWER_KEY_PRESSED";
    Uri uri = Settings.Global.getUriFor("okay_angle_mode"/*Settings.Global.OKAY_ANGLE_MODE*/);

    private Context mContext;
    private PdfMainContract.IPresenter mPdfPresenter;

    /**
     * 根view
     */
    private LinearLayout mRootView;
    /**
     * 控制菜单栏显示隐藏
     */
    private MainLayout mContentView;
    /**
     * pdf的直接父类
     */
    private FrameLayout mFlReadWidget;
    private PDFViewPager mPdfViewPager;
    private PDFListView mPdfListView;
    private View mGuideView;
    private View mFootView;
    private PDFOnPageChangeListener mViewPagerListener;
    private PDFListViewPageChangeListener mListViewListener;

    private OnPageChangeListener mPageChangeListener;
    private OnPdfLoadListener mPdfLoadListener;
    private boolean isContainerReady = false;
    private PDFState mCurrentState = PDFState.DEFAULT;
    private GuideState mGuideState = GuideState.FIRST_PAGE;
    private View guideFClickView;
    private View guideSClickView;
    private View guideCenterClickView;
    private TextView mGuideMessage;
    private AngleModeObserver mAngleModeObserver;
    private HelpHandler mHelpHandler;


    private enum PDFState {
        DEFAULT, START_LOAD, ERROR, COMPLETE
    }

    public enum GuideState {
        FIRST_PAGE, SECOND_PAGE
    }

    public boolean isScreenOff = false;


    public PDFViewer(Context context) {
        super(context);
        initPdf(context);
    }

    public PDFViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPdf(context);
    }

    public PDFViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPdf(context);
    }

    private void initPdf(Context context) {
        mContext = getContext();
        LayoutInflater.from(context).inflate(R.layout.activity_read_pdf, this);
        //伴随view的生命周期
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenOffReceiver, intentFilter);
        initView();
        initData();
    }

    private void initData() {
        ReaderOkayAdapterMananger.getInstance().init(mContext);
        mPdfPresenter = new PdfMainPresenter(this, mContext);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFICATION_BROADCAST);
        intentFilter.addAction(ALARM_ALERT_ACTION);
        intentFilter.addAction(ALARM_DONE_ACTION);
        intentFilter.addAction(POWER_PRESSED_ACTION);
        mContext.registerReceiver(broadcastReceiver, intentFilter);

        mAngleModeObserver = new AngleModeObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(uri, false, mAngleModeObserver);
        mIsRecycled = false;
        Constant.isNavigationBarShowing = false;
    }

    private void initView() {
        mRootView = (LinearLayout) findViewById(R.id.layout_root);
        mContentView = (MainLayout) findViewById(R.id.layout_content);
        mFlReadWidget = (FrameLayout) findViewById(R.id.flReadWidget);
        initPdfWidget();
        mContentView.setView(this);
        mContentView.setOnCenterClickListener(this);
    }

    private void initPdfWidget() {
        if (mPdfListView == null) {
            mPdfListView = new PDFListView(mContext);
            mListViewListener = new PDFListViewPageChangeListener();
            mPdfListView.setPageChangeListener(mListViewListener);
        }
    }

    public void onResume(boolean isShowMain) {
        LogUtils.d(TAG, "onResume:  isShowMain=" + isShowMain + " mIsRecycled=" + mIsRecycled + " isContainerReady=" + isContainerReady);
        Constant.isShowInMainSceen = isShowMain;
        PdfParseManager.getInstance().configure();
        isScreenOff = false;
        if (mIsRecycled) {
            initData();
        }
        if (mPdfPresenter != null) {
            mPdfPresenter.onResume();
        }
        mPdfListView.notifyDataChanged();
    }

    public void onPause() {
        if (mPdfPresenter != null) {
            mPdfListView.saveScroll();
            mPdfPresenter.onPause();
            recycle();
        }
    }

    public Configurator displayFromPath(String filePath) {
        mCurrentState = PDFState.DEFAULT;
        mPdfPresenter.initData(filePath);
        PdfParseManager.getInstance().initPath(filePath, this);
        return new Configurator();
    }

    private void setIsDebug(boolean isDebug) {
        LogUtils.setIsDebug(isDebug);
    }

    private void setMainTitleBarHeight(int height) {
        Constant.MAIN_TITLE_HEIGHT = height;
        int epadPdfMinHeight = AppUtils.getEpadPdfMinHeight();
        int epadPdfMaxHeight = AppUtils.getEpadPdfMaxHeight();
        LogUtils.d(TAG, "epadPdfMinHeight=" + epadPdfMinHeight + " epadPdfMaxHeight=" + epadPdfMaxHeight + " height=" + height
                + " statusbar=" + AppUtils.getStatusBarHeight());

    }

    private void setPrentTitleBarHeight(int height) {
        Constant.PRENT_TITLE_HEIGHT = height;
    }

    public void setFooterView(View footerView) {
        if (!Constant.isShangXia) {
            return;
        }

        if (mPdfListView != null) {
            if (mPdfListView.getFooterViewsCount() >= 1 && mFootView != null) {
                mPdfListView.removeFooterView(mFootView);
            }
            mFootView = footerView;
            mPdfListView.addFooterView(footerView);
        }
    }

    private void load() {
        if (mCurrentState == PDFState.ERROR) {
            return;
        }

        if (mCurrentState == PDFState.DEFAULT) {
            mCurrentState = PDFState.START_LOAD;
            requestLayout();
        }
    }

    public ViewGroup getRootView() {
        return mRootView;
    }

    public boolean mIsRecycled = true;

    public void recycle() {
        mIsRecycled = true;
        isContainerReady = false;
        mCurrentState = PDFState.DEFAULT;
        if (!Constant.isShowInMainSceen) {
            AppUtils.setLCDSwitchOnOff("1");
        }
        mContext.unregisterReceiver(broadcastReceiver);
        mContext.getContentResolver().unregisterContentObserver(mAngleModeObserver);
        if (mHelpHandler != null) {
            mHelpHandler.removeCallbacksAndMessages(null);
        }
        mPdfPresenter.onDestroy();

        //reinit　Data
        Constant.nCurrentPageIndex.set(0);
        Constant.isShowInMainSceen = true;
        Constant.isShangXia = true;
        Constant.DOCUMENT_TOTAL_COUNT = 0;
        Constant.IS_SHOWING_GUIDE = false;
    }

    private void resumeGuidePageIfNeed() {
        boolean showGuide = SharedPreferencesUtil.getInstance().getBoolean(Constant.SHOW_GUIDE＿PAGE, true);
        LogUtils.d(TAG, "resumeGuidePageIfNeed: showGuide=" + showGuide + " Constant.isShowInMainSceen=" + Constant.isShowInMainSceen);
        if (Constant.isShowInMainSceen || !showGuide) {
            return;
        }
        if (showGuide) {
            mPdfListView.setVisibility(View.GONE);
            addGuidView(R.layout.guide_page_layout, mFlReadWidget);
            SharedPreferencesUtil.getInstance().putBoolean(Constant.SHOW_GUIDE＿PAGE, false);
        }
    }

    public void addGuidView(int layoutId, ViewGroup rootView) {
        mGuideView = LayoutInflater.from(mContext).inflate(layoutId, null);
        guideFClickView = mGuideView.findViewById(R.id.guid_up_down_click_first);
        guideSClickView = mGuideView.findViewById(R.id.guid_up_down_click_second);
        guideCenterClickView = mGuideView.findViewById(R.id.guide_up_down_scroll);
        mGuideMessage = (TextView) mGuideView.findViewById(R.id.tv_message);
        mGuideMessage.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (Constant.IS_SHOWING_GUIDE && mGuideState.ordinal() == PDFViewer.GuideState.FIRST_PAGE.ordinal()) {
                    showSecondGuidePage();
                } else if (Constant.IS_SHOWING_GUIDE && mGuideState.ordinal() == PDFViewer.GuideState.SECOND_PAGE.ordinal()) {
                    removeGuideView();
                }
                return false;
            }
        });


        rootView.addView(mGuideView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                .MATCH_PARENT));
        Constant.IS_SHOWING_GUIDE = true;
        mGuideState = GuideState.FIRST_PAGE;
    }

    public void showSecondGuidePage() {
        if (guideFClickView == null) {
            return;
        }
        guideFClickView.setVisibility(View.GONE);
        guideSClickView.setVisibility(View.GONE);
        guideCenterClickView.setVisibility(View.VISIBLE);
        mGuideMessage.setText(R.string.guide_message_second);
        mGuideState = PDFViewer.GuideState.SECOND_PAGE;
    }

    public void removeGuideView() {
        if (mGuideView == null) {
            return;
        }

        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGuideState = PDFViewer.GuideState.FIRST_PAGE;
                mGuideView.setVisibility(View.GONE);
                mPdfListView.setVisibility(View.VISIBLE);
                Constant.IS_SHOWING_GUIDE = false;
                requestLayout();
            }
        }, 500);
    }

    public void isShowMainScreen(boolean isShowMainScreen) {
        Constant.isShowInMainSceen = isShowMainScreen;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogUtils.d(TAG, "onMeasure");
        getCurrentWH();
        if (mCurrentState != PDFState.ERROR) {
            PdfParseManager.getInstance().configure();
        }
    }

    @Override
    public void update() {
        LogUtils.d(TAG, "update....");
        isContainerReady = true;
        requestLayout();
    }

    @Override
    public void refresh() {
        if (mPdfListView != null) {
            LogUtils.d(TAG, "refresh..........");
            mPdfListView.notifyDataChanged();
            requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        LogUtils.d(TAG, "onLayout: mFlReadWidget.getChildCount()=" + mFlReadWidget.getChildCount() + " Constant.isShowInMainSceen=" + Constant.isShowInMainSceen);
        if (mFlReadWidget.getChildCount() == 0 && mCurrentState == PDFState.START_LOAD && isContainerReady) {
            mPdfListView.notifyDataChanged();
            mFlReadWidget.addView(Constant.isShangXia ? mPdfListView : mPdfViewPager);
            showLoadCompletePage();
            mCurrentState = PDFState.COMPLETE;
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getCurrentWH();
        showOrientationChangeDialog();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mCurrentState == PDFState.COMPLETE) {
            if (hasWindowFocus) {
                if (Constant.isShowInMainSceen)
                    return;
            }
        }

        //假如副屏阅读，此时来通知，当用户滑下状态栏目时，应该取消５秒关闭LCD的逻辑，因为此时用户再操作．当hasWindowFocus为true时，并且在副屏幕再关闭
        if (mHelpHandler != null && !hasWindowFocus) {
            mHelpHandler.removeCallbacksAndMessages(null);
        }

        if (!hasWindowFocus && !Constant.isShowInMainSceen) {
            if (mHelpHandler == null) {
                mHelpHandler = new HelpHandler();
            }
            //当阅读器失去焦点的时候，并且在副屏幕．比如用户正常操作状态栏，应该保证LCD点亮，但是如果此时锁屏则不处理
            mHelpHandler.sendEmptyMessageDelayed(MSG_HANDLE_LOST_WINDOW_FOCUS, 2000);
        } else if (hasWindowFocus && !Constant.isShowInMainSceen) {
            //SystemPropertyUtils.setSystemProperty(mContext, "sys.primarypanel.enable", "0"); //关闭LCD
            AppUtils.setLCDSwitchOnOff("0");
        }
        LogUtils.d(TAG, "onWindowFocusChanged: hasWindowFocus=" + hasWindowFocus + " Constant.isShowInMainSceen=" + Constant.isShowInMainSceen);
    }

    /**
     * 当view销毁的时候会调用
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScreenOffReceiver != null) {
            mContext.unregisterReceiver(mScreenOffReceiver);
        }
    }


    /**
     * 获取当前View的宽高
     */
    private void getCurrentWH() {
        int measuredWidth = this.mFlReadWidget.getMeasuredWidth();
        int measuredHeight = this.mFlReadWidget.getMeasuredHeight();
        Constant.pdfContainerWidth = measuredWidth;
        Constant.pdfContainerHeight = measuredHeight;
        LogUtils.d(TAG, "getCurrentWH measuredWidth=" + measuredWidth + " measuredHeight=" + measuredHeight);
    }

    /**
     * 初始化PDF加载成功
     */
    private void showLoadCompletePage() {
        if (mPdfLoadListener != null) {
            mPdfLoadListener.loadComplete();
        }
    }

    /**
     * 解析失败显示的页面
     */
    @Override
    public void showErrorPage() {
        mFlReadWidget.removeAllViews();
        ImageView errorView = new ImageView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        errorView.setLayoutParams(params);
        errorView.setImageResource(R.drawable.app_empty_icon);
        mFlReadWidget.addView(errorView);

        mCurrentState = PDFState.ERROR;
        if (this.mPdfLoadListener != null) {
            this.mPdfLoadListener.loadError();
        } else {
            LogUtils.e("PDFView", "load pdf error");
        }
    }

    public void showLCD2EPDDialog(final ViewGroup root) {
        LogUtils.d(TAG, "showLCD2EPDDialog.........");
        Constant.isShowInMainSceen = false;
        mRootView.removeAllViews();
        mPdfListView.saveScroll();
        AppUtils.setLCDSwitchOnOff("0");//关闭LCD
        mPdfListView.setDivider();
        new ScreenChangeTask(mContext, getCurrentPdfView())
                .setChangeCompleteListener(new BaseTask.ChangeCompleteListener() {
                    @Override
                    public void complete() {
                        LogUtils.d(TAG, "complete..........");
                        if (root instanceof LinearLayout)
                            ((LinearLayout) root).addView(mContentView);
                        else {
                            root.addView(mContentView);
                        }
                        resumeGuidePageIfNeed();
                    }
                }).execute();

    }

    /**
     * 活动到上次屏幕的位置
     * relScroll 小于0 表示往上滑
     * relScroll 大于0 表示往下滑
     */
    @Override
    public void scrollToDestPosition() {

    }

    public void showEPD2LCDDialog() {
        Constant.isShowInMainSceen = true;
        Constant.IS_SHOWING_GUIDE = false;
        mRootView.removeAllViews();
        mRootView.getRootView().setBackground(null);
        mPdfListView.saveScroll();
        AppUtils.setLCDSwitchOnOff("1");//打开LCD
        mPdfListView.setDivider();
        new ScreenChangeTask(mContext, getCurrentPdfView())
                .setChangeCompleteListener(new BaseTask.ChangeCompleteListener() {
                    @Override
                    public void complete() {
                        mRootView.addView(mContentView);
                        if (mGuideView != null) {
                            mFlReadWidget.removeView(mGuideView);
                        }

                        if (mPdfListView != null && mPdfListView.getVisibility() != View.VISIBLE) {
                            mPdfListView.setVisibility(View.VISIBLE);
                        }
                    }
                }).execute();
    }

    @Override
    public void showChangeModeDialog(BasePDFView basePDFView) {
        mFlReadWidget.removeView((View) basePDFView);
        new ChangeModeTask(mContext, getCurrentPdfView())
                .setChangeCompleteListener(new BaseTask.ChangeCompleteListener() {
                    @Override
                    public void complete() {
                        mFlReadWidget.addView((View) getCurrentPdfView());
                    }
                }).execute();
    }

    @Override
    public void showOrientationChangeDialog() {
        new OrientationChangeTask(mContext, getCurrentPdfView())
                .setChangeCompleteListener(new BaseTask.ChangeCompleteListener() {
                    @Override
                    public void complete() {
                        PDFViewer.this.requestLayout();
                    }
                }).execute();
    }

    /**
     * 点击上一页
     */
    @Override
    public void onPreClick() {
        getCurrentPdfView().prePage();
        if (mPageChangeListener != null) {
            mPageChangeListener.onPreClick();
        }
    }

    /**
     * 点击下一页
     */
    @Override
    public void onLaterClick() {
        getCurrentPdfView().nextPage();
        if (mPageChangeListener != null) {
            mPageChangeListener.onLaterClick();
        }
    }

    @Override
    public void setPresenter(PdfBaseContract.IPresenter presenter) {
    }

    /**
     * 获取当前解析PDF的View（上下浏览:listview  左右浏览:viewpager）
     *
     * @return
     */
    @Override
    public BasePDFView getCurrentPdfView() {
        return Constant.isShangXia ? mPdfListView : mPdfViewPager;
    }

    /**
     * 获取当前页号（从0开始）
     *
     * @return
     */
    @Override
    public int getCurrentPage() {
        return getCurrentPdfView().getCurrentItem();
    }

    /**
     * 获取PDF总页数
     *
     * @return
     */
    @Override
    public int getCount() {
        return getCurrentPdfView().getCount();
    }

    private void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mPageChangeListener = listener;
    }

    public void setPdfListViewDataChangeListener(PDFListView.DataChangedListener listener) {
        if (mPdfListView != null) {
            mPdfListView.setDataChangedListener(listener);
        }
    }

    private void setOnPdfLoadListener(OnPdfLoadListener listener) {
        this.mPdfLoadListener = listener;
    }

    class PDFOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            Constant.nCurrentPageIndex.set(position);
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageChange(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    class PDFListViewPageChangeListener implements PDFListView.PageChangeListener {

        @Override
        public void onPageChange(int pageIndex) {
            Constant.nCurrentPageIndex.set(pageIndex);
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageChange(pageIndex);
            }
        }

        @Override
        public void scrollToBottom(boolean isBottom) {
            if (mPageChangeListener != null) {
                mPageChangeListener.endDocument(isBottom);
            }
        }
    }

    /**
     * 按目前逻辑，状态在Activity 的onresume中初始化，在onpause中销毁．所以关于状态的一些广播的注册和监听也放在PDFView的相应（onresume,onPasue）地方
     * <p>
     * 如果在副屏幕，
     * 监听0-180,来通知时LCD给电
     * 180-360 来通知不管
     * <p>
     * 不论angleMode什么模式，闹钟响铃，并且在副屏则点亮LCD屏幕
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_NOTIFICATION_BROADCAST)) {
                LogUtils.d(TAG, "onReceive: 收到通知");
                handleNotification();
            } else if (ALARM_ALERT_ACTION.equals(intent.getAction())) {
                LogUtils.d(TAG, " com.android.deskclock.ALARM_ALERT");
                if (!Constant.isShowInMainSceen) {
                    AppUtils.setLCDSwitchOnOff("1");
                }
            } else if (ALARM_DONE_ACTION.equals(intent.getAction())) {
                LogUtils.d(TAG, " com.android.deskclock.ALARM_DONE");
                if (mHelpHandler == null) {
                    mHelpHandler = new HelpHandler();
                }
                if (mHelpHandler.hasMessages(MSG_HANDLE_ALARM_DONE)) {
                    mHelpHandler.removeMessages(MSG_HANDLE_ALARM_DONE);
                }
                mHelpHandler.sendEmptyMessage(MSG_HANDLE_ALARM_DONE);
            } else if (POWER_PRESSED_ACTION.equals(intent.getAction())) {
                if (!Constant.isShowInMainSceen) {
                    //SystemPropertyUtils.setSystemProperty(mContext, "sys.primarypanel.enable", "1");
                    AppUtils.setLCDSwitchOnOff("1");
                }
            }

        }
    };

    /**
     * 放在view中一直存活，直到被销毁
     */
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, "mScreenOffReceiver intent.getaction=" + intent.getAction());
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) && !isScreenOff) {
                LogUtils.d(TAG, "锁屏幕如果在副屏幕则关闭LCD　Constant.isShowInMainSceen＝" + Constant.isShowInMainSceen + " isScreenOff=" + isScreenOff);
                if (!Constant.isShowInMainSceen) {
                    AppUtils.setLCDSwitchOnOff("0");
                    isScreenOff = true;
                }
            }
        }
    };

    private void handleNotification() {
        if (!Constant.isShowInMainSceen) {
            int angleMode = android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                    "okay_angle_mode", 0);
            /**
             * angleMode:
             * 0 - 1°~180° //正常模式（normal）
             * 1 - 181°~300° //帐篷模式1（stand up1）
             * 2 - 300°~340° //帐篷模式2（stand up2）
             * 3 - 340°~359° //平板模式（pad)
             * */
            switch (angleMode) {
                case 0:
                    AppUtils.setLCDSwitchOnOff("1");
                    if (mHelpHandler == null) {
                        mHelpHandler = new HelpHandler();
                    }
                    if (mHelpHandler.hasMessages(MSG_SET_LCD_DISABLE)) {
                        mHelpHandler.removeMessages(MSG_SET_LCD_DISABLE);
                    }
                    mHelpHandler.sendEmptyMessageDelayed(MSG_SET_LCD_DISABLE, 5 * 1000);
                    break;
                case 1:
                case 2:
                case 3:
                    break;
                default:
                    break;
            }
            LogUtils.d(TAG, "当前有通知，并且在副屏阅读 angleMode=" + angleMode);

        } else {
            LogUtils.d(TAG, "当前有通知，并且在主阅读，不做任何处理");
        }
    }


    class HelpHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_LCD_DISABLE:
                    LogUtils.d(TAG, "handleMessage: 0 -180 度接收到通知，5秒没有操作，主动熄灭LCD屏幕");
                    AppUtils.setLCDSwitchOnOff("0");
                    break;
                case MSG_HANDLE_ALARM_DONE:
                    if (!Constant.isShowInMainSceen) {
                        AppUtils.setLCDSwitchOnOff("0");
                    }
                    LogUtils.d(TAG, " com.android.deskclock.ALARM_DONE");
                    break;
                case MSG_HANDLE_LOST_WINDOW_FOCUS:
                    LogUtils.d(TAG, "MSG_HANDLE_LOST_WINDOW_FOCUS isScreenOff = " + isScreenOff);
                    if (!isScreenOff) {
                        AppUtils.setLCDSwitchOnOff("1");
                    }

                    break;
                default:
                    break;
            }
        }
    }

    private class AngleModeObserver extends ContentObserver {

        public AngleModeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            int angleMode = android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                    "okay_angle_mode", 0);
            LogUtils.d(TAG, "onchange selfChange=" + selfChange + " angleMode=" + angleMode);
        }
    }


    /**
     * 页面变化的监听
     */
    public interface OnPageChangeListener {
        void onPageChange(int selected);

        void onPreClick();

        void onLaterClick();

        void endDocument(boolean isBottom);//阅读到文档底部时的回调
    }

    /**
     * 页面加载成功/失败监听
     */
    public interface OnPdfLoadListener {
        void loadError();

        void loadComplete();
    }

    public class Configurator {
        private OnPageChangeListener mPageChangeListener;
        private OnPdfLoadListener mPdfLoadListener;
        private boolean mIsDebug = true;
        private int mMainTitleBarHeight = 0;
        private int mPrentTitleBarHeight = 0;

        public Configurator() {
        }

        public Configurator onPageChangeListener(OnPageChangeListener pageChangeListener) {
            mPageChangeListener = pageChangeListener;
            return this;
        }

        public Configurator onPdfLoadListener(OnPdfLoadListener pdfLoadListener) {
            mPdfLoadListener = pdfLoadListener;
            return this;
        }

        public Configurator isDebug(boolean isDebug) {
            mIsDebug = isDebug;
            return this;
        }

        public Configurator setContext(Activity activity) {
            //mActivity = activity;
            return this;
        }

        public Configurator setMainTitleBarHeight(int height) {
            mMainTitleBarHeight = height;
            return this;
        }

        public Configurator setPrentTitleBarHeight(int height) {
            mPrentTitleBarHeight = height;
            return this;
        }

        public void load() {
            //PDFViewer.this.recycle();
            PDFViewer.this.setOnPageChangeListener(mPageChangeListener);
            PDFViewer.this.setOnPdfLoadListener(mPdfLoadListener);
            PDFViewer.this.setIsDebug(mIsDebug);
            PDFViewer.this.setMainTitleBarHeight(mMainTitleBarHeight);
            PDFViewer.this.setPrentTitleBarHeight(mPrentTitleBarHeight);
            PDFViewer.this.load();
        }
    }
}
