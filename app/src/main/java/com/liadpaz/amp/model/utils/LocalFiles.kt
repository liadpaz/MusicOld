package com.liadpaz.amp.model.utils

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore.Audio
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liadpaz.amp.utils.Constants
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.data.Song
import java.util.concurrent.atomic.AtomicBoolean

class LocalFiles private constructor(private val context: Context) {

    private var musicSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var playlistsSharedPreferences: SharedPreferences = context.getSharedPreferences("Music.Playlists", 0)

    private val isFirstTimeQueue = AtomicBoolean(true)
    private val isFirstTimePosition = AtomicBoolean(true)

    fun setQueue(songs: List<Song>) = musicSharedPreferences.edit().putString(Constants.PREFERENCES_QUEUE, Gson().toJson(songs.map { song: Song -> song.mediaId })).apply()
    fun setPosition(position: Int) = musicSharedPreferences.edit().putInt(Constants.PREFERENCES_QUEUE_POSITION, position).apply()

    private fun loadQueue() =
            listFromIdList(Gson().fromJson<List<Long>>(musicSharedPreferences.getString(Constants.PREFERENCES_QUEUE, "[]"), object : TypeToken<java.util.ArrayList<String>>() {}.type)).also {
                // TODO: load queue from [it]
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

    fun listSongs(sortBy: String = SORT_DEFAULT, folder: String = path): ArrayList<Song> {
        context.contentResolver.query(Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, "_data like ? ", arrayOf("%$folder%"), sortBy).use { musicCursor ->
            return arrayListOf<Song>().apply {
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
                        val title = musicCursor.getString(titleColumn)
                        add(Song(musicCursor.getLong(idColumn), title, Utilities.getArtistsFromSong(title, musicCursor.getString(artistColumn)), musicCursor.getString(albumColumn), ContentUris.withAppendedId(ART_URI, musicCursor.getString(albumIdColumn).toLong())))
                    } while (musicCursor.moveToNext())
                }
            }
        }
    }

    private fun listFromIdList(songsIds: List<Long>): ArrayList<Song> {
        val allSongs = listSongs(folder = "")
        return arrayListOf<Song>().apply {
            for (id in songsIds) {
                allSongs.find { song -> song.mediaId == id }?.also { add(it) }
            }
        }
    }

    fun getPlaylists(): LinkedHashMap<String, ArrayList<Song>> =
            LinkedHashMap<String, ArrayList<Song>>().apply {
                playlistsSharedPreferences.all.forEach { entry: Map.Entry<String?, Any?> ->
                    put(entry.key!!, listFromIdList(Gson().fromJson<ArrayList<Long>>(entry.value.toString(), object : TypeToken<ArrayList<Long>>() {}.type)))
                }
            }

    fun addPlaylist(playlistName: String, songs: List<Song>) =
            playlistsSharedPreferences.edit().putString(playlistName, Gson().toJson(songs.map { song: Song -> song.mediaId })).apply()

    fun deletePlaylist(playlistName: String) =
            playlistsSharedPreferences.edit().remove(playlistName).apply()

    fun listSongsByLastAdded(): ArrayList<Song> = listSongs(Audio.Media.DATE_ADDED + " DESC")

    companion object {
        @Volatile
        private lateinit var instance: LocalFiles

        @JvmStatic
        fun getInstance(context: Context): LocalFiles {
            synchronized(LocalFiles::class.java) {
                if (!Companion::instance.isInitialized) {
                    instance = LocalFiles(context)
                }
            }
            return instance
        }
    }

}

private const val TAG = "AmpApp.LocalFiles"

private val PROJECTION = arrayOf(Audio.Media.TITLE, Audio.Media._ID, Audio.Media.ARTIST, Audio.Media.ALBUM, Audio.Media.ALBUM_ID)
private const val SORT_DEFAULT = Audio.Media.TITLE + " COLLATE NOCASE"

private val ART_URI = Uri.parse("content://media/external/audio/albumart")