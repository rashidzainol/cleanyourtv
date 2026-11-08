package my.tv.esolat;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The month's times for one zone from MPT (https://mpt.i906.my), which serves JAKIM's e-solat data
 * as UNIX timestamps: times[day-1] = {Subuh, Syuruk, Zohor, Asar, Maghrib, Isyak} in seconds UTC.
 * The last successful response is kept in SharedPreferences so the TV shows something offline.
 */
public final class Api {
    public static final int PRAYERS = 6;

    public static final class Month {
        public final int year, month;
        public final long[][] times;
        public final String place;

        Month(int year, int month, long[][] times, String place) {
            this.year = year;
            this.month = month;
            this.times = times;
            this.place = place;
        }
    }

    private static final String PREFS = "esolat_prefs";

    private Api() {
    }

    public static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** This month, from the cache if it is there and fresh, otherwise the network. */
    public static Month month(Context ctx, String mpt, int year, int month) throws Exception {
        String key = "cache_" + mpt + "_" + year + "_" + month;
        String cached = prefs(ctx).getString(key, null);
        if (cached != null) {
            try {
                return parse(cached);
            } catch (Exception e) {
                // A cache nobody can read is no cache: fall through and refetch.
            }
        }
        String body = fetch("https://mpt.i906.my/api/prayer/" + mpt + "?year=" + year + "&month=" + month);
        Month m = parse(body);
        prefs(ctx).edit().putString(key, body).apply();
        return m;
    }

    static Month parse(String body) throws Exception {
        JSONObject data = new JSONObject(body).getJSONObject("data");
        JSONArray rows = data.getJSONArray("times");
        long[][] times = new long[rows.length()][];
        for (int d = 0; d < rows.length(); d++) {
            JSONArray row = rows.getJSONArray(d);
            times[d] = new long[PRAYERS];
            for (int i = 0; i < PRAYERS && i < row.length(); i++) {
                times[d][i] = row.getLong(i);
            }
        }
        return new Month(data.getInt("year"), data.getInt("month"), times,
                data.optString("place", ""));
    }

    private static String fetch(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestProperty("User-Agent", "EsolatJAKIM-AndroidTV/1.0");
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }
}
