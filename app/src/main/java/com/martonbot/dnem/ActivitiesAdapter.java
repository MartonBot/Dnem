package com.martonbot.dnem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.martonbot.dnem.activities.UpdatableActivity;

import java.util.List;

/**
 * An adapter for the list of Dnem activities to be displayed in the main activity
 */
public class ActivitiesAdapter extends BaseAdapter {

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
        TextView labelText = convertView.findViewById(R.id.label_text);
        TextView detailsText = convertView.findViewById(R.id.details_text);
        TextView streakText = convertView.findViewById(R.id.streak_text);
        View doneButton = convertView.findViewById(R.id.done_button);
        ImageView starImage = convertView.findViewById(R.id.star_image);

        ViewUpdater.updateDoneButton(context, activity, labelText, detailsText, doneButton, streakText, starImage);

        // click on the view
        convertView.setOnClickListener(new OnActivityClickListener(context, activity));

        doneButton.setOnClickListener(new OnDoneClickListener(context, updatableActivity, activity));

        return convertView;
    }

}
