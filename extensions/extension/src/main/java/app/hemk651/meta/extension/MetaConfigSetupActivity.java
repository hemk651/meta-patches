package app.hemk651.meta.extension;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * One-time, user-visible SAF consent screen.
 *
 * Android requires the user to grant access to the destination directory.
 * This Activity is intended to be launched by the future verified startup hook.
 */
public final class MetaConfigSetupActivity extends Activity {
    private static final int REQUEST_SELECT_FOLDER = 7412;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        new AlertDialog.Builder(this)
                .setTitle("Developer Options Unlocker")
                .setMessage("Choose the removable-storage data/files folder. The patch will create mobileconfig/mc_overrides.json there. Android requires this one-time folder permission.")
                .setCancelable(false)
                .setPositiveButton("Choose folder", (dialog, which) ->
                        startActivityForResult(
                                MetaConfigInstaller.createFolderPickerIntent(),
                                REQUEST_SELECT_FOLDER))
                .setNegativeButton("Not now", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_SELECT_FOLDER) return;
        if (resultCode != RESULT_OK || data == null) {
            finish();
            return;
        }

        Uri tree = data.getData();
        String result = MetaConfigInstaller.install(this, tree, data.getFlags());
        new AlertDialog.Builder(this)
                .setTitle("MetaConfig installer")
                .setMessage(result)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }
}
