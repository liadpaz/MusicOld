package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.liadpaz.amp.model.repositories.MainRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val path = MainRepository.getInstance(application).getPath()
    val screenOn = MainRepository.getInstance(application).getScreenOn()
    val stopOnTask = MainRepository.getInstance(application).getStopOnTask()
}