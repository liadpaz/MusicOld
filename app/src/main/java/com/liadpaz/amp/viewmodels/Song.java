package com.liadpaz.amp.viewmodels;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.amp.utils.Utilities;

import java.util.List;

public class Song implements Parcelable {
    private static final String TAG = "AmpApp.Song";
    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @NonNull
        @Override
        public Song createFromParcel(@NonNull Parcel in) {
            return new Song(in);
        }

        @NonNull
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public final long id;
    public final String title;
    public final List<String> artists;
    public final String album;
    public final MediaMetadataCompat mediaMetadata;

    public Song(long id, @NonNull String title, @NonNull String artists, @NonNull String album, @NonNull String albumId) {
        this.id = id;
        this.title = title;
        this.artists = Utilities.getArtistsFromSong(title, artists);
        this.album = album;
        this.mediaMetadata = new MediaMetadataCompat.Builder()
                                     .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(id))
                                     .putText(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                     .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                                     .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, artists)
                                     .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artists)
                                     .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                                     .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                                     .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId)).toString())
                                     .build();
    }

    private Song(@NonNull Parcel in) {
        id = in.readLong();
        title = in.readString();
        artists = in.createStringArrayList();
        album = in.readString();
        mediaMetadata = in.readParcelable(MediaMetadataCompat.class.getClassLoader());
    }

    public Song(@NonNull MediaMetadataCompat mediaMetadata) {
        this.mediaMetadata = mediaMetadata;
        this.id = Long.parseLong(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        this.title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        this.artists = Utilities.getArtistsFromSong(title, mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        this.album = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
    }

    @SuppressWarnings("ConstantConditions")
    public Song(@NonNull MediaDescriptionCompat mediaDescription) {
        this.id = Long.parseLong(mediaDescription.getMediaId());
        this.title = mediaDescription.getTitle().toString();
        this.artists = Utilities.getArtistsFromSong(title, mediaDescription.getSubtitle().toString());
        this.album = mediaDescription.getDescription().toString();
        this.mediaMetadata = new MediaMetadataCompat.Builder()
                                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(id))
                                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, Utilities.joinArtists(artists))
                                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, Utilities.joinArtists(artists))
                                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, mediaDescription.getIconUri().toString())
                                .build();
    }

    @Nullable
    public Uri getCoverUri() {
        return Uri.parse(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
    }

    @NonNull
    public MediaBrowserCompat.MediaItem toMediaItem() {
        return new MediaBrowserCompat.MediaItem(mediaMetadata.getDescription(), 0);
    }

    public boolean isMatchingQuery(@NonNull String query) {
        return title.contains(query) || Utilities.joinArtists(artists).contains(query) || album.contains(query);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Song) {
            Song other = (Song)obj;
            return title.equals(other.title) && album.equals(other.album) && Utilities.joinArtists(artists).equals(Utilities.joinArtists(other.artists));
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeStringList(artists);
        dest.writeString(album);
        dest.writeParcelable(mediaMetadata, 0);
    }
}