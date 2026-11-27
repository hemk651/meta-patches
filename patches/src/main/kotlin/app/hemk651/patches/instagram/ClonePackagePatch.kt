/*
 * Meta Patches — Clone package.
 *
 * Installs the patched Instagram beside the original app (or replaces it),
 * with a user-selected package name, app name and optional launcher icon.
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.patches.instagram

import app.hemk651.patches.shared.MetaCompatibilities
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.StringFilter
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.ImageSize
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.document
import app.morphe.patcher.patch.imageOption
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceFile
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.string
import app.morphe.patcher.util.getReference
import app.morphe.patcher.util.matchAllMethodIndicesForEach
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File

private const val ORIGINAL_PACKAGE_NAME = "com.instagram.android"
private const val DEFAULT_PACKAGE_NAME = "com.metapaches.insta"
private const val DEFAULT_APP_NAME = "insta by meta"

/** New drawable created when a custom launcher icon is supplied. */
private const val CUSTOM_ICON_RESOURCE = "ic_clone_package_launcher"

@Suppress("unused")
val clonePackagePatch = patch(
    name = "Clone package",
    description =
        "Install Instagram beside the original app (clone) or replace it. " +
            "Choose the package name, the app name and optionally a custom launcher icon.",
    default = false
) {
    compatibleWith(MetaCompatibilities.INSTAGRAM)

    val installBesideOriginal by booleanOption(
        key = "installBesideOriginal",
        default = true,
        title = "Install beside original (clone)",
        description =
            "Enabled: rename the package so the patched app installs alongside Instagram. " +
                "Disabled: keep com.instagram.android and replace the installed app.",
        required = true
    )

    val packageName by stringOption(
        key = "packageName",
        default = DEFAULT_PACKAGE_NAME,
        title = "Package name",
        description = "A new package name for the cloned app.",
        required = true
    ) {
        it?.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+$")) == true
    }

    val appName by stringOption(
        key = "appName",
        default = DEFAULT_APP_NAME,
        title = "App name",
        description = "A new app name (label). Entering \"Instagram\" will skip changing the app name.",
        required = true
    )

    val customIcon by imageOption(
        key = "customIcon",
        default = null,
        title = "Custom launcher icon",
        description =
            "Optional image (PNG/JPG) used as the launcher icon of the cloned app. " +
                "Leave empty to keep the original Instagram icon. " +
                "Morphe's file picker lets you choose an image from your device gallery.",
        required = false,
        allowedExtensions = listOf("png", "jpg", "jpeg"),
        recommendedSize = ImageSize(1024, 1024)
    )

    execute {
        if (installBesideOriginal != true) return@execute

        val newPackageName = packageName!!
        check(newPackageName != ORIGINAL_PACKAGE_NAME) {
            "The clone package name must be different from $ORIGINAL_PACKAGE_NAME. " +
                "Disable 'Install beside original (clone)' to replace the original app."
        }

        /** Pairs of the original authority and the renamed authority. */
        val providerReplacements = mutableListOf<Pair<String, String>>()

        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement

            // Change the package name.
            manifest.setAttribute("package", newPackageName)

            // Rename custom permissions and the matching uses-permission entries.
            val permissions = manifest.getElementsByTagName("permission")
            val usesPermissions = manifest.getElementsByTagName("uses-permission")
            for (i in 0 until permissions.length) {
                val permission = permissions.item(i) as? Element ?: continue
                val oldName = permission.getAttribute("android:name")
                if (oldName.startsWith('.')) continue

                val newName = oldName.replace(ORIGINAL_PACKAGE_NAME, newPackageName)
                permission.setAttribute("android:name", newName)

                for (j in 0 until usesPermissions.length) {
                    val usesPermission = usesPermissions.item(j) as? Element ?: continue
                    if (usesPermission.getAttribute("android:name") == oldName) {
                        usesPermission.setAttribute("android:name", newName)
                    }
                }
            }

            // Rename provider authorities so the clone can register them.
            val application = manifest.getElementsByTagName("application").item(0) as? Element
                ?: error("AndroidManifest.xml has no application element")
            val providers = application.getElementsByTagName("provider")
            for (i in 0 until providers.length) {
                val provider = providers.item(i) as? Element ?: continue
                val oldAuthority = provider.getAttribute("android:authorities")
                val newAuthority =
                    if (oldAuthority.startsWith("$ORIGINAL_PACKAGE_NAME.")) {
                        oldAuthority.replaceFirst(ORIGINAL_PACKAGE_NAME, newPackageName)
                    } else {
                        "${newPackageName}_$oldAuthority"
                    }
                provider.setAttribute("android:authorities", newAuthority)
                providerReplacements += oldAuthority to newAuthority
            }

            // Redirect launcher components to the custom icon, if one was supplied.
            customIcon?.let { installCustomIcon(it, manifest) }
        }

        // Change the app name. The unlocalized "Instagram" label lives in "values".
        if (!appName.isNullOrEmpty() && appName != "Instagram") {
            document("res/values/strings.xml").use { document ->
                val children = document.documentElement.childNodes
                for (i in 0 until children.length) {
                    val child = children.item(i) as? Element ?: continue
                    if (child.textContent == "Instagram") child.textContent = appName
                }
            }
        }

        // Replace bytecode references to the original package (video stories otherwise
        // play audio only) and to renamed provider authorities.
        val targets = listOf(string(ORIGINAL_PACKAGE_NAME)) +
            providerReplacements.map { string(it.first, StringComparisonType.CONTAINS) }
        transformStringReferences(targets) { value ->
            if (value == ORIGINAL_PACKAGE_NAME) return@transformStringReferences newPackageName

            val replacement = providerReplacements.firstOrNull { value.contains(it.first) }
            replacement?.let { value.replaceFirst(it.first, it.second) }
        }
    }

    finalize {
        if (installBesideOriginal != true) return@finalize

        // SettingsResourcePatch may add shortcuts after this patch runs.
        document("res/xml/shortcuts.xml").use { document ->
            val intents = document.getElementsByTagName("intent")
            for (i in 0 until intents.length) {
                val intent = intents.item(i) as? Element ?: continue
                if (intent.getAttribute("android:targetPackage") == ORIGINAL_PACKAGE_NAME) {
                    intent.setAttribute("android:targetPackage", packageName)
                }
            }
        }
    }
}

/**
 * Copies the chosen image into a new drawable resource, registers it in public.xml and points the
 * launcher components at it. No desktop-only image APIs (javax.imageio/java.awt) are used: Morphe
 * runs on Android, where those classes do not exist. The original image bytes are kept and the
 * launcher scales the drawable to its icon bounds; the source extension is preserved so both PNG
 * and JPEG picks work.
 */
private fun installCustomIcon(iconPath: String, manifest: Element) {
    val source = File(iconPath)
    check(source.isFile) { "Custom launcher icon does not exist: $iconPath" }

    val extension = source.extension.lowercase()
    check(extension in setOf("png", "jpg", "jpeg")) {
        "Custom launcher icon must be a PNG or JPEG image: $iconPath"
    }

    val destination = resourceFile("res/drawable/$CUSTOM_ICON_RESOURCE.$extension")
    destination.parentFile?.mkdirs()
    source.copyTo(destination, overwrite = true)

    document("res/values/public.xml").use { it.ensurePublicDrawableResource(CUSTOM_ICON_RESOURCE) }

    val reference = "@drawable/$CUSTOM_ICON_RESOURCE"
    val components = mutableListOf<Element>()
    listOf("application", "activity", "activity-alias").forEach { tag ->
        val nodes: NodeList = manifest.getElementsByTagName(tag)
        for (i in 0 until nodes.length) (nodes.item(i) as? Element)?.let { components += it }
    }
    components.forEach { component ->
        component.setAttribute("android:icon", reference)
        if (component.hasAttribute("android:roundIcon")) {
            component.setAttribute("android:roundIcon", reference)
        }
    }
}

private fun org.w3c.dom.Document.ensurePublicDrawableResource(name: String) {
    val publicNodes = getElementsByTagName("public")
    val typeIds = mutableListOf<Long>()
    for (i in 0 until publicNodes.length) {
        val resource = publicNodes.item(i) as? Element ?: continue
        if (resource.getAttribute("name") == name) {
            check(resource.getAttribute("type") == "drawable") {
                "Resource $name already exists with another type"
            }
            return
        }
        if (resource.getAttribute("type") == "drawable") {
            typeIds += resource.getAttribute("id").removePrefix("0x").toLong(16)
        }
    }

    check(typeIds.isNotEmpty()) { "Unable to allocate public drawable resource $name" }
    val typePrefix = typeIds.first() and 0xffff0000L
    check(typeIds.all { it and 0xffff0000L == typePrefix }) {
        "Unexpected public resource type IDs for drawable"
    }
    val newId = typeIds.max() + 1L
    check(newId and 0xffff0000L == typePrefix) { "No public resource IDs remain for drawable" }

    documentElement.appendChild(
        createElement("public").apply {
            setAttribute("type", "drawable")
            setAttribute("name", name)
            setAttribute("id", "0x%08x".format(newId))
        }
    )
}

private fun transformStringReferences(
    targets: List<StringFilter>,
    transform: (String) -> String?
) {
    targets.forEach { filter ->
        Fingerprint(filters = listOf(filter)).matchAllMethodIndicesForEach(requireMatches = false) { index ->
            val instruction = getInstruction(index)
            val found = instruction.getReference<StringReference>()!!.string
            val replacement = transform(found) ?: return@matchAllMethodIndicesForEach
            replaceInstruction(
                index,
                BuilderInstruction21c(
                    Opcode.CONST_STRING,
                    (instruction as OneRegisterInstruction).registerA,
                    ImmutableStringReference(replacement)
                )
            )
        }
    }
}
