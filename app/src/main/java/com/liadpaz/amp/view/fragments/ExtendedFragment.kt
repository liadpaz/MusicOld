package com.liadpaz.amp.view.fragments

import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentExtendedBinding
import com.liadpaz.amp.service.server.service.MediaPlayerService
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.MainActivity
import com.liadpaz.amp.view.data.CurrentSong
import com.liadpaz.amp.viewmodels.PlayingViewModel

class ExtendedFragment : Fragment() {

    private var screenOn: Boolean = false

    private var currentSong: CurrentSong? = null

    private val viewModel: PlayingViewModel by viewModels {
        val application = requireActivity().application
        PlayingViewModel.Factory(application, ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)))
    }

    private var duration: Long = 0L
    private var isPlaying = false
    private var isRepeating = false
    private lateinit var transportControls: MediaControllerCompat.TransportControls

    @ColorInt
    private var defaultColor = Color.WHITE

    @ColorInt
    private var lastColor = Color.WHITE
    private var isUp = false

    private lateinit var binding: FragmentExtendedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.mediaMetadata.observe(viewLifecycleOwner) {
            if (currentSong != it) {
                currentSong = it
                binding.tvSongTitle.text = it.title
                binding.tvSongArtist.text = it.artists
                binding.tvTotalTime.text = Utilities.formatTime(it.duration)
                duration = it.duration

                lastColor = defaultColor
                defaultColor = it.color
                val transitionDrawable = TransitionDrawable(arrayOf(GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(lastColor, Color.BLACK)), GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(defaultColor, Color.BLACK))))
                binding.extendedFragment.background = transitionDrawable
                transitionDrawable.startTransition(400)
            } else if (currentSong?.duration != it.duration) {
                binding.tvTotalTime.text = Utilities.formatTime(it.duration)
                duration = it.duration
            }
        }
        viewModel.playbackState.observe(viewLifecycleOwner) {
            isPlaying = it.state == PlaybackStateCompat.STATE_PLAYING
            if (it.state == PlaybackStateCompat.STATE_STOPPED || it.state == PlaybackStateCompat.STATE_NONE) {
                (requireActivity() as MainActivity).setBottomSheetHidden(true)
                return@observe
            }
            (requireActivity() as MainActivity).setBottomSheetHidden(false)
            binding.btnPlayPause.setImageResource(if (isPlaying) {
                R.drawable.pause
            } else {
                R.drawable.play
            })
            binding.btnRepeat.setImageResource(if (isRepeating) R.drawable.repeat_one else R.drawable.repeat_all)
        }
        viewModel.mediaPosition.observe(viewLifecycleOwner) {
            binding.sbSongProgress.progress = (it!! * 1000.0 / duration).toInt()
            binding.tvTimeElapsed.text = Utilities.formatTime(it)
        }
        viewModel.screenOnObserver.observe(viewLifecycleOwner) {
            screenOn = it
        }
        transportControls = viewModel.transportControls

        binding.tvSongTitle.isSelected = true
        binding.tvSongArtist.isSelected = true

        binding.btnSkipPrev.setOnClickListener { transportControls.skipToPrevious() }
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                transportControls.pause()
            } else {
                transportControls.play()
            }
        }
        binding.btnSkipNext.setOnClickListener { transportControls.skipToNext() }
        binding.btnRepeat.setOnClickListener { transportControls.setRepeatMode(if (isRepeating) PlaybackStateCompat.REPEAT_MODE_ALL else PlaybackStateCompat.REPEAT_MODE_ONE) }
        binding.sbSongProgress.max = 1000
        binding.sbSongProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = Unit

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) = transportControls.seekTo((seekBar.progress.toDouble() * duration / 1000).toLong())
        })
        binding.tvTimeElapsed.text = Utilities.formatTime(0)
        binding.tvTotalTime.text = Utilities.formatTime(0)

        BottomSheetBehavior.from((requireActivity() as MainActivity).binding.extendedFragment).apply {
            addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            if (!isUp) {
                                // show the info fragment
                                childFragmentManager.beginTransaction()
                                        .replace(R.id.infoFragment, ExtendedInfoFragment.newInstance())
                                        .commitNow()
                                binding.infoFragment.alpha = 1f
                                isUp = true
                                if (screenOn) {
                                    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                }
                            }
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            if (isUp) {
                                // show the controller fragment
                                childFragmentManager.beginTransaction()
                                        .replace(R.id.infoFragment, ControllerFragment.newInstance())
                                        .replace(R.id.layoutFragment, ExtendedViewPagerFragment.newInstance())
                                        .commitNow()
                                binding.infoFragment.alpha = 1f
                                isUp = false
                            }
                            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            isUp = true
                        }
                        else -> Unit
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.infoFragment.alpha = if (isUp) slideOffset else 1 - slideOffset
                }
            })

            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                if (!isUp) {
                    // show the info fragment
                    childFragmentManager.beginTransaction()
                            .replace(R.id.infoFragment, ExtendedInfoFragment.newInstance())
                            .commitNow()
                    binding.infoFragment.alpha = 1f
                    isUp = true
                    if (screenOn) {
                        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }
        childFragmentManager.beginTransaction().apply {
            if (!isUp) {
                replace(R.id.infoFragment, ControllerFragment.newInstance())
            }
            replace(R.id.layoutFragment, ExtendedViewPagerFragment.newInstance())
        }.commitNow()
    }

    companion object {
        private const val TAG = "AmpApp.ExtendedFragment"

        @JvmStatic
        fun newInstance(): ExtendedFragment =
                ExtendedFragment()
    }
}