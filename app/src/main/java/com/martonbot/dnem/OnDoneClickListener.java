package com.martonbot.dnem;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;

public class OnDoneClickListener implements ImageButton.OnClickListener {

    private DnemActivity activity;
    private ImageButton doneButton;
    private Context context;
    private SQLiteDatabase db;

    public OnDoneClickListener(Context context, View itemView, DnemActivity activity) {
        this.activity = activity;
        this.context = context;
        this.db = new DnemDbHelper(context).getWritableDatabase();

        // UI elements
        this.doneButton = (ImageButton) itemView.findViewById(R.id.done_button);
    }

    @Override
    public void onClick(View v) {
        DnemTrackingLog trackingLog = activity.trackingLogs.get(0);
        boolean isDoneForToday = trackingLog.getDay().equals(Time.today());
        if (isDoneForToday) {
            long trackingLogId = trackingLog.getId();
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
            ConfirmUndoClickListener confirmUndoClickListener = new ConfirmUndoClickListener(trackingLogId);
            adBuilder.setMessage("Undo for today?").setPositiveButton("Yup", confirmUndoClickListener).setNegativeButton("Nope", confirmUndoClickListener).show();
        } else {
            // insert the tracking log
            DnemTrackingLog newTrackingLog = TrackingLogs.insert(db, activity.getId());
            activity.addNewTrackingLog(newTrackingLog);
            uiMarkButtonAsDone(true);
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
                    uiMarkButtonAsDone(false);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

    private void uiMarkButtonAsDone(boolean done) {
        int doneButtonBackgroundId = done ? R.drawable.background_button_done : R.drawable.background_button_not_done;
        doneButton.setBackground(context.getResources().getDrawable(doneButtonBackgroundId, null));
    }

}
