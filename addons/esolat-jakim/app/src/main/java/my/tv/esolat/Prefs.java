package my.tv.esolat;

import android.content.Context;
import android.content.SharedPreferences;

/** The zone and the reminder choice, kept in SharedPreferences; set on the TV in SettingsActivity. */
public final class Prefs {
    private static final String KEY_MPT = "zone_mpt";
    private static final String KEY_REMINDERS = "reminders";

    static SharedPreferences prefs(Context ctx) {
        return Api.prefs(ctx);
    }

    /** The MPT code of the chosen zone; WLY01 (Kuala Lumpur & Putrajaya) until it is changed. */
    public static String mpt(Context ctx) {
        return prefs(ctx).getString(KEY_MPT, Zones.DEFAULT_MPT);
    }

    public static void setMpt(Context ctx, String mpt) {
        prefs(ctx).edit().putString(KEY_MPT, mpt).apply();
    }

    /** Whether the azan reminder (notification + on-screen card) is on. On until it is turned off. */
    public static boolean reminders(Context ctx) {
        return prefs(ctx).getBoolean(KEY_REMINDERS, true);
    }

    public static void setReminders(Context ctx, boolean on) {
        prefs(ctx).edit().putBoolean(KEY_REMINDERS, on).apply();
    }

    /** Minutes before the prayer the reminder fires: 0 to 60, in steps of five. */
    public static int reminderMinutes(Context ctx) {
        return prefs(ctx).getInt("reminder_minutes", 0);
    }

    public static void setReminderMinutes(Context ctx, int minutes) {
        prefs(ctx).edit().putInt("reminder_minutes", minutes).apply();
    }

    /** The reminder's sound: "sound", "vibrate" or "silent". */
    public static String reminderNotify(Context ctx) {
        return prefs(ctx).getString("reminder_notify", "sound");
    }

    public static void setReminderNotify(Context ctx, String notify) {
        prefs(ctx).edit().putString("reminder_notify", notify).apply();
    }

    private Prefs() {
    }
}
