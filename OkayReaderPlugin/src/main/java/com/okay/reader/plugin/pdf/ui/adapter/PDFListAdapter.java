/**
 * Copyright 2016 JustWayward Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okay.reader.plugin.pdf.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.model.manager.PdfParseManager;
import com.okay.reader.plugin.pdf.ui.view.PdfImageView;
import com.okay.reader.plugin.pdf.utils.PdfScale;
import com.okay.reader.plugin.utils.AppUtils;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LogUtils;


public class PDFListAdapter extends BaseAdapter{

    private static final String TAG = "PDFListAdapter";
    private Context context;

    public PDFListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return PdfParseManager.getInstance().getMuPDFCore() != null ? PdfParseManager.getInstance().getMuPDFCore().countPages() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LogUtils.d(TAG + " getView position=" + position + "; convertView=" + convertView+" Constant.isNavigationBarShowing="+Constant.isNavigationBarShowing);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View defaultBg = null;
        PdfImageView iv = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_pdf_list_item, parent, false);
        }else{

            //因为底部导航栏引起的listview重新获取view,则此次不重新解析pdf页面
            if(Constant.nCurrentPageIndex.get() == position && Constant.isNavigationBarShowing && AppUtils.isEPAD()){
                return convertView;
            }

            if(convertView.findViewById(R.id.layout_loading) != null){
                defaultBg = convertView.findViewById(R.id.layout_loading);
            }
            if(defaultBg != null)
                ((FrameLayout)convertView).removeView(defaultBg);

            if(convertView.findViewById(R.id.iv_left_and_right) != null)
                iv = (PdfImageView) convertView.findViewById(R.id.iv_left_and_right);

            if(iv != null)
                    ((FrameLayout)convertView).removeView(iv);
        }

        if (PdfParseManager.getInstance().getMuPDFCore() == null || getCount() < position) {
            return convertView;
        }

        ViewGroup.LayoutParams params = convertView.getLayoutParams();
        params.width = PdfParseManager.getInstance().getDestWidth();
        params.height = PdfParseManager.getInstance().getDestHeight();
        convertView.setLayoutParams(params);

        //2.添加默认页面
        defaultBg = inflater.inflate(R.layout.default_pdf_view_layout, null);

        ((FrameLayout)convertView).addView(defaultBg);

        iv = (PdfImageView) inflater.inflate(R.layout.pdf_image_view, null);
        ((FrameLayout)convertView).addView(iv);

        PdfParseManager.getInstance().loadImage(iv, position, defaultBg, (FrameLayout)convertView);

        return convertView;
    }

    public static class Builder {
        Context context;
        String pdfPath = "";
        float scale = PdfScale.DEFAULT_SCALE;
        float centerX = 0f, centerY = 0f;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setScale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder setScale(PdfScale scale) {
            this.scale = scale.getScale();
            this.centerX = scale.getCenterX();
            this.centerY = scale.getCenterY();
            return this;
        }

        public Builder setCenterX(float centerX) {
            this.centerX = centerX;
            return this;
        }

        public Builder setCenterY(float centerY) {
            this.centerY = centerY;
            return this;
        }

        public PDFListAdapter create() {
            PDFListAdapter adapter = new PDFListAdapter(context);
            return adapter;
        }
    }


}
