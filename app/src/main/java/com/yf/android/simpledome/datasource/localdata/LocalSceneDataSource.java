package com.yf.android.simpledome.datasource.localdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;

import java.util.ArrayList;
import java.util.List;

public class LocalSceneDataSource implements SceneDataSource {
    private LocalDataSqlHelper helper;
    private SQLiteDatabase database;

    private static LocalSceneDataSource instance = null;

    private LocalSceneDataSource(Context context) {
        helper = new LocalDataSqlHelper(context);
        database = helper.getReadableDatabase();
    }

    public static LocalSceneDataSource getInstance(Context context) {
        if (instance == null && context != null) {
            synchronized (LocalControllerDataSource.class) {
                if (instance == null) {
                    instance = new LocalSceneDataSource(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void getScenes(@NonNull LoadScenesCallback callback, int sceneCode) {
        List<Scene> data = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM "
                        + LocalDataSqlHelper.TABLE_SCEN + " WHERE "
                        + DataBaseColumn.SCIN_COD + "=" + sceneCode
                , null);

        if (cursor == null || cursor.getCount() == 0) {
            callback.onDataNotAvailable();
            return;
        }

        while (cursor.moveToNext()) {
            Scene scene = new Scene();

            scene.setId(cursor.getInt(cursor.getColumnIndex("ID")));

            scene.setCode(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.SCIN_COD))));
            scene.setContCode(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_COD)));
            scene.setData(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_DATA)));

            data.add(scene);
        }
        cursor.close();
        callback.onScenesLoaded(data);
    }

    @Override
    public void insertScene(@NonNull Scene scene) {
        ContentValues value = new ContentValues();

        value.put(DataBaseColumn.SCIN_COD, scene.getCode());
        value.put(DataBaseColumn.CONT_COD, scene.getContCode());
        value.put(DataBaseColumn.CONT_DATA, scene.getData());

        database.insert(LocalDataSqlHelper.TABLE_SCEN, null, value);
    }

    @Override
    public void refreshScene(@NonNull Scene scene) {
        ContentValues values = new ContentValues();
        values.put(DataBaseColumn.CONT_DATA, scene.getData());

        String selection = DataBaseColumn.CONT_DATA + " LIKE ?";
        String[] selectionArgs = {scene.getId() + ""};

        database.update(LocalDataSqlHelper.TABLE_SCEN, values, selection, selectionArgs);
    }

    @Override
    public void deleteScene(@NonNull int sceneId) {
        database.execSQL("DELETE FROM " + LocalDataSqlHelper.TABLE_SCEN + " WHERE ID = " + sceneId);
    }

    @Override
    public void deleteScene(int sceneCode, String contCode) {
        if (null != contCode) {
            database.execSQL("DELETE FROM " + LocalDataSqlHelper.TABLE_SCEN +
                    " WHERE " + DataBaseColumn.SCIN_COD + " = " + sceneCode + " AND " +
                    DataBaseColumn.CONT_COD + " = '" + contCode + "'");
        } else {
            database.execSQL("DELETE FROM " + LocalDataSqlHelper.TABLE_SCEN +
                    " WHERE " + DataBaseColumn.SCIN_COD + " = " + sceneCode);
        }
    }

    @Override
    public void close() {
        if (database != null) {
            database.close();
        }
    }

}
