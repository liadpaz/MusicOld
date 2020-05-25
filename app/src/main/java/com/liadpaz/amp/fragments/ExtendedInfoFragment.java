package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentExtendedInfoBinding;

public class ExtendedInfoFragment extends Fragment {
    private boolean isShowingQueue = false;

    private FragmentExtendedInfoBinding binding;

    public ExtendedInfoFragment() { }

    public static ExtendedInfoFragment newInstance() { return new ExtendedInfoFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentExtendedInfoBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnQueue.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.layoutFragment, isShowingQueue ? ExtendedSongFragment.newInstance() : CurrentQueueFragment.newInstance()).commit();
            v.setBackgroundResource(isShowingQueue ? R.drawable.queue_music_not_shown : R.drawable.queue_music_shown);
            isShowingQueue = !isShowingQueue;
        });
    }
}
