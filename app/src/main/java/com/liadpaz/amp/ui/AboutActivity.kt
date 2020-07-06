package com.liadpaz.amp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.liadpaz.amp.BuildConfig
import com.liadpaz.amp.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding: ActivityAboutBinding
        setContentView(ActivityAboutBinding.inflate(layoutInflater).also { binding = it }.root)
        setSupportActionBar(binding.toolbarAbout)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.tvVersion.text = BuildConfig.VERSION_NAME
    }

    companion object {
        private const val TAG = "AmpApp.AboutActivity"
    }
}