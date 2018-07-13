package com.okay.reader.plugin.pdf.ui.task;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.view.Display;
import android.view.WindowManager;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.model.manager.PdfParseManager;
import com.okay.reader.plugin.pdf.ui.presentation.ChangeModeDialogPresentation;
import com.okay.reader.plugin.pdf.ui.view.BasePDFView;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LogUtils;
import com.okay.reader.plugin.utils.SystemPropertyUtils;


/**
 * Created by ZhanTao on 7/12/17.
 */

public class BaseTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "BaseTask";
    private Context mContext;

    private boolean mIsShowDialog;
    public int dialogMsgResId = R.string.mode_change_dialog_msg;

    private ChangeModeDialogPresentation mChangeModeDialog;
    public ChangeCompleteListener mListener;
    private BasePDFView mPdfView;

    public BaseTask(Context context, BasePDFView pdfView) {
        mContext = context;
        mIsShowDialog = true;
        mPdfView = pdfView;
    }

    public BaseTask(Context context, BasePDFView pdfView, boolean isShowDialog) {
        mContext = context;
        mIsShowDialog = isShowDialog;
        mPdfView = pdfView;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (PdfParseManager.getInstance().getImageLoader() != null) {
            PdfParseManager.getInstance().getImageLoader().changeMode();
        }

        LogUtils.d(TAG + " onPreExecute mIsShowDialog=" + mIsShowDialog);
        if (mIsShowDialog)
            showChangeModeDialog();
    }

    @Override
    protected Void doInBackground(Void... params) {
        LogUtils.d(TAG + " doInBackground-----");

        if (PdfParseManager.getInstance().getImageLoader() != null) {
            PdfParseManager.getInstance().getImageLoader().acquireRunningSemaphore();
            PdfParseManager.getInstance().getImageLoader().releaseRunningSemaphore();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        LogUtils.d(TAG + " onPostExecute");
        if (mChangeModeDialog != null && mChangeModeDialog.isShowing()) {
            mChangeModeDialog.dismiss();
        }
        PdfParseManager.getInstance().reConfigure();

        if (mPdfView != null) {
            LogUtils.d(TAG + " Constant.nCurrentPageIndex.get()=" + Constant.nCurrentPageIndex.get());
            mPdfView.goTo(Constant.nCurrentPageIndex.get());
            mPdfView.notifyDataChanged();
        }

        if (mListener != null) {
            mListener.complete();
        }
    }


    public void showChangeModeDialog() {
        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager.getDisplays();

        if (mChangeModeDialog != null && mChangeModeDialog.isShowing()) {
            return;
        }

        int layoutId = R.layout.change_mode_dialog;
        Display presentationDisplay = presentationDisplays[0];
        if (presentationDisplays.length > 1 && !Constant.isShowInMainSceen) {
            presentationDisplay = presentationDisplays[1];
            layoutId = R.layout.change_mode_dialog_epd;
        }
        mChangeModeDialog = new ChangeModeDialogPresentation(mContext, presentationDisplay,
                R.style.BasePresentation, layoutId, dialogMsgResId);
        mChangeModeDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        SystemPropertyUtils.setSystemProperty(mContext, "sys.eink.Appmode", "13");
        mChangeModeDialog.show();
    }

    public interface ChangeCompleteListener {
        void complete();
    }

    public BaseTask setChangeCompleteListener(ChangeCompleteListener listener) {
        mListener = listener;
        return this;
    }
}