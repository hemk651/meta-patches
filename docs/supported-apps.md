# Supported apps

| App | Android package ID | Status |
|---|---|---|
| Instagram | `com.instagram.android` | Verified on device (v1.1.0-dev.5, Instagram 457.0.0.0.26) |
| Instagram Instants | `com.instagram.moonshot` | Ported from Wagg13Patch_Morphe; verified in the build harness, awaiting device confirmation (target 455.0.0.55.95) |

Instagram verification covers: payload install via the runtime writer, developer options unlocked, all patches apply cleanly in the build harness, and the patched DEX hierarchy matches the stock APK.

The Instants Mod patch is a port of the functional parts of
[Wagg13Patch_Morphe](https://github.com/WaggBR/Wagg13Patch_Morphe) by Wagg13
(GPL-3): a gallery button on the camera screen (posts a picked photo or video
through the app's own pipeline) and a "Save to gallery" row in the friends'
Instant overflow menu. Upstream anchors it to 451.0.0.53.106; this bundle
declares it for every version and verifies against 455.0.0.55.95 in the
harness (device confirmation pending).

The previous multi-app list (Threads, Edits) was removed along with the
per-app packaging patches; those declarations were never verified on device.

