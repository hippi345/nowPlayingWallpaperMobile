package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class SpotifyWebApiClient(
    private val authManager: SpotifyAuthManager,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    sealed class CurrentlyPlayingResult {
        data class Playing(val track: NowPlayingTrack) : CurrentlyPlayingResult()
        data object NothingPlaying : CurrentlyPlayingResult()
        data object Unauthorized : CurrentlyPlayingResult()
        data class Error(val message: String) : CurrentlyPlayingResult()
    }

    suspend fun fetchCurrentlyPlaying(): CurrentlyPlayingResult = withContext(Dispatchers.IO) {
        val token = authManager.ensureValidAccessToken()
            ?: return@withContext CurrentlyPlayingResult.Unauthorized

        val request = Request.Builder()
            .url(SpotifyOAuthConfig.CURRENTLY_PLAYING_URL)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            when (response.code) {
                204 -> CurrentlyPlayingResult.NothingPlaying
                401 -> CurrentlyPlayingResult.Unauthorized
                200 -> {
                    val body = response.body?.string() ?: ""
                    val track = SpotifyCurrentlyPlayingParser.parse(body)
                    if (track == null) {
                        CurrentlyPlayingResult.NothingPlaying
                    } else {
                        CurrentlyPlayingResult.Playing(track)
                    }
                }
                else -> CurrentlyPlayingResult.Error("Spotify API error (${response.code})")
            }
        }
    }
}
