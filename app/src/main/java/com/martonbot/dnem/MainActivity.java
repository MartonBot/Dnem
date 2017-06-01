package com.martonbot.dnem;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.martonbot.dnem.Dnem.Schedule;
import com.martonbot.dnem.Dnem.TrackingLog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import java.util.Calendar;

public class MainActivity extends android.app.Activity {

    private ListView listView;
    private DnemActivityAdapter listAdapter;

    private DnemDbHelper dbHelper;
    private SQLiteDatabase db;

    private ImageButton addButton;
    private TextView dateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onFirstRun();

        setContentView(R.layout.activity_main);
        findControls();

        // create the database to read and write
        db = new DnemDbHelper(MainActivity.this).getWritableDatabase();
        dbHelper = new DnemDbHelper(MainActivity.this);

        listAdapter = new DnemActivityAdapter(MainActivity.this, null);
        listView.setAdapter(listAdapter);
        addButton.setOnClickListener(new OnAddButtonClickListener());
    }

    private void findControls() {
        listView = (ListView) findViewById(R.id.listView);
        addButton = (ImageButton) findViewById(R.id.add_button);
        dateText = (TextView) findViewById(R.id.date_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dateText.setText(Calendar.getInstance().getTime().toString());
        reloadList();
    }

    private void reloadList() {
        listAdapter.changeCursor(getActivities());
    }

    private Cursor getActivities() {
        String[] activitiesSelectionArgs = new String[] {
            "" + DateTime.now()
        };
        return dbHelper.getWritableDatabase().query(
                DnemDbHelper.activitiesJoinTable,
                DnemDbHelper.activitiesProjection,
                DnemDbHelper.activitiesSelection,
                activitiesSelectionArgs,
                DnemDbHelper.activitiesGroupBy,
                null,
                DnemDbHelper.activitiesOrderBy
        );
    }

    private class ConfirmUndoClickListener implements DialogInterface.OnClickListener {

        private long trackingLogId;

        public ConfirmUndoClickListener(long trackingLogId) {
            this.trackingLogId = trackingLogId;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    deleteTrackingLog(trackingLogId);
                    reloadList();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

    private void deleteTrackingLog(long trackingLogId) {
        String where = TrackingLog._ID + " =? ";
        String[] whereArgs = new String[]{
                "" + trackingLogId
        };
        db.delete(
                TrackingLog.T_NAME,
                where,
                whereArgs
        );
    }

    private void insertTrackingLog(long activityId) {
        Instant now = new Instant();
        ContentValues trackingLogValues = new ContentValues();
        trackingLogValues.put(TrackingLog.C_ACTIVITY_ID, activityId); // we set the foreign key
        trackingLogValues.put(TrackingLog.C_TIMESTAMP, now.getMillis()); // we set the current instant
        trackingLogValues.put(TrackingLog.C_UTC_DAY, new LocalDate(now).toString()); // we set the local date
        trackingLogValues.put(TrackingLog.C_TIMEZONE, DateTimeZone.getDefault().getID()); // we set the local timezone
        db.insertOrThrow(TrackingLog.T_NAME, null, trackingLogValues);
        reloadList();
    }

    private void onFirstRun() {
        // do stuff for first run here
        checkTimeZone();

        // flag as run
        SharedPreferences sp = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        boolean firstRun = !sp.getBoolean(Constants.ALREADY_RUN, false);
        if (firstRun) {
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean(Constants.ALREADY_RUN, true);
            e.apply();
        }
    }

    private void checkTimeZone() {
        SharedPreferences sp = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        String timeZoneId = DateTimeZone.getDefault().getID();
        SharedPreferences.Editor e = sp.edit();
        e.putString(Constants.DEFAULT_TIMEZONE_ID, timeZoneId);
        e.apply();
    }

    private class DnemActivityAdapter extends CursorAdapter {

        public DnemActivityAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // get the values from the cursor
            int idIndex = cursor.getColumnIndex(Activity._ID);
            int labelIndex = cursor.getColumnIndex(Activity.C_LABEL);
            int detailsIndex = cursor.getColumnIndex(Activity.C_DETAILS);
            int scheduledIndex = cursor.getColumnIndex(Schedule.C_IS_ACTIVE);
            int timestampIndex = cursor.getColumnIndex(TrackingLog.C_TIMESTAMP);
            int timezoneIndex = cursor.getColumnIndex(TrackingLog.C_TIMEZONE);

            long activityId = cursor.getLong(idIndex);

            // set them on the view elements
            TextView labelText = (TextView) view.findViewById(R.id.label_text);
            TextView detailsText = (TextView) view.findViewById(R.id.details_text);
            ImageButton doneButton = (ImageButton) view.findViewById(R.id.done_button);

            labelText.setText(cursor.getString(labelIndex));
            detailsText.setText(cursor.getString(detailsIndex));
            int backgroundDrawableId = cursor.getInt(scheduledIndex) > 0 ? R.drawable.background_item_rc_enabled : R.drawable.background_item_rc_disabled;
            view.setBackground(getResources().getDrawable(backgroundDrawableId, null));

            String timezoneId = cursor.getString(timezoneIndex);
            DateTimeZone timezone = DateTimeZone.forID(timezoneId);
            long timestamp = cursor.getLong(timestampIndex);
            boolean isDoneForToday = new LocalDate(timestamp, timezone).equals(LocalDate.now()); // same day?
            int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
            doneButton.setBackground(getResources().getDrawable(doneButtonBackgroundId, null));

            // set the listeners
            view.setOnClickListener(new OnActivityClickListener(activityId));
            doneButton.setOnClickListener(new OnDoneClickListener(doneButton, activityId));
        }
    }

    private class OnActivityClickListener implements View.OnClickListener {

        private long activityId;

        public OnActivityClickListener(long activityId) {
            this.activityId = activityId;
        }

        @Override
        public void onClick(View v) {
            Intent viewActivity = new Intent(MainActivity.this, ViewActivity.class);
            viewActivity.putExtra(Constants.EXTRA_ACTIVITY_ID, activityId);
            startActivity(viewActivity);
        }
    }

    private class OnAddButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
            startActivity(editActivity);
        }
    }



    private class OnDoneClickListener implements ImageButton.OnClickListener {

        private long activityId;
        private ImageButton doneButton;

        public OnDoneClickListener(ImageButton doneButton, long activityId) {
            this.activityId = activityId;
            this.doneButton = doneButton;
        }

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
                AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
                ConfirmUndoClickListener confirmUndoClickListener = new ConfirmUndoClickListener(trackingLogId);
                adBuilder.setMessage("Undo for today?").setPositiveButton("Yup", confirmUndoClickListener).setNegativeButton("Nope", confirmUndoClickListener).show();
            } else {
                // insert the tracking log
                insertTrackingLog(activityId);
            }
            tlCursor.close();
        }
    }

}
