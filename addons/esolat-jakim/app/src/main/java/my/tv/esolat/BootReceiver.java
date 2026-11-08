package my.tv.esolat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Comes back after a reboot or an update, so the reminders keep working. */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "EsolatBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "";
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            Log.i(TAG, "Rescheduling after " + action);
            Scheduler.scheduleNext(context);
        }
    }
}
