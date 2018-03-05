package com.martonbot.dnem;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;

import com.martonbot.dnem.activities.UpdatableActivity;

public class OnDoneClickListener implements ImageButton.OnClickListener {

    private DnemActivity activity;
    private UpdatableActivity updatableActivity;
    private Context context;
    private SQLiteDatabase db;

    // todo change parameter, pass adapter instead
    public OnDoneClickListener(Context context, UpdatableActivity updatableActivity, DnemActivity activity) {
        this.activity = activity;
        this.context = context;
        this.db = new DnemDbHelper(context).getWritableDatabase();

        // to update the activity's UI
        this.updatableActivity = updatableActivity;
    }

    @Override
    public void onClick(View v) {
        boolean isDoneForToday = activity.isDoneForToday();
        if (isDoneForToday) {
            DnemTrackingLog trackingLog = activity.trackingLogs.get(0);
            long trackingLogId = trackingLog.getId();
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
            ConfirmUndoClickListener confirmUndoClickListener = new ConfirmUndoClickListener(trackingLogId);
            adBuilder.setMessage("Undo for today?").setPositiveButton("Yup", confirmUndoClickListener).setNegativeButton("Nope", confirmUndoClickListener).show();
        } else {
            // insert the tracking log
            DnemTrackingLog newTrackingLog = TrackingLogs.insert(db, activity.getId());
            activity.addNewTrackingLog(newTrackingLog);
            if (updatableActivity != null) {
                updatableActivity.updateUI();
            }
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            vibrator.vibrate(50);
        }
    }

    // todo maybe get this class outside
    private class ConfirmUndoClickListener implements DialogInterface.OnClickListener {

        private long trackingLogId;

        public ConfirmUndoClickListener(long trackingLogId) {
            this.trackingLogId = trackingLogId;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    TrackingLogs.delete(db, trackingLogId);
                    activity.removeLatestTrackinglog();
                    if (updatableActivity != null) {
                        updatableActivity.updateUI();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

}
