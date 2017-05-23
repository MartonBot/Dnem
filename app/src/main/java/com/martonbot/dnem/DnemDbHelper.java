package com.martonbot.dnem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mgrihangne on 5/05/2017.
 */
public class DnemDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Dnem.db";

    public DnemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Dnem.SQL_CREATE_ACTIVITY);
        db.execSQL(Dnem.SQL_CREATE_SCHEDULE);
        db.execSQL(Dnem.SQL_CREATE_TRACKING_LOG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(Dnem.SQL_DELETE_ACTIVITY);
        db.execSQL(Dnem.SQL_DELETE_SCHEDULE);
        db.execSQL(Dnem.SQL_DELETE_TRACKING_LOG);
        onCreate(db);
    }

}
