package com.martonbot.dnem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.List;

public class  ActivitiesAdapter extends BaseAdapter {

    private List<DnemActivity> activities;
    private Context context;
    private UpdatableActivity updatableActivity;

    public ActivitiesAdapter(Context context, UpdatableActivity updatableActivity, List<DnemActivity> activities) {
        this.context = context;
        this.activities = activities;
        this.updatableActivity = updatableActivity;
    }

    @Override
    public int getCount() {
        return activities.size();
    }

    @Override
    public Object getItem(int position) {
        return activities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return activities.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DnemActivity activity = activities.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }
        // todo this needs to be refactored
        TextView labelText = (TextView) convertView.findViewById(R.id.label_text);
        TextView detailsText = (TextView) convertView.findViewById(R.id.details_text);
        TextView streakText = (TextView) convertView.findViewById(R.id.streak_text);
        View doneButton = convertView.findViewById(R.id.done_button);
        ImageView starImage = (ImageView) convertView.findViewById(R.id.star_image);

        labelText.setText(activity.getLabel());
        detailsText.setText(activity.getDetails());

        // done button
        boolean isDoneForToday = activity.isDoneForToday();
        int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
        doneButton.setBackground(context.getResources().getDrawable(doneButtonBackgroundId, null));
        streakText.setText("" + activity.getCurrentStreak());

        // Silver or golden star
        int visibility = activity.getStarCounter() >= 7 ? View.VISIBLE : View.INVISIBLE;
        starImage.setVisibility(visibility);
        int starBackground = activity.getStarCounter() >= 28 ? R.drawable.ic_star_gold_24dp : R.drawable.ic_star_silver_24dp;
        starImage.setBackground(context.getDrawable(starBackground));


        // click on the view
        convertView.setOnClickListener(new OnActivityClickListener(context, activity));

        doneButton.setOnClickListener(new OnDoneClickListener(context, updatableActivity, activity));

        return convertView;
    }

}
