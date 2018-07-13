package com.okay.reader.plugin.pdf.ui.task;

import android.content.Context;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.ui.view.BasePDFView;


/**
 * Created by qiaodong on 17-7-12.
 */
public class ScreenChangeTask extends BaseTask {
    public int dialogMsgResId = R.string.screen_change_dialog_msg;


    public ScreenChangeTask(Context context, BasePDFView pdfView) {
        super(context, pdfView);
    }

    public ScreenChangeTask(Context context, BasePDFView pdfView, boolean isShowDialog) {
        super(context, pdfView, isShowDialog);
    }
}
