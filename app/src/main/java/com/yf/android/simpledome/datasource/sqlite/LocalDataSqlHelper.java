package com.yf.android.simpledome.datasource.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDataSqlHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "yfappdb.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_CONT = "CONT";
    public static final String TABLE_SCIN = "SCIN";
    public static final String TABLE_SCEN = "SCEN";

    public LocalDataSqlHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql1 = "create table " + TABLE_CONT
                + "(ID INTEGER primary key autoincrement," + DataBaseColumn.CONT_NAME + " TEXT, "
                + DataBaseColumn.CONT_DATA + " TEXT, " + DataBaseColumn.CONT_COD + " TEXT, "
                + DataBaseColumn.CONT_BA + " TEXT, " + DataBaseColumn.CONT_BB + " TEXT, "
                + DataBaseColumn.CONT_BC + " TEXT, " + DataBaseColumn.CONT_BD + " TEXT, "
                + DataBaseColumn.CONT_BAS + " TEXT, " + DataBaseColumn.CONT_BBS + " TEXT, "
                + DataBaseColumn.CONT_BCS + " TEXT, " + DataBaseColumn.CONT_BDS + " TEXT, "
                + DataBaseColumn.CONT_TYPE + " TEXT, " + DataBaseColumn.CONT_OPT + " TEXT, "
                + DataBaseColumn.CONT_ON + " TEXT)";
        db.execSQL(sql1);

        String sql2 = "create table " + TABLE_SCIN
                + "(ID INTEGER primary key autoincrement," + DataBaseColumn.SCIN_COD + " TEXT, "
                + DataBaseColumn.SCIN_ICON + " TEXT, " + DataBaseColumn.SCIN_NAME + " TEXT)";
        db.execSQL(sql2);

        String sql3 = "create table " + TABLE_SCEN
                + "(ID INTEGER primary key autoincrement," + DataBaseColumn.SCIN_COD + " TEXT, "
                + DataBaseColumn.CONT_COD + " TEXT, " + DataBaseColumn.CONT_DATA + " TEXT)";
        db.execSQL(sql3);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
