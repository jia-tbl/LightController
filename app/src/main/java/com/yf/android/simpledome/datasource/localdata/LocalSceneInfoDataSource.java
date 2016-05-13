package com.yf.android.simpledome.datasource.localdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;

import java.util.ArrayList;

public class LocalSceneInfoDataSource implements SceneInfoDataSource {
    private LocalDataSqlHelper helper;
    private SQLiteDatabase database;

    private static LocalSceneInfoDataSource instance = null;

    private LocalSceneInfoDataSource(Context context) {
        helper = new LocalDataSqlHelper(context);
        database = helper.getReadableDatabase();
    }

    public static LocalSceneInfoDataSource getInstance(Context context) {
        if (instance == null && context != null) {
            synchronized (LocalControllerDataSource.class) {
                if (instance == null) {
                    instance = new LocalSceneInfoDataSource(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void getSceneInfos(@NonNull LoadSceneInfosCallback callback) {
        ArrayList<SceneInfo> datas = new ArrayList<SceneInfo>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + LocalDataSqlHelper.TABLE_SCIN, null);

        if (cursor == null || cursor.getCount() == 0) {
            callback.onDataNotAvailable();
            return;
        }

        while (cursor.moveToNext()) {
            SceneInfo sceneInfo = new SceneInfo();
            sceneInfo.setId(cursor.getInt(cursor.getColumnIndex("ID")));

            sceneInfo.setCode(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.SCIN_COD))));
            sceneInfo.setName(cursor.getString(cursor.getColumnIndex(DataBaseColumn.SCIN_NAME)));
            sceneInfo.setIcon(cursor.getString(cursor.getColumnIndex(DataBaseColumn.SCIN_ICON)));


            datas.add(sceneInfo);
        }
        cursor.close();
        callback.onSceneInfosLoaded(datas);
    }

    @Override
    public void insertSceneInfo(@NonNull SceneInfo sceneInfo) {
        ContentValues value = new ContentValues();

        value.put(DataBaseColumn.SCIN_COD, sceneInfo.getCode());
        value.put(DataBaseColumn.SCIN_NAME, sceneInfo.getName());
        value.put(DataBaseColumn.SCIN_ICON, sceneInfo.getIcon());

        database.insert(LocalDataSqlHelper.TABLE_SCIN, null, value);
    }

    @Override
    public void refreshSceneInfoName(@NonNull SceneInfo sceneInfo) {
        ContentValues values = new ContentValues();
        values.put(DataBaseColumn.SCIN_NAME, sceneInfo.getName());

        String selection = DataBaseColumn.CONT_DATA + " LIKE ?";
        String[] selectionArgs = {sceneInfo.getId() + ""};

        database.update(LocalDataSqlHelper.TABLE_SCIN, values, selection, selectionArgs);
    }

    @Override
    public void refreshSceneInfoIcon(@NonNull SceneInfo sceneInfo) {
        ContentValues values = new ContentValues();
        values.put(DataBaseColumn.SCIN_NAME, sceneInfo.getIcon());

        String selection = DataBaseColumn.CONT_DATA + " LIKE ?";
        String[] selectionArgs = {sceneInfo.getId() + ""};

        database.update(LocalDataSqlHelper.TABLE_SCIN, values, selection, selectionArgs);
    }

    @Override
    public void deleteSceneInfo(@NonNull int sceneCod) {
        database.execSQL("DELETE FROM " + LocalDataSqlHelper.TABLE_SCIN
                + " WHERE " + DataBaseColumn.SCIN_COD + " = " + sceneCod);
    }

    @Override
    public void close() {
        if (database != null) {
            database.close();
        }
    }
}
