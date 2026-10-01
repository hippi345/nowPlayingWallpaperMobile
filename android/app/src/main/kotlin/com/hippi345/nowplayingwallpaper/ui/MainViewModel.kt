package com.hippi345.nowplayingwallpaper.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hippi345.nowplayingwallpaper.NowPlayingWallpaperApplication
import com.hippi345.nowplayingwallpaper.spotify.SpotifyAuthManager
import com.hippi345.nowplayingwallpaper.spotify.SpotifyOAuthConfig
import com.hippi345.nowplayingwallpaper.spotify.SpotifySignInSession
import com.hippi345.nowplayingwallpaper.spotify.SpotifyTokenStore
import com.hippi345.nowplayingwallpaper.sync.SpotifyWallpaperSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val layout: com.hippi345.nowplayingwallpaper.domain.WallpaperLayout? = null,
    val authInProgress: Boolean = false,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val app = application as NowPlayingWallpaperApplication
    private val syncEngine = app.wallpaperSyncEngine
    private val signInSession = SpotifySignInSession(
        SpotifyAuthManager(SpotifyTokenStore(application)),
    )

    private var screenPhase: ScreenPhase = ScreenPhase.SignedOut

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPhase()
        viewModelScope.launch {
            syncEngine.state.collect { sync ->
                _uiState.value = MainUiState(
                    phase = screenPhase,
                    loading = sync.loading && screenPhase == ScreenPhase.SignedIn,
                    statusMessage = messageForPhase(screenPhase, sync.statusMessage),
                    layout = if (screenPhase == ScreenPhase.SignedIn) sync.layout else null,
                    authInProgress = _uiState.value.authInProgress,
                )
            }
        }
    }

    fun refreshPhase() {
        screenPhase = when {
            !SpotifyOAuthConfig.isClientIdConfigured() -> ScreenPhase.MissingClientId
            syncEngine.isSignedIn() -> ScreenPhase.SignedIn
            else -> ScreenPhase.SignedOut
        }
        when (screenPhase) {
            ScreenPhase.SignedIn -> {
                SpotifyWallpaperSyncService.start(getApplication())
                viewModelScope.launch { syncEngine.syncOnce() }
            }
            else -> SpotifyWallpaperSyncService.stop(getApplication())
        }
        _uiState.value = _uiState.value.copy(
            phase = screenPhase,
            statusMessage = messageForPhase(screenPhase, syncEngine.state.value.statusMessage),
            layout = if (screenPhase == ScreenPhase.SignedIn) syncEngine.state.value.layout else null,
        )
    }

    fun signIn(openAuthorizePage: (Uri) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authInProgress = true)
            when (val result = signInSession.signIn(openAuthorizePage)) {
                is SpotifySignInSession.SignInResult.Success -> {
                    SpotifyWallpaperSyncService.start(getApplication())
                    refreshPhase()
                }
                is SpotifySignInSession.SignInResult.Cancelled -> {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "Spotify sign-in was cancelled.",
                    )
                }
                is SpotifySignInSession.SignInResult.Failure -> {
                    _uiState.value = _uiState.value.copy(statusMessage = result.message)
                }
            }
            _uiState.value = _uiState.value.copy(authInProgress = false)
        }
    }

    fun signOut() {
        SpotifyWallpaperSyncService.stop(getApplication())
        syncEngine.signOut()
        screenPhase = if (SpotifyOAuthConfig.isClientIdConfigured()) {
            ScreenPhase.SignedOut
        } else {
            ScreenPhase.MissingClientId
        }
        _uiState.value = MainUiState(
            phase = screenPhase,
            loading = false,
            statusMessage = messageForPhase(screenPhase, null),
        )
    }

    private fun messageForPhase(phase: ScreenPhase, syncMessage: String?): String? = when (phase) {
        ScreenPhase.MissingClientId ->
            "Add SPOTIFY_CLIENT_ID to android/local.properties (paste the Client ID from your existing Spotify app used for nowPlayingDesktops / spot-ai-fy)."
        ScreenPhase.SignedOut ->
            "Sign in with Spotify to mirror your live now playing track to the wallpaper."
        ScreenPhase.SignedIn -> syncMessage
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
}
