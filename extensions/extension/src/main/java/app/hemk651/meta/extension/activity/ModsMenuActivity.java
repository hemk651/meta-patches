/*
 * Meta Patches — main menu activity.
 *
 * Forked from InstaFL activity/ifl_a_menu.java
 * (https://github.com/mamiiblt/instafel) by mamiiblt (Muhammed Ali Bulut),
 * rebranded to Meta Patches with the following changes:
 *   - the Updates (OTA) menu was removed
 *   - the Library section was removed
 *   - the admin dashboard was removed
 *   - the Join Community, guide and social icons were removed;
 *     only the language and information icons remain
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.meta.extension.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;

import app.hemk651.meta.extension.ModsEnv;
import app.hemk651.meta.extension.ModsHomeSheet;
import app.hemk651.meta.extension.ModsUi;

/**
 * The main Mods menu.
 */
public class ModsMenuActivity extends Activity {

    private ModsUi ui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = new ModsUi(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ui.background());

        root.addView(ui.pageTitle(ModsEnv.APP_NAME));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int side = ui.dp(16);
        content.setPadding(side, 0, side, side);

        content.addView(ui.tile(
                "⌥", "Developer options", "Open the employee options menu", "›",
                v -> openDeveloperOptions()));

        content.addView(ui.tile(
                "↯", "Crash logs", "View and manage crash reports", "›",
                v -> openActivity(ModsCrashReportsActivity.class)));

        content.addView(ui.tile(
                "☰", "Misc", "Patch states and other settings", "›",
                v -> openActivity(ModsMiscActivity.class)));

        // ── Bottom icons ────────────────────────────────────────────────────
        // InstaFL shows language, chat, guide and GitHub icons here; only the
        // language and information icons are kept.
        content.addView(ui.sectionTitle("General"));

        content.addView(ui.tile(
                "🌐", "Language", "Menu language", "›",
                v -> openActivity(ModsLanguageActivity.class)));

        content.addView(ui.tile(
                "ⓘ", "About " + ModsEnv.PROJECT_NAME, "Build information and credits", "›",
                v -> openActivity(ModsAboutActivity.class)));

        root.addView(ui.pageContent(content));
        setContentView(root);
    }

    /**
     * Opens the Instagram developer options the same way the home sheet does.
     * The navigation code is injected into ModsHomeSheet.openDeveloperOptions
     * by the patch; it needs the main activity and session that are captured
     * when the home long press fires.
     */
    private void openDeveloperOptions() {
        new ModsHomeSheet(this).openDeveloperOptions();
    }

    private void openActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    /** Opens a web page in the browser, used by the about screens. */
    public static void openUrl(Activity activity, String url) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
