package com.liadpaz.amp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayoutMediator;
import com.liadpaz.amp.adapters.ViewPagerAdapter;
import com.liadpaz.amp.databinding.ActivityMainBinding;
import com.liadpaz.amp.fragments.AlbumsFragment;
import com.liadpaz.amp.fragments.ArtistsFragment;
import com.liadpaz.amp.fragments.PlaylistsFragment;
import com.liadpaz.amp.fragments.SongsListFragment;
import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.service.MediaPlayerService;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 459;
    private static final int REQUEST_PICK_FOLDER = 44;

    @SuppressWarnings("unused")
    private static final String TAG = "MAIN_ACTIVITY";

    private static MediaControllerCompat controller;

    private ActivityMainBinding binding;

    private MediaBrowserCompat mediaBrowser;


    public static MediaControllerCompat getController() {return controller;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBarMain);

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class), new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    MediaControllerCompat controller = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                    MediaControllerCompat.setMediaController(MainActivity.this, MainActivity.controller = controller);

                    controller.registerCallback(new MediaControllerCompat.Callback() {
                        @Override
                        public void onPlaybackStateChanged(PlaybackStateCompat state) {
                            setPlayback(state);
                        }

                        @Override
                        public void onMetadataChanged(MediaMetadataCompat metadata) {
                            setMetadata(metadata);
                        }
                    });

                    setPlayback(controller.getPlaybackState());
                    setMetadata(controller.getMetadata());

                    setView();
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
        ContextCompat.startForegroundService(this ,new Intent(this, MediaPlayerService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
    private void setView() {
        ViewPager2 viewPager = binding.viewPagerMain;

        viewPager.setAdapter(new ViewPagerAdapter(this, new ArrayList<Class>() {{
            add(SongsListFragment.class);
            add(PlaylistsFragment.class);
            add(ArtistsFragment.class);
            add(AlbumsFragment.class);
        }}));

        ArrayList<String> tabsTitle = new ArrayList<String>() {{
            add(getString(R.string.tab_songs));
            add(getString(R.string.tab_playlists));
            add(getString(R.string.tab_artists));
            add(getString(R.string.tab_albums));
        }};

        new TabLayoutMediator(binding.tabLayoutMain, viewPager, ((tab, position) -> tab.setText(tabsTitle.get(position)))).attach();

        binding.btnPlay.setOnClickListener(v -> {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            if (QueueUtil.queue.getValue().size() == 0) {
                QueueUtil.queue.setValue(LocalFiles.listSongs(this));
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.ACTION_QUEUE_POSITION, 0);
                controller.sendCommand(Constants.ACTION_QUEUE_POSITION, bundle, null);
            } else if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else {
                controller.getTransportControls().play();
            }
        });
        binding.constraintLayoutSongInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, ExtendedSongActivity.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        });
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
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
    }

    private void setPlayback(PlaybackStateCompat state) {
        if (state != null) {
            binding.btnPlay.setBackgroundResource(state.getState() == PlaybackStateCompat.STATE_PLAYING ? R.drawable.pause : R.drawable.play);
        }
    }

    private void setMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            try {
                MediaDescriptionCompat description = metadata.getDescription();

                Glide.with(this).load(description.getIconUri()).placeholder(R.drawable.song).into(binding.ivCurrentTrack);
                binding.tvSongName.setText(description.getTitle());
                binding.tvSongArtist.setText(description.getSubtitle());
            } catch (Exception ignored) {
            }
        }
    }
}
