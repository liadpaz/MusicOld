package com.liadpaz.amp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.ItemAlbumBinding;
import com.liadpaz.amp.utils.Album;
import com.liadpaz.amp.utils.Utilities;

import java.util.List;

public class AlbumsListAdapter extends ArrayAdapter<Album> {

    public AlbumsListAdapter(@NonNull Context context, @NonNull List<Album> albums) {
        super(context, R.layout.item_album, albums);
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ItemAlbumBinding binding;
        if (convertView == null) {
            binding = ItemAlbumBinding.inflate(LayoutInflater.from(getContext()), parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ItemAlbumBinding)convertView.getTag();
        }

        Album album = getItem(position);

        Glide.with(getContext()).load(Utilities.getCoverUri(album.songs.get(0))).into(binding.ivAlbumCover);
        binding.tvAlbumName.setText(album.name);
        binding.tvAlbumArtist.setText(album.artist);

        return convertView;
    }
}
