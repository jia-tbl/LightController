package com.yf.android.simpledome.choosepic;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllPicActivity extends AppCompatActivity {
    private GridView allPicGridView;
    private ImageAdapter adapter;
    private List<String> mImages;

    private ProgressDialog dialog;

    private LinearLayout bottomLayout;
    private TextView dirName;
    private TextView fileCount;
    private File fileDirName;
    private int dirCount;

    private List<FolderBean> allDirs = new ArrayList<FolderBean>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x110) {
                dialog.dismiss();
                data2View();
            }
        }
    };

    private void data2View() {
        if (fileDirName == null) {
            Toast.makeText(AllPicActivity.this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
            return;
        }
        mImages = Arrays.asList(fileDirName.list());
        adapter = new ImageAdapter(AllPicActivity.this, mImages, fileDirName.getAbsolutePath());
        allPicGridView.setAdapter(adapter);
        String path = fileDirName.getAbsolutePath();
        dirName.setText(path.substring(path.lastIndexOf("/") + 1));
        fileCount.setText(dirCount + "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_pic);
        initView();
        initEvents();
        initDatas();
    }

    private void initView() {
        allPicGridView = (GridView) findViewById(R.id.allPicGridView);
        bottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
        dirName = (TextView) findViewById(R.id.dirName);
        fileCount = (TextView) findViewById(R.id.fileCount);
    }

    private void initEvents() {
        bottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 扫描
     */
    private void initDatas() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(AllPicActivity.this, "当前存储卡不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        dialog = ProgressDialog.show(this, null, "正在加载....");
        new Thread() {
            @Override
            public void run() {
                Uri mUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = AllPicActivity.this.getContentResolver();
                Cursor cursor = cr.query(mUri, null, MediaStore.Images.Media.MIME_TYPE + " = ? or " +
                                MediaStore.Images.Media.MIME_TYPE + " = ? ",
                        new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

                Set<String> mDirPaths = new HashSet<String>();

                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    FolderBean folderBean = null;

                    String dirPath = parentFile.getAbsolutePath();
                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        folderBean = new FolderBean();
                        folderBean.setDir(dirPath);
                        folderBean.setFirstImagPath(path);
                    }
                    if (parentFile.list() == null) {
                        continue;
                    }
                    int fileSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")
                                    || filename.endsWith(".png")) {
                                return true;
                            }
                            return false;
                        }
                    }).length;
                    folderBean.setFileCount(fileSize);
                    allDirs.add(folderBean);

                    if (dirCount < fileSize) {
                        dirCount = fileSize;
                        fileDirName = parentFile;
                    }
                }
                cursor.close();
                handler.sendEmptyMessage(0x110);
            }
        }.start();
    }

}
