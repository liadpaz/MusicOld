package com.liadpaz.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.liadpaz.music.databinding.ActivityMainBinding;
import com.liadpaz.music.fragments.ViewPagerFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 459;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else if (getSupportFragmentManager().findFragmentByTag("ViewPager") == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ViewPagerFragment(), "ViewPager").commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (getSupportFragmentManager().findFragmentByTag("ViewPager") == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ViewPagerFragment(), "ViewPager").commit();
            }
        }
    }
}
