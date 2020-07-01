package com.liadpaz.amp.utils

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
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.service.ServiceConnector

class AmpPlaybackPreparer(private val exoPlayer: ExoPlayer, private val dataSourceFactory: DataSource.Factory) : MediaSessionConnector.PlaybackPreparer {
    private val TAG = "AmpApp.PlaybackPreparer"

    override fun getSupportedPrepareActions(): Long = PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) {
        val mediaSource = ConcatenatingMediaSource(true)
        mediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(null))
        ServiceConnector.getInstance()!!.mediaSource.value = mediaSource
        exoPlayer.prepare(mediaSource)
        exoPlayer.seekTo(QueueUtil.queuePosition.value!!, 0)
    }

    override fun onPrepareFromSearch(query: String, extras: Bundle?) {}

    override fun onPrepareFromUri(uri: Uri, extras: Bundle) {}

    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle, cb: ResultReceiver) = false
}