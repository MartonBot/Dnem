package com.martonbot.dnem.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.martonbot.dnem.database.DnemDatabase;
import com.martonbot.dnem.database.DnemDbHelper;
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

    public DnemList() {
        dnems = new LinkedList<>();
        filteredDnems = new LinkedList<>();
    }

    /**
     * Retrieve the list of dnems.
     *
     * @return
     */
    public List<Dnem> getDnems() {
        return dnems;
    }

    public void load(Context context) {
        loadDnemsFromDatabase(context);
        sortDnems();
    }

    /**
     * This method triggers the loading of all the Dnems and their tracking logs from the database.
     */
    private void loadDnemsFromDatabase(Context context) {
        SQLiteDatabase db = new DnemDbHelper(context).getReadableDatabase();

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
            Dnem dnem = new Dnem(dnemId);
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
    private void sortDnems() {
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

    public void delete(Context context, Dnem dnem) {
        long dnemId = dnem.getId();
        if (dnemId == 0) {
            throw new IllegalStateException("Illegal deletion of inexisting Dnem");
        }

        String dnemSelection = DnemDatabase.Activity._ID + " = ?";
        String[] dnemSelectionArgs = {
                "" + dnemId
        };

        SQLiteDatabase db = new DnemDbHelper(context).getWritableDatabase();

        db.delete(DnemDatabase.Activity.T_NAME,
                dnemSelection,
                dnemSelectionArgs);

        db.close();

        dnems.remove(dnem);
    }

    public void insert(Context context, Dnem dnem) {
        if (dnem.getId() != 0) {
            throw new IllegalStateException("Illegal insertion of existing Dnem");
        }

        boolean success = false;
        long newDnemId = 0;
        SQLiteDatabase db = new DnemDbHelper(context).getWritableDatabase();
        db.beginTransaction();
        try {

            newDnemId = db.insert(
                    DnemDatabase.Activity.T_NAME,
                    null,
                    buildDnemContentValuesFromDnem(dnem));
            ContentValues scheduleValues = buildScheduleContentValuesFromDnem(dnem);

            scheduleValues.put(DnemDatabase.Schedule.C_ACTIVITY_ID, newDnemId); // this is where we set the foreign key
            long scheduleId = db.insert(
                    DnemDatabase.Schedule.T_NAME,
                    null,
                    scheduleValues);

            if (newDnemId == -1 || scheduleId == -1) {
                throw new IllegalStateException("Inconsistent Dnem insertion");
            }
            db.setTransactionSuccessful();
            success = true;

        } catch (IllegalStateException e) {
            Toast.makeText(context, "Failed to insert the new Dnem into the database", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }

        if (success) {
            dnem.setId(newDnemId);
            dnems.add(dnem);
            sortDnems();
        }
    }

    public void update(Context context, Dnem dnem) {
        // todo can be refactored with the code in insert()
        long dnemId = dnem.getId();
        if (dnemId == 0) {
            throw new IllegalStateException("Illegal update of inexisting Dnem");
        }

        boolean success = false;

        String dnemSelection = DnemDatabase.Activity._ID + " = ?";
        String[] dnemSelectionArgs = {
                "" + dnemId
        };

        String scheduleSelection = DnemDatabase.Schedule.C_ACTIVITY_ID + " = ?";
        String[] scheduleSelectionArgs = {
                "" + dnemId
        };

        SQLiteDatabase db = new DnemDbHelper(context).getWritableDatabase();
        db.beginTransaction();
        try {
            int activityCount = db.update(
                    DnemDatabase.Activity.T_NAME,
                    buildDnemContentValuesFromDnem(dnem),
                    dnemSelection,
                    dnemSelectionArgs);

            int scheduleCount = db.update(
                    DnemDatabase.Schedule.T_NAME,
                    buildScheduleContentValuesFromDnem(dnem),
                    scheduleSelection,
                    scheduleSelectionArgs);

            if (activityCount != 1 || scheduleCount != 1) {
                throw new IllegalStateException("Illegal database state");
            }
            db.setTransactionSuccessful();
            success = true;
        } catch (IllegalStateException e) {
            Toast.makeText(context, "Failed to update the Dnem in the database.", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }

        if (success) {
            sortDnems();
        }

    }

    private ContentValues buildDnemContentValuesFromDnem(Dnem dnem) {
        ContentValues activityValues = new ContentValues();
        activityValues.put(DnemDatabase.Activity.C_LABEL, dnem.getLabel());
        activityValues.put(DnemDatabase.Activity.C_DETAILS, dnem.getDetails());
        return activityValues;
    }

    private ContentValues buildScheduleContentValuesFromDnem(Dnem dnem) {
        ContentValues scheduleValues = new ContentValues();
        scheduleValues.put(DnemDatabase.Schedule.C_IS_ACTIVE, dnem.isActive());
        scheduleValues.put(DnemDatabase.Schedule.C_ALLOW_STARS, dnem.allowStars());
        return scheduleValues;
    }
}
