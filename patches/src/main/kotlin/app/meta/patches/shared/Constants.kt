package app.meta.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    /**
     * The exact MobileConfig overrides JSON that unlocks Meta's internal
     * Employee Options / developer options menu.
     *
     * Must stay byte-for-byte identical to [app.meta.extension.MetaConfigOverrides.DEFAULT_OVERRIDES_JSON].
     */
    const val DEFAULT_MC_OVERRIDES_JSON =
        "{\"56474:\":[\"0: : true\"],\"28538:\":[\"0: : true\"],\"32432:\":[\"1: : true\"],\"_qe_overrides_\":[]}"

    val COMPATIBILITY_INSTAGRAM = Compatibility(
        name = "Instagram",
        packageName = "com.instagram.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xE1306C,
        targets = listOf(
            // Any version is supported experimentally; the patch is package-agnostic
            // and only depends on standard Android APIs.
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )

    val COMPATIBILITY_EDITS = Compatibility(
        name = "Edits",
        packageName = "com.instagram.basel",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x5B4CFF,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )

    val COMPATIBILITY_INSTANTS = Compatibility(
        name = "Instants",
        packageName = "com.instagram.moonshot",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x4A90D9,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )
}
