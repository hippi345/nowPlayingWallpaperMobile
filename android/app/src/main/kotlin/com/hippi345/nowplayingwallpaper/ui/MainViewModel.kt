package com.hippi345.nowplayingwallpaper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hippi345.nowplayingwallpaper.domain.WallpaperLayout
import com.hippi345.nowplayingwallpaper.spotify.NowPlayingRepository
import com.hippi345.nowplayingwallpaper.spotify.StubNowPlayingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val layout: WallpaperLayout? = null,
    val loading: Boolean = true,
)

class MainViewModel(
    private val repository: NowPlayingRepository = StubNowPlayingRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val track = repository.currentTrack()
            _uiState.value = MainUiState(
                layout = track?.let { WallpaperLayout.fromTrack(it) },
                loading = false,
            )
        }
    }
}
