package com.liadpaz.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.liadpaz.music.adapters.ViewPagerAdapter;
import com.liadpaz.music.databinding.ActivityMainBinding;
import com.liadpaz.music.fragments.BlankFragment;
import com.liadpaz.music.fragments.SongsListFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 459;

    private ViewPager2 viewPager;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBarMain);

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            setViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            setViewPager();
        }
    }

    private void setViewPager() {
        viewPager = binding.viewPagerMain;

        viewPager.setAdapter(new ViewPagerAdapter(this, new ArrayList<Class>() {{
            add(SongsListFragment.class);
            add(BlankFragment.class);
        }}));

        ArrayList<String> tabsTitle = new ArrayList<String>() {{
            add("Songs");
            add("Blank");
        }};

        new TabLayoutMediator(binding.tabLayoutMain, viewPager, ((tab, position) -> tab.setText(tabsTitle.get(position)))).attach();
    }
}
