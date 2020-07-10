package com.liadpaz.amp.view.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.recyclerview.widget.DiffUtil
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.liadpaz.amp.R
import com.liadpaz.amp.utils.Utilities

data class Song(val mediaId: Long, val title: String, val artists: List<String>, val album: String, val artUri: Uri) : Parcelable {
    private lateinit var mediaMetadata: MediaMetadataCompat

    init {
        if (!this::mediaMetadata.isInitialized) {
            this.mediaMetadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, Utilities.joinArtists(artists))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, Utilities.joinArtists(artists))
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, Utilities.joinArtists(artists))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artUri.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, artUri.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, artUri.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, songUri.toString())
                    .build()
        }
    }

    val mediaDescription: MediaDescriptionCompat
        get() = mediaMetadata.description

    val songUri: Uri
        get() = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId)

    val mediaItem: MediaBrowserCompat.MediaItem
        get() = MediaBrowserCompat.MediaItem(mediaMetadata.description, 0)

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString()!!,
            parcel.createStringArrayList()!!,
            parcel.readString()!!,
            parcel.readParcelable(Uri::class.java.classLoader)!!) {
        mediaMetadata = parcel.readParcelable(MediaMetadataCompat::class.java.classLoader)!!
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

    override fun hashCode(): Int {
        var result = mediaId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + artists.hashCode()
        result = 31 * result + album.hashCode()
        return result
    }

    fun toMediaSource(context: Context): ProgressiveMediaSource = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context.applicationContext, Util.getUserAgent(context.applicationContext, context.getString(R.string.app_name)))).setTag(mediaMetadata.description).createMediaSource(songUri)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(mediaId)
        parcel.writeString(title)
        parcel.writeStringList(artists)
        parcel.writeString(album)
        parcel.writeParcelable(artUri, flags)
        parcel.writeParcelable(mediaMetadata, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val TAG = "AmpApp.Song"

        fun from(mediaMetadata: MediaMetadataCompat) = Song(
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toLong(),
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                Utilities.getArtistsFromSong(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE), mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)),
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM),
                Uri.parse(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
        ).apply {
            this.mediaMetadata = mediaMetadata
        }

        fun from(mediaDescription: MediaDescriptionCompat) = Song(
                mediaDescription.mediaId!!.toLong(),
                mediaDescription.title.toString(),
                Utilities.getArtistsFromSong(mediaDescription.title.toString(), mediaDescription.subtitle.toString()),
                mediaDescription.description.toString(),
                mediaDescription.iconUri!!
        ).apply {
            val mediaId = mediaDescription.mediaId
            val title = mediaDescription.title.toString()
            val artist = Utilities.joinArtists(Utilities.getArtistsFromSong(title, mediaDescription.subtitle.toString()))
            val album = mediaDescription.description.toString()
            val artUri = mediaDescription.iconUri
            this.mediaMetadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artUri.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, artUri.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, artUri.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, songUri.toString())
                    .build()
        }

        val CREATOR: Parcelable.Creator<Song> = object : Parcelable.Creator<Song> {
            override fun createFromParcel(parcel: Parcel): Song = Song(parcel)

            override fun newArray(size: Int): Array<Song?> = arrayOfNulls(size)
        }

        val diffCallback = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.mediaId == newItem.mediaId && oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = areItemsTheSame(oldItem, newItem)

        }
    }
}