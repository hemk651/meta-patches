/*
 * Meta Patches — Remove snooze warning.
 *
 * Ported from Instafel patcher-core "Remove Snooze Warning"
 * (https://github.com/mamiiblt/instafel) by mamiiblt (Muhammed Ali Bulut),
 * reworked for the Morphe patcher.
 *
 * The hook is found structurally instead of through a positional fingerprint filter: the method
 * that references DogfoodingEligibilityApi and converts the lockout duration with LONG_TO_INT.
 * This avoids version- and patcher-specific fingerprint matching behaviour.
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.patches.meta

import app.hemk651.patches.shared.MetaCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.classDefForEach
import app.morphe.patcher.patch.mutableClassDefBy
import app.morphe.patcher.patch.patch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val ELIGIBILITY_API = "Lcom/instagram/release/lockout/DogfoodingEligibilityApi;"

private fun Method.referencesEligibilityApi(): Boolean =
    implementation?.instructions?.any { instruction ->
        ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.definingClass == ELIGIBILITY_API
    } == true

private fun Method.longToIntIndices(): List<Int> =
    implementation?.instructions?.withIndex()
        ?.filter { it.value.opcode == Opcode.LONG_TO_INT }
        ?.map { it.index }
        ?: emptyList()

@Suppress("unused")
val removeSnoozeWarningPatch = patch(
    name = "Remove snooze warning",
    description = "Removes the \"build expired\" snooze warning dialog that alpha builds show.",
    default = true
) {
    compatibleWith(MetaCompatibilities.INSTAGRAM)

    execute {
        // The lockout duration is calculated as "days since the build release" and converted to
        // int before the eligibility check. Overriding that conversion with 0 makes the app treat
        // the build as freshly released, so the snooze dialog never appears.
        var host: Pair<String, Method>? = null
        var conversionIndex = -1
        classDefForEach { definition: ClassDef ->
            if (host != null) return@classDefForEach
            definition.methods.forEach { method ->
                if (!method.referencesEligibilityApi()) return@forEach
                val indices = method.longToIntIndices()
                if (indices.isNotEmpty()) {
                    host = definition.type to method
                    conversionIndex = indices.last()
                    return@classDefForEach
                }
            }
        }

        val (definingClass, method) = host
            ?: error("Instagram: unable to locate the dogfooding lockout conversion")
        val mutableMethod = mutableClassDefBy(definingClass).methods.first {
            it.name == method.name && it.parameterTypes.map { p -> p.toString() } == method.parameterTypes.map { p -> p.toString() }
        }

        val register = mutableMethod.getInstruction<TwoRegisterInstruction>(conversionIndex).registerA
        // const/4 can only address registers v0-v15; use const for any wider register.
        val replacement = if (register < 16) "const/4 v$register, 0x0" else "const v$register, 0x0"
        mutableMethod.replaceInstruction(conversionIndex, replacement)
    }
}
