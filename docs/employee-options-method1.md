# Method 1: Enable Instagram employee options through MobileConfig

## Finding

The Rex IG Lite implementation confirms this Instagram flag:

- Universal ID: `28538`
- Parameter ID: `0`
- Full flag key: `28538::0`
- Meaning in upstream source: `ig_android_employee_options::is_enabled`
- Forced value: `true`

Source: [Rex IG Lite HookFlags.java](https://github.com/xboyrex/rex-ig-lite/blob/main/extensions/instagram/src/main/java/app/morphe/extension/instagram/patches/devFlags/HookFlags.java).

Rex implements this as a runtime MobileConfig read override in `HookFlags.employeeOptionsFlags()`, gated by the `enableEmployeeOptions` preference. The bytecode patch installs a hook into Instagram's boolean MobileConfig flag-check method. The hook maps the incoming MobileConfig specifier to a universal ID and parameter ID, then returns the override when a match exists.

Relevant upstream implementation:
- [UnlockEmployeeOptionsPatch.kt](https://github.com/xboyrex/rex-ig-lite/blob/main/patches/src/main/kotlin/app/rex/patches/instagram/misc/internalStuffs/employeeOptions/UnlockEmployeeOptionsPatch.kt)
- [HookFlagsPatch.kt](https://github.com/crimera/piko/blob/main/patches/src/main/kotlin/app/crimera/patches/instagram/misc/hookFlags/HookFlagsPatch.kt)
- [HookFlags.java](https://github.com/xboyrex/rex-ig-lite/blob/main/extensions/instagram/src/main/java/app/morphe/extension/instagram/patches/devFlags/HookFlags.java)
- [DeveloperOptionsItem.java](https://github.com/xboyrex/rex-ig-lite/blob/main/extensions/instagram/src/main/java/app/morphe/extension/instagram/entity/DeveloperOptionsItem.java)

## Important distinction

The canonical external file already contains the corresponding entry:

```json
"28538:": [
  "0: : true"
]
```

That entry is not, by itself, proof that Instagram reads the file from the removable-storage path. The upstream implementation does not rely on that file being discovered by Instagram: it intercepts MobileConfig flag reads inside the patched process.

Therefore Method 1 for this repository has two possible mechanisms:

1. **External-file experiment:** retain the current payload and test whether the target Instagram build actually reads the specified external file. This remains unverified.
2. **Verified runtime flag hook:** port the upstream MobileConfig hook and employee flag override into this project, with compatible fingerprints and extension code. This is the reliable source-backed route.

Do not claim Method 1 works until an actual Instagram build is patched, the hook is present in the APK, and the employee-options UI is verified at runtime.

## Repository state

- `config/mc_overrides.json` is canonical and must not be edited as part of this work.
- Current Meta Patches declarations only register package compatibility and bundle the SAF extension. They do not install a MobileConfig bytecode hook.
- No equivalent verified hook currently exists for Threads, Edits, or Instants.
- The upstream Instagram hook must not be blindly applied to those three apps.

## Next implementation steps

1. Keep the canonical JSON unchanged.
2. Add an Instagram-only opt-in patch for `28538::0`.
3. Port the MobileConfig boolean-read hook and flag resolver, preserving upstream license notices and attribution.
4. Verify the hook fingerprint against the exact Instagram APK being patched.
5. Build and inspect the resulting Morphe bundle/APK.
6. Test the employee-options UI. If this fails, proceed to the separate Developer Options unlocker patch (Method 2).
