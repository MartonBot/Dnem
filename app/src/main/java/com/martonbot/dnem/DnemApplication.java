package com.martonbot.dnem;

import android.app.Application;

import com.martonbot.dnem.data.DnemList;

public class DnemApplication extends Application {

    private DnemList dnemList;

    public DnemList getDnemList() {
        if (dnemList == null) {
            dnemList = new DnemList();
        }
        return dnemList;
    }

}
