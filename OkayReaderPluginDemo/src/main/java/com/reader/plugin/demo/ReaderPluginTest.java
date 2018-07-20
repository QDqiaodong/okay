package com.reader.plugin.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.okay.reader.plugin.pdf.PDFViewer;

public class ReaderPluginTest extends Activity implements View.OnClickListener {

    private static final String TAG = "ReaderPluginTest";
    private static final String KEY = "cX1/PGB3c3Z3YDxifmd1e3w8dnd/fQ==\n";
    private String filePath;
    private TextView tvTitle;
    private View tvSwitchScreen;
    private ImageView ivReturn;
    private PdfPresentation mPresentation;
    /**
     * Example: /sdcard/<时间简史>中文版.pdf
     */
    private PDFViewer pdfViewer;
    private String mFileName;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.reader_pdf_test_layout);
        initView();
        initData();
        tvTitle.setText(FileUtils.getFileName(filePath));
        if (filePath == null) {
            Toast.makeText(this,"该文件打不开",Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private void initView() {
        pdfViewer = (PDFViewer) findViewById(R.id.pdf_view);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSwitchScreen = findViewById(R.id.tv_switch_screen);
        ivReturn = (ImageView) findViewById(R.id.iv_return);
        ivReturn.setOnClickListener(this);
        tvSwitchScreen.setOnClickListener(this);
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
        show();
        pdfViewer.onResume(DemoConstant.isShowInMainSceen);
        if (DemoConstant.isShowInMainSceen)
            return;
        performShowPresentation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 2");
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
                    exit();
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
        pdfViewer.displayFromPath(filePath,KEY)
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
                .setMainTitleBarHeight(90) /**强制传入，主屏的titlebar的高度*/
                .setPrentTitleBarHeight(80)/**强制传入，副屏的titlebar的高度*/
                .load();
    }


    @Override
    public void onClick(View v) {
        if (v == ivReturn) {
            exit();
        //    showExitDialog();
        } else if (v == tvSwitchScreen) {
            DemoConstant.isShowInMainSceen = !DemoConstant.isShowInMainSceen;
            if (!DemoConstant.isShowInMainSceen) {
                performShowPresentation();
            }
        }
    }

    /**
     * 副屏幕显示
     */
    private void performShowPresentation() {
        if (mPresentation == null) {
            initPresentation();
        }
        //从父类中移除当前pdfView
        pdfViewer.getRootView().removeAllViews();
        pdfViewer.getRootView().setBackgroundColor(getResources().getColor(com.okay.reader.plugin.R.color.color_black));
        //将副屏中的所有子view都移除
        mPresentation.mContentView.removeAllViews();
        //切换副屏，将你定义的副屏中添加PDFView的父控件传入
        pdfViewer.showLCD2EPDDialog(mPresentation.mContentView);
    }

    private void initPresentation() {
        if (DemoConstant.isShowInMainSceen)
            return;
        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager
                .getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (presentationDisplays.length > 0) {
            Display presentationDisplay = presentationDisplays[0];
            mPresentation = new PdfPresentation(this, presentationDisplay, pdfViewer);
            mPresentation.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mPresentation.show();
        }
    }

    /**
     * EPD 切换到 LCD
     */
    public void performShowMainScreen() {
        DemoConstant.isShowInMainSceen = !DemoConstant.isShowInMainSceen;
        if (mPresentation != null && mPresentation.isShowing()) {
            mPresentation.mContentView.removeAllViews();
            mPresentation.mMainLayoutRoot.removeAllViews();
            mPresentation.dismiss();
            mPresentation = null;
        }
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
            DemoConstant.isShowInMainSceen = false;
        }
        this.finish();
    }
}
