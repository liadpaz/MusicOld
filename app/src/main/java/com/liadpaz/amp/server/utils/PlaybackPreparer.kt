package com.liadpaz.amp.server.utils

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.liadpaz.amp.model.repositories.SongsRepository
import java.util.*

class PlaybackPreparer(
        private val exoPlayer: ExoPlayer,
        private val mediaSource: ConcatenatingMediaSource,
        private val dataSourceFactory: DataSource.Factory,
        private val songsRepository: SongsRepository
) : MediaSessionConnector.PlaybackPreparer {

    init {
        exoPlayer.prepare(mediaSource)
    }

    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        mediaSource.clear()
        val (action, who, index) = getFromMediaId(mediaId)
        val shuffle = extras?.getBoolean(EXTRA_SHUFFLE) ?: false
        Log.d(TAG, "onPrepareFromMediaId: $index")
        when (action) {
            ARTIST -> {
                val songs = songsRepository.getArtists().value!!.find { artist -> artist.name == who }!!.songs.apply { if (shuffle) shuffle() }
                songsRepository.getQueue().postValue(songs)
                for (song in songs) {
                    mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaDescription).createMediaSource(song.songUri))
                }
            }
            ALBUM -> {
                val songs = songsRepository.getAlbums().value!!.find { album -> album.name == who }!!.songs.apply { if (shuffle) shuffle() }
                songsRepository.getQueue().postValue(songs)
                for (song in songs) {
                    mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaDescription).createMediaSource(song.songUri))
                }
            }
            PLAYLIST -> {
                val songs = songsRepository.getPlaylists().value!![who]!!.apply { if (shuffle) shuffle() }
                songsRepository.getQueue().postValue(songs)
                for (song in songs) {
                    mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaDescription).createMediaSource(song.songUri))
                }
            }
            RECENTLY -> {
                val songs = songsRepository.getRecentlyAddedPlaylist().value!!.apply { if (shuffle) shuffle() }
                songsRepository.getQueue().postValue(songs)
                for (song in songs) {
                    mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaDescription).createMediaSource(song.songUri))
                }
            }
            SEARCH -> {
                onPrepareFromSearch(who, playWhenReady, extras)
            }
            ALL -> {
                val songs = songsRepository.getSongs().value!!.apply { if (shuffle) shuffle() }
                songsRepository.getQueue().postValue(songs)
                for (song in songs) {
                    mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaDescription).createMediaSource(song.songUri))
                }
            }
            else -> throw IllegalArgumentException("mediaId isn't set correctly")
        }
        exoPlayer.seekTo(index, 0)
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
        mediaSource.clear()
        val queryString = query.toLowerCase(Locale.ROOT)
        val songs = songsRepository.getSongs().value?.filter { song -> song.isMatchingQuery(queryString) }
        if (songs != null) {
            songsRepository.getQueue().postValue(songs.toMutableList())
            for (song in songs) {
                mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaDescription).createMediaSource(song.songUri))
            }
            exoPlayer.seekTo(extras?.getInt(EXTRA_POSITION) ?: 0, 0)
        }
    }

    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?): Boolean = false

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepare(playWhenReady: Boolean) = Unit
}

private const val TAG = "AmpApp.PlaybackPreparer"

fun buildMediaId(action: String, who: String?, index: Int) = "$action:$who:$index"

private fun getFromMediaId(mediaId: String): Triple<String, String, Int> {
    val splitId = mediaId.split(":")
    return Triple(splitId[0], splitId[1], splitId[2].toInt())
}

const val ARTIST = "artist"
const val ALBUM = "album"
const val PLAYLIST = "playlist"
const val RECENTLY = "recently"
const val SEARCH = "search"
const val ALL = "all"

const val EXTRA_SHUFFLE = "shuffle"
const val EXTRA_POSITION = "position"