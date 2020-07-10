package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.liadpaz.amp.service.repository.Repository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val path = Repository.getInstance(application).getPath()
    val screenOn = Repository.getInstance(application).getScreenOn()
}