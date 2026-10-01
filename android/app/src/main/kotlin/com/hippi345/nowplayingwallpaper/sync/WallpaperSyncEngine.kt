package com.hippi345.nowplayingwallpaper.sync

import android.content.Context
import androidx.core.content.edit
import com.hippi345.nowplayingwallpaper.domain.WallpaperLayout
import com.hippi345.nowplayingwallpaper.spotify.SpotifyAuthManager
import com.hippi345.nowplayingwallpaper.spotify.SpotifyNowPlayingRepository
import com.hippi345.nowplayingwallpaper.spotify.SpotifyPollBackoff
import com.hippi345.nowplayingwallpaper.spotify.SpotifyTokenStore
import com.hippi345.nowplayingwallpaper.spotify.SpotifyWebApiClient
import com.hippi345.nowplayingwallpaper.wallpaper.AndroidWallpaperInstaller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WallpaperSyncEngine(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val tokenStore = SpotifyTokenStore(appContext)
    private val authManager = SpotifyAuthManager(tokenStore)
    private val repository = SpotifyNowPlayingRepository(SpotifyWebApiClient(authManager))
    private val wallpaperInstaller = AndroidWallpaperInstaller()
    private val wallpaperPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(WallpaperSyncState())
    val state: StateFlow<WallpaperSyncState> = _state.asStateFlow()

    private var lastAppliedWallpaperIdentity: String? = null
    private var consecutive429WithoutRetryAfter: Int = 0

    fun isSignedIn(): Boolean = authManager.isSignedIn()

    fun signOut() {
        authManager.signOut()
        lastAppliedWallpaperIdentity = null
        _state.value = WallpaperSyncState(
            loading = false,
            statusMessage = "Sign in with Spotify to mirror your live now playing track to the wallpaper.",
        )
    }

    /**
     * @return milliseconds until the next poll (honors Spotify rate limits after HTTP 429).
     */
    suspend fun syncOnce(): Long {
        if (!authManager.isSignedIn()) {
            _state.value = WallpaperSyncState(
                loading = false,
                statusMessage = "Sign in with Spotify to mirror your live now playing track to the wallpaper.",
            )
            return SpotifyPollBackoff.DEFAULT_POLL_INTERVAL_MS
        }
        when (val result = repository.fetchWithStatus()) {
            is SpotifyWebApiClient.CurrentlyPlayingResult.Playing -> {
                consecutive429WithoutRetryAfter = 0
                val track = result.track
                val layout = WallpaperLayout.fromTrack(track)
                _state.value = _state.value.copy(
                    loading = false,
                    layout = layout,
                    statusMessage = null,
                    lastWallpaperTrack = track,
                )
                val identity = track.wallpaperIdentity()
                val pipelineStale =
                    wallpaperPrefs.getInt(KEY_PIPELINE_VERSION, 0) < WALLPAPER_PIPELINE_VERSION
                if (identity != lastAppliedWallpaperIdentity || pipelineStale) {
                    val applied = wallpaperInstaller.apply(appContext, track)
                    if (applied) {
                        lastAppliedWallpaperIdentity = identity
                        wallpaperPrefs.edit {
                            putInt(KEY_PIPELINE_VERSION, WALLPAPER_PIPELINE_VERSION)
                        }
                    }
                }
                return SpotifyPollBackoff.DEFAULT_POLL_INTERVAL_MS
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.NothingPlaying -> {
                consecutive429WithoutRetryAfter = 0
                _state.value = _state.value.copy(
                    loading = false,
                    layout = null,
                    statusMessage = "Nothing is playing on Spotify right now.",
                )
                return SpotifyPollBackoff.DEFAULT_POLL_INTERVAL_MS
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.Unauthorized -> {
                authManager.signOut()
                lastAppliedWallpaperIdentity = null
                SpotifyWallpaperSyncService.stop(appContext)
                _state.value = WallpaperSyncState(
                    loading = false,
                    statusMessage = "Session expired. Sign in with Spotify again.",
                )
                return SpotifyPollBackoff.DEFAULT_POLL_INTERVAL_MS
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.RateLimited -> {
                val waitMs = SpotifyPollBackoff.nextDelayMs(
                    retryAfterHeader = result.retryAfterHeader,
                    consecutive429WithoutRetryAfter = consecutive429WithoutRetryAfter,
                    nowEpochMs = System.currentTimeMillis(),
                )
                if (result.retryAfterHeader.isNullOrBlank()) {
                    consecutive429WithoutRetryAfter++
                } else {
                    consecutive429WithoutRetryAfter = 0
                }
                _state.value = _state.value.copy(
                    loading = false,
                    statusMessage = "Spotify rate limit (HTTP 429). Keeping your current wallpaper; will check again after a short wait.",
                )
                return waitMs
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.Error -> {
                _state.value = _state.value.copy(
                    loading = false,
                    statusMessage = result.message,
                )
                return SpotifyPollBackoff.DEFAULT_POLL_INTERVAL_MS
            }
        }
    }

    companion object {
        /** Poll interval while signed in (foreground service). */
        const val POLL_INTERVAL_MS: Long = SpotifyPollBackoff.DEFAULT_POLL_INTERVAL_MS

        private const val PREFS_NAME = "wallpaper_sync"
        private const val KEY_PIPELINE_VERSION = "pipeline_version"
        /** Bump when wallpaper compose output changes so devices refresh stale center-crop frames. */
        private const val WALLPAPER_PIPELINE_VERSION = 2
    }
}
