package com.liadpaz.amp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.liadpaz.amp.databinding.ActivityMainBinding;
import com.liadpaz.amp.fragments.ExtendedFragment;
import com.liadpaz.amp.fragments.MainViewPagerFragment;
import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.service.MediaPlayerService;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AmpApp.MainActivity";

    private static final int REQUEST_PERMISSION = 459;
    private static final int REQUEST_SETTINGS = 525;

    private static MediaController controller;
    public ActivityMainBinding binding;
    private MediaBrowser mediaBrowser;

    private AtomicBoolean shouldInitializeView = new AtomicBoolean(false);

    public static MediaController getController() {return controller;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView((binding = ActivityMainBinding.inflate(getLayoutInflater())).getRoot());
        setSupportActionBar(binding.toolBarMain);

        mediaBrowser = new MediaBrowser(this, new ComponentName(this, MediaPlayerService.class), new MediaBrowser.ConnectionCallback() {
            @Override
            public void onConnected() {
                controller = new MediaController(MainActivity.this, mediaBrowser.getSessionToken());
                if (!shouldInitializeView.get()) {
                    shouldInitializeView.set(true);
                } else {
                    initializeView();
                }
            }
        }, null);

        new MediaNotification(this);

        LocalFiles.init(this, this);

        startService();

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            if (!shouldInitializeView.get()) {
                shouldInitializeView.set(true);
            } else {
                initializeView();
            }
        }
    }

    private void startService() {
        if (!mediaBrowser.isConnected()) {
            startService(new Intent(this, MediaPlayerService.class));
            mediaBrowser.connect();
        }
    }

    private void initializeView() {
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, MainViewPagerFragment.newInstance()).replace(R.id.extendedFragment, ExtendedFragment.newInstance()).commitNow();
        if (getIntent() != null) {
            if (getIntent().hasExtra(Constants.PREFERENCES_SHOW_CURRENT) && LocalFiles.getShowCurrent()) {
                BottomSheetBehavior.from(binding.extendedFragment).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemSettings: {
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
                return true;
            }

            case R.id.menuItemAbout: {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (shouldInitializeView.get()) {
                initializeView();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            recreate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            if (intent.hasExtra(Constants.PREFERENCES_SHOW_CURRENT) && LocalFiles.getShowCurrent()) {
                BottomSheetBehavior.from(binding.extendedFragment).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void recreate() {
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
    }

    @Override
    public void onBackPressed() {
        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(binding.extendedFragment);
        if (bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
    }
}
