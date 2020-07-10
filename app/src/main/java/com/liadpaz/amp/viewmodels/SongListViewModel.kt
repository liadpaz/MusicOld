package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.liadpaz.amp.service.repository.Repository
import com.liadpaz.amp.view.data.Song

class SongListViewModel(application: Application) : AndroidViewModel(application) {
    val songsObservable: LiveData<List<Song>> = Repository.getInstance(application).getSongs()
}