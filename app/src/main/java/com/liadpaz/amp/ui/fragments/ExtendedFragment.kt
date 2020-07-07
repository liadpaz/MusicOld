package com.liadpaz.amp.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentExtendedBinding
import com.liadpaz.amp.livedatautils.ColorUtil
import com.liadpaz.amp.server.service.ServiceConnector
import com.liadpaz.amp.ui.MainActivity
import com.liadpaz.amp.utils.LocalFiles
import com.liadpaz.amp.utils.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ExtendedFragment : Fragment() {

    private var handler: Handler? = null
    private var runnable: Runnable? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var shouldSeek = false
    private var isPlaying = false
    private var isRepeating = false
    private lateinit var transportControls: MediaControllerCompat.TransportControls

    private var duration: Long = 0
    private var currentPosition = 0.0

    @ColorInt
    private var defaultColor = 0
    private var isUp = false

    private lateinit var binding: FragmentExtendedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        defaultColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        handler = Handler()
        ServiceConnector.getInstance().apply {
            playbackState.observe(viewLifecycleOwner, Observer { state -> setPlayback(state) })
            nowPlaying.observe(viewLifecycleOwner, Observer { metadata -> setMetadata(metadata) })
            repeatMode.observe(viewLifecycleOwner, Observer { repeatMode -> isRepeating = repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE })
            this@ExtendedFragment.transportControls = transportControls
        }
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
                                requireActivity().window.statusBarColor = defaultColor
                                binding.infoFragment.alpha = 1f
                                isUp = true
                                if (LocalFiles.screenOn) {
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
                                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
                                binding.infoFragment.alpha = 1f
                                isUp = false
                            }
                            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            requireActivity().window.let {
                                it.statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
                                it.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            }
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
                    requireActivity().window.statusBarColor = defaultColor
                    binding.infoFragment.alpha = 1f
                    isUp = true
                    if (LocalFiles.screenOn) {
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

    override fun onResume() {
        super.onResume()
        if (shouldSeek) {
            if (runnable != null) {
                handler?.post(runnable!!)
            }
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
        binding.btnPlayPause.setImageResource(if (isPlaying.also { shouldSeek = it }) {
            R.drawable.pause
        } else {
            R.drawable.play
        })
        updateSeekBar()
        binding.btnRepeat.setImageResource(if (isRepeating) R.drawable.repeat_one else R.drawable.repeat_all)
    }

    private fun setMetadata(metadata: MediaMetadataCompat?) {
        if (metadata != null && metadata.description != null) {
            val description = metadata.description
            binding.tvSongTitle.text = description.title
            binding.tvSongArtist.text = description.subtitle
            binding.tvTimeElapsed.text = Utilities.formatTime(0)
            binding.tvTotalTime.text = Utilities.formatTime(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).also { duration = it })
            Glide.with(requireContext()).asBitmap().placeholder(R.drawable.song).load(description.iconUri).into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) = Unit

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Palette.from(errorDrawable!!.toBitmap()).generate { palette: Palette? ->
                        defaultColor = palette!!.getDominantColor(Color.WHITE)
                        if (isUp) {
                            requireActivity().window.statusBarColor = defaultColor
                        }
                        binding.extendedFragment.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(defaultColor, Color.BLACK))
                        ColorUtil.setColor(defaultColor)
                    }
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource).generate { palette: Palette? ->
                        defaultColor = palette!!.getDominantColor(Color.WHITE)
                        if (isUp) {
                            requireActivity().window.statusBarColor = defaultColor
                        }
                        binding.extendedFragment.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(defaultColor, Color.BLACK))
                        ColorUtil.setColor(defaultColor)
                    }
                }
            })
        } else {
            binding.tvSongTitle.text = null
            binding.tvSongArtist.text = null
            binding.tvTimeElapsed.text = Utilities.formatTime(0)
            binding.tvTotalTime.text = Utilities.formatTime(0)
            binding.extendedFragment.setBackgroundColor(Color.parseColor("#555555"))
            requireActivity().window.statusBarColor = Color.parseColor("#101820").also { defaultColor = it }
            shouldSeek = false
        }
    }

    /**
     * This function updates the progress bar and the elapsed time text.
     */
    private fun updateSeekBar(): Boolean? =
            handler?.postDelayed(Runnable {
                binding.sbSongProgress.progress = (currentPosition / duration * 1000).toInt()
                binding.tvTimeElapsed.text = Utilities.formatTime(currentPosition.toLong())
                currentPosition += 500
                if (runnable != null) {
                    handler?.removeCallbacks(runnable!!)
                }
                if (isPlaying) {
                    updateSeekBar()
                }
            }.also { runnable = it }, 500)

    override fun onPause() {
        super.onPause()
        if (runnable != null) {
            handler?.removeCallbacks(runnable!!)
        }
    }

    companion object {
        private const val TAG = "AmpApp.ExtendedFragment"

        @JvmStatic
        fun newInstance(): ExtendedFragment {
            return ExtendedFragment()
        }
    }
}

private val glideOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(R.drawable.song)
//        .fallback(R.drawable.song)
//        .error(R.drawable.song)