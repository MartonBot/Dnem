package com.martonbot.dnem;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ListAdapter listAdapter;

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

    }

    @Override
    protected void onResume() {
        super.onResume();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
                startActivity(editActivity);
            }
        });

        dbHelper = new DnemDbHelper(MainActivity.this);
        String joinTable = DnemContract.Activity.TABLE_NAME + " JOIN " + DnemContract.Schedule.TABLE_NAME + " ON " + DnemContract.Activity.TABLE_NAME + "." + DnemContract.Activity._ID + " = " + DnemContract.Schedule.COLUMN_NAME_ACTIVITY_ID;
        String[] queryProjection = {
                DnemContract.Activity.TABLE_NAME + "." + DnemContract.Activity._ID,
                DnemContract.Activity.COLUMN_NAME_LABEL,
                DnemContract.Activity.COLUMN_NAME_DETAILS,
                DnemContract.Schedule.COLUMN_NAME_IS_ACTIVE
        };

        String querySelection = DnemContract.Schedule.COLUMN_NAME_IS_ACTIVE + " = ?";
        String[] activitySelectionArgs = {
                "true"
        };

        cursor = dbHelper.getWritableDatabase().query(joinTable, queryProjection, null, null, null, null, null);

        listAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.list_item, cursor, stringArray, intArray, 0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
                editActivity.putExtra(Constants.EXTRA_ACTIVITY_ID, id);
                startActivity(editActivity);
            }
        });

        listView.setAdapter(listAdapter);
    }
}
