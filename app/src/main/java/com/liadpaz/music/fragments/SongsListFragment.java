package com.liadpaz.music.fragments;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.music.R;
import com.liadpaz.music.adapters.SongListAdapter;
import com.liadpaz.music.databinding.FragmentSongsListBinding;
import com.liadpaz.music.utils.LocalFiles;
import com.liadpaz.music.utils.Song;
import com.liadpaz.music.utils.Utilities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongsListFragment extends Fragment {

    private static final String TAG = "SONGS_LIST_FRAGMENT";

    private FragmentSongsListBinding binding;

    public SongsListFragment() { }

    public static SongsListFragment newInstance() {
        return new SongsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentSongsListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView lvSongs;
        (lvSongs = binding.lvSongs).setAdapter(new SongListAdapter(getActivity()));
        lvSongs.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        registerForContextMenu(lvSongs);

        new LoadSongsTask(this, (SongListAdapter)lvSongs.getAdapter()).execute();

        lvSongs.setOnItemClickListener((parent, view1, position, id) -> {
            String name = ((Song)lvSongs.getAdapter().getItem(position)).getSongName();
            Toast.makeText(getContext(), name, Toast.LENGTH_LONG).show();
            Log.d(TAG, "onViewCreated: " + name);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_song, menu);
    }

    static class LoadSongsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<SongsListFragment> songsListFragmentWeakReference;
        private WeakReference<SongListAdapter> adapterWeakReference;

        LoadSongsTask(SongsListFragment songsListFragment, SongListAdapter adapter) {
            super();
            this.songsListFragmentWeakReference = new WeakReference<>(songsListFragment);
            this.adapterWeakReference = new WeakReference<>(adapter);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Void doInBackground(Void... voids) {
            final String songUntitled = songsListFragmentWeakReference.get().getString(R.string.song_no_name);
            for (File file : Utilities.listFiles(LocalFiles.getPath())) {
                new FutureTask<>(() -> {
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(file.getPath());
                    String songName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    if (songName == null || songName.isEmpty()) {
                        songName = file.getName();
                    }
                    String artistsJoin = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    ArrayList<String> artists = new ArrayList<>();
                    if (artistsJoin != null && !artistsJoin.isEmpty()) {
                        Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artistsJoin);
                        while (matcher.find()) {
                            artists.add(matcher.group());
                        }
                    } else {
                        artists.add(songUntitled);
                    }

                    byte[] data = null;

                    try {
                        data = metadataRetriever.getEmbeddedPicture();
                    } catch (Exception ignored) {
                    } finally {
                        Song song = new Song(songName, artists, data);
                        songsListFragmentWeakReference.get().getActivity().runOnUiThread(() -> adapterWeakReference.get().addSong(song));
                    }
                    metadataRetriever.release();
                }, null).run();
            }
            return null;
        }
    }
}
