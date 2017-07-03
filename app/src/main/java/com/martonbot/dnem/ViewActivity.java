package com.martonbot.dnem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.martonbot.dnem.Dnem.Activity;
import com.martonbot.dnem.Dnem.TrackingLog;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

public class ViewActivity extends android.app.Activity {

    private ImageButton doneButton;
    private ImageButton editButton;
    private TextView labelText;
    private TextView detailsText;
    private TextView streakText;
    private ListView trackinLogsLists;

    private DnemDbHelper dbHelper;
    private SQLiteDatabase db;
    private long activityId;

    private TrackingLogAdapter trackingLogAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityId = getIntent().getLongExtra("EXTRA_ACTIVITY_ID", 0);
        if (activityId == 0) {
            throw new IllegalStateException("A valid activity ID must be passed");
        }
        setContentView(R.layout.activity_view);

        db = new DnemDbHelper(ViewActivity.this).getWritableDatabase();
        dbHelper = new DnemDbHelper(ViewActivity.this);

        findControls();

        editButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent editActivity = new Intent(ViewActivity.this, EditActivity.class);
                editActivity.putExtra(Constants.EXTRA_ACTIVITY_ID, activityId);
                startActivity(editActivity);
            }
        });

        trackingLogAdapter = new TrackingLogAdapter(ViewActivity.this, null);
        trackinLogsLists.setAdapter(trackingLogAdapter);

        doneButton.setOnClickListener(new OnDoneClickListener());
    }

    private void findControls() {
        labelText = (TextView) findViewById(R.id.label_text);
        detailsText = (TextView) findViewById(R.id.details_text);
        streakText = (TextView) findViewById(R.id.streak_text);
        doneButton = (ImageButton) findViewById(R.id.done_button);
        editButton = (ImageButton) findViewById(R.id.edit_button);
        trackinLogsLists = (ListView) findViewById(R.id.tracking_logs_list);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadActivityInfo();
        trackingLogAdapter.changeCursor(getTrackingLogs());
        loadTrackingLogsInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        trackingLogAdapter.changeCursor(null);
    }

    private void loadActivityInfo() {

        String[] queryProjection = {
                Activity.T_NAME + "." + Activity._ID,
                Activity.C_LABEL,
                Activity.C_DETAILS
        };

        String querySelection = Activity.T_NAME + "." + Activity._ID + " = ?";
        String[] querySelectionArgs = {
                "" + activityId
        };

        Cursor activityCursor = dbHelper.getWritableDatabase().query(
                DnemDbHelper.activitiesJoinTable,
                DnemDbHelper.activitiesProjection,
                querySelection,
                querySelectionArgs,
                DnemDbHelper.activitiesGroupBy,
                null,
                null
        );

        if (activityCursor.getCount() != 1) {
            throw new IllegalStateException("There should be one and only one result.");
        }

        if (activityCursor.moveToNext()) {
            int idIndex = activityCursor.getColumnIndex(Activity._ID);
            int labelIndex = activityCursor.getColumnIndex(Activity.C_LABEL);
            int detailsIndex = activityCursor.getColumnIndex(Activity.C_DETAILS);
            int timezoneIndex = activityCursor.getColumnIndex(TrackingLog.C_TIMEZONE);
            int timestampIndex = activityCursor.getColumnIndex(TrackingLog.C_TIMESTAMP);

            String label = activityCursor.getString(labelIndex);
            String details = activityCursor.getString(detailsIndex);

            labelText.setText(label);
            detailsText.setText(details);

            String timezoneId = activityCursor.getString(timezoneIndex);
            DateTimeZone timezone = DateTimeZone.forID(timezoneId);
            long timestamp = activityCursor.getLong(timestampIndex);
            boolean isDoneForToday = new LocalDate(timestamp, timezone).equals(LocalDate.now()); // same day?
            int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
            doneButton.setBackground(getResources().getDrawable(doneButtonBackgroundId, null));
        }

        activityCursor.close();
    }

    private void loadTrackingLogsInfo() {

        Cursor tlCursor = getTrackingLogs();

        int currentStreak = 0;

        long todayStamp = new Instant().getMillis();
        DateTimeZone todayZone = DateTimeZone.getDefault();

        // loop through the tracking logs to compute the streak
        while (tlCursor.moveToNext()) {
            int idIndex = tlCursor.getColumnIndex(TrackingLog._ID);
            int dayIndex = tlCursor.getColumnIndex(TrackingLog.C_UTC_DAY);
            int timestampIndex = tlCursor.getColumnIndex(TrackingLog.C_TIMESTAMP);
            int timezoneIndex = tlCursor.getColumnIndex(TrackingLog.C_TIMEZONE);

            long yesterdayStamp = tlCursor.getLong(timestampIndex);
            DateTimeZone yesterdayZone = DateTimeZone.forID(tlCursor.getString(timezoneIndex));

            if (Time.keepStreak(todayStamp, todayZone, yesterdayStamp, yesterdayZone)) {
                currentStreak++;
            } else {
                break;
            }
            // move down one tracking log
            todayStamp = yesterdayStamp;
            todayZone = yesterdayZone;
        }

        String streakString = currentStreak + " days";
        streakText.setText(streakString);

        tlCursor.close();
    }

    private class TrackingLogAdapter extends CursorAdapter {

        public TrackingLogAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(ViewActivity.this).inflate(R.layout.tracking_log_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int dateIndex = cursor.getColumnIndex(TrackingLog.C_UTC_DAY);
            int timestampIndex = cursor.getColumnIndex(TrackingLog.C_TIMESTAMP);
            int timezoneIndex = cursor.getColumnIndex(TrackingLog.C_TIMEZONE);

            TextView dayText = (TextView) view.findViewById(R.id.date_text);
            TextView timestampText = (TextView) view.findViewById(R.id.timestamp_text);
            TextView timezoneText = (TextView) view.findViewById(R.id.timezone_text);

            String date = cursor.getString(dateIndex);
            String timezoneId = cursor.getString(timezoneIndex);
            String timestamp = "" + cursor.getLong(timestampIndex);

            dayText.setText(date);
            timestampText.setText(timestamp);
            timezoneText.setText(timezoneId);
        }
    }

    private Cursor getTrackingLogs() {
        String[] queryProjection = {
                TrackingLog._ID,
                TrackingLog.C_UTC_DAY,
                TrackingLog.C_TIMESTAMP,
                TrackingLog.C_TIMEZONE
        };

        String querySelection = TrackingLog.T_NAME + "." + TrackingLog.C_ACTIVITY_ID + " = ?";
        String[] querySelectionArgs = {
                "" + activityId
        };

        String orderBy = TrackingLog.C_TIMESTAMP + " DESC";

        return dbHelper.getWritableDatabase().query(
                TrackingLog.T_NAME,
                queryProjection,
                querySelection,
                querySelectionArgs,
                null,
                null,
                orderBy
        );
    }

    // TODO refactor with listener from MainActivity
    private class OnDoneClickListener implements ImageButton.OnClickListener {

        @Override
        public void onClick(View v) {
            // is there a tracking log for the current day?
            String[] trackingLogProjection = {
                    TrackingLog._ID,
                    TrackingLog.C_TIMESTAMP,
                    TrackingLog.C_UTC_DAY,
                    TrackingLog.C_TIMEZONE
            };

            String trackingLogSelection = TrackingLog.C_ACTIVITY_ID + " = ?" +
                    "AND " + TrackingLog.C_UTC_DAY + " = ?" +
                    "AND " + TrackingLog.C_TIMEZONE + " = ?";
            String[] trackingLogSelectionArgs = {
                    "" + activityId,
                    new LocalDate().toString(),
                    DateTimeZone.getDefault().getID() // we want to find a tracking log for the current timezone
            };

            String groupBy = TrackingLog.C_ACTIVITY_ID;

            String orderBy = TrackingLog.C_TIMESTAMP + " DESC";

            Cursor tlCursor = db.query(
                    TrackingLog.T_NAME,
                    trackingLogProjection,
                    trackingLogSelection,
                    trackingLogSelectionArgs,
                    groupBy,
                    null,
                    orderBy
            );

            // if there is, pop a confirmation dialog for undoing
            if (tlCursor.moveToNext()) {
                long trackingLogId = tlCursor.getLong(tlCursor.getColumnIndex(TrackingLog._ID));
                AlertDialog.Builder adBuilder = new AlertDialog.Builder(ViewActivity.this);
                ConfirmUndoClickListener confirmUndoClickListener = new ConfirmUndoClickListener(trackingLogId);
                adBuilder.setMessage("Undo for today?").setPositiveButton("Yup", confirmUndoClickListener).setNegativeButton("Nope", confirmUndoClickListener).show();
            } else {
                // insert the tracking log
                TrackingLogs.insert(db, activityId);
                onResume();
            }
            tlCursor.close();
        }
    }

    // TODO refactor with duplicated code in mainActivity
    private class ConfirmUndoClickListener implements DialogInterface.OnClickListener {

        private long trackingLogId;

        public ConfirmUndoClickListener(long trackingLogId) {
            this.trackingLogId = trackingLogId;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    TrackingLogs.delete(db, trackingLogId);
                    onResume();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
