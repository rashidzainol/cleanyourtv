package my.tv.esolat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

/** The azan reminder when a prayer time arrives: a notification and a small on-screen card. */
public final class Reminder {
    private static final String TAG = "EsolatReminder";
    private static final String CHANNEL_ID = "esolat_reminder";
    private static final int NOTIFICATION_ID = 1001;
    private static final int ACCENT = Color.parseColor("#2BB673");

    private Reminder() {
    }

    static void show(Context ctx, String name, String time, String place) {
        if (name == null) name = "Subuh";
        if (time == null) time = "";
        if (place == null) place = "";

        notify(ctx, name, time, place);

        try {
            Intent popup = new Intent(ctx, EsolatAlertActivity.class);
            popup.putExtra(Scheduler.EXTRA_NAME, name);
            popup.putExtra(Scheduler.EXTRA_TIME, time);
            popup.putExtra(Scheduler.EXTRA_PLACE, place);
            popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(popup);
        } catch (Exception e) {
            Log.w(TAG, "alert activity: " + e);
        }
    }

    private static void notify(Context ctx, String name, String time, String place) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        String notifyType = Prefs.reminderNotify(ctx);
        boolean sound = "sound".equals(notifyType);
        boolean vibrate = "vibrate".equals(notifyType) || sound;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, ctx.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            if (sound) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                AudioAttributes audio = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .build();
                ch.setSound(soundUri, audio);
            } else {
                ch.setSound(null, null);
            }
            ch.enableVibration(vibrate);
            ch.enableLights(true);
            ch.setLightColor(ACCENT);
            nm.createNotificationChannel(ch);
        }

        Intent main = new Intent(ctx, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, main,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder nb = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(ctx, CHANNEL_ID)
                : new Notification.Builder(ctx);
        nb.setContentTitle(ctx.getString(R.string.notification_title, name))
                .setContentText(ctx.getString(R.string.notification_body, name, time, place))
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_ALARM)
                .setColor(ACCENT);
        if (!sound && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            nb.setSound(null);
        }
        if (vibrate && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            nb.setVibrate(new long[]{0, 250, 250, 250});
        }
        nm.notify(NOTIFICATION_ID, nb.build());
    }
}
