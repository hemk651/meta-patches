# Meta Patches

An independent Morphe patch source for a shared MetaConfig override file across four Meta Android apps.

## Morphe patches

<!-- PATCHES_START -->
> **[v1.0.0-dev.2](https://github.com/hemk651/meta-patches/releases/tag/v1.0.0-dev.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 Edits&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.basel`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Edits — compatibility entry](#edits-compatibility-entry) | Registers Edits package compatibility. Runtime config injection is not included yet. |  |

</details>

<details open>
<summary>📦 Instagram&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.android`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Instagram — compatibility entry](#instagram-compatibility-entry) | Registers Instagram package compatibility. Runtime config injection is not included yet. |  |

</details>

<details open>
<summary>📦 Instants&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.moonshot`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Instants — compatibility entry](#instants-compatibility-entry) | Registers Instants package compatibility. Runtime config injection is not included yet. |  |

</details>

<details open>
<summary>📦 Threads&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.barcelona`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Threads — compatibility entry](#threads-compatibility-entry) | Registers Threads package compatibility. Runtime config injection is not included yet. |  |

</details>

<!-- PATCHES_END -->

## Target apps

| App | Package ID | Verification |
|---|---|---|
| Instagram | `com.instagram.android` | Known package; target APK not yet tested |
| Threads | `com.instagram.barcelona` | Known package; target APK not yet tested |
| Edits | `com.instagram.basel` | Google Play package confirmed; target APK not yet tested |
| Instants | `com.instagram.moonshot` | Candidate package; verify on-device before release |

## Feature

Install the same `mc_overrides.json` payload into each app's intended `mobileconfig` directory.

The requested destination pattern is:

```text
/storage/<volume-id>/data/files/mobileconfig/mc_overrides.json
```

The volume ID is dynamic and must never be hard-coded.

## Canonical configuration

The single source of truth is [config/mc_overrides.json](config/mc_overrides.json). Validate it with:

```bash
python3 scripts/validate_config.py
```

## Android storage constraints

On Android 11 and later, a normal app cannot assume write access to an arbitrary directory at the root of removable storage. Discovering a volume UUID does not grant write permission. The implementation must use a verified access path, such as a user-granted Storage Access Framework tree URI where appropriate, and must fail clearly if the requested destination cannot be written.

The patch must not silently write to app-private storage or another folder as a fallback. It must also verify that the target app actually reads this file from the requested location.

## Development status

The repository contains the shared payload, validation, project metadata, and initial Morphe Gradle configuration. App-specific startup hooks, APK fingerprints, removable-storage write integration, and a verified build are still pending. No app is currently advertised as tested or working.

## Risk and disclaimer

MetaConfig experiments may be unstable, account-specific, or carry service/account risk. No safety or compatibility guarantee is made. This independent community project is not affiliated with Meta or Morphe.
