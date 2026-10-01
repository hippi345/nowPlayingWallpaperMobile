package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.BuildConfig

object SpotifyOAuthConfig {
    const val REDIRECT_URI: String = "com.hippi345.nowplayingwallpaper://callback"

    const val AUTHORIZE_URL: String = "https://accounts.spotify.com/authorize"
    const val TOKEN_URL: String = "https://accounts.spotify.com/api/token"
    const val CURRENTLY_PLAYING_URL: String =
        "https://api.spotify.com/v1/me/player/currently-playing"

    /** Space-separated scopes for the Web API currently-playing endpoint. */
    const val SCOPES: String = "user-read-currently-playing user-read-playback-state"

    val clientId: String = BuildConfig.SPOTIFY_CLIENT_ID

    fun isClientIdConfigured(): Boolean =
        clientId.isNotBlank() && clientId != "your_spotify_client_id_here"
}
