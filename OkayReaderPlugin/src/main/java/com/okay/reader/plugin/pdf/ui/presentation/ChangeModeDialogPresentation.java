package com.okay.reader.plugin.pdf.ui.presentation;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.okay.reader.plugin.R;

/**
 * Created by ZhanTao on 7/12/17.
 */

public class ChangeModeDialogPresentation extends Presentation {

    private int mLayoutId;
    private int mMsgResId;
    private ImageView loadingIv;
    private TextView msgTv;
    private Animation rotateInfinite;
    private Context mContext;

    public ChangeModeDialogPresentation(Context outerContext, Display display, int theme, int layoutId,int msgResId) {
        super(outerContext, display, theme);
        mLayoutId = layoutId;
        mMsgResId = msgResId;
        mContext = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutId);
        loadingIv = (ImageView) findViewById(R.id.mode_change_loading_iv);
        msgTv = (TextView) findViewById(R.id.mode_change_msg);
        msgTv.setText(mMsgResId);
        rotateInfinite = AnimationUtils.loadAnimation(mContext, R.anim.rotate_infinite);
        rotateInfinite.setInterpolator(new LinearInterpolator());
        loadingIv.startAnimation(rotateInfinite);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        loadingIv.clearAnimation();
    }
}
