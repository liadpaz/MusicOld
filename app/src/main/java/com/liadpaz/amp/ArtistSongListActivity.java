package com.liadpaz.amp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.ActivityArtistSongListBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.Song;
import com.liadpaz.amp.utils.Utilities;

import java.util.ArrayList;

public class ArtistSongListActivity extends AppCompatActivity {

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private ActivityArtistSongListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtistSongListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String artist = getIntent().getStringExtra(Constants.ARTIST);

        ArrayList<Song> songs = LocalFiles.getSongsByArtist(artist);
        SongsListAdapter adapter = new SongsListAdapter(this, songs);

        binding.tvArtistTitle.setText(artist);
        binding.lvArtistSongs.setAdapter(adapter);

        binding.lvArtistSongs.setOnItemClickListener((parent, view1, position, id) -> {
            LocalFiles.setQueue(songs);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.ACTION_QUEUE_POSITION, position);
            MainActivity.getController().sendCommand(Constants.ACTION_SET_QUEUE, bundle, null);
        });

        (controller = MainActivity.getController()).registerCallback(callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) { setPlayback(state); }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) { setMetadata(metadata); }
        });

        binding.btnPlay.setOnClickListener(v -> {
            MediaControllerCompat controller = MainActivity.getController();
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                LocalFiles.setQueue(LocalFiles.listSongs(this));
                controller.sendCommand(Constants.ACTION_SET_QUEUE, null, null);
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

    @SuppressWarnings("ConstantConditions")
    private void setMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            MediaDescriptionCompat description = metadata.getDescription();
            Utilities.isUriExists(this, description.getIconUri()).thenApply(cover -> {
                if (cover != null) {
                    binding.ivCurrentTrack.setImageBitmap(cover);
                } else {
                    binding.ivCurrentTrack.setImageResource(R.drawable.song);
                }
                return null;
            });
            binding.tvSongName.setText(description.getTitle());
            binding.tvSongArtist.setText(description.getSubtitle());
        }
    }
}
