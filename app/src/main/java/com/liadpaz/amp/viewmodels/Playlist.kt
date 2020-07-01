package com.liadpaz.amp.viewmodels

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Playlist(val name: String, val songs: ArrayList<Song>) : Parcelable {

    private constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.createTypedArrayList(Song.CREATOR)!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeTypedList(songs)
    }

    override fun toString(): String {
        return """
            name: $name
            songs: ${songs.toTypedArray().contentDeepToString()}
            """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as Playlist
        return other.name == name && other.songs == songs
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + songs.hashCode()
        return result
    }

    companion object {
        val CREATOR: Parcelable.Creator<Playlist> = object : Parcelable.Creator<Playlist> {
            override fun createFromParcel(`in`: Parcel): Playlist {
                return Playlist(`in`)
            }

            override fun newArray(size: Int): Array<Playlist?> {
                return arrayOfNulls(size)
            }
        }
    }
}