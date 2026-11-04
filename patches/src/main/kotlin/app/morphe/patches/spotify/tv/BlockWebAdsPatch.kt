package app.morphe.patches.spotify.tv

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private object WebViewOnPageFinishedFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/webkit/WebView;", "Ljava/lang/String;"),
    strings = listOf("Executing pending javascript intent"),
)

// Injects adblock styles, product state override (premium), and queue-level ad stream removal into the WebView
private const val ADBLOCK_JS =
    "javascript:(function(){" +
        "if(window.__morphe_adblock_initialized)return;" +
        "window.__morphe_adblock_initialized=true;" +
        "var c='[data-testid*=\\\"sponsor\\\"],[aria-label*=\\\"Sponsored\\\"],.sponsor-container,.ad-container," +
        "div[data-testid*=\\\"hpto\\\"],[data-testid*=\\\"billboard\\\"],#view-billboard-ad,#leaderboard-ad-element," +
        ".main-leaderboardComponent-container,[data-testid=\\\"home-ad-card\\\"],[data-testid=\\\"home-ads-container\\\"]," +
        "[data-testid*=\\\"home-ad-\\\"],[data-testid=\\\"signup-bar\\\"],.main-topBar-UpgradeButton,a[href*=\\\"/premium\\\"]," +
        "[data-testid=\\\"upgrade_url\\\"],.home-promotion,.tilePromotionsItem,.entity-list-row__promoted-text," +
        ".entity-list-row__promoted-text--green,.entity-metadata__promoted-text,.layout-header__promoted-text," +
        ".tile-captions__promoted-text,.entity-list-row__body--with-promoted-text,.library-scroller-banner," +
        ".search-landing-scroller-banner,.search-result-banner,.information-banner,.layout-header__information-banner," +
        ".episode-description-body__banner,.force-upgrade-view,.npv-currently-playing__wrapper--ad," +
        "[data-testid=\\\"progress__ad-remaining-time\\\"]{display:none!important}';" +
        "function injectStyle(){" +
            "if(!document.getElementById(\\\"morphe-adblock\\\")){" +
                "var s=document.createElement(\\\"style\\\");" +
                "s.id=\\\"morphe-adblock\\\";" +
                "s.textContent=c;" +
                "(document.head||document.documentElement).appendChild(s);" +
            "}" +
        "}" +
        "injectStyle();" +
        "function isAd(item){" +
            "if(!item)return false;" +
            "var uri=item.uri||(item.metadata&&(item.metadata.entity_uri||item.metadata.context_uri));" +
            "if(uri&&(uri.startsWith(\\\"spotify:ad:\\\")||uri.indexOf(\\\":ad:\\\")!==-1))return true;" +
            "if(item.type===\\\"ad\\\"||item.content_type===\\\"ad\\\")return true;" +
            "var m=item.metadata;" +
            "if(m&&(m.is_advertisement===\\\"true\\\"||m.ad_id||m[\\\"ad.token\\\"]||m.ad_system))return true;" +
            "return false;" +
        "}" +
        "var lastSkip=0;" +
        "function muteAndPlayNext(nextTrack,contextUri){" +
            "try{" +
                "if(typeof SpBridge!==\\\"undefined\\\"&&SpBridge.execute){" +
                    "SpBridge.execute(JSON.stringify({ns:\\\"android\\\",msgId:Math.floor(Math.random()*100000),func:2,args:[]}));" +
                "}" +
            "}catch(e){}" +
            "var now=Date.now();" +
            "if(now-lastSkip<1500)return;" +
            "lastSkip=now;" +
            "try{" +
                "if(typeof SpBridge!==\\\"undefined\\\"&&SpBridge.execute){" +
                    "if(nextTrack&&nextTrack.uri){" +
                        "var param={" +
                            "contextUri:(nextTrack.metadata&&nextTrack.metadata.context_uri)||(nextTrack.metadata&&nextTrack.metadata.entity_uri)||contextUri||\\\"\\\"," +
                            "trackIndex:0," +
                            "startView:\\\"\\\"," +
                            "trackUri:nextTrack.uri," +
                            "parentUri:\\\"\\\"," +
                            "shuffleMode:false," +
                            "offsetMs:0" +
                        "};" +
                        "SpBridge.execute(JSON.stringify({" +
                            "ns:\\\"android\\\"," +
                            "msgId:Math.floor(Math.random()*100000)," +
                            "func:12," +
                            "args:[param]" +
                        "}));" +
                    "}" +
                "}" +
            "}catch(e){}" +
        "}" +
        "function patchProductStateModule(){" +
            "try{" +
                "var chunk=window.webpackChunkSpotifyTVApp;" +
                "if(!chunk)return false;" +
                "var req;" +
                "chunk.push([[\\\"morphe_pstate_init\\\"],{},function(r){req=r;}]);" +
                "if(!req)return false;" +
                "var PState=req(\\\"25312\\\");" +
                "if(PState&&PState.$&&PState.$.prototype){" +
                    "var origGetCurrentState=PState.$.prototype.getCurrentState;" +
                    "PState.$.prototype.getCurrentState=async function(){" +
                        "var state=await origGetCurrentState.apply(this,arguments);" +
                        "if(!state)state={};" +
                        "var patched=Object.assign({},state,{" +
                            "ads:\\\"0\\\"," +
                            "catalogue:\\\"premium\\\"," +
                            "product:\\\"premium\\\"," +
                            "type:\\\"premium\\\"," +
                            "\\\"player-license\\\":\\\"premium\\\"," +
                            "\\\"on-demand\\\":\\\"1\\\"" +
                        "});" +
                        "this._currentState=patched;" +
                        "return patched;" +
                    "};" +
                    "return true;" +
                "}" +
            "}catch(e){}" +
            "return false;" +
        "}" +
        "patchProductStateModule();" +
        "function hookRuntime(){" +
            "try{" +
                "var el=document.querySelector('[data-testid=\\\"root\\\"]')||document.getElementById('root');" +
                "if(!el)return false;" +
                "var k=Object.keys(el).find(function(x){return x.startsWith('__reactFiber');});" +
                "if(!k)return false;" +
                "var f=el[k];" +
                "while(f&&f.return)f=f.return;" +
                "var playerObj=null;" +
                "var connectHolder=null;" +
                "var reduxStore=null;" +
                "function walk(n,d){" +
                    "if(!n||d>40)return;" +
                    "var p=n.memoizedProps;" +
                    "if(p&&p.value){" +
                        "if(p.value.player&&!playerObj)playerObj=p.value.player;" +
                        "if(p.value.store&&!reduxStore)reduxStore=p.value.store;" +
                        "for(var key in p.value){" +
                            "if(p.value[key]&&typeof p.value[key].getProductState===\\\"function\\\"){" +
                                "connectHolder=p.value[key];" +
                            "}" +
                        "}" +
                    "}" +
                    "if(n.stateNode&&typeof n.stateNode.store==='object'&&!reduxStore)reduxStore=n.stateNode.store;" +
                    "var ch=n.child;while(ch){walk(ch,d+1);ch=ch.sibling;}" +
                "}" +
                "walk(f,0);" +
                "if(connectHolder&&connectHolder._client&&connectHolder._client._productStateObserver){" +
                    "var obs=connectHolder._client._productStateObserver;" +
                    "obs._currentState=Object.assign({},obs._currentState,{" +
                        "ads:\\\"0\\\"," +
                        "catalogue:\\\"premium\\\"," +
                        "product:\\\"premium\\\"," +
                        "type:\\\"premium\\\"," +
                        "\\\"player-license\\\":\\\"premium\\\"," +
                        "\\\"on-demand\\\":\\\"1\\\"" +
                    "});" +
                    "try{obs.emit(\\\"product_state_changed\\\",{productState:obs._currentState});}catch(e){}" +
                "}" +
                "if(playerObj&&playerObj._state){" +
                    "var s=playerObj._state;" +
                    "if(s.queueManager){" +
                        "var qmProto=Object.getPrototypeOf(s.queueManager);" +
                        "if(!qmProto.__morphe_patched){" +
                            "qmProto.__morphe_patched=true;" +
                            "var origParse=qmProto._parsePlayerState;" +
                            "qmProto._parsePlayerState=function(t,e,n){" +
                                "if(t&&Array.isArray(t.next_tracks)){" +
                                    "t.next_tracks=t.next_tracks.filter(function(x){return !isAd(x);});" +
                                "}" +
                                "if(t&&isAd(t.track)){" +
                                    "var nextSong=t.next_tracks&&t.next_tracks.find(function(x){return !isAd(x);});" +
                                    "muteAndPlayNext(nextSong,t.context_uri);" +
                                "}" +
                                "return origParse.call(this,t,e,n);" +
                            "};" +
                        "}" +
                    "}" +
                    "if(s.playerStateDecorator){" +
                        "var decProto=Object.getPrototypeOf(s.playerStateDecorator);" +
                        "if(!decProto.__morphe_patched){" +
                            "decProto.__morphe_patched=true;" +
                            "var origSetState=decProto.setState;" +
                            "decProto.setState=function(e){" +
                                "if(e&&e.track_window&&Array.isArray(e.track_window.next_tracks)){" +
                                    "e.track_window.next_tracks=e.track_window.next_tracks.filter(function(x){return !isAd(x);});" +
                                "}" +
                                "return origSetState.call(this,e);" +
                            "};" +
                        "}" +
                    "}" +
                "}" +
                "if(reduxStore&&typeof reduxStore.subscribe==='function'&&!window.__morphe_store_hooked){" +
                    "window.__morphe_store_hooked=true;" +
                    "reduxStore.subscribe(function(){" +
                        "try{" +
                            "var state=reduxStore.getState();" +
                            "var curr=state&&state.player&&state.player.trackWindow&&state.player.trackWindow.current_track;" +
                            "if(isAd(curr)){" +
                                "var list=state.player.trackWindow.next_tracks;" +
                                "var next=Array.isArray(list)?list.find(function(x){return !isAd(x);}):null;" +
                                "var ctx=state.player.context&&state.player.context.uri;" +
                                "muteAndPlayNext(next,ctx);" +
                            "}" +
                        "}catch(e){}" +
                    "});" +
                "}" +
                "return !!(playerObj&&reduxStore);" +
            "}catch(e){}" +
            "return false;" +
        "}" +
        "if(!hookRuntime()){" +
            "var retry=setInterval(function(){if(hookRuntime())clearInterval(retry);},1000);" +
        "}" +
        "try{" +
            "new MutationObserver(function(){injectStyle();}).observe(document.documentElement,{childList:true,subtree:true});" +
        "}catch(e){}" +
    "})();"

@Suppress("unused")
val blockWebAdsPatch = bytecodePatch(
    name = "Block web banners & promo UI (Android TV)",
    description = "Injects adblock styles, product state override (premium), and queue-level ad stream removal into the WebView.",
    default = true,
) {
    compatibleWith(spotifyTvCompatibility)

    execute {
        WebViewOnPageFinishedFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$ADBLOCK_JS"
                invoke-virtual {p1, v0}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V
            """,
        )
    }
}
