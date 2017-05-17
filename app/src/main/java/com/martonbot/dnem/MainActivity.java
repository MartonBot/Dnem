package com.martonbot.dnem;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private DnemActivityAdapter listAdapter;

    private Cursor cursor;
    private DnemDbHelper dbHelper;

    private String[] stringArray = new String[]{
            DnemContract.Activity.COLUMN_NAME_LABEL,
            DnemContract.Activity.COLUMN_NAME_DETAILS,
    }; // the column names
    private int[] intArray = new int[]{
            R.id.label_text,
            R.id.details_text
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        dbHelper = new DnemDbHelper(MainActivity.this);
        String joinTable = DnemContract.Activity.TABLE_NAME + " JOIN " + DnemContract.Schedule.TABLE_NAME + " ON " + DnemContract.Activity.TABLE_NAME + "." + DnemContract.Activity._ID + " = " + DnemContract.Schedule.COLUMN_NAME_ACTIVITY_ID;
        String[] queryProjection = {
                DnemContract.Activity.TABLE_NAME + "." + DnemContract.Activity._ID,
                DnemContract.Activity.COLUMN_NAME_LABEL,
                DnemContract.Activity.COLUMN_NAME_DETAILS,
                DnemContract.Schedule.COLUMN_NAME_IS_ACTIVE
        };

        // TODO use those
        String querySelection = DnemContract.Schedule.COLUMN_NAME_IS_ACTIVE + " = ?";
        String[] activitySelectionArgs = {
                "true"
        };

        cursor = dbHelper.getWritableDatabase().query(joinTable, queryProjection, null, null, null, null, null);
        listAdapter.changeCursor(cursor);
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
            int idIndex = cursor.getColumnIndex(DnemContract.Activity._ID);
            int labelIndex = cursor.getColumnIndex(DnemContract.Activity.COLUMN_NAME_LABEL);
            int detailsIndex = cursor.getColumnIndex(DnemContract.Activity.COLUMN_NAME_DETAILS);
            int scheduledIndex = cursor.getColumnIndex(DnemContract.Schedule.COLUMN_NAME_IS_ACTIVE);

            long activityId = cursor.getLong(idIndex);

            // set them on the view elements
            TextView labelText = (TextView) view.findViewById(R.id.label_text);
            TextView detailsText = (TextView) view.findViewById(R.id.details_text);

            labelText.setText(cursor.getString(labelIndex));
            detailsText.setText(cursor.getString(detailsIndex));
            int backgroundDrawableId = cursor.getInt(scheduledIndex) > 0 ? R.drawable.background_item_rc_enabled : R.drawable.background_item_rc_disabled;
            view.setBackground(getResources().getDrawable(backgroundDrawableId, null));

            // set the listeners
            view.setOnClickListener(new OnActivityClickListener(activityId));
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

}
