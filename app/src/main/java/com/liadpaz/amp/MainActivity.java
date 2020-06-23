package com.liadpaz.amp;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.liadpaz.amp.databinding.ActivityMainBinding;
import com.liadpaz.amp.fragments.ExtendedFragment;
import com.liadpaz.amp.fragments.MainViewPagerFragment;
import com.liadpaz.amp.fragments.SearchFragment;
import com.liadpaz.amp.livedatautils.SongsUtil;
import com.liadpaz.amp.service.MediaPlayerService;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AmpApp.MainActivity";

    private static final int REQUEST_SETTINGS = 525;

    private static MediaController controller;
    public ActivityMainBinding binding;
    private MediaBrowser mediaBrowser;

    public static MediaController getController() {return controller;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView((binding = ActivityMainBinding.inflate(getLayoutInflater())).getRoot());
        setSupportActionBar(binding.toolBarMain);

        mediaBrowser = new MediaBrowser(this, new ComponentName(this, MediaPlayerService.class), new MediaBrowser.ConnectionCallback() {
            @Override
            public void onConnected() {
                controller = new MediaController(MainActivity.this, mediaBrowser.getSessionToken());
                initializeView();
            }
        }, null);

        startService();
    }

    private void startService() {
        startService(new Intent(this, MediaPlayerService.class));
        if (!mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void initializeView() {
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, MainViewPagerFragment.newInstance()).replace(R.id.extendedFragment, ExtendedFragment.newInstance()).commitNowAllowingStateLoss();
        if (getIntent() != null) {
            if (getIntent().hasExtra(Constants.PREFERENCES_SHOW_CURRENT) && LocalFiles.getShowCurrent()) {
                BottomSheetBehavior.from(binding.extendedFragment).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
        if (!((PowerManager)getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())) {
            new AlertDialog.Builder(this).setTitle(R.string.battery_optimization_title).setMessage(R.string.battery_optimization_message).setPositiveButton(R.string.battery_optimization_optimize, (dialog, which) -> startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))).show();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ((SearchView)menu.findItem(R.id.menuSearch).getActionView()).setSearchableInfo(((SearchManager)getSystemService(SEARCH_SERVICE)).getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemSettings: {
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
                return true;
            }

            case R.id.menuItemAbout: {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            recreate();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            if (intent.hasExtra(Constants.PREFERENCES_SHOW_CURRENT) && LocalFiles.getShowCurrent()) {
                BottomSheetBehavior.from(binding.extendedFragment).setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                handleIntent(intent);
            }
        }
    }

    @Override
    public void recreate() {
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
    }

    /**
     * This function handles the query intent.
     *
     * @param intent An {@link Intent} with {@code Intent.ACTION_SEARCH} as its action and a {@code
     *               SearchManager.QUERY} extra as the query parameter.
     */
    @SuppressWarnings("ConstantConditions")
    private void handleIntent(@Nullable Intent intent) {
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction()) && intent.hasExtra(SearchManager.QUERY)) {
            query(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    /**
     * This function queries the list of songs corresponds to the query string.
     *
     * @param queryString The query string.
     */
    private void query(@NonNull String queryString) {
        String loweredQueryString = queryString.toLowerCase();
        List<Song> queriedSongs = SongsUtil.getSongs().parallelStream().filter(song -> song.songTitle.toLowerCase().contains(loweredQueryString) || song.songArtists.stream().anyMatch(artist -> artist.toLowerCase().contains(loweredQueryString)) || song.album.toLowerCase().contains(loweredQueryString)).collect(Collectors.toCollection(ArrayList::new));
        if (queriedSongs.size() == 0) {
            queriedSongs.add(null);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, SearchFragment.newInstance(queryString, queriedSongs)).addToBackStack(null).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(binding.extendedFragment);
        if (bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
    }
}
