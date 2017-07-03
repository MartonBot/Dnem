package com.martonbot.dnem;

import android.app.Application;

import java.util.List;

public class DnemApplication extends Application {

    private List<DnemActivity> activities = null;

    public List<DnemActivity> getActivities() {
        if (activities == null) {
            throw new IllegalStateException("The activities must be loaded before using them");
        }
        return activities;
    }

    public void loadActivities() {
        activities = DnemDataLoader.loadAll(this);
    }

}
