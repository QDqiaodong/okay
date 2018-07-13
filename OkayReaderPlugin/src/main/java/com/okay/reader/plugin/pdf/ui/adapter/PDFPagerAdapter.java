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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.okay.reader.plugin.R;
import com.okay.reader.plugin.pdf.model.manager.PdfParseManager;
import com.okay.reader.plugin.pdf.ui.view.PdfImageView;
import com.okay.reader.plugin.pdf.utils.OnPageClickListener;
import com.okay.reader.plugin.pdf.utils.PdfScale;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.LogUtils;


public class PDFPagerAdapter extends BasePDFPagerAdapter
        {

    private static final String TAG = "PDFPagerAdapter";
    private static final float DEFAULT_SCALE = 1f;

    PdfScale scale = new PdfScale();
    OnPageClickListener pageClickListener;

    public PDFPagerAdapter(Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("NewApi")
    public Object instantiateItem(ViewGroup container, int position) {
        LogUtils.d(TAG,"PDFPagerAdapter instantiateItem position="+position);
        FrameLayout root = (FrameLayout) inflater.inflate(R.layout.view_pdf_page_item, container, false);
        TextView progress = (TextView) root.findViewById(R.id.tv_progress);
        final PdfImageView iv;
        View defaultBg;

        iv = (PdfImageView) root.findViewById(R.id.iv_left_and_right);

        if (PdfParseManager.getInstance().getMuPDFCore() == null || getCount() < position) {
            return root;
        }
        //2.添加默认页面
        defaultBg = inflater.inflate(R.layout.default_pdf_view_layout, null);
        root.addView(defaultBg);

        //3.异步线程渲染pdf到bitmap
        PdfParseManager.getInstance().loadImage(iv, position, defaultBg, root);

        //4.当前页号
        progress.setText(String.valueOf(position+1)+"/"+Constant.DOCUMENT_TOTAL_COUNT);
        if(Constant.isShowInMainSceen){
            //setTextSize 会计算此值对应的px值
            progress.setTextSize(18);
        }else{
            progress.setTextSize(12);
        }

        //5.将imageview添加到PhotoView用于缩放
     /*   PhotoViewAttacher attacher = new PhotoViewAttacher(iv);
        attacher.setScale(scale.getScale(), scale.getCenterX(), scale.getCenterY(), true);
        attacher.setOnMatrixChangeListener(this);
        attachers.put(position, new WeakReference<PhotoViewAttacher>(attacher));
        attacher.update();*/
        container.addView(root, 0);
        return root;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container,position,object);
    }

    public static class Builder {
        Context context;
        String pdfPath = "";
        float scale = DEFAULT_SCALE;
        float centerX = 0f, centerY = 0f;
        OnPageClickListener pageClickListener;

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

        public Builder setPdfPath(String path) {
            this.pdfPath = path;
            return this;
        }

        public Builder setOnPageClickListener(OnPageClickListener listener) {
            if (listener != null) {
                pageClickListener = listener;
            }
            return this;
        }

        public PDFPagerAdapter create() {
            PDFPagerAdapter adapter = new PDFPagerAdapter(context);
            adapter.scale.setScale(scale);
            adapter.scale.setCenterX(centerX);
            adapter.scale.setCenterY(centerY);
            adapter.pageClickListener = pageClickListener;
            return adapter;
        }
    }
}
