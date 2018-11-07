package com.martonbot.dnem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.martonbot.dnem.filters.Filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class DnemList {

    private List<DnemActivity> dnems;
    private List<DnemActivity> filteredDnems;

    DnemList(Context context) {
        dnems = new LinkedList<>();
        filteredDnems = new LinkedList<>();
        loadDnemsFromDb(context);
    }

    private void loadDnemsFromDb(Context context) {
        // todo do this in another thread :)
        SQLiteDatabase db = new DnemDbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(
                DnemDbHelper.activitiesTable,
                DnemDbHelper.activitiesProjectionIdOnly,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(DnemDatabase.Activity._ID);
            long dnemId = cursor.getLong(idIndex);
            DnemActivity activity = new DnemActivity(dnemId);
            dnems.add(activity);
        }
        cursor.close();
        db.close();

        // now we load each dnem
        for (DnemActivity dnem : dnems) {
            dnem.loadFromDb(context);
        }
    }

    public void sortDnems() {
        Collections.sort(dnems, new Comparator<DnemActivity>() {
            @Override
            public int compare(DnemActivity dnem1, DnemActivity dnem2) {
                int result = 0;
                boolean done1 = dnem1.isDoneForToday();
                boolean done2 = dnem2.isDoneForToday();
                if (done1 == done2) {
                    // both are done or undone
                    result = dnem2.getCurrentStreak() - dnem1.getCurrentStreak();
                } else {
                    result = done1 ? 1 : -1;
                }
                return result;
            }
        });
    }

    /**
     * Retrieve the list of dnems.
     *
     * @return
     */
    public List<DnemActivity> getDnems() {
        return dnems;
    }


    public List<DnemActivity> getFilteredDnems() {
        return filteredDnems;
    }

    public DnemActivity getDnem(long dnemId) {
        if (dnems == null) {
            throw new IllegalStateException("The dnems must be loaded before using them");
        }
        DnemActivity dnem = null;
        for (DnemActivity d : dnems) {
            if (d.getId() == dnemId) {
                dnem = d;
                break;
            }
        }
        return dnem;
    }

    public void applyFilters(List<Filter> filters) {
        filteredDnems.clear();
        for (DnemActivity dnem : dnems) {
            for (Filter filter : filters) {
                if (filter.evaluate(dnem)) {
                    filteredDnems.add(dnem);
                }
            }
        }
    }

    public void onDnemInserted(Context context, long dnemId) {
        DnemActivity activity = new DnemActivity(dnemId);
        dnems.add(activity);
        activity.loadFromDb(context);
    }

    public void onDnemDeleted(long activityId) {
        dnems.remove(getDnem(activityId));
    }

    public void onDnemUpdated(Context context, long dnemId) {
        DnemActivity dnem = getDnem(dnemId);
        dnem.loadFromDb(context);
    }

}
