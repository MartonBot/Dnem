package com.martonbot.dnem;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private DnemActivityAdapter listAdapter;

    private Cursor cursor;
    private DnemDbHelper dbHelper;
    private SQLiteDatabase db;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the database to read and write
        db = new DnemDbHelper(MainActivity.this).getWritableDatabase();
        dbHelper = new DnemDbHelper(MainActivity.this);
        calendar = Calendar.getInstance();

        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new DnemActivityAdapter(MainActivity.this, cursor);
        listView.setAdapter(listAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
                startActivity(editActivity);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadList();
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
            int dateIndex = cursor.getColumnIndex(TrackingLog.C_DATE);

            long activityId = cursor.getLong(idIndex);

            // set them on the view elements
            TextView labelText = (TextView) view.findViewById(R.id.label_text);
            TextView detailsText = (TextView) view.findViewById(R.id.details_text);
            ImageButton doneButton = (ImageButton) view.findViewById(R.id.done_button);

            String label = cursor.getString(labelIndex);
            labelText.setText(label);
            detailsText.setText(cursor.getString(detailsIndex));
            int backgroundDrawableId = cursor.getInt(scheduledIndex) > 0 ? R.drawable.background_item_rc_enabled : R.drawable.background_item_rc_disabled;
            view.setBackground(getResources().getDrawable(backgroundDrawableId, null));

            long timestamp = cursor.getLong(dateIndex);
            boolean isDoneForToday = timestamp >= getMidnight();
            int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
            doneButton.setBackground(getResources().getDrawable(doneButtonBackgroundId, null));

            // set the listeners
            view.setOnClickListener(new OnActivityClickListener(activityId));
            doneButton.setOnClickListener(new OnDoneClickListener(doneButton, activityId));
            doneButton.setEnabled(true);
        }
    }

    private class OnActivityClickListener implements View.OnClickListener {

        private long activityId;

        public OnActivityClickListener(long activityId) {
            this.activityId = activityId;
        }

        @Override
        public void onClick(View v) {
            Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
            editActivity.putExtra(Constants.EXTRA_ACTIVITY_ID, activityId);
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
            // first disable the button
            doneButton.setEnabled(false);

            // is there a tracking log for the current day?
            String[] trackingLogProjection = {
                    TrackingLog._ID,
                    TrackingLog.C_DATE
            };

            String trackingLogSelection = TrackingLog.C_ACTIVITY_ID + " = ? AND " + TrackingLog.C_DATE + " >= ?";
            String[] trackingLogSelectionArgs = {
                    "" + activityId,
                    "" + getMidnight()
            };

            String groupBy = TrackingLog.C_ACTIVITY_ID;

            String orderBy = TrackingLog.C_DATE + " DESC";

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

    private long getMidnight() {
        Calendar midnight = new GregorianCalendar();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        return midnight.getTimeInMillis();
    }

    private void reloadList() {
        String joinTable = Activity.T_NAME
                + " JOIN "
                + Schedule.T_NAME
                + " ON " + Activity.T_NAME + "." + Activity._ID
                + " = " + Schedule.T_NAME + "." + Schedule.C_ACTIVITY_ID
                + " LEFT OUTER JOIN "
                + TrackingLog.T_NAME
                + " ON " + Activity.T_NAME + "." + Activity._ID
                + " = " + TrackingLog.T_NAME + "." + TrackingLog.C_ACTIVITY_ID;
        String[] queryProjection = {
                Activity.T_NAME + "." + Activity._ID,
                Activity.C_LABEL,
                Activity.C_DETAILS,
                Schedule.C_IS_ACTIVE,
                "MAX(" + TrackingLog.C_DATE + ") AS " + TrackingLog.C_DATE,
        };

        String groupBy = Activity.T_NAME + "." + Activity._ID;

        /*
        String querySelection = TrackingLog.C_DATE + " >= ? OR " + TrackingLog.C_DATE + " IS NULL";
        String[] querySelectionArgs = {
                "" + getMidnight()
        };*/

        String orderBy = TrackingLog.C_DATE + " ASC";

        cursor = dbHelper.getWritableDatabase().query(
                joinTable,
                queryProjection,
                null,
                null,
                groupBy,
                null,
                orderBy
        );
        listAdapter.changeCursor(cursor);
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
        reloadList();
    }

    private void insertTrackingLog(long activityId) {
        ContentValues trackingLogValues = new ContentValues();
        trackingLogValues.put(TrackingLog.C_ACTIVITY_ID, activityId); // we set the foreign key
        trackingLogValues.put(TrackingLog.C_DATE, calendar.getTimeInMillis()); // we set the date
        trackingLogValues.put(TrackingLog.C_IS_DONE, true);
        db.insert(TrackingLog.T_NAME, null, trackingLogValues);
        reloadList();
    }

}
