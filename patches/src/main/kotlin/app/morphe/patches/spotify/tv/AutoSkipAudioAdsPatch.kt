package app.morphe.patches.spotify.tv

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private object AdPlayingCheckFingerprint : Fingerprint(
    returnType = "L",
    strings = listOf("playback_id", "is_ad_playing"),
    filters = listOf(
        string("is_ad_playing"),
        methodCall(
            definingClass = "Lorg/json/JSONObject;",
            name = "getBoolean",
            returnType = "Z",
        ),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
    ),
)

@Suppress("unused")
val autoSkipAudioAdsPatch = bytecodePatch(
    name = "Auto-skip audio ads (Android TV)",
    description = "Automatically skips commercial audio intervals when detected by TVBridge.",
    default = true,
) {
    compatibleWith(spotifyTvCompatibility)

    execute {
        val moveResultIndex = AdPlayingCheckFingerprint.instructionMatches.last().index
        val adPlayingRegister = AdPlayingCheckFingerprint.method
            .getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

        AdPlayingCheckFingerprint.method.addInstructionsWithLabels(
            moveResultIndex + 1,
            """
                if-eqz v$adPlayingRegister, :skip_ad_block
                iget-object v4, p0, Lcom/spotify/tv/android/audio/PlayerStateManager;->k:Lcom/spotify/tv/android/bindings/tvbridge/TVBridgeApi;
                if-eqz v4, :skip_ad_block
                invoke-interface {v4}, Lcom/spotify/tv/android/bindings/tvbridge/TVBridgeApi;->pause()V
                const/4 v$adPlayingRegister, 0x0
                :skip_ad_block
                nop
            """,
        )
    }
}
