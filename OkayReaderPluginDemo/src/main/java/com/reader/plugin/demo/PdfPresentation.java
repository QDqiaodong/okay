package com.reader.plugin.demo;

import android.app.Presentation;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.okay.reader.plugin.pdf.PDFViewer;

/**
 * Created by qiaodong on 17-6-16.
 */
public class PdfPresentation extends Presentation implements View.OnClickListener {

    private ReaderPluginTest mContext;
    private PDFViewer mPdfViewer;

    public LinearLayout mMainLayoutRoot; //Presentation的顶级布局
    public FrameLayout mContentView; //PdfWiew 的父布局

    private TextView tvTitle;
    private View tvOpenWPS;
    private View tvSwitchScreen;
    private ImageView ivReturn;

    public PdfPresentation(ReaderPluginTest outerContext, Display display, PDFViewer pdfViewer) {
        super(outerContext, display);
        mContext = outerContext;
        mPdfViewer = pdfViewer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_main_layout);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateTitle();
    }

    private void initView() {
        //1.顶层布局： 内容布局
        mMainLayoutRoot = (LinearLayout) findViewById(R.id.main_presentation_layout_root);
        mContentView = (FrameLayout) findViewById(R.id.layout_content);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvOpenWPS = findViewById(R.id.tv_open_wps);
        tvSwitchScreen = findViewById(R.id.tv_switch_screen);
        ivReturn = (ImageView) findViewById(R.id.iv_return);

        ivReturn.setOnClickListener(this);
        tvSwitchScreen.setOnClickListener(this);
        tvOpenWPS.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View v) {
        if (v == ivReturn) {
            performExitDialog();
        } else if (v == tvSwitchScreen) {
            mContext.performShowMainScreen();
        }
    }

    public void updateTitle() {
        if (!TextUtils.isEmpty(DemoConstant.FILE_NAME)) {
            tvTitle.setText(DemoConstant.FILE_NAME);
        } else {
            tvTitle.setText("无标题");
        }
    }

    public void performExitDialog() {
       mContext.exit();
    }
}
