package com.liadpaz.amp.fragments;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.liadpaz.amp.notification.OnColorChange;
import com.liadpaz.amp.utils.ColorUtil;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;

import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

public class ExtendedFragment extends Fragment {
    private static OnColorChange onColorChangeController;
    private static OnColorChange onColorChangeInfo;
    private Handler handler;
    private Runnable runnable;
    private boolean shouldSeek = false;
    private MediaController controller;
    private MediaController.Callback callback;
    private long duration = 0;
    private double currentPosition = 0;
    @ColorInt
    private int defaultColor;
    private boolean isUp = false;

    private FragmentExtendedBinding binding;

    public ExtendedFragment() { }

    @NonNull
    public static ExtendedFragment newInstance() { return new ExtendedFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentExtendedBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        defaultColor = requireContext().getColor(R.color.colorPrimaryDark);

        handler = new Handler();

        (controller = MainActivity.getController()).registerCallback(callback = new MediaController.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackState state) { setPlayback(state); }

            @Override
            public void onMetadataChanged(MediaMetadata metadata) { setMetadata(metadata); }
        });

        binding.tvSongTitle.setSelected(true);
        binding.tvSongArtist.setSelected(true);

        setPlayback(controller.getPlaybackState());
        setMetadata(controller.getMetadata());

        binding.btnSkipPrev.setOnClickListener(v -> controller.getTransportControls().skipToPrevious());
        binding.btnPlayPause.setOnClickListener(v -> {
            if (QueueUtil.queue.getValue().size() == 0) {
                QueueUtil.queue.setValue(LocalFiles.listSongsByName(requireContext()));
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.ACTION_QUEUE_POSITION, 0);
                controller.sendCommand(Constants.ACTION_QUEUE_POSITION, bundle, null);
            } else if (controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else {
                controller.getTransportControls().play();
            }
        });
        binding.btnSkipNext.setOnClickListener(v -> controller.getTransportControls().skipToNext());
        binding.btnRepeat.setOnClickListener(v -> {
            if (controller.getPlaybackState().getExtras().containsKey(Constants.LOOP_EXTRA)) {
                if (controller.getPlaybackState().getExtras().containsKey(Constants.LOOP_EXTRA)) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.LOOP_EXTRA, !controller.getPlaybackState().getExtras().getBoolean(Constants.LOOP_EXTRA));
                    controller.sendCommand(Constants.LOOP_EXTRA, bundle, null);
                }
            }
        });

        binding.sbSongProgress.setMax(1000);
        binding.sbSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                controller.getTransportControls().seekTo((long)((double)seekBar.getProgress() * duration / 1000));
            }
        });
        binding.tvTimeElapsed.setText(Utilities.formatTime(0));
        binding.tvTotalTime.setText(Utilities.formatTime(0));

        setMetadata(controller.getMetadata());
        setPlayback(controller.getPlaybackState());

        BottomSheetBehavior.from(((MainActivity)requireActivity()).binding.extendedFragment).addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (!isUp) {
                        // show the info fragment
                        getChildFragmentManager().beginTransaction().replace(R.id.infoFragment, ExtendedInfoFragment.newInstance()).commitNow();
                        requireActivity().getWindow().setStatusBarColor(defaultColor);
                        binding.infoFragment.setAlpha(1);
                        isUp = true;
                    }
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (isUp) {
                        // show the controller fragment
                        getChildFragmentManager().beginTransaction().replace(R.id.infoFragment, ControllerFragment.newInstance()).replace(R.id.layoutFragment, ExtendedSongFragment.newInstance()).commitNow();
                        requireActivity().getWindow().setStatusBarColor(requireActivity().getColor(R.color.colorPrimaryDark));
                        binding.infoFragment.setAlpha(1);
                        isUp = false;
                    }
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

        getChildFragmentManager().beginTransaction().replace(R.id.infoFragment, ControllerFragment.newInstance()).replace(R.id.layoutFragment, ExtendedSongFragment.newInstance()).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldSeek) {
            setPlayback(controller.getPlaybackState());
            handler.post(runnable);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setPlayback(PlaybackState state) {
        if (state == null) {
            binding.btnPlayPause.setBackgroundResource(R.drawable.play);
            binding.btnRepeat.setBackgroundResource(R.drawable.repeat);
        } else {
            currentPosition = state.getPosition();
            if (state.getState() == PlaybackState.STATE_PLAYING) {
                binding.btnPlayPause.setBackgroundResource(R.drawable.pause);
                updateSeekBar();
                shouldSeek = true;
            } else {
                binding.btnPlayPause.setBackgroundResource(R.drawable.play);
                shouldSeek = false;
            }
            binding.btnRepeat.setBackgroundResource(state.getExtras().getBoolean(Constants.LOOP_EXTRA) ? R.drawable.repeat_one : R.drawable.repeat);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setMetadata(@Nullable MediaMetadata metadata) {
        if (metadata != null) {
            MediaDescription description = metadata.getDescription();
            if (description != null) {
                binding.tvSongTitle.setText(description.getTitle());
                binding.tvSongArtist.setText(description.getSubtitle());
                binding.tvTimeElapsed.setText(Utilities.formatTime(0));
                binding.tvTotalTime.setText(Utilities.formatTime(duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)));
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(description.getIconUri()));
                    } catch (FileNotFoundException e) {
                        return null;
                    }
                }).thenAccept(bitmap -> {
                    if (bitmap != null) {
                        Palette.from(bitmap).generate(palette -> {
                            int colorDominant = palette.getDominantColor(Color.WHITE);
                            defaultColor = colorDominant;
                            if (isUp) {
                                requireActivity().getWindow().setStatusBarColor(defaultColor);
                            }
                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{colorDominant, Color.BLACK});
                            binding.extendedFragment.setBackground(gradientDrawable);
                            ColorUtil.setColor(defaultColor);
                        });
                    } else {
                        int colorTop = ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.3F);
                        defaultColor = colorTop;
                        if (isUp) {
                            requireActivity().getWindow().setStatusBarColor(defaultColor);
                        }
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{colorTop, Color.BLACK});
                        binding.extendedFragment.setBackground(gradientDrawable);
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
        }
    }

    /**
     * This function updates the progress bar and the elapsed time text.
     */
    @SuppressWarnings("ConstantConditions")
    private void updateSeekBar() {
        handler.postDelayed(runnable = () -> {
            binding.sbSongProgress.setProgress((int)((currentPosition / duration) * 1000));
            binding.tvTimeElapsed.setText(Utilities.formatTime((long)currentPosition));
            currentPosition += 250;
            handler.removeCallbacks(runnable);
            if (controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                updateSeekBar();
            }
        }, 250);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (callback != null) {
            controller.unregisterCallback(callback);
        }
    }
}
