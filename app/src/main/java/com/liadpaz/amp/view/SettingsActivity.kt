package com.liadpaz.amp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.SettingsActivityBinding
import com.liadpaz.amp.utils.Constants
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.viewmodels.SettingsViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SettingsActivityBinding.inflate(layoutInflater).also { binding = it }.root)
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var path: String

        private val viewModel: SettingsViewModel by viewModels()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel.path.observe(viewLifecycleOwner) { path = it }
            path = viewModel.path.value!!

            findPreference<SwitchPreferenceCompat>("stop")?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                viewModel.stopOnTask.postValue(newValue as Boolean)
                true
            }
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
            findPreference<SwitchPreferenceCompat>("screen_on")?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                viewModel.screenOn.postValue(newValue as Boolean)
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == REQUEST_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
                val path = Utilities.getPathFromUri(data?.data!!)
                if (this.path != path) {
                    viewModel.path.postValue(path)
                    findPreference<Preference>("path")?.summary = if (TextUtils.isEmpty(path)) getString(R.string.preference_all_path) else path
                }
            }
        }
    }

    companion object {
        private const val REQUEST_PICK_FOLDER = 44

        private const val TAG = "AmpApp.SettingsActivity"
    }
}