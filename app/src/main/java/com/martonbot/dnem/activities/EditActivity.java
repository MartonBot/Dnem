package com.martonbot.dnem.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.martonbot.dnem.Constants;
import com.martonbot.dnem.Dnem;
import com.martonbot.dnem.DnemApplication;
import com.martonbot.dnem.DnemDatabase.Activity;
import com.martonbot.dnem.DnemDatabase.Schedule;
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

    private Dnem dnem;
    private int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the activity ID from the intent if it was passed
        long activityId = getIntent().getLongExtra(Constants.EXTRA_ACTIVITY_ID, 0);
        action = getIntent().getIntExtra(Constants.EXTRA_ACTION, 0);

        setContentView(R.layout.activity_edit);

        dnem = activityId != 0 ? ((DnemApplication) getApplicationContext()).getDnemList().getDnem(activityId) : null;

        // set the controls
        cancelButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.save_button);
        labelEdit = findViewById(R.id.label_edit);
        detailsEdit = findViewById(R.id.details_edit);
        scheduleActivitySwitch = findViewById(R.id.schedule_activity_switch);
        allowStarsSwitch = findViewById(R.id.cheat_days_enable_switch);
        deleteButton = findViewById(R.id.delete_button);

        // populate the fields from the database if the activity ID was passed
        if (dnem != null) {
            labelEdit.setText(dnem.getLabel());
            detailsEdit.setText(dnem.getDetails());
            scheduleActivitySwitch.setChecked(dnem.isActive());
            allowStarsSwitch.setChecked(dnem.allowStars());
        }

        // controls listeners
        cancelButton.setOnClickListener(new CancelButtonOnClickListener());
        saveButton.setOnClickListener(new SaveButtonOnClickListener());
        deleteButton.setOnClickListener(new DeleteButtonOnClickListener());

    }

    private ContentValues buildDnemValuesFromUi() {
        String label = labelEdit.getText().toString();
        String details = detailsEdit.getText().toString();
        ContentValues activityValues = new ContentValues();
        activityValues.put(Activity.C_LABEL, label);
        activityValues.put(Activity.C_DETAILS, details);
        return activityValues;
    }

    private ContentValues buildScheduleValuesFromUi() {
        boolean isActive = scheduleActivitySwitch.isChecked();
        boolean allowStars = allowStarsSwitch.isChecked();
        ContentValues scheduleValues = new ContentValues();
        scheduleValues.put(Schedule.C_IS_ACTIVE, isActive);
        scheduleValues.put(Schedule.C_ALLOW_STARS, allowStars);
        return scheduleValues;
    }

    /**
     * If the dnem doesn't exist yet in the database.
     */
    private void insertDnem() {

        boolean success = false;
        long newDnemId = 0;
        SQLiteDatabase db = new DnemDbHelper(EditActivity.this).getWritableDatabase();
        db.beginTransaction();
        try {

            newDnemId = db.insert(
                    Activity.T_NAME,
                    null,
                    buildDnemValuesFromUi());
            ContentValues scheduleValues = buildScheduleValuesFromUi();

            scheduleValues.put(Schedule.C_ACTIVITY_ID, newDnemId); // this is where we set the foreign key
            long scheduleId = db.insert(
                    Schedule.T_NAME,
                    null,
                    scheduleValues);

            if (newDnemId == -1 || scheduleId == -1) {
                throw new IllegalStateException("Illegal database state");
            }
            db.setTransactionSuccessful();
            success = true;

        } catch (IllegalStateException e) {
            Toast.makeText(EditActivity.this, "Failed to insert the new Dnem into the database.", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }

        if (success) {
            ((DnemApplication) getApplicationContext()).getDnemList().onDnemInserted(newDnemId);
        }
    }

    /**
     * If the dnem doesn't exist yet in the database.
     */
    private void updateDnem() {
        boolean success = false;
        long activityId = dnem.getId();

        String activitySelection = Activity._ID + " = ?";
        String[] activitySelectionArgs = {
                "" + activityId
        };

        String scheduleSelection = Schedule.C_ACTIVITY_ID + " = ?";
        String[] scheduleSelectionArgs = {
                "" + activityId
        };

        SQLiteDatabase db = new DnemDbHelper(EditActivity.this).getWritableDatabase();
        db.beginTransaction();
        try {
            int activityCount = db.update(
                    Activity.T_NAME,
                    buildDnemValuesFromUi(),
                    activitySelection,
                    activitySelectionArgs);

            int scheduleCount = db.update(
                    Schedule.T_NAME,
                    buildScheduleValuesFromUi(),
                    scheduleSelection,
                    scheduleSelectionArgs);

            if (activityCount != 1 || scheduleCount != 1) {
                throw new IllegalStateException("Illegal database state");
            }
            db.setTransactionSuccessful();
            success = true;
        } catch (IllegalStateException e) {
            Toast.makeText(EditActivity.this, "Failed to update the Dnem in the database.", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }

        if (success) {
            ((DnemApplication) getApplicationContext()).getDnemList().onDnemUpdated(activityId);
        }

    }

    private void deleteDnem() {
        long activityId = dnem.getId();
        String activitySelection = Activity._ID + " = ?";
        String[] activitySelectionArgs = {
                "" + activityId
        };

        SQLiteDatabase db = new DnemDbHelper(EditActivity.this).getWritableDatabase();

        db.delete(Activity.T_NAME,
                activitySelection,
                activitySelectionArgs);

        db.close();

        ((DnemApplication) getApplicationContext()).getDnemList().onDnemDeleted(activityId);
    }

    private class SaveButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (action) {
                case Constants.ACTION_EDIT:
                    updateDnem();
                    // notify the list of Dnems that it should reload the updated Dnem
                    break;
                case Constants.ACTION_NEW:
                    insertDnem();
                    // notify the list of Dnems that it should load the new Dnem from the db
                    break;
                default:
                    throw new IllegalStateException("The action is invalid");
            }

            // leave activity
            finish();
            // todo here, when we go back to the Main activity we don't see the newly created Dnem. fix that
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
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(EditActivity.this);
            ConfirmDeleteClickListener confirmDeleteClickListener = new ConfirmDeleteClickListener();
            adBuilder.setMessage("Do you really want to delete this Dnem? All the history will be lost.").setPositiveButton("Delete it", confirmDeleteClickListener).setNegativeButton("Never mind", confirmDeleteClickListener).show();


        }
    }

    private class ConfirmDeleteClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    // delete the activity if it exists
                    if (dnem != null) {
                        deleteDnem();
                    }

                    // go directly to the main activity
                    Intent mainActivity = new Intent(EditActivity.this, MainActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Will clear out your activity history stack till now
                    startActivity(mainActivity);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

}
