package com.martonbot.dnem;

import android.provider.BaseColumns;

public class DnemContract {

    private DnemContract() {
    }

    public static class Activity implements BaseColumns {
        public static final String TABLE_NAME = "activity";
        public static final String COLUMN_NAME_LABEL = "label";
        public static final String COLUMN_NAME_ICON = "icon";
        public static final String COLUMN_NAME_DETAILS = "details";
    }

    public static class Schedule implements BaseColumns {
        public static final String TABLE_NAME = "schedule";
        public static final String COLUMN_NAME_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_NAME_IS_DAILY = "is_daily"; // mmh
        public static final String COLUMN_NAME_IS_ACTIVE = "is_active";
    }

    public static class TrackingLog implements BaseColumns {
        public static final String TABLE_NAME = "tracking_log";
        public static final String COLUMN_NAME_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_IS_DONE = "is_done";
    }

    static final String SQL_CREATE_ACTIVITY =
            "CREATE TABLE " + Activity.TABLE_NAME + " (" +
                    Activity._ID + " INTEGER PRIMARY KEY," +
                    Activity.COLUMN_NAME_LABEL + " TEXT," +
                    Activity.COLUMN_NAME_ICON + " TEXT," +
                    Activity.COLUMN_NAME_DETAILS + " TEXT)";

    static final String SQL_CREATE_SCHEDULE =
            "CREATE TABLE " + Schedule.TABLE_NAME + " (" +
                    Schedule._ID + " INTEGER PRIMARY KEY," +
                    Schedule.COLUMN_NAME_ACTIVITY_ID + " INTEGER," +
                    Schedule.COLUMN_NAME_IS_DAILY + " BOOLEAN," +
                    Schedule.COLUMN_NAME_IS_ACTIVE + " BOOLEAN," +
                    " FOREIGN KEY(" + Schedule.COLUMN_NAME_ACTIVITY_ID + ") REFERENCES " + Activity.TABLE_NAME + "(" + Activity._ID + "))";

    static final String SQL_CREATE_TRACKING_LOG =
            "CREATE TABLE " + TrackingLog.TABLE_NAME + " (" +
                    TrackingLog._ID + " INTEGER PRIMARY KEY," +
                    TrackingLog.COLUMN_NAME_ACTIVITY_ID + " INTEGER," +
                    TrackingLog.COLUMN_NAME_DATE + " DATE," +
                    TrackingLog.COLUMN_NAME_IS_DONE + " BOOLEAN," +
                    " FOREIGN KEY(" + TrackingLog.COLUMN_NAME_ACTIVITY_ID + ") REFERENCES " + Activity.TABLE_NAME + "(" + Activity._ID + "))";

    static final String SQL_DELETE_ACTIVITY =
            "DROP TABLE IF EXISTS " + Activity.TABLE_NAME;

    static final String SQL_DELETE_SCHEDULE =
            "DROP TABLE IF EXISTS " + Schedule.TABLE_NAME;

    static final String SQL_DELETE_TRACKING_LOG =
            "DROP TABLE IF EXISTS " + TrackingLog.TABLE_NAME;

}
