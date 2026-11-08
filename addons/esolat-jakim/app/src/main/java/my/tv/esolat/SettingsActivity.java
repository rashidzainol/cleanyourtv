package my.tv.esolat;

import android.app.Activity;
import android.content.Intent;
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
 * The on-TV settings: four rows — Lokasi automatik, Manual location, Peringatan azan, and the
 * copyright line. Everything works from the D-pad: Up/Down move, OK selects, Back returns.
 */
public class SettingsActivity extends Activity {
    private static final String TAG = "EsolatSettings";
    private static final int ROW_AUTO = 0;
    private static final int ROW_MANUAL = 1;
    private static final int ROW_REMINDERS = 2;
    private static final int ROW_COPYRIGHT = 3;

    private Typeface font;
    private int accent;
    private LinearLayout list;
    private int selected = ROW_AUTO;
    private TextView toast;

    // Manual-location sub-screen
    private boolean manualOpen;
    private int stateIdx = 0;
    private int zoneIdx = 0;
    private final List<Zones.Zone> stateZones = new ArrayList<>();

    // Reminders sub-screen
    private boolean remindersOpen;
    private boolean remOn = true;
    private int remMinutes = 0;
    private String remNotify = "sound";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        font = getResources().getFont(R.font.outfit);
        accent = getColor(R.color.accent);

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

        toast = text(15, 400, accent);
        toast.setPadding(0, dp(10), 0, 0);
        col.addView(toast);

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

    private void rebuild() {
        list.removeAllViews();
        if (manualOpen) {
            rebuildManual();
            return;
        }
        if (remindersOpen) {
            rebuildReminders();
            return;
        }

        list.addView(rowView(getString(R.string.settings_auto),
                getString(R.string.settings_auto_hint), selected == ROW_AUTO));
        list.addView(rowView(getString(R.string.settings_manual),
                getString(R.string.settings_manual_hint), selected == ROW_MANUAL));
        boolean on = Prefs.reminders(this);
        list.addView(rowView(getString(R.string.settings_reminders),
                on ? getString(R.string.settings_on) : getString(R.string.settings_off),
                selected == ROW_REMINDERS));
        list.addView(rowView(getString(R.string.settings_copyright),
                getString(R.string.settings_copyright_hint), selected == ROW_COPYRIGHT));
    }

    private void rebuildManual() {
        list.removeAllViews();
        loadZones();

        // Back row
        list.addView(rowView("← " + getString(R.string.settings_back), "", true));

        // Negeri
        list.addView(rowView(getString(R.string.settings_zone) + " · Negeri",
                Zones.STATES[stateIdx], selected == 1));

        // Zon
        Zones.Zone zone = stateZones.get(zoneIdx);
        list.addView(rowView(getString(R.string.settings_zone) + " · Zon",
                zone.titled(), selected == 2));

        // Save row
        list.addView(rowView("✓ " + getString(R.string.settings_save), "", selected == 3));

        // The zones of the chosen state
        for (int i = 0; i < stateZones.size(); i++) {
            Zones.Zone z = stateZones.get(i);
            list.addView(zoneView(z, i, selected == 4 + i));
        }
    }

    private void rebuildReminders() {
        list.removeAllViews();
        remOn = Prefs.reminders(this);
        remMinutes = Prefs.reminderMinutes(this);
        remNotify = Prefs.reminderNotify(this);

        list.addView(rowView("← " + getString(R.string.settings_back), "", true));

        list.addView(rowView(getString(R.string.settings_reminders),
                remOn ? getString(R.string.settings_on) : getString(R.string.settings_off),
                selected == 1));

        list.addView(rowView(getString(R.string.settings_remind_before),
                remMinutes + " minit", selected == 2));

        String notifyLabel;
        switch (remNotify) {
            case "sound": notifyLabel = "Bunyi"; break;
            case "vibrate": notifyLabel = "Getar"; break;
            case "silent": notifyLabel = "Diam"; break;
            default: notifyLabel = remNotify; break;
        }
        list.addView(rowView(getString(R.string.settings_notify),
                notifyLabel, selected == 3));
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

    private LinearLayout rowView(String label, String value, boolean focused) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(20), dp(10), dp(20), dp(10));
        row.setBackground(bgOf(focused, false));
        TextView l = text(12, 400, focused ? accent : 0x80FFFFFF);
        l.setLetterSpacing(0.1f);
        l.setText(label.toUpperCase(java.util.Locale.ROOT));
        row.addView(l);
        if (!value.isEmpty()) {
            TextView v = text(19, 500, focused ? 0xFFFFFFFF : 0xB3FFFFFF);
            v.setText(value);
            row.addView(v);
        }
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
        int rows;
        if (manualOpen) rows = 4 + stateZones.size();
        else if (remindersOpen) rows = 4;
        else rows = 4;
        selected = Math.max(0, Math.min(rows - 1, selected + delta));
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
                if (manualOpen && selected == 1) {
                    stateIdx = (stateIdx - 1 + Zones.STATES.length) % Zones.STATES.length;
                    loadZones();
                    rebuild();
                } else if (manualOpen && selected == 2) {
                    zoneIdx = (zoneIdx - 1 + stateZones.size()) % stateZones.size();
                    rebuild();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (manualOpen && selected == 1) {
                    stateIdx = (stateIdx + 1) % Zones.STATES.length;
                    loadZones();
                    rebuild();
                } else if (manualOpen && selected == 2) {
                    zoneIdx = (zoneIdx + 1) % stateZones.size();
                    rebuild();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                activate();
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (manualOpen) {
                    manualOpen = false;
                    selected = ROW_MANUAL;
                    rebuild();
                    return true;
                }
                if (remindersOpen) {
                    remindersOpen = false;
                    selected = ROW_REMINDERS;
                    rebuild();
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void activate() {
        if (manualOpen) {
            activateManual();
            return;
        }
        if (remindersOpen) {
            activateReminders();
            return;
        }

        switch (selected) {
            case ROW_AUTO:
                detect();
                break;
            case ROW_MANUAL:
                manualOpen = true;
                selected = 0;
                loadZones();
                rebuild();
                break;
            case ROW_REMINDERS:
                remindersOpen = true;
                selected = 0;
                rebuild();
                break;
            case ROW_COPYRIGHT:
                openCopyright();
                break;
        }
    }

    private void activateManual() {
        if (selected == 0) {
            manualOpen = false;
            selected = ROW_MANUAL;
            rebuild();
            return;
        }
        if (selected == 1) {
            stateIdx = (stateIdx + 1) % Zones.STATES.length;
            loadZones();
            rebuild();
            return;
        }
        if (selected == 2) {
            zoneIdx = (zoneIdx + 1) % stateZones.size();
            rebuild();
            return;
        }
        if (selected == 3) {
            // Save
            Zones.Zone z = stateZones.get(zoneIdx);
            apply(z);
            manualOpen = false;
            selected = ROW_MANUAL;
            rebuild();
            return;
        }
        // A zone row
        int i = selected - 4;
        if (i >= 0 && i < stateZones.size()) {
            zoneIdx = i;
            rebuild();
        }
    }

    private void activateReminders() {
        if (selected == 0) {
            remindersOpen = false;
            selected = ROW_REMINDERS;
            rebuild();
            return;
        }
        if (selected == 1) {
            Prefs.setReminders(this, !Prefs.reminders(this));
            rebuild();
            return;
        }
        if (selected == 2) {
            remMinutes = (remMinutes + 5) % 65; // 0, 5, 10, ..., 60
            Prefs.setReminderMinutes(this, remMinutes);
            rebuild();
            return;
        }
        if (selected == 3) {
            String[] opts = {"sound", "vibrate", "silent"};
            int idx = 0;
            for (int i = 0; i < opts.length; i++) {
                if (opts[i].equals(remNotify)) idx = i;
            }
            remNotify = opts[(idx + 1) % opts.length];
            Prefs.setReminderNotify(this, remNotify);
            rebuild();
            return;
        }
    }

    /** Saves the zone and shares it with the launcher (Home shows the same zone). */
    private void apply(Zones.Zone z) {
        Prefs.setMpt(this, z.mpt);
        SharedPrefs.shareZone(this, z.mpt);
        if (Prefs.reminders(this)) Scheduler.scheduleNext(this);
        else Scheduler.cancel(this);
        toast.setText(getString(R.string.settings_saved, z.jakim, z.desc.split(",")[0].trim()));
    }

    /** Lokasi automatik: the TV's IP is geocoded, the nearest zone is chosen. */
    private void detect() {
        if (detecting) return;
        detecting = true;
        toast.setText(getString(R.string.settings_detecting));
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
                    toast.setText(getString(R.string.settings_detect_failed));
                    return;
                }
                apply(found);
                toast.setText(getString(R.string.settings_detected, found.jakim,
                        found.desc.split(",")[0].trim()));
            });
        }).start();
    }

    private static double dist2(double a1, double o1, double a2, double o2) {
        double dA = a1 - a2, dO = o1 - o2;
        return dA * dA + dO * dO;
    }

    private void openCopyright() {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/rashidzainol"));
            startActivity(i);
        } catch (Exception e) {
            toast.setText("github.com/rashidzainol");
        }
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

    private boolean detecting;
}
