# Meta Patches

An independent Morphe patch source for Instagram.

<!-- PATCHES_START EXPANDED -->
> **[v1.1.0-dev.7](https://github.com/hemk651/meta-patches/releases/tag/v1.1.0-dev.7)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;6 patches total
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

## Targets

| App | Package |
|---|---|
| Instagram | `com.instagram.android` |

## Validation

Run `python3 scripts/validate_config.py` to validate the source payload. This validates repository data only; it does not prove device deployment.

## Current limitation

For Instagram the startup hook is wired and the payload install plus dev-options unlock are confirmed working on device (v1.1.0-dev.5). The "Remove ads" patch is currently a bytecode-only port; ad coverage (stories, promotional videos, reels) is being extended.
