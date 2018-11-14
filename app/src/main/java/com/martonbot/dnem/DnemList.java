package com.martonbot.dnem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.martonbot.dnem.filters.Filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a singleton class representing the core data of the application: a list of Dnems. Each Dnem has its own set of tracking logs.
 */
public class DnemList {

    private List<Dnem> dnems;
    private List<Dnem> filteredDnems;
    private DnemApplication applicationContext;

    DnemList(Context context) {
        this.applicationContext = (DnemApplication) context.getApplicationContext();
        dnems = new LinkedList<>();
        filteredDnems = new LinkedList<>();
        loadDnemsFromDatabase();
    }

    /**
     * Retrieve the list of dnems.
     * @return
     */
    public List<Dnem> getDnems() {
        return dnems;
    }

    /**
     * This method triggers the loading of all the Dnems and their tracking logs from the database.
     */
    private void loadDnemsFromDatabase() {
        SQLiteDatabase db = new DnemDbHelper(applicationContext).getReadableDatabase();

        // todo do this in another thread :)
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
            Dnem dnem = new Dnem(dnemId, applicationContext);
            dnems.add(dnem);
        }
        cursor.close();

        // now we load each dnem
        for (Dnem dnem : dnems) {
            dnem.loadFromDatabase(db);
        }

        db.close();
    }

    /**
     * This method sorts the Dnems in the list, according to the rules implemented in the comparator.
     */
    public void sortDnems() {
        Collections.sort(dnems, new Comparator<Dnem>() {
            @Override
            public int compare(Dnem dnem1, Dnem dnem2) {
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

    public List<Dnem> getFilteredDnems() {
        return filteredDnems;
    }

    public Dnem getDnem(long dnemId) {
        Dnem dnem = null;
        for (Dnem d : dnems) {
            if (d.getId() == dnemId) {
                dnem = d;
                break;
            }
        }
        return dnem;
    }

    public void applyFilters(List<Filter> filters) {
        filteredDnems.clear();
        for (Dnem dnem : dnems) {
            boolean pass = true;
            for (Filter filter : filters) {
                if (!filter.evaluate(dnem)) {
                    pass = false;
                }
            }
            if (pass) {
                filteredDnems.add(dnem);
            }
        }
    }

    /**
     * Callback method to notify that a new Dnem has been inserted in the database so that the DnemList can update itself to reflect that.
     * @param dnemId
     */
    public void onDnemInserted(long dnemId) {
        Dnem dnem = new Dnem(dnemId, applicationContext);
        dnems.add(dnem);
        dnem.loadFromDatabase();
    }

    /**
     * Callback method to notify that a new Dnem has been deleted from the database so that the DnemList can update itself to reflect that.
     * @param dnemId
     */
    public void onDnemDeleted(long dnemId) {
        dnems.remove(getDnem(dnemId));
    }

    /**
     *      * Callback method to notify that a new Dnem has been updated in the database so that the DnemList can update itself to reflect that.
     * @param dnemId
     */
    public void onDnemUpdated(long dnemId) {
        Dnem dnem = getDnem(dnemId);
        dnem.loadFromDatabase();
    }

}
