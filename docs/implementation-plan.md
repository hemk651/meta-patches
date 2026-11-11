# Implementation plan

## Completed in repository

- [x] Canonical JSON payload and repository validator.
- [x] Morphe patch declarations for the four target packages.
- [x] Version-independent compatibility declarations.
- [x] Bundle the JSON as an extension asset.
- [x] Add a SAF installer helper that creates the `mobileconfig` child directory and verifies the written bytes.
- [x] Persist user-granted tree URI access.
- [x] Add a three-launch retry entry point that does not launch UI from a background context.
- [x] Attach the extension bundle to all four patch declarations.

## Still required before claiming automatic deployment

- [ ] Add a visible, user-initiated folder picker flow in the target app.
- [ ] Inspect each target APK and identify a stable app-start hook.
- [ ] Invoke `MetaConfigInstaller.onAppLaunch(context)` from the verified hook.
- [ ] Invoke the picker flow from a visible Activity when the result is `NEEDS_FOLDER_PERMISSION`.
- [ ] Verify Android 11+ permits selection of the intended parent folder on the target device.
- [ ] Build and inspect the Morphe bundle and verify the helper/asset are present.
- [ ] Test all four apps on real APK versions, including first install, denied permission, missing volume, restart, and update.
- [ ] Verify the app itself reads the written configuration and observe the intended developer/employee options.

## Important platform constraint

The target directory is outside ordinary app-private storage. Android 11+ scoped storage prevents assuming raw filesystem write access. The helper uses the Storage Access Framework and requires explicit user consent. It does not bypass the system picker restrictions.
