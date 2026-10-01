package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.BuildConfig

object SpotifyOAuthConfig {
    /**
     * Must match an entry on Joel's existing Spotify app (same Client ID as nowPlayingDesktops).
     * Loopback on the emulator/device — does not collide with desktop apps on the host.
     */
    const val REDIRECT_URI: String = "http://127.0.0.1:8897/callback"

    const val LOOPBACK_HOST: String = "127.0.0.1"
    const val LOOPBACK_PORT: Int = 8897
    const val LOOPBACK_PATH: String = "/callback"

    const val AUTHORIZE_URL: String = "https://accounts.spotify.com/authorize"
    const val TOKEN_URL: String = "https://accounts.spotify.com/api/token"
    const val CURRENTLY_PLAYING_URL: String =
        "https://api.spotify.com/v1/me/player/currently-playing"

    /** Space-separated scopes for the Web API currently-playing endpoint. */
    const val SCOPES: String = "user-read-currently-playing user-read-playback-state"

    val clientId: String = BuildConfig.SPOTIFY_CLIENT_ID

    val redirectUri: String = BuildConfig.SPOTIFY_REDIRECT_URI

    fun isClientIdConfigured(): Boolean =
        clientId.isNotBlank() && clientId != "your_spotify_client_id_here"

    fun isRedirectConfigured(): Boolean = redirectUri == REDIRECT_URI
}
