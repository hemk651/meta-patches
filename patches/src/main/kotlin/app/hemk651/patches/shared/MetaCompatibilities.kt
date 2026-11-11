package app.hemk651.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object MetaCompatibilities {
    private fun target(name: String, packageName: String, color: Long) = Compatibility(
        name = name,
        packageName = packageName,
        apkFileType = ApkFileType.APK,
        appIconColor = color,
        targets = listOf(
            AppTarget(version = null, isExperimental = true)
        )
    )

    val INSTAGRAM = target("Instagram", "com.instagram.android", 0xFFE1306C)
    val THREADS = target("Threads", "com.instagram.barcelona", 0xFF101010)
    val EDITS = target("Edits", "com.instagram.basel", 0xFF6C63FF)
    val INSTANTS = target("Instants", "com.instagram.moonshot", 0xFF222222)
}
