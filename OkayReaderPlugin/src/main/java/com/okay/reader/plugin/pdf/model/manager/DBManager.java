package com.okay.reader.plugin.pdf.model.manager;

import android.content.Context;
import android.text.TextUtils;

import com.okay.reader.plugin.pdf.model.entity.DaoMaster;
import com.okay.reader.plugin.pdf.model.entity.DaoSession;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 数据库辅助类
 */

public class DBManager {
    private static final String TAG = DBManager.class.getSimpleName();
    private static DBManager mInstance;
    private DaoMaster.DevOpenHelper mOpenHelper;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private String password = "123456";
    private static final String DBName = "PdfProgress";

    private DBManager() {
    }

    public static DBManager getInstance() {
        if (mInstance == null) {
            mInstance = new DBManager();
        }
        return mInstance;
    }

    public void init(Context context) {
        mOpenHelper = new DaoMaster.DevOpenHelper(context, DBName, null);
        mDaoMaster = new DaoMaster(mOpenHelper.getWritableDb());//getEncryptedWritableDb(getMd5(password)));
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }



    public static String getMd5(String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
