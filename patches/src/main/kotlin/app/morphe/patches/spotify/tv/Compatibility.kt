package app.morphe.patches.spotify.tv

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

val spotifyTvCompatibility = Compatibility(
    name = "Spotify (Android TV)",
    packageName = "com.spotify.tv.android",
    apkFileType = ApkFileType.APKM,
    appIconColor = 0x1DB954,
    description = "Spotify Android TV application",
    targets = listOf(
        AppTarget(
            version = "1.134.2",
            isExperimental = false,
            description = "Android TV Leanback client; supports video, audio, and web ad blocking.",
        ),
    ),
)
