package com.liadpaz.amp.viewmodels

import android.os.Parcel
import android.os.Parcelable
import com.liadpaz.amp.viewmodels.Song
import java.util.*
import kotlin.collections.ArrayList

data class Artist(val name: String, val  songs: ArrayList<Song>) : Parcelable {
    private constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.createTypedArrayList(Song.CREATOR)!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeTypedList(songs)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as Artist
        return other.name == name && other.songs == songs
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + songs.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<Artist> {
        override fun createFromParcel(parcel: Parcel): Artist {
            return Artist(parcel)
        }

        override fun newArray(size: Int): Array<Artist?> {
            return arrayOfNulls(size)
        }
    }
}