package com.okay.reader.plugin.pdf.presenter;

import android.content.Context;

import android.app.OKAYAdapterManager;
import com.okay.reader.plugin.pdf.PdfMainContract;
import com.okay.reader.plugin.pdf.model.entity.PdfProgress;
import com.okay.reader.plugin.pdf.model.entity.PdfProgressDao;
import com.okay.reader.plugin.pdf.model.manager.DBManager;
import com.okay.reader.plugin.pdf.model.manager.PdfParseManager;
import com.okay.reader.plugin.pdf.ui.view.BasePDFView;
import com.okay.reader.plugin.pdf.ui.view.PDFListView;
import com.okay.reader.plugin.utils.AppUtils;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.FileUtils;
import com.okay.reader.plugin.utils.LangUtils;
import com.okay.reader.plugin.utils.LogUtils;
import com.okay.reader.plugin.utils.ReaderOkayAdapterMananger;
import com.okay.reader.plugin.utils.SharedPreferencesUtil;
import com.okay.reader.plugin.utils.SystemPropertyUtils;

import java.util.List;

/**
 * Created by qiaodong on 17-9-4.
 */

public class PdfMainPresenter implements PdfMainContract.IPresenter {

    private static final String TAG = "PdfMainPresenter";
    private PdfMainContract.IView mPdfActvity;
    private Context mContext;
    private final OKAYAdapterManager mOkayAdapterManager;
    private boolean isGoToPage = false;

    public PdfMainPresenter(PdfMainContract.IView pdfActivity, Context context) {
        mPdfActvity = pdfActivity;
        mContext = context;
        mOkayAdapterManager = (OKAYAdapterManager) mContext.getSystemService("okay");
    }

    @Override
    public void initData(String filePath) {
        boolean exist = FileUtils.isFileExist(filePath);
        if (!exist)
            mPdfActvity.showErrorPage();

        LogUtils.d(TAG + " init mFilePath=" + Constant.strPdfFilePath);
    }

    @Override
    public void onResume() {
        resumePdfProgress();
        ReaderOkayAdapterMananger.getInstance().performOpenEinkTouch(mContext);
       // performOpenEinkTouch();
    }

    @Override
    public void onPause() {
        SharedPreferencesUtil.getInstance().putBoolean(Constant.READ_MODE, Constant.isShangXia);
        performSavePdfProgress();
        ReaderOkayAdapterMananger.getInstance().performResumeEinkTouch(mContext);
        //performResumeEinkTouch();
    }

    private void resumePdfProgress() {
        final BasePDFView basePDFView = mPdfActvity.getCurrentPdfView();
        if (LangUtils.isEmpty(Constant.strPdfFilePath) || basePDFView == null) {
            LogUtils.d(TAG + " resumePdfProgress error Constant.strPdfFilePath=" + Constant.strPdfFilePath);
            return;
        }

        String strQuery = "where " + PdfProgressDao.Properties.Path.columnName + " = ?";
        List<PdfProgress> pdfProgresses = DBManager.getInstance().getSession().getPdfProgressDao()
                .queryRaw(strQuery, Constant.strPdfFilePath);

        final PdfProgress pdfProgress = LangUtils.getFirstObj(pdfProgresses);
        if (pdfProgress != null) {
            if (basePDFView.getCount() < 1
                    || pdfProgress.getIndex() < 0
                    || pdfProgress.getSum() < 1
                    || pdfProgress.getIndex() > basePDFView.getCount() - 1) {
                LogUtils.d(TAG, "resumePdfProgress error");
            } else {
                Constant.nCurrentPageIndex.set(pdfProgress.getIndex());
                mPdfActvity.setPdfListViewDataChangeListener(new PDFListView.DataChangedListener() {
                    @Override
                    public void onSuccess() {
                        if(!isGoToPage){
                            basePDFView.goTo(pdfProgress.getIndex());
                            isGoToPage = true;
                        }
                    }
                });
                LogUtils.d(TAG, "resumePdfProgress..pdfProgress.getIndex()=" + pdfProgress.getIndex());
            }
        }
    }

    private void performOpenEinkTouch() {
        if (Constant.isDoubleScreen) {
            if (AppUtils.isSwtcon2()) {
                mOkayAdapterManager.setEinkMode(1);
                mOkayAdapterManager.setEinkDither(0);
                mOkayAdapterManager.enableEinkForceUpdate();
                SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subInvalid", "1");
            } else {
                SystemPropertyUtils.setSystemProperty(mContext, "sys.eink.mode", "11"); //图片用MODE_GLD16
                SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subPen", "0"); //笔触开关
                SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subKey", "0");//电容开关
                SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subTp", "0");//eink屏幕首触开关
                SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subInvalid", "1");
            }
        }
    }

    private void performResumeEinkTouch() {
        if (Constant.isDoubleScreen) {
            int angleMode = android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                    "okay_angle_mode", 0);
            if (angleMode > 0) {
                if (AppUtils.isSwtcon2()) {
                    //nothing
                } else {
                    SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subPen", "1");
                    SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subKey", "1");
                    SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subTp", "1");
                }
            }
            SystemPropertyUtils.setSystemProperty(mContext, "sys.close.subInvalid", "0");
        }
    }

    @Override
    public void performSavePdfProgress() {
        BasePDFView basePDFView = mPdfActvity.getCurrentPdfView();

        if (LangUtils.isEmpty(Constant.strPdfFilePath)
                || basePDFView == null
                || basePDFView.getCurrentItem() < 0
                || basePDFView.getCount() < 1
                || basePDFView.getCurrentItem() >= basePDFView.getCount()) {
            LogUtils.d(TAG + " performSavePdfProgress error");
            return;
        }

        PdfProgress pdfProgress = new PdfProgress();
        pdfProgress.setIndex(basePDFView.getCurrentItem());
        pdfProgress.setPath(Constant.strPdfFilePath);
        pdfProgress.setSum(basePDFView.getCount());
        pdfProgress.setTime(System.currentTimeMillis());
        pdfProgress.setSeen(Constant.isOriginalOfficeOpened ? 1 : 0);
        DBManager.getInstance().getSession().getPdfProgressDao().insertOrReplace(pdfProgress);
        LogUtils.d(TAG, "performSavePdfProgress success index=" + basePDFView.getCurrentItem());
    }


    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "PdfMainPresenter onDestroy ");
        PdfParseManager.getInstance().getImageLoader().changeMode();
        PdfParseManager.getInstance().close();
        mPdfActvity = null;
    }
}
