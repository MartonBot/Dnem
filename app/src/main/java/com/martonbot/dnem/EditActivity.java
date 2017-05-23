package com.martonbot.dnem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.martonbot.dnem.Dnem.Activity;
import com.martonbot.dnem.Dnem.Schedule;
import com.martonbot.dnem.Dnem.TrackingLog;

public class EditActivity extends AppCompatActivity {

    private Button cancelButton;
    private Button saveButton;
    private EditText labelEdit;
    private EditText detailsEdit;
    private Switch scheduleActivitySwitch;
    private ImageButton deleteButton;

    private SQLiteDatabase db;
    private long activityId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the activity ID from the intent if it was passed
        activityId = getIntent().getLongExtra("EXTRA_ACTIVITY_ID", 0);

        setContentView(R.layout.activity_edit);

        // set the controls
        cancelButton = (Button) findViewById(R.id.cancel_button);
        saveButton = (Button) findViewById(R.id.save_button);
        labelEdit = (EditText) findViewById(R.id.label_edit);
        detailsEdit = (EditText) findViewById(R.id.details_edit);
        scheduleActivitySwitch = (Switch) findViewById(R.id.schedule_activity_switch);
        deleteButton = (ImageButton) findViewById(R.id.delete_button);

        // create the database to read and write
        db = new DnemDbHelper(EditActivity.this).getWritableDatabase();

        // populate the fields from the database if the activity ID was passed
        populateActivity(activityId);

        // controls listeners
        cancelButton.setOnClickListener(new CancelButtonOnClickListener());
        saveButton.setOnClickListener(new SaveButtonOnClickListener());
        deleteButton.setOnClickListener(new DeleteButtonOnClickListener());

    }

    private void populateActivity(long activityId) {

        // get data from the join between activity and schedule
        String[] projection = {
                Activity.T_NAME + "." + Activity._ID,
                Activity.C_LABEL,
                Activity.C_DETAILS,
                Schedule.C_IS_ACTIVE
        };
        String selection = Activity.T_NAME + "." + Activity._ID + " = ?";
        String[] selectionArgs = {
                "" + activityId
        };
        String joinTable = Activity.T_NAME + " JOIN " + Schedule.T_NAME + " ON " + Activity.T_NAME + "." + Activity._ID + " = " + Schedule.C_ACTIVITY_ID;
        Cursor cursor = db.query(
                joinTable,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // if we find a result
        if (cursor.moveToNext()) {
            int labelIndex = cursor.getColumnIndex(Activity.C_LABEL);
            int detailsIndex = cursor.getColumnIndex(Activity.C_DETAILS);
            int scheduledIndex = cursor.getColumnIndex(Schedule.C_IS_ACTIVE);
            labelEdit.setText(cursor.getString(labelIndex));
            detailsEdit.setText(cursor.getString(detailsIndex));
            scheduleActivitySwitch.setChecked(cursor.getInt(scheduledIndex) > 0);
        } else {
            // throw new IllegalStateException("No activity corresponding to this ID");
        }

        cursor.close();

    }

    private class SaveButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            db.beginTransaction();
            try {
                saveActivity();
                db.setTransactionSuccessful();
            } catch (Exception e) {
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
            if (activityId != 0) {
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
        }
    }

    private void saveActivity() throws Exception {

        // Activity
        ContentValues activityValues = new ContentValues();
        activityValues.put(Activity.C_LABEL, labelEdit.getText().toString());
        activityValues.put(Activity.C_DETAILS, detailsEdit.getText().toString());

        // Schedule
        ContentValues scheduleValues = new ContentValues();
        scheduleValues.put(Schedule.C_IS_ACTIVE, scheduleActivitySwitch.isChecked());

        String activitySelection = Activity._ID + " = ?";
        String[] activitySelectionArgs = {
                "" + activityId
        };

        String scheduleSelection = Schedule.C_ACTIVITY_ID + " = ?";
        String[] scheduleSelectionArgs = {
                "" + activityId
        };

        if (activityId != 0) {
            // update
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
                throw new Exception("Illegal database state");
            }

            //debug
            Toast.makeText(EditActivity.this, activityCount + " rows updated", Toast.LENGTH_SHORT).show();
        } else {
            // insert
            long newActivityId = db.insert(
                    Activity.T_NAME,
                    null,
                    activityValues);

            scheduleValues.put(Schedule.C_ACTIVITY_ID, newActivityId); // this is where we set the foreign key

            long newScheduleId = db.insert(
                    Schedule.T_NAME,
                    null,
                    scheduleValues);

            // debug
            Toast.makeText(EditActivity.this, "activity created", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteActivity() throws Exception {
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
}
