package com.liadpaz.amp.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import java.util.*
import java.util.function.BiConsumer

class ServiceConnector private constructor(context: Context, componentName: ComponentName) {

    val isConnected = MutableLiveData<Boolean>().apply {
        postValue(false)
    }
    val playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }
    val nowPlaying = MutableLiveData(NOTHING_PLAYING)
    val repeatMode = MutableLiveData(PlaybackStateCompat.REPEAT_MODE_ALL)
    val mediaSource = MutableLiveData(ConcatenatingMediaSource())
    val queue = MutableLiveData<List<MediaSessionCompat.QueueItem>>(ArrayList())
    val queueTitle = MutableLiveData<CharSequence?>(null)
    var transportControls: MediaControllerCompat.TransportControls? = null
    private var mediaBrowserConnectionCallback: MediaBrowserConnectionCallback? = null
    private val mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    fun subscribe(parentId: String, subscriptionCallback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, subscriptionCallback)
    }

    fun unsubscribe(parentId: String, subscriptionCallback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, subscriptionCallback)
    }

    @JvmOverloads
    fun sendCommand(command: String, parameters: Bundle?, resultCallback: BiConsumer<Int?, Bundle?> = BiConsumer { _, _ -> }) {
        mediaController!!.sendCommand(command, parameters, object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                resultCallback.accept(resultCode, resultData)
            }
        })
    }

    private inner class MediaBrowserConnectionCallback internal constructor(private val context: Context) : MediaBrowserCompat.ConnectionCallback() {
        private val TAG = "AmpApp.MediaBrowserConnectionCallback"

        override fun onConnected() {
            try {
                mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            mediaController!!.registerCallback(MediaControllerCallback())
            transportControls = mediaController!!.transportControls
            isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }

    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        private val TAG = "AmpApp.MediaControllerCallback"

        override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>) {
            this@ServiceConnector.queue.postValue(queue)
        }

        override fun onQueueTitleChanged(title: CharSequence) {
            queueTitle.postValue(title)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.postValue(if (metadata!!.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) == null) NOTHING_PLAYING else metadata)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            this@ServiceConnector.repeatMode.postValue(mediaController!!.repeatMode)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback!!.onConnectionSuspended()
        }
    }

    companion object {
        private const val TAG = "AmpApp.ServiceConnector"

        @Volatile
        private var instance: ServiceConnector? = null

        @JvmStatic
        @NonNull
        fun getInstance(context: Context, componentName: ComponentName): ServiceConnector? =
                instance ?: synchronized(this) {
                    instance ?: ServiceConnector(context, componentName).also {
                        instance = it
                    }
                }

        @JvmStatic
        @NonNull
        fun getInstance(): ServiceConnector? {
            checkNotNull(instance)
            return instance
        }

        fun playFromId(id: String) {
            getInstance()!!.transportControls!!.playFromMediaId(id, null)
        }
    }

    init {
        mediaBrowser = MediaBrowserCompat(context, componentName, MediaBrowserConnectionCallback(context).also { mediaBrowserConnectionCallback = it }, null)
        mediaBrowser.connect()
    }
}


val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
        .build()

val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
        .build()