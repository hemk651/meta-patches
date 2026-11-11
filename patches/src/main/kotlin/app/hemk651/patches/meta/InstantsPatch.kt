package app.hemk651.patches.meta

import app.morphe.patcher.patch.patch
import app.hemk651.patches.shared.MetaCompatibilities

/**
 * Packages the user-consented MetaConfig override installer for Instants.
 * The package name and Employee Options flag remain candidate compatibility
 * data until confirmed against an actual installed APK.
 */
val instantsPatch = patch(
    name = "Developer Options Unlocker (Employee Options)",
    description = "Packages the MetaConfig override and SAF installer for Instants. Package and employee-flag compatibility require on-device verification; no bytecode unlock hook is included.",
    default = false
) {
    compatibleWith(MetaCompatibilities.INSTANTS)
    extendWith("extensions/extension.mpe")
}
