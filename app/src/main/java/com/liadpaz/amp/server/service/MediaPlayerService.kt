package com.liadpaz.amp.server.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.liadpaz.amp.R
import com.liadpaz.amp.model.repositories.MainRepository
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.server.notification.AmpNotificationManager
import com.liadpaz.amp.server.utils.PlaybackPreparer
import com.liadpaz.amp.server.utils.QueueEditor
import com.liadpaz.amp.utils.Constants
import com.liadpaz.amp.view.MainActivity
import com.liadpaz.amp.view.data.Song
import java.util.*

class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var notificationManager: AmpNotificationManager

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val songsRepository by lazy {
        SongsRepository.getInstance(application)
    }
    private val mainRepository by lazy {
        MainRepository.getInstance(application)
    }

    private val mediaSource = ConcatenatingMediaSource()

    private var lastWindowIndex = -1

    private var isForeground = false

    private val ampAudioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    private val playerEventListener = PlayerEventListener()

    private val exoPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            setHandleAudioBecomingNoisy(true)
            setAudioAttributes(ampAudioAttributes, true)
            addListener(playerEventListener)
        }
    }

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java).putExtra(Constants.PREFERENCES_SHOW_CURRENT, ""), PendingIntent.FLAG_UPDATE_CURRENT)
        mediaSession = MediaSessionCompat(applicationContext, LOG_TAG).apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        notificationManager = AmpNotificationManager(applicationContext, exoPlayer, sessionToken!!, PlayerNotificationListener())

        mediaSessionConnector = MediaSessionConnector(mediaSession).also { connector ->
            val dataSourceFactory = DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name)))
            val playbackPreparer: MediaSessionConnector.PlaybackPreparer = PlaybackPreparer(exoPlayer, mediaSource, dataSourceFactory, songsRepository)

            connector.setPlayer(exoPlayer)
            connector.setPlaybackPreparer(playbackPreparer)
            connector.setQueueNavigator(QueueNavigator(mediaSession))
            val queueDataAdapter = object : QueueEditor.QueueDataAdapter {
                override fun add(position: Int, mediaSource: MediaSource) {
                    songsRepository.addSongToQueue(position, Song.from(mediaSource.tag as MediaDescriptionCompat))
                }

                override fun remove(position: Int) {
                    songsRepository.removeSongFromQueue(position)
                }

                override fun move(from: Int, to: Int) {
                    songsRepository.moveSongInQueue(from, to)
                }

                override fun clear() {
                    songsRepository.clearQueue()
                }
            }
            val mediaSourceFactory = object : QueueEditor.MediaSourceFactory {
                override fun createMediaSource(description: MediaDescriptionCompat): MediaSource? =
                        ProgressiveMediaSource.Factory(dataSourceFactory).setTag(description).createMediaSource(description.mediaUri)
            }
            connector.setQueueEditor(QueueEditor(mediaSession.controller, mediaSource, queueDataAdapter, mediaSourceFactory))
        }

        mediaSession.controller.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        if (mainRepository.getStopOnTask().value!!) {
            exoPlayer.stop(true)
        }
    }

    override fun onDestroy() {
        mediaSession.isActive = false
        mediaSession.release()
        exoPlayer.removeListener(playerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot = BrowserRoot(getString(R.string.app_name), null)

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val mediaItems: ArrayList<MediaBrowserCompat.MediaItem> = arrayListOf()
        songsRepository.getSongs().value?.let { songs ->
            for (song in songs) {
                mediaItems.add(song.mediaItem)
            }
            result.sendResult(mediaItems)
        } ?: result.sendError(null)
    }

    override fun onSearch(query: String, extras: Bundle, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val queryString = query.toLowerCase(Locale.ROOT)
        result.sendResult(SongsRepository.getInstance(application).getSongs().value?.filter { song -> song.isMatchingQuery(queryString) }?.map { song: Song -> song.mediaItem })
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {

        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            if (ongoing && !isForeground) {
                ContextCompat.startForegroundService(applicationContext, Intent(application, MediaPlayerService::class.java))
                startForeground(notificationId, notification)
                isForeground = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForeground = false
            stopSelf()
        }
    }

    private inner class PlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                notificationManager.showNotification()
                if (playbackState == Player.STATE_READY) {
                    if (!playWhenReady) {
                        stopForeground(false)
                    }
                }
            } else {
                notificationManager.hideNotification()
            }
        }

        override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
            if (lastWindowIndex != exoPlayer.currentWindowIndex) {
                lastWindowIndex = exoPlayer.currentWindowIndex
                exoPlayer.playWhenReady = true
                songsRepository.getPosition().postValue(exoPlayer.currentWindowIndex)
//                Log.d(TAG, "onPositionDiscontinuity: ${exoPlayer.currentTimeline.windowCount} $lastWindowIndex")
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            Log.e(TAG, "onPlayerError: $error")
        }
    }

    private class QueueNavigator(mediaSession: MediaSessionCompat) : TimelineQueueNavigator(mediaSession) {
        private val window = Timeline.Window()

        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
                player.currentTimeline.getWindow(windowIndex, window).tag as MediaDescriptionCompat

        companion object {
            private const val TAG = "AmpApp.AmpQueueNavigator"
        }
    }
}

private const val TAG = "AmpApp.MediaService"
private const val LOG_TAG = "AmpApp2.MediaSessionLog"