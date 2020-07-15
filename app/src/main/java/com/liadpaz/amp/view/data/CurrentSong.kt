package com.liadpaz.amp.view.data

import android.graphics.Bitmap
import com.liadpaz.amp.utils.Utilities

data class CurrentSong(val title: String, val artists: List<String>, val album: String, val duration: Long, val art: Bitmap, val color: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as CurrentSong
        return title == other.title && artists == other.artists && album == other.album
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artists.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + art.hashCode()
        result = 31 * result + color
        return result
    }

    override fun toString(): String = "$artists - $title ($album) [${Utilities.formatTime(duration)}]"
}