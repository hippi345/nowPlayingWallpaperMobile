package com.hippi345.nowplayingwallpaper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hippi345.nowplayingwallpaper.spotify.SpotifyOAuthConfig
import com.hippi345.nowplayingwallpaper.ui.MainScreen
import com.hippi345.nowplayingwallpaper.ui.MainViewModel
import com.hippi345.nowplayingwallpaper.ui.openSpotifyAuthorizeTab

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleOAuthIntent(intent)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface {
                    val state by mainViewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    MainScreen(
                        state = state,
                        onSignIn = {
                            val uri = mainViewModel.buildAuthorizeUri()
                            if (uri != null) {
                                openSpotifyAuthorizeTab(context, uri)
                            }
                        },
                        onSignOut = mainViewModel::signOut,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.startPolling()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == REDIRECT_SCHEME && data.host == REDIRECT_HOST) {
            mainViewModel.onAuthRedirect(
                code = data.getQueryParameter("code"),
                error = data.getQueryParameter("error"),
            )
        }
    }

    companion object {
        private val REDIRECT_SCHEME = SpotifyOAuthConfig.REDIRECT_URI.substringBefore(":")
        private val REDIRECT_HOST =
            SpotifyOAuthConfig.REDIRECT_URI.removePrefix("$REDIRECT_SCHEME://").substringBefore("/")
    }
}
