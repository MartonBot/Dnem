package com.martonbot.dnem.activities;

import android.app.Activity;
import android.widget.BaseAdapter;

/**
 * An instance of this activity displays some information that may be updated over time. For example, the ViewActivity should update when a new tracking log is added for the day, since it changes the streak, the color of the button and other interface elements.
 * The activity defines how to refresh its UI based on underlying data in the updateUi() method
 */
public abstract class UpdatableActivity extends Activity {

    private BaseAdapter adapter;

    public void updateUI() {
        updateUi();
        adapter.notifyDataSetChanged();
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

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
        // when an UpdatableActivity resumes, we refresh the data, then update the UI
        refreshData();
        updateUI();
    }

}
