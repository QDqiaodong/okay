package com.okay.reader.plugin;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.okay.reader.plugin.pdf.model.manager.DBManager;
import com.okay.reader.plugin.utils.AppUtils;
import com.okay.reader.plugin.utils.Constant;
import com.okay.reader.plugin.utils.SharedPreferencesUtil;

/**
 * Created by qiaodong on 17-12-2.
 */

public class PDFManager {
    private static PDFManager instance;

    private PDFManager() {

    }

    public static PDFManager getInstance() {
        if (instance == null) {
            instance = new PDFManager();
        }
        return instance;
    }

    public void init(Context context) {
        AppUtils.init(context);
        SharedPreferencesUtil.init(context, context.getPackageName() + "_preference", Context.MODE_PRIVATE);
        DBManager.getInstance().init(context);

        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager
                .getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (presentationDisplays.length <= 0) {
            Constant.isDoubleScreen = false;
        } else {
            Constant.isDoubleScreen = true;
        }
    }
}
