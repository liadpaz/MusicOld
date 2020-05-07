package com.liadpaz.music.fragments;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
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
import com.liadpaz.music.notification.MusicNotification;
import com.liadpaz.music.service.MusicPlayerService;
import com.liadpaz.music.utils.LocalFiles;
import com.liadpaz.music.utils.Song;
import com.liadpaz.music.utils.Utilities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongsListFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "SONGS_LIST_FRAGMENT";

    private static MusicPlayerService musicPlayerService;
    private boolean isLooping = false;

    private MusicNotification musicNotification;

    private ListView lvSongs;
    private FragmentSongsListBinding binding;
    // service connection to the music player service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (musicPlayerService == null) {
                musicPlayerService = ((MusicPlayerService.MusicPlayerBinder)service).getService();
                startNotification();
            }
            if (musicPlayerService.getSource() != null) {
                if (musicPlayerService.isPlaying()) {
                    binding.btnPlay.setBackground(getContext().getDrawable(R.drawable.pause));
                }
                if (musicPlayerService.isLooping()) {
                    binding.btnLoop.setBackground(getContext().getDrawable(R.drawable.repeat_one));
                }
                new Thread(() -> {
                    Bitmap cover = musicPlayerService.getCurrentSong().getCover();
                    if (cover != null) {
                        getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageBitmap(cover));
                    } else {
                        getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageResource(R.drawable.ic_audiotrack_black_24dp));
                    }
                }).start();
            }

            musicPlayerService.setListeners(() -> {
                if (!musicPlayerService.hasQueue()) {
                    musicPlayerService.startQueue(getSongs(), 0);
                    binding.ivCurrentTrack.setImageBitmap(musicPlayerService.getCurrentSong().getCover());
                }
                binding.btnPlay.setBackground(getContext().getDrawable(R.drawable.pause));
            }, nextSong -> new Thread(() -> {
                Bitmap cover = nextSong.getCover();
                if (cover != null) {
                    getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageBitmap(cover));
                } else {
                    getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageResource(R.drawable.ic_audiotrack_black_24dp));
                }
            }).start(), () -> binding.btnPlay.setBackground(getContext().getDrawable(R.drawable.play)));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerService = null;
        }
    };

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
        binding.btnPrev.setOnClickListener(v -> musicPlayerService.playPrev());
        binding.btnPlay.setOnClickListener(v -> musicPlayerService.startPause());
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

        lvSongs.setOnItemClickListener((parent, view1, position, id) -> musicPlayerService.startQueue(getSongs(), position));

        musicNotification = new MusicNotification(getContext(), getContext().getSystemService(NotificationManager.class));
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

    private void startNotification() {
        musicNotification.sendNotification();
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDestroy() {
        getContext().unbindService(serviceConnection);
        super.onDestroy();
    }

    static class LoadSongsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<SongsListFragment> songsListFragmentWeakReference;
        private WeakReference<SongListAdapter> adapterWeakReference;

        private CopyOnWriteArrayList<Song> songs;

        LoadSongsTask(SongsListFragment songsListFragment, SongListAdapter adapter) {
            super();
            this.songsListFragmentWeakReference = new WeakReference<>(songsListFragment);
            this.adapterWeakReference = new WeakReference<>(adapter);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final String songUntitled = songsListFragmentWeakReference.get().getString(R.string.song_no_name);

            ArrayList<Thread> threads = new ArrayList<>();

            songs = new CopyOnWriteArrayList<>();

            for (File file : Utilities.listFiles(LocalFiles.getPath())) {
                Thread thread = new Thread(() -> {
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

                    songs.add(new Song(songName, artists, file.getPath()));

                    metadataRetriever.release();
                });
                thread.start();
                threads.add(thread);
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapterWeakReference.get().setSource(songs);
        }
    }
}
