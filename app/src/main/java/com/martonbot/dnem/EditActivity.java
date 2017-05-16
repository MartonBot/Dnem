package com.martonbot.dnem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    private Button cancelButton;
    private Button saveButton;
    private EditText labelEdit;
    private EditText detailsEdit;
    private Switch scheduleActivitySwitch;

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

        // create the database to read and write
        db = new DnemDbHelper(EditActivity.this).getWritableDatabase();

        // populate the fields from the database if the activity ID was passed
        populateActivity(activityId);

        // controls listeners
        cancelButton.setOnClickListener(new CancelButtonOnClickListener());
        saveButton.setOnClickListener(new SaveButtonOnClickListener());

    }

    private void populateActivity(long activityId) {

        // get data from the join between activity and schedule
        String[] projection = {
                DnemContract.Activity.TABLE_NAME + "." + DnemContract.Activity._ID,
                DnemContract.Activity.COLUMN_NAME_LABEL,
                DnemContract.Activity.COLUMN_NAME_DETAILS,
                DnemContract.Schedule.COLUMN_NAME_IS_ACTIVE
        };
        String selection = DnemContract.Activity.TABLE_NAME + "." + DnemContract.Activity._ID + " = ?";
        String[] selectionArgs = {
                "" + activityId
        };
        String joinTable = DnemContract.Activity.TABLE_NAME + " JOIN " + DnemContract.Schedule.TABLE_NAME + " ON " + DnemContract.Activity.TABLE_NAME + "." + DnemContract.Activity._ID + " = " + DnemContract.Schedule.COLUMN_NAME_ACTIVITY_ID;
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
            labelEdit.setText(cursor.getString(1));
            detailsEdit.setText(cursor.getString(2));
        } else {
            // there's something wrong with the database

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

    private void saveActivity() throws Exception {

        // Activity
        ContentValues activityValues = new ContentValues();
        activityValues.put(DnemContract.Activity.COLUMN_NAME_LABEL, labelEdit.getText().toString());
        activityValues.put(DnemContract.Activity.COLUMN_NAME_DETAILS, detailsEdit.getText().toString());

        // Schedule
        ContentValues scheduleValues = new ContentValues();
        scheduleValues.put(DnemContract.Schedule.COLUMN_NAME_IS_ACTIVE, scheduleActivitySwitch.isChecked());

        String activitySelection = DnemContract.Activity._ID + " = ?";
        String[] activitySelectionArgs = {
                "" + activityId
        };

        String scheduleSelection = DnemContract.Schedule.COLUMN_NAME_ACTIVITY_ID + " = ?";
        String[] scheduleSelectionArgs = {
                "" + activityId
        };

        if (activityId != 0) {

            int activityCount = db.update(
                    DnemContract.Activity.TABLE_NAME,
                    activityValues,
                    activitySelection,
                    activitySelectionArgs);

            int scheduleCount = db.update(
                    DnemContract.Schedule.TABLE_NAME,
                    scheduleValues,
                    scheduleSelection,
                    scheduleSelectionArgs);

            if (activityCount != scheduleCount) {
                throw  new Exception("Illegal database state");
            }

            //debug
            Toast.makeText(EditActivity.this, activityCount + " rows updated", Toast.LENGTH_SHORT).show();
        } else {
            long newActivityId = db.insert(
                    DnemContract.Activity.TABLE_NAME,
                    null,
                    activityValues);

            scheduleValues.put(DnemContract.Schedule.COLUMN_NAME_ACTIVITY_ID, newActivityId);

            long newScheduleId = db.insert(
                    DnemContract.Schedule.TABLE_NAME,
                    null,
                    scheduleValues);

            // debug
            Toast.makeText(EditActivity.this, "activity created", Toast.LENGTH_SHORT).show();
        }
    }
}
