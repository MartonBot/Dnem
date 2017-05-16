package com.martonbot.dnem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    private Button cancelButton;
    private Button saveButton;
    private EditText labelEdit;

    private SQLiteDatabase db;
    private long activityId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityId = getIntent().getLongExtra("EXTRA_ACTIVITY_ID", 0);

        setContentView(R.layout.activity_edit);

        cancelButton = (Button) findViewById(R.id.cancel_button);
        saveButton = (Button) findViewById(R.id.save_button);
        labelEdit = (EditText) findViewById(R.id.label_edit);

        db = new DnemDbHelper(EditActivity.this).getWritableDatabase();

        populateActivity(activityId);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                cv.put(DnemContract.Activity.COLUMN_NAME_LABEL, labelEdit.getText().toString());

                if (activityId != 0) {
                    // update
                    String selection = DnemContract.Activity._ID + " = ?";
                    String[] selectionArgs = {
                            "" + activityId
                    };
                    int count = db.update(
                            DnemContract.Activity.TABLE_NAME,
                            cv,
                            selection,
                            selectionArgs);

                    Toast.makeText(EditActivity.this, count + " rows updated", Toast.LENGTH_SHORT).show();
                }
                else {
                    // insert
                    long newRowId = db.insert(
                            DnemContract.Activity.TABLE_NAME,
                            null,
                            cv);
                    Toast.makeText(EditActivity.this, "inserted row " + newRowId, Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

    }

    private void populateActivity(long activityId) {

        String[] projection =  {
                DnemContract.Activity._ID,
                DnemContract.Activity.COLUMN_NAME_LABEL
        };
        String selection = DnemContract.Activity._ID + " = ?";
        String[] selectionArgs = {
                "" + activityId
        };
        Cursor cursor = db.query(
                DnemContract.Activity.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToNext()) {
            labelEdit.setText(cursor.getString(1));
        }

    }
}
