package com.liadpaz.amp.viewmodels

import android.content.ContentUris
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.liadpaz.amp.utils.Utilities

class Song : Parcelable {
    val id: Long
    val title: String
    val artists: List<String>
    val album: String
    private val mediaMetadata: MediaMetadataCompat

    constructor(id: Long, title: String, artists: String, album: String, albumId: String) {
        this.id = id
        this.title = title
        this.artists = Utilities.getArtistsFromSong(title, artists)
        this.album = album
        this.mediaMetadata = MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id.toString())
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, artists)
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artists)
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId.toLong()).toString())
                .build()
    }

    private constructor(parcel: Parcel) {
        id = parcel.readLong()
        title = parcel.readString()!!
        artists = parcel.createStringArrayList()!!
        album = parcel.readString()!!
        mediaMetadata = parcel.readParcelable(MediaMetadataCompat::class.java.classLoader)!!
    }

    constructor(mediaMetadata: MediaMetadataCompat) {
        this.mediaMetadata = mediaMetadata
        id = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toLong()
        title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        artists = Utilities.getArtistsFromSong(title, mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
        album = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
    }

    constructor(mediaDescription: MediaDescriptionCompat) {
        id = mediaDescription.mediaId!!.toLong()
        title = mediaDescription.title.toString()
        artists = Utilities.getArtistsFromSong(title, mediaDescription.subtitle.toString())
        album = mediaDescription.description.toString()
        mediaMetadata = MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id.toString())
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, Utilities.joinArtists(artists))
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, Utilities.joinArtists(artists))
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, mediaDescription.iconUri.toString())
                .build()
    }

    val songUri: Uri
        get() = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

    val coverUri: Uri?
        get() = Uri.parse(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))

    fun toMediaItem(): MediaBrowserCompat.MediaItem {
        return MediaBrowserCompat.MediaItem(mediaMetadata.description, 0)
    }

    fun isMatchingQuery(query: String): Boolean {
        return title.contains(query) || Utilities.joinArtists(artists).contains(query) || album.contains(query)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as Song
        return title == other.title && album == other.album && Utilities.joinArtists(artists) == Utilities.joinArtists(other.artists)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(title)
        dest.writeStringList(artists)
        dest.writeString(album)
        dest.writeParcelable(mediaMetadata, 0)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (title.hashCode())
        result = 31 * result + artists.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + mediaMetadata.hashCode()
        return result
    }

    companion object {
        private const val TAG = "AmpApp.Song"
        val CREATOR: Parcelable.Creator<Song> = object : Parcelable.Creator<Song> {
            override fun createFromParcel(`in`: Parcel): Song {
                return Song(`in`)
            }

            override fun newArray(size: Int): Array<Song?> {
                return arrayOfNulls(size)
            }
        }
    }
}