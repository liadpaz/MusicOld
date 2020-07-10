package com.liadpaz.amp.service.server.service

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
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.liadpaz.amp.R
import com.liadpaz.amp.service.model.LocalFiles
import com.liadpaz.amp.service.repository.Repository
import com.liadpaz.amp.service.server.notification.AmpNotificationManager
import com.liadpaz.amp.service.server.utils.PlaybackPreparer
import com.liadpaz.amp.utils.Constants
import com.liadpaz.amp.view.MainActivity
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil
import java.util.*

class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var notificationManager: AmpNotificationManager

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val localFiles by lazy {
        LocalFiles.getInstance(applicationContext)
    }

    private var lastWindowIndex = -1

    private var isForeground = false

    private val ampAudioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    private val playerEventListener = PlayerEventListener()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
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

        becomingNoisyReceiver = BecomingNoisyReceiver(applicationContext, mediaSession.controller)

        mediaSessionConnector = MediaSessionConnector(mediaSession).also { connector ->
            val playbackPreparer: MediaSessionConnector.PlaybackPreparer = PlaybackPreparer(exoPlayer, DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name))))

            connector.setPlayer(exoPlayer)
            connector.setPlaybackPreparer(playbackPreparer)
            connector.setQueueNavigator(QueueNavigator(mediaSession))
        }

        mediaSession.controller.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        if (localFiles.stopOnTask) {
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
        Repository.getInstance(applicationContext).getSongs().observeForever { songs ->
            for (song in songs) {
                mediaItems.add(song.mediaItem)
            }
            result.sendResult(mediaItems)
        }
    }

    override fun onSearch(query: String, extras: Bundle, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val queryString = query.toLowerCase(Locale.US)
//        result.sendResult(SongsUtil.getSongs().filter { song -> song.isMatchingQuery(queryString) }.map { song: Song -> song.mediaItem })
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

        override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
            if (lastWindowIndex != exoPlayer.currentWindowIndex) {
                lastWindowIndex = exoPlayer.currentWindowIndex
                exoPlayer.playWhenReady = true
                QueueUtil.queuePosition.postValue(exoPlayer.currentWindowIndex)
//                val timeline = exoPlayer.currentTimeline
//                val queue = arrayListOf<Song>()
//                for (i in 0 until timeline.windowCount) {
//                    queue.add(Song.from(timeline.getWindow(i, Timeline.Window(), true).tag as MediaDescriptionCompat))
//                }
//                QueueUtil.queue.postValue(queue)
                Log.d(TAG, "onPositionDiscontinuity: ${/*queue.size*/null} $lastWindowIndex")
            }
        }
    }

    companion object {
        private const val TAG = "AmpApp.MediaService"
        private const val LOG_TAG = "AmpApp2.MediaSessionLog"
    }

    private class QueueNavigator(mediaSession: MediaSessionCompat) : TimelineQueueNavigator(mediaSession) {
        private val window = Timeline.Window()

        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat? = player.currentTimeline.getWindow(windowIndex, window, true).tag as MediaDescriptionCompat?

        companion object {
            private const val TAG = "AmpApp.AmpQueueNavigator"
        }
    }

    /**
     * This class is for the *Becoming Noisy* broadcast, eg. when the user hears music with
     * earphones and the earphones disconnects.
     *
     * It's stopping the playback when it receives that broadcast.
     */
    private class BecomingNoisyReceiver(private val context: Context, private val controller: MediaControllerCompat) : BroadcastReceiver() {

        private val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private var registered = false

        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                controller.transportControls.pause()
            }
        }

        /**
         * This function registers the broadcast receiver.
         */
        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyFilter)
                registered = true
            }
        }

        /**
         * This function unregisters the broadcast receiver.
         */
        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        companion object {
            private const val TAG = "AmpApp.BecomingNoisyReceiver"
        }
    }
}