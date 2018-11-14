package com.martonbot.dnem.filters;

import com.martonbot.dnem.Dnem;

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

    abstract boolean evaluate(Dnem activity);

    public List<Dnem> filter (List<Dnem> list) {
        if (!isOn) {
            return list;
        }
        List<Dnem> filteredList = new LinkedList<>();
        for (Dnem activity : list) {
            if (evaluate(activity)) {
                filteredList.add(activity);
            }
        }

        return filteredList;
    }

}
