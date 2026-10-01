package com.hippi345.nowplayingwallpaper.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import com.hippi345.nowplayingwallpaper.domain.WallpaperLayout
import com.hippi345.nowplayingwallpaper.spotify.SpotifyAuthManager
import com.hippi345.nowplayingwallpaper.spotify.SpotifyNowPlayingRepository
import com.hippi345.nowplayingwallpaper.spotify.SpotifyOAuthConfig
import com.hippi345.nowplayingwallpaper.spotify.SpotifyTokenStore
import com.hippi345.nowplayingwallpaper.spotify.SpotifyWebApiClient
import com.hippi345.nowplayingwallpaper.wallpaper.AndroidWallpaperInstaller
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenPhase {
    MissingClientId,
    SignedOut,
    SignedIn,
}

data class MainUiState(
    val phase: ScreenPhase = ScreenPhase.SignedOut,
    val loading: Boolean = true,
    val statusMessage: String? = null,
    val layout: WallpaperLayout? = null,
    val lastWallpaperTrack: NowPlayingTrack? = null,
    val authInProgress: Boolean = false,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val tokenStore = SpotifyTokenStore(application)
    private val authManager = SpotifyAuthManager(tokenStore)
    private val apiClient = SpotifyWebApiClient(authManager)
    private val repository = SpotifyNowPlayingRepository(apiClient)
    private val wallpaperInstaller = AndroidWallpaperInstaller()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        refreshPhase()
        if (_uiState.value.phase == ScreenPhase.SignedIn) {
            startPolling()
        }
    }

    fun refreshPhase() {
        val phase = when {
            !SpotifyOAuthConfig.isClientIdConfigured() -> ScreenPhase.MissingClientId
            authManager.isSignedIn() -> ScreenPhase.SignedIn
            else -> ScreenPhase.SignedOut
        }
        _uiState.value = _uiState.value.copy(
            phase = phase,
            loading = phase == ScreenPhase.SignedIn,
            statusMessage = statusForPhase(phase),
            layout = if (phase != ScreenPhase.SignedIn) null else _uiState.value.layout,
        )
        if (phase != ScreenPhase.SignedIn) {
            pollingJob?.cancel()
        }
    }

    fun buildAuthorizeUri(): Uri? {
        if (!SpotifyOAuthConfig.isClientIdConfigured()) return null
        _uiState.value = _uiState.value.copy(authInProgress = true)
        return authManager.buildAuthorizeUri()
    }

    fun onAuthRedirect(code: String?, error: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authInProgress = false)
            if (error != null) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Spotify sign-in was cancelled or failed.",
                )
                return@launch
            }
            if (code.isNullOrBlank()) return@launch
            val result = authManager.completeAuthorization(code)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Could not complete Spotify sign-in. Try again.",
                )
                return@launch
            }
            refreshPhase()
            startPolling()
        }
    }

    fun signOut() {
        authManager.signOut()
        pollingJob?.cancel()
        _uiState.value = MainUiState(
            phase = if (SpotifyOAuthConfig.isClientIdConfigured()) {
                ScreenPhase.SignedOut
            } else {
                ScreenPhase.MissingClientId
            },
            loading = false,
            statusMessage = statusForPhase(ScreenPhase.SignedOut),
        )
    }

    fun startPolling() {
        if (_uiState.value.phase != ScreenPhase.SignedIn) return
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                pollNowPlaying()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun pollNowPlaying() {
        when (val result = repository.fetchWithStatus()) {
            is SpotifyWebApiClient.CurrentlyPlayingResult.Playing -> {
                val track = result.track
                val previous = _uiState.value.lastWallpaperTrack
                val layout = WallpaperLayout.fromTrack(track)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    layout = layout,
                    statusMessage = null,
                    lastWallpaperTrack = track,
                )
                if (track != previous) {
                    wallpaperInstaller.apply(getApplication(), track)
                }
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.NothingPlaying -> {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    layout = null,
                    statusMessage = "Nothing is playing on Spotify right now.",
                )
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.Unauthorized -> {
                authManager.signOut()
                refreshPhase()
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    statusMessage = "Session expired. Sign in with Spotify again.",
                )
            }
            is SpotifyWebApiClient.CurrentlyPlayingResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    statusMessage = result.message,
                )
            }
        }
    }

    private fun statusForPhase(phase: ScreenPhase): String? = when (phase) {
        ScreenPhase.MissingClientId ->
            "Add SPOTIFY_CLIENT_ID to android/local.properties (paste the Client ID from your existing Spotify app used for nowPlayingDesktops / spot-ai-fy)."
        ScreenPhase.SignedOut ->
            "Sign in with Spotify to mirror your live now playing track to the wallpaper."
        ScreenPhase.SignedIn -> null
    }

    class Factory(
        private val application: Application,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 10_000L
    }
}
