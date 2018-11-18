package com.martonbot.dnem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.martonbot.dnem.R;
import com.martonbot.dnem.UiUpdater;
import com.martonbot.dnem.activities.UpdatableActivity;
import com.martonbot.dnem.data.Dnem;
import com.martonbot.dnem.listeners.OnActivityClickListener;
import com.martonbot.dnem.listeners.OnDoneClickListener;

import java.util.List;

/**
 * An adapter for the list of DnemDatabase dnems to be displayed in the main activity
 */
public class ActivitiesAdapter extends BaseAdapter {
    // todo rename to DnemsAdapter

    private List<Dnem> dnems;
    private Context context;
    private UpdatableActivity updatableActivity;

    public ActivitiesAdapter(Context context, UpdatableActivity updatableActivity, List<Dnem> dnems) {
        this.context = context;
        this.dnems = dnems;
        this.updatableActivity = updatableActivity;
    }

    @Override
    public int getCount() {
        return dnems.size();
    }

    @Override
    public Object getItem(int position) {
        return dnems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return dnems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Dnem activity = dnems.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }
        // todo this needs to be refactored with the code in UiUpdater? or does it?
        TextView labelText = convertView.findViewById(R.id.label_text);
        TextView detailsText = convertView.findViewById(R.id.details_text);
        TextView streakText = convertView.findViewById(R.id.streak_text);
        View doneButton = convertView.findViewById(R.id.done_button);
        ImageView starImage = convertView.findViewById(R.id.star_image);

        UiUpdater.updateDoneButton(context, activity, labelText, detailsText, doneButton, streakText, starImage);

        // click on the view
        convertView.setOnClickListener(new OnActivityClickListener(context, activity));

        doneButton.setOnClickListener(new OnDoneClickListener(context, updatableActivity, activity));

        return convertView;
    }

}
