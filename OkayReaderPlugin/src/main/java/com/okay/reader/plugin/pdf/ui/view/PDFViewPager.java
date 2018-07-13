/**
 * Copyright 2016 JustWayward Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okay.reader.plugin.pdf.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.ui.adapter.PDFPagerAdapter;
import com.okay.reader.plugin.pdf.utils.OnPageClickListener;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LangUtils;
import com.okay.reader.plugin.utils.ToastUtil;

public class PDFViewPager extends ViewPager implements BasePDFView {

    protected Context context;
    private PDFPagerAdapter mPdfPagerAdapter;

    public PDFViewPager(Context context) {
        super(context);
        this.context = context;
        setClickable(true);
        initAdapter(context);
    }

    protected void initAdapter(Context context) {
        mPdfPagerAdapter = new PDFPagerAdapter.Builder(context)
                .setOnPageClickListener(clickListener)
                .create();
        setAdapter(mPdfPagerAdapter);
        this.notifyDataChanged();
    }

    private OnPageClickListener clickListener = new OnPageClickListener() {

        @Override
        public void onPageTap(View view, float x, float y) {
            int item = getCurrentItem();
            int total = getChildCount();

            if (x < 0.33f && item > 0) {
                item -= 1;
                setCurrentItem(item);
            } else if (x >= 0.67f && item < total - 1) {
                item += 1;
                setCurrentItem(item);
            }
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getCount() {
        if (mPdfPagerAdapter != null)
            return mPdfPagerAdapter.getCount();
        return 0;
    }

    @Override
    public void prePage(){
        int item = this.getCurrentItem();
        if(item < 1) {
            ToastUtil.getInstance().showToast(context.getString(R.string.page_index_already_begin),
                    Toast.LENGTH_SHORT, Gravity.BOTTOM | Gravity.CENTER, 0, LangUtils.rp(192));
            return;
        }
        this.setCurrentItem(item - 1, Constant.isShowInMainSceen ? true : false);
    }

    @Override
    public void nextPage(){
        int item = this.getCurrentItem();
        if (item >= this.getCount() - 1) {
            ToastUtil.getInstance().showToast(context.getString(R.string.page_index_already_end),
                    Toast.LENGTH_SHORT, Gravity.BOTTOM | Gravity.CENTER, 0, LangUtils.rp(192));
            return;
        }
        this.setCurrentItem(item + 1,Constant.isShowInMainSceen ? true : false);
    }

    @Override
    public void goTo(int destNum) {
       this.setCurrentItem(destNum,false);
    }

    @Override
    public void scrollTo(int scroll) {

    }

    @Override
    public void notifyDataChanged() {
        mPdfPagerAdapter.notifyDataSetChanged();
    }


    public PDFPagerAdapter getAdapter() {
        return mPdfPagerAdapter;
    }
}
