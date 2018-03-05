package com.martonbot.dnem;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class ViewUpdater {

    public static void updateDoneButton(Context context, DnemActivity activity, TextView labelText, TextView detailsText, View doneButton, TextView streakText, View starImage) {
        labelText.setText(activity.getLabel());
        detailsText.setText(activity.getDetails());

        boolean isDoneForToday = activity.isDoneForToday();
        int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
        doneButton.setBackground(context.getResources().getDrawable(doneButtonBackgroundId, null));
        streakText.setText(String.format("%d", activity.getCurrentStreak()));

        int visibility = activity.getStarCounter() >= 7 ? View.VISIBLE : View.INVISIBLE;
        starImage.setVisibility(visibility);
        int starBackground = activity.getStarCounter() >= 28 ? R.drawable.ic_star_gold_24dp : R.drawable.ic_star_silver_24dp;
        starImage.setBackground(context.getDrawable(starBackground));
    }

}
