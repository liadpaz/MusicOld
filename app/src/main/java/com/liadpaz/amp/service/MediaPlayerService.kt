package com.liadpaz.amp.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.liadpaz.amp.MainActivity
import com.liadpaz.amp.R
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.livedatautils.SongsUtil
import com.liadpaz.amp.notification.AmpNotificationManager
import com.liadpaz.amp.utils.AmpPlaybackPreparer
import com.liadpaz.amp.viewmodels.Song
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class MediaPlayerService : MediaBrowserServiceCompat() {
    private val TAG = "AmpApp.MediaPlayerService"
    private val LOG_TAG = "AmpApp2.MediaSessionLog"

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var notificationManager: AmpNotificationManager

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var isForeground = false

    private val ampAudioAttributes = AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build()
    private val playerEventListener = PlayerEventListener()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
            setAudioAttributes(ampAudioAttributes, true)
            addListener(playerEventListener)
        }
    }

    // observers
    //    private Observer<Integer> observerPosition;
    //    private Observer<ArrayList<Song>> observerQueue;
    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java), 0)
        mediaSession = MediaSessionCompat(applicationContext, LOG_TAG).apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        notificationManager = AmpNotificationManager(applicationContext, exoPlayer, sessionToken!!, PlayerNotificationListener())

        becomingNoisyReceiver = BecomingNoisyReceiver(applicationContext, mediaSession.controller)

        mediaSessionConnector = MediaSessionConnector(mediaSession).also { connector ->
            val dataSourceFactory = DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name)))
            val playbackPreparer: MediaSessionConnector.PlaybackPreparer = AmpPlaybackPreparer(exoPlayer, dataSourceFactory)

            connector.setPlayer(exoPlayer)
            connector.setPlaybackPreparer(playbackPreparer)
            connector.setQueueNavigator(AmpQueueNavigator(mediaSession))
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        exoPlayer.stop(true)
    }

    override fun onDestroy() {
        mediaSession.isActive = false
        mediaSession.release()
        exoPlayer.removeListener(playerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot = BrowserRoot(getString(R.string.app_name), null)

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val mediaItems: ArrayList<MediaBrowserCompat.MediaItem> = java.util.ArrayList()
        for (song in SongsUtil.getSongs()) {
            mediaItems.add(song.toMediaItem())
        }
        result.sendResult(mediaItems)
    }

    override fun onSearch(query: String, extras: Bundle, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val finalQuery = query.toLowerCase(Locale.US)
        result.sendResult(SongsUtil.getSongs().filter { song -> song.isMatchingQuery(finalQuery)}.map {song: Song -> song.toMediaItem() })
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        private val TAG = "AmpApp.PlayerNotificationListener"

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
        private val TAG = "AmpApp.PlayerEventListener"

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                notificationManager.showNotification()
                becomingNoisyReceiver.register()
                if (playbackState == Player.STATE_READY) {
                    if (!playWhenReady) {
                        stopForeground(false)
                    }
                }
            } else {
                notificationManager.hideNotification()
                becomingNoisyReceiver.unregister()
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                mediaSession.controller.transportControls.play()
            }
            QueueUtil.queuePosition.postValue(exoPlayer.currentWindowIndex)
        }

        override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
            QueueUtil.queuePosition.postValue(exoPlayer.currentWindowIndex)
        }
    }

    private class AmpQueueNavigator(mediaSession: MediaSessionCompat) : TimelineQueueNavigator(mediaSession, 1000) {
        private val TAG = "AmpApp.AmpQueueNavigator"

        private val window = Timeline.Window()

        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat = player.currentTimeline.getWindow(windowIndex, window, true).tag as MediaDescriptionCompat
    }

    /**
     * This class is for the *Becoming Noisy* broadcast, eg. when the user hears music with
     * earphones and the earphones disconnects.
     *
     * It's stopping the playback when it receives that broadcast.
     */
    private class BecomingNoisyReceiver(private val context: Context, private val controller: MediaControllerCompat) : BroadcastReceiver() {
        private val TAG = "AmpApp.BecomingNoisyReceiver"

        private val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private var registered = false

        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                controller.transportControls.pause()
            }
        }

        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyFilter)
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }
    }
}