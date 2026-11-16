/*
 * Meta Patches — Unlock developer options.
 *
 * Ported from InstaFL patcher-core "Unlock Developer Options"
 * (https://github.com/mamiiblt/instafel) by mamiiblt (Muhammed Ali Bulut),
 * reworked for the Morphe patcher.
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.patches.meta

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.classDefBy
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.returnLate
import app.hemk651.patches.shared.MetaCompatibilities

@Suppress("unused")
val unlockDeveloperOptionsPatch = patch(
    name = "Unlock developer options",
    description = "Unlocks the employee options (developer options) menu.",
    default = true
) {
    compatibleWith(MetaCompatibilities.INSTAGRAM)

    execute {
        // The gate is a static (UserSession)Z method of an obfuscated class that
        // reads a MobileConfig boolean and returns it. It is resolved by following
        // the call from a stable, non-obfuscated reference class.
        //
        // Original InstaFL patch walks a fallback chain of reference classes, and
        // overrides the gate result with "const v0, 0x1" after the single
        // "move-result v0". returnLate(true) achieves the same here: the gate
        // method still runs, but every return value is forced to true.
        var gateMatched = false

        for (referenceClass in employeeOptionsReferenceClasses) {
            val referenceClassDef = classDefBy(referenceClass) ?: continue

            val gateCallFingerprint = Fingerprint(
                filters = listOf(
                    methodCall(
                        returnType = "Z",
                        parameters = listOf("Lcom/instagram/common/session/UserSession;")
                    )
                )
            )

            val callMatch = gateCallFingerprint.matchOrNull(referenceClassDef)
                ?: continue

            // The defining class of the called (UserSession)Z method is the
            // obfuscated gate class.
            val gateClassType = callMatch.instructionMatches
                .first()
                .methodReference
                .definingClass

            val gateClassDef = classDefBy(gateClassType) ?: continue

            val gateMethodFingerprint = Fingerprint(
                isStatic = true,
                returnType = "Z",
                parameters = listOf("Lcom/instagram/common/session/UserSession;"),
                filters = listOf(
                    methodCall(
                        definingClass = "Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;",
                        returnType = "Z",
                        parameters = listOf("J")
                    )
                )
            )

            val gateMatch = gateMethodFingerprint.matchOrNull(gateClassDef)
                ?: continue

            gateMatch.method.returnLate(true)
            gateMatched = true
            break
        }

        if (!gateMatched) {
            throw PatchException(
                "Could not resolve the employee options gate from any reference class. " +
                        "Tried: ${employeeOptionsReferenceClasses.joinToString(", ")}"
            )
        }
    }
}
