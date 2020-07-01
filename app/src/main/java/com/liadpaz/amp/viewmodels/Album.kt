package com.liadpaz.amp.viewmodels

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Album(val name: String, val artist: String, val songs: ArrayList<Song>) : Parcelable {

    private constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readString()!!, parcel.createTypedArrayList(Song.CREATOR)!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(artist)
        dest.writeTypedList(songs)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as Album
        return other.artist == artist && other.name == name && other.songs == songs
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + songs.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }
}