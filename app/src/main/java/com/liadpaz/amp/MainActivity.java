package com.liadpaz.amp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.liadpaz.amp.databinding.ActivityMainBinding;
import com.liadpaz.amp.fragments.ExtendedFragment;
import com.liadpaz.amp.fragments.MainFragment;
import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.service.MediaPlayerService;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.Utilities;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 459;
    private static final int REQUEST_PICK_FOLDER = 44;

    @SuppressWarnings("unused")
    private static final String TAG = "MAIN_ACTIVITY";

    private static MediaControllerCompat controller;
    private MediaBrowserCompat mediaBrowser;

    public ActivityMainBinding binding;

    public static MediaControllerCompat getController() {return controller;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class), new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    MediaControllerCompat controller = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                    MediaControllerCompat.setMediaController(MainActivity.this, MainActivity.controller = controller);

                    initView();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, null);

        new MediaNotification(this);

        new LocalFiles(getSharedPreferences("Music.Data", 0), getSharedPreferences("Music.Playlists", 0));

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
        ContextCompat.startForegroundService(this, new Intent(this, MediaPlayerService.class));
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
        if (binding.mainLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            binding.mainLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
