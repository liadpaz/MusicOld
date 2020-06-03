package com.liadpaz.amp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.liadpaz.amp.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AmpApp.AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding;
        setContentView((binding = ActivityAboutBinding.inflate(getLayoutInflater())).getRoot());

        binding.tvVersion.setText(BuildConfig.VERSION_NAME);
    }
}
