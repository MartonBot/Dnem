package com.martonbot.dnem.listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;

import com.martonbot.dnem.activities.UpdatableActivity;
import com.martonbot.dnem.data.Dnem;
import com.martonbot.dnem.data.TrackingLog;

import org.joda.time.Instant;

public class OnDoneClickListener implements ImageButton.OnClickListener {

    private Dnem dnem;
    private UpdatableActivity updatableActivity;
    private Context context;

    // todo change parameter, pass adapter instead
    public OnDoneClickListener(Context context, UpdatableActivity updatableActivity, Dnem activity) {
        this.dnem = activity;
        this.context = context;

        // to update the activity's UI
        this.updatableActivity = updatableActivity;
    }

    @Override
    public void onClick(View v) {
        boolean isDoneForToday = dnem.isDoneForToday();
        if (isDoneForToday) {
            TrackingLog trackingLog = dnem.getTodayTrackingLog();
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
            ConfirmUndoClickListener confirmUndoClickListener = new ConfirmUndoClickListener(trackingLog);
            adBuilder.setMessage("Undo for today?").setPositiveButton("Yup", confirmUndoClickListener).setNegativeButton("Nope", confirmUndoClickListener).show();
        } else {
            // insert the tracking log
            TrackingLog trackingLog = new TrackingLog(dnem, Instant.now().getMillis());
            dnem.insert(context, trackingLog);

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 100 milliseconds
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));

        }
    }

    // todo maybe get this class outside
    private class ConfirmUndoClickListener implements DialogInterface.OnClickListener {

        private TrackingLog trackingLog;

        public ConfirmUndoClickListener(TrackingLog trackingLog) {
            this.trackingLog = trackingLog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    dnem.delete(context, trackingLog);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

}
