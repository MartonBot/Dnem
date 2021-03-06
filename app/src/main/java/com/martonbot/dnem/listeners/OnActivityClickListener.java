package com.martonbot.dnem.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.martonbot.dnem.Constants;
import com.martonbot.dnem.activities.ViewActivity;
import com.martonbot.dnem.data.Dnem;

/**
 * Created by mgrihangne on 3/07/2017.
 */

public class OnActivityClickListener implements View.OnClickListener {

    private Dnem activity;
    private Context context;

    public OnActivityClickListener(Context context, Dnem activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        Intent viewActivity = new Intent(context, ViewActivity.class);
        viewActivity.putExtra(Constants.EXTRA_ACTIVITY_ID, activity.getId());
        context.startActivity(viewActivity);
    }
}
