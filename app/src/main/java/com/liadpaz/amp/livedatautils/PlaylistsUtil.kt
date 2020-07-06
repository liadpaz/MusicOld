package com.liadpaz.amp.livedatautils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.liadpaz.amp.ui.viewmodels.Playlist
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object PlaylistsUtil {
    private const val TAG = "PlaylistsUtil"
    private val playlists = MutableLiveData<ArrayDeque<Playlist>>()
    private val isChanging = AtomicBoolean(false)

    fun isPlaylistExists(name: String): Boolean {
        for ((name1) in playlists.value!!) {
            if (name1 == name) {
                return true
            }
        }
        return false
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<Queue<Playlist>>) {
        playlists.observe(lifecycleOwner, observer)
    }

    fun getPlaylists(): Deque<Playlist> {
        return playlists.value!!
    }

    @JvmStatic
    fun setPlaylists(playlists: Queue<Playlist>) {
        PlaylistsUtil.playlists.postValue(ArrayDeque(playlists))
    }

    fun addPlaylist(playlist: Playlist) {
        val playlists = getPlaylists()
        playlists.addFirst(playlist)
        setPlaylists(playlists)
    }

    fun removePlaylist(name: String): Playlist? {
        getPlaylists().also {
            for (playlist in it) {
                if (playlist.name == name) {
                    it.remove(playlist)
                    setPlaylists(it)
                    return playlist
                }
            }
            return null
        }
    }

    fun getPlaylistByName(name: String): Playlist? {
        for (playlist in getPlaylists()) {
            if (name == playlist.name) {
                return playlist
            }
        }
        return null
    }

    val playlistsNames: List<String>
        get() = getPlaylists().map { (name) -> name }

    fun getIsChanging(): Boolean {
        return isChanging.get()
    }

    fun setIsChanging(isChanging: Boolean) {
        PlaylistsUtil.isChanging.set(isChanging)
    }
}