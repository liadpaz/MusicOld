package com.liadpaz.amp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.SettingsActivityBinding
import com.liadpaz.amp.utils.Constants
import com.liadpaz.amp.utils.LocalFiles
import com.liadpaz.amp.utils.Utilities

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding: SettingsActivityBinding
        setContentView(SettingsActivityBinding.inflate(layoutInflater).also { binding = it }.root)
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var path: String

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            path = LocalFiles.path

            val pathPreference = findPreference<Preference>("path")?.apply {
                summary = if (TextUtils.isEmpty(path)) getString(R.string.preference_all_path) else path
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), REQUEST_PICK_FOLDER)
                    true
                }
            }!!
            findPreference<Preference>("default_path")?.apply {
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    pathPreference.summary = Constants.DEFAULT_PATH
                    requireActivity().setResult(if (path == Constants.DEFAULT_PATH) Activity.RESULT_CANCELED else Activity.RESULT_OK)
                    true
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == REQUEST_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
                val path = Utilities.getPathFromUri(data?.data!!)
                if (LocalFiles.path != path) {
                    findPreference<Preference>("path")?.summary = if (TextUtils.isEmpty(path)) getString(R.string.preference_all_path) else path
                    requireActivity().setResult(if (this@SettingsFragment.path == path) Activity.RESULT_CANCELED else Activity.RESULT_OK)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_PICK_FOLDER = 44
    }
}