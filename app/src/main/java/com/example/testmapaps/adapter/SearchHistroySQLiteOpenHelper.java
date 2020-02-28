package com.example.testmapaps.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchHistroySQLiteOpenHelper extends SQLiteOpenHelper {
    //数据库基础信息
    private static String name = "search.db";
    private static Integer version = 1;
    public SearchHistroySQLiteOpenHelper(Context context) {
        super(context, name, null, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
       db.execSQL("create table history(id integer primary key autoincrement,name varchar(200))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
