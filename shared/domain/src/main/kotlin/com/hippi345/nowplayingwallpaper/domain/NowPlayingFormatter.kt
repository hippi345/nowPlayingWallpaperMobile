package com.hippi345.nowplayingwallpaper.domain

object NowPlayingFormatter {
    fun caption(track: NowPlayingTrack): String =
        "${track.title} — ${track.artist}"

    fun accessibilityDescription(track: NowPlayingTrack): String =
        "Now playing ${track.title} by ${track.artist} from ${track.albumName}"
}
