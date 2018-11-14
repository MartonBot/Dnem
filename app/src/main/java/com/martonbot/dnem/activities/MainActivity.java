package com.martonbot.dnem.activities;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.martonbot.dnem.ActivitiesAdapter;
import com.martonbot.dnem.Constants;
import com.martonbot.dnem.Dnem;
import com.martonbot.dnem.DnemApplication;
import com.martonbot.dnem.DnemChannels;
import com.martonbot.dnem.DnemList;
import com.martonbot.dnem.Preferences;
import com.martonbot.dnem.R;
import com.martonbot.dnem.filters.Filter;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.LinkedList;
import java.util.List;

/**
 * The main activity that displays the date and a list of DnemDatabase activities to do, in order of priority
 */
public class MainActivity extends UpdatableActivity {

    private SharedPreferences sharedPreferences;

    private ImageButton addButton;
    private ImageButton notifButton; // todo only temporary
    private ImageButton filtersButton;
    private TextView weekdayText;
    private TextView dateText;
    private ListView listView;

    private DnemList dnemList;

    // adapter for the DnemDatabase activities list
    private ActivitiesAdapter dnemsAdapter;

    private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd MMMM yyyy");
    private DateTimeFormatter weekdayFormat = DateTimeFormat.forPattern("EEEE");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);

        // run this to capture the timezone the first time the app is run
        onFirstRun();

        // get the list of Dnems from the application context
        // todo this should be done in a background thread
        dnemList = ((DnemApplication) getApplicationContext()).getDnemList();

        // UI elements
        listView = findViewById(R.id.listView);
        addButton = findViewById(R.id.add_button);
        notifButton = findViewById(R.id.notif_button);
        filtersButton = findViewById(R.id.filters_button);
        weekdayText = findViewById(R.id.weekday_text);
        dateText = findViewById(R.id.date_text);

        // list adapter for Dnems
        dnemsAdapter = new ActivitiesAdapter(MainActivity.this, MainActivity.this, dnemList.getFilteredDnems());
        listView.setAdapter(dnemsAdapter);

        // set listeners on buttons
        addButton.setOnClickListener(new OnAddButtonClickListener());
        filtersButton.setOnClickListener(new OnFiltersButtonClickListener());
        notifButton.setOnClickListener(new OnNotifButtonClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        dnemList.sortDnems();
        applyFilters();
    }

    /**
     * This method should be run only once, at the very first run of this activity
     */
    private void onFirstRun() {
        // is it the first time the app is run?
        boolean firstRun = !sharedPreferences.getBoolean(Constants.ALREADY_RUN, false);
        if (firstRun) {
            // capture the timezone
            String timeZoneId = DateTimeZone.getDefault().getID();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString(Constants.DEFAULT_TIMEZONE_ID, timeZoneId);
            // and flag as run
            e.putBoolean(Constants.ALREADY_RUN, true);
            e.apply();
        }
    }

    @Override
    public void update() {
        dnemsAdapter.notifyDataSetChanged();

        LocalDate today = LocalDate.now();
        LocalDate firstOfApril = new LocalDate(LocalDate.now().getYear(), 4, 1);
        String niceDate = dateFormat.print(today);
        if (today.isEqual(firstOfApril)) {
            niceDate += "\uD83D\uDC09"; // todo add more easter eggs, emojis...
        }
        weekdayText.setText(weekdayFormat.print(today));
        dateText.setText(niceDate);
        // todo consider adding a fireworks/confetti animation when completing all active activities
        // todo https://github.com/plattysoft/Leonids
    }

    /**
     * When pressing the new DnemDatabase activity button
     */
    private class OnAddButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent editActivity = new Intent(MainActivity.this, EditActivity.class);
            editActivity.putExtra(Constants.EXTRA_ACTION, Constants.ACTION_NEW);
            startActivity(editActivity);
        }
    }

    private class OnNotifButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            DnemList dnemList = ((DnemApplication) getApplicationContext()).getDnemList();
            List<Dnem> nextBestActivities = dnemList.getDnems();

            if (!nextBestActivities.isEmpty()) {
                Dnem nextBestActivity = nextBestActivities.get(0);
                String notifText = "Dont lose your " + nextBestActivity.getCurrentStreak() + " days streak";

                Notification.Builder builder = new Notification.Builder(MainActivity.this, DnemChannels.REMINDERS);
                builder.setSmallIcon(R.drawable.ic_star_black_24dp)
                        .setContentText(notifText);

                // Registering channel
                NotificationChannel remindersChannel = new NotificationChannel(DnemChannels.REMINDERS, DnemChannels.REMINDERS_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                remindersChannel.setDescription(DnemChannels.REMINDERS_DESC);
                NotificationManager notifManager = getSystemService(NotificationManager.class);
                notifManager.createNotificationChannel(remindersChannel);

                // Create intent
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                // todo review that code, especially around the activity backstack
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

                builder.setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                // Pop it
                int myNotifId = (int) (nextBestActivity.getId() % 1000); // todo generate an ID for the notif (PK of the activity to do?)
                builder.setContentTitle(nextBestActivity.getLabel());
                notifManager.notify(myNotifId, builder.build());
            } else {
                Toast.makeText(MainActivity.this, "No activity to be reminded of", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * When pressing the filters button
     */
    private class OnFiltersButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            CharSequence[] items = new CharSequence[]{
                    "Inactive"
            };
            boolean showInactive = !sharedPreferences.getBoolean(Preferences.PREF_IS_ACTIVE_FILTER_ON, true);
            boolean[] checkedItems = new boolean[]{
                    showInactive
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Filters");
            builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    SharedPreferences.Editor spEditor = sharedPreferences.edit();
                    if (which == 0) {
                        spEditor.putBoolean(Preferences.PREF_IS_ACTIVE_FILTER_ON, !isChecked);
                    }
                    spEditor.apply();
                }
            })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            applyFilters();
                            // and update the activity
                            update();
                        }
                    });
            builder.create().show();
        }
    }

    private void applyFilters() {
        boolean activeFilter = sharedPreferences.getBoolean(Preferences.PREF_IS_ACTIVE_FILTER_ON, false);
        List<Filter> filters = new LinkedList<>();
        if (activeFilter) {
            filters.add(new Filter() {
                @Override
                public boolean evaluate(Dnem dnem) {
                    return dnem.isActive();
                }
            });
        }
        dnemList.applyFilters(filters);
    }
}
