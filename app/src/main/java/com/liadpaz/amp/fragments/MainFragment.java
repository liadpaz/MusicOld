package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    public MainFragment() { }

    @NonNull
    public static MainFragment newInstance() { return new MainFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentMainBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)requireActivity()).setSupportActionBar(binding.toolBarMain);

        getChildFragmentManager().beginTransaction().replace(R.id.viewpagerFragment, ViewPagerFragment.newInstance()).commit();
    }
}
