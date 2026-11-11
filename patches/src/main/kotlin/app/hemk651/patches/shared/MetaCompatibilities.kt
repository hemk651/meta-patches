package app.hemk651.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.Compatibility

object MetaCompatibilities {
    private fun target(name: String, packageName: String, color: Int) = Compatibility(
        name = name,
        packageName = packageName,
        apkFileType = ApkFileType.APK,
        appIconColor = color,
        // Empty targets means any version of this package.
        targets = emptyList()
    )

    val INSTAGRAM = target("Instagram", "com.instagram.android", 0xE1306C)
    val THREADS = target("Threads", "com.instagram.barcelona", 0x101010)
    val EDITS = target("Edits", "com.instagram.basel", 0x6C63FF)
    val INSTANTS = target("Instants", "com.instagram.moonshot", 0x222222)
}
