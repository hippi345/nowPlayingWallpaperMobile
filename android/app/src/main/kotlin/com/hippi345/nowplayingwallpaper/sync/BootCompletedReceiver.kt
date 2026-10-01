package com.hippi345.nowplayingwallpaper.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hippi345.nowplayingwallpaper.NowPlayingWallpaperApplication
import com.hippi345.nowplayingwallpaper.spotify.SpotifyOAuthConfig

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!SpotifyOAuthConfig.isClientIdConfigured()) return
        val app = context.applicationContext as? NowPlayingWallpaperApplication ?: return
        if (app.wallpaperSyncEngine.isSignedIn()) {
            SpotifyWallpaperSyncService.start(context)
        }
    }
}
