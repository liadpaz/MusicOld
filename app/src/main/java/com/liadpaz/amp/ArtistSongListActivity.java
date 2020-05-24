package com.liadpaz.amp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.ActivityArtistSongListBinding;
import com.liadpaz.amp.fragments.ControllerFragment;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.Artist;

public class ArtistSongListActivity extends AppCompatActivity {
    private static final String TAG = "ARTIST_ACTIVITY";
    private SongsListAdapter adapter;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityArtistSongListBinding binding = ActivityArtistSongListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarArtist);

        Artist artist = getIntent().getParcelableExtra(Constants.ARTIST);

        binding.setArtist(artist);

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
        adapter.submitList(artist.songs);

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);

        Log.d(TAG, "onCreate: ");

        getSupportFragmentManager().beginTransaction().replace(R.id.controllerFragment, ControllerFragment.newInstance()).commit();
    }

    // TODO: add menu
}
