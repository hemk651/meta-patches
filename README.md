# Meta Patches

An independent Morphe patch source for Instagram.

<!-- PATCHES_START EXPANDED -->
> **[v1.1.0-dev.10](https://github.com/hemk651/meta-patches/releases/tag/v1.1.0-dev.10)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;6 patches total
<details open>
<summary>📦 Instagram&nbsp;&nbsp;•&nbsp;&nbsp;6 patches</summary>
<br>

**Package name:** `com.instagram.android`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [AMOLED theme](#amoled-theme) | Forces Instagram dark theme surfaces to pure black (AMOLED). |  |
| [Add Mods menu](#add-mods-menu) | Adds the Mods menu (fork of the InstaFL menu): long press the home feed to open the quick menu, incl. developer options. |  |
| [Change channel name](#change-channel-name) | Replaces the release channel name (NONE, ALPHA, BETA, PROD) shown in the developer options UI. | • Channel name |
| [Remove ads](#remove-ads) | Removes ads in Instagram by stopping the ad pod flow. |  |
| [Remove snooze warning](#remove-snooze-warning) | Removes the "build expired" snooze warning dialog that alpha builds show. |  |
| [Unlock developer options](#unlock-developer-options) | Unlocks the developer options: writes the MobileConfig overrides file that reveals Developer options in the settings together with a dedicated Employee options entry, and forces the employee options gate to true (shake-gesture entry). The overrides file becomes active after an app restart. |  |

</details>

<!-- PATCHES_END -->

## Runtime install (Instagram)

For Instagram the payload is installed without any user consent flow:

- `config/mc_overrides.json` stays the canonical, validated source of truth and must remain a **single line** (Instagram's loader only reliably parses compact single-line JSON).
- The payload is embedded into the extension at build time as a `BuildConfig` string (`extensions/extension/build.gradle.kts`); no asset is required because the patcher merges only the extension's dex into the target APK.
- The "Unlock developer options" patch injects a call to `MCOverrides.install(Context)` right after the app shell's `Application.onCreate` super call, so it works without the optional "Add Mods menu" patch.
- The writer installs the payload atomically and idempotently to `<filesDir>/mobileconfig/mc_overrides.json` — the exact path Instagram's MobileConfig loader reads natively (verified in the app's smali). The settings become active on the next app start.
- The same patch also forces the employee options gate (`A00(UserSession)Z`) to true, which unlocks the shake-gesture developer options entry.
- Verified by `:patches:instagramApkBuild`: all patches apply, all DEX files pass dexdump, and the cross-DEX hierarchy issue set is identical to the stock APK.

## Mods menu (`Add Mods menu`)

The optional "Add Mods menu" patch ports the InstaFL quick menu: **long press the home feed** to open the sheet with the ported InstaFL tools (Open Meta menu, Developer options, About, Restart app, …).

- The home long-press listener and the sheet's `openDeveloperOptions` placeholder are replaced wholesale with fixed-size bodies, mirroring InstaFL's smali (`.registers 6` for the listener, `.registers 7` for the developer-options body, which dismisses the sheet through `p0`). Earlier bundles (v1.1.0-dev.5–dev.8) injected the developer-options body with hardcoded registers and called `dismissSheetDialog()` on an uninitialized register; ART failed to verify the class, so loading the sheet crashed the app exactly when the long press fired. Since v1.1.0-dev.9 the method is normalized to the fixed register layout before the body is injected, and the patched registers are verified by dex inspection in the build harness.
- Opening the menu can no longer take the app down: if the sheet fails to open, the handler reports the error into the in-app crash reports and shows a toast naming the cause (`Menu error: <Exception>`).
- Since v1.1.0-dev.10 the popups behave like back gestures: tapping the dimmed background (or any empty spot of the sheet) closes the quick sheet, and tapping an empty area of the Mods menu activity closes it. Every item action and both activity `onCreate` bodies are wrapped — a failure lands in the crash reports and a `Menu error: <Exception>` toast instead of crashing the app.
- The menu activities declare the framework theme `Theme.Material.NoActionBar` in the manifest. Without it they inherited Instagram's application theme — the splash theme chain (`Theme.Instagram.Splash`) — whose unresolved `textAppearance` attributes made the first `TextView` of the menu throw `Failed to resolve attribute at index 7`, crashing every menu activity at start. Reported on device with v1.1.0-dev.9; fixed in v1.1.0-dev.11 (verified by `aapt2 dump xmltree` of the output APK).
- Crash reports live in the Mods menu (Open Meta menu → Crash reports); the log file is written to `<external files dir>/mp_update_files/mp_crashlogs.json` (e.g. `/storage/emulated/0/Android/data/com.instagram.android/files/mp_update_files/mp_crashlogs.json`).
- The extension initialization (`ModsInit.init`) is injected after the app shell's `Application.onCreate` super call whenever it is not present yet — independently of the "Unlock developer options" patch, which injects its own `MCOverrides.install` call at the same site. (Previously the two patches could each skip the other's injection, leaving the menu without a context.)

## Targets

| App | Package |
|---|---|
| Instagram | `com.instagram.android` |

## Validation

Run `python3 scripts/validate_config.py` to validate the source payload. This validates repository data only; it does not prove device deployment.

## Current limitation

For Instagram the startup hook is wired and the payload install plus dev-options unlock are confirmed working on device (v1.1.0-dev.5). The "Remove ads" patch now installs the full community-tested ad-removal override set (1370 parameters, merged with the developer-options parameters; see InstaFL issues #143, #150 and #160 for on-device ad coverage reports), embedded into the extension DEX at build time (v1.1.0-dev.7).

The Mods menu crash on a home long press (v1.1.0-dev.5–dev.8) is fixed in v1.1.0-dev.9: the register layouts of the replaced methods now match InstaFL's proven smali and the patched registers were verified by dex inspection of the output APK. On-device confirmation of the menu itself is still pending.

> Note for consumers: the Morphe manager runs the patcher on-device and loads the patch classes from the bundle's `classes.dex`, which only the `:patches:buildAndroid` task adds to the bundle.
