package com.liadpaz.amp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.ActivityAlbumSongListBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.Song;
import com.liadpaz.amp.utils.QueueUtil;

import java.util.ArrayList;

public class AlbumSongListActivity extends AppCompatActivity {

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private ActivityAlbumSongListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlbumSongListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarAlbum);

        String album = getIntent().getStringExtra(Constants.ALBUM);

        ArrayList<Song> songs = LocalFiles.getSongsByAlbum(album);
        SongsListAdapter adapter = new SongsListAdapter(this, (v, position) -> {

        });
        adapter.submitList(songs);

        binding.tvAlbumName.setText(songs.get(0).getAlbum());
        binding.tvAlbumArtist.setText(songs.get(0).getSongArtists().get(0));
        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);

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

    // TODO: add menu


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
