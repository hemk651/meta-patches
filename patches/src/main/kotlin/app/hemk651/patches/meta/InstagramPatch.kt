package app.hemk651.patches.meta

import app.morphe.patcher.patch.patch
import app.hemk651.patches.shared.MetaCompatibilities

/**
 * Packages the user-consented MetaConfig override installer for Instagram.
 *
 * The canonical payload includes the Employee Options candidate flag
 * 28538::0 = true. This patch does not yet hook Instagram's MobileConfig
 * reader or unlock the UI through bytecode; those require APK-verified
 * fingerprints and a separate runtime hook.
 */
val instagramPatch = patch(
    name = "Developer Options Unlocker (Employee Options)",
    description = "Packages the Employee Options MetaConfig override (28538::0 = true) and SAF installer for Instagram. Actual activation requires the app to read the deployed file; bytecode unlocking is not included.",
    default = false
) {
    compatibleWith(MetaCompatibilities.INSTAGRAM)
    extendWith("extensions/extension.mpe")
}
