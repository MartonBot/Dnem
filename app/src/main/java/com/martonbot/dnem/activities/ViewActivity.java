package com.martonbot.dnem.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.martonbot.dnem.Constants;
import com.martonbot.dnem.DnemActivity;
import com.martonbot.dnem.DnemApplication;
import com.martonbot.dnem.DnemDbHelper;
import com.martonbot.dnem.OnDoneClickListener;
import com.martonbot.dnem.R;
import com.martonbot.dnem.Time;
import com.martonbot.dnem.TrackingLogs;
import com.martonbot.dnem.TrackingLogsAdapter;
import com.martonbot.dnem.UiUpdater;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

public class ViewActivity extends UpdatableActivity {

    private View doneButton;
    private ImageButton editButton;
    private ImageButton restoreButton;
    private TextView labelText;
    private TextView streakText;
    private TextView detailsText;
    private TextView bestStreakText;
    private TextView currentStreakText;
    private ListView trackingLogsListView;
    private ImageView starImage;
    private TrackingLogsAdapter trackingLogsAdapter;

    // the Dnem activity viewed here
    private long activityId;
    private DnemActivity activity;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        // get Dnem activity from the intent
        activityId = getIntent().getLongExtra("EXTRA_ACTIVITY_ID", 0);
        if (activityId == 0) {
            throw new IllegalStateException("A valid Dnem activity ID must be passed");
        }
        activity = ((DnemApplication) getApplicationContext()).getActivity(activityId);

        // UI elements
        labelText = findViewById(R.id.label_text);
        streakText = findViewById(R.id.streak_text);
        detailsText = findViewById(R.id.details_text);
        currentStreakText = findViewById(R.id.current_streak_text);
        bestStreakText = findViewById(R.id.best_streak_text);
        doneButton = findViewById(R.id.done_button);
        editButton = findViewById(R.id.edit_button);
        restoreButton = findViewById(R.id.restore_button);
        trackingLogsListView = findViewById(R.id.tracking_logs_list);
        starImage = findViewById(R.id.star_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // create the database to read and write
        db = new DnemDbHelper(ViewActivity.this).getWritableDatabase();
    }

    @Override
    protected void onPause() {
        db.close();
        super.onPause();
    }

    @Override
    protected void refreshActivityData(DnemActivity dnemActivity) {
        // it's the same as refreshData() since the activity is centered on a specific Dnem activity
        refreshData();
    }

    @Override
    protected void refreshData() {
        ((DnemApplication) getApplicationContext()).reloadActivityFromDb(activityId);
    }

    @Override
    protected void updateUi() {
        if (trackingLogsAdapter == null) {
            trackingLogsAdapter = new TrackingLogsAdapter(ViewActivity.this, activity);
            trackingLogsListView.setAdapter(trackingLogsAdapter);
        }
        trackingLogsAdapter.notifyDataSetChanged();

        currentStreakText.setText(String.format(getString(R.string.current_streak), activity.getCurrentStreak()));
        bestStreakText.setText(String.format(getString(R.string.best_streak), activity.getBestStreak()));

        UiUpdater.updateDoneButton(ViewActivity.this, activity, labelText, detailsText, doneButton, streakText, starImage);

        doneButton.setOnClickListener(new OnDoneClickListener(ViewActivity.this, ViewActivity.this, activity));

        editButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent editActivity = new Intent(ViewActivity.this, EditActivity.class);
                editActivity.putExtra(Constants.EXTRA_ACTIVITY_ID, activity.getId());
                startActivity(editActivity);
            }
        });

        restoreButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                // check whether there is something to fix for the day before
                LocalDate yesterday = Time.today().minusDays(1);
                if (activity.trackingLogFor(yesterday)) {
                    Toast.makeText(ViewActivity.this, "There is already a log for yesterday.", Toast.LENGTH_SHORT).show(); // todo make it a resource
                } else {
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(ViewActivity.this);
                    ConfirmRestoreClickListener confirmRestoreClickListener = new ConfirmRestoreClickListener();
                    adBuilder.setMessage("Restore yesterday?").setPositiveButton("Sure", confirmRestoreClickListener).setNegativeButton("Nah", confirmRestoreClickListener).show();
                }

            }
        });
    }

    /**
     * When pressing the "Restore tracking log for yesterday" button
     */
    private class ConfirmRestoreClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    long yesterdayTimestamp = new Instant().minus(Duration.standardDays(1)).getMillis();
                    TrackingLogs.insert(db, activity, ViewActivity.this, yesterdayTimestamp);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

}
