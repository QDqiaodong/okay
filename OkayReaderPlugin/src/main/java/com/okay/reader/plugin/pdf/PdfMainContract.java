package com.okay.reader.plugin.pdf;

import com.okay.reader.plugin.pdf.ui.view.PDFListView;

/**
 * Created by qiaodong on 17-6-17.
 * This specifies the contract between the view and the presenter.
 */
public interface PdfMainContract {


    interface IView extends PdfBaseContract.IView {
        void update();
        void refresh();
        void scrollToDestPosition();
        void setPdfListViewDataChangeListener(PDFListView.DataChangedListener dataChangedListener);
    }


    interface IPresenter extends PdfBaseContract.IPresenter {

       // void setView(PdfMainContract.IView iView);
        /**
         *　获取文件路径，阅读模式。
         */
        void initData(String filePath);

        void onPause();

        void onDestroy();

        void performSavePdfProgress();
    }
}





