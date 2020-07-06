package com.liadpaz.amp.utils

import android.content.Context
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
import com.google.android.exoplayer2.upstream.ContentDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.server.service.ServiceConnector
import com.liadpaz.amp.ui.viewmodels.Song

class PlaybackPreparer(
        private val exoPlayer: ExoPlayer,
        private val context: Context
) : MediaSessionConnector.PlaybackPreparer {
    override fun getSupportedPrepareActions(): Long = PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) {
        val mediaSource = ConcatenatingMediaSource()
        ContentDataSource(context).apply {
            for (song: Song in QueueUtil.queue.value!!) {
                mediaSource.addMediaSource(ProgressiveMediaSource.Factory(DataSource.Factory { this }).setTag(song.mediaDescription).createMediaSource(song.songUri))
            }
        }
        exoPlayer.prepare(mediaSource)
        exoPlayer.seekTo(QueueUtil.queuePosition.value!!, 0)
        ServiceConnector.getInstance().mediaSource.value = mediaSource
    }

    override fun onPrepareFromSearch(query: String, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, extras: Bundle) = Unit

    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle, cb: ResultReceiver) = false

    companion object {
        private const val TAG = "AmpApp.PlaybackPreparer"
    }
}