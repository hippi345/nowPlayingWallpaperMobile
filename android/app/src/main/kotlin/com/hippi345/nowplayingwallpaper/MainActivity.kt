package com.hippi345.nowplayingwallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hippi345.nowplayingwallpaper.ui.MainViewModel
import com.hippi345.nowplayingwallpaper.ui.WallpaperPreviewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface {
                    val vm: MainViewModel = viewModel()
                    val state by vm.uiState.collectAsStateWithLifecycle()
                    state.layout?.let { layout ->
                        WallpaperPreviewScreen(layout = layout)
                    }
                }
            }
        }
    }
}
