package com.liadpaz.amp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.liadpaz.amp.databinding.SettingsActivityBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.Utilities;

public class SettingsActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_FOLDER = 44;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.liadpaz.amp.databinding.SettingsActivityBinding binding;
        setContentView((binding = SettingsActivityBinding.inflate(getLayoutInflater())).getRoot());
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commit();
        setSupportActionBar(binding.toolbarSettings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        Preference pathPreference;

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            String path = LocalFiles.getPath();

            pathPreference = findPreference("path");
            pathPreference.setSummary(TextUtils.isEmpty(path) ? getString(R.string.preference_all_path) : path);
            pathPreference.setOnPreferenceClickListener(preference -> {
                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), REQUEST_PICK_FOLDER);
                return true;
            });
            findPreference("default_path").setOnPreferenceClickListener(preference -> {
                LocalFiles.setPath(Constants.DEFAULT_PATH);
                pathPreference.setSummary(Constants.DEFAULT_PATH);
                requireActivity().setResult(RESULT_OK);
                return true;
            });
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if (requestCode == REQUEST_PICK_FOLDER && resultCode == RESULT_OK) {
                String path = Utilities.getPathFromUri(data.getData());
                if (!LocalFiles.getPath().equals(path)) {
                    LocalFiles.setPath(path);
                    pathPreference.setSummary(TextUtils.isEmpty(path) ? getString(R.string.preference_all_path) : path);
                    requireActivity().setResult(RESULT_OK);
                }
            }
        }
    }
}