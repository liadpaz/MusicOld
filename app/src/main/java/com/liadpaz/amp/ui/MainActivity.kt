package com.liadpaz.amp.ui

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.ActivityMainBinding
import com.liadpaz.amp.livedatautils.SongsUtil.getSongs
import com.liadpaz.amp.ui.fragments.ExtendedFragment
import com.liadpaz.amp.ui.fragments.MainViewPagerFragment
import com.liadpaz.amp.ui.fragments.SearchFragment.Companion.newInstance
import com.liadpaz.amp.ui.viewmodels.Song
import com.liadpaz.amp.utils.Constants
import com.liadpaz.amp.utils.LocalFiles.showCurrent
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    
    lateinit var binding: ActivityMainBinding

    private var searchItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)
        setSupportActionBar(binding.toolBarMain)

        volumeControlStream = AudioManager.STREAM_MUSIC

        supportFragmentManager.beginTransaction().replace(R.id.mainFragment, MainViewPagerFragment.newInstance()).replace(R.id.extendedFragment, ExtendedFragment.newInstance()).commitNow()
        if (intent != null) {
            if (intent.hasExtra(Constants.PREFERENCES_SHOW_CURRENT) && showCurrent) {
                BottomSheetBehavior.from(binding.extendedFragment).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        (menu.findItem(R.id.menuSearch).also { searchItem = it }.actionView as SearchView).setSearchableInfo((getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(componentName))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.menuItemSettings -> {
                    startActivityForResult(Intent(applicationContext, SettingsActivity::class.java), REQUEST_SETTINGS)
                    true
                }
                R.id.menuItemAbout -> {
                    startActivity(Intent(applicationContext, AboutActivity::class.java))
                    true
                }
                else -> false
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
            if (requestCode == REQUEST_SETTINGS && resultCode == Activity.RESULT_OK) {
                recreate()
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(Constants.PREFERENCES_SHOW_CURRENT) && showCurrent) {
            BottomSheetBehavior.from(binding.extendedFragment).setState(BottomSheetBehavior.STATE_EXPANDED)
        } else {
            handleIntent(intent)
        }
    }

    override fun recreate() {
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    /**
     * This function handles the query intent.
     *
     * @param intent An [Intent] with `Intent.ACTION_SEARCH` as its action and a `SearchManager.QUERY` extra as the query parameter.
     */
    private fun handleIntent(intent: Intent?) {
        if (intent != null && Intent.ACTION_SEARCH == intent.action && intent.hasExtra(SearchManager.QUERY)) {
            query(intent.getStringExtra(SearchManager.QUERY)!!)
        }
    }

    /**
     * This function queries the list of songs corresponds to the query string.
     *
     * @param queryString The query string.
     */
    private fun query(queryString: String) {
        val finalQuery = queryString.toLowerCase(Locale.US)
        val queriedSongs: ArrayList<Song> = ArrayList(getSongs().filter { song -> song.isMatchingQuery(finalQuery) })
        supportFragmentManager.beginTransaction().replace(R.id.mainFragment, newInstance(queryString, queriedSongs)).addToBackStack(null).commitNow()
    }

    fun setBottomSheetHidden(isHidden: Boolean) {
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val bottomSheetBehavior = BottomSheetBehavior.from<View>(binding.extendedFragment)
        if (isHidden) {
            layoutParams.setMargins(0, 0, 0, 0)
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        } else {
            layoutParams.setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72f, resources.displayMetrics).toInt())
            bottomSheetBehavior.state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) BottomSheetBehavior.STATE_COLLAPSED else bottomSheetBehavior.state
            bottomSheetBehavior.setHideable(false)
        }
        binding.mainFragment.layoutParams = layoutParams
    }

    override fun onBackPressed(): Unit = with(BottomSheetBehavior.from<View>(binding.extendedFragment)) {
        when {
            state == BottomSheetBehavior.STATE_EXPANDED -> {
                state = BottomSheetBehavior.STATE_COLLAPSED
            }
            searchItem!!.isActionViewExpanded -> {
                searchItem!!.collapseActionView()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    companion object {
        private const val TAG = "AmpApp.MainActivity"
        private const val REQUEST_SETTINGS = 525
    }
}