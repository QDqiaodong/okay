package com.okay.reader.plugin.demo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.okay.reader.plugin.pdf.PDFViewer;
import com.okay.reader.plugin.utils.FileUtils;
import com.okay.reader.plugin.utils.ToastUtil;

public class ReaderPluginTest extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ReaderPluginTest";
    private String filePath;
    private TextView tvTitle;
    private View tvSwitchScreen;
    private ImageView ivReturn;
    private ImageView tvOpenWPS;
    private boolean isShowInMain = true;
    private PdfPresentation mPresentation;
    /**
     * Example: /sdcard/<时间简史>中文版.pdf
     */
    private PDFViewer pdfViewer;
    private String mFileName;

    private Context mContext;
    private HelpHandler helpHandler;
    private ImageView centerIv;
    private View footerView2;
    private CommonDialog commonDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.reader_pdf_test_layout);
        initView();
        initData();
        tvTitle.setText(FileUtils.getFileName(filePath));
        if (filePath == null) {
            ToastUtil.getInstance().showToast("该文件打不开");
            return;
        }
    }

    private void initView() {
        pdfViewer = (PDFViewer) findViewById(R.id.pdf_view);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvOpenWPS = (ImageView) findViewById(R.id.tv_open_wps);
        tvSwitchScreen = findViewById(R.id.tv_switch_screen);
        ivReturn = (ImageView) findViewById(R.id.iv_return);
        ivReturn.setOnClickListener(this);
        tvSwitchScreen.setOnClickListener(this);
        tvOpenWPS.setOnClickListener(this);
        helpHandler = new HelpHandler();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                filePath = Uri.decode(intent.getData().getEncodedPath());
                mFileName = intent.getStringExtra("FILE_NAME");
                DemoConstant.FILE_NAME = mFileName;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume．．．．．．．．");
        show();
        pdfViewer.onResume(DemoConstant.isShowInMainSceen);
        if (DemoConstant.isShowInMainSceen)
            return;
        performShowPresentation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause．．．．．．．．");
        if (mPresentation != null && mPresentation.isShowing()) {
            mPresentation.mContentView.removeAllViews();
            mPresentation.mMainLayoutRoot.removeAllViews();
            mPresentation.dismiss();
            mPresentation = null;
            Log.d(TAG, "onPause: ReaderPluginTest1 removeAllViews");
        }

        pdfViewer.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (pdfViewer != null) {
                if (DemoConstant.isShowInMainSceen) {
                    showExitDialog();
                } else if (mPresentation != null) {
                    mPresentation.performExitDialog();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //TODO
    private void show() {
        int mainTitleBarHeight = (int) getResources().getDimension(R.dimen.main_title_bar_height);
        pdfViewer.displayFromPath(filePath)
                .onPageChangeListener(new PDFViewer.OnPageChangeListener() {
                    @Override
                    public void onPageChange(int selected) {
                    }

                    @Override
                    public void onPreClick() {
                    }

                    @Override
                    public void onLaterClick() {
                    }

                    @Override
                    public void endDocument(boolean isBottom) {
                        if (isBottom) {
                            //TODO
                            int count = pdfViewer.getCount();
                            int currentPage = pdfViewer.getCurrentPage() + 1;
                            String message = currentPage + "/" + count;
                            ToastUtil.getInstance().showToast("当前页号　" + message);
                        }

                    }

                }).onPdfLoadListener(new PDFViewer.OnPdfLoadListener() {
            @Override
            public void loadError() {
            }

            @Override
            public void loadComplete() {
            }
        })
                .isDebug(true)/**The default value is true,if true will output logs*/
                .setMainTitleBarHeight(mainTitleBarHeight)
                .setPrentTitleBarHeight(80)
                .load();

        final View footerView1 = getLayoutInflater().inflate(R.layout.footer_view, null);
        footerView2 = getLayoutInflater().inflate(R.layout.footer_view2, null);
        centerIv = (ImageView) footerView2.findViewById(R.id.iv_center);
        centerIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.getInstance().showToast("点击底部View");
            }
        });

        //TODO
        /*pdfViewer.setFooterView(footerView1);
        helpHandler.sendEmptyMessageDelayed(HelpHandler.MSG_REPLACE_IMAGE_VIEW, 3000);
        helpHandler.sendEmptyMessageDelayed(HelpHandler.MSG_REPLACE_FOOTER_VIEW, 6000);*/
    }


    @Override
    public void onClick(View v) {
        if (v == ivReturn) {
            showExitDialog();
        } else if (v == tvOpenWPS) {
            //ToastUtil.getInstance().showToast("用WPS打开文档");
        } else if (v == tvSwitchScreen) {
            //TODO 需要简化
            DemoConstant.isShowInMainSceen = !isShowInMain;
            if (!DemoConstant.isShowInMainSceen) {
                performShowPresentation();
            }
        }
    }

    private void showExitDialog() {
        if (commonDialog != null && commonDialog.isShowing()) {
            return;
        }

        commonDialog = new CommonDialog(this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
        commonDialog.setTitle(R.string.dialog_title_quit);
        commonDialog.setButtonTxt("取消", "确定");
        commonDialog.show();
    }

    /**
     * 副屏幕显示
     */
    private void performShowPresentation() {
        if (mPresentation == null) {
            initPresentation();
        }
        pdfViewer.getRootView().removeAllViews();
        pdfViewer.getRootView().setBackgroundColor(getResources().getColor(com.okay.reader.plugin.R.color.color_black));
        mPresentation.mContentView.removeAllViews();

        //TODO
        pdfViewer.showLCD2EPDDialog(mPresentation.mContentView);
    }

    private void initPresentation() {
       /* if (DemoConstant.isShowInMainSceen)
            return;
        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager
                .getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (presentationDisplays.length > 0) {
            Display presentationDisplay = presentationDisplays[0];
            mPresentation = new PdfPresentation(this, presentationDisplay, pdfViewer);
            mPresentation.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

            SystemPropertyUtils.setSystemProperty(mContext, "sys.eink.Appmode", "13");
            mPresentation.show();
        }*/
    }

    /**
     * EPD 切换到 LCD
     */
    public void performShowMainScreen() {
        if (mPresentation != null && mPresentation.isShowing()) {
            mPresentation.mContentView.removeAllViews();
            mPresentation.mMainLayoutRoot.removeAllViews();
            mPresentation.dismiss();
            mPresentation = null;
        }

        //TODO
        pdfViewer.showEPD2LCDDialog();
    }

    public void exit() {
        if (!DemoConstant.isShowInMainSceen) {
            if (mPresentation != null && mPresentation.isShowing()) {
                mPresentation.mContentView.removeAllViews();
                mPresentation.mMainLayoutRoot.removeAllViews();
                mPresentation.dismiss();
                mPresentation = null;
            }
            DemoConstant.isShowInMainSceen = !DemoConstant.isShowInMainSceen;
        }
        this.finish();
    }

    class HelpHandler extends Handler {
        public static final int MSG_REPLACE_FOOTER_VIEW = 1;
        public static final int MSG_REPLACE_IMAGE_VIEW = 2;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REPLACE_FOOTER_VIEW:
                    pdfViewer.setFooterView(footerView2);
                    break;
                case MSG_REPLACE_IMAGE_VIEW:
                    centerIv.setImageResource(R.drawable.ppt);
                    break;
                default:
                    break;
            }

        }
    }
}
