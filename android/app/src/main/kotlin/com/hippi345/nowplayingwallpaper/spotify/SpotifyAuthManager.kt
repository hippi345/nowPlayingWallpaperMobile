package com.hippi345.nowplayingwallpaper.spotify

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SpotifyAuthManager(
    private val tokenStore: SpotifyTokenStore,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    fun buildAuthorizeUri(): Uri {
        val verifier = SpotifyPkce.generateCodeVerifier()
        tokenStore.pendingCodeVerifier = verifier
        val challenge = SpotifyPkce.generateCodeChallenge(verifier)
        return Uri.parse(SpotifyOAuthConfig.AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("client_id", SpotifyOAuthConfig.clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", SpotifyOAuthConfig.redirectUri)
            .appendQueryParameter("scope", SpotifyOAuthConfig.SCOPES)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .build()
    }

    suspend fun completeAuthorization(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        val verifier = tokenStore.pendingCodeVerifier
            ?: return@withContext Result.failure(IllegalStateException("Missing PKCE verifier"))
        exchangeAuthorizationCode(code, verifier)
    }

    suspend fun ensureValidAccessToken(): String? = withContext(Dispatchers.IO) {
        if (tokenStore.hasValidAccessToken()) {
            return@withContext tokenStore.accessToken()
        }
        val refresh = tokenStore.refreshToken() ?: return@withContext null
        refreshAccessToken(refresh).getOrNull()
    }

    fun isSignedIn(): Boolean =
        tokenStore.hasValidAccessToken() || !tokenStore.refreshToken().isNullOrBlank()

    fun signOut() {
        tokenStore.clear()
    }

    private fun exchangeAuthorizationCode(code: String, verifier: String): Result<Unit> {
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", SpotifyOAuthConfig.redirectUri)
            .add("client_id", SpotifyOAuthConfig.clientId)
            .add("code_verifier", verifier)
            .build()
        return postTokenRequest(body)
    }

    private fun refreshAccessToken(refreshToken: String): Result<String> {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", SpotifyOAuthConfig.clientId)
            .build()
        return postTokenRequest(body).map { tokenStore.accessToken()!! }
    }

    private fun postTokenRequest(body: FormBody): Result<Unit> {
        val request = Request.Builder()
            .url(SpotifyOAuthConfig.TOKEN_URL)
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            val payload = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("Token request failed: ${response.code}"))
            }
            val json = JSONObject(payload)
            val access = json.getString("access_token")
            val refresh = json.optString("refresh_token").ifBlank { tokenStore.refreshToken() }
            val expiresIn = json.getLong("expires_in")
            tokenStore.saveTokens(access, refresh, expiresIn)
            return Result.success(Unit)
        }
    }
}
