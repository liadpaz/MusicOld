package com.liadpaz.amp.service.server.service

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
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.source.ConcatenatingMediaSource

class ServiceConnection private constructor(context: Context, componentName: ComponentName) {

    val isConnected = MutableLiveData<Boolean>().apply {
        postValue(false)
    }
    val playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }
    val nowPlaying = MutableLiveData(NOTHING_PLAYING)
    val repeatMode = MutableLiveData(PlaybackStateCompat.REPEAT_MODE_ALL)
    val mediaSource = MutableLiveData(ConcatenatingMediaSource())

    lateinit var transportControls: MediaControllerCompat.TransportControls

    private var mediaBrowserConnectionCallback: MediaBrowserConnectionCallback? = null
    private val mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat

    fun subscribe(parentId: String, subscriptionCallback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, subscriptionCallback)
    }

    fun unsubscribe(parentId: String, subscriptionCallback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, subscriptionCallback)
    }

    @JvmOverloads
    fun sendCommand(command: String, parameters: Bundle?, resultCallback: ((Int?, Bundle?) -> Unit) = { _, _ -> }) {
        mediaController.sendCommand(command, parameters, object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                resultCallback(resultCode, resultData)
            }
        })
    }

    private inner class MediaBrowserConnectionCallback internal constructor(private val context: Context) : MediaBrowserCompat.ConnectionCallback() {
        private val TAG = "AmpApp.MediaBrowserConnectionCallback"

        override fun onConnected() {
            try {
                mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
                mediaController.registerCallback(MediaControllerCallback())
                transportControls = mediaController.transportControls
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
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

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.postValue(if (metadata!!.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) == null) NOTHING_PLAYING else metadata)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            this@ServiceConnection.repeatMode.postValue(mediaController.repeatMode)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback!!.onConnectionSuspended()
        }
    }

    companion object {
        private const val TAG = "AmpApp.ServiceConnector"

        @Volatile
        private var instance: ServiceConnection? = null

        @JvmStatic
        fun getInstance(context: Context, componentName: ComponentName): ServiceConnection {
            synchronized(ServiceConnection::class.java) {
                if (instance == null) {
                    instance = ServiceConnection(context, componentName)
                }
            }
            return instance!!
        }

        @JvmStatic
        fun getInstance(): ServiceConnection {
            checkNotNull(instance)
            return instance!!
        }

        fun playFromQueue() {
            getInstance().transportControls.playFromMediaId("queue", null)
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