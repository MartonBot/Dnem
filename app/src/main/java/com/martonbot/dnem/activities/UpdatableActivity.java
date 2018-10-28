package com.martonbot.dnem.activities;

import android.app.Activity;
import android.widget.BaseAdapter;

import com.martonbot.dnem.DnemActivity;

/**
 * An instance of this activity displays some information that may be updated over time. For example, the ViewActivity should update when a new tracking log is added for the day, since it changes the streak, the color of the button and other interface elements.
 * The activity defines how to refresh its UI based on underlying data in the updateUi() method
 */
public abstract class UpdatableActivity extends Activity {

    /**
     * This method should be called in the onResume() method of an activity to trigger a complete refresh of the data and display the changes in the UI.
     */
    public void update() {
        refreshData();
        updateUi();
    }

    /**
     * This method can ba called from outside to notify the updatable activity to update itself. For example, when a new tracking log has been inserted in the database for a given Dnem activity, it should be reflected by changes in the activity UI.
     */
    public void updateForActivity(DnemActivity dnemActivity) {
        refreshActivityData(dnemActivity);
        updateUi();
    }

    /**
     * This method is overriden by subclasses to define how to load the data of a specific Dnem activity so that it is refreshed for the activity to display.
     */
    protected abstract void refreshActivityData(DnemActivity dnemActivity);

    /**
     * This method is overriden by subclasses to define how to load and prepare the data necessary for the activity to display it.
     */
    protected abstract void refreshData();

    /**
     * This method needs to be overriden by subclasses to define how UI elements need to be updated based on changes in the underlying data.
     */
    protected abstract void updateUi();

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

}
