package com.okay.reader.plugin.pdf.ui.view;

/**
 * Created by qiaodong on 17-7-12.
 */
public interface BasePDFView {
    public int getCount();

    public int getCurrentItem();

    public void prePage();

    public void nextPage();

    public void goTo(int destNum);

    public void notifyDataChanged();

    public void scrollTo(int scroll);

}
