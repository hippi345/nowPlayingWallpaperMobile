package com.hippi345.nowplayingwallpaper.spotify

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Computes how long to wait before the next currently-playing poll after HTTP 429.
 */
object SpotifyPollBackoff {
    const val DEFAULT_POLL_INTERVAL_MS: Long = 2_000L

    private const val INITIAL_BACKOFF_MS: Long = 30_000L
    private const val MAX_BACKOFF_MS: Long = 5 * 60_000L

    fun nextDelayMs(
        retryAfterHeader: String?,
        consecutive429WithoutRetryAfter: Int,
        nowEpochMs: Long,
    ): Long {
        val fromHeader = parseRetryAfterMs(retryAfterHeader, nowEpochMs)
        if (fromHeader != null) {
            return fromHeader.coerceAtLeast(DEFAULT_POLL_INTERVAL_MS)
        }
        val exponent = consecutive429WithoutRetryAfter.coerceAtLeast(0).coerceAtMost(4)
        val multiplier = 1L shl exponent
        return (INITIAL_BACKOFF_MS * multiplier).coerceAtMost(MAX_BACKOFF_MS)
    }

    fun parseRetryAfterMs(header: String?, nowEpochMs: Long): Long? {
        if (header.isNullOrBlank()) return null
        val trimmed = header.trim()
        trimmed.toLongOrNull()?.let { seconds ->
            return seconds.coerceAtLeast(0) * 1_000L
        }
        return try {
            val formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US)
            val instant = Instant.from(formatter.parse(trimmed))
            (instant.toEpochMilli() - nowEpochMs).coerceAtLeast(0)
        } catch (_: Exception) {
            null
        }
    }
}
