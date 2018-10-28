package com.martonbot.dnem;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * This class is meant to provide methods to update the UI in multiple places across the app in a consistent way. For example, the "Done" button appears in both the Main activity (in the Dnem activities list) and in the View activity.
 */
public class UiUpdater {

    public static void updateDoneButton(Context context, DnemActivity activity, TextView labelText, TextView detailsText, View doneButton, TextView streakText, View starImage) {
        labelText.setText(activity.getLabel());
        detailsText.setText(activity.getDetails());

        boolean isDoneForToday = activity.isDoneForToday();
        int doneButtonBackgroundId = isDoneForToday ? R.drawable.background_button_done : R.drawable.background_button_not_done;
        doneButton.setBackground(context.getResources().getDrawable(doneButtonBackgroundId, null));
        streakText.setText(String.format("%d", activity.getCurrentStreak()));

        int visibility = activity.getStarCounter() >= 7 ? View.VISIBLE : View.INVISIBLE;
        starImage.setVisibility(visibility);
        int starBackground;
        if (activity.getStarCounter() < 14) {
            starBackground = R.drawable.ic_star_bronze_24dp;
        }
        else if (activity.getStarCounter() < 28) {
            starBackground = R.drawable.ic_star_silver_24dp;
        }
        else {
            starBackground = R.drawable.ic_star_gold_24dp;
        }
        starImage.setBackground(context.getDrawable(starBackground));
    }

}
