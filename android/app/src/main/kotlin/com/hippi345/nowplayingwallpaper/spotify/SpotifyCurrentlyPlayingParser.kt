package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import org.json.JSONObject

/**
 * Parses Spotify Web API `GET /v1/me/player/currently-playing` JSON (200 response body).
 */
object SpotifyCurrentlyPlayingParser {
    fun parse(body: String): NowPlayingTrack? {
        val root = JSONObject(body)
        if (!root.optBoolean("is_playing", false)) {
            return null
        }
        val item = root.optJSONObject("item") ?: return null
        val title = item.optString("name").takeIf { it.isNotBlank() } ?: return null
        val artists = item.optJSONArray("artists")
        val artist = artists?.optJSONObject(0)?.optString("name")?.takeIf { it.isNotBlank() }
            ?: return null
        val album = item.optJSONObject("album")
        val albumName = album?.optString("name") ?: ""
        val artUrl = album?.optJSONArray("images")
            ?.optJSONObject(0)
            ?.optString("url")
        return NowPlayingTrack(
            title = title,
            artist = artist,
            albumName = albumName,
            albumArtUrl = artUrl,
        )
    }
}
