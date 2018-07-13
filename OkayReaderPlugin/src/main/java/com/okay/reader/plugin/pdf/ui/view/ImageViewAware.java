package com.okay.reader.plugin.pdf.ui.view;

import android.view.View;
import android.widget.ImageView;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Wrapper for Android {@link ImageView ImageView}. Keeps weak reference of ImageView to prevent memory
 */

public class ImageViewAware {
    protected Reference<View> viewRef;

    public ImageViewAware(ImageView view) {
        if (view == null) throw new IllegalArgumentException("view must not be null");
        this.viewRef = new WeakReference<View>(view);
    }

    public View getWrappedView() {
        return viewRef.get();
    }

    public void setTag(final Object tag) {
        getWrappedView().setTag(tag);
    }

    public boolean isCollected() {
        return viewRef.get() == null;
    }
}
