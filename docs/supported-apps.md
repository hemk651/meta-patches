# Supported apps

The project targets exactly one app:

| App | Android package ID | Status |
|---|---|---|
| Instagram | `com.instagram.android` | Verified on device (v1.1.0-dev.5, Instagram 457.0.0.0.26) |

Verification covers: payload install via the runtime writer, developer options unlocked, all patches apply cleanly in the build harness, and the patched DEX hierarchy matches the stock APK.

The previous multi-app list (Threads, Edits, Instants) was removed along with the per-app packaging patches; those declarations were never verified on device.
