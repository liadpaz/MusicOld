package com.liadpaz.amp

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.liadpaz.amp.service.MediaPlayerService
import com.liadpaz.amp.service.ServiceConnector
import com.liadpaz.amp.utils.LocalFiles.init
import java.util.concurrent.TimeUnit

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        } else {
            Handler().postDelayed({ initializeView() }, TimeUnit.SECONDS.toMillis(1))
        }
        volumeControlStream = AudioManager.STREAM_MUSIC
        ServiceConnector.getInstance(applicationContext, ComponentName(applicationContext, MediaPlayerService::class.java))
        init(this)
    }

    private fun initializeView() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeView()
        }
    }

    companion object {
        private const val TAG = "AmpApp.LoadingActivity"
        private const val REQUEST_PERMISSION = 459
    }
}