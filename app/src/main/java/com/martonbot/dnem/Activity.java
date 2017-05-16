package com.martonbot.dnem;

import android.content.ContentValues;

/**
 * Created by mgrihangne on 8/05/2017.
 */
public class Activity {

    private String label;

    public Activity(String label) {
        this.label = label;
    }

    public ContentValues toContent() {
        ContentValues values = new ContentValues();
        values.put(DnemContract.Activity.COLUMN_NAME_LABEL, label);
        return values;
    }

}
