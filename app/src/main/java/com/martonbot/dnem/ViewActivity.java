package com.martonbot.dnem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ViewActivity extends UpdatableActivity {

    private View doneButton;
    private ImageButton editButton;
    private TextView labelText;
    private TextView streakText;
    private TextView detailsText;
    private TextView bestStreakText;
    private TextView currentStreakText;
    private ListView trackingLogsLists;

    private DnemActivity activity;
    private long activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityId = getIntent().getLongExtra("EXTRA_ACTIVITY_ID", 0);
        if (activityId == 0) {
            throw new IllegalStateException("A valid activity ID must be passed");
        }

        setContentView(R.layout.activity_view);

        // UI elements
        labelText = (TextView) findViewById(R.id.label_text);
        streakText = (TextView) findViewById(R.id.streak_text);
        detailsText = (TextView) findViewById(R.id.details_text);
        currentStreakText = (TextView) findViewById(R.id.current_streak_text);
        bestStreakText = (TextView) findViewById(R.id.best_streak_text);
        doneButton = findViewById(R.id.done_button);
        editButton = (ImageButton) findViewById(R.id.edit_button);
        trackingLogsLists = (ListView) findViewById(R.id.tracking_logs_list);
    }


    @Override
    protected void refreshDataset() {
        activity = ((DnemApplication) getApplicationContext()).getActivity(activityId);
    }

    @Override
    protected void updateUiElements() {
        if (getAdapter() == null) {
            setAdapter(new TrackingLogsAdapter(ViewActivity.this, activity.trackingLogs));
            trackingLogsLists.setAdapter(getAdapter());
        }

        labelText.setText(activity.getLabel());
        detailsText.setText(activity.getDetails());
        currentStreakText.setText("Current: " + activity.getCurrentStreak() + " days"); // todo use resource with placeholders
        bestStreakText.setText("Best: " + activity.getBestStreak() + " days");
        // todo refactor the following code with the duplicated one in ActivitiesAdapter
        boolean isDoneForToday = activity.isDoneForToday();
        int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
        doneButton.setBackground(getResources().getDrawable(doneButtonBackgroundId, null));
        doneButton.setOnClickListener(new OnDoneClickListener(ViewActivity.this, ViewActivity.this, activity));
        streakText.setText("" + activity.getCurrentStreak());

        editButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent editActivity = new Intent(ViewActivity.this, EditActivity.class);
                editActivity.putExtra(Constants.EXTRA_ACTIVITY_ID, activity.getId());
                startActivity(editActivity);
            }
        });
    }

}
