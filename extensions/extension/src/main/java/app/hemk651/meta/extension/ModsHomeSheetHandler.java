/*
 * Meta Patches — home sheet handler.
 *
 * Forked from InstaFL utils/HomeSheetHandler.java
 * (https://github.com/mamiiblt/instafel) by mamiiblt (Muhammed Ali Bulut),
 * rebranded to Meta Patches.
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.meta.extension;

import android.app.Activity;
import android.content.Context;
import android.view.View;

/**
 * Replaces the Instagram home button long click listener.
 */
public class ModsHomeSheetHandler implements View.OnLongClickListener {

    private final Activity mainAppActivity;

    public ModsHomeSheetHandler(Context context) {
        this.mainAppActivity = context instanceof Activity ? (Activity) context : null;
    }

    @Override
    public boolean onLongClick(View view) {
        if (mainAppActivity == null) {
            return false;
        }

        new ModsHomeSheet(mainAppActivity).buildAndShowDialog();
        return true;
    }
}
