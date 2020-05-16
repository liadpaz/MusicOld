package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentSongsListBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;

public class SongsListFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "SONGS_LIST_FRAGMENT";

    private FragmentSongsListBinding binding;

    public SongsListFragment() { }

    @SuppressWarnings("unused")
    @NonNull
    public static SongsListFragment newInstance() {
        return new SongsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentSongsListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SongsListAdapter adapter = new SongsListAdapter(getContext(), LocalFiles.listSongs(getContext()));

        ListView lvSongs;
        (lvSongs = binding.lvSongs).setAdapter(adapter);

        lvSongs.setOnItemClickListener((parent, view1, position, id) -> {
            LocalFiles.setQueue(LocalFiles.listSongs(getContext()));
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.ACTION_QUEUE_POSITION, position);
            MainActivity.getController().sendCommand(Constants.ACTION_SET_QUEUE, bundle, null);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater()
                     .inflate(R.menu.menu_song, menu);
    }
}
