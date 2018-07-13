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
package com.okay.reader.plugin.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yuyh.
 * @date 16/8/5.
 */
public class Constant {

    public static final String READ_MODE = "isShangeXia";
    public static final String SHOW_GUIDE＿PAGE = "show_guide_page";

    public static volatile boolean isShangXia = true;
    public static volatile boolean isShowInMainSceen = true;
    public static volatile boolean isOriginalOfficeOpened = false;
    public static volatile boolean isDoubleScreen = false;
    public static volatile boolean isBottomViewShowing = false;
    public static volatile int bottomViewYLocation = -1;
    public static volatile String SCROLL_PRECENT = "scroll_precent";
    public static volatile  boolean IS_SHOWING_GUIDE = false;
    public static volatile  boolean isNavigationBarShowing = false;

    public static volatile String strPdfFilePath;
    public static volatile int pdfContainerWidth;
    public static volatile int pdfContainerHeight;

    public static AtomicInteger nCurrentPageIndex = new AtomicInteger(0);
    public static int DOCUMENT_TOTAL_COUNT;
    public static volatile int MAIN_TITLE_HEIGHT = 0;
    public static volatile int PRENT_TITLE_HEIGHT = 0;
    public static String FILE_NAME = "";
}
