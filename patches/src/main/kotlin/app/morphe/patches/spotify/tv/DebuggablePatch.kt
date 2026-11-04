package app.morphe.patches.spotify.tv

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val debuggablePatch = resourcePatch(
    name = "Debuggable build",
    description = "Marks the app debuggable: enables run-as access to app files and WebView remote inspection.",
    default = true,
) {
    compatibleWith(spotifyTvCompatibility)

    execute {
        document("AndroidManifest.xml").use { doc ->
            val application = doc.getElementsByTagName("application").item(0) as Element
            application.setAttribute("android:debuggable", "true")
        }
    }
}

