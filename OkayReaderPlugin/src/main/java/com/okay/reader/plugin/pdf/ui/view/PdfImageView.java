package com.okay.reader.plugin.pdf.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.okay.reader.plugin.utils.LogUtils;

/**
 * Created by ZhanTao on 7/12/17.
 */

public class PdfImageView extends ImageView {

    public PdfImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            LogUtils.e("MyImageView  -> onDraw() Canvas: trying to use a recycled bitmap");
        }
    }

}