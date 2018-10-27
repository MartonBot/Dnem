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
import com.martonbot.dnem.DnemActivity;
import com.martonbot.dnem.DnemApplication;
import com.martonbot.dnem.DnemChannels;
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

/**
 * The main activity that displays the date and a list of Dnem activities to do, in order of priority
 */
public class MainActivity extends UpdatableActivity {

    private ListView listView;
    private SharedPreferences sharedPreferences;

    private ImageButton addButton;
    private ImageButton notifButton;
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
        notifButton.setOnClickListener(new OnNotifButtonClickListener());
    }

    private void findControls() {
        listView = findViewById(R.id.listView);
        addButton = findViewById(R.id.add_button);
        notifButton = findViewById(R.id.notif_button);
        filtersButton = findViewById(R.id.filters_button);
        weekdayText =  findViewById(R.id.weekday_text);
        dateText = findViewById(R.id.date_text);
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
    protected void refreshData() {
        globalContext.loadActivitiesFromDb();
        if (activities == null) {
            activities = new LinkedList<>();
        }
        activities.clear();
        activities.addAll(isActiveFilter.filter(globalContext.getActivities()));
    }

    @Override
    protected void updateUi() {
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

    private class OnNotifButtonClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            globalContext.loadActivitiesFromDb();
            List<DnemActivity> nextBestActivities = globalContext.getActivities();
            List<DnemActivity> nextBestActiveActivities = isActiveFilter.filter(nextBestActivities);
            DnemActivity nextBestActivity = nextBestActiveActivities.get(0);

            Notification.Builder builder = new Notification.Builder(MainActivity.this, DnemChannels.REMINDERS);
            builder.setSmallIcon(R.drawable.ic_star_black_24dp)
                    .setContentText(nextBestActivity.getDetails());

            // Registering channel
            NotificationChannel remindersChannel = new NotificationChannel(DnemChannels.REMINDERS, DnemChannels.REMINDERS_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            remindersChannel.setDescription(DnemChannels.REMINDERS_DESC);
            NotificationManager notifManager = getSystemService(NotificationManager.class);
            notifManager.createNotificationChannel(remindersChannel);


            // Create intent
            Intent intent  = new Intent(MainActivity.this, ViewActivity.class);
            long myActivityId = nextBestActivity.getId();
            intent.putExtra(Constants.EXTRA_ACTIVITY_ID, myActivityId);
            // todo review that code, especially around the activity backstack
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

            builder.setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Pop it
            int myNotifid = (int) (nextBestActivity.getId() % 1000); // todo generate an ID for the notif (PK of the activity to do?)

            builder.setContentTitle(nextBestActivity.getLabel() + " - " + myActivityId + " - " + myNotifid);

            notifManager.notify(myNotifid, builder.build());


        }
    }

    /**
     * When clickin on the filters button
     */
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
