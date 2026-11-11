package app.hemk651.meta.extension;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.database.Cursor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * SAF-based installer for the MetaConfig payload.
 *
 * The host app must launch createFolderPickerIntent() from a visible Activity,
 * then pass the returned tree URI to install(). This does not bypass Android
 * scoped-storage restrictions and never writes to a guessed raw filesystem path.
 */
public final class MetaConfigInstaller {
    private static final String PREFS = "meta_config_installer";
    private static final String PREF_TREE_URI = "tree_uri";
    private static final String SOURCE_ASSET = "mc_overrides.json";
    private static final String TARGET_DIRECTORY = "mobileconfig";
    private static final String TARGET_FILE = "mc_overrides.json";

    private MetaConfigInstaller() {}

    /** Call from an Activity after explaining why the folder is needed. */
    public static Intent createFolderPickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        return intent;
    }

    /**
     * Persist the user's selected parent directory and install the bundled file.
     * Select the "files" directory that should contain the mobileconfig folder.
     */
    public static String install(Context context, Uri selectedTree, int resultFlags) {
        if (context == null || selectedTree == null) return "ERROR: missing context or folder URI";

        int takeFlags = resultFlags & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if ((takeFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0) {
            return "ERROR: folder was not granted write access";
        }

        try {
            context.getContentResolver().takePersistableUriPermission(selectedTree, takeFlags);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(PREF_TREE_URI, selectedTree.toString()).apply();
            return installIntoTree(context, selectedTree);
        } catch (Exception e) {
            return "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    /** Retry on later app launches after a folder grant has been saved. */
    public static String installUsingSavedGrant(Context context) {
        String saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(PREF_TREE_URI, null);
        if (saved == null) return "NEEDS_FOLDER_PERMISSION";
        try {
            Uri tree = Uri.parse(saved);
            boolean stillGranted = false;
            for (UriPermission permission : context.getContentResolver().getPersistedUriPermissions()) {
                if (permission.getUri().equals(tree) && permission.isWritePermission()) {
                    stillGranted = true;
                    break;
                }
            }
            if (!stillGranted) return "NEEDS_FOLDER_PERMISSION";
            return installIntoTree(context, tree);
        } catch (Exception e) {
            return "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private static String installIntoTree(Context context, Uri tree) throws Exception {
        byte[] payload;
        try (InputStream in = context.getAssets().open(SOURCE_ASSET);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            payload = out.toByteArray();
        }

        ContentResolver resolver = context.getContentResolver();
        String treeId = DocumentsContract.getTreeDocumentId(tree);
        Uri parent = DocumentsContract.buildDocumentUriUsingTree(tree, treeId);
        Uri directory = findChild(resolver, tree, parent, TARGET_DIRECTORY);
        if (directory == null) {
            directory = DocumentsContract.createDocument(
                    resolver, parent, DocumentsContract.Document.MIME_TYPE_DIR, TARGET_DIRECTORY);
        }
        if (directory == null) return "ERROR: could not create mobileconfig directory";

        Uri existing = findChild(resolver, tree, directory, TARGET_FILE);
        Uri destination = existing;
        if (destination == null) {
            destination = DocumentsContract.createDocument(
                    resolver, directory, "application/json", TARGET_FILE);
        }
        if (destination == null) return "ERROR: could not create mc_overrides.json";

        try (OutputStream out = resolver.openOutputStream(destination, "wt")) {
            if (out == null) return "ERROR: provider did not open destination for writing";
            out.write(payload);
            out.flush();
        }

        try (InputStream in = resolver.openInputStream(destination);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) return "ERROR: cannot read back installed file";
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            byte[] written = out.toByteArray();
            if (!java.util.Arrays.equals(payload, written)) {
                return "ERROR: read-back verification failed";
            }
        }
        return "INSTALLED_AND_VERIFIED";
    }

    private static Uri findChild(ContentResolver resolver, Uri tree, Uri parent, String name) {
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(
                tree, DocumentsContract.getDocumentId(parent));
        String[] columns = {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
        };
        try (Cursor cursor = resolver.query(children, columns, null, null, null)) {
            if (cursor == null) return null;
            int idColumn = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameColumn = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                if (name.equals(cursor.getString(nameColumn))) {
                    return DocumentsContract.buildDocumentUriUsingTree(tree, cursor.getString(idColumn));
                }
            }
        }
        return null;
    }
}
