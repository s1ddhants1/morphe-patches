package app.morphe.patches.spotify.tv

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private object HideUpgradeButtonFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/webkit/WebView;", "Ljava/lang/String;"),
    strings = listOf("Executing pending javascript intent"),
)

private const val HIDE_UPGRADE_JS =
    "javascript:(function(){" +
        "if(window.__morphe_hide_upgrade)return;" +
        "window.__morphe_hide_upgrade=true;" +
        "var c='[data-testid=\\\"upsell-menu-item\\\"],.upsell-menu-item{display:none!important}';" +
        "function i(){" +
            "if(!document.getElementById(\\\"morphe-hide-upgrade\\\")){" +
                "var s=document.createElement(\\\"style\\\");" +
                "s.id=\\\"morphe-hide-upgrade\\\";" +
                "s.textContent=c;" +
                "(document.head||document.documentElement).appendChild(s);" +
            "}" +
        "}" +
        "i();" +
        "try{" +
            "new MutationObserver(i).observe(document.documentElement,{childList:true,subtree:true});" +
        "}catch(e){}" +
    "})();"

@Suppress("unused")
val hideUpgradeButtonPatch = bytecodePatch(
    name = "Hide upgrade button",
    description = "Hides the Upgrade button in the navigation bar.",
    default = true,
) {
    compatibleWith(spotifyTvCompatibility)

    execute {
        HideUpgradeButtonFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$HIDE_UPGRADE_JS"
                invoke-virtual {p1, v0}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V
            """,
        )
    }
}
