package app.meta.extension;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Writes a MobileConfig overrides file into the host app's storage as early as possible.
 *
 * <p>Meta apps read {@code mc_overrides.json} from their internal {@code filesDir/mobileconfig}
 * directory when initializing MobileConfig. Placing the file there (and in the well-known
 * external variants) unlocks the internal Employee Options / developer options menu.
 *
 * <p>Every failure is swallowed: this code must never crash the host app, worst case the
 * file is simply not written and the app runs unpatched.
 */
public final class MetaConfigOverrides {

    /** Default overrides JSON. Byte-for-byte identical to the value documented in the README. */
    public static final String DEFAULT_OVERRIDES_JSON =
            "{\"56474:\":[\"0: : true\"],\"28538:\":[\"0: : true\"],\"32432:\":[\"1: : true\"],\"_qe_overrides_\":[]}";

    private static final String TAG = "MetaConfigOverrides";
    private static final String FILE_NAME = "mc_overrides.json";
    private static final String DIR_NAME = "mobileconfig";

    /** Startup runs once per process; no need to rewrite the files again afterwards. */
    private static volatile boolean alreadyRan = false;

    private MetaConfigOverrides() {
    }

    /**
     * Entry point invoked from the patched Application class at startup.
     *
     * @param context        any app context (the Application instance or its base context)
     * @param overridesJson  the JSON to write; blank values fall back to {@link #DEFAULT_OVERRIDES_JSON}
     */
    public static void writeOverrides(Context context, String overridesJson) {
        if (alreadyRan) {
            return;
        }
        alreadyRan = true;

        final String json = overridesJson == null || overridesJson.trim().isEmpty()
                ? DEFAULT_OVERRIDES_JSON
                : overridesJson.trim();

        try {
            final List<File> directories = collectTargetDirectories(context);
            for (File directory : directories) {
                writeFileSafely(new File(directory, FILE_NAME), json);
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Could not write " + FILE_NAME, throwable);
        }
    }

    /**
     * Collects every directory known to (or observed to be read by) Meta MobileConfig:
     *
     * <ol>
     *     <li>{@code <filesDir>/mobileconfig} — the canonical internal location.</li>
     *     <li>{@code <externalFilesDir>/mobileconfig} for every mounted storage volume.</li>
     *     <li>{@code <storageRoot>/data/files/mobileconfig} for every mounted storage volume —
     *     the layout reported on devices where MobileConfig reads from the volume root.
     *     Writes there usually fail on scoped storage, which is harmless.</li>
     * </ol>
     */
    private static List<File> collectTargetDirectories(Context context) {
        final List<File> directories = new ArrayList<>();

        try {
            final File filesDir = context.getFilesDir();
            if (filesDir != null) {
                addDirectory(directories, new File(filesDir, DIR_NAME));
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "filesDir unavailable", throwable);
        }

        try {
            final String packageName = context.getPackageName();
            for (File externalFilesDir : context.getExternalFilesDirs(null)) {
                if (externalFilesDir == null) {
                    continue;
                }
                addDirectory(directories, new File(externalFilesDir, DIR_NAME));

                final File storageRoot = storageRootOf(externalFilesDir);
                if (storageRoot != null) {
                    final File dataFiles = new File(new File(storageRoot, "data"), "files");
                    addDirectory(directories, new File(dataFiles, DIR_NAME));
                }
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "external storage unavailable", throwable);
        }

        return directories;
    }

    /**
     * Derives the storage root of an app-specific external files dir, e.g.
     * {@code /storage/23DE-10EA/Android/data/<package>/files} → {@code /storage/23DE-10EA}.
     */
    private static File storageRootOf(File externalFilesDir) {
        File files = externalFilesDir.getParentFile();          // .../Android/data/<package>
        File data = files == null ? null : files.getParentFile(); // .../Android/data
        File android = data == null ? null : data.getParentFile(); // .../Android
        if (files == null || data == null || android == null
                || !"files".equals(files.getName())
                || !"data".equals(data.getName())
                || !"Android".equals(android.getName())) {
            return null;
        }
        return android.getParentFile();                         // <storageRoot>
    }

    private static void addDirectory(List<File> directories, File directory) {
        if (!directories.contains(directory)) {
            directories.add(directory);
        }
    }

    /**
     * Writes {@code content} to {@code target} atomically (temp file + rename) and idempotently
     * (an already identical file is left untouched so Meta's own reader timestamps stay intact).
     */
    private static void writeFileSafely(File target, String content) {
        try {
            final byte[] bytes = content.getBytes("UTF-8");

            if (isSameContent(target, bytes)) {
                return;
            }

            final File directory = target.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs() && !directory.isDirectory()) {
                Log.w(TAG, "Could not create directory " + directory);
                return;
            }

            final File tempFile = new File(directory, FILE_NAME + ".tmp");
            final FileOutputStream outputStream = new FileOutputStream(tempFile);
            try {
                outputStream.write(bytes);
                outputStream.flush();
            } finally {
                outputStream.close();
            }

            if (!tempFile.renameTo(target)) {
                // Rename can fail across some filesystems; delete and rename again.
                target.delete();
                if (!tempFile.renameTo(target)) {
                    Log.w(TAG, "Could not finalize " + target);
                    tempFile.delete();
                }
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Could not write " + target, throwable);
        }
    }

    private static boolean isSameContent(File file, byte[] bytes) {
        if (!file.isFile() || file.length() != bytes.length) {
            return false;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            final byte[] existing = new byte[bytes.length];
            int read = 0;
            while (read < bytes.length) {
                final int count = inputStream.read(existing, read, bytes.length - read);
                if (count < 0) {
                    return false;
                }
                read += count;
            }
            return Arrays.equals(existing, bytes);
        } catch (Throwable throwable) {
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
