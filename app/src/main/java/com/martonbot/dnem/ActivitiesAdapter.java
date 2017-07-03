package com.martonbot.dnem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.List;

public class  ActivitiesAdapter extends BaseAdapter {

    private List<DnemActivity> activities;
    private Context context;

    public ActivitiesAdapter(Context context, List<DnemActivity> activities) {
        this.context = context;
        this.activities = activities;
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

        TextView labelText = (TextView) convertView.findViewById(R.id.label_text);
        TextView detailsText = (TextView) convertView.findViewById(R.id.details_text);
        ImageButton doneButton = (ImageButton) convertView.findViewById(R.id.done_button);

        labelText.setText(activity.getLabel());
        detailsText.setText(activity.getDetails());

        // done button
        boolean isDoneForToday = false;
        if (activity.trackingLogs.size() > 0) {
            DnemTrackingLog mostRecentTrackingLog = activity.trackingLogs.get(0);
            DateTimeZone timezone = mostRecentTrackingLog.getTimezone();
            long timestamp = mostRecentTrackingLog.getTimestamp();
            isDoneForToday = new LocalDate(timestamp, timezone).equals(LocalDate.now()); // same day?
        }
        int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
        doneButton.setBackground(context.getResources().getDrawable(doneButtonBackgroundId, null));

        // click on the view
        convertView.setOnClickListener(new OnActivityClickListener(context, activity));

        doneButton.setOnClickListener(new OnDoneClickListener(context, convertView, activity));

        return convertView;
    }

}
