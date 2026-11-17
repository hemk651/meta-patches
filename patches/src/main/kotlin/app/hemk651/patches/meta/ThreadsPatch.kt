package app.hemk651.patches.meta

import app.morphe.patcher.patch.patch
import app.hemk651.patches.shared.MetaCompatibilities

/**
 * Packages the user-consented MetaConfig override installer for Threads.
 * The payload's Employee Options flag is not verified for Threads; no
 * app-specific MobileConfig hook is claimed by this compatibility entry.
 */
val threadsPatch = patch(
    name = "Developer Options Unlocker (Employee Options)",
    description = "Packages the MetaConfig override and SAF installer for Threads. Employee flag compatibility and app-side loading are unverified; no bytecode unlock hook is included.",
    default = false
) {
    compatibleWith(MetaCompatibilities.THREADS)
    extendWith("extensions/extension.mpe")
}
