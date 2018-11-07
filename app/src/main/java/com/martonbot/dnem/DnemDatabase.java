package com.martonbot.dnem;

import android.provider.BaseColumns;

public class DnemDatabase {

    private DnemDatabase() {
    }

    public static class Activity implements BaseColumns {
        public static final String T_NAME = "activity";
        public static final String C_LABEL = "label";
        public static final String C_ICON = "icon";
        public static final String C_DETAILS = "details";
    }

    public static class Schedule implements BaseColumns {
        public static final String T_NAME = "schedule";
        public static final String C_ACTIVITY_ID = "activity_id";
        public static final String C_IS_DAILY = "is_daily";
        public static final String C_IS_ACTIVE = "is_active";
        public static final String C_ALLOW_STARS = "allow_stars";
        public static final String C_WEEKENDS_ON = "weekends_on";
    }

    public static class TrackingLog implements BaseColumns {
        public static final String T_NAME = "tracking_log";
        public static final String C_ACTIVITY_ID = "activity_id";
        public static final String C_UTC_DAY = "utc_day";
        public static final String C_TIMEZONE = "timezone";
        public static final String C_TIMESTAMP = "timestamp";
        public static final String I_ACTIVITY_TIMESTAMP = "index_activity_timestamp";
    }

    static final String SQL_CREATE_ACTIVITY =
            "CREATE TABLE " + Activity.T_NAME + " (" +
                    Activity._ID + " INTEGER PRIMARY KEY," +
                    Activity.C_LABEL + " TEXT," +
                    Activity.C_ICON + " TEXT," +
                    Activity.C_DETAILS + " TEXT)";

    static final String SQL_CREATE_SCHEDULE =
            "CREATE TABLE " + Schedule.T_NAME + " (" +
                    Schedule._ID + " INTEGER PRIMARY KEY," +
                    Schedule.C_ACTIVITY_ID + " INTEGER," +
                    Schedule.C_IS_DAILY + " BOOLEAN," +
                    Schedule.C_IS_ACTIVE + " BOOLEAN," +
                    Schedule.C_ALLOW_STARS + " BOOLEAN," +
                    Schedule.C_WEEKENDS_ON + " BOOLEAN," +
                    " FOREIGN KEY(" + Schedule.C_ACTIVITY_ID + ") REFERENCES " + Activity.T_NAME + "(" + Activity._ID + ")" +
                    " ON DELETE CASCADE)";

    static final String SQL_CREATE_TRACKING_LOG =
            "CREATE TABLE " + TrackingLog.T_NAME + " (" +
                    TrackingLog._ID + " INTEGER PRIMARY KEY," +
                    TrackingLog.C_ACTIVITY_ID + " INTEGER," +
                    TrackingLog.C_UTC_DAY + " TEXT," +
                    TrackingLog.C_TIMEZONE + " TEXT," +
                    TrackingLog.C_TIMESTAMP + " INTEGER," +
                    " FOREIGN KEY(" + TrackingLog.C_ACTIVITY_ID + ") REFERENCES " + Activity.T_NAME + "(" + Activity._ID + ")" +
                    " ON DELETE CASCADE" +
                    " UNIQUE (" + TrackingLog.C_ACTIVITY_ID + ", " + TrackingLog.C_UTC_DAY + ", " + TrackingLog.C_TIMEZONE + "))";

    static final String SQL_CREATE_INDEX_TRACKING_LOG =
            "CREATE UNIQUE INDEX " + TrackingLog.I_ACTIVITY_TIMESTAMP + " ON " + TrackingLog.T_NAME + "(" + TrackingLog.C_ACTIVITY_ID + ", " + TrackingLog.C_TIMESTAMP + ")";

    static final String SQL_DELETE_ACTIVITY =
            "DROP TABLE IF EXISTS " + Activity.T_NAME;

    static final String SQL_DELETE_SCHEDULE =
            "DROP TABLE IF EXISTS " + Schedule.T_NAME;

    static final String SQL_DELETE_TRACKING_LOG =
            "DROP TABLE IF EXISTS " + TrackingLog.T_NAME;

}
