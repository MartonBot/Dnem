package com.martonbot.dnem.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.martonbot.dnem.Dnem.Activity;
import com.martonbot.dnem.Dnem.Schedule;
import com.martonbot.dnem.DnemActivity;
import com.martonbot.dnem.DnemApplication;
import com.martonbot.dnem.DnemDbHelper;
import com.martonbot.dnem.R;

public class EditActivity extends android.app.Activity {

    private Button cancelButton;
    private Button saveButton;
    private EditText labelEdit;
    private EditText detailsEdit;
    private Switch scheduleActivitySwitch;
    private Switch allowStarsSwitch;
    private ImageButton deleteButton;

    private SQLiteDatabase db;
    private DnemActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the activity ID from the intent if it was passed
        long activityId = getIntent().getLongExtra("EXTRA_ACTIVITY_ID", 0);

        setContentView(R.layout.activity_edit);

        activity = activityId != 0 ? ((DnemApplication) getApplicationContext()).getActivity(activityId) : null;

        // set the controls
        cancelButton = (Button) findViewById(R.id.cancel_button);
        saveButton = (Button) findViewById(R.id.save_button);
        labelEdit = (EditText) findViewById(R.id.label_edit);
        detailsEdit = (EditText) findViewById(R.id.details_edit);
        scheduleActivitySwitch = (Switch) findViewById(R.id.schedule_activity_switch);
        allowStarsSwitch = (Switch) findViewById(R.id.cheat_days_enable_switch);
        deleteButton = (ImageButton) findViewById(R.id.delete_button);

        // populate the fields from the database if the activity ID was passed
        if (activity != null) {
            labelEdit.setText(activity.getLabel());
            detailsEdit.setText(activity.getDetails());
            scheduleActivitySwitch.setChecked(activity.isActive());
            allowStarsSwitch.setChecked(activity.allowStars());
        }

        // controls listeners
        cancelButton.setOnClickListener(new CancelButtonOnClickListener());
        saveButton.setOnClickListener(new SaveButtonOnClickListener());
        deleteButton.setOnClickListener(new DeleteButtonOnClickListener());

    }

    @Override
    protected void onResume() {
        super.onResume();
        // create the database to read and write
        db = new DnemDbHelper(EditActivity.this).getWritableDatabase();
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    private void saveActivity() {

        // if the activity doesn't exist yet, create it
        if (activity == null) {
            activity = new DnemActivity();
        }

        // first edit the DnemActivity
        activity.setLabel(labelEdit.getText().toString());
        activity.setDetails(detailsEdit.getText().toString());
        activity.setActive(scheduleActivitySwitch.isChecked());
        activity.setAllowStars(allowStarsSwitch.isChecked());

        // Activity
        ContentValues activityValues = new ContentValues();
        activityValues.put(Activity.C_LABEL, activity.getLabel());
        activityValues.put(Activity.C_DETAILS, activity.getDetails());

        // Schedule
        ContentValues scheduleValues = new ContentValues();
        scheduleValues.put(Schedule.C_IS_ACTIVE, activity.isActive());
        scheduleValues.put(Schedule.C_ALLOW_STARS, activity.allowStars());

        if (activity.getId() == 0) {
            // insert a new one
            long newActivityId = db.insert(
                    Activity.T_NAME,
                    null,
                    activityValues);

            scheduleValues.put(Schedule.C_ACTIVITY_ID, newActivityId); // this is where we set the foreign key

            long newScheduleId = db.insert(
                    Schedule.T_NAME,
                    null,
                    scheduleValues);

            activity.setId(newActivityId);
        }
        else {
            // update
            long activityId = activity.getId();

            String activitySelection = Activity._ID + " = ?";
            String[] activitySelectionArgs = {
                    "" + activityId
            };

            String scheduleSelection = Schedule.C_ACTIVITY_ID + " = ?";
            String[] scheduleSelectionArgs = {
                    "" + activityId
            };

            int activityCount = db.update(
                    Activity.T_NAME,
                    activityValues,
                    activitySelection,
                    activitySelectionArgs);

            int scheduleCount = db.update(
                    Schedule.T_NAME,
                    scheduleValues,
                    scheduleSelection,
                    scheduleSelectionArgs);

            if (activityCount != scheduleCount) {
                throw new IllegalStateException("Illegal database state");
            }
        }
    }

    private void deleteActivity() {
        long activityId = activity.getId();
        String activitySelection = Activity._ID + " = ?";
        String[] activitySelectionArgs = {
                "" + activityId
        };

        String scheduleSelection = Schedule.C_ACTIVITY_ID + " = ?";
        String[] scheduleSelectionArgs = {
                "" + activityId
        };
        db.delete(Schedule.T_NAME,
                scheduleSelection,
                scheduleSelectionArgs);

        db.delete(Activity.T_NAME,
                activitySelection,
                activitySelectionArgs);

        Toast.makeText(EditActivity.this, "deleted", Toast.LENGTH_SHORT).show();
    }

    private class SaveButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            db.beginTransaction();
            try {
                saveActivity();
                db.setTransactionSuccessful();
                Toast.makeText(EditActivity.this, "Activity saved", Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e) {
                Toast.makeText(EditActivity.this, "Failed to save the activity", Toast.LENGTH_SHORT).show();
            } finally {
                db.endTransaction();
            }

            // leave activity
            finish();
        }
    }

    private class CancelButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }
    }

    private class DeleteButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // delete the activity if it exists
            if (activity != null) {
                db.beginTransaction();
                try {
                    deleteActivity();
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                } finally {
                    db.endTransaction();
                }
            }

            // leave activity
            finish();
            Intent mainActivity = new Intent(EditActivity.this, MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Will clear out your activity history stack till now
            startActivity(mainActivity);
        }
    }
}
