package com.okay.reader.plugin.pdf.ui.task;

import android.content.Context;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.ui.view.BasePDFView;

/**
 * Created by qiaodong on 17-7-12.
 */
public class OrientationChangeTask extends BaseTask {
    public int dialogMsgResId = R.string.orientation_change_dialog_msg;

    public OrientationChangeTask(Context context, BasePDFView pdfView) {
        super(context, pdfView);
    }

    public OrientationChangeTask(Context context, BasePDFView pdfView, boolean isShowDialog) {
        super(context, pdfView, isShowDialog);
    }


}
