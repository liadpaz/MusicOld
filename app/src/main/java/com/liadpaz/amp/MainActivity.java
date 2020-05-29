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
import com.liadpaz.amp.fragments.MainFragment;
import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.service.MediaPlayerService;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.PlaylistsUtil;
import com.liadpaz.amp.utils.Utilities;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 459;
    private static final int REQUEST_PICK_FOLDER = 44;

    @SuppressWarnings("unused")
    private static final String TAG = "MAIN_ACTIVITY";

    private static MediaController controller;
    public ActivityMainBinding binding;
    private MediaBrowser mediaBrowser;

    public static MediaController getController() {return controller;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaBrowser = new MediaBrowser(this, new ComponentName(this, MediaPlayerService.class), new MediaBrowser.ConnectionCallback() {
            @Override
            public void onConnected() {
                controller = new MediaController(MainActivity.this, mediaBrowser.getSessionToken());
                initView();
            }
        }, null);

        new MediaNotification(this);

        LocalFiles.init(this, this);

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            startService();
        }
    }

    private void startService() {
        if (!mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }
        startForegroundService(new Intent(this, MediaPlayerService.class));
    }

    private void initView() {
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, MainFragment.newInstance()).replace(R.id.extendedFragment, ExtendedFragment.newInstance()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemSelectFolder: {
                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), REQUEST_PICK_FOLDER);
                return true;
            }

            case R.id.menuItemSettings: {
                startActivity(new Intent(this, SettingsActivity.class));
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
            startService();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_PICK_FOLDER) {
            String path = Utilities.getPathFromUri(data.getData());
            if (!LocalFiles.getPath().equals(path)) {
                LocalFiles.setPath(path);
                recreate();
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

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
        LocalFiles.setPlaylists(PlaylistsUtil.playlists.getValue());
    }


}
