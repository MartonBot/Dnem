package com.martonbot.dnem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TrackingLogsAdapter extends BaseAdapter {

    private List<DnemTrackingLog> trackingLogs;
    private Context context;

    public TrackingLogsAdapter(Context context, List<DnemTrackingLog> trackingLogs) {
        this.context = context;
        this.trackingLogs = trackingLogs;
    }

    @Override
    public int getCount() {
        return trackingLogs.size();
    }

    @Override
    public Object getItem(int position) {
        return trackingLogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return trackingLogs.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DnemTrackingLog trackingLog = trackingLogs.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.tracking_log_item, parent, false);
        }

        TextView dayText = (TextView) convertView.findViewById(R.id.date_text);
        TextView timestampText = (TextView) convertView.findViewById(R.id.timestamp_text);
        TextView timezoneText = (TextView) convertView.findViewById(R.id.timezone_text);
        TextView starText = (TextView) convertView.findViewById(R.id.star_counter);
        TextView runningStreakText = (TextView) convertView.findViewById(R.id.streak_counter);
        ImageView starView = (ImageView) convertView.findViewById(R.id.star_view);

        dayText.setText(trackingLog.getDay().toString());
        timestampText.setText(Long.toString(trackingLog.getTimestamp()));
        timezoneText.setText(trackingLog.getTimezone().getID());
        starText.setText("" + trackingLog.getStarCounter());
        runningStreakText.setText("" + trackingLog.getStreakCounter());
        int visibility = trackingLog.getStarCounter() >= 7 ? View.VISIBLE : View.INVISIBLE;
        starView.setVisibility(visibility);
        int starBackground = trackingLog.getStarCounter() == 28 ? R.drawable.ic_star_gold_24dp : R.drawable.ic_star_silver_24dp;
        starView.setBackground(context.getDrawable(starBackground));

        return convertView;
    }
}
