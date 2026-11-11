package app.hemk651.patches.meta

import app.morphe.patcher.patch.patch
import app.hemk651.patches.shared.MetaCompatibilities

/**
 * Compatibility registration for Threads.
 *
 * This declaration makes the patch visible for the exact package name without
 * a version-code allowlist. The runtime mc_overrides writer is not implemented
 * yet; this patch intentionally does not modify app bytecode.
 */
val threadsPatch = patch(
    name = "Developer Options Unlocker (Employee Options)",
    description = "Provides the user-consented MetaConfig installer for Threads. A user-granted folder permission is required; startup invocation is not yet wired.",
    default = false
) {
    compatibleWith(MetaCompatibilities.THREADS)

    // Package the SAF installer helper and mc_overrides.json asset with this target.
    extendWith("extensions/extension.mpe")
}
