package com.liadpaz.music.fragments;

import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.liadpaz.music.R;
import com.liadpaz.music.adapters.SongListAdapter;
import com.liadpaz.music.databinding.FragmentSongsListBinding;
import com.liadpaz.music.utils.Song;

import java.io.File;
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
        loadSongs();
        lv_songs.setOnItemClickListener((parent, view1, position, id) -> {
            // TODO: play song
            Log.d(TAG, "onActivityCreated: ON ITEM CLICK");
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void loadSongs() {
        //        ArrayList<String> fileList = new ArrayList<>();


        File rootFolder = new File("/storage/self/primary/music");
        Log.d(TAG, "loadSongs: Before iterating files");
        for (File file : rootFolder.listFiles()) {
            try {
                if (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") || file.getName().endsWith(".m4a") || file.getName().endsWith(".mwa") || file.getName().endsWith(".flac")) {
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(file.getPath());
                    String songName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String artistsJoin = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                    ArrayList<String> artists = new ArrayList<>();

                    Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artistsJoin);
                    while (matcher.find()) {
                        artists.add(matcher.group());
                    }

                    Drawable cover = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_launcher_foreground, null);

                    Song song = new Song(songName, artists, cover);
                    Log.d(TAG, "loadSongs: Add Song");
                    songs.add(song);
                    songs.sort((o1, o2) -> o1.getSongName().compareTo(o2.getSongName()));

                    metadataRetriever.release();
                }
            } catch (Exception ignored) {
                Log.d(TAG, "failed");
            }
        }
    }
}
