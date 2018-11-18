package com.martonbot.dnem.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.martonbot.dnem.R;
import com.martonbot.dnem.activities.UpdatableActivity;
import com.martonbot.dnem.data.Dnem;
import com.martonbot.dnem.data.TrackingLog;

import java.util.List;

public class TrackingLogsAdapter extends BaseAdapter {

    private List<TrackingLog> trackingLogs;
    private Context context;
    private Dnem dnem;

    public TrackingLogsAdapter(UpdatableActivity updatableActivity, Dnem dnem) {
        this.context = updatableActivity;
        this.trackingLogs = dnem.trackingLogs;
        this.dnem = dnem;
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
        TrackingLog trackingLog = trackingLogs.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.tracking_log_item, parent, false);
        }

        TextView dayText = convertView.findViewById(R.id.date_text);
        TextView timestampText = convertView.findViewById(R.id.timestamp_text);
        TextView timezoneText = convertView.findViewById(R.id.timezone_text);
        TextView starText = convertView.findViewById(R.id.star_counter);
        TextView runningStreakText = convertView.findViewById(R.id.streak_counter);
        ImageView starImage = convertView.findViewById(R.id.star_image);

        dayText.setText(trackingLog.getDay().toString());
        timestampText.setText(String.format("%d", trackingLog.getTimestamp()));
        timezoneText.setText(trackingLog.getTimezone().getID());
        starText.setText(String.format("%d", trackingLog.getStarCounter()));
        runningStreakText.setText(String.format("%d", trackingLog.getStreakCounter()));
        int visibility = trackingLog.getStarCounter() >= 7 ? View.VISIBLE : View.INVISIBLE;
        starImage.setVisibility(visibility);
        int starBackground;
        if (trackingLog.getStarCounter() < 14) {
            starBackground = R.drawable.ic_star_bronze_24dp;
        } else if (trackingLog.getStarCounter() < 28) {
            starBackground = R.drawable.ic_star_silver_24dp;
        } else {
            starBackground = R.drawable.ic_star_gold_24dp;
        }
        starImage.setBackground(context.getDrawable(starBackground));

        convertView.setOnLongClickListener(new OnTrackingLogLongClickListener(trackingLog, dnem));

        return convertView;
    }

    private class OnTrackingLogLongClickListener implements View.OnLongClickListener {

        private TrackingLog trackingLog;
        private Dnem dnem;

        OnTrackingLogLongClickListener(TrackingLog trackingLog, Dnem dnem) {
            this.trackingLog = trackingLog;
            this.dnem = dnem;
        }

        @Override
        public boolean onLongClick(View view) {
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
            ConfirmDeleteClickListener confirmDeleteClickListener = new ConfirmDeleteClickListener(trackingLog, dnem);
            adBuilder.setMessage("Do you really want to delete this log? It cannot be undone.").setPositiveButton("Do it!", confirmDeleteClickListener).setNegativeButton("Never mind", confirmDeleteClickListener).show();
            return false;
        }
    }

    private class ConfirmDeleteClickListener implements DialogInterface.OnClickListener {

        private TrackingLog trackingLog;
        private Dnem dnem;

        ConfirmDeleteClickListener(TrackingLog trackingLog, Dnem dnem) {
            this.trackingLog = trackingLog;
            this.dnem = dnem;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    dnem.delete(context, trackingLog);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
