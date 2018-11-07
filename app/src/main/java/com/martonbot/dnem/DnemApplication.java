package com.martonbot.dnem;

import android.app.Application;

public class DnemApplication extends Application {

    private DnemList dnemList;

    public DnemList getDnemList() {
        if (dnemList == null) {
            dnemList = new DnemList(DnemApplication.this);
        }
        return dnemList;
    }

}
