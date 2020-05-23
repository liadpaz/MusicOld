package com.liadpaz.amp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.ActivityArtistSongListBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;

public class ArtistSongListActivity extends AppCompatActivity {
    private SongsListAdapter adapter;

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private ActivityArtistSongListBinding binding;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtistSongListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarArtist);

        String artist = getIntent().getStringExtra(Constants.ARTIST);

        adapter = new SongsListAdapter(this, (v, position) -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.inflate(R.menu.menu_song);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menuPlayNext: {
                        QueueUtil.addToNext(adapter.getCurrentList().get(position));
                        break;
                    }

                    case R.id.menuAddQueue: {
                        QueueUtil.addToEnd(adapter.getCurrentList().get(position));
                        break;
                    }

                    case R.id.menuQueueAddPlaylist: {
                        // TODO: add to playlist
                        break;
                    }
                }
                return true;
            });
            popupMenu.show();
        });
        adapter.submitList(LocalFiles.getSongsByArtist(artist));

        binding.tvArtistTitle.setText(artist);
        binding.rvArtistSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvArtistSongs.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.rvArtistSongs.setAdapter(adapter);

        (controller = MainActivity.getController()).registerCallback(callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) { setPlayback(state); }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) { setMetadata(metadata); }
        });

        binding.btnPlay.setOnClickListener(v -> {
            MediaControllerCompat controller = MainActivity.getController();
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                QueueUtil.queue.postValue(LocalFiles.listSongs(this));
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

        setPlayback(controller.getPlaybackState());
        setMetadata(controller.getMetadata());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callback != null) {
            controller.unregisterCallback(callback);
        }
    }

    private void setPlayback(PlaybackStateCompat state) {
        if (state != null) {
            binding.btnPlay.setBackgroundResource(state.getState() == PlaybackStateCompat.STATE_PLAYING ? R.drawable.pause : R.drawable.play);
        }
    }

    private void setMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            MediaDescriptionCompat description = metadata.getDescription();

            Glide.with(this).load(description.getIconUri()).placeholder(R.drawable.song).into(binding.ivCurrentTrack);
            binding.tvSongName.setText(description.getTitle());
            binding.tvSongArtist.setText(description.getSubtitle());
        }
    }
}
