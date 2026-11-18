# Supported apps — main branch

This page describes the **main** branch only. It must not be read as a feature-verification list for the development branch.

## Registered package targets

| App | Android package ID | Main-branch declaration | Runtime feature status |
|---|---|---|---|
| Instagram | `com.instagram.android` | Registered | No runtime injection in this branch |
| Threads | `com.instagram.barcelona` | Registered | No runtime injection in this branch |
| Edits | `com.instagram.basel` | Registered | No runtime injection in this branch |
| Instants | `com.instagram.moonshot` | Registered | No runtime injection in this branch; confirm package identity on-device |

These entries are compatibility declarations. They make the package targets available to Morphe but do not modify app bytecode or install `mc_overrides.json`.

## Verification requirements

A package declaration alone does not establish that a patch works. Before advertising an app as supported, record:

- The exact package ID and installed app version/version code.
- The APK fingerprint or structural anchors used by the patch.
- The build-harness result for the exact APK.
- On-device installation and runtime behavior.
- Restart, update, and failure-path behavior.

The development branch currently contains the functional Instagram and Instagram Instants implementation. See [the dev branch's supported-app page](https://github.com/hemk651/meta-patches/blob/dev/docs/supported-apps.md) for its separate scope and evidence.
