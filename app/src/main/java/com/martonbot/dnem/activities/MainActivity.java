package com.martonbot.dnem.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.martonbot.dnem.ActivitiesAdapter;
import com.martonbot.dnem.Constants;
import com.martonbot.dnem.DnemActivity;
import com.martonbot.dnem.DnemApplication;
import com.martonbot.dnem.Preferences;
import com.martonbot.dnem.R;
import com.martonbot.dnem.filters.DnemFilter;
import com.martonbot.dnem.filters.IsActiveFilter;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends UpdatableActivity {

    private ListView listView;
    private SharedPreferences sharedPreferences;

    private ImageButton addButton;
    private TextView weekdayText;
    private TextView dateText;
    private ImageButton filtersButton;

    // filters
    private DnemFilter isActiveFilter = new IsActiveFilter();

    private DnemApplication globalContext;

    private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd MMMM yyyy");
    private DateTimeFormatter weekdayFormat = DateTimeFormat.forPattern("EEEE");

    private List<DnemActivity> activities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        onFirstRun();

        globalContext = ((DnemApplication) getApplicationContext());

        setContentView(R.layout.activity_main);
        findControls();

        addButton.setOnClickListener(new OnAddButtonClickListener());
        filtersButton.setOnClickListener(new OnFiltersButtonClickListener());
    }

    private void findControls() {
        listView = (ListView) findViewById(R.id.listView);
        addButton = (ImageButton) findViewById(R.id.add_button);
        filtersButton = (ImageButton) findViewById(R.id.filters_button);
        weekdayText = (TextView) findViewById(R.id.weekday_text);
        dateText = (TextView) findViewById(R.id.date_text);
    }

    private void onFirstRun() {
        // do stuff for first run here
        checkTimeZone();

        // flag as run

        boolean firstRun = !sharedPreferences.getBoolean(Constants.ALREADY_RUN, false);
        if (firstRun) {
            SharedPreferences.Editor e = sharedPreferences.edit();
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

    @Override
    protected void refreshDataset() {
        globalContext.loadActivities();
        if (activities == null) {
            activities = new LinkedList<>();
        }
        activities.clear();
        activities.addAll(isActiveFilter.filter(globalContext.getActivities()));
    }

    @Override
    protected void updateUiElements() {
        // todo add a fireworks/confetti animation when completing all active activities
        // todo https://github.com/plattysoft/Leonids
        if (getAdapter() == null) {
            setAdapter(new ActivitiesAdapter(MainActivity.this, MainActivity.this, activities));
            listView.setAdapter(getAdapter());
        }
        LocalDate today = LocalDate.now();
        LocalDate firstOfApril = new LocalDate(LocalDate.now().getYear(), 4, 1);
        String niceDate = dateFormat.print(today);
        if (today.isEqual(firstOfApril)) {
            niceDate += "\uD83D\uDC09"; // todo add more easter eggs
        }
        weekdayText.setText(weekdayFormat.print(today));
        dateText.setText(niceDate);
    }

    @Override
    protected void onResume() {
        isActiveFilter.switchFilter(sharedPreferences.getBoolean(Preferences.PREF_IS_ACTIVE_FILTER_ON, true));
        super.onResume();
    }

    private class OnAddButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
            startActivity(editActivity);
        }
    }

    private class OnFiltersButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            CharSequence[] items = new CharSequence[]{
                    "Inactive"
            };
            boolean[] checkedItems = new boolean[] {
                    !sharedPreferences.getBoolean(Preferences.PREF_IS_ACTIVE_FILTER_ON, true)
            };
            final SharedPreferences.Editor spEditor = sharedPreferences.edit();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Filters");
            builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    // todo refactor a bit to make this clean...
                    if (which == 0) {
                        spEditor.putBoolean(Preferences.PREF_IS_ACTIVE_FILTER_ON, !isChecked);
                    }
                    spEditor.apply();
                }
            })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            onResume();
                        }
                    });
            builder.create().show();

        }
    }
}
