package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This fork: the waktu solat line under the clock, for the JAKIM zone in config.xml (esolat_zone,
 * an MPT code like "wlp-0" or "sgr-1"), from MPT's mirror of JAKIM's e-solat data (mpt.i906.my).
 *
 * The month's rows are fetched on a background thread and kept in SharedPreferences, so the line
 * survives the TV being offline: it always shows the most recent fetch. Malaysia is UTC+8 with no
 * daylight saving, so the stamps are read in the Asia/Kuala_Lumpur zone rather than the TV's clock
 * setting. The line is compact: "Subuh 05:48 · Syuruk 06:56 · ...".
 */
final class EsolatTimes {
    private static final String TAG = "EsolatTimes";
    private static final String PREFS = "esolat_prefs";
    private static final int PRAYERS = 6;
    private static final String[] NAMES = {"Subuh", "Syuruk", "Zohor", "Asar", "Maghrib", "Isyak"};

    /** Malaysia's calendar now; the TV's timezone is ignored on purpose. */
    private static Calendar malaysia() {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
    }

    /** The prayer's time of day in Malaysia as "HH:mm". */
    private static String clockOf(long stampUtcSec) {
        Calendar c = malaysia();
        c.setTimeInMillis(stampUtcSec * 1000L);
        return String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    /** Today's line for the zone, from the network when fresh, otherwise the cache. */
    static String today(Context ctx, String mpt) {
        String[] times = load(ctx, mpt);
        if (times == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PRAYERS; i++) {
            if (i > 0) sb.append("  ·  ");
            sb.append(NAMES[i]).append(' ').append(times[i]);
        }
        return sb.toString();
    }

    /** The zone's six times for today, refreshing the cache in the background when it is another day's. */
    private static String[] load(Context ctx, String mpt) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String day = dayOf();
        String cacheKey = "times_" + mpt;
        String cachedDay = prefs.getString(cacheKey + "_day", null);
        String cachedJson = prefs.getString(cacheKey, null);

        // Serve the cache straight away; refresh in the background when it is stale or missing.
        if (!day.equals(cachedDay)) {
            final Context app = ctx.getApplicationContext();
            new Thread(() -> {
                try {
                    String body = fetch("https://mpt.i906.my/api/prayer/" + mpt
                            + "?year=" + malaysia().get(Calendar.YEAR)
                            + "&month=" + (malaysia().get(Calendar.MONTH) + 1));
                    prefs.edit().putString(cacheKey, body).putString(cacheKey + "_day", dayOf()).apply();
                } catch (Exception e) {
                    Log.w(TAG, "fetch: " + e);
                }
            }).start();
        }
        return cachedJson != null ? parse(cachedJson) : null;
    }

    /** The month JSON as today's six "HH:mm" strings, or null if the cache predates the format. */
    private static String[] parse(String body) {
        try {
            JSONObject data = new JSONObject(body).getJSONObject("data");
            JSONArray rows = data.getJSONArray("times");
            int dom = malaysia().get(Calendar.DAY_OF_MONTH);
            JSONArray row = rows.getJSONArray(Math.min(dom - 1, rows.length() - 1));
            String[] times = new String[PRAYERS];
            for (int i = 0; i < PRAYERS && i < row.length(); i++) times[i] = clockOf(row.getLong(i));
            return times;
        } catch (Exception e) {
            return null;
        }
    }

    private static String dayOf() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(malaysia().getTime());
    }

    private static String fetch(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestProperty("User-Agent", "cleanyourtv-esolat/1.0");
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }

    private EsolatTimes() {
    }
}
