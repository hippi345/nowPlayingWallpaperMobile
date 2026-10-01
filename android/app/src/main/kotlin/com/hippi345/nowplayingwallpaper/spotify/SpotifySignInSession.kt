package com.hippi345.nowplayingwallpaper.spotify

import android.net.Uri

class SpotifySignInSession(
    private val authManager: SpotifyAuthManager,
    private val redirectServer: SpotifyLoopbackRedirectServer = SpotifyLoopbackRedirectServer(),
) {
    sealed class SignInResult {
        data object Success : SignInResult()
        data class Failure(val message: String) : SignInResult()
        data object Cancelled : SignInResult()
    }

    suspend fun signIn(openAuthorizePage: (Uri) -> Unit): SignInResult {
        if (!SpotifyOAuthConfig.isClientIdConfigured()) {
            return SignInResult.Failure(
                "Add SPOTIFY_CLIENT_ID to android/local.properties (existing Spotify app Client ID).",
            )
        }
        if (!SpotifyOAuthConfig.isRedirectConfigured()) {
            return SignInResult.Failure(
                "This build's redirect URI does not match ${SpotifyOAuthConfig.REDIRECT_URI}. Reinstall the latest APK.",
            )
        }
        return try {
            redirectServer.start()
            val authorizeUri = authManager.buildAuthorizeUri()
            openAuthorizePage(authorizeUri)
            val redirect = redirectServer.awaitRedirect()
            when {
                redirect.error == "redirect_timeout" -> SignInResult.Failure(
                    "Timed out waiting for Spotify at ${SpotifyOAuthConfig.REDIRECT_URI}.",
                )
                redirect.error != null -> SignInResult.Cancelled
                redirect.code.isNullOrBlank() ->
                    SignInResult.Failure("Spotify did not return an authorization code.")
                else -> {
                    val exchange = authManager.completeAuthorization(redirect.code!!)
                    if (exchange.isSuccess) {
                        SignInResult.Success
                    } else {
                        SignInResult.Failure(
                            "Token exchange failed. Check that redirect URI ${SpotifyOAuthConfig.REDIRECT_URI} is on your Spotify app.",
                        )
                    }
                }
            }
        } catch (e: Exception) {
            SignInResult.Failure(
                e.message ?: "Could not listen on ${SpotifyOAuthConfig.REDIRECT_URI}",
            )
        } finally {
            redirectServer.stop()
        }
    }
}
