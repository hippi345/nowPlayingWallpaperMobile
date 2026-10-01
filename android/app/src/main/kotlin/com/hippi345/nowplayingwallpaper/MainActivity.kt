package com.hippi345.nowplayingwallpaper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hippi345.nowplayingwallpaper.ui.MainScreen
import com.hippi345.nowplayingwallpaper.ui.MainViewModel
import com.hippi345.nowplayingwallpaper.ui.openSpotifyAuthorizeTab

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Foreground sync still runs; notification may be hidden if denied on API 33+. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface {
                    val state by mainViewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    MainScreen(
                        state = state,
                        onSignIn = {
                            maybeRequestNotificationPermission()
                            mainViewModel.signIn { uri ->
                                openSpotifyAuthorizeTab(context, uri)
                            }
                        },
                        onSignOut = mainViewModel::signOut,
                    )
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
