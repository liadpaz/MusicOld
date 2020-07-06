package com.liadpaz.amp.ui.viewmodels

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil

data class Artist(val name: String, val songs: ArrayList<Song>) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.createTypedArrayList(Song.CREATOR)!!)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as Artist
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
        val CREATOR: Parcelable.Creator<Artist> = object : Parcelable.Creator<Artist> {
            override fun createFromParcel(parcel: Parcel): Artist = Artist(parcel)

            override fun newArray(size: Int): Array<Artist?> = arrayOfNulls(size)
        }

        val diffCallback = object : DiffUtil.ItemCallback<Artist>() {
            override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean = oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean =
                    areItemsTheSame(oldItem, newItem) && oldItem.songs == newItem.songs
        }
    }
}