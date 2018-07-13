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

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;


public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final String[][] MIME_MapTable = {
            //{后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    /**
     * 获取当前的pdf的路径
     *
     * @return
     */
    public static boolean isFileExist(String filePath) {
        Constant.isShangXia = true;
        Constant.strPdfFilePath = "";
        String mFilePath = "";
        File pdfFile = null;

        if (LangUtils.isNotEmpty(filePath) && filePath.toLowerCase().endsWith(".pdf")) {
            mFilePath = filePath;
            pdfFile = new File(filePath);
        } else {
            return false;
        }

        //PDF not exist
        if (pdfFile == null || !pdfFile.exists()) {
            //1.pdf and office url not exit
            return false;
        }

        Constant.isShangXia = SharedPreferencesUtil.getInstance().getBoolean(Constant.READ_MODE, true);
        Constant.strPdfFilePath = mFilePath;
        return true;
    }

    /**
     * 根据路径获取文件名
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath){
        if (TextUtils.isEmpty(filePath)){
            return null;
        }
        String title;
        try {
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));

            if (LangUtils.isNotEmpty(fileName)) {
                title = fileName;
            } else {
                title = filePath;
            }
        } catch (Exception e) {
            title = filePath;
        }
        return title;
    }

    public static Intent createWpsIntent(String filePath) {
        Intent intent = new Intent();
        intent.setPackage("cn.wps.moffice_eng");
        intent.setAction(Intent.ACTION_VIEW);
        //            String type = getMIMEType(filePath);
        //            intent.setDataAndType(/*uri*/Uri.fromFile(new File(filePath)), type);
        intent.setData(Uri.fromFile(new File(filePath)));
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    public static Intent createIntent(String filePath) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMIMEType(filePath);
        intent.setDataAndType(/*uri*/Uri.fromFile(new File(filePath)), type);
        //需try catch
        return intent;
    }

    public static String getMIMEType(String filePath) {
        String type = "*/*";
        int dotIndex = filePath.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        String end = filePath.substring(dotIndex, filePath.length()).toLowerCase();  /* 获取文件的后缀名*/

        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }
}