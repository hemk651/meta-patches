# Meta Patches

An independent Morphe patch source for a shared MetaConfig override file across four Meta Android apps.

## Morphe patches

<!-- PATCHES_START -->
> **[v1.0.0](https://github.com/hemk651/meta-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
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

The `mc_overrides.json` payload is bundled into the patches at build time and
installed into each app's **own internal storage** at runtime:

```text
<filesDir>/mobileconfig/mc_overrides.json
```

This is the path Instagram natively reads its MobileConfig overrides from
(verified in the app's smali). The "Unlock developer options" patch injects a
static call into the app shell's `Application.onCreate` that writes the payload
there; it becomes active on the next app start. The payload must be kept as a
single line — Instagram's loader only reliably parses compact single-line JSON.

## Canonical configuration

The single source of truth is [config/mc_overrides.json](config/mc_overrides.json).
It is embedded into the extension at build time (as a `BuildConfig` string), so
local and CI builds always carry the reviewed payload. Validate it with:

```bash
python3 scripts/validate_config.py
```

## Development status

The Morphe patch set is ported from InstaFL and verified in the build harness
(`:patches:instagramApkBuild` applies every patch to a real Instagram APK and
checks all DEX files). On-device verification of individual patches is ongoing.

## Risk and disclaimer

MetaConfig experiments may be unstable, account-specific, or carry service/account risk. No safety or compatibility guarantee is made. This independent community project is not affiliated with Meta or Morphe.
