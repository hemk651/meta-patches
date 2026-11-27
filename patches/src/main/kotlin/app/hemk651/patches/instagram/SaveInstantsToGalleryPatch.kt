/*
 * Meta Patches — main Instagram: save friends' Instants to the gallery.
 *
 * Ported from Wagg13Patch_Morphe (https://github.com/WaggBR/Wagg13Patch_Morphe)
 * by Wagg13, licensed under the GNU General Public License version 3.
 * Rebranded for Meta Patches: helpers live in app.hemk651.meta.extension.instants.
 *
 * VERSION-AGNOSTIC: no obfuscated class/method names are hard-coded. The menu action enum is
 * found by its stable enum constant names, the item factory and menu composable by their
 * call graph, the selection handler by its check-cast/int-read/enum-entries shape, and the
 * viewer state parameter and video helper by their Media-field/shape relationships. This lets
 * one patch work on both 458.0.0.0.3 and 458.0.0.0.57 (and future builds with the same UI).
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

package app.hemk651.patches.instagram

import app.hemk651.patches.shared.MetaCompatibilities
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.classDefByOrNull
import app.morphe.patcher.patch.classDefForEach
import app.morphe.patcher.patch.mutableClassDefBy
import app.morphe.patcher.patch.patch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val SAVE_HELPER = "Lapp/hemk651/meta/extension/instants/InstantsSaveImage;"
private const val STRINGS_HELPER = "Lapp/hemk651/meta/extension/instants/InstantsStrings;"
private const val GALLERY_HELPER = "Lapp/hemk651/meta/extension/instants/InstantsGalleryHelper;"

private const val MEDIA_TYPE = "Lcom/instagram/feed/media/Media;"
private const val EXTENDED_IMAGE_URL = "Lcom/instagram/model/mediasize/ExtendedImageUrl;"
private const val ENUM_ENTRIES_TYPE = "Lkotlin/enums/EnumEntries;"
private const val STRING_TYPE = "Ljava/lang/String;"
private const val ACC_STATIC = 0x0008

/** Enum constant names of the Instants context-menu action enum (R8 keeps enum names). */
private val MENU_ACTION_NAMES = setOf("VIEW_PROFILE", "MUTE", "UNFOLLOW", "REPORT", "SKIP_AUTHOR")

private data class HostMethod(
    val definingClass: String,
    val name: String,
    val parameters: List<String>,
    val returnType: String,
) {
    val descriptor: String
        get() = "$definingClass->$name(${parameters.joinToString("")})$returnType"
}

private fun Method.host(definingClass: String) =
    HostMethod(definingClass, name, parameterTypes.map { it.toString() }, returnType)

private fun methodReference(instruction: Instruction): MethodReference? =
    (instruction as? ReferenceInstruction)?.reference as? MethodReference

private fun fieldReference(instruction: Instruction): FieldReference? =
    (instruction as? ReferenceInstruction)?.reference as? FieldReference

private fun stringReference(instruction: Instruction): String? =
    ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string

private fun instructionsOf(method: Method): List<Instruction> =
    method.implementation?.instructions?.toList() ?: emptyList()

private fun isStatic(method: Method) = method.accessFlags and ACC_STATIC != 0

/** The action enum: its static initializer contains the stable action names. */
private fun findMenuActionEnum(): String {
    var result: String? = null
    classDefForEach { definition ->
        if (result != null) return@classDefForEach
        val initializer = definition.methods.firstOrNull { it.name == "<clinit>" } ?: return@classDefForEach
        val strings = instructionsOf(initializer).mapNotNull(::stringReference).toSet()
        // Require all five action names; logging enums contain only a subset of them.
        if (MENU_ACTION_NAMES.all { it in strings }) result = definition.type
    }
    return result ?: error("Instagram: unable to locate the Instants menu action enum")
}

/**
 * Locates the app's menu-item factory: a static 3-argument method whose second parameter is the
 * action enum and whose third is a String (the label). Its call sites identify the menu builder.
 */
private fun findItemFactory(enumType: String): Pair<HostMethod, HostMethod> {
    val candidates = mutableListOf<HostMethod>()
    classDefForEach { definition ->
        definition.methods.forEach { method ->
            if (!isStatic(method)) return@forEach
            val parameters = method.parameterTypes.map { it.toString() }
            if (parameters.size == 3 &&
                parameters[1] == enumType &&
                parameters[2] == STRING_TYPE &&
                method.returnType != "V"
            ) {
                candidates += method.host(definition.type)
            }
        }
    }
    check(candidates.isNotEmpty()) { "Instagram: unable to locate the Instants menu item factory" }

    var bestCaller: HostMethod? = null
    var bestFactory: HostMethod? = null
    var bestCalls = 1
    classDefForEach { definition ->
        definition.methods.forEach { method ->
            candidates.forEach { candidate ->
                val calls = instructionsOf(method).count { instruction ->
                    methodReference(instruction)?.let { reference ->
                        reference.definingClass == candidate.definingClass &&
                            reference.name == candidate.name &&
                            reference.parameterTypes.map { it.toString() } == candidate.parameters
                    } == true
                }
                if (calls > bestCalls) {
                    bestCalls = calls
                    bestCaller = method.host(definition.type)
                    bestFactory = candidate
                }
            }
        }
    }

    return (bestFactory ?: error("Instagram: the Instants menu item factory is never called")) to
        (bestCaller ?: error("Instagram: unable to locate the Instants menu builder"))
}

/**
 * Index of the instruction that reads the selected item's id:
 *   check-cast vX, <event>; ...; iget vN, vX, <event>.<id>:I; sget-object <enum entries>
 */
private fun List<Instruction>.selectedItemIdReadIndex(enumType: String): Int {
    for (index in indices) {
        val instruction = this[index]
        if (instruction.opcode != Opcode.IGET) continue
        val field = fieldReference(instruction) ?: continue
        if (field.type != "I") continue
        val castBefore = (maxOf(0, index - 8) until index).any {
            this[it].opcode == Opcode.CHECK_CAST &&
                ((this[it] as? ReferenceInstruction)?.reference as? TypeReference)?.type == field.definingClass
        }
        val entriesAfter = ((index + 1)..minOf(lastIndex, index + 3)).any {
            this[it].opcode == Opcode.SGET_OBJECT &&
                fieldReference(this[it])?.let { entries ->
                    entries.type == ENUM_ENTRIES_TYPE && entries.definingClass == enumType
                } == true
        }
        if (castBefore && entriesAfter) return index
    }
    return -1
}

/** The click handler lambda that maps the selected item id to an action enum entry. */
private fun findSelectionHandler(enumType: String): HostMethod {
    var result: HostMethod? = null
    classDefForEach { definition ->
        definition.methods.forEach { method ->
            if (method.parameterTypes.size != 1 || method.returnType != "Ljava/lang/Object;") return@forEach
            if (instructionsOf(method).selectedItemIdReadIndex(enumType) >= 0 && result == null) {
                result = method.host(definition.type)
            }
        }
    }
    return result ?: error("Instagram: unable to locate the Instants menu selection handler")
}

private fun classHasMediaField(type: String): Boolean {
    if (!type.startsWith("L") || type.startsWith("Lkotlin/") || type.startsWith("Ljava/")) return false
    val definition = classDefByOrNull { it.type == type } ?: return false
    return (definition.instanceFields + definition.staticFields).any { it.type == MEDIA_TYPE }
}

/**
 * The viewer state parameter of the menu builder: its type exposes a no-argument accessor whose
 * result holds a Media field. This is the object the extension inspects to find the current snap.
 */
private fun findViewerStateParameter(menu: HostMethod): Int {
    menu.parameters.forEachIndexed { index, type ->
        if (!type.startsWith("L") || type.startsWith("Lkotlin/") || type.startsWith("Ljava/")) return@forEachIndexed
        val definition = classDefByOrNull { it.type == type } ?: return@forEachIndexed
        val found = definition.methods.any { method ->
            method.parameterTypes.isEmpty() &&
                classHasMediaField(method.returnType.toString())
        }
        if (found) return index
    }
    error("Instagram: unable to locate the Instants viewer state parameter")
}

/**
 * The app's original-image helper: a static (Media[, Long]) -> ExtendedImageUrl method. Its
 * defining class changes between builds (MediaExtKt in older builds, LX/06vu; in newer ones).
 */
private fun findOriginalImageHelper(): Pair<String, String>? {
    var result: Pair<String, String>? = null
    var bestScore = 0
    classDefForEach { definition ->
        definition.methods.forEach { method ->
            if (!isStatic(method)) return@forEach
            val parameters = method.parameterTypes.map { it.toString() }
            if (parameters.isEmpty() || parameters[0] != MEDIA_TYPE ||
                method.returnType != EXTENDED_IMAGE_URL ||
                parameters.size !in 1..2
            ) return@forEach

            val score = if (parameters.size == 2 && parameters[1] == "Ljava/lang/Long;") 2 else 1
            if (score > bestScore) {
                bestScore = score
                result = definition.type to method.name
            }
        }
    }
    return result
}

/** A class with a static (Media) -> String helper, used by the extension for video sources. */
private fun findVideoSourceClass(): String {
    var result: String? = null
    classDefForEach { definition ->
        if (result != null) return@classDefForEach
        definition.methods.forEach { method ->
            if (isStatic(method) &&
                method.parameterTypes.size == 1 &&
                method.parameterTypes[0] == MEDIA_TYPE &&
                method.returnType == STRING_TYPE
            ) {
                result = definition.type
            }
        }
    }
    return result ?: "LX/0252;" // harmless fallback: the extension ignores a class without the helper
}

@Suppress("unused")
val saveInstantsToGalleryPatch = patch(
    name = "Save Instants to gallery",
    description = "Adds a \"Save to gallery\" option to the Instants menu, so you can keep the photo " +
        "or video you are viewing. Hooks are discovered at patch time, so this works across builds.",
) {
    compatibleWith(MetaCompatibilities.INSTAGRAM)

    extendWith("extensions/extension.mpe")

    execute {
        // 1) Discover the version-specific hosts from stable structural anchors.
        val enumType = findMenuActionEnum()
        val (factory, menu) = findItemFactory(enumType)
        val selection = findSelectionHandler(enumType)
        val viewerStateParameter = findViewerStateParameter(menu)
        val videoSourceClass = findVideoSourceClass()
        val originalImageHelper = findOriginalImageHelper()

        println(
            "Instants save hooks: enum=$enumType factory=${factory.definingClass}->${factory.name} " +
                "menu=${menu.definingClass}->${menu.name} " +
                "selection=${selection.definingClass}->${selection.name} " +
                "state=p$viewerStateParameter video=$videoSourceClass original=$originalImageHelper"
        )

        // 2) Add one item to the viewer menu, built with the app's own item factory.
        val menuMethod = mutableClassDefBy(menu.definingClass).methods
            .first { it.host(menu.definingClass) == menu }
        val menuInstructions = instructionsOf(menuMethod)
        val factoryCalls = menuInstructions.withIndex().filter { (_, instruction) ->
            methodReference(instruction)?.let {
                it.definingClass == factory.definingClass && it.name == factory.name
            } == true
        }
        check(factoryCalls.size >= 2) {
            "Instagram: expected the Instants menu item calls, found ${factoryCalls.size}"
        }

        fun enumFieldBefore(callIndex: Int): Int =
            (callIndex - 1 downTo 0).first { index ->
                menuInstructions[index].opcode == Opcode.SGET_OBJECT &&
                    fieldReference(menuInstructions[index])?.type == enumType
            }

        val lastCall = factoryCalls.last()
        val callInstruction = lastCall.value as FiveRegisterInstruction
        val composerRegister = callInstruction.registerC
        val enumRegister = callInstruction.registerD
        val textRegister = callInstruction.registerE

        val addInstruction = (lastCall.index + 1 until menuInstructions.size)
            .map { menuInstructions[it] }
            .first {
                it.opcode == Opcode.INVOKE_VIRTUAL &&
                    methodReference(it)?.name == "add"
            }
        val listRegister = (addInstruction as FiveRegisterInstruction).registerC
        val addReference = methodReference(addInstruction)!!
        check(listOf(composerRegister, enumRegister, textRegister, listRegister).all { it < 16 }) {
            "Instagram: Instants menu registers do not fit a 4-bit encoding"
        }

        val baseField = fieldReference(menuInstructions[enumFieldBefore(factoryCalls[1].index)])!!
        val factoryMethod = mutableClassDefBy(factory.definingClass).methods
            .first { it.host(factory.definingClass) == factory }
        val factoryInstructions = instructionsOf(factoryMethod)
        val painterCallIndex = factoryInstructions.indexOfFirst { instruction ->
            instruction.opcode == Opcode.INVOKE_STATIC && methodReference(instruction)?.let { reference ->
                reference.parameterTypes.size == 2 &&
                    reference.parameterTypes[1].toString() == "I" && reference.returnType != "V"
            } == true
        }
        check(painterCallIndex >= 0) { "Instagram: icon painter call not found in the menu item factory" }
        val painterReference = methodReference(factoryInstructions[painterCallIndex])!!
        val painterResRegister = (factoryInstructions[painterCallIndex] as FiveRegisterInstruction).registerD
        val iconField = (0 until painterCallIndex).reversed()
            .map { factoryInstructions[it] }
            .first {
                it.opcode == Opcode.IGET && (it as TwoRegisterInstruction).registerA == painterResRegister
            }
            .let { fieldReference(it)!! }
        check(iconField.definingClass == enumType && iconField.type == "I") {
            "Instagram: icon resource field not found on the Instants menu action enum"
        }
        val painterDescriptor = painterReference.descriptor

        val insertAt = enumFieldBefore(lastCall.index)
        menuMethod.addInstructions(
            insertAt,
            """
            sget-object v$enumRegister, ${baseField.definingClass}->${baseField.name}:${baseField.type}
            invoke-static {}, $STRINGS_HELPER->saveToGallery()Ljava/lang/String;
            move-result-object v$textRegister
            invoke-static {v$composerRegister, v$enumRegister, v$textRegister}, ${factory.descriptor}
            move-result-object v$textRegister
            iget v$enumRegister, v$enumRegister, ${iconField.definingClass}->${iconField.name}:I
            invoke-static {v$enumRegister}, $SAVE_HELPER->downloadIcon(I)I
            move-result v$enumRegister
            invoke-static {v$composerRegister, v$enumRegister}, $painterDescriptor
            move-result-object v$enumRegister
            invoke-static {v$textRegister, v$enumRegister}, $SAVE_HELPER->markMenuItem(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            move-result-object v$textRegister
            invoke-virtual {v$listRegister, v$textRegister}, ${addReference.descriptor}
            """.trimIndent(),
        )

        // 3) Report the viewer state, the video-source class and the original-image helper.
        val originalHelperInstructions = originalImageHelper?.let { (type, name) ->
            """
            const-class v1, $type
            const-string v2, "$name"
            invoke-static {v1, v2}, $SAVE_HELPER->setOriginalUrlHelper(Ljava/lang/Class;Ljava/lang/String;)V
            """.trimIndent()
        } ?: ""
        menuMethod.addInstructions(
            0,
            """
            invoke-static/range {p$viewerStateParameter .. p$viewerStateParameter}, $SAVE_HELPER->onViewerState(Ljava/lang/Object;)V
            const-class v0, $videoSourceClass
            invoke-static {v0}, $SAVE_HELPER->setVideoSourceClass(Ljava/lang/Class;)V
            $originalHelperInstructions
            """.trimIndent(),
        )

        // 4) Report the id of the selected item.
        val selectionMethod = mutableClassDefBy(selection.definingClass).methods
            .first { it.host(selection.definingClass) == selection }
        val selectionInstructions = instructionsOf(selectionMethod)
        val idRead = selectionInstructions.selectedItemIdReadIndex(enumType)
        check(idRead >= 0) { "Instagram: Instants menu selection handler not found" }
        val idRegister = (selectionInstructions[idRead] as TwoRegisterInstruction).registerA
        selectionMethod.addInstructions(
            idRead + 1,
            """
            invoke-static/range {v$idRegister .. v$idRegister}, $SAVE_HELPER->onMenuSelected(I)V
            """.trimIndent(),
        )

        // 5) Activity, for the toast and the download icon lookup.
        Fingerprint(
            definingClass = "Lcom/instagram/base/activity/BaseFragmentActivity;",
            name = "onCreate",
            returnType = "V",
            parameters = listOf("Landroid/os/Bundle;"),
        ).method.addInstructions(
            0,
            """
            invoke-static/range {p0 .. p0}, $GALLERY_HELPER->setActivity(Landroid/app/Activity;)V
            """.trimIndent(),
        )
    }
}

private val MethodReference.descriptor: String
    get() = "$definingClass->$name(${parameterTypes.joinToString("")})$returnType"
