package com.okay.reader.plugin.pdf.ui.task;

import android.content.Context;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.ui.view.BasePDFView;


/**
 * Created by ZhanTao on 7/12/17.
 */

public class ChangeModeTask extends BaseTask{
    public int dialogMsgResId = R.string.mode_change_dialog_msg;


    public ChangeModeTask(Context context, BasePDFView pdfView) {
        super(context, pdfView);
    }

    public ChangeModeTask(Context context, BasePDFView pdfView, boolean isShowDialog) {
        super(context, pdfView, isShowDialog);
    }
}