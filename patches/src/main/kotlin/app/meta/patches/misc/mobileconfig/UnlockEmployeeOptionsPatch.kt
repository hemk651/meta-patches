package app.meta.patches.misc.mobileconfig

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.document
import app.morphe.patcher.patch.mutableClassDefByOrNull
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.meta.patches.shared.Constants.COMPATIBILITY_EDITS
import app.meta.patches.shared.Constants.COMPATIBILITY_INSTAGRAM
import app.meta.patches.shared.Constants.COMPATIBILITY_INSTANTS
import app.meta.patches.shared.Constants.DEFAULT_MC_OVERRIDES_JSON
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/meta/extension/MetaConfigOverrides;"

/** Name of the static helper method the extension exposes. */
private const val EXTENSION_WRITE_METHOD = "writeOverrides"

/** Name of the synthetic static wrapper method added to the app's Application class. */
private const val HOOK_METHOD_NAME = "morpheWriteMcOverrides"

/** Maximum Application superclass chain length to walk before giving up. */
private const val MAX_SUPERCLASS_DEPTH = 32

private const val OBJECT_TYPE = "Ljava/lang/Object;"

/**
 * Smali-string-literal escaping, so any option JSON can be safely embedded in a const-string.
 */
private fun escapeForSmali(value: String): String = buildString(value.length) {
    for (char in value) {
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
}

/**
 * Light sanity check for the overrides JSON: it must look like a single-line JSON object.
 * Full parsing is intentionally avoided so no JSON library has to be bundled with the patches.
 */
private fun isValidOverridesJson(value: String?): Boolean {
    if (value == null) return false
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return true // empty falls back to the extension default
    if (trimmed.contains('\n') || trimmed.contains('\r')) return false
    return trimmed.startsWith("{") && trimmed.endsWith("}")
}

/**
 * Discovers the app's Application class, then hooks the earliest Android lifecycle method
 * available in its class hierarchy so the extension runs before any Meta framework code.
 */
private class ApplicationHook private constructor(
    val definingClass: MutableClass,
    val method: MutableMethod,
    /** The parameter register holding the [android.content.Context] to pass to the extension. */
    val smaliRegister: String,
) {
    companion object {
        /**
         * Walks the superclass chain of the given Application class and picks a hook method:
         * `attachBaseContext(Context)` is preferred because it runs before every content
         * provider and service; a plain `onCreate()` is the fallback.
         */
        fun find(applicationClass: MutableClass): ApplicationHook {
            var currentClass: MutableClass? = applicationClass

            var attachBase: ApplicationHook? = null
            var onCreate: ApplicationHook? = null
            var depth = 0

            while (currentClass != null && depth < MAX_SUPERCLASS_DEPTH) {
                for (method in currentClass.methods) {
                    if (method.accessFlags and AccessFlags.ABSTRACT.value != 0) continue

                    if (attachBase == null &&
                        method.name == "attachBaseContext" &&
                        method.returnType == "V" &&
                        method.parameterTypes.map(CharSequence::toString) == listOf("Landroid/content/Context;")
                    ) {
                        attachBase = ApplicationHook(currentClass, method, "p1")
                    }

                    if (onCreate == null &&
                        method.name == "onCreate" &&
                        method.returnType == "V" &&
                        method.parameterTypes.isEmpty()
                    ) {
                        onCreate = ApplicationHook(currentClass, method, "p0")
                    }
                }

                if (attachBase != null && onCreate != null) break

                val superclass = currentClass.superclass
                currentClass = if (superclass == null || superclass == OBJECT_TYPE) {
                    null
                } else {
                    mutableClassDefByOrNull(superclass)
                }
                depth++
            }

            // attachBaseContext always runs before onCreate, so it is strictly better.
            return attachBase
                ?: onCreate
                ?: throw PatchException(
                    "Could not find a hookable attachBaseContext or onCreate method in the " +
                        "Application class hierarchy of ${applicationClass.type}"
                )
        }
    }
}

@Suppress("unused")
val unlockEmployeeOptionsPatch = patch(
    name = "Unlock employee options",
    description =
        "Writes a MobileConfig overrides file (mc_overrides.json) into the app's storage at startup, " +
            "which unlocks Meta's internal Employee Options / developer options menu.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM, COMPATIBILITY_EDITS, COMPATIBILITY_INSTANTS)

    val overridesJson by stringOption(
        key = "mc_overrides_json",
        default = DEFAULT_MC_OVERRIDES_JSON,
        title = "MobileConfig overrides JSON",
        description =
            "The JSON written to mobileconfig/mc_overrides.json at app startup. " +
                "Leave unchanged to use the default employee options unlock.",
        required = true,
    ) {
        isValidOverridesJson(it)
    }

    extendWith("extensions/meta.mpe")

    execute {
        val json = overridesJson!!.trim().ifEmpty { DEFAULT_MC_OVERRIDES_JSON }

        //
        // Resolve the Application class declared in the merged manifest.
        //
        val applicationType = document("AndroidManifest.xml").use { manifest ->
            val application = manifest.documentElement.getElementsByTagName("application").item(0) as? Element
                ?: throw PatchException("AndroidManifest.xml has no <application> element")

            val packageName = manifest.documentElement.getAttribute("package")
            val declaredName = application.getAttribute("android:name")
            if (declaredName.isEmpty()) {
                throw PatchException(
                    "The app uses the default android.app.Application class, which cannot be patched. " +
                        "This app is not supported."
                )
            }

            // Manifest class names can be relative (".InstagramApp") or fully qualified.
            val className = when {
                declaredName.startsWith(".") -> packageName + declaredName
                declaredName.contains('.') -> declaredName
                else -> "$packageName.$declaredName"
            }

            "L${className.replace('.', '/')};"
        }

        val applicationClass = mutableClassDefByOrNull(applicationType)
            ?: throw PatchException("Application class $applicationType was not found in the app's dex files")

        val hook = ApplicationHook.find(applicationClass)

        //
        // Add a synthetic static wrapper method with registers we fully control. It holds the
        // overrides JSON in a const-string and forwards the context to the extension:
        //
        //     const-string v0, "<json>"
        //     invoke-static {p0, v0}, MetaConfigOverrides->writeOverrides(Context, String)V
        //     return-void
        //
        if (hook.definingClass.methods.any { it.name == HOOK_METHOD_NAME }) {
            throw PatchException(
                "${hook.definingClass.type} already defines $HOOK_METHOD_NAME; the bundle may have been applied twice"
            )
        }

        hook.definingClass.methods.add(
            ImmutableMethod(
                hook.definingClass.type,
                HOOK_METHOD_NAME,
                listOf(ImmutableMethodParameter("Landroid/content/Context;", emptySet(), null)),
                "V",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                emptySet(),
                emptySet(),
                MutableMethodImplementation(2),
            ).toMutable().apply {
                addInstructions(
                    0,
                    """
                    const-string v0, "${escapeForSmali(json)}"
                    invoke-static {p0, v0}, $EXTENSION_CLASS_DESCRIPTOR->$EXTENSION_WRITE_METHOD(Landroid/content/Context;Ljava/lang/String;)V
                    return-void
                    """.trimIndent(),
                )
            },
        )

        //
        // Invoke the wrapper at index 0 of the hook method. invoke-static/range encodes register
        // numbers as 16-bit, so it works even in methods with more than 16 registers.
        //     attachBaseContext(Context): p1 is the base context.
        //     onCreate():                p0 is the Application instance, itself a Context.
        //
        hook.method.addInstructions(
            0,
            "invoke-static/range {${hook.smaliRegister} .. ${hook.smaliRegister}}, " +
                "${hook.definingClass.type}->$HOOK_METHOD_NAME(Landroid/content/Context;)V",
        )
    }
}
