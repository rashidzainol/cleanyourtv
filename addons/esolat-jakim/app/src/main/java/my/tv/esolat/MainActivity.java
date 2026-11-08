package my.tv.esolat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.res.ColorStateList;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Today's waktu solat for the chosen JAKIM zone, full screen on the TV, the next prayer highlighted
 * with a countdown. The zone and reminders are changed on the TV in SettingsActivity.
 */
public class MainActivity extends Activity {
    private static final String TAG = "EsolatMain";
    static final int[] NAME_RES = {
            R.string.prayer_fajr, R.string.prayer_sunrise, R.string.prayer_dhuhr,
            R.string.prayer_asr, R.string.prayer_maghrib, R.string.prayer_isha
    };
    private static final String[] ARABIC = {"الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"};
    private static final int[] ICONS = {
            R.drawable.ic_fajr, R.drawable.ic_sunrise, R.drawable.ic_dhuhr,
            R.drawable.ic_asr, R.drawable.ic_maghrib, R.drawable.ic_isha
    };
    private static final long MINUTE_MS = 60_000L;

    private final Handler main = new Handler(Looper.getMainLooper());
    private Typeface font;
    private int accent;
    private TextView place, next, dates, status;
    private TextView settingsBtn;
    private LinearLayout rows;
    /** Today's times as "HH:mm", in NAME_RES order; null until loaded. */
    private String[] times;
    /** Today's times in minutes after midnight, Malaysia. */
    private int[] minutes;
    private String zoneName = "";
    private String loadedFor;
    private boolean loading;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!today().equals(loadedFor)) load();
            else render();
            main.postDelayed(this, MINUTE_MS);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        font = getResources().getFont(R.font.outfit);
        accent = getColor(R.color.accent);

        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{(accent & 0x00FFFFFF) | 0x55000000, 0xFF050607, 0xFF050607}));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(80), dp(48), dp(80), dp(40));
        root.addView(col, new FrameLayout.LayoutParams(-1, -1));

        place = text(16, 400, 0xB3FFFFFF);
        place.setLetterSpacing(0.25f);
        col.addView(place);

        next = text(44, 300, 0xFFFFFFFF);
        next.setPadding(0, dp(8), 0, dp(2));
        col.addView(next);
        dates = text(18, 400, 0xB3FFFFFF);
        col.addView(dates);

        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, 0, 1f);
        rlp.setMargins(0, dp(24), 0, 0);
        col.addView(rows, rlp);

        status = text(14, 400, 0x80FFFFFF);
        status.setPadding(0, dp(14), 0, 0);
        status.setText(getString(R.string.loading));
        col.addView(status);

        // Settings button at the bottom
        settingsBtn = new TextView(this);
        settingsBtn.setText(getString(R.string.settings_title));
        settingsBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        settingsBtn.setTextColor(0xB3FFFFFF);
        settingsBtn.setTypeface(Build.VERSION.SDK_INT >= 28 ? Typeface.create(font, 500, false) : font);
        settingsBtn.setGravity(Gravity.CENTER);
        settingsBtn.setPadding(dp(24), dp(12), dp(24), dp(12));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(dp(10));
        btnBg.setColor(0x14FFFFFF);
        settingsBtn.setBackground(btnBg);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-2, -2);
        blp.setMargins(0, dp(16), 0, 0);
        blp.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(settingsBtn, blp);
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        main.postDelayed(tick, MINUTE_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        main.removeCallbacks(tick);
    }

    private static String today() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(c.getTime());
    }

    private void load() {
        if (loading) return;
        loading = true;
        final String day = today();
        final String mpt = Prefs.mpt(this);
        new Thread(() -> {
            Api.Month m = null;
            try {
                Calendar now = Scheduler.malaysia();
                m = Api.month(this, mpt, now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
            } catch (Exception e) {
                Log.w(TAG, "fetch: " + e);
            }
            final Api.Month result = m;
            main.post(() -> {
                loading = false;
                if (result == null) {
                    status.setText(getString(R.string.offline));
                    return;
                }
                try {
                    Calendar now = Scheduler.malaysia();
                    int dom = now.get(Calendar.DAY_OF_MONTH);
                    long[] stamps = dom <= result.times.length ? result.times[dom - 1] : result.times[0];
                    String[] s = new String[Api.PRAYERS];
                    int[] mins = new int[Api.PRAYERS];
                    for (int i = 0; i < Api.PRAYERS; i++) {
                        s[i] = Scheduler.clockOf(stamps[i]);
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
                        c.setTimeInMillis(stamps[i] * 1000L);
                        mins[i] = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
                    }
                    times = s;
                    minutes = mins;
                    Zones.Zone zone = Zones.find(mpt);
                    zoneName = zone != null ? zone.titled() : result.place;
                    loadedFor = day;
                    if (Prefs.reminders(this)) {
                        Scheduler.scheduleNext(MainActivity.this);
                    } else {
                        Scheduler.cancel(MainActivity.this);
                    }
                    render();
                } catch (Exception e) {
                    Log.w(TAG, "parse: " + e);
                    status.setText(getString(R.string.unexpected_error));
                }
            });
        }).start();
    }

    private void render() {
        if (times == null) return;
        place.setText(zoneName.toUpperCase(Locale.ROOT));
        dates.setText(new SimpleDateFormat("EEEE d MMMM", Locale.ENGLISH).format(new Date()));

        Calendar c = Scheduler.malaysia();
        int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        int nextIdx = -1;
        for (int i = 0; i < Api.PRAYERS; i++) {
            if (i == 1) continue;   // Syuruk is shown but is not a prayer
            if (minutes[i] > now) { nextIdx = i; break; }
        }
        if (nextIdx >= 0) {
            int left = minutes[nextIdx] - now;
            String timeStr = (left >= 60 ? left / 60 + " j " : "") + left % 60 + " min";
            next.setText(getString(R.string.prayer_in, getString(NAME_RES[nextIdx]), timeStr));
        } else {
            next.setText(getString(R.string.prayer_tomorrow, getString(NAME_RES[0])));
        }

        rows.removeAllViews();
        for (int i = 0; i < Api.PRAYERS; i++) {
            boolean hl = i == nextIdx;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(6), dp(14), dp(6), dp(14));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(16));
            bg.setColor(hl ? accent : 0x14FFFFFF);
            card.setBackground(bg);
            int dim = i == 1 ? 0x80FFFFFF : 0xFFFFFFFF;
            int fg = hl ? 0xFFFFFFFF : dim;
            ImageView icon = new ImageView(this);
            icon.setImageResource(ICONS[i]);
            icon.setImageTintList(ColorStateList.valueOf(hl ? 0xFFFFFFFF : (i == 1 ? 0x80FFFFFF : accent)));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(36), dp(36));
            ilp.bottomMargin = dp(16);
            card.addView(icon, ilp);
            TextView ar = centered(text(22, 400, fg));
            ar.setText(ARABIC[i]);
            card.addView(ar);
            TextView name = centered(text(18, 500, hl ? 0xE6FFFFFF : (i == 1 ? 0x80FFFFFF : 0xB3FFFFFF)));
            name.setText(getString(NAME_RES[i]));
            name.setPadding(0, dp(6), 0, dp(14));
            card.addView(name);
            TextView time = centered(text(36, hl ? 500 : 300, fg));
            time.setText(times[i]);
            // Shrink rather than cut "05:10" when the card is narrow.
            time.setMaxLines(1);
            time.setAutoSizeTextTypeUniformWithConfiguration(18, 36, 1, TypedValue.COMPLEX_UNIT_SP);
            card.addView(time, new LinearLayout.LayoutParams(-1, dp(44)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            lp.setMargins(i == 0 ? 0 : dp(8), 0, i == Api.PRAYERS - 1 ? 0 : dp(8), 0);
            rows.addView(card, lp);
        }
        String updated = getString(R.string.updated_at,
                new SimpleDateFormat("HH:mm", Locale.ROOT).format(new Date()));
        String statusStr = "JAKIM · e-solat (MPT) · " + updated;
        if (Prefs.reminders(this)) {
            statusStr += " · " + getString(R.string.reminders_active);
        }
        status.setText(statusStr);
    }

    /** Opens the zone and reminder settings; bound to the centre (OK) button. */
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** Centred on its own line, whatever the script direction. */
    private static TextView centered(TextView t) {
        t.setGravity(Gravity.CENTER);
        t.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        return t;
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
}
