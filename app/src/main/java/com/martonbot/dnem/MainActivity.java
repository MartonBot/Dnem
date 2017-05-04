package com.martonbot.dnem;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    ListAdapter listAdapter;

    private Cursor cursor;

    private String[] stringArray = new String[] {"name"}; // the column names
    private int[] intArray = new int[] {R.id.textView}; // the views IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_SHORT).show();
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        
        listAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.list_item, null, stringArray, intArray, 0);

        listView.setAdapter(listAdapter);
    }

}
