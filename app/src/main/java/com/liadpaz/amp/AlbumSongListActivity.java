package com.liadpaz.amp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.ActivityAlbumSongListBinding;
import com.liadpaz.amp.fragments.ControllerFragment;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.Album;

public class AlbumSongListActivity extends AppCompatActivity {
    private SongsListAdapter adapter;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAlbumSongListBinding binding = ActivityAlbumSongListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarAlbum);

        Album album = getIntent().getParcelableExtra(Constants.ALBUM);

        binding.setAlbum(album);

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
        adapter.submitList(album.songs);

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);

        getSupportFragmentManager().beginTransaction().replace(R.id.controllerFragment, ControllerFragment.newInstance()).commit();
    }

    // TODO: add menu
}
