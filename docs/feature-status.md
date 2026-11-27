# Feature and verification status — dev branch

**Branch:** `dev` · **Documented release:** `v1.2.0-beta.1`

This page separates implemented code from the verification evidence recorded in the repository. Status statements reflect the checked-in project notes; they are not a fresh physical-device test.

## Patch inventory

| Patch | Target | Default | Purpose |
|---|---|---:|---|
| AMOLED theme | Instagram | On | Forces supported dark surfaces to pure black |
| Add Mods menu | Instagram | Off | Adds the home-feed long-press menu and companion activities |
| Change channel name | Instagram | Off | Replaces the release-channel label; option `channelName` defaults to `Mods` |
| Clone package | Instagram | Off | Installs beside the original app (`com.metapaches.insta`, `insta by meta`) or replaces it; optional custom launcher icon |
| Remove ads | Instagram | On | Suppresses the ad-pod flow and includes the bundled override set |
| Save Instants to gallery | Instagram | Off | Adds a save-to-gallery option to the Instants menu; hooks discovered at patch time |
| Remove snooze warning | Instagram | On | Removes the alpha-build expiry/snooze warning dialog |
| Unlock developer options | Instagram | On | Installs MobileConfig overrides and enables the employee-options gate |
| Upload instants from gallary | Instagram | Off | Adds gallery-based photo/video selection to the Instants camera |
| Instants Mod | Instagram Instants | Off | Adds camera gallery selection and save-to-gallery action |

Defaults and names above are taken from the branch's generated `patches-list.json`. The misspelling in the gallery-upload patch name is retained because it is the published patch identifier.

## Runtime MobileConfig installation

The canonical source is `config/mc_overrides.json`. The extension embeds it as a compact `BuildConfig` string at build time. The Unlock developer options patch injects the runtime installer after the Instagram app-shell `Application.onCreate` super call.

The writer installs the file atomically and idempotently at:

```text
<filesDir>/mobileconfig/mc_overrides.json
```

The same patch forces the employee-options gate used by the shake-gesture entry. The newly installed overrides are read after an app restart. The repository validator checks the JSON shape and required employee-options candidate flag; it does not prove that an app build will load the file.

The old SAF/removable-storage design was removed. The extension is merged as DEX into the target APK, so an asset stored in the Morphe host app is not available as a target-app asset. The runtime writer avoids that broken host-asset assumption.

## Mods menu and extension

The optional Add Mods menu patch adds a long-press home-feed entry point. The extension includes:

- Meta menu and developer-options navigation
- About and build information pages
- Applied package-patches information
- Miscellaneous tools and backup import
- Crash-report viewer and guarded menu actions
- App restart action
- System-bar inset handling for menu pages

Menu actions report handled failures through a toast and in-app crash log rather than intentionally propagating an exception into the host app. The crash log path documented by the implementation is under the target app's external files directory:

```text
<external-files-dir>/mp_update_files/mp_crashlogs.json
```

### Backup import behavior

The file picker accepts plain overrides JSON and an Instafel-style backup wrapper. The import modes are:

- **Full replace:** replace the current overrides file.
- **Merge:** add keys that are absent from the current file; current values take precedence.
- **Restore bundled overrides:** reinstall the payload bundled with the patch.

A marker prevents the bundled installer from overwriting imported values on subsequent starts. Backup export is not implemented.

## Evidence and verification matrix

| Feature / area | Evidence recorded in project notes | Remaining qualification |
|---|---|---|
| MobileConfig writer + developer-options unlock | Confirmed on-device | Evidence is tied to that recorded app build |
| Mods menu opening | Confirmed on-device | Later menu additions need separate device confirmation |
| Menu register-layout fix | APK harness / DEX inspection | Does not replace device testing |
| Menu activity theme fix | Output APK manifest inspected; menu opening confirmed on-device | Device coverage is limited to recorded test |
| Developer-options navigation from menu | Harness-verified fix | Awaiting device confirmation in project notes |
| Applied-patches JSON, version display, insets | Harness-verified in project notes | Awaiting device confirmation in project notes |
| Backup import | Harness-verified implementation documented | Awaiting device confirmation in project notes |
| Remove ads | Ad-pod patch and 1,370-parameter override set documented | Stories, Reels, and promotional placements are not claimed as fully covered |
| Instants Mod | Port and harness work documented | Device confirmation pending |
| Instagram gallery upload | Harness build documented | Device confirmation pending |

## Validation and build commands

Requirements: JDK 21, Git, and network access to resolve Gradle/Morphe dependencies.

```bash
python3 scripts/validate_config.py
./gradlew :patches:buildAndroid --no-daemon
```

The repository also includes APK-oriented test/harness sources under `patches/src/test`. Use the exact task and APK inputs defined by the current Gradle build; a successful compile or harness run is not the same as device confirmation.

## Compatibility policy

Morphe targets are version-agnostic. That removes version allowlists but does not make obfuscated bytecode fingerprints universal; app changes can affect classes, methods, Compose traces, lifecycle behavior, or configuration loaders.

Record what was tested for every change. Treat **declared**, **harness verified**, and **device confirmed** as separate evidence levels.
