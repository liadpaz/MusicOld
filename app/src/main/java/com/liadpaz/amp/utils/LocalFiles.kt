package com.liadpaz.amp.utils

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.provider.MediaStore.Audio
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.livedatautils.SongsUtil.setSongs
import com.liadpaz.amp.viewmodels.Playlist
import com.liadpaz.amp.viewmodels.Song
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors
import kotlin.collections.ArrayList

object LocalFiles {
    private const val TAG = "AmpApp.LocalFiles"

    private val PROJECTION = arrayOf(Audio.Media.TITLE, Audio.Media._ID, Audio.Media.ARTIST, Audio.Media.ALBUM, Audio.Media.ALBUM_ID)
    private const val SORT_DEFAULT = Audio.Media.TITLE + " COLLATE NOCASE"

    private lateinit var musicSharedPreferences: SharedPreferences
    private lateinit var playlistsSharedPreferences: SharedPreferences

    private val isFirstTimeQueue = AtomicBoolean(true)
    private val isFirstTimePosition = AtomicBoolean(true)

    @JvmStatic
    fun init(context: Context) {
        musicSharedPreferences = context.getSharedPreferences("Music.Data", 0)
        playlistsSharedPreferences = context.getSharedPreferences("Music.Playlists", 0)

        val contentResolver = context.contentResolver
        CompletableFuture.runAsync { PlaylistsUtil.setPlaylists(getPlaylists(contentResolver)) }
        CompletableFuture.runAsync { setSongs(listSongs(contentResolver, SORT_DEFAULT)) }

        QueueUtil.queue.observeForever { songs ->
            if (!isFirstTimeQueue.getAndSet(false)) {
                val songsIdsList = songs.map { song -> song.id }
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

    private fun loadQueue(contentResolver: ContentResolver) {
        val songsIds = Gson().fromJson<List<Long>>(musicSharedPreferences.getString(Constants.PREFERENCES_QUEUE, "[]"), object : TypeToken<java.util.ArrayList<Long?>?>() {}.type)
        val songs: List<Song> = listSongs(contentResolver, SORT_DEFAULT, songsIds)
        songs.sortedBy { song: Song -> songsIds.indexOf(song.id) }.also {
            QueueUtil.queue.postValue(ArrayList(it))
            QueueUtil.queuePosition.postValue(musicSharedPreferences.getInt(Constants.PREFERENCES_QUEUE_POSITION, -1))
        }
    }

    @JvmStatic
    var path: String
        get() = musicSharedPreferences.getString(Constants.PREFERENCES_PATH, Constants.DEFAULT_PATH)!!
        set(path) = musicSharedPreferences.edit().putString(Constants.PREFERENCES_PATH, path).apply()

    @JvmStatic
    var showCurrent: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_SHOW_CURRENT, true)
        set(showCurrent) {
            musicSharedPreferences.edit().putBoolean(Constants.PREFERENCES_SHOW_CURRENT, showCurrent).apply()
        }

    @JvmStatic
    var screenOn: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_SCREEN_ON, false)
        set(screenOn) {
            musicSharedPreferences.edit().putBoolean(Constants.PREFERENCES_SCREEN_ON, screenOn).apply()
        }

    private fun listSongs(contentResolver: ContentResolver, sort: String, songsIds: List<Long>? = null): java.util.ArrayList<Song> {
        val start = System.currentTimeMillis()
        val songs = java.util.ArrayList<Song>()
        contentResolver.query(Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, Audio.Media.DATA + " like ? ", arrayOf("%$path%"), sort).use { musicCursor ->
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
                        songs.add(Song(id, musicCursor.getString(titleColumn), musicCursor.getString(artistColumn), musicCursor.getString(albumColumn), musicCursor.getString(albumIdColumn)))
                    }
                } while (musicCursor.moveToNext())
            }
        }
        Log.d(TAG, "listSongs: " + (System.currentTimeMillis() - start))
        return songs
    }

    private fun getPlaylists(contentResolver: ContentResolver): Queue<Playlist> {
        val start = System.currentTimeMillis()
        val playlists: Queue<Playlist> = ArrayDeque()
        playlistsSharedPreferences.all.forEach { entry: Map.Entry<String?, Any?> ->
            val songsIdsList = Gson().fromJson<ArrayList<Long>>(entry.value.toString(), object : TypeToken<java.util.ArrayList<Long?>?>() {}.type)
            listSongs(contentResolver, SORT_DEFAULT, songsIdsList).sortedBy { song -> songsIdsList.indexOf(song.id) }.also {
                playlists.add(Playlist(entry.key!!, ArrayList(it)))
            }
        }
        Log.d(TAG, "getPlaylists: " + (System.currentTimeMillis() - start))
        return playlists
    }

    fun setPlaylists(playlists: Queue<Playlist>) {
        val editor = playlistsSharedPreferences.edit().clear()
        for ((name, songs) in playlists) {
            editor.putString(name, Gson().toJson(songs.map { song: Song -> song.id }))
        }
        editor.apply()
    }

    fun listSongsByLastAdded(contentResolver: ContentResolver): java.util.ArrayList<Song> {
        return listSongs(contentResolver, Audio.Media.DATE_ADDED + " DESC")
    }
}