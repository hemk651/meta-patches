/*
 * Meta Patches — misc activity.
 *
 * Forked from Instafel activity/ifl_a_misc.java
 * (https://github.com/mamiiblt/instafel) by mamiiblt (Muhammed Ali Bulut),
 * rebranded to Meta Patches.
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.meta.extension.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import app.hemk651.meta.extension.ModsEnv;
import app.hemk651.meta.extension.ModsUi;

/**
 * Miscellaneous Meta Patches information and backup tools.
 *
 * The patches compiled into this build are listed in one place only — the
 * "Applied package patches" screen — so this page does not repeat them.
 */
public class ModsMiscActivity extends Activity {

    private ModsUi ui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = new ModsUi(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ui.background());

        root.addView(ui.pageTitle("Misc"));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int side = ui.dp(16);
        content.setPadding(side, 0, side, side);

        content.addView(ui.sectionTitle("Meta Patches"));
        content.addView(ui.paragraph(
                "Meta Patches is a community bundle of independent modifications. "
                        + "Every patch compiled into this build is listed under "
                        + "Applied package patches — open it to see what is active."));
        content.addView(ui.tile(
                "▤", "Applied package patches",
                "See every patch included in this build", "›",
                v -> openActivity(ModsPatchesInfoActivity.class)));

        content.addView(ui.sectionTitle("Backup"));
        content.addView(ui.tile("⤓", "Import / export backup",
                "Restore overrides from a backup file or save the current ones", "›",
                v -> openActivity(ModsImportBackupActivity.class)));

        content.addView(ui.sectionTitle("Build"));
        content.addView(ui.tile("⎇", "Patch Version", ModsEnv.PATCH_VERSION, "›",
                v -> openActivity(ModsBuildInfoActivity.class)));

        root.addView(ui.pageContent(content));
        ModsUi.applySystemBarInsets(root);
        setContentView(root);
    }

    private void openActivity(Class<? extends Activity> activityClass) {
        startActivity(new android.content.Intent(this, activityClass));
    }
}
