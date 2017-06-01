package com.martonbot.dnem;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.martonbot.dnem.Dnem.Activity;
import com.martonbot.dnem.Dnem.Schedule;
import com.martonbot.dnem.Dnem.TrackingLog;

import org.joda.time.LocalDate;

public class ViewActivity extends android.app.Activity {

    private ImageButton doneButton;
    private ImageButton editButton;
    private TextView labelText;
    private TextView detailsText;
    private TextView latestText;
    private TextView streakText;

    private DnemDbHelper dbHelper;
    private SQLiteDatabase db;
    private long activityId;


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
    }

    private void findControls() {
        labelText = (TextView) findViewById(R.id.label_text);
        detailsText = (TextView) findViewById(R.id.details_text);
        latestText = (TextView) findViewById(R.id.latest_text);
        streakText = (TextView) findViewById(R.id.streak_text);
        doneButton = (ImageButton) findViewById(R.id.done_button);
        editButton = (ImageButton) findViewById(R.id.edit_button);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadActivityInfo();
        loadTrackingLogsInfo();

    }

    private void loadActivityInfo() {
        String joinTable = Activity.T_NAME
                + " JOIN "
                + Schedule.T_NAME
                + " ON " + Activity.T_NAME + "." + Activity._ID
                + " = " + Schedule.T_NAME + "." + Schedule.C_ACTIVITY_ID;
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
                joinTable,
                queryProjection,
                querySelection,
                querySelectionArgs,
                null,
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

            String label = activityCursor.getString(labelIndex);
            String details = activityCursor.getString(detailsIndex);

            labelText.setText(label);
            detailsText.setText(details);
        }

        activityCursor.close();
    }

    private void loadTrackingLogsInfo() {
        String[] queryProjection = {
                TrackingLog._ID,
                TrackingLog.C_UTC_DAY,
                TrackingLog.C_TIMEZONE
        };

        String querySelection = TrackingLog.T_NAME + "." + TrackingLog.C_ACTIVITY_ID + " = ?";
        String[] querySelectionArgs = {
                "" + activityId
        };

        Cursor tlCursor = dbHelper.getWritableDatabase().query(
                TrackingLog.T_NAME,
                queryProjection,
                querySelection,
                querySelectionArgs,
                null,
                null,
                null
        );

        int currentStreak = 0;
        LocalDate today = new LocalDate();
        LocalDate currentDay = today;
        // loop through the tracking logs to compute the streak
        while (tlCursor.moveToNext()) {
            int idIndex = tlCursor.getColumnIndex(TrackingLog._ID);
            int dayIndex = tlCursor.getColumnIndex(TrackingLog.C_UTC_DAY);
            int timezoneIndex = tlCursor.getColumnIndex(TrackingLog.C_TIMEZONE);

            LocalDate tlDate = new LocalDate(tlCursor.getString(dayIndex)); // get the date of the tracking log
            if (currentDay.equals(tlDate)) {
                currentStreak++;
            }
            else {
                break;
            }
            // TODO at some point we will need to involve the timezone
            currentDay = currentDay.minusDays(1);
        }

        String streakString = currentStreak + " days";
        streakText.setText(streakString);

        tlCursor.close();
    }

}
