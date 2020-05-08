package com.liadpaz.music.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

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
            }
            if (musicPlayerService.getSource() != null) {
                if (musicPlayerService.isPlaying()) {
                    binding.btnPlay.setBackground(getContext().getDrawable(R.drawable.pause));
                }
                if (musicPlayerService.isLooping()) {
                    binding.btnLoop.setBackground(getContext().getDrawable(R.drawable.repeat_one));
                }
                new Thread(() -> {
                    Bitmap cover = musicPlayerService.getSource()
                                                     .getCover();
                    if (cover != null) {
                        getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageBitmap(cover));
                    } else {
                        getActivity().runOnUiThread(() -> binding.ivCurrentTrack.setImageResource(R.drawable.ic_audiotrack_black_24dp));
                    }
                }).start();
            }

            musicPlayerService.setListeners(song -> {
                musicNotification.sendNotification(song, true);
                binding.btnPlay.setBackgroundResource(R.drawable.pause);
            }, nextSong -> {
                Handler handler = new Handler();
                new Thread(() -> {
                    Bitmap cover = nextSong.getCover();
                    musicNotification.sendNotification(nextSong, true);
//                    try {
                        if (cover != null) {
                            handler.post(() -> {
                                binding.ivCurrentTrack.setImageBitmap(cover);
                                setTheme(nextSong);
                            });
                        } else {
                            handler.post(() -> binding.ivCurrentTrack.setImageResource(R.drawable.ic_audiotrack_black_24dp));
                        }
//                    } catch (Exception ignored) {
//                    }
                }).start();
            }, song -> {
                binding.btnPlay.setBackgroundResource(R.drawable.play);
                musicNotification.sendNotification(song, false);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicNotification.stopNotification();
            musicPlayerService = null;
        }
    };

    public SongsListFragment() { }

    @SuppressWarnings("unused")
    @NonNull
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
        binding.btnPrev.setOnClickListener(v -> {
            if (!musicPlayerService.hasQueue()) {
                musicPlayerService.startQueue(getSongs(), 0);
            } else {
                musicPlayerService.playPrev();
            }
        });
        binding.btnPlay.setOnClickListener(v -> {
            if (!musicPlayerService.hasQueue()) {
                musicPlayerService.startQueue(getSongs(), 0);
            } else {
                musicPlayerService.startPause();
            }
        });
        binding.btnNext.setOnClickListener(v -> {
            if (!musicPlayerService.hasQueue()) {
                musicPlayerService.startQueue(getSongs(), 0);
            } else {
                musicPlayerService.playNext();
            }
        });
        binding.btnLoop.setOnClickListener(v -> {
            binding.btnLoop.setBackground(getContext().getDrawable(isLooping ? R.drawable.repeat : R.drawable.repeat_one));
            isLooping = !isLooping;
            musicPlayerService.setLooping(isLooping);
        });

        getContext().bindService(new Intent(getContext(), MusicPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        new LoadSongsTask(this, (SongListAdapter)lvSongs.getAdapter()).execute();

        lvSongs.setOnItemClickListener((parent, view1, position, id) -> musicPlayerService.startQueue(getSongs(), position));

        musicNotification = new MusicNotification(getContext());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater()
                     .inflate(R.menu.menu_song, menu);
    }

    private Song[] getSongs() {
        return ((SongListAdapter)lvSongs.getAdapter()).getSongs();
    }

    @SuppressWarnings("ConstantConditions")
    private void setTheme(Song song) {
        try {
            Palette.from(song.getCover())
                   .generate(palette -> {
                       int mainColor = palette.getDominantColor(0);
                       int secColor = palette.getVibrantColor(0);
                       binding.constraintLayoutSongInfo.setBackgroundColor(mainColor);
                       binding.btnPrev.setTextColor(secColor);
                       Log.d(TAG, "getTheme: set theme: " + mainColor);
                   });
        } catch (Exception ignored) {
            binding.constraintLayoutSongInfo.setBackgroundColor(getContext().getColor(R.color.colorPrimary));
        }
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
            final String songUntitled = songsListFragmentWeakReference.get()
                                                                      .getString(R.string.song_no_name);

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
                        Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)")
                                                 .matcher(artistsJoin);
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
            adapterWeakReference.get()
                                .setSource(songs);
        }
    }
}
