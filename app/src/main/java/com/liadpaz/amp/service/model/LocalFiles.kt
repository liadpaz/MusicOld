package com.liadpaz.amp.service.model

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore.Audio
import androidx.annotation.GuardedBy
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liadpaz.amp.utils.Constants
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.data.Playlist
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

class LocalFiles private constructor(private val context: Context) {

    private var musicSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var playlistsSharedPreferences: SharedPreferences = context.getSharedPreferences("Music.Playlists", 0)

    private val isFirstTimeQueue = AtomicBoolean(true)
    private val isFirstTimePosition = AtomicBoolean(true)

    init {
        QueueUtil.queue.observeForever { songs ->
            if (!isFirstTimeQueue.getAndSet(false)) {
                val songsIdsList = songs.map { song -> song.mediaId }
                musicSharedPreferences.edit().putString(Constants.PREFERENCES_QUEUE, Gson().toJson(songsIdsList)).apply()
            }
        }
        QueueUtil.queuePosition.observeForever { queuePosition ->
            if (!isFirstTimePosition.getAndSet(false)) {
                musicSharedPreferences.edit().putInt(Constants.PREFERENCES_QUEUE_POSITION, queuePosition).apply()
            }
        }

        //        loadQueue(context.getContentResolver());
        // TODO: fix queue saving
    }

    private fun loadQueue() {
        val songsIds = Gson().fromJson<List<Long>>(musicSharedPreferences.getString(Constants.PREFERENCES_QUEUE, "[]"), object : TypeToken<java.util.ArrayList<String>>() {}.type)
        val songs: List<Song> = listSongs(SORT_DEFAULT, songsIds)
        songs.sortedBy { song: Song -> songsIds.indexOf(song.mediaId) }.also {
            QueueUtil.queue.postValue(ArrayList(it))
            QueueUtil.queuePosition.postValue(musicSharedPreferences.getInt(Constants.PREFERENCES_QUEUE_POSITION, -1))
        }
    }

    fun observePreferences(callback: (SharedPreferences, String) -> Unit) {
        musicSharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String -> callback(sharedPreferences, key) }
    }

    var path: String
        get() = musicSharedPreferences.getString(Constants.PREFERENCES_PATH, Constants.DEFAULT_PATH)!!
        set(value) {
            musicSharedPreferences.edit().putString(Constants.PREFERENCES_PATH, value).apply()
        }

    val showCurrent: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_SHOW_CURRENT, true)

    val screenOn: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_SCREEN_ON, false)

    val stopOnTask: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_STOP_ON_TASK, false)

    fun listSongs(sortBy: String = SORT_DEFAULT, songsIds: List<Long>? = null): ArrayList<Song> {
        context.contentResolver.query(Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, "_data like ? ", arrayOf("%$path%"), sortBy).use { musicCursor ->
            val songs = arrayListOf<Song>()
            //iterate over results if valid
            if (musicCursor != null && musicCursor.moveToFirst()) {
                //get columns
                val titleColumn = musicCursor.getColumnIndex(Audio.Media.TITLE)
                val idColumn = musicCursor.getColumnIndex(Audio.Media._ID)
                val artistColumn = musicCursor.getColumnIndex(Audio.Media.ARTIST)
                val albumColumn = musicCursor.getColumnIndex(Audio.Media.ALBUM)
                val albumIdColumn = musicCursor.getColumnIndex(Audio.Media.ALBUM_ID)
                //add songs to list
                do {
                    val id = musicCursor.getLong(idColumn)
                    if (songsIds == null || songsIds.contains(id)) {
                        val title = musicCursor.getString(titleColumn)
                        songs.add(Song(id, title, Utilities.getArtistsFromSong(title, musicCursor.getString(artistColumn)), musicCursor.getString(albumColumn), ContentUris.withAppendedId(ART_URI, musicCursor.getString(albumIdColumn).toLong())))
                    }
                } while (musicCursor.moveToNext())
            }
            return songs
        }
    }

    fun getPlaylists(): Queue<Playlist> {
        val playlists: Queue<Playlist> = ArrayDeque()
        playlistsSharedPreferences.all.forEach { entry: Map.Entry<String?, Any?> ->
            val songsIdsList = Gson().fromJson<ArrayList<Long>>(entry.value.toString(), object : TypeToken<ArrayList<Long>>() {}.type)
            listSongs(SORT_DEFAULT, songsIdsList).sortedBy { song -> songsIdsList.indexOf(song.mediaId) }.also {
                playlists.add(Playlist(entry.key!!, ArrayList(it)))
            }
        }
        return playlists
    }

    fun setPlaylists(playlists: Queue<Playlist>) {
        val editor = playlistsSharedPreferences.edit().clear()
        for ((name, songs) in playlists) {
            editor.putString(name, Gson().toJson(songs.map { song: Song -> song.mediaId }))
        }
        editor.apply()
    }

    fun listSongsByLastAdded(): ArrayList<Song> = listSongs(Audio.Media.DATE_ADDED + " DESC")

    companion object {
        private const val TAG = "AmpApp.LocalFiles"

        private val PROJECTION = arrayOf(Audio.Media.TITLE, Audio.Media._ID, Audio.Media.ARTIST, Audio.Media.ALBUM, Audio.Media.ALBUM_ID)
        private const val SORT_DEFAULT = Audio.Media.TITLE + " COLLATE NOCASE"

        private val ART_URI = Uri.parse("content://media/external/audio/albumart")

        private var instance: LocalFiles? = null

        fun getInstance(context: Context): LocalFiles {
            if (instance == null) {
                instance = LocalFiles(context)
            }
            return instance!!
        }
    }
}
