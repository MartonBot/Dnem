package com.martonbot.dnem;

import android.app.Application;

import java.util.Collections;
import java.util.Comparator;
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
        // sort here?
        Collections.sort(activities, new Comparator<DnemActivity>() {
            @Override
            public int compare(DnemActivity a1, DnemActivity a2) {
                int result = 0;
                boolean done1 = a1.isDoneForToday();
                boolean done2 = a2.isDoneForToday();
                if (done1 == done2) {
                    // both are done or undone
                    result = a2.getCurrentStreak() - a1.getCurrentStreak();
                }
                else {
                    result = done1 ? 1 : -1;
                }
                return result;
            }
        });
    }

    public DnemActivity getActivity(long activityId) {
        DnemActivity activity = null;
        for (DnemActivity a : activities) {
            if (a.getId() == activityId) {
                activity = a;
                break;
            }
        }
        return activity;
    }

}