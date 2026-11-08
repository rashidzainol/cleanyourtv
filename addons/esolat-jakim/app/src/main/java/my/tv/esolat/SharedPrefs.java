package my.tv.esolat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Shares the chosen zone with the launcher. SharedPreferences are private to this app, so the zone
 * is also written to the shared app-specific file the launcher's own card reads. On Android 10+
 * (API 29+) scoped storage keeps /sdcard/Android/data visible to both apps; below that the
 * WRITE_EXTERNAL_STORAGE permission (granted at install on TV) lets the launcher read it.
 */
final class SharedPrefs {
    private static final String TAG = "EsolatShared";
    static final String FILE_NAME = "esolat_zone.txt";

    private SharedPrefs() {
    }

    /** The file the launcher reads. Falls back to the app's own files dir when the shared one is gone. */
    static File sharedFile(Context ctx) {
        File dir = ctx.getExternalFilesDir(null);
        return new File(dir != null ? dir : ctx.getFilesDir(), FILE_NAME);
    }

    /** Writes the MPT code (e.g. "wlp-0") so the launcher's card shows the same zone. */
    static void shareZone(Context ctx, String mpt) {
        try (FileOutputStream out = new FileOutputStream(sharedFile(ctx))) {
            out.write(mpt.getBytes("UTF-8"));
        } catch (Exception e) {
            Log.w(TAG, "shareZone: " + e);
        }
    }
}
