package my.tv.esolat;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * The on-TV settings: pick negeri, then zon (JAKIM code), and turn the azan reminder on or off.
 * Everything works from the D-pad: Up/Down move, OK selects, Back returns to the times.
 */
public class SettingsActivity extends Activity {
    private static final int ROW_STATE = 0;
    private static final int ROW_ZONE = 1;
    private static final int ROW_REMINDERS = 2;
    private static final int ROW_FIRST_ZONE = 3;

    private Typeface font;
    private int accent;
    private LinearLayout list;
    private int selected = ROW_STATE;
    private int stateIdx = 0;
    private int zoneIdx = 0;
    private final List<Zones.Zone> stateZones = new ArrayList<>();
    private TextView savedToast;

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
        col.setPadding(dp(120), dp(48), dp(120), dp(24));
        root.addView(col, new FrameLayout.LayoutParams(-1, -1));

        TextView title = text(30, 500, 0xFFFFFFFF);
        title.setLetterSpacing(0.15f);
        title.setText(getString(R.string.settings_title));
        col.addView(title);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, 0, 1f);
        slp.topMargin = dp(28);
        col.addView(scroll, slp);

        savedToast = text(16, 400, accent);
        savedToast.setPadding(0, dp(12), 0, 0);
        col.addView(savedToast);

        TextView hint = text(14, 400, 0x59FFFFFF);
        hint.setPadding(0, dp(10), 0, 0);
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
        row.setPadding(dp(24), dp(14), dp(24), dp(14));
        row.setBackground(bgOf(focused, false));
        TextView l = text(14, 400, focused ? accent : 0x80FFFFFF);
        l.setLetterSpacing(0.1f);
        l.setText(label.toUpperCase(java.util.Locale.ROOT));
        row.addView(l);
        TextView v = text(22, 500, focused ? 0xFFFFFFFF : 0xB3FFFFFF);
        v.setText(value);
        row.addView(v);
        return row;
    }

    private LinearLayout zoneView(Zones.Zone z, int idx, boolean focused) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(24), dp(12), dp(24), dp(12));
        boolean chosen = z.mpt.equals(Prefs.mpt(this));
        row.setBackground(bgOf(focused, chosen));
        TextView code = text(18, 500, chosen ? accent : (focused ? 0xFFFFFFFF : 0xB3FFFFFF));
        code.setText(z.jakim + " · " + z.state);
        row.addView(code);
        TextView desc = text(14, 400, focused ? 0xB3FFFFFF : 0x73FFFFFF);
        desc.setText(z.desc);
        row.addView(desc);
        return row;
    }

    private GradientDrawable bgOf(boolean focused, boolean chosen) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        if (focused) bg.setColor(0x1F2BB673);
        else if (chosen) bg.setColor(0x142BB673);
        else bg.setColor(0x0DFFFFFF);
        if (focused) bg.setStroke(dp(2), accent);
        return bg;
    }

    private void move(int delta) {
        int rows = ROW_FIRST_ZONE + stateZones.size();
        selected = Math.max(ROW_STATE, Math.min(rows - 1, selected + delta));
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
                Prefs.setMpt(this, stateZones.get(i).mpt);
                if (Prefs.reminders(this)) Scheduler.scheduleNext(this);
                else Scheduler.cancel(this);
            }
        }
        rebuild();
        Zones.Zone z = stateZones.get(zoneIdx);
        savedToast.setText(getString(R.string.settings_saved, z.jakim, z.desc.split(",")[0].trim()));
    }
}
