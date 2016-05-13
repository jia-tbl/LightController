package com.yf.android.simpledome.datasource.localdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;

import java.util.ArrayList;

public class LocalControllerDataSource implements ControllerDataSource {
    private LocalDataSqlHelper helper;
    private SQLiteDatabase database;

    private static LocalControllerDataSource instance = null;

    private LocalControllerDataSource(Context context) {
        helper = new LocalDataSqlHelper(context);
        database = helper.getReadableDatabase();
    }

    public static LocalControllerDataSource getInstance(Context context) {
        if (instance == null && context != null) {
            synchronized (LocalControllerDataSource.class) {
                if (instance == null) {
                    instance = new LocalControllerDataSource(context);
                }
            }
        }
        return instance;
    }

    public void updateControllerName(int id, String name) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET CONT_NAME='" + name + "' WHERE ID = " + id);
    }

    public void updateBtn1Name(int id, String name) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BA + "='" + name + "' WHERE ID = " + id);
    }

    public void updateBtn2Name(int id, String name) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BB + "='" + name + "' WHERE ID = " + id);
    }

    public void updateBtn3Name(int id, String name) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BC + "='" + name + "' WHERE ID = " + id);
    }

    public void updateBtn4Name(int id, String name) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BD + "='" + name + "' WHERE ID = " + id);
    }

    public void updateBtn1Scene(int id, int sceneCode) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BAS + "='" + sceneCode + "' WHERE ID = " + id);
    }

    public void updateBtn2Scene(int id, int sceneCode) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BBS + "='" + sceneCode + "' WHERE ID = " + id);
    }

    public void updateBtn3Scene(int id, int sceneCode) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BCS + "='" + sceneCode + "' WHERE ID = " + id);
    }

    public void updateController(String contCode, String column, String value) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + column + "='" + value + "' WHERE " + DataBaseColumn.CONT_COD + " = '" + contCode + "'");
    }

    public void updateBtn4Scene(int id, int sceneCode) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_BDS + "='" + sceneCode + "' WHERE ID = " + id);
    }

    public void updateContOnline(int id, int online) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_ON + "='" + online + "' WHERE ID = " + id);
    }

    public void updateContType(int id, int type) {
        database.execSQL("UPDATE " + LocalDataSqlHelper.TABLE_CONT
                + " SET " + DataBaseColumn.CONT_TYPE + "='" + type + "' WHERE ID = " + id);
    }

    public void deleteTable() {
        database.execSQL("DELETE FROM " + LocalDataSqlHelper.TABLE_CONT);
        database.execSQL("DELETE FROM " + LocalDataSqlHelper.TABLE_SCEN);
        database.execSQL("DELETE FROM " + LocalDataSqlHelper.TABLE_SCIN);
    }

    @Override
    public void getControllers(@NonNull LoadControllersCallback callback) {
        ArrayList<Controller> datas = new ArrayList<Controller>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + LocalDataSqlHelper.TABLE_CONT, null);

        if (cursor == null || cursor.getCount() == 0) {
            callback.onDataNotAvailable();
            return;
        }

        while (cursor.moveToNext()) {
            Controller controller = new Controller();
            controller.setId(cursor.getInt(cursor.getColumnIndex("ID")));

            controller.setContName(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_NAME)));
            controller.setContCode(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_COD)));
            controller.setContData(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_DATA)));

            controller.setBtnName1(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BA)));
            controller.setBtnName2(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BB)));
            controller.setBtnName3(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BC)));
            controller.setBtnName4(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BD)));

            controller.setBtnScene1(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BAS))));
            controller.setBtnScene2(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BBS))));
            controller.setBtnScene3(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BCS))));
            controller.setBtnScene4(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_BDS))));

            controller.setContType(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_TYPE))));
            controller.setContOnline(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_ON))));

            controller.setOptData(cursor.getString(cursor.getColumnIndex(DataBaseColumn.CONT_OPT)));

            datas.add(controller);
        }
        cursor.close();
        callback.onControllersLoaded(datas);
    }

    @Override
    public void insertController(@NonNull Controller controller) {
        ContentValues value = new ContentValues();

        value.put(DataBaseColumn.CONT_NAME, controller.getContName());
        value.put(DataBaseColumn.CONT_COD, controller.getContCode());
        value.put(DataBaseColumn.CONT_DATA, controller.getContData());

        value.put(DataBaseColumn.CONT_BA, controller.getBtnName1());
        value.put(DataBaseColumn.CONT_BB, controller.getBtnName2());
        value.put(DataBaseColumn.CONT_BC, controller.getBtnName3());
        value.put(DataBaseColumn.CONT_BD, controller.getBtnName4());

        value.put(DataBaseColumn.CONT_BAS, controller.getBtnScene1());
        value.put(DataBaseColumn.CONT_BBS, controller.getBtnScene2());
        value.put(DataBaseColumn.CONT_BCS, controller.getBtnScene3());
        value.put(DataBaseColumn.CONT_BDS, controller.getBtnScene4());

        value.put(DataBaseColumn.CONT_TYPE, controller.getContType());
        value.put(DataBaseColumn.CONT_ON, controller.getContOnline());

        value.put(DataBaseColumn.CONT_OPT, controller.getOptData());

        database.insert(LocalDataSqlHelper.TABLE_CONT, null, value);
    }


    @Override
    public void operateController(@NonNull Controller controller) {
        ContentValues values = new ContentValues();
        values.put(DataBaseColumn.CONT_DATA, controller.getContData());

        String selection = DataBaseColumn.CONT_DATA + " LIKE ?";
        String[] selectionArgs = {controller.getId() + ""};

        database.update(LocalDataSqlHelper.TABLE_CONT, values, selection, selectionArgs);
    }

    @Override
    public void deleteController(@NonNull String contCode) {
        String sql = "DELETE FROM " + LocalDataSqlHelper.TABLE_CONT
                + " WHERE " + DataBaseColumn.CONT_COD + " = '" + contCode + "'";
        Log.e("SQL", "-------------deleteController: " + sql);
        database.execSQL(sql);
    }

    @Override
    public void close() {
        if (database != null) {
            database.close();
        }
    }
}
