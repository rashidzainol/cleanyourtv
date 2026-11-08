package my.tv.esolat;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The on-TV settings: lokasi automatik (from the TV's internet), or pick negeri then zon by hand,
 * and turn the azan reminder on or off. Everything works from the D-pad: Up/Down move, OK selects,
 * Back returns to the times.
 */
public class SettingsActivity extends Activity {
    private static final String TAG = "EsolatSettings";
    private static final int ROW_AUTO = 0;
    private static final int ROW_STATE = 1;
    private static final int ROW_ZONE = 2;
    private static final int ROW_REMINDERS = 3;
    private static final int ROW_FIRST_ZONE = 4;

    private Typeface font;
    private int accent;
    private LinearLayout list;
    private int selected = ROW_AUTO;
    private int stateIdx = 0;
    private int zoneIdx = 0;
    private final List<Zones.Zone> stateZones = new ArrayList<>();
    private TextView savedToast;
    private boolean detecting;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        font = getResources().getFont(R.font.outfit);
        accent = getColor(R.color.accent);

        // Where we start: the state and zone already chosen.
        String current = Prefs.mpt(this);
        for (int s = 0; s < Zones.STATES.length; s++) {
            for (Zones.Zone z : Zones.ZONES) {
                if (z.state.equals(Zones.STATES[s]) && z.mpt.equals(current)) {
                    stateIdx = s;
                }
            }
        }
        loadZones();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF050607);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(80), dp(36), dp(80), dp(20));
        root.addView(col, new FrameLayout.LayoutParams(-1, -1));

        TextView title = text(24, 500, 0xFFFFFFFF);
        title.setLetterSpacing(0.15f);
        title.setText(getString(R.string.settings_title));
        col.addView(title);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, 0, 1f);
        slp.topMargin = dp(20);
        col.addView(scroll, slp);

        savedToast = text(15, 400, accent);
        savedToast.setPadding(0, dp(10), 0, 0);
        col.addView(savedToast);

        TextView hint = text(13, 400, 0x59FFFFFF);
        hint.setPadding(0, dp(8), 0, 0);
        hint.setText(getString(R.string.settings_hint));
        col.addView(hint);

        setContentView(root);
        rebuild();
    }

    private TextView text(float sp, int weight, int color) {
        TextView t = new TextView(this);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setIncludeFontPadding(false);
        t.setTypeface(Build.VERSION.SDK_INT >= 28 ? Typeface.create(font, weight, false) : font);
        return t;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void loadZones() {
        stateZones.clear();
        String state = Zones.STATES[stateIdx];
        for (Zones.Zone z : Zones.ZONES) {
            if (z.state.equals(state)) stateZones.add(z);
        }
        if (stateZones.isEmpty()) stateZones.add(Zones.ZONES[0]);
        String current = Prefs.mpt(this);
        zoneIdx = 0;
        for (int i = 0; i < stateZones.size(); i++) {
            if (stateZones.get(i).mpt.equals(current)) {
                zoneIdx = i;
                break;
            }
        }
    }

    private void rebuild() {
        list.removeAllViews();

        // Lokasi automatik
        list.addView(rowView(ROW_AUTO, getString(R.string.settings_auto),
                getString(R.string.settings_auto_hint), selected == ROW_AUTO));

        // Negeri
        list.addView(rowView(ROW_STATE, getString(R.string.settings_zone) + " · Negeri",
                Zones.STATES[stateIdx], selected == ROW_STATE));

        // Zon
        Zones.Zone zone = stateZones.get(zoneIdx);
        list.addView(rowView(ROW_ZONE, getString(R.string.settings_zone) + " · Zon",
                zone.titled(), selected == ROW_ZONE));

        // Peringatan
        boolean on = Prefs.reminders(this);
        list.addView(rowView(ROW_REMINDERS, getString(R.string.settings_reminders),
                on ? getString(R.string.settings_on) : getString(R.string.settings_off),
                selected == ROW_REMINDERS));

        // The zones of the chosen state, one row each
        for (int i = 0; i < stateZones.size(); i++) {
            Zones.Zone z = stateZones.get(i);
            list.addView(zoneView(z, i, selected == ROW_FIRST_ZONE + i));
        }
    }

    private LinearLayout rowView(int tag, String label, String value, boolean focused) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(20), dp(10), dp(20), dp(10));
        row.setBackground(bgOf(focused, false));
        TextView l = text(12, 400, focused ? accent : 0x80FFFFFF);
        l.setLetterSpacing(0.1f);
        l.setText(label.toUpperCase(java.util.Locale.ROOT));
        row.addView(l);
        TextView v = text(19, 500, focused ? 0xFFFFFFFF : 0xB3FFFFFF);
        v.setText(value);
        row.addView(v);
        return row;
    }

    private LinearLayout zoneView(Zones.Zone z, int idx, boolean focused) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(20), dp(9), dp(20), dp(9));
        boolean chosen = z.mpt.equals(Prefs.mpt(this));
        row.setBackground(bgOf(focused, chosen));
        TextView code = text(16, 500, chosen ? accent : (focused ? 0xFFFFFFFF : 0xB3FFFFFF));
        code.setText(z.jakim + " · " + z.state);
        row.addView(code);
        TextView desc = text(13, 400, focused ? 0xB3FFFFFF : 0x73FFFFFF);
        desc.setText(z.desc);
        row.addView(desc);
        return row;
    }

    private GradientDrawable bgOf(boolean focused, boolean chosen) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(10));
        if (focused) bg.setColor(0x1F2BB673);
        else if (chosen) bg.setColor(0x142BB673);
        else bg.setColor(0x0DFFFFFF);
        if (focused) bg.setStroke(dp(2), accent);
        return bg;
    }

    private void move(int delta) {
        int rows = ROW_FIRST_ZONE + stateZones.size();
        selected = Math.max(ROW_AUTO, Math.min(rows - 1, selected + delta));
        rebuild();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                move(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                move(1);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (selected == ROW_STATE) {
                    stateIdx = (stateIdx - 1 + Zones.STATES.length) % Zones.STATES.length;
                    loadZones();
                    rebuild();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (selected == ROW_STATE) {
                    stateIdx = (stateIdx + 1) % Zones.STATES.length;
                    loadZones();
                    rebuild();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                activate();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void activate() {
        if (selected == ROW_AUTO) {
            detect();
            return;
        }
        if (selected == ROW_STATE) {
            stateIdx = (stateIdx + 1) % Zones.STATES.length;
            loadZones();
        } else if (selected == ROW_ZONE) {
            zoneIdx = (zoneIdx + 1) % stateZones.size();
        } else if (selected == ROW_REMINDERS) {
            Prefs.setReminders(this, !Prefs.reminders(this));
        } else {
            int i = selected - ROW_FIRST_ZONE;
            if (i >= 0 && i < stateZones.size()) {
                zoneIdx = i;
                apply(stateZones.get(i));
            }
        }
        rebuild();
    }

    /** Saves the zone and shares it with the launcher (Home shows the same zone). */
    private void apply(Zones.Zone z) {
        Prefs.setMpt(this, z.mpt);
        SharedPrefs.shareZone(this, z.mpt);
        if (Prefs.reminders(this)) Scheduler.scheduleNext(this);
        else Scheduler.cancel(this);
        savedToast.setText(getString(R.string.settings_saved, z.jakim, z.desc.split(",")[0].trim()));
    }

    /** Lokasi automatik: the TV's IP is geocoded, the nearest zone is chosen. */
    private void detect() {
        if (detecting) return;
        detecting = true;
        savedToast.setText(getString(R.string.settings_detecting));
        new Thread(() -> {
            Zones.Zone best = null;
            try {
                JSONObject loc = new JSONObject(fetch("http://ip-api.com/json"));
                double lat = loc.getDouble("lat");
                double lon = loc.getDouble("lon");
                double bestDist = Double.MAX_VALUE;
                for (Zones.Zone z : Zones.ZONES) {
                    double d = dist2(lat, lon, z.lat, z.lon);
                    if (d < bestDist) {
                        bestDist = d;
                        best = z;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "detect: " + e);
            }
            final Zones.Zone found = best;
            runOnUiThread(() -> {
                detecting = false;
                if (found == null) {
                    savedToast.setText(getString(R.string.settings_detect_failed));
                    return;
                }
                apply(found);
                // Move the picker to what was detected, so manual still agrees.
                for (int s = 0; s < Zones.STATES.length; s++) {
                    if (Zones.STATES[s].equals(found.state)) stateIdx = s;
                }
                loadZones();
                for (int i = 0; i < stateZones.size(); i++) {
                    if (stateZones.get(i).mpt.equals(found.mpt)) zoneIdx = i;
                }
                rebuild();
                savedToast.setText(getString(R.string.settings_detected, found.jakim,
                        found.desc.split(",")[0].trim()));
            });
        }).start();
    }

    private static double dist2(double a1, double o1, double a2, double o2) {
        double dA = a1 - a2, dO = o1 - o2;
        return dA * dA + dO * dO;
    }

    private static String fetch(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
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
