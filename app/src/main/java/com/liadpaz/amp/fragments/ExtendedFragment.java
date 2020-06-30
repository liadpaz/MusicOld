package com.liadpaz.amp.fragments;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentExtendedBinding;
import com.liadpaz.amp.livedatautils.ColorUtil;
import com.liadpaz.amp.service.ServiceConnector;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.Utilities;

import java.util.concurrent.CompletableFuture;

public class ExtendedFragment extends Fragment {
    private static final String TAG = "AmpApp.ExtendedFragment";

    private Handler handler;
    private Runnable runnable;
    private boolean shouldSeek = false;

    private boolean isPlaying = false;
    private boolean isRepeating = false;
    private MediaControllerCompat.TransportControls transportControls;

    private long duration = 0;
    private double currentPosition = 0;
    @ColorInt
    private int defaultColor;
    private boolean isUp = false;

    private FragmentExtendedBinding binding;

    private ExtendedFragment() { }

    @NonNull
    public static ExtendedFragment newInstance() { return new ExtendedFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentExtendedBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        defaultColor = requireContext().getColor(R.color.colorPrimaryDark);

        handler = new Handler();

        ServiceConnector serviceConnector = ServiceConnector.getInstance();
        serviceConnector.playbackState.observe(getViewLifecycleOwner(), this::setPlayback);
        serviceConnector.nowPlaying.observe(getViewLifecycleOwner(), this::setMetadata);
        serviceConnector.repeatMode.observe(getViewLifecycleOwner(), repeatMode -> isRepeating = repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE);
        transportControls = serviceConnector.transportControls;

        binding.tvSongTitle.setSelected(true);
        binding.tvSongArtist.setSelected(true);

        binding.btnSkipPrev.setOnClickListener(v -> transportControls.skipToPrevious());
        binding.btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                transportControls.pause();
            } else {
                transportControls.play();
            }
        });
        binding.btnSkipNext.setOnClickListener(v -> transportControls.skipToNext());
        binding.btnRepeat.setOnClickListener(v -> transportControls.setRepeatMode(isRepeating ? PlaybackStateCompat.REPEAT_MODE_ALL : PlaybackStateCompat.REPEAT_MODE_ONE));

        binding.sbSongProgress.setMax(1000);
        binding.sbSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                transportControls.seekTo((long)((double)seekBar.getProgress() * duration / 1000));
            }
        });
        binding.tvTimeElapsed.setText(Utilities.formatTime(0));
        binding.tvTotalTime.setText(Utilities.formatTime(0));

        BottomSheetBehavior.from(((MainActivity)requireActivity()).binding.extendedFragment).addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (!isUp) {
                        // show the info fragment
                        getChildFragmentManager().beginTransaction()
                                                 .replace(R.id.infoFragment, ExtendedInfoFragment.newInstance())
                                                 .commitNowAllowingStateLoss();
                        requireActivity().getWindow().setStatusBarColor(defaultColor);
                        binding.infoFragment.setAlpha(1);
                        isUp = true;
                        if (LocalFiles.getScreenOn()) {
                            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (isUp) {
                        // show the controller fragment
                        getChildFragmentManager().beginTransaction()
                                                 .replace(R.id.infoFragment, ControllerFragment.newInstance())
                                                 .replace(R.id.layoutFragment, ExtendedViewPagerFragment.newInstance())
                                                 .commitNow();
                        requireActivity().getWindow().setStatusBarColor(requireActivity().getColor(R.color.colorPrimaryDark));
                        binding.infoFragment.setAlpha(1);
                        isUp = false;
                    }
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    requireActivity().getWindow().setStatusBarColor(requireActivity().getColor(R.color.colorPrimaryDark));
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    isUp = true;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (isUp) {
                    binding.infoFragment.setAlpha(slideOffset);
                } else {
                    binding.infoFragment.setAlpha(1 - slideOffset);
                }
            }
        });

        getChildFragmentManager().beginTransaction()
                                 .replace(R.id.infoFragment, ControllerFragment.newInstance())
                                 .replace(R.id.layoutFragment, ExtendedViewPagerFragment.newInstance())
                                 .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldSeek) {
            handler.post(runnable);
        }
    }

    private void setPlayback(@NonNull PlaybackStateCompat state) {
        isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        if (state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
            ((MainActivity)requireActivity()).setBottomSheetHidden(true);
            return;
        }
        ((MainActivity)requireActivity()).setBottomSheetHidden(false);
        currentPosition = state.getPosition();
        if (isPlaying) {
            binding.btnPlayPause.setImageResource(R.drawable.pause);
            updateSeekBar();
            shouldSeek = true;
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.play);
            shouldSeek = false;
        }
        binding.btnRepeat.setImageResource(isRepeating ? R.drawable.repeat_one : R.drawable.repeat_all);
    }

    @SuppressWarnings("ConstantConditions")
    private void setMetadata(@Nullable MediaMetadataCompat metadata) {
        if (metadata != null) {
            MediaDescriptionCompat description = metadata.getDescription();
            if (description != null) {
                binding.tvSongTitle.setText(description.getTitle());
                binding.tvSongArtist.setText(description.getSubtitle());
                binding.tvTimeElapsed.setText(Utilities.formatTime(0));
                binding.tvTotalTime.setText(Utilities.formatTime(duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)));
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(description.getIconUri()));
                    } catch (Exception e) {
                        return null;
                    }
                }).thenAccept(bitmap -> {
                    if (bitmap != null) {
                        Palette.from(bitmap).generate(palette -> {
                            defaultColor = palette.getDominantColor(Color.WHITE);
                            if (isUp) {
                                requireActivity().getWindow().setStatusBarColor(defaultColor);
                            }
                            binding.extendedFragment.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{defaultColor, Color.BLACK}));
                            ColorUtil.setColor(defaultColor);
                        });
                    } else {
                        defaultColor = ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.3F);
                        if (isUp) {
                            requireActivity().getWindow().setStatusBarColor(defaultColor);
                        }
                        binding.extendedFragment.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{defaultColor, Color.BLACK}));
                        ColorUtil.setColor(defaultColor);
                    }
                });
            }
        } else {
            binding.tvSongTitle.setText(null);
            binding.tvSongArtist.setText(null);
            binding.tvTimeElapsed.setText(Utilities.formatTime(0));
            binding.tvTotalTime.setText(Utilities.formatTime(0));
            binding.extendedFragment.setBackgroundColor(Color.parseColor("#555555"));
            requireActivity().getWindow().setStatusBarColor(defaultColor = Color.parseColor("#101820"));
            shouldSeek = false;
        }
    }

    /**
     * This function updates the progress bar and the elapsed time text.
     */
    private void updateSeekBar() {
        handler.postDelayed(runnable = () -> {
            binding.sbSongProgress.setProgress((int)((currentPosition / duration) * 1000));
            binding.tvTimeElapsed.setText(Utilities.formatTime((long)currentPosition));
            currentPosition += 500;
            handler.removeCallbacks(runnable);
            if (isPlaying) {
                updateSeekBar();
            }
        }, 500);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
