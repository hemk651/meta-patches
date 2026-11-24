# Supported apps — dev branch

This page records the functional target scope and the verification evidence documented for the current development branch.

| App | Android package ID | Evidence recorded in project notes |
|---|---|---|
| Instagram | `com.instagram.android` | Runtime payload installation and developer-options unlock confirmed on-device on the target build (v1.1.0-dev.5); Mods menu opening confirmed on-device by v1.1.0-dev.11 |
| Instagram Instants | `com.instagram.moonshot` | Instants Mod port and harness work documented on the target build; device confirmation pending |

Threads and Edits are not targets of this branch. Their compatibility declarations remain on `main`; they are not included in the dev patch list.

## Instagram

The Instagram target includes the developer-options/MobileConfig runtime path, Mods menu extension, theme and channel-label modifications, ad-pod suppression, snooze-warning removal, and gallery upload to Instants.

The runtime writer installs the bundled configuration to:

```text
<filesDir>/mobileconfig/mc_overrides.json
```

The app-private path is intentional: the previous removable-storage/SAF design was removed because the extension's dex-only merge does not deliver the host app's assets to the target app.

## Instagram Instants

The Instants Mod is a port of the functional parts of [Wagg13Patch_Morphe](https://github.com/WaggBR/Wagg13Patch_Morphe) (GPL-3). It adds a gallery picker to the camera flow and a save-to-gallery action for friends' Instants. The patch declares a version-agnostic target, but the documented harness check on the target build is not a guarantee for future releases. Device verification remains pending in the project notes.

## Verification terminology

- **Declared:** Morphe metadata registers the package/target.
- **Harness verified:** the project's APK-oriented build/test harness completed for the recorded input.
- **Device confirmed:** the behavior was observed on a physical device and exact app build.

Do not promote a feature from one evidence level to another without new test results. See [feature-status.md](feature-status.md) and [implementation-plan.md](implementation-plan.md).
