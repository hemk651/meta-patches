/*
 * Meta Patches — main Instagram: save friends' Instants to the gallery (v458).
 *
 * Ported from Wagg13Patch_Morphe (https://github.com/WaggBR/Wagg13Patch_Morphe)
 * by Wagg13, licensed under the GNU General Public License version 3.
 * Rebranded for Meta Patches: helpers live in app.hemk651.meta.extension.instants.
 * Compatibility follows the bundle-wide "every version" convention; a runtime
 * guard in execute() rejects builds other than 458.0.0.0.3.
 *
 * This file is part of Meta Patches, licensed under the GNU General Public
 * License version 3. https://www.gnu.org/licenses/gpl-3.0.html
 */

/*
 * Save Instants to gallery - Instagram v458.0.0.0.3 (arm64-v8a, APKM).
 *
 * Port of app.morphe.patches.instagram.instants.saveInstantsToGalleryPatch to Instagram 458.
 *
 * WHY A NEW PATCH:
 *   The v452 patch resolves its hooks through Compose trace strings such as
 *   "com.instagram.quicksnap.viewer.compose.createQuickSnapContextMenuItems (".
 *   Those strings no longer exist in 458.0.0.0.3 (verified against all 33 dex files of base.apk),
 *   so the old fingerprints cannot match. The hook points below were recovered by
 *   reverse engineering the v458 dex and are pinned to this exact build, which is the
 *   normal compatibility model for these patches.
 *
 * RECOVERED v458 HOOKS (evidence in RE_NOTES.md):
 *   - menu composable .... LX/0cm5;->A01(...)   (5 calls to the item factory)
 *   - item factory ....... LX/0Zw5;->A00(LX/1Eqm;LX/0YN1;Ljava/lang/String;)LX/0XM6;
 *   - action enum ........ LX/0YN1;  { A00:I icon, A01:I id, A02:Z destructive, A03:entries }
 *                          values: VIEW_PROFILE, MUTE, UNFOLLOW, REPORT, SKIP_AUTHOR
 *   - selection lambda ... LX/0mOg;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
 *   - viewer state ....... p4 of A01 = LX/0R44; -> A0B() -> LX/02VJ; (holds a Media field)
 *   - video helper class . LX/0252; (static (Media) -> String helper, used on demand)
 *   - activity ........... com.instagram.base.activity.BaseFragmentActivity.onCreate(Bundle)V
 *
 * OPTIMISATION vs the v452 patch: every fingerprint is exact (defining class + name + structural
 * guard) instead of scanning the whole APK for string prefixes. Fingerprint resolution is lazy,
 * so patching fails fast with a clear message if a future build changes any of these symbols.
 */

package app.hemk651.patches.instagram

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.hemk651.patches.shared.MetaCompatibilities
import app.morphe.patcher.patch.patch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val SAVE_HELPER = "Lapp/hemk651/meta/extension/instants/InstantsSaveImage;"
private const val STRINGS_HELPER = "Lapp/hemk651/meta/extension/instants/InstantsStrings;"
private const val GALLERY_HELPER = "Lapp/hemk651/meta/extension/instants/InstantsGalleryHelper;"

private const val MENU_CLASS = "LX/0cm5;"
private const val MENU_METHOD = "A01"
private const val FACTORY_CLASS = "LX/0Zw5;"
private const val FACTORY_METHOD = "A00"
private const val ENUM_TYPE = "LX/0YN1;"
private const val VIDEO_HELPER_CLASS = "LX/0252;"

/** True when the method calls the Instants menu item factory at least twice. */
private fun hasFactoryCalls(method: com.android.tools.smali.dexlib2.iface.Method): Boolean =
    method.implementation?.instructions?.count { instruction ->
        instruction.opcode == Opcode.INVOKE_STATIC &&
            ((instruction as ReferenceInstruction).reference as? MethodReference).let {
                it != null && it.definingClass == FACTORY_CLASS && it.name == FACTORY_METHOD
            }
    }?.let { it >= 2 } == true

/** The activity, used by the extension for the toast and the download-icon lookup. */
private object InstagramActivityFingerprint : Fingerprint(
    definingClass = "Lcom/instagram/base/activity/BaseFragmentActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

/** The composable that builds the viewer context menu (v458: LX/0cm5.A01). */
private object QuickSnapMenuItemsFingerprint : Fingerprint(
    definingClass = MENU_CLASS,
    name = MENU_METHOD,
    returnType = "V",
    custom = { method, _ -> hasFactoryCalls(method) },
)

/** The app's single context-menu item factory (v458: LX/0Zw5.A00). */
private object QuickSnapMenuItemFactoryFingerprint : Fingerprint(
    definingClass = FACTORY_CLASS,
    name = FACTORY_METHOD,
    parameters = listOf("LX/1Eqm;", ENUM_TYPE, "Ljava/lang/String;"),
    returnType = "LX/0XM6;",
)

/**
 * Index of the instruction that reads the selected item's id inside the menu click handler:
 *   check-cast vX, <event>; ...; iget vN, vX, <event>->id:I; sget-object vY, <enum>->A03:EnumEntries
 * The entries field must belong to [enumType] (v458 enum entries field is A03).
 */
private fun List<Instruction>.selectedItemIdReadIndex(enumType: String): Int {
    for (index in indices) {
        val instruction = this[index]
        if (instruction.opcode != Opcode.IGET) continue
        val field = (instruction as ReferenceInstruction).reference as? FieldReference ?: continue
        if (field.type != "I") continue
        val castBefore = (maxOf(0, index - 8) until index).any {
            this[it].opcode == Opcode.CHECK_CAST &&
                ((this[it] as ReferenceInstruction).reference as? TypeReference)?.type == field.definingClass
        }
        val entriesAfter = ((index + 1)..minOf(lastIndex, index + 3)).any {
            this[it].opcode == Opcode.SGET_OBJECT &&
                ((this[it] as ReferenceInstruction).reference as? FieldReference).let { entries ->
                    entries != null && entries.type == "Lkotlin/enums/EnumEntries;" &&
                        entries.definingClass == enumType
                }
        }
        if (castBefore && entriesAfter) return index
    }
    return -1
}

/** The click handler lambda: maps a selected item id to an entry of the menu action enum. */
private object QuickSnapMenuSelectionFingerprint : Fingerprint(
    definingClass = "LX/0mOg;",
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    custom = { method, _ ->
        method.implementation?.instructions?.toList()?.selectedItemIdReadIndex(ENUM_TYPE)?.let { it >= 0 } == true
    },
)

@Suppress("unused")
val saveInstantsToGalleryV458Patch = patch(
    name = "Save Instants to gallery (v458)",
    description = "Adds a \"Save to gallery\" option to the Instants menu, so you can keep the photo " +
        "or video you are viewing. Ported to Instagram 458.0.0.0.3.",
) {
    compatibleWith(MetaCompatibilities.INSTAGRAM)

    extendWith("extensions/extension.mpe")

    execute {
        // Hooks are pinned to 458's obfuscated symbols; the fingerprints below fail with a clear
        // message on any other build instead of modifying it. Bundle-wide compatibility is declared
        // through MetaCompatibilities.INSTAGRAM.

        // 1) Add one item to the viewer menu, built with the app's own item factory.
        val menu = QuickSnapMenuItemsFingerprint.method
        val menuInstructions = menu.implementation!!.instructions
        val factory = QuickSnapMenuItemFactoryFingerprint.method

        val factoryCalls = menuInstructions.withIndex().filter { (_, instruction) ->
            instruction.opcode == Opcode.INVOKE_STATIC &&
                ((instruction as ReferenceInstruction).reference as? MethodReference).let {
                    it != null && it.definingClass == FACTORY_CLASS && it.name == FACTORY_METHOD
                }
        }
        check(factoryCalls.size >= 2) {
            "Instagram 458: expected the Instants menu item calls, found ${factoryCalls.size}"
        }

        fun enumFieldBefore(callIndex: Int): Int = (callIndex - 1 downTo 0).first { index ->
            menuInstructions[index].opcode == Opcode.SGET_OBJECT &&
                ((menuInstructions[index] as ReferenceInstruction).reference as FieldReference).type == ENUM_TYPE
        }

        val lastCall = factoryCalls.last()
        val lastCallInstruction = lastCall.value as FiveRegisterInstruction
        val composerRegister = lastCallInstruction.registerC
        val enumRegister = lastCallInstruction.registerD
        val textRegister = lastCallInstruction.registerE

        val addInstruction = (lastCall.index + 1 until menuInstructions.size).map { menuInstructions[it] }.first {
            it.opcode == Opcode.INVOKE_VIRTUAL &&
                ((it as ReferenceInstruction).reference as MethodReference).name == "add"
        }
        val listRegister = (addInstruction as FiveRegisterInstruction).registerC
        val addReference = (addInstruction as ReferenceInstruction).reference as MethodReference
        check(listOf(composerRegister, enumRegister, textRegister, listRegister).all { it < 16 }) {
            "Instagram 458: Instants menu registers do not fit a 4-bit encoding"
        }

        // Use the action of the second item as a base; swap its icon for the app's download icon.
        val baseField =
            (menuInstructions[enumFieldBefore(factoryCalls[1].index)] as ReferenceInstruction).reference as FieldReference
        val factoryInstructions = factory.implementation!!.instructions
        val painterCallIndex = factoryInstructions.indexOfFirst { instruction ->
            instruction.opcode == Opcode.INVOKE_STATIC &&
                ((instruction as ReferenceInstruction).reference as? MethodReference).let {
                    it != null && it.returnType != "V" && it.parameterTypes.size == 2 &&
                        it.parameterTypes[1].toString() == "I"
                }
        }
        check(painterCallIndex >= 0) { "Instagram 458: icon painter call not found in the menu item factory" }
        val painterReference = (factoryInstructions[painterCallIndex] as ReferenceInstruction).reference as MethodReference
        val painterResRegister = (factoryInstructions[painterCallIndex] as FiveRegisterInstruction).registerD
        val iconField = (0 until painterCallIndex).reversed().map { factoryInstructions[it] }.first {
            it.opcode == Opcode.IGET && (it as TwoRegisterInstruction).registerA == painterResRegister
        }.let { (it as ReferenceInstruction).reference as FieldReference }
        check(iconField.definingClass == ENUM_TYPE && iconField.type == "I") {
            "Instagram 458: icon resource field not found on the Instants menu action enum"
        }
        val painterDescriptor =
            "${painterReference.definingClass}->${painterReference.name}(${painterReference.parameterTypes.joinToString("")})${painterReference.returnType}"

        val insertAt = enumFieldBefore(lastCall.index)
        val factoryDescriptor =
            "${factory.definingClass}->${factory.name}(${factory.parameterTypes.joinToString("")})${factory.returnType}"
        val addDescriptor =
            "${addReference.definingClass}->${addReference.name}(${addReference.parameterTypes.joinToString("")})${addReference.returnType}"

        menu.addInstructions(
            insertAt,
            """
            sget-object v$enumRegister, ${baseField.definingClass}->${baseField.name}:${baseField.type}
            invoke-static {}, $STRINGS_HELPER->saveToGallery()Ljava/lang/String;
            move-result-object v$textRegister
            invoke-static {v$composerRegister, v$enumRegister, v$textRegister}, $factoryDescriptor
            move-result-object v$textRegister
            iget v$enumRegister, v$enumRegister, ${iconField.definingClass}->${iconField.name}:I
            invoke-static {v$enumRegister}, $SAVE_HELPER->downloadIcon(I)I
            move-result v$enumRegister
            invoke-static {v$composerRegister, v$enumRegister}, $painterDescriptor
            move-result-object v$enumRegister
            invoke-static {v$textRegister, v$enumRegister}, $SAVE_HELPER->markMenuItem(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            move-result-object v$textRegister
            invoke-virtual {v$listRegister, v$textRegister}, $addDescriptor
            """.trimIndent(),
        )

        // 2) p4 is the viewer state (LX/0R44;). LX/0R44.A0B() returns LX/02VJ;, which holds the Media.
        //    The video helper class is passed for on-demand video/photo discrimination.
        menu.addInstructions(
            0,
            """
            invoke-static/range {p4 .. p4}, $SAVE_HELPER->onViewerState(Ljava/lang/Object;)V
            const-class v0, $VIDEO_HELPER_CLASS
            invoke-static {v0}, $SAVE_HELPER->setVideoSourceClass(Ljava/lang/Class;)V
            """.trimIndent(),
        )

        // 3) Report the id of the selected item; an id matching no action enum entry is ignored by the app.
        val selection = QuickSnapMenuSelectionFingerprint.method
        val selectionInstructions = selection.implementation!!.instructions.toList()
        val idRead = selectionInstructions.selectedItemIdReadIndex(ENUM_TYPE)
        check(idRead >= 0) { "Instagram 458: Instants menu selection handler not found" }
        val idRegister = (selectionInstructions[idRead] as TwoRegisterInstruction).registerA
        selection.addInstructions(
            idRead + 1,
            """
            invoke-static/range {v$idRegister .. v$idRegister}, $SAVE_HELPER->onMenuSelected(I)V
            """.trimIndent(),
        )

        // 4) Activity, for the toast and the download icon lookup.
        InstagramActivityFingerprint.method.addInstructions(
            0,
            """
            invoke-static/range {p0 .. p0}, $GALLERY_HELPER->setActivity(Landroid/app/Activity;)V
            """.trimIndent(),
        )
    }
}
