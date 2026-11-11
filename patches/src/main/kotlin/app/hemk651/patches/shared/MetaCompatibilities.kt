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

    val INSTAGRAM = target("Instagram", "com.instagram.android", 0xFFE1306C.toInt())
    val THREADS = target("Threads", "com.instagram.barcelona", 0xFF101010.toInt())
    val EDITS = target("Edits", "com.instagram.basel", 0xFF6C63FF.toInt())
    val INSTANTS = target("Instants", "com.instagram.moonshot", 0xFF222222.toInt())
}
