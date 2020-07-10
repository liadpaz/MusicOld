package com.liadpaz.amp.service.server.utils

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil

class PlaybackPreparer(
        private val exoPlayer: ExoPlayer,
        private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {
    override fun getSupportedPrepareActions(): Long = PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) {
        val mediaSource = ConcatenatingMediaSource()
        for (song: Song in QueueUtil.queue.value!!) {
            mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaDescription).createMediaSource(song.songUri))
        }
        exoPlayer.prepare(mediaSource)
        exoPlayer.seekTo(QueueUtil.queuePosition.value!!, 0)
        ServiceConnection.getInstance().mediaSource.value = mediaSource
    }

    override fun onPrepareFromSearch(query: String, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, extras: Bundle) = Unit

    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle, cb: ResultReceiver) = false

    companion object {
        private const val TAG = "AmpApp.PlaybackPreparer"
    }
}