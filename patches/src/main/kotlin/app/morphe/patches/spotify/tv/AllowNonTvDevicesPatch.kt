package app.morphe.patches.spotify.tv

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private object IsTvDeviceFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("android.hardware.touchscreen", "uimode"),
)

@Suppress("unused")
val allowNonTvDevicesPatch = bytecodePatch(
    name = "Allow non-TV devices",
    description = "Enables Spotify Android TV to run on phones, tablets, and emulators by bypassing the TV UI mode check.",
    default = true,
) {
    compatibleWith(spotifyTvCompatibility)

    execute {
        IsTvDeviceFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
