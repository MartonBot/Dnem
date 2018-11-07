package com.martonbot.dnem;

import android.content.Context;
import android.content.DialogInterface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;

import com.martonbot.dnem.activities.UpdatableActivity;

public class OnDoneClickListener implements ImageButton.OnClickListener {

    private DnemActivity dnem;
    private UpdatableActivity updatableActivity;
    private Context context;

    // todo change parameter, pass adapter instead
    public OnDoneClickListener(Context context, UpdatableActivity updatableActivity, DnemActivity activity) {
        this.dnem = activity;
        this.context = context;

        // to update the activity's UI
        this.updatableActivity = updatableActivity;
    }

    @Override
    public void onClick(View v) {
        boolean isDoneForToday = dnem.isDoneForToday();
        if (isDoneForToday) {
            DnemTrackingLog trackingLog = dnem.getTodayTrackingLog();
            long trackingLogId = trackingLog.getId();
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
            ConfirmUndoClickListener confirmUndoClickListener = new ConfirmUndoClickListener(trackingLogId);
            adBuilder.setMessage("Undo for today?").setPositiveButton("Yup", confirmUndoClickListener).setNegativeButton("Nope", confirmUndoClickListener).show();
        } else {
            // insert the tracking log
            TrackingLogs.insert(dnem, updatableActivity);
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 200 milliseconds
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));

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
                    TrackingLogs.delete(trackingLogId, dnem, updatableActivity);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

}
