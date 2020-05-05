package com.liadpaz.music.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.music.R;
import com.liadpaz.music.adapters.SongListAdapter;
import com.liadpaz.music.databinding.FragmentSongsListBinding;
import com.liadpaz.music.service.MusicPlayerService;
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

    @SuppressWarnings("unused")
    private static final String TAG = "SONGS_LIST_FRAGMENT";

    private boolean isLooping = false;

    private ListView lvSongs;

    private MusicPlayerService musicPlayerService;
    // service connection to the music player service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicPlayerService = ((MusicPlayerService.MusicPlayerBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerService = null;
        }
    };
    private FragmentSongsListBinding binding;

    public SongsListFragment() { }

    @SuppressWarnings("unused")
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
        (lvSongs = binding.lvSongs).setAdapter(new SongListAdapter(getActivity()));
        binding.btnPrev.setOnClickListener(v -> musicPlayerService.resetSong());
        binding.btnPlay.setOnClickListener(v -> {
            if (musicPlayerService.isPlaying()) {
                musicPlayerService.pause();
                v.setBackground(getContext().getDrawable(R.drawable.play));
            } else {
                musicPlayerService.start();
                v.setBackground(getContext().getDrawable(R.drawable.pause));
            }
        });
        binding.btnNext.setOnClickListener(v -> musicPlayerService.playNext());
        binding.btnLoop.setOnClickListener(v -> {
            if (isLooping) {
                binding.btnLoop.setBackground(getContext().getDrawable(R.drawable.repeat));
            } else {
                binding.btnLoop.setBackground(getContext().getDrawable(R.drawable.repeat_one));
            }
            isLooping = !isLooping;
            musicPlayerService.setLooping(isLooping);
        });

        getContext().bindService(new Intent(getContext(), MusicPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        new LoadSongsTask(this, (SongListAdapter)lvSongs.getAdapter()).execute();

        lvSongs.setOnItemClickListener((parent, view1, position, id) -> {
            Song song = ((Song)lvSongs.getAdapter().getItem(position));
            musicPlayerService.startQueue(getSongs(), position);
            musicPlayerService.start();
            musicPlayerService.setOnNextSongListener(nextSong -> {
                if (nextSong.getCover() == null) {
                    binding.ivCurrentTrack.setImageDrawable(getContext().getDrawable(R.drawable.ic_audiotrack_black_24dp));
                } else {
                    new Thread(() -> {
                        byte[] data = nextSong.getCover();
                        Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                        getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageBitmap(cover));
                    }).start();
                }
            });
            binding.btnPlay.setBackground(getContext().getDrawable(R.drawable.pause));
            if (song.getCover() == null) {
                binding.ivCurrentTrack.setImageDrawable(getContext().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            } else {
                new Thread(() -> {
                    byte[] data = song.getCover();
                    Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                    getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageBitmap(cover));
                }).start();
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_song, menu);
    }

    private Song[] getSongs() {
        return ((SongListAdapter)lvSongs.getAdapter()).getSongs();
    }

//    private void startNotification() {
//        Notification.Builder builder = new Notification.Builder(getContext(), "1")
//                .setCont
//    }

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
                        Song song = new Song(songName, artists, data, file.getPath());
                        songsListFragmentWeakReference.get().getActivity().runOnUiThread(() -> adapterWeakReference.get().addSong(song));
                    }
                    metadataRetriever.release();
                }, null).run();
            }
            return null;
        }
    }
}
