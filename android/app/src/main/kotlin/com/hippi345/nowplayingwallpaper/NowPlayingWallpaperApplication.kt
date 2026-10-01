package com.hippi345.nowplayingwallpaper

import android.app.Application
import com.hippi345.nowplayingwallpaper.spotify.SpotifyOAuthConfig
import com.hippi345.nowplayingwallpaper.spotify.SpotifyTokenStore
import com.hippi345.nowplayingwallpaper.spotify.SpotifyAuthManager
import com.hippi345.nowplayingwallpaper.sync.SpotifyWallpaperSyncService
import com.hippi345.nowplayingwallpaper.sync.WallpaperSyncEngine

class NowPlayingWallpaperApplication : Application() {
    lateinit var wallpaperSyncEngine: WallpaperSyncEngine
        private set

    override fun onCreate() {
        super.onCreate()
        wallpaperSyncEngine = WallpaperSyncEngine(this)
        if (SpotifyOAuthConfig.isClientIdConfigured()) {
            val signedIn = SpotifyAuthManager(SpotifyTokenStore(this)).isSignedIn()
            if (signedIn) {
                SpotifyWallpaperSyncService.start(this)
            }
        }
    }
}
