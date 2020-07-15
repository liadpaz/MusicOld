package com.liadpaz.amp.view.data

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import java.util.*

data class Playlist(val name: String, val songs: List<Song>) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.createTypedArrayList(Song.CREATOR)!!)

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeTypedList(songs)
    }

    override fun describeContents(): Int = 0

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + songs.hashCode()
        return result
    }

    companion object {
        val CREATOR: Parcelable.Creator<Playlist> = object : Parcelable.Creator<Playlist> {
            override fun createFromParcel(parcel: Parcel): Playlist = Playlist(parcel)

            override fun newArray(size: Int): Array<Playlist?> = arrayOfNulls(size)
        }

        val diffCallback = object : DiffUtil.ItemCallback<Playlist>() {
            override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean = oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
                    areItemsTheSame(oldItem, newItem) && oldItem.songs == newItem.songs
        }
    }
}