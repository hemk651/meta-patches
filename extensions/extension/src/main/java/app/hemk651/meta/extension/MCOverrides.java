/*
 * Meta Patches — MobileConfig overrides installer.
 *
 * Forked from InstaFL (https://github.com/mamiiblt/instafel) by
 * mamiiblt (Muhammed Ali Bulut), rebranded to Meta Patches.
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.meta.extension;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Installs the bundled MobileConfig overrides into Instagram's native
 * overrides file {@code <filesDir>/mobileconfig/mc_overrides.json}.
 *
 * Instagram loads this file at MobileConfig init through its overrides
 * loader (single-line UTF-8 JSON, keyed {@code "<metaId>:"} with values
 * {@code "<subflag>: <name>: <value>"}). The bundled payload (the curated
 * {@code config/mc_overrides.json}, embedded at build time as
 * {@link BuildConfig#MC_OVERRIDES_JSON}) unlocks the Settings-level
 * Developer options and the dedicated Employee options entry.
 *
 * The file is written atomically (temp file + rename) and only when its
 * content differs, so repeated launches stay cheap. The payload becomes
 * active on the next MobileConfig init — either picked up live by the
 * app's overrides-file callback or after an app restart.
 *
 * Never throws: any failure is logged and swallowed, it must not break
 * the app shell.
 */
public final class MCOverrides {

    private static final String TAG = "MetaPatchesMC";

    private static final String DIRECTORY = "mobileconfig";
    private static final String FILE_NAME = "mc_overrides.json";
    private static final String TEMP_SUFFIX = ".tmp";

    private static boolean attempted;

    /**
     * Installs the bundled overrides file. Safe to call multiple times;
     * only the first call does work.
     *
     * @param context any Context (the Application works); may be null
     */
    public static void install(Context context) {
        if (attempted || context == null) {
            return;
        }
        attempted = true;
        try {
            installInternal(context);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to install " + FILE_NAME, t);
        }
    }

    private static void installInternal(Context context) throws Exception {
        String content = BuildConfig.MC_OVERRIDES_JSON;
        if (content == null || content.isEmpty()) {
            Log.w(TAG, "No overrides payload bundled, skipping");
            return;
        }

        File filesDir = context.getFilesDir();
        if (filesDir == null) {
            return;
        }

        File directory = new File(filesDir, DIRECTORY);
        File target = new File(directory, FILE_NAME);

        if (target.isFile() && content.equals(readUtf8(target))) {
            return;
        }

        if (!directory.exists() && !directory.mkdirs()) {
            Log.w(TAG, "Could not create " + directory);
            return;
        }

        // Atomic write: temp file in the same directory, then rename.
        File temp = new File(directory, FILE_NAME + TEMP_SUFFIX);
        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.getFD().sync();
        }
        if (temp.renameTo(target)) {
            Log.i(TAG, "Installed " + FILE_NAME + " (" + content.length() + " chars)");
            return;
        }

        // Rename can fail across filesystems; fall back to a direct write.
        try (FileOutputStream out = new FileOutputStream(target)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.getFD().sync();
        }
        temp.delete();
        Log.i(TAG, "Installed " + FILE_NAME + " (direct write, " + content.length() + " chars)");
    }

    private static String readUtf8(File file) throws Exception {
        long length = file.length();
        if (length <= 0 || length > 1 << 20) {
            return "";
        }
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) length];
            int total = 0;
            while (total < buffer.length) {
                int read = in.read(buffer, total, buffer.length - total);
                if (read < 0) {
                    break;
                }
                total += read;
            }
            return new String(buffer, 0, total, StandardCharsets.UTF_8);
        }
    }

    private MCOverrides() {
    }
}
