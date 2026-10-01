package com.hippi345.nowplayingwallpaper.sync

import android.content.Context
import androidx.core.content.edit
import com.hippi345.nowplayingwallpaper.domain.WallpaperLayout
import com.hippi345.nowplayingwallpaper.spotify.SpotifyAuthManager
import com.hippi345.nowplayingwallpaper.spotify.SpotifyNowPlayingRepository
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

    fun isSignedIn(): Boolean = authManager.isSignedIn()

    fun signOut() {
        authManager.signOut()
        lastAppliedWallpaperIdentity = null
        _state.value = WallpaperSyncState(
            loading = false,
            statusMessage = "Sign in with Spotify to mirror your live now playing track to the wallpaper.",
        )
    }

    suspend fun syncOnce() {
        if (!authManager.isSignedIn()) {
            _state.value = WallpaperSyncState(
                loading = false,
                statusMessage = "Sign in with Spotify to mirror your live now playing track to the wallpaper.",
            )
            return
        }
        when (val result = repository.fetchWithStatus()) {
            is SpotifyWebApiClient.CurrentlyPlayingResult.Playing -> {
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
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.NothingPlaying -> {
                _state.value = _state.value.copy(
                    loading = false,
                    layout = null,
                    statusMessage = "Nothing is playing on Spotify right now.",
                )
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.Unauthorized -> {
                authManager.signOut()
                lastAppliedWallpaperIdentity = null
                SpotifyWallpaperSyncService.stop(appContext)
                _state.value = WallpaperSyncState(
                    loading = false,
                    statusMessage = "Session expired. Sign in with Spotify again.",
                )
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.Error -> {
                _state.value = _state.value.copy(
                    loading = false,
                    statusMessage = result.message,
                )
            }
        }
    }

    companion object {
        /** Poll interval while signed in (foreground service). */
        const val POLL_INTERVAL_MS: Long = 2_000L

        private const val PREFS_NAME = "wallpaper_sync"
        private const val KEY_PIPELINE_VERSION = "pipeline_version"
        /** Bump when wallpaper compose output changes so devices refresh stale center-crop frames. */
        private const val WALLPAPER_PIPELINE_VERSION = 2
    }
}
