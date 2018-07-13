package com.okay.reader.plugin.pdf.pdflib;

import android.graphics.RectF;

/**
 * 对应一串
 */
public class TextWord extends RectF {
	public String w;

	public TextWord() {
		super();
		w = new String();
	}

	public void Add(TextChar tc) {
		super.union(tc);
		w = w.concat(new String(new char[]{tc.c}));
	}
}
