package com.martonbot.dnem.activities;

import android.app.Activity;

/**
 * An instance of this activity displays some information that may be updated over time. For example, the ViewActivity should update when a new tracking log is added for the day, since it changes the streak, the color of the button and other interface elements.
 * The activity defines how to refresh its UI based on underlying data in the updateUi() method.
 * Any subclass of UpdatableActivity will call its update() method in onResume().
 */
public abstract class UpdatableActivity extends Activity {

    /**
     * This method needs to be overriden by subclasses to define how UI elements need to be updated after the underlying data has changed.
     */
    public abstract void update();

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

}
