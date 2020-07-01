package com.liadpaz.amp.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadata
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.liadpaz.amp.MainActivity
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentExtendedBinding
import com.liadpaz.amp.livedatautils.ColorUtil
import com.liadpaz.amp.service.ServiceConnector.Companion.getInstance
import com.liadpaz.amp.utils.LocalFiles
import com.liadpaz.amp.utils.Utilities
import java.util.concurrent.CompletableFuture

class ExtendedFragment private constructor() : Fragment() {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var shouldSeek = false
    private var isPlaying = false
    private var isRepeating = false
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private var duration: Long = 0
    private var currentPosition = 0.0

    @ColorInt
    private var defaultColor = 0
    private var isUp = false
    private var binding: FragmentExtendedBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        defaultColor = requireContext().getColor(R.color.colorPrimaryDark)
        handler = Handler()
        val serviceConnector = getInstance()
        serviceConnector!!.playbackState.observe(viewLifecycleOwner, Observer { state: PlaybackStateCompat -> setPlayback(state) })
        serviceConnector.nowPlaying.observe(viewLifecycleOwner, Observer { metadata: MediaMetadataCompat? -> setMetadata(metadata) })
        serviceConnector.repeatMode.observe(viewLifecycleOwner, Observer { repeatMode: Int -> isRepeating = repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE })
        transportControls = serviceConnector.transportControls
        binding!!.tvSongTitle.isSelected = true
        binding!!.tvSongArtist.isSelected = true
        binding!!.btnSkipPrev.setOnClickListener { transportControls!!.skipToPrevious() }
        binding!!.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                transportControls!!.pause()
            } else {
                transportControls!!.play()
            }
        }
        binding!!.btnSkipNext.setOnClickListener { transportControls!!.skipToNext() }
        binding!!.btnRepeat.setOnClickListener { transportControls!!.setRepeatMode(if (isRepeating) PlaybackStateCompat.REPEAT_MODE_ALL else PlaybackStateCompat.REPEAT_MODE_ONE) }
        binding!!.sbSongProgress.max = 1000
        binding!!.sbSongProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                transportControls!!.seekTo((seekBar.progress.toDouble() * duration / 1000).toLong())
            }
        })
        binding!!.tvTimeElapsed.text = Utilities.formatTime(0)
        binding!!.tvTotalTime.text = Utilities.formatTime(0)
        BottomSheetBehavior.from((requireActivity() as MainActivity).binding.extendedFragment).addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (!isUp) {
                        // show the info fragment
                        childFragmentManager.beginTransaction()
                                .replace(R.id.infoFragment, ExtendedInfoFragment.newInstance())
                                .commitNowAllowingStateLoss()
                        requireActivity().window.statusBarColor = defaultColor
                        binding!!.infoFragment.alpha = 1f
                        isUp = true
                        if (LocalFiles.screenOn) {
                            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (isUp) {
                        // show the controller fragment
                        childFragmentManager.beginTransaction()
                                .replace(R.id.infoFragment, ControllerFragment.newInstance())
                                .replace(R.id.layoutFragment, ExtendedViewPagerFragment.newInstance())
                                .commitNow()
                        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.colorPrimaryDark)
                        binding!!.infoFragment.alpha = 1f
                        isUp = false
                    }
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    requireActivity().window.statusBarColor = requireActivity().getColor(R.color.colorPrimaryDark)
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    isUp = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (isUp) {
                    binding!!.infoFragment.alpha = slideOffset
                } else {
                    binding!!.infoFragment.alpha = 1 - slideOffset
                }
            }
        })
        childFragmentManager.beginTransaction()
                .replace(R.id.infoFragment, ControllerFragment.newInstance())
                .replace(R.id.layoutFragment, ExtendedViewPagerFragment.newInstance())
                .commit()
    }

    override fun onResume() {
        super.onResume()
        if (shouldSeek) {
            handler!!.post(runnable!!)
        }
    }

    private fun setPlayback(state: PlaybackStateCompat) {
        isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        if (state.state == PlaybackStateCompat.STATE_STOPPED || state.state == PlaybackStateCompat.STATE_NONE) {
            (requireActivity() as MainActivity).setBottomSheetHidden(true)
            return
        }
        (requireActivity() as MainActivity).setBottomSheetHidden(false)
        currentPosition = state.position.toDouble()
        shouldSeek = if (isPlaying) {
            binding!!.btnPlayPause.setImageResource(R.drawable.pause)
            updateSeekBar()
            true
        } else {
            binding!!.btnPlayPause.setImageResource(R.drawable.play)
            false
        }
        binding!!.btnRepeat.setImageResource(if (isRepeating) R.drawable.repeat_one else R.drawable.repeat_all)
    }

    private fun setMetadata(metadata: MediaMetadataCompat?) {
        if (metadata != null) {
            val description = metadata.description
            if (description != null) {
                binding!!.tvSongTitle.text = description.title
                binding!!.tvSongArtist.text = description.subtitle
                binding!!.tvTimeElapsed.text = Utilities.formatTime(0)
                binding!!.tvTotalTime.text = Utilities.formatTime(metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).also { duration = it })
                CompletableFuture.supplyAsync<Bitmap> {
                    try {
                        return@supplyAsync BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(description.iconUri!!))
                    } catch (e: Exception) {
                        return@supplyAsync null
                    }
                }.thenAccept { bitmap: Bitmap? ->
                    if (bitmap != null) {
                        Palette.from(bitmap).generate { palette: Palette? ->
                            defaultColor = palette!!.getDominantColor(Color.WHITE)
                            if (isUp) {
                                requireActivity().window.statusBarColor = defaultColor
                            }
                            binding!!.extendedFragment.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(defaultColor, Color.BLACK))
                            ColorUtil.setColor(defaultColor)
                        }
                    } else {
                        defaultColor = ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.3f)
                        if (isUp) {
                            requireActivity().window.statusBarColor = defaultColor
                        }
                        binding!!.extendedFragment.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(defaultColor, Color.BLACK))
                        ColorUtil.setColor(defaultColor)
                    }
                }
            }
        } else {
            binding!!.tvSongTitle.text = null
            binding!!.tvSongArtist.text = null
            binding!!.tvTimeElapsed.text = Utilities.formatTime(0)
            binding!!.tvTotalTime.text = Utilities.formatTime(0)
            binding!!.extendedFragment.setBackgroundColor(Color.parseColor("#555555"))
            requireActivity().window.statusBarColor = Color.parseColor("#101820").also { defaultColor = it }
            shouldSeek = false
        }
    }

    /**
     * This function updates the progress bar and the elapsed time text.
     */
    private fun updateSeekBar() {
        handler!!.postDelayed(Runnable {
            binding!!.sbSongProgress.progress = (currentPosition / duration * 1000).toInt()
            binding!!.tvTimeElapsed.text = Utilities.formatTime(currentPosition.toLong())
            currentPosition += 500.0
            handler!!.removeCallbacks(runnable!!)
            if (isPlaying) {
                updateSeekBar()
            }
        }.also { runnable = it }, 500)
    }

    override fun onPause() {
        super.onPause()
        handler!!.removeCallbacks(runnable!!)
    }

    companion object {
        private const val TAG = "AmpApp.ExtendedFragment"
        @JvmStatic
        fun newInstance(): ExtendedFragment {
            return ExtendedFragment()
        }
    }
}