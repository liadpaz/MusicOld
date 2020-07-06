package com.liadpaz.amp.ui.viewmodels

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import java.util.*

data class Album(val name: String, val artist: String, val songs: ArrayList<Song>) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.createTypedArrayList(Song.CREATOR)!!)

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(artist)
    }

    override fun describeContents(): Int = 0

    companion object {
        private const val TAG = "AmpApp.Album"

        val CREATOR: Parcelable.Creator<Album> = object : Parcelable.Creator<Album> {
            override fun createFromParcel(parcel: Parcel): Album = Album(parcel)

            override fun newArray(size: Int): Array<Album?> = arrayOfNulls(size)
        }

        val diffCallback = object : DiffUtil.ItemCallback<Album>() {
            override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean =
                    oldItem.name == newItem.name && oldItem.artist == newItem.artist

            override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean =
                    areItemsTheSame(oldItem, newItem) && oldItem.songs == newItem.songs
        }
    }
}