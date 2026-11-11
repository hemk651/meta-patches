# Supported apps

The project targets exactly these four Android apps:

| App | Android package ID | Status |
|---|---|---|
| Instagram | `com.instagram.android` | Package known; patch not yet verified |
| Threads | `com.instagram.barcelona` | Package known; patch not yet verified |
| Edits | `com.instagram.basel` | Package confirmed from Google Play listing; patch not yet verified |
| Instants | `com.instagram.moonshot` | Candidate package ID; verify against installed app before release |

Instants is Meta's standalone companion app for sharing disappearing photos. The package ID above is a candidate from third-party app metadata and must be checked against the official installed APK before it is treated as confirmed.

A package name is not sufficient evidence of compatibility. Each app needs its own Morphe compatibility declaration and verified startup integration point.

## Acceptance checklist

- [ ] Confirm exact package ID on a device using `adb shell pm list packages`.
- [ ] Record app version name and version code.
- [ ] Identify the correct process and startup hook for that APK build.
- [ ] Confirm a supported way to request and persist removable-storage access.
- [ ] Write the shared `mc_overrides.json` to the requested mobileconfig folder.
- [ ] Verify the file contents on-device.
- [ ] Test update/restart behavior and failure handling.
