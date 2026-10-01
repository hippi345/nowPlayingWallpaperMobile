package com.hippi345.nowplayingwallpaper.spotify

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SpotifyTokenStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var pendingCodeVerifier: String?
        get() = prefs.getString(KEY_CODE_VERIFIER, null)
        set(value) {
            prefs.edit().putString(KEY_CODE_VERIFIER, value).apply()
        }

    fun saveTokens(accessToken: String, refreshToken: String?, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT_MS, expiresAt)
            .remove(KEY_CODE_VERIFIER)
            .apply()
    }

    fun accessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun refreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun expiresAtMs(): Long = prefs.getLong(KEY_EXPIRES_AT_MS, 0L)

    fun hasValidAccessToken(): Boolean {
        val token = accessToken()
        if (token.isNullOrBlank()) return false
        return System.currentTimeMillis() < expiresAtMs() - EXPIRY_BUFFER_MS
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "spotify_auth_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT_MS = "expires_at_ms"
        private const val KEY_CODE_VERIFIER = "code_verifier"
        private const val EXPIRY_BUFFER_MS = 60_000L
    }
}
