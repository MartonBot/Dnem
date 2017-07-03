package com.martonbot.dnem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MainActivity extends android.app.Activity {

    private ListView listView;

    private ImageButton addButton;
    private TextView weekdayText;
    private TextView dateText;

    private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd MMMM yyyy");
    private DateTimeFormatter weekdayFormat = DateTimeFormat.forPattern("EEEE");

    private ActivitiesAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onFirstRun();

        setContentView(R.layout.activity_main);
        findControls();

        DnemApplication globalContext = (DnemApplication) getApplicationContext();

        globalContext.loadActivities();
        listAdapter = new ActivitiesAdapter(MainActivity.this, globalContext.getActivities());
        listView.setAdapter(listAdapter);
        addButton.setOnClickListener(new OnAddButtonClickListener());
    }

    private void findControls() {
        listView = (ListView) findViewById(R.id.listView);
        addButton = (ImageButton) findViewById(R.id.add_button);
        weekdayText = (TextView) findViewById(R.id.weekday_text);
        dateText = (TextView) findViewById(R.id.date_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalDate today = LocalDate.now();
        weekdayText.setText(weekdayFormat.print(today));
        dateText.setText(dateFormat.print(today));
        listAdapter.notifyDataSetChanged();
    }

    private void onFirstRun() {
        // do stuff for first run here
        checkTimeZone();

        // flag as run
        SharedPreferences sp = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        boolean firstRun = !sp.getBoolean(Constants.ALREADY_RUN, false);
        if (firstRun) {
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean(Constants.ALREADY_RUN, true);
            e.apply();
        }
    }

    private void checkTimeZone() {
        SharedPreferences sp = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        String timeZoneId = DateTimeZone.getDefault().getID();
        SharedPreferences.Editor e = sp.edit();
        e.putString(Constants.DEFAULT_TIMEZONE_ID, timeZoneId);
        e.apply();
    }

    private class OnAddButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
            startActivity(editActivity);
        }
    }

}
