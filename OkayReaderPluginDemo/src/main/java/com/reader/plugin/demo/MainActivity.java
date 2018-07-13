package com.reader.plugin.demo;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 读取手机sdcard页面
 *
 * @author cyd
 * @Description
 * @Date 2016年1月19日上午9:40:39
 */
public class MainActivity extends ListActivity {
    boolean isopen = false;
    private static final int MENU_EDIT_DELETE_FILE = 6;
    private static final int MENU_SHOW_DETAILS = 8;

    private static final int WARN_DELETE_FILE = 1;

    private static final int MSG_FILE_DELETE_SUCCESS = 0;
    private static final int MSG_FILE_DELETE_FAILED = 1;
    private static final int MSG_CANNOT_PICKFILE = 2;

    private static final String TAG = "MainActivity";
    private static final String m_rootDir = "/sdcard";
    public static final String TmpFilePath = "/sdcard/.hyf_temp";
    private List<String> mfilelist;
    private String mDir;
    private int mlongClickPositon = 0;

    private class FileSortUtil {
        private List<File> mfiles = null;
        private List<File> mdirs = null;

        public FileSortUtil(File[] files) {
            int i = 0, length = files.length;
            mfiles = new ArrayList<File>();
            mdirs = new ArrayList<File>();
            for (; i < length; i++) {
                if (files[i].isHidden())
                    continue;
                if (files[i].isDirectory())
                    mdirs.add(files[i]);
                else
                    mfiles.add(files[i]);
            }
        }

        private ArrayList<String> sortFileDirs() {
            FileSortByName[] fileWrappers = new FileSortByName[mdirs.size()];
            for (int i = 0; i < mdirs.size(); i++) {
                fileWrappers[i] = new FileSortByName(mdirs.get(i));
            }
            Arrays.sort(fileWrappers);
            ArrayList<String> sortedDirs = new ArrayList<String>();
            for (int i = 0; i < fileWrappers.length; i++) {
                sortedDirs.add(fileWrappers[i].file.getName());
            }
            return sortedDirs;
        }

        private ArrayList<String> sortFilesByName() {
            FileSortByName[] fileWrappers = new FileSortByName[mfiles.size()];
            for (int i = 0; i < mfiles.size(); i++) {
                fileWrappers[i] = new FileSortByName(mfiles.get(i));
            }
            Arrays.sort(fileWrappers);
            ArrayList<String> sortedFiles = new ArrayList<String>();
            for (int i = 0; i < fileWrappers.length; i++) {
                sortedFiles.add(fileWrappers[i].file.getName());
            }
            return sortedFiles;
        }

        public ArrayList<String> sortFileList() {
            ArrayList<String> sorted = new ArrayList<String>();
            ArrayList<String> dirs = null;
            ArrayList<String> files = null;
            dirs = sortFileDirs();
            files = sortFilesByName();

            // for(int i=dirs.size();i>0;i--){
            for (int i = 0; i < dirs.size(); i++) {
                sorted.add(dirs.get(i));
            }

            // for(int i=files.size();i>0;i--){
            for (int i = 0; i < files.size(); i++) {
                sorted.add(files.get(i));
            }

            return sorted;
        }

        class FileSortByName implements Comparable {
            public File file;

            public FileSortByName(File f) {
                file = f;
            }

            public int compareTo(Object obj) {
                // TODO Auto-generated method stub
                FileSortByName castObj = (FileSortByName) obj;
                if (this.file.getName().compareToIgnoreCase(castObj.file.getName()) > 0) {
                    return 1;
                } else if (this.file.getName().compareToIgnoreCase(castObj.file.getName()) < 0) {
                    return -1;
                } else
                    return 0;
            }
        }
    }

    private static class FileExploreAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<String> mfilelist;
        private Bitmap mIconDoc;
        private Bitmap mIconXls;
        private Bitmap mIconPdf;
        private Bitmap mIconPpt;
        private Bitmap mIconDir;
        private Bitmap mIconTxt;
        private Bitmap mIconPps;
        private Bitmap mIconEpub;
        private Bitmap mIconHtml;
        private Bitmap mIconChm;
        private Bitmap mIconImage;
        private Bitmap mIconHyf;

        public FileExploreAdapter(Context context, List<String> objects) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            mfilelist = objects;

            // Icons bound to the rows.
            mIconDoc = BitmapFactory.decodeResource(context.getResources(), R.drawable.doc);
            mIconXls = BitmapFactory.decodeResource(context.getResources(), R.drawable.excel);
            mIconPdf = BitmapFactory.decodeResource(context.getResources(), R.drawable.pdf);
            mIconPpt = BitmapFactory.decodeResource(context.getResources(), R.drawable.ppt);
            mIconDir = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
            mIconTxt = BitmapFactory.decodeResource(context.getResources(), R.drawable.txt);
            mIconPps = BitmapFactory.decodeResource(context.getResources(), R.drawable.pps);
            mIconEpub = BitmapFactory.decodeResource(context.getResources(), R.drawable.epub);
            mIconHtml = BitmapFactory.decodeResource(context.getResources(), R.drawable.html);
            mIconChm = BitmapFactory.decodeResource(context.getResources(), R.drawable.chm);
            mIconImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.image);
            mIconHyf = BitmapFactory.decodeResource(context.getResources(), R.drawable.hyf);

            // Icons bound to the rows.
        }

        public int getCount() {
            return mfilelist.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_main_file_row, parent, false);
                //
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(mfilelist.get(position));
            holder.icon.setImageBitmap(fileTypeIcon(mfilelist.get(position)));
            return convertView;
        }

        private Bitmap fileTypeIcon(String fileName) {
            if (fileName.toLowerCase().endsWith(".pdf"))
                return mIconPdf;
            else if (fileName.toLowerCase().endsWith(".ppt") || fileName.toLowerCase().endsWith(".pptx"))
                return mIconPpt;
            else if (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))
                return mIconXls;
            else if (fileName.toLowerCase().endsWith(".doc") || fileName.toLowerCase().endsWith(".docx"))
                return mIconDoc;
            else if (fileName.toLowerCase().endsWith(".txt"))
                return mIconTxt;
            else if (fileName.toLowerCase().endsWith(".pps"))
                return mIconPps;
            else if (fileName.toLowerCase().endsWith(".epub"))
                return mIconEpub;
            else if (fileName.toLowerCase().endsWith(".htm") || fileName.toLowerCase().endsWith(".html"))
                return mIconHtml;
            else if (fileName.toLowerCase().endsWith(".chm"))
                return mIconChm;
            else if (fileName.toLowerCase().endsWith(".hyf"))
                return mIconHyf;
            else if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".bmp")
                    || fileName.toLowerCase().endsWith(".gif"))
                return mIconImage;
            else
                return mIconDir;

        }

        static class ViewHolder {
            TextView text;
            ImageView icon;
        }
    }

    public static boolean isSDMounted() {
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState())) {
            return true;
        } else
            return false;
    }

    public static boolean isRO_SDMounted() {
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equalsIgnoreCase(Environment.getExternalStorageState())) {
            return true;
        } else
            return false;
    }

    private boolean isCanStartViewer() {
        final int MIN_SPACE = 200 * 1024;
        if (!isSDMounted()) {

            return false;
        }
        if (isRO_SDMounted()) {

            return false;
        }

        File f = new File(TmpFilePath);
        if (f.exists() && !f.isDirectory()) {

            return false;
        } else if (!f.exists() && !f.mkdir()) {

            return false;
        }
        return true;

    }

    Toast mtoast = null;

    void showMessage(int msgType) {

        switch (msgType) {
            case MSG_FILE_DELETE_SUCCESS:
                break;
            case MSG_FILE_DELETE_FAILED:
                if (mtoast != null)
                    mtoast.cancel();
                mtoast = Toast.makeText(this, "文件无法删除！", Toast.LENGTH_LONG);
                mtoast.show();
                break;
            case MSG_CANNOT_PICKFILE:
                if (mtoast != null)
                    mtoast.cancel();
                mtoast = Toast.makeText(this, "没有选择文件", Toast.LENGTH_LONG);
                mtoast.show();
                break;

        }
    }

    Context c = this;
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_DELETE_SUCCESS:
                    mfilelist = throughPath(mDir);
                    setListAdapter(new FileExploreAdapter(c, mfilelist));
                    break;
                case MSG_FILE_DELETE_FAILED:
                    showMessage(MSG_FILE_DELETE_FAILED);
                    break;
                case MSG_CANNOT_PICKFILE:
                    showMessage(MSG_CANNOT_PICKFILE);
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "HYF FileExplore =============== stop the activity...");

    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "HYF FileExplore =============== onResume the activity...");
        /*
		 * if(DemoConstant.isExitApp){ DemoConstant.isExitApp = false; finish(); }
		 */
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        boolean isStartFromReciver = getIntent().getBooleanExtra("isStartFromReciver", false);

        /**
         * if app start from reciver,then can not remove temp files because user
         * may be not pick file
         */

        mDir = getIntent().getStringExtra("current_dir");
        if (mDir == null)
            mDir = m_rootDir;
        else {
            File dirf = new File(mDir);
            if (!dirf.isDirectory())
                mDir = m_rootDir;
        }
        mfilelist = throughPath(mDir);
        if (mfilelist != null) {
            setListAdapter(new FileExploreAdapter(this, mfilelist));
        }

        registerForContextMenu(getListView());

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                mlongClickPositon = position;
                return false;
            }
        });

    }

    File[] laa;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        File f = new File(mDir + "/" + mfilelist.get(mlongClickPositon));
        if (!f.isDirectory())
            menu.add(0, MENU_EDIT_DELETE_FILE, 0, "删除");
        menu.add(0, MENU_SHOW_DETAILS, 1, "详情");
    }

    // 复制 assets下的xml到手机sdcard根目录下 cyd 2016-1-15
    public void copy() {
        try {
            InputStream inStream = this.getAssets().open("font-config.xml");
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "font-config.xml";
            OutputStream outStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int length = inStream.read(buffer);
            outStream.write(buffer, 0, length);
            outStream.flush();
            inStream.close();
            outStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private List<String> throughPath(String path) {

        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        File[] ls = f.listFiles(new DocViewerFileFilter());

        if (ls != null) {
            FileSortUtil sortUtil = new FileSortUtil(ls);
            return sortUtil.sortFileList();
        } else
            return null;// cyd 2016-1-15
    }

    private class DocViewerFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            if (pathname.isDirectory())
                return true;
            String fileName = pathname.getName().toLowerCase();
            if (fileName.endsWith(".pdf") || fileName.endsWith("doc") || fileName.endsWith("docx") || fileName.endsWith("txt") || fileName.endsWith("ppt") || fileName.endsWith("pptx")
                    || fileName.endsWith("xlsx") || fileName.endsWith("xls") || fileName.endsWith("htm") || fileName.endsWith("html") || fileName.endsWith("epub") || fileName.endsWith("chm")
                    || fileName.endsWith("gif") || fileName.endsWith("hyf") || fileName.endsWith("png") || fileName.endsWith("jpg") || fileName.endsWith("bmp")
                // 2009-12-03
                    )
                return true;
            return false;

        }
    }

    ProgressDialog pd = null;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        File f = new File(mDir + "/" + mfilelist.get(position));
        if (f.isDirectory()) {
            mDir = f.getAbsolutePath();
            mfilelist = throughPath(mDir);
            setListAdapter(new FileExploreAdapter(this, mfilelist));
        } else {
            String fileName = f.getAbsolutePath();
//                Intent i = new Intent(HYFFileExplore.this, MainActivity.class);
//                i.putExtra("filename", fileName);
//                i.putExtra("is_showing_in_main_screen", true);
//                startActivityForResult(i, 59);
            Log.d(TAG, "onListItemClick: fileName="+fileName);
            startMyActivity(this, fileName, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 59) {
            if (resultCode == 12) {
                Message msg = new Message();
                msg.what = MSG_FILE_DELETE_SUCCESS;
                mHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case MENU_EDIT_DELETE_FILE:
                showDialog(WARN_DELETE_FILE);
                break;
            case MENU_SHOW_DETAILS:
                onFileDetails();
                break;

            default:
                break;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case WARN_DELETE_FILE: {
            }
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackKeyDown();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean onBackKeyDown() {

        if (mDir.equals(m_rootDir)) {
            Intent i = getIntent();
            setResult(RESULT_CANCELED, i);
            finish();
        } else {
            File f = new File(mDir);
            mDir = f.getParent();
            mfilelist = throughPath(mDir);
            setListAdapter(new FileExploreAdapter(this, mfilelist));
        }
        return true;
    }

    protected boolean onFileDetails() {

        int selected = mlongClickPositon;

        if (selected < 0) {
            Message msg = new Message();
            msg.what = MSG_CANNOT_PICKFILE;
            mHandler.sendMessage(msg);
            return true;
        }
        String fileName = mDir + "/" + mfilelist.get(selected);
        Log.d(TAG, "onFileDetails: fileName="+fileName);
        startMyActivity(this, fileName, true);
        return true;

    }

    public static void startMyActivity(Context context, String filePath, boolean bShowInMainScreen) {
        Intent intent = new Intent(context,ReaderPluginTest.class);
        intent.setAction(Intent.ACTION_VIEW);
        //Uri的方式传递，自动加密了文字
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/pdf");
        context.startActivity(intent);
    }
}
