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
import com.liadpaz.amp.model.repositories.MainRepository
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.server.service.EMPTY_PLAYBACK_STATE
import com.liadpaz.amp.server.service.NOTHING_PLAYING
import com.liadpaz.amp.server.service.ServiceConnection
import com.liadpaz.amp.server.utils.COMMAND_CLEAR_QUEUE
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.data.CurrentSong
import com.liadpaz.amp.view.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayingViewModel(application: Application, serviceConnection: ServiceConnection) : AndroidViewModel(application) {

    val screenOnObserver: LiveData<Boolean> = MainRepository.getInstance(application).getScreenOn()

    private val repository = SongsRepository.getInstance(application)

    fun getQueue(): LiveData<MutableList<Song>> = repository.getQueue()
    fun getPlaybackState(): LiveData<PlaybackStateCompat> = _playbackState
    fun getMediaMetadata(): LiveData<CurrentSong> = _mediaMetadata
    fun getRepeatMode(): LiveData<Int> = _repeatMode
    fun getMediaPosition(): LiveData<Long> = _mediaPosition

    private val _playbackState = MutableLiveData<PlaybackStateCompat>().apply { postValue(EMPTY_PLAYBACK_STATE) }
    private val _mediaMetadata = MutableLiveData<CurrentSong>()
    private val _repeatMode = MutableLiveData<Int>().apply { postValue(PlaybackStateCompat.REPEAT_MODE_ALL) }
    private val _mediaPosition = MutableLiveData<Long>().apply { postValue(0L) }

    val transportControls: MediaControllerCompat.TransportControls

    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        _playbackState.postValue(it ?: EMPTY_PLAYBACK_STATE)
        val metadata = serviceConnection.nowPlaying.value ?: NOTHING_PLAYING
        CoroutineScope(Dispatchers.Main).launch {
            updateState(metadata)
        }
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        CoroutineScope(Dispatchers.Main).launch {
            updateState(it)
        }
    }

    private val repeatModeObserver = Observer<Int> {
        _repeatMode.postValue(it)
    }

    private val serviceConnection = serviceConnection.also {
        transportControls = it.transportControls
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        it.repeatMode.observeForever(repeatModeObserver)
        checkPlaybackState()
    }

    fun clearQueue() {
        transportControls.sendCustomAction(COMMAND_CLEAR_QUEUE, null)
    }

    private fun checkPlaybackState(): Boolean = handler.postDelayed({
        val currentPosition = _playbackState.value?.currentPlayBackPosition
        if (_mediaPosition.value != currentPosition) {
            _mediaPosition.postValue(currentPosition)
        }
        if (updatePosition) {
            checkPlaybackState()
        }
    }, 100)

    override fun onCleared() {
        super.onCleared()

        serviceConnection.playbackState.removeObserver(playbackStateObserver)
        serviceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
        serviceConnection.repeatMode.removeObserver(repeatModeObserver)

        updatePosition = false
    }

    private suspend fun updateState(mediaMetadata: MediaMetadataCompat) {
        if (mediaMetadata.duration != 0L) {
            val bitmap = getCover(getApplication(), mediaMetadata.artUri)
            val dominantColor = Palette.from(bitmap).generate().getDominantColor(Color.WHITE)

            val title = mediaMetadata.title
            val artist = mediaMetadata.artist
            val currentSong = CurrentSong(title, Utilities.getArtistsFromSong(title, artist), mediaMetadata.album, mediaMetadata.duration, bitmap, dominantColor)
            _mediaMetadata.postValue(currentSong)
        }
    }

    class Factory(private val application: Application, private val serviceConnection: ServiceConnection) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                PlayingViewModel(application, serviceConnection) as T
    }
}

private suspend fun getCover(context: Context, artUri: Uri) = withContext(Dispatchers.IO) {
    try {
        getBitmap(context, artUri)
    } catch (_: Exception) {
        Utilities.getSongBitmap(context)
    }
}

private fun getBitmap(context: Context, uri: Uri) = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))

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

inline val MediaMetadataCompat.artist: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

inline val MediaMetadataCompat.album: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)

private const val TAG = "AmpApp.PlayingViewModel"