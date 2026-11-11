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
    name = "Edits — compatibility entry",
    description = "Registers Edits package compatibility. Runtime config injection is not included yet.",
    default = false
) {
    compatibleWith(MetaCompatibilities.EDITS)
}
