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
package com.reader.plugin.demo;

import android.text.TextUtils;


public class FileUtils {
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

            if (isNotEmpty(fileName)) {
                title = fileName;
            } else {
                title = filePath;
            }
        } catch (Exception e) {
            title = filePath;
        }
        return title;
    }

    /**
     * Check if a CharSequence is not empty.
     *
     * @param s
     * @return boolean
     */
    public static boolean isNotEmpty(CharSequence s) {
        return s != null && s.length() > 0;
    }
}