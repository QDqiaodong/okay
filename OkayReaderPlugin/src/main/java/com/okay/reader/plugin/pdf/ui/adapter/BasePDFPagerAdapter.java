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
package com.okay.reader.plugin.pdf.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.okay.reader.plugin.pdf.model.manager.PdfParseManager;


public class BasePDFPagerAdapter extends PagerAdapter {

    public Context context;
    public LayoutInflater inflater;

    public BasePDFPagerAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // bitmap.recycle() causes crashes if called here.
        // All bitmaps are recycled in close().
        container.removeView((View) object);
    }

    /**
     * 当需要刷新页面时，调用notifyDataSetChanged()
     * 会调用getItemPosition，返回两个状态值，POSITION_NONE / POSITION_UNCHANGED
     * 如果是POSITION_NONE item会被remove掉再重新加载。
     *
     * @param object
     * @return
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    @Override
    @SuppressWarnings("NewApi")
    public int getCount() {
        return PdfParseManager.getInstance().getMuPDFCore() != null ? PdfParseManager.getInstance().getMuPDFCore().countPages() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
