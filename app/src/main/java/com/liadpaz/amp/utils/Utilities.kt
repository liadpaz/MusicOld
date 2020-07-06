package com.liadpaz.amp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Utilities {
    private const val TAG = "UTILITIES"
    fun joinArtists(artists: List<String?>): String {
        val joinedArtists = StringBuilder(artists[0]!!)
        for (i in 1 until artists.size) {
            joinedArtists.append(", ").append(artists[i])
        }
        return joinedArtists.toString()
    }

    fun getPathFromUri(uri: Uri): String {
        val path = uri.path!!.split(":").toTypedArray()
        return if (path.size == 1) "" else path[1]
    }

    fun isColorBright(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) > 0.5
    }

    /**
     * This function returns the time in a formatted string, eg. 0 millis to 0:00.
     *
     * @param millis the time to format in milliseconds
     * @return the formatted time
     */
    fun formatTime(millis: Long): String {
        val minutesTime = TimeUnit.MILLISECONDS.toMinutes(millis)
        val secondsTime = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val seconds = StringBuilder(secondsTime.toString())
        if (secondsTime < 10) {
            seconds.insert(0, "0")
        }
        return String.format("%s:%s", minutesTime, seconds)
    }

    fun getArtistsFromSong(title: String, artistsString: String): ArrayList<String> {
        val artists = ArrayList<String>()
        val matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artistsString)
        while (matcher.find()) {
            artists.add(matcher.group())
        }

        //        int openBracketIndex;
        //        int closeBracketIndex;
        //        if ((openBracketIndex = title.indexOf('(')) != -1 && (closeBracketIndex = title.indexOf(')')) != -1 && openBracketIndex < closeBracketIndex) {
        //
        //        }
        return artists
    }
}