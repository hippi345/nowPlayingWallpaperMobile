package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.BuildConfig

/**
 * Spotify Client ID from [android/local.properties] at build time.
 * Never commit real secrets; the default is an obvious placeholder.
 */
object SpotifyConfig {
    val clientId: String = BuildConfig.SPOTIFY_CLIENT_ID

    fun isPlaceholder(): Boolean =
        clientId.isBlank() || clientId == "your_spotify_client_id_here"
}
