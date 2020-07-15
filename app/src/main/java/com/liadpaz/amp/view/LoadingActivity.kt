package com.liadpaz.amp.view

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.liadpaz.amp.R
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection
import java.util.concurrent.TimeUnit

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        if (Build.VERSION.SDK_INT > 23 && PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        } else {
            Handler().postDelayed({ initializeView() }, TimeUnit.SECONDS.toMillis(1))
        }
        ServiceConnection.getInstance(applicationContext, ComponentName(applicationContext, MediaPlayerService::class.java))
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