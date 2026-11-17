extension {
    name = "extensions/extension.mpe"
}

/*
 * The MobileConfig overrides payload is embedded at build time instead of
 * being shipped as an asset: the patcher merges only the extension's dex
 * into the target APK, so assets never reach the device.
 *
 * `config/mc_overrides.json` stays the canonical, validated source of
 * truth (see scripts/validate_config.py). It must be a single line —
 * Instagram's loader only reliably parses compact single-line JSON.
 */
val mcOverridesFile = rootProject.file("config/mc_overrides.json")

android {
    namespace = "app.hemk651.meta.extension"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val json = mcOverridesFile.readText().trim()
        require(json.isNotEmpty()) { "config/mc_overrides.json is empty" }
        require(!json.contains('\n') && !json.contains('\r')) {
            "config/mc_overrides.json must be a single line (Instagram's " +
                    "loader only reliably parses compact single-line JSON)"
        }
        buildConfigField(
            "String",
            "MC_OVERRIDES_JSON",
            "\"" + json.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        )
    }
}

// Re-generate BuildConfig whenever the canonical payload changes.
tasks.matching { it.name.startsWith("generate") && it.name.endsWith("BuildConfig") }
    .configureEach {
        inputs.file(mcOverridesFile)
    }
