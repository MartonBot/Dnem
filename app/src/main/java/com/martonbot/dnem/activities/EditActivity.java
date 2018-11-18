package com.martonbot.dnem.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.martonbot.dnem.Constants;
import com.martonbot.dnem.DnemApplication;
import com.martonbot.dnem.R;
import com.martonbot.dnem.data.Dnem;
import com.martonbot.dnem.data.DnemList;

public class EditActivity extends android.app.Activity {

    private Button cancelButton;
    private Button saveButton;
    private EditText labelEdit;
    private EditText detailsEdit;
    private Switch scheduleActivitySwitch;
    private Switch allowStarsSwitch;
    private ImageButton deleteButton;

    private Dnem dnem;
    private DnemList dnemList;
    private int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the activity ID from the intent if it was passed
        long dnemId = getIntent().getLongExtra(Constants.EXTRA_ACTIVITY_ID, 0);
        action = getIntent().getIntExtra(Constants.EXTRA_ACTION, 0);

        setContentView(R.layout.activity_edit);

        dnemList = ((DnemApplication) getApplicationContext()).getDnemList();
        if (dnemId == 0) {
            dnem = new Dnem(dnemId);
        } else {
            dnem = dnemList.getDnem(dnemId);
        }

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

    private class SaveButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (action) {
                case Constants.ACTION_EDIT:
                    setUiValues(dnem);
                    dnemList.update(EditActivity.this, dnem);
                    break;
                case Constants.ACTION_NEW:
                    setUiValues(dnem);
                    dnemList.insert(EditActivity.this, dnem);
                    break;
                default:
                    throw new IllegalStateException("The action is invalid");
            }

            // leave activity
            finish();
            // todo here, when we go back to the Main activity we don't see the newly created Dnem. fix that
        }
    }

    private void setUiValues(Dnem dnem) {
        String label = labelEdit.getText().toString();
        String details = detailsEdit.getText().toString();
        boolean isActive = scheduleActivitySwitch.isChecked();
        boolean allowStars = allowStarsSwitch.isChecked();
        dnem.setLabel(label);
        dnem.setDetails(details);
        dnem.setActive(isActive);
        dnem.setAllowStars(allowStars);
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

                    dnemList.delete(EditActivity.this, dnem);

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
