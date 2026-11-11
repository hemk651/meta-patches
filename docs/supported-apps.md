# Supported apps

The initial scope contains exactly four apps.

| App | Package ID | Status |
|---|---|---|
| Instagram | `com.instagram.android` | Package known; patch not yet verified |
| Threads | `com.instagram.barcelona` | Package known; patch not yet verified |
| Edits | To verify from installed APK / official listing | Pending |
| Instant | To confirm exact app identity and package | Pending |

A package name is not sufficient evidence of compatibility. Each app needs its own Morphe compatibility declaration and verified startup integration point.

## Acceptance checklist

- [ ] Confirm exact package ID and app identity.
- [ ] Identify app version and version code used for development.
- [ ] Confirm the correct process and startup hook.
- [ ] Confirm a supported way to request and persist removable-storage access.
- [ ] Write the shared `mc_overrides.json` to the requested mobileconfig folder.
- [ ] Verify the file contents on-device.
- [ ] Test update/restart behavior and failure handling.
