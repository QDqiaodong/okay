package com.okay.reader.plugin.pdf;

import com.okay.reader.plugin.base.BasePresenter;
import com.okay.reader.plugin.base.BaseView;
import com.okay.reader.plugin.pdf.ui.view.BasePDFView;

/**
 * Created by qiaodong on 17-6-17.
 * This specifies the contract between the view and the presenter.
 */
public interface PdfBaseContract {


    interface IView extends BaseView<IPresenter> {
        /**
         * 更新文章标题
         */
        //void updateTitle(String title);

        /**
         * 更新浏览模式UI
         */
        //void updateBrowseDirection();

        /**
         * 菜单栏的显示与隐藏
         */
        //void toggleMenuView();

        /**
         * 浏览模式切换dialog
         *
         * @param basePDFView 当前PDFview
         */
        void showChangeModeDialog(BasePDFView basePDFView);

        /**
         * 屏幕方向切换dialog
         */
        void showOrientationChangeDialog();

        /**
         * 显示更多设置
         *//*
        //void showMoreSettingsWindow();

        /**
         * 　PDF解析或者加载错误页面
         */
        void showErrorPage();

        /**
         * 添加引导页面　(左右 / 上下)
         *
         * @param resId
         */
        // void addGuidView(int resId);

      /*  void quickSeekChanged(int progress);*/

        /**
         * 获取当前的PDFView (PDFViewPager or PDFListView)
         *
         * @return
         */
        BasePDFView getCurrentPdfView();

        /**
         * 获取当前页号
         *
         * @return
         */
        int getCurrentPage();

        /**
         * 获取页数
         *
         * @return
         */
        int getCount();

        //void exit();
    }


    interface IPresenter extends BasePresenter {

        /**
         * 退出阅读器
         */
        // void performExitDialog();

        /**
         * 切换浏览方向（上下浏览，左右浏览）
         */
        //void performBrowseDirection();

        /**
         * 快速阅读listener
         */
        //SeekBar.OnSeekBarChangeListener getSeekBarChangeListener();

        void onResume();
    }
}





