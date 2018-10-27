package com.martonbot.dnem.filters;

import com.martonbot.dnem.DnemActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mgrihangne on 13/10/2017.
 */

public abstract class DnemFilter {

    private boolean isOn;

    public void switchOn(boolean on) {
        isOn = on;
    }

    abstract boolean evaluate(DnemActivity activity);

    public List<DnemActivity> filter (List<DnemActivity> list) {
        if (!isOn) {
            return list;
        }
        List<DnemActivity> filteredList = new LinkedList<DnemActivity>();
        for (DnemActivity activity : list) {
            if (evaluate(activity)) {
                filteredList.add(activity);
            }
        }

        return filteredList;
    }

}
