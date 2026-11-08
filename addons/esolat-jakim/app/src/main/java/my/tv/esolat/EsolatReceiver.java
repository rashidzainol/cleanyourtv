package my.tv.esolat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Alarm dispatch: shows the azan reminder and schedules the next prayer, or refetches the month. */
public class EsolatReceiver extends BroadcastReceiver {
    private static final String TAG = "EsolatReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        Log.i(TAG, "onReceive: " + action);
        if (Scheduler.ACTION_REFRESH.equals(action)) {
            // A new month: drop this month's cache so the fetch sees the new one.
            Scheduler.scheduleNext(context);
            return;
        }
        if (Scheduler.ACTION_PRAYER.equals(action)) {
            if (!Prefs.reminders(context)) {
                Scheduler.cancel(context);
                return;
            }
            String name = intent.getStringExtra(Scheduler.EXTRA_NAME);
            String time = intent.getStringExtra(Scheduler.EXTRA_TIME);
            String place = intent.getStringExtra(Scheduler.EXTRA_PLACE);
            Reminder.show(context, name, time, place);
            Scheduler.scheduleNext(context);
        }
    }
}
