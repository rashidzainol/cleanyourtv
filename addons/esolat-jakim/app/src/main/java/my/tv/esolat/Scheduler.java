package my.tv.esolat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Schedules the next prayer time as an exact alarm, refetching the month when a new one starts.
 * Times arrive as UTC UNIX seconds from MPT; Malaysia is UTC+8 with no daylight saving, so the
 * conversion uses the Asia/Kuala_Lumpur zone rather than trusting the TV's clock setting.
 */
public final class Scheduler {
    private static final String TAG = "EsolatScheduler";
    public static final String ACTION_PRAYER = "my.tv.esolat.ACTION_PRAYER";
    public static final String ACTION_REFRESH = "my.tv.esolat.ACTION_REFRESH";
    public static final String EXTRA_NAME = "prayer_name";
    public static final String EXTRA_TIME = "prayer_time";
    public static final String EXTRA_PLACE = "place";

    private static final String[] NAMES = {"Subuh", "Syuruk", "Zohor", "Asar", "Maghrib", "Isyak"};

    private Scheduler() {
    }

    /** Malaysia's calendar now, whatever the TV's timezone is set to. */
    static Calendar malaysia() {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
    }

    /** The prayer's time of day in Malaysia as "HH:mm". */
    static String clockOf(long stampUtcSec) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(stampUtcSec * 1000L);
        Calendar my = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        my.setTimeInMillis(c.getTimeInMillis());
        return String.format(Locale.US, "%02d:%02d",
                my.get(Calendar.HOUR_OF_DAY), my.get(Calendar.MINUTE));
    }

    /** Schedules the next prayer alarm from the cached month, fetching first if needed. */
    public static void scheduleNext(final Context ctx) {
        if (!Prefs.reminders(ctx)) {
            cancel(ctx);
            return;
        }
        new Thread(() -> {
            try {
                Calendar now = malaysia();
                Api.Month m = Api.month(ctx, Prefs.mpt(ctx),
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
                long nowSec = now.getTimeInMillis() / 1000L;

                // Walk the month's days in order; the first prayer past now wins. The rows are in
                // date order, so tomorrow's Subuh is found after today's Isyak.
                long next = -1;
                int nextIdx = -1;
                outer:
                for (long[] day : m.times) {
                    for (int i = 0; i < Api.PRAYERS; i++) {
                        if (i == 1) continue; // Syuruk is shown but is not a prayer
                        if (day[i] > nowSec) {
                            next = day[i];
                            nextIdx = i;
                            break outer;
                        }
                    }
                }
                if (next < 0) {
                    // Every prayer this month has passed: refetch next month shortly after midnight.
                    scheduleRefresh(ctx, nextRefreshAt(now));
                    return;
                }
                String time = clockOf(next);
                Log.i(TAG, "Scheduling " + NAMES[nextIdx] + " " + time);
                Intent it = new Intent(ctx, EsolatReceiver.class);
                it.setAction(ACTION_PRAYER);
                it.putExtra(EXTRA_NAME, NAMES[nextIdx]);
                it.putExtra(EXTRA_TIME, time);
                it.putExtra(EXTRA_PLACE, m.place);
                setAlarm(ctx, next * 1000L, it, 1001);
            } catch (Exception e) {
                Log.w(TAG, "scheduleNext: " + e);
                scheduleRefresh(ctx, nextRefreshAt(malaysia()));
            }
        }).start();
    }

    /** 00:05 Malaysia time tonight, or tomorrow night if that has already passed. */
    private static long nextRefreshAt(Calendar now) {
        Calendar t = (Calendar) now.clone();
        t.set(Calendar.HOUR_OF_DAY, 0);
        t.set(Calendar.MINUTE, 5);
        t.set(Calendar.SECOND, 0);
        t.set(Calendar.MILLISECOND, 0);
        if (!t.after(now)) t.add(Calendar.DAY_OF_MONTH, 1);
        return t.getTimeInMillis();
    }

    private static void scheduleRefresh(Context ctx, long at) {
        Intent it = new Intent(ctx, EsolatReceiver.class);
        it.setAction(ACTION_REFRESH);
        setAlarm(ctx, at, it, 1002);
    }

    public static void cancel(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        for (int id : new int[]{1001, 1002}) {
            Intent it = new Intent(ctx, EsolatReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(ctx, id, it,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
    }

    private static void setAlarm(Context ctx, long at, Intent it, int id) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = PendingIntent.getBroadcast(ctx, id, it,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        } else if (Build.VERSION.SDK_INT >= 23) {
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            } catch (SecurityException e) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            }
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }
}
