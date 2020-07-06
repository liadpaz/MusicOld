package com.liadpaz.amp.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore.Audio
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.livedatautils.SongsUtil.setSongs
import com.liadpaz.amp.ui.viewmodels.Playlist
import com.liadpaz.amp.ui.viewmodels.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

object LocalFiles {
    private const val TAG = "AmpApp.LocalFiles"

    private val PROJECTION = arrayOf(Audio.Media.TITLE, Audio.Media._ID, Audio.Media.ARTIST, Audio.Media.ALBUM, Audio.Media.ALBUM_ID)
    private const val SORT_DEFAULT = Audio.Media.TITLE + " COLLATE NOCASE"

    private val ART_URI = Uri.parse("content://media/external/audio/albumart")

    private lateinit var musicSharedPreferences: SharedPreferences
    private lateinit var playlistsSharedPreferences: SharedPreferences

    private val isFirstTimeQueue = AtomicBoolean(true)
    private val isFirstTimePosition = AtomicBoolean(true)

    @Suppress("DeferredResultUnused")
    @JvmStatic
    fun init(context: Context) {
        musicSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        playlistsSharedPreferences = context.applicationContext.getSharedPreferences("Music.Playlists", 0)

        val contentResolver = context.contentResolver
        CoroutineScope(Dispatchers.IO).async {
            PlaylistsUtil.setPlaylists(getPlaylists(contentResolver))
            setSongs(listSongs(contentResolver, SORT_DEFAULT))
        }

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

    private suspend fun loadQueue(contentResolver: ContentResolver) {
        val songsIds = Gson().fromJson<List<Long>>(musicSharedPreferences.getString(Constants.PREFERENCES_QUEUE, "[]"), object : TypeToken<java.util.ArrayList<String>>() {}.type)
        val songs: List<Song> = listSongs(contentResolver, SORT_DEFAULT, songsIds)
        songs.sortedBy { song: Song -> songsIds.indexOf(song.mediaId) }.also {
            QueueUtil.queue.postValue(ArrayList(it))
            QueueUtil.queuePosition.postValue(musicSharedPreferences.getInt(Constants.PREFERENCES_QUEUE_POSITION, -1))
        }
    }

    @JvmStatic
    val path: String
        get() = musicSharedPreferences.getString(Constants.PREFERENCES_PATH, Constants.DEFAULT_PATH)!!

    @JvmStatic
    val showCurrent: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_SHOW_CURRENT, true)

    @JvmStatic
    val screenOn: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_SCREEN_ON, false)

    @JvmStatic
    val stopOnTask: Boolean
        get() = musicSharedPreferences.getBoolean(Constants.PREFERENCES_STOP_ON_TASK, false)

    @JvmStatic
    fun listSongs(contentResolver: ContentResolver?, sortBy: String? = null, songsIds: List<Long>? = null): ArrayList<Song> {
        val sort = sortBy ?: SORT_DEFAULT
        contentResolver?.query(Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, "_data like ? ", arrayOf("%$path%"), sort).use { musicCursor ->
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

    private fun getPlaylists(contentResolver: ContentResolver): Queue<Playlist> {
        val start = System.currentTimeMillis()
        val playlists: Queue<Playlist> = ArrayDeque()
        playlistsSharedPreferences.all.forEach { entry: Map.Entry<String?, Any?> ->
            val songsIdsList = Gson().fromJson<ArrayList<Long>>(entry.value.toString(), object : TypeToken<ArrayList<Long>>() {}.type)
            listSongs(contentResolver, SORT_DEFAULT, songsIdsList).sortedBy { song -> songsIdsList.indexOf(song.mediaId) }.also {
                playlists.add(Playlist(entry.key!!, ArrayList(it)))
            }
        }
        Log.d(TAG, "getPlaylists: " + (System.currentTimeMillis() - start))
        return playlists
    }

    fun setPlaylists(playlists: Queue<Playlist>) {
        val editor = playlistsSharedPreferences.edit().clear()
        for ((name, songs) in playlists) {
            editor.putString(name, Gson().toJson(songs.map { song: Song -> song.mediaId }))
        }
        editor.apply()
    }

    fun listSongsByLastAdded(contentResolver: ContentResolver): java.util.ArrayList<Song> {
        return listSongs(contentResolver, Audio.Media.DATE_ADDED + " DESC")
    }
}