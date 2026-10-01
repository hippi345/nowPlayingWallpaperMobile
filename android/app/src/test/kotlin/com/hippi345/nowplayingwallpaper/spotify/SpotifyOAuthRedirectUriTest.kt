package com.hippi345.nowplayingwallpaper.spotify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Documents the redirect string sent to Spotify (authorize + token exchange).
 */
class SpotifyOAuthRedirectUriTest {
    @Test
    fun redirectUri_matchesExistingSpotifyAppLoopbackEntry() {
        assertEquals("http://127.0.0.1:8897/callback", SpotifyOAuthConfig.REDIRECT_URI)
        assertEquals(8897, SpotifyOAuthConfig.LOOPBACK_PORT)
        assertEquals("/callback", SpotifyOAuthConfig.LOOPBACK_PATH)
    }

    @Test
    fun redirectUri_urlEncodedForAuthorizeQuery() {
        val encoded = URLEncoder.encode(SpotifyOAuthConfig.REDIRECT_URI, StandardCharsets.UTF_8)
        assertEquals("http%3A%2F%2F127.0.0.1%3A8897%2Fcallback", encoded)
    }
}
