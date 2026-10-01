package com.hippi345.nowplayingwallpaper.spotify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SpotifyPollBackoffTest {
    @Test
    fun parseRetryAfter_seconds() {
        assertEquals(120_000L, SpotifyPollBackoff.parseRetryAfterMs("120", 0L))
    }

    @Test
    fun parseRetryAfter_httpDate() {
        val header = "Wed, 21 Oct 2015 07:28:00 GMT"
        val delay = SpotifyPollBackoff.parseRetryAfterMs(header, 1_445_412_420_000L)
        assertEquals(60_000L, delay)
    }

    @Test
    fun parseRetryAfter_invalid_returnsNull() {
        assertNull(SpotifyPollBackoff.parseRetryAfterMs("not-a-date", 0L))
    }

    @Test
    fun nextDelay_usesRetryAfterWhenPresent() {
        val delay = SpotifyPollBackoff.nextDelayMs("45", consecutive429WithoutRetryAfter = 3, nowEpochMs = 0L)
        assertEquals(45_000L, delay)
    }

    @Test
    fun nextDelay_exponentialWhenHeaderMissing() {
        assertEquals(30_000L, SpotifyPollBackoff.nextDelayMs(null, 0, 0L))
        assertEquals(60_000L, SpotifyPollBackoff.nextDelayMs(null, 1, 0L))
        assertEquals(120_000L, SpotifyPollBackoff.nextDelayMs(null, 2, 0L))
        assertEquals(300_000L, SpotifyPollBackoff.nextDelayMs(null, 10, 0L))
    }
}
