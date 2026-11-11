package app.hemk651.patches.meta

import app.morphe.patcher.patch.patch
import app.hemk651.patches.shared.MetaCompatibilities

/**
 * Compatibility registration for Edits.
 *
 * This declaration makes the patch visible for the exact package name without
 * a version-code allowlist. The runtime mc_overrides writer is not implemented
 * yet; this patch intentionally does not modify app bytecode.
 */
val editsPatch = patch(
    name = "Developer option Unlocker (Employee option)",
    description = "Bundles the MetaConfig installer for Edits. A user-granted folder permission is required; startup invocation is not yet wired.",
    default = false
) {
    compatibleWith(MetaCompatibilities.EDITS)

    // Package the SAF installer helper and mc_overrides.json asset with this target.
    extendWith("extensions/extension.mpe")
}
