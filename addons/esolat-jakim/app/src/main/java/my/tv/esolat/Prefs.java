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

    private Prefs() {
    }
}
