package app.morphe.patches.spotify.tv

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private object VideoAdEnabledFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("does not allow video ad"),
)

@Suppress("unused")
val disableVideoAdsPatch = bytecodePatch(
    name = "Disable video ads (Android TV)",
    description = "Disables video advertisements in the Android TV embedded bridge config.",
    default = true,
) {
    compatibleWith(spotifyTvCompatibility)

    execute {
        VideoAdEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
