package com.liadpaz.amp.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.liadpaz.amp.R
import com.liadpaz.amp.service.repository.Repository
import com.liadpaz.amp.service.server.service.EMPTY_PLAYBACK_STATE
import com.liadpaz.amp.service.server.service.NOTHING_PLAYING
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.view.data.CurrentSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayingViewModel(application: Application, serviceConnection: ServiceConnection) : AndroidViewModel(application) {

    val screenOnObserver = Repository.getInstance(application).getScreenOn()

    val playbackState: MutableLiveData<PlaybackStateCompat> = MutableLiveData(EMPTY_PLAYBACK_STATE)
    val mediaMetadata = MutableLiveData<CurrentSong>()
    val mediaPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }

    val transportControls: MediaControllerCompat.TransportControls

    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState.postValue(it ?: EMPTY_PLAYBACK_STATE)
        val metadata = serviceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(metadata)
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(it)
    }

    private val serviceConnection = serviceConnection.also {
        transportControls = it.transportControls
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        checkPlaybackState()
    }

    private fun checkPlaybackState(): Boolean = handler.postDelayed({
        val currentPosition = playbackState.value?.currentPlayBackPosition
        if (mediaPosition.value != currentPosition) {
            mediaPosition.postValue(currentPosition)
        }
        if (updatePosition) {
            checkPlaybackState()
        }
    }, 100)

    override fun onCleared() {
        super.onCleared()

        serviceConnection.playbackState.removeObserver(playbackStateObserver)
        serviceConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        updatePosition = false
    }

    private fun updateState(mediaMetadata: MediaMetadataCompat) {
        if (mediaMetadata.duration != 0L) {
            CoroutineScope(Dispatchers.Default).launch {
                val bitmap = getCover(getApplication(), mediaMetadata.artUri)
                        ?: BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.song)
                val dominantColor = Palette.from(bitmap).generate().getDominantColor(Color.WHITE)
                val currentSong = CurrentSong(mediaMetadata.title, mediaMetadata.artists, mediaMetadata.album, mediaMetadata.duration, bitmap, dominantColor)
                if (this@PlayingViewModel.mediaMetadata.value != currentSong) {
                    this@PlayingViewModel.mediaMetadata.postValue(currentSong)
                }
            }
        }
    }

    class Factory(private val application: Application, private val serviceConnection: ServiceConnection) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlayingViewModel(application, serviceConnection) as T
        }
    }
}

private fun getCover(context: Context, artUri: Uri) = BitmapFactory.decodeStream(context.contentResolver.openInputStream(artUri))

inline val PlaybackStateCompat.currentPlayBackPosition: Long
    get() = if (state == PlaybackStateCompat.STATE_PLAYING) {
        val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (timeDelta * playbackSpeed)).toLong()
    } else {
        position
    }

inline val MediaMetadataCompat.duration
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

inline val MediaMetadataCompat.id: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline val MediaMetadataCompat.artUri: Uri
    get() = Uri.parse(getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI))

inline val MediaMetadataCompat.title: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)

inline val MediaMetadataCompat.artists: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

inline val MediaMetadataCompat.album: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)

private const val TAG = "AmpApp.PlayingViewModel"