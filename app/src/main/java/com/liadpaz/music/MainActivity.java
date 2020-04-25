package com.liadpaz.music;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.liadpaz.music.adapters.ViewPagerAdapter;
import com.liadpaz.music.databinding.ActivityMainBinding;
import com.liadpaz.music.fragments.BlankFragment;
import com.liadpaz.music.fragments.SongsListFragment;
import com.liadpaz.music.utils.LocalFiles;
import com.liadpaz.music.utils.Utilities;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 459;
    private static final int REQUEST_PICK_FOLDER = 44;
    @SuppressWarnings("unused")
    private static final String TAG = "MAIN_ACTIVITY";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBarMain);

        new LocalFiles(getSharedPreferences("Music.Data", 0));

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            setViewPager();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemSelectFolder: {
                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_PICK_FOLDER);
                break;
            }

            case R.id.menuItemSettings: {
                //TODO: add settings activity
                break;
            }

            case R.id.menuItemAbout: {
                //TODO: add about activity
                break;
            }

            default: {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            setViewPager();
        }
    }

    private void setViewPager() {
        ViewPager2 viewPager = binding.viewPagerMain;

        viewPager.setAdapter(new ViewPagerAdapter(this, new ArrayList<Class>() {{
            add(SongsListFragment.class);
            add(BlankFragment.class);
        }}));

        ArrayList<String> tabsTitle = new ArrayList<String>() {{
            add(getString(R.string.tab_songs));
            add("Blank");
        }};

        new TabLayoutMediator(binding.tabLayoutMain, viewPager, ((tab, position) -> tab.setText(tabsTitle.get(position)))).attach();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_PICK_FOLDER) {
            String path = Utilities.getPathFromUri(DocumentsContract.buildDocumentUriUsingTree(data.getData(), DocumentsContract.getTreeDocumentId(data.getData())));
            if (!LocalFiles.getPath().equals(path)) {
                LocalFiles.setPath(path);
                recreate();
            }
        }
    }

    @Override
    public void recreate() {
        overridePendingTransition(0,0);
        finish();
        overridePendingTransition(0,0);
        startActivity(getIntent());
    }
}
