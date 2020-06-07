package com.liadpaz.amp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.liadpaz.amp.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AmpApp.AboutActivity";

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding;
        setContentView((binding = ActivityAboutBinding.inflate(getLayoutInflater())).getRoot());
        setSupportActionBar(binding.toolbarAbout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.tvVersion.setText(BuildConfig.VERSION_NAME);
    }
}
