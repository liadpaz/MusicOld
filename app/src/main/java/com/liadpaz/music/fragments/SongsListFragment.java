package com.liadpaz.music.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.liadpaz.music.utils.Song;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass. Use the {@link SongsListFragment#newInstance} factory method
 * to create an instance of this fragment.
 */
public class SongsListFragment extends Fragment {

    private static final String TAG = "SONGSLIST_FRAGMENT";

    private ArrayList<Song> songs = new ArrayList<>();

    private FragmentSongsListBinding binding;

    public SongsListFragment() { }

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment SongsListFragment.
     */
    @SuppressWarnings("unused")
    private static SongsListFragment newInstance() {
        SongsListFragment fragment = new SongsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentSongsListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView lv_songs;
        (lv_songs = binding.lvSongs).setAdapter(new SongListAdapter(getActivity(), songs));
        registerForContextMenu(lv_songs);
        new LoadSongs(this, (SongListAdapter)lv_songs.getAdapter()).execute();
        //        lv_songs.setOnItemClickListener((parent, view1, position, id) -> {
        //            // TODO: play song
        //            Log.d(TAG, "onActivityCreated: ON ITEM CLICK");
        //        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_song, menu);
    }

    static class LoadSongs extends AsyncTask<Void, Void, Void> {

        private WeakReference<SongsListFragment> songsListFragmentWeakReference;
        private WeakReference<SongListAdapter> adapterWeakReference;

        LoadSongs(SongsListFragment songsListFragment, SongListAdapter adapter) {
            super();
            this.songsListFragmentWeakReference = new WeakReference<>(songsListFragment);
            this.adapterWeakReference = new WeakReference<>(adapter);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Void doInBackground(Void... voids) {
            File rootFolder = new File("/storage/self/primary/music");
            Log.d(TAG, "loadSongs: Before iterating files");
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

            for (File file : rootFolder.listFiles()) {
                try {
                    if (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") || file.getName().endsWith(".m4a") || file.getName().endsWith(".mwa") || file.getName().endsWith(".flac")) {
                        metadataRetriever.setDataSource(file.getPath());
                        String songName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artistsJoin = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                        ArrayList<String> artists = new ArrayList<>();

                        Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artistsJoin);
                        while (matcher.find()) {
                            artists.add(matcher.group());
                        }


                        Bitmap cover = null;
                        try {
                            byte[] data = metadataRetriever.getEmbeddedPicture();
                            cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                        } catch (Exception ignored) {
                            cover = BitmapFactory.decodeResource(songsListFragmentWeakReference.get().getResources(), R.drawable.ic_launcher_foreground);
                        } finally {
                            if (cover == null) {
                                cover = BitmapFactory.decodeResource(songsListFragmentWeakReference.get().getResources(), R.drawable.ic_launcher_foreground);
                            }

                            Song song = new Song(songName, artists, cover);
                            Log.d(TAG, "loadSongs: Add Song");
                            songsListFragmentWeakReference.get().getActivity().runOnUiThread(() -> {
                                adapterWeakReference.get().addSong(song);
                                adapterWeakReference.get().sort();
                            });
                        }
                    }
                } catch (Exception ignored) {
                    Log.d(TAG, "failed");
                }
            }
            metadataRetriever.release();

            songsListFragmentWeakReference.get().getActivity().runOnUiThread(() -> Toast.makeText(songsListFragmentWeakReference.get().getContext(), "Finished Loading Songs", Toast.LENGTH_LONG).show());

            return null;
        }
    }
}
